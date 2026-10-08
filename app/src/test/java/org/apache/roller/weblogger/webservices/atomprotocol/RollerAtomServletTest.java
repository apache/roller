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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rometools.propono.atom.server.AtomHandler;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.util.SecureXmlParsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.clearInvocations;
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
    void disabledServiceAnswersNotFoundForWritesAndReads() throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                .thenReturn(false);
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", ENTRY);

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(request, never()).getInputStream();
        assertNull(servlet.forwarded);

        HttpServletResponse readResponse = mock(HttpServletResponse.class);
        when(readResponse.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        servlet.service(request("GET", "/blog/entries", null, ""), readResponse);

        verify(readResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
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
    void mediaUploadsBypassTheEntryLimit() throws Exception {
        mediaUploadIsForwardedWithoutApplyingTheEntryLimit("POST", "/blog/resources");
        mediaUploadIsForwardedWithoutApplyingTheEntryLimit("PUT", "/blog/resources/image.png");
    }

    private void mediaUploadIsForwardedWithoutApplyingTheEntryLimit(String method, String path) throws Exception {
        config.when(() -> WebloggerRuntimeConfig.getProperty(RollerAtomServlet.MAX_ENTRY_SIZE_PROPERTY))
                .thenReturn("1");
        HttpServletRequest request = request(method, path, "image/png", "not xml");

        servlet.service(request, response);

        assertSame(request, servlet.forwarded);
        verify(request, never()).getInputStream();
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
        assertNull(servlet.forwarded);
        assertEquals(99, input.available(), "Read only one byte past the entry limit");
    }

    @Test
    void entryExactlyAtTheDefaultLimitIsAccepted() throws Exception {
        String body = ENTRY + " ".repeat(
                RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES - ENTRY.getBytes(StandardCharsets.UTF_8).length);

        servlet.service(request("POST", "/blog/entries", "application/atom+xml", body), response);

        assertNotNull(servlet.forwarded);
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), servlet.forwardedBody);
    }

    @Test
    void configuredLimitAppliesToEntryPostsAndUpdates() throws Exception {
        configuredLimitCountsUtf8BytesAndChangesOnTheNextRequest("POST", "/blog/entries");
        servlet.forwarded = null;
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
        assertNull(servlet.forwarded);

        servlet.service(request(method, path, "application/atom+xml", body), response);

        assertNotNull(servlet.forwarded);
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), servlet.forwardedBody);
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
        assertNotNull(servlet.forwarded);
        assertEquals(RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES, servlet.forwardedBody.length);
        servlet.forwarded = null;
        byte[] big = new byte[RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES + 1];
        HttpServletRequest request = request("POST", "/blog/entries", "application/atom+xml", "");
        when(request.getInputStream()).thenReturn(stream(big));

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertNull(servlet.forwarded);
    }

    @Test
    void configuredLimitCanBeRaisedAboveTheDefault() throws Exception {
        String body = ENTRY + " ".repeat(RollerAtomServlet.DEFAULT_MAX_ENTRY_BYTES);
        config.when(() -> WebloggerRuntimeConfig.getProperty(RollerAtomServlet.MAX_ENTRY_SIZE_PROPERTY))
                .thenReturn(Integer.toString(body.getBytes(StandardCharsets.UTF_8).length));

        servlet.service(request("POST", "/blog/entries", "application/atom+xml", body), response);

        assertNotNull(servlet.forwarded);
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
        assertNull(servlet.forwarded);
    }

    @Test
    void parserSetupFailureIsAServerError() throws Exception {
        try (MockedStatic<SecureXmlParsers> parsers = mockStatic(SecureXmlParsers.class)) {
            SAXParserFactory factory = mock(SAXParserFactory.class);
            parsers.when(SecureXmlParsers::newSAXParserFactory).thenReturn(factory);
            when(factory.newSAXParser()).thenThrow(new ParserConfigurationException("Cannot create parser"));

            assertThrows(ServletException.class, () -> servlet.service(
                    request("POST", "/blog/entries", "application/atom+xml", ENTRY), response));

            verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            assertNull(servlet.forwarded);
        }
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
