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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @BeforeEach
    void setUp() throws IOException {
        config = mockStatic(WebloggerRuntimeConfig.class);
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                .thenReturn(true);
        servlet = new RecordingServlet();
        response = response();
    }

    @AfterEach
    void tearDown() {
        config.close();
    }

    @Test
    void disabledServiceAnswersNotFoundForWritesAndReads() throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                .thenReturn(false);
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(request, never()).getInputStream();
        assertNull(servlet.handler);

        HttpServletResponse readResponse = response();
        servlet.service(request("GET", "/blog/entries", null, ""), readResponse);

        verify(readResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertNull(servlet.handler);
    }

    @Test
    void readsAreDispatchedToTheHandler() throws Exception {
        servlet.service(request("GET", "/blog/entries", null, ""), response);

        verify(servlet.handler).getCollection(any());
    }

    @Test
    void wellFormedEntryIsPostedWithTheSameBody() throws Exception {
        servlet.service(request("POST", "/blog/entries", "application/atom+xml;type=entry", ENTRY), response);

        assertEquals("Hello", servlet.postedEntry.getTitle());
        assertArrayEquals(ENTRY.getBytes(StandardCharsets.UTF_8), servlet.body);
    }

    @Test
    void postedEntryWithDoctypeIsRefused() throws Exception {
        servlet.service(request("POST", "/blog/entries", "application/atom+xml", ENTRY_WITH_DOCTYPE), response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(servlet.body);
    }

    @Test
    void entryUpdateWithDoctypeIsRefusedWhateverItsContentType() throws Exception {
        servlet.service(request("PUT", "/blog/entry/abc", "text/plain", ENTRY_WITH_DOCTYPE), response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(servlet.body);
    }

    @Test
    void mediaUploadsBypassTheEntryLimit() throws Exception {
        mediaUploadBypassesTheEntryLimit("POST", "/blog/resources");
        servlet.body = null;
        mediaUploadBypassesTheEntryLimit("PUT", "/blog/resource/image.png");
    }

    private void mediaUploadBypassesTheEntryLimit(String method, String path) throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getProperty(RollerAtomServlet.MAX_ENTRY_SIZE_PROPERTY))
                .thenReturn("1");
        HttpServletRequest request = request(method, path, "image/png", "not xml");

        servlet.service(request, response);

        assertArrayEquals("not xml".getBytes(StandardCharsets.UTF_8), servlet.body);
        // The servlet does not buffer media; the handler reads the request stream
        verify(request, times(1)).getInputStream();
    }

    @Test
    void postWithoutContentTypeIsUnsupportedMediaType() throws Exception {
        HttpServletRequest request = request("POST", "/blog/resources", null, "data");

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(request, never()).getInputStream();
        assertNull(servlet.body);
    }

    @Test
    void oversizedEntryIsRefused() throws Exception {
        byte[] big = new byte[RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES + 100];
        Arrays.fill(big, (byte) ' ');
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", "");
        ServletInputStream input = stream(big);
        when(request.getInputStream()).thenReturn(input);

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertNull(servlet.body);
        assertEquals(99, input.available(), "Read only one byte past the entry limit");
    }

    @Test
    void entryExactlyAtTheDefaultLimitIsAccepted() throws Exception {
        String body = ENTRY + " ".repeat(
                RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES - ENTRY.getBytes(StandardCharsets.UTF_8).length);

        servlet.service(request("POST", "/blog/entries", "application/atom+xml", body), response);

        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), servlet.body);
    }

    @Test
    void configuredLimitAppliesToEntryPostsAndUpdates() throws Exception {
        configuredLimitCountsUtf8BytesAndChangesOnTheNextRequest("POST", "/blog/entries");
        servlet.body = null;
        clearInvocations(response);
        configuredLimitCountsUtf8BytesAndChangesOnTheNextRequest("PUT", "/blog/entry/abc");
    }

    private void configuredLimitCountsUtf8BytesAndChangesOnTheNextRequest(String method, String path) throws Exception {
        String body = ENTRY.replace("Hello", "Hello 世界");
        int size = body.getBytes(StandardCharsets.UTF_8).length;
        config.when(() -> WebloggerRuntimeConfig.getProperty(RollerAtomServlet.MAX_ENTRY_SIZE_PROPERTY))
                .thenReturn(Integer.toString(size - 1), Integer.toString(size));

        servlet.service(request(method, path, "application/atom+xml", body), response);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertNull(servlet.body);

        servlet.service(request(method, path, "application/atom+xml", body), response);

        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), servlet.body);
    }

    @Test
    void missingOrInvalidLimitsUseTheDefault() throws Exception {
        for (String value : new String[] {null, "", "0", "-1", "2147483647", "2147483648", "not a number"}) {
            clearInvocations(response);
            missingOrInvalidLimitUsesTheDefault(value);
        }
    }

    private void missingOrInvalidLimitUsesTheDefault(String value) throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getProperty(RollerAtomServlet.MAX_ENTRY_SIZE_PROPERTY))
                .thenReturn(value);
        String body = ENTRY + " ".repeat(
                RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES - ENTRY.getBytes(StandardCharsets.UTF_8).length);
        servlet.service(request("POST", "/blog/entries", "application/atom+xml", body), response);
        assertEquals(RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES, servlet.body.length);
        servlet.body = null;
        byte[] big = new byte[RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES + 1];
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", "");
        when(request.getInputStream()).thenReturn(stream(big));

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertNull(servlet.body);
    }

    @Test
    void configuredLimitCanBeRaisedAboveTheDefault() throws Exception {
        String body = ENTRY + " ".repeat(RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES);
        config.when(() -> WebloggerRuntimeConfig.getProperty(RollerAtomServlet.MAX_ENTRY_SIZE_PROPERTY))
                .thenReturn(Integer.toString(body.getBytes(StandardCharsets.UTF_8).length));

        servlet.service(request("POST", "/blog/entries", "application/atom+xml", body), response);

        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), servlet.body);
    }

    @Test
    void malformedEntryPostsAndUpdatesAreRefused() throws Exception {
        malformedEntryIsRefused("POST", "/blog/entries");
        clearInvocations(response);
        malformedEntryIsRefused("PUT", "/blog/entry/abc");
    }

    private void malformedEntryIsRefused(String method, String path) throws Exception {
        servlet.service(request(method, path, "application/atom+xml", "<entry><title></entry>"), response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(servlet.body);
    }

    @Test
    void unauthenticatedEntryPostsAndUpdatesDoNotReadTheBody() throws Exception {
        unauthenticatedEntryIsRefusedWithoutReadingTheBody("POST", "/blog/entries");
        clearInvocations(response);
        unauthenticatedEntryIsRefusedWithoutReadingTheBody("PUT", "/blog/entry/abc");
    }

    private void unauthenticatedEntryIsRefusedWithoutReadingTheBody(String method, String path) throws Exception {
        servlet.userName = null;
        HttpServletRequest request = request(method, path, "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(request, never()).getInputStream();
        assertNull(servlet.body);
    }

    private static HttpServletResponse response() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) {
                out.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
                throw new UnsupportedOperationException();
            }
        });
        return response;
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
            public int available() {
                return in.available();
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

    /** Records the request body and entry that reach the handler. */
    private static final class RecordingServlet extends RollerAtomServlet {
        private static final long serialVersionUID = 1L;
        String userName = "alice";
        RollerAtomHandler handler;
        AtomEntry postedEntry;
        byte[] body;

        @Override
        protected RollerAtomHandler createHandler(HttpServletRequest req, HttpServletResponse res) {
            handler = mock(RollerAtomHandler.class);
            try {
                when(handler.getAuthenticatedUsername()).thenReturn(userName);
                when(handler.isCollectionURI(any())).thenAnswer(call ->
                        call.<AtomRequest>getArgument(0).getPathInfo().matches("/[^/]+/(entries|resources).*"));
                when(handler.isEntryURI(any())).thenAnswer(call ->
                        RollerAtomHandler.isEntryPath(call.<AtomRequest>getArgument(0).getPathInfo()));
                when(handler.isMediaEditURI(any())).thenAnswer(call ->
                        call.<AtomRequest>getArgument(0).getPathInfo().matches("/[^/]+/resource/.*"));
                when(handler.getCollection(any())).thenReturn(new AtomFeed());
                when(handler.postEntry(any(), any())).thenAnswer(call -> {
                    record(call.getArgument(0));
                    postedEntry = call.getArgument(1);
                    return new AtomEntry();
                });
                when(handler.postMedia(any(), any())).thenAnswer(call -> {
                    record(call.getArgument(0));
                    return new AtomEntry();
                });
                doAnswer(call -> {
                    record(call.getArgument(0));
                    return null;
                }).when(handler).putEntry(any(), any());
                doAnswer(call -> {
                    record(call.getArgument(0));
                    return null;
                }).when(handler).putMedia(any());
            } catch (AtomException e) {
                throw new IllegalStateException(e);
            }
            return handler;
        }

        private void record(AtomRequest areq) throws IOException {
            body = areq.getInputStream().readAllBytes();
        }
    }
}
