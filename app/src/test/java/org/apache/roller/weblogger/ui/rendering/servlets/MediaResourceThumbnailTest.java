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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A thumbnail request for an image that is too large to get a thumbnail is
 * answered with 404, so previews show the file type icon instead of loading
 * the full image. Other images without a thumbnail still get the original.
 */
class MediaResourceThumbnailTest {

    private static final String FILE_ID = "3b6d1f0a-7c2e-4e5a-8d9b-0a1b2c3d4e5f";

    private MockedStatic<WebloggerFactory> factory;
    private Weblog weblog;
    private MediaFile file;
    private MediaResourceServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        MediaFileManager media = mock(MediaFileManager.class);
        Weblogger roller = mock(Weblogger.class);
        when(roller.getMediaFileManager()).thenReturn(media);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

        weblog = mock(Weblog.class);
        when(weblog.getId()).thenReturn("weblog-id");
        when(weblog.getHandle()).thenReturn("photos");

        file = mock(MediaFile.class);
        when(file.getId()).thenReturn(FILE_ID);
        when(file.getWeblog()).thenReturn(weblog);
        when(file.getName()).thenReturn("panorama.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.isImageFile()).thenReturn(true);
        when(file.getLastModified()).thenReturn(0L);
        when(file.getInputStream()).thenAnswer(call -> stream("original"));
        when(media.getMediaFile(FILE_ID, true)).thenReturn(file);
        // the servlet may look the file up in the addressed weblog first
        when(media.getMediaFile(any(Weblog.class), eq(FILE_ID))).thenReturn(file);

        servlet = new MediaResourceServlet();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void thumbnailOfImageStoredWithoutOneIsNotFound() throws Exception {
        when(file.getThumbnailInputStream()).thenReturn(null);
        when(file.isThumbnailSkipped()).thenReturn(true);

        ByteArrayOutputStream written = new ByteArrayOutputStream();
        HttpServletResponse response = serve(true, written);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(file, never()).getInputStream();
        assertEquals("", written.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    void imageStoredWithoutThumbnailIsStillServedInFull() throws Exception {
        when(file.isThumbnailSkipped()).thenReturn(true);

        ByteArrayOutputStream written = new ByteArrayOutputStream();
        HttpServletResponse response = serve(false, written);

        verify(response, never()).sendError(anyInt());
        assertEquals("original", written.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    void thumbnailIsServedWhenPresent() throws Exception {
        when(file.getThumbnailInputStream()).thenAnswer(call -> stream("thumbnail"));

        ByteArrayOutputStream written = new ByteArrayOutputStream();
        HttpServletResponse response = serve(true, written);

        verify(response, never()).sendError(anyInt());
        assertEquals("thumbnail", written.toString(StandardCharsets.UTF_8.name()));
    }

    /** An image that simply has no thumbnail, e.g. one ImageIO can't read. */
    @Test
    void missingThumbnailOfUnreadImageFallsBackToOriginal() throws Exception {
        when(file.getThumbnailInputStream()).thenReturn(null);
        when(file.isThumbnailSkipped()).thenReturn(false);

        ByteArrayOutputStream written = new ByteArrayOutputStream();
        HttpServletResponse response = serve(true, written);

        verify(response, never()).sendError(anyInt());
        assertEquals("original", written.toString(StandardCharsets.UTF_8.name()));
    }

    private static ByteArrayInputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private HttpServletResponse serve(boolean thumbnail, ByteArrayOutputStream written)
            throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1L);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new CapturingStream(written));

        try (MockedConstruction<WeblogMediaResourceRequest> parsed = mockConstruction(
                WeblogMediaResourceRequest.class, (mocked, ctx) -> {
                    when(mocked.getWeblog()).thenReturn(weblog);
                    when(mocked.getWeblogHandle()).thenReturn("photos");
                    when(mocked.getResourceId()).thenReturn(FILE_ID);
                    when(mocked.isThumbnail()).thenReturn(thumbnail);
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
