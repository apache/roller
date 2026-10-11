/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * What an uploaded file is stored as, and how it comes back.
 *
 * <p>The type a client puts on an upload states what the sender meant to send,
 * not what the bytes are, and the browser acts on whatever Roller repeats back.
 * These cases fix both ends of that: which type is kept, and which types are
 * allowed to render as a document rather than download.
 */
public class MediaTypePolicyTest {

    // ----------------------------------------------------------------- //
    // Stored type
    // ----------------------------------------------------------------- //

    /** The file name decides, so a declaration cannot contradict it. */
    @Test
    public void theFileNameDecidesTheStoredType() {
        assertEquals("image/jpeg",
                MediaTypePolicy.storedTypeFor("holiday.jpg", "text/html",
                        MediaTypePolicyTest::mimeTypeFor),
                "a declared type overrode the file name");
        assertEquals("image/png",
                MediaTypePolicy.storedTypeFor("diagram.png", "image/svg+xml",
                        MediaTypePolicyTest::mimeTypeFor));
        assertEquals("image/gif",
                MediaTypePolicy.storedTypeFor("loop.GIF", "application/xhtml+xml",
                        MediaTypePolicyTest::mimeTypeFor),
                "the extension must be matched regardless of case");
    }

    /** Where the name says nothing, an executable declaration is still refused. */
    @Test
    public void anExecutableDeclarationIsNeverAdopted() {
        for (String active : new String[]{
                "text/html", "text/html; charset=utf-8", "application/xhtml+xml",
                "image/svg+xml", "application/xml", "text/javascript",
                "application/javascript", "text/xsl", "something/custom+xml"}) {
            assertEquals(MediaTypePolicy.DEFAULT_TYPE,
                    MediaTypePolicy.storedTypeFor("payload.unknownext", active,
                            MediaTypePolicyTest::mimeTypeFor),
                    "adopted an executable declared type: " + active);
        }
    }

    /** A harmless declaration is still useful where the name is opaque. */
    @Test
    public void aHarmlessDeclarationIsUsedWhenTheNameIsOpaque() {
        assertEquals("application/zip",
                MediaTypePolicy.storedTypeFor("bundle.unknownext", "application/zip",
                        MediaTypePolicyTest::mimeTypeFor));
        assertEquals(MediaTypePolicy.DEFAULT_TYPE,
                MediaTypePolicy.storedTypeFor("bundle.unknownext", null,
                        MediaTypePolicyTest::mimeTypeFor));
        assertEquals(MediaTypePolicy.DEFAULT_TYPE,
                MediaTypePolicy.storedTypeFor(null, null,
                        MediaTypePolicyTest::mimeTypeFor));
    }

    // ----------------------------------------------------------------- //
    // Inline policy
    // ----------------------------------------------------------------- //

    @Test
    public void passiveFormatsRenderInline() {
        for (String inline : new String[]{
                "image/jpeg", "image/jpg", "image/png", "image/x-png",
                "image/apng", "image/avif", "image/gif", "image/webp",
                "application/pdf", "text/plain", "audio/mpeg", "video/mp4",
                "image/png; charset=binary"}) {
            assertTrue(MediaTypePolicy.isInlineSafe(inline),
                    "expected to render inline: " + inline);
        }
    }

    @Test
    public void formatsThatCanCarryScriptDoNot() {
        for (String blocked : new String[]{
                "image/svg+xml", "text/html", "application/xhtml+xml",
                "text/xml", "application/javascript",
                "application/zip", null, ""}) {
            assertFalse(MediaTypePolicy.isInlineSafe(blocked),
                    "expected not to render inline: " + blocked);
        }
    }

    // ----------------------------------------------------------------- //
    // Response headers
    // ----------------------------------------------------------------- //

