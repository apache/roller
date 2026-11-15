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

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RollerXmlRpcServletTest {

    private static final int CAP = 4096;

    private static final String UNKNOWN_CALL =
            "<?xml version=\"1.0\"?><methodCall><methodName>blogger.unknown</methodName></methodCall>";

    /** The servlet with a small, fixed request cap. */
    private final RollerXmlRpcServlet servlet = new RollerXmlRpcServlet() {
        private static final long serialVersionUID = 1L;

        @Override
        int maxRequestBytes() {
            return CAP;
        }
    };

    private HttpServletRequest request;
    private HttpServletResponse response;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private final StringWriter text = new StringWriter();

    @BeforeEach
    public void setUp() throws IOException {
        servlet.init();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getContentLengthLong()).thenReturn(-1L);
        when(response.getWriter()).thenReturn(new PrintWriter(text, true));
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
            }

            @Override
            public void write(int b) {
                body.write(b);
            }
        });
    }

    private void send(byte[] content, String contentEncoding) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        when(request.getHeader("Content-Encoding")).thenReturn(contentEncoding);
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
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
            }

            @Override
            public int read() {
                return in.read();
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return in.read(b, off, len);
            }
        });
        servlet.doPost(request, response);
    }

    private static byte[] gzip(byte[] content) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(content);
        }
        return out.toByteArray();
    }

    private String responseBody() {
        return body.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void answersACallWithAnXmlRpcResponse() throws IOException {
        send(UNKNOWN_CALL.getBytes(StandardCharsets.UTF_8), null);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("text/xml;charset=UTF-8");
        assertTrue(responseBody().contains("No such handler: blogger.unknown"), responseBody());
    }

    @Test
    public void refusesADeclaredLengthOverTheCapWithoutReadingIt() throws IOException {
        when(request.getContentLengthLong()).thenReturn((long) CAP + 1);
        servlet.doPost(request, response);
        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        verify(request, never()).getInputStream();
    }

    @Test
    public void refusesABodyOverTheCap() throws IOException {
        send(new byte[CAP + 1], null);
        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertEquals(0, body.size());
    }

    @Test
    public void acceptsAGzipBody() throws IOException {
        send(gzip(UNKNOWN_CALL.getBytes(StandardCharsets.UTF_8)), "gzip");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseBody().contains("No such handler: blogger.unknown"), responseBody());
    }

    @Test
    public void capsTheDecompressedSizeOfAGzipBody() throws IOException {
        // small on the wire, far over the cap once decompressed
        byte[] compressed = gzip(new byte[CAP * 256]);
        assertTrue(compressed.length < CAP);
        send(compressed, "gzip");
        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertEquals(0, body.size());
    }

    @Test
    public void refusesAnInvalidGzipBody() throws IOException {
        send("not gzip".getBytes(StandardCharsets.UTF_8), "gzip");
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void refusesOtherContentEncodings() throws IOException {
        send(UNKNOWN_CALL.getBytes(StandardCharsets.UTF_8), "br");
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void capIsOneMebibyteWhileUploadsAreOff() {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")).thenReturn(false);
            assertEquals(RollerXmlRpcServlet.MIN_MAX_REQUEST_BYTES, new RollerXmlRpcServlet().maxRequestBytes());
        }
    }

    @Test
    public void capLeavesRoomForTheLargestUploadAsBase64() {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")).thenReturn(true);
            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize")).thenReturn("3.00");
            int cap = new RollerXmlRpcServlet().maxRequestBytes();
            long upload = 3L * 1024 * 1024;
            long base64WithLineBreaks = (upload + 2) / 3 * 4 * 78 / 76;
            assertTrue(cap >= base64WithLineBreaks + RollerXmlRpcServlet.ENVELOPE_BYTES, String.valueOf(cap));

            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize")).thenReturn("not a number");
            assertEquals(RollerXmlRpcServlet.MIN_MAX_REQUEST_BYTES, new RollerXmlRpcServlet().maxRequestBytes());
        }
    }

    @Test
    public void capStopsAt32MebibytesHoweverLargeAnUploadIsAllowed() {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")).thenReturn(true);
            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize")).thenReturn("100.00");
            assertEquals(RollerXmlRpcServlet.MAX_MAX_REQUEST_BYTES, new RollerXmlRpcServlet().maxRequestBytes());

            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize")).thenReturn("1e12");
            assertEquals(RollerXmlRpcServlet.MAX_MAX_REQUEST_BYTES, new RollerXmlRpcServlet().maxRequestBytes());
        }
    }
}
