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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rometools.propono.atom.server.AtomHandler;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RollerAtomServletTest {

    private static final String ENTRY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
            + "<title>Hello</title>"
            + "<id>urn:uuid:00000000-0000-0000-0000-000000000001</id>"
            + "<updated>2026-01-01T00:00:00Z</updated>"
            + "<content type=\"text\">Body</content>"
            + "</entry>";

    private static final String ENTRY_WITH_DOCTYPE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE entry>\n"
            + "<entry xmlns=\"http://www.w3.org/2005/Atom\"><title>Hello</title></entry>";

    private MockedStatic<WebloggerRuntimeConfig> config;
    private RecordingServlet servlet;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws IOException {
        config = mockStatic(WebloggerRuntimeConfig.class);
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                .thenReturn(true);
        servlet = new RecordingServlet();
        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @AfterEach
    void tearDown() {
        config.close();
    }

    @Test
    void disabledServiceAnswersNotFoundWithoutReadingTheBody() throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                .thenReturn(false);
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(request, never()).getInputStream();
        assertNull(servlet.forwarded);
    }

    @Test
    void disabledServiceAlsoRefusesReads() throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                .thenReturn(false);

        servlet.service(request("GET", "/blog/entries", null, ""), response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertNull(servlet.forwarded);
    }

    @Test
    void readsPassThroughUnchanged() throws Exception {
        HttpServletRequest request = request("GET", "/blog/entries", null, "");

        servlet.service(request, response);

        assertSame(request, servlet.forwarded);
    }

    @Test
    void wellFormedEntryIsForwardedWithTheSameBody() throws Exception {
        servlet.service(request("POST", "/blog/entries", "application/atom+xml;type=entry", ENTRY), response);

        assertNotNull(servlet.forwarded);
        assertArrayEquals(ENTRY.getBytes(StandardCharsets.UTF_8), servlet.forwardedBody);
    }

    @Test
    void postedEntryWithDoctypeIsRefused() throws Exception {
        servlet.service(request("POST", "/blog/entries", "application/atom+xml", ENTRY_WITH_DOCTYPE), response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(servlet.forwarded);
    }

    @Test
    void entryUpdateWithDoctypeIsRefusedWhateverItsContentType() throws Exception {
        servlet.service(request("PUT", "/blog/entry/abc", "text/plain", ENTRY_WITH_DOCTYPE), response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(servlet.forwarded);
    }

    @Test
    void mediaUploadIsForwardedWithoutParsing() throws Exception {
        HttpServletRequest request = request("POST", "/blog/resources", "image/png", "not xml");

        servlet.service(request, response);

        assertSame(request, servlet.forwarded);
        verify(request, never()).getInputStream();
    }

    @Test
    void mediaUpdateIsForwardedWithoutParsing() throws Exception {
        HttpServletRequest request = request("PUT", "/blog/resource/photo.png", "image/png", "not xml");

        servlet.service(request, response);

        assertSame(request, servlet.forwarded);
        verify(request, never()).getInputStream();
    }

    @Test
    void oversizedEntryIsRefused() throws Exception {
        byte[] big = new byte[RollerAtomServlet.MAX_ENTRY_BYTES + 1];
        Arrays.fill(big, (byte) ' ');
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", "");
        when(request.getInputStream()).thenReturn(stream(big));

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertNull(servlet.forwarded);
    }

    @Test
    void unauthenticatedEntryPostIsRefusedWithoutReadingTheBody() throws Exception {
        servlet.userName = null;
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(request, never()).getInputStream();
        assertNull(servlet.forwarded);
    }

    @Test
    void unauthenticatedEntryUpdateIsRefusedWithoutReadingTheBody() throws Exception {
        servlet.userName = null;
        HttpServletRequest request = request("PUT", "/blog/entry/abc", "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(request, never()).getInputStream();
        assertNull(servlet.forwarded);
    }

    @Test
    void authenticatedHandlerIsReusedByTheFactory() throws Exception {
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(request).setAttribute(RollerAtomServlet.HANDLER_ATTRIBUTE, servlet.handler);
        when(request.getAttribute(RollerAtomServlet.HANDLER_ATTRIBUTE)).thenReturn(servlet.handler);
        assertSame(servlet.handler, new RollerAtomHandlerFactory().newAtomHandler(request, response));
        verify(request).removeAttribute(RollerAtomServlet.HANDLER_ATTRIBUTE);
    }

    @Test
    void entryPathsMatchTheHandler() {
        assertTrue(RollerAtomHandler.isEntryPath("/blog/entry/abc"));
        assertTrue(RollerAtomHandler.isEntryPath("/blog/resource/photo.png.media-link"));
        assertFalse(RollerAtomHandler.isEntryPath("/blog/resource/photo.png"));
        assertFalse(RollerAtomHandler.isEntryPath("/blog/entries"));
        assertFalse(RollerAtomHandler.isEntryPath(null));
    }

    private static HttpServletRequest request(String method, String pathInfo,
                                              String contentType, String body) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getContentType()).thenReturn(contentType);
        when(request.getInputStream()).thenReturn(stream(body.getBytes(StandardCharsets.UTF_8)));
        return request;
    }

    private static ServletInputStream stream(byte[] bytes) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public int read() {
                return in.read();
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return in.read(b, off, len);
            }

            @Override
            public boolean isFinished() {
                return in.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /** Records what would have been handed to the Propono servlet. */
    private static final class RecordingServlet extends RollerAtomServlet {
        private static final long serialVersionUID = 1L;
        HttpServletRequest forwarded;
        byte[] forwardedBody;
        String userName = "alice";
        AtomHandler handler;

        @Override
        protected AtomHandler createHandler(HttpServletRequest req, HttpServletResponse res) {
            handler = mock(AtomHandler.class);
            when(handler.getAuthenticatedUsername()).thenReturn(userName);
            return handler;
        }

        @Override
        protected void forward(HttpServletRequest req, HttpServletResponse res) throws IOException {
            forwarded = req;
            if (req instanceof BufferedBodyRequest) {
                forwardedBody = req.getInputStream().readAllBytes();
            }
        }
    }
}