    @Test
    public void everyResponseDeclaresNosniff() {
        for (String type : new String[]{"image/png", "text/html", null}) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            MediaTypePolicy.applyResponseHeaders(response, type, "f.bin");
            verify(response).setHeader("X-Content-Type-Options", "nosniff");
        }
    }

    @Test
    public void anInlineTypeKeepsItsTypeAndIsNotAnAttachment() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        MediaTypePolicy.applyResponseHeaders(response, "image/png", "diagram.png");
        verify(response).setContentType("image/png");
        verify(response, never()).setHeader(eq("Content-Disposition"), anyString());
    }

    @Test
    public void anythingElseIsSentAsAnAttachment() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        MediaTypePolicy.applyResponseHeaders(response, "text/html", "page.html");
        verify(response).setContentType(MediaTypePolicy.DEFAULT_TYPE);
        verify(response).setHeader("Content-Disposition",
                "attachment; filename=\"page.html\"");
    }

    /** The name is placed inside a header, so it cannot be allowed to leave it. */
    @Test
    public void theAttachmentNameCannotBreakOutOfTheHeader() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        MediaTypePolicy.applyResponseHeaders(response, "text/html",
                "evil\r\nSet-Cookie: a=b\".html");
        verify(response).setHeader("Content-Disposition",
                "attachment; filename=\"evilSet-Cookie: a=b.html\"");
    }

    @Test
    public void servletMappingsAreUsedCaseInsensitively() {
        assertEquals("application/pdf", MediaTypePolicy.typeFromName(
                "report.PDF", MediaTypePolicyTest::mimeTypeFor));
        assertEquals("video/mp4", MediaTypePolicy.typeFromName(
                "clip.MP4", MediaTypePolicyTest::mimeTypeFor));
        assertEquals(null, MediaTypePolicy.typeFromName(
                "payload.unmapped", MediaTypePolicyTest::mimeTypeFor));
    }

    @Test
    public void unknownAndActiveTypesUseTheDownloadType() {
        assertEquals(MediaTypePolicy.DEFAULT_TYPE,
                MediaTypePolicy.responseTypeFor(null));
        assertEquals(MediaTypePolicy.DEFAULT_TYPE,
                MediaTypePolicy.responseTypeFor("image/svg+xml"));
        assertEquals("text/plain",
                MediaTypePolicy.responseTypeFor("text/plain; charset=utf-8"));
    }

    private static String mimeTypeFor(String fileName) {
        if (fileName.endsWith(".jpg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".mp4")) return "video/mp4";
        return null;
    }

    // ----------------------------------------------------------------- //
    // The callers actually use it
    // ----------------------------------------------------------------- //

    private String source(String relativePath) throws Exception {
        Path path = Paths.get("src", "main", "java");
        for (String segment : relativePath.split("/")) {
            path = path.resolve(segment);
        }
        assertTrue(Files.isReadable(path),
                "cannot read " + path.toAbsolutePath() + " (run from the app module)");
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * The serving path must not set a type of its own, or the headers above
     * are decided somewhere this test cannot see.
     */
    @Test
    public void theServingPathGoesThroughThePolicy() throws Exception {
        String servlet = source("org/apache/roller/weblogger/ui/rendering/"
                + "servlets/MediaResourceServlet.java");
        assertTrue(servlet.contains("MediaTypePolicy.applyResponseHeaders"),
                "MediaResourceServlet must apply the policy to its response");
        assertFalse(servlet.contains("response.setContentType("),
                "MediaResourceServlet must not set a content type directly");
    }

    /**
     * Uploaded media is also reachable through the two resource servlets, which
     * serve theme resources from the same method. Only the uploaded-media
     * branch takes the media policy — applying it to theme resources would send
     * every stylesheet as a download — but both branches must refuse sniffing.
     */
    @Test
    public void theResourceServletsCoverTheirUploadedMediaBranch() throws Exception {
        for (String name : new String[]{"ResourceServlet", "PreviewResourceServlet"}) {
            String servlet = source("org/apache/roller/weblogger/ui/rendering/"
                    + "servlets/" + name + ".java");
            assertTrue(servlet.contains("MediaTypePolicy.applyResponseHeaders"),
                    name + " must apply the media policy to uploaded media");
            assertTrue(servlet.contains("fromUploadedMedia"),
                    name + " must distinguish uploaded media from theme resources");
            assertTrue(servlet.contains("X-Content-Type-Options"),
                    name + " must refuse sniffing on the theme branch too");
        }
    }

    /** Each upload path must derive the stored type rather than take it. */
    @Test
    public void everyUploadPathGoesThroughThePolicy() throws Exception {
        String[][] callers = {
                {"org/apache/roller/weblogger/ui/struts2/editor/MediaFileAdd.java",
                        "this.uploadedFilesContentType[i]"},
                {"org/apache/roller/weblogger/webservices/atomprotocol/"
                        + "MediaCollection.java", "setContentType(contentType)"},
                {"org/apache/roller/weblogger/webservices/xmlrpc/"
                        + "MetaWeblogAPIHandler.java", "setContentType(type)"},
                // Replacing an existing file's body is an upload too.
                {"org/apache/roller/weblogger/ui/struts2/editor/MediaFileEdit.java",
                        "this.uploadedFileContentType"},
        };
        for (String[] caller : callers) {
            String src = source(caller[0]);
            assertTrue(src.contains("MediaTypePolicy.storedTypeFor"),
                    caller[0] + " must derive the stored type through the policy");
            assertTrue(src.contains("getFileContentManager().canSave"),
                    caller[0] + " must validate the declared type before deriving it");
            assertFalse(src.contains("setContentType(" + caller[1] + ")"),
                    caller[0] + " still stores the client's declared type directly");
        }
    }

    @Test
    public void replacementRoutesUseTheReplacementName() throws Exception {
        String editor = source("org/apache/roller/weblogger/ui/struts2/editor/"
                + "MediaFileEdit.java");
        assertTrue(editor.contains("storedTypeFor(\n                            this.uploadedFileName"));

        String atom = source("org/apache/roller/weblogger/webservices/atomprotocol/"
                + "MediaCollection.java");
        assertTrue(atom.contains("storedTypeFor(\n                            replacementName"));
        assertTrue(atom.contains("createMediaResource(mf, response)"),
                "Atom media reads must apply the response policy");
    }
}
