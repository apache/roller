/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.tightblog.TestUtils;
import org.tightblog.service.MediaManager;
import org.tightblog.domain.MediaFile;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.dao.WeblogDao;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaFileControllerTest {

    private MediaFileController controller;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private LazyExpiringCache mockCache;
    private WeblogDao mockWD;
    private MediaManager mockMFM;
    private MediaFile mediaFile;

    private static final String TEST_IMAGE = "/hawk.jpg";

    @Before
    public void initializeMocks() throws IOException {
        mockRequest = TestUtils.createMockServletRequest();

        mockWD = mock(WeblogDao.class);
        Weblog weblog = new Weblog();
        weblog.setHandle(TestUtils.BLOG_HANDLE);
        when(mockWD.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(weblog);

        mockCache = mock(LazyExpiringCache.class);

        mediaFile = new MediaFile();
        mediaFile.setContentType("image/jpeg");
        mockMFM = mock(MediaManager.class);
        when(mockMFM.getMediaFileWithContent("1234")).thenReturn(mediaFile);

        controller = new MediaFileController(mockWD, mockCache, mockMFM);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWD.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(null);
        ResponseEntity<Resource> result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void test404OnNoMediaFile() throws IOException {
        when(mockMFM.getMediaFileWithContent("1234")).thenReturn(null);
        ResponseEntity<Resource> result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        verify(mockMFM).getMediaFileWithContent("1234");
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void test304OnNotModified() throws IOException {
        Instant now = Instant.now();
        // make four days old
        mediaFile.setLastUpdated(now.minus(4, ChronoUnit.DAYS));
        // request has cached version two days old, i.e., still usable without downloading
        when(mockRequest.getDateHeader(any())).thenReturn(now.minus(2,
                ChronoUnit.DAYS).toEpochMilli());
        ResponseEntity<Resource> result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        verify(mockRequest).getDateHeader("If-Modified-Since");
        assertEquals(HttpStatus.NOT_MODIFIED, result.getStatusCode());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache).incrementRequestsHandledBy304();
    }

    @Test
    public void testReturn404onFileNotFound() throws IOException {
        ResponseEntity<Resource> result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();

        // not found also sent on missing thumbnail requests
        Mockito.clearInvocations(mockResponse);
        mediaFile.setContentType("image/jpeg");
        when(mockRequest.getParameter("tn")).thenReturn("true");
        result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testReturnCorrectImage() throws IOException, URISyntaxException {
        Instant now = Instant.now();
        File regularFile = new File(getClass().getResource(TEST_IMAGE).toURI());
        mediaFile.setLastUpdated(now);
        mediaFile.setContent(regularFile);
        mediaFile.setThumbnail(regularFile);
        mediaFile.setContentType("image/jpeg");
        ResponseEntity<Resource> result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        // image/jpeg: regular image sent
        assertEquals(MediaType.IMAGE_JPEG, result.getHeaders().getContentType());

        Mockito.clearInvocations(mockResponse);
        when(mockRequest.getParameter("tn")).thenReturn("true");
        result = controller.getMediaFile(TestUtils.BLOG_HANDLE, "1234", mockRequest, mockResponse);
        // image/png: thumbnail image sent
        assertEquals(MediaType.IMAGE_PNG, result.getHeaders().getContentType());
        assertEquals(CacheControl.noCache().getHeaderValue(), result.getHeaders().getCacheControl());
        assertEquals(now.truncatedTo(ChronoUnit.SECONDS).toEpochMilli(),
                result.getHeaders().getLastModified());

        verify(mockCache, times(2)).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
    }
}
