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
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MultipartSizeFilterTest {

    private static final long MIB = 1024L * 1024L;

    @Test
    void limitFollowsTheUploadSetting() {
        assertEquals(5 * 2 * MIB + MIB, limit("2.00"));
        assertEquals(5 * 50 * MIB + MIB, limit("50"));
        // unset or invalid settings fall back to the 2 MiB default
        assertEquals(5 * 2 * MIB + MIB, limit(null));
        assertEquals(5 * 2 * MIB + MIB, limit("lots"));
        assertEquals(5 * 2 * MIB + MIB, limit("-1"));
    }

    @Test
    void refusesADeclaredLengthOverTheLimit() throws Exception {
        HttpServletRequest request = multipart(11 * MIB + 1);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter(request, response, chain, "2.00");

        verify(response).sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        verify(request, never()).getInputStream();
        verifyNoInteractions(chain);
    }

    @Test
    void aRaisedUploadSettingAllowsALargerRequest() throws Exception {
        HttpServletRequest request = multipart(40 * MIB);
        when(request.getInputStream()).thenReturn(stream(new byte[16]));
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter(request, response, chain, "10");

        verify(chain).doFilter(any(), eq(response));
        verify(response, never()).sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    }

    @Test
    void stopsReadingABodyOfUnknownLengthPastTheLimit() throws Exception {
        HttpServletRequest request = multipart(-1);
        when(request.getInputStream()).thenReturn(stream(new byte[(int) (11 * MIB + 1)]));
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        doAnswer(call -> {
            HttpServletRequest delegated = call.getArgument(0);
            assertThrows(IOException.class, () -> delegated.getInputStream().readAllBytes());
            return null;
        }).when(chain).doFilter(any(), eq(response));

        filter(request, response, chain, "2.00");

        verify(chain).doFilter(any(), eq(response));
    }

    @Test
    void otherRequestsPassUntouched() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(request.getContentLengthLong()).thenReturn(100 * MIB);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter(request, response, chain, "2.00");

        verify(chain).doFilter(request, response);
    }

    private static long limit(String megabytes) {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"))
                    .thenReturn(megabytes);
            return MultipartSizeFilter.maxRequestBytes();
        }
    }

    private static void filter(HttpServletRequest request, HttpServletResponse response,
                               FilterChain chain, String megabytes) throws Exception {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"))
                    .thenReturn(megabytes);
            new MultipartSizeFilter().doFilter(request, response, chain);
        }
    }

    private static HttpServletRequest multipart(long length) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data; boundary=x");
        when(request.getContentLengthLong()).thenReturn(length);
        return request;
    }

    private static ServletInputStream stream(byte[] body) {
        ByteArrayInputStream input = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return input.read();
            }

            @Override
            public int read(byte[] buffer, int offset, int length) {
                return input.read(buffer, offset, length);
            }

            @Override
            public boolean isFinished() {
                return input.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // not used
            }
        };
    }
}
