/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.core.filters;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class XmlRpcBodyLimitTest {

    private static final int MIB = 1024 * 1024;

    @Test
    void rejectsAnOversizedDeclaredBodyWithoutReadingIt() throws Exception {
        HttpServletRequest request = post(MIB + 1);
        HttpServletResponse response = response();
        FilterChain chain = mock(FilterChain.class);

        filter(request, response, chain, null);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        verify(request, never()).getInputStream();
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsAnOversizedBodyOfUnknownLengthBeforeDelegation() throws Exception {
        HttpServletRequest request = post(-1);
        when(request.getInputStream()).thenReturn(stream(new byte[MIB + 1]));
        HttpServletResponse response = response();
        FilterChain chain = mock(FilterChain.class);

        filter(request, response, chain, null);

        verify(response).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        verifyNoInteractions(chain);
    }

    @Test
    void passesAnOrdinaryBodyThroughUnchanged() throws Exception {
        byte[] body = "<?xml version=\"1.0\"?><methodCall><methodName>blogger.getUsersBlogs</methodName></methodCall>"
                .getBytes(StandardCharsets.UTF_8);
        assertPassesUnchanged(body, null);
    }

    @Test
    void allowsBase64AndEnvelopeForTheDefaultUploadSize() throws Exception {
        assertPassesUnchanged(new byte[2_900_000], "2.00");
    }

    private static void assertPassesUnchanged(byte[] body, String uploadMegabytes) throws Exception {
        HttpServletRequest request = post(-1);
        when(request.getInputStream()).thenReturn(stream(body));
        HttpServletResponse response = response();
        FilterChain chain = mock(FilterChain.class);
        doAnswer(call -> {
            HttpServletRequest delegated = call.getArgument(0);
            assertArrayEquals(body, delegated.getInputStream().readAllBytes());
            return null;
        }).when(chain).doFilter(any(), eq(response));

        filter(request, response, chain, uploadMegabytes);

        verify(chain).doFilter(any(), eq(response));
        verify(response, never()).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    }

    private static void filter(HttpServletRequest request, HttpServletResponse response,
                               FilterChain chain, String uploadMegabytes) throws Exception {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc"))
                    .thenReturn(true);
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled"))
                    .thenReturn(uploadMegabytes != null);
            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"))
                    .thenReturn(uploadMegabytes);
            new XmlRpcEnabledFilter().doFilter(request, response, chain);
        }
    }

    private static HttpServletRequest post(long length) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentLengthLong()).thenReturn(length);
        return request;
    }

    private static HttpServletResponse response() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        return response;
    }

    private static ServletInputStream stream(byte[] bytes) {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override public int read() { return input.read(); }
            @Override public int read(byte[] buffer, int offset, int length) {
                return input.read(buffer, offset, length);
            }
            @Override public boolean isFinished() { return input.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) {
                throw new IllegalStateException("Synchronous test request");
            }
        };
    }
}
