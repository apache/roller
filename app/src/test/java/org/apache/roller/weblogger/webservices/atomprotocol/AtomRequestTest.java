/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link AtomRequest}, the lightweight request wrapper that
 * buffers the body and normalizes a null pathInfo to "".
 */
public class AtomRequestTest {

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNullPathInfoBecomesEmptyString() {
        when(request.getPathInfo()).thenReturn(null);
        assertEquals("", new AtomRequest(request, null).getPathInfo());
    }

    @Test
    public void testPathInfoPassesThrough() {
        when(request.getPathInfo()).thenReturn("/blog/entries");
        assertEquals("/blog/entries", new AtomRequest(request, null).getPathInfo());
    }

    @Test
    public void testHeaderAndContentTypeDelegate() {
        when(request.getHeader("Slug")).thenReturn("my-slug");
        when(request.getContentType()).thenReturn("application/atom+xml");
        AtomRequest areq = new AtomRequest(request, null);
        assertEquals("my-slug", areq.getHeader("Slug"));
        assertEquals("application/atom+xml", areq.getContentType());
    }

    @Test
    public void testInputStreamReturnsBufferedBody() throws Exception {
        byte[] body = "hello body".getBytes(StandardCharsets.UTF_8);
        AtomRequest areq = new AtomRequest(request, body);
        try (InputStream in = areq.getInputStream()) {
            assertArrayEquals(body, in.readAllBytes());
        }
    }

    @Test
    public void testInputStreamIsFreshEachCall() throws Exception {
        byte[] body = "abc".getBytes(StandardCharsets.UTF_8);
        AtomRequest areq = new AtomRequest(request, body);
        try (InputStream first = areq.getInputStream();
             InputStream second = areq.getInputStream()) {
            assertArrayEquals(body, first.readAllBytes());
            // second stream is independent and still readable from the start
            assertArrayEquals(body, second.readAllBytes());
        }
    }

    @Test
    public void testNullBodyYieldsEmptyStream() throws Exception {
        AtomRequest areq = new AtomRequest(request, null);
        try (InputStream in = areq.getInputStream()) {
            assertArrayEquals(new byte[0], in.readAllBytes());
        }
    }
}
