/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.rendering.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogMediaResourceRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Media files are served at the address of the weblog they belong to.
 */
class MediaResourceServletTest {

    private static final String FILE_ID = "8f9c2a4e-1d3b-4c5e-9f6a-7b8c9d0e1f2a";

    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager media;
    private Weblog owner;
    private MediaResourceServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        Weblogger roller = mock(Weblogger.class);
        when(roller.getMediaFileManager()).thenReturn(media);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

        owner = weblog("owner-id", "family");
        MediaFile file = mock(MediaFile.class);
        when(file.getId()).thenReturn(FILE_ID);
        when(file.getWeblog()).thenReturn(owner);
        when(file.getName()).thenReturn("photo.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getLastModified()).thenReturn(0L);
        when(file.getInputStream()).thenAnswer(call ->
                new ByteArrayInputStream("jpeg".getBytes(StandardCharsets.UTF_8)));
        when(media.getMediaFile(FILE_ID, true)).thenReturn(file);

        servlet = new MediaResourceServlet();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void aFileIsServedAtItsOwnWeblogsAddress() throws Exception {
        ByteArrayOutputStream written = new ByteArrayOutputStream();
        HttpServletResponse response = serve(owner, written);

        verify(response, never()).sendError(anyInt());
        assertEquals("jpeg", written.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    void aFileIsNotServedAtAnotherWeblogsAddress() throws Exception {
        ByteArrayOutputStream written = new ByteArrayOutputStream();
        HttpServletResponse response = serve(weblog("other-id", "public"), written);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("", written.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    void anUnknownFileIsNotFound() throws Exception {
        when(media.getMediaFile(FILE_ID, true)).thenReturn(null);
        HttpServletResponse response = serve(owner, new ByteArrayOutputStream());

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private static Weblog weblog(String id, String handle) {
        Weblog weblog = mock(Weblog.class);
        when(weblog.getId()).thenReturn(id);
        when(weblog.getHandle()).thenReturn(handle);
        return weblog;
    }

    private HttpServletResponse serve(Weblog addressed, ByteArrayOutputStream written) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1L);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new CapturingStream(written));

        String handle = addressed.getHandle();
        try (MockedConstruction<WeblogMediaResourceRequest> parsed = mockConstruction(
                WeblogMediaResourceRequest.class, (mocked, ctx) -> {
                    when(mocked.getWeblog()).thenReturn(addressed);
                    when(mocked.getWeblogHandle()).thenReturn(handle);
                    when(mocked.getResourceId()).thenReturn(FILE_ID);
                })) {
            servlet.doGet(request, response);
        }
        return response;
    }

    private static final class CapturingStream extends ServletOutputStream {
        private final ByteArrayOutputStream target;

        CapturingStream(ByteArrayOutputStream target) {
            this.target = target;
        }

        @Override
        public void write(int b) {
            target.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
        }
    }
}
