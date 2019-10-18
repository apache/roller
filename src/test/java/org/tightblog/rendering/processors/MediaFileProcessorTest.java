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
package org.tightblog.rendering.processors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.TestUtils;
import org.tightblog.WebloggerTest;
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

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaFileProcessorTest {

    private static Logger log = LoggerFactory.getLogger(MediaFileProcessorTest.class);

    private MediaFileProcessor processor;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private LazyExpiringCache mockCache;
    private WeblogDao mockWD;
    private MediaManager mockMFM;
    private MediaFile mediaFile;

    private static final String TEST_IMAGE = "/hawk.jpg";

    @Before
    public void initializeMocks() throws IOException {
        mockRequest = TestUtils.createMockServletRequestForMediaFileRequest("1234");

        mockWD = mock(WeblogDao.class);
        Weblog weblog = new Weblog();
        weblog.setHandle(TestUtils.BLOG_HANDLE);
        when(mockWD.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(weblog);

        mockCache = mock(LazyExpiringCache.class);

        mediaFile = new MediaFile();
        mockMFM = mock(MediaManager.class);
        when(mockMFM.getMediaFileWithContent("1234")).thenReturn(mediaFile);

        processor = new MediaFileProcessor(mockWD, mockCache, mockMFM);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWD.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(null);
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void test404OnNoPathInfo() throws IOException {
        mockRequest = TestUtils.createMockServletRequestForMediaFileRequest(null);
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockMFM, never()).getMediaFileWithContent(anyString());
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void test404OnNoMediaFile() throws IOException {
        when(mockMFM.getMediaFileWithContent("1234")).thenReturn(null);
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockMFM).getMediaFileWithContent("1234");
        verify(mockResponse).sendError(SC_NOT_FOUND);
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
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockRequest).getDateHeader("If-Modified-Since");
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache).incrementRequestsHandledBy304();
    }

    @Test
    public void testReturn404onFileNotFound() throws IOException {
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();

        // not found also sent on missing thumbnail requests
        Mockito.clearInvocations(mockResponse);
        mediaFile.setContentType("image/jpeg");
        when(mockRequest.getParameter("tn")).thenReturn("true");
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testReturnCorrectImage() throws IOException, URISyntaxException {
        Instant now = Instant.now();
        File regularFile = new File(getClass().getResource(TEST_IMAGE).toURI());
        mediaFile.setLastUpdated(now);
        mediaFile.setContent(regularFile);
        mediaFile.setThumbnail(regularFile);
        mediaFile.setContentType("image/jpeg");
        processor.getMediaFile(mockRequest, mockResponse);
        // image/jpeg: regular image sent
        verify(mockResponse).setContentType("image/jpeg");

        Mockito.clearInvocations(mockResponse);
        when(mockRequest.getParameter("tn")).thenReturn("true");
        processor.getMediaFile(mockRequest, mockResponse);
        // image/png: thumbnail image sent
        verify(mockResponse).setContentType("image/png");

        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockResponse).setDateHeader("Last-Modified", now.toEpochMilli());
        verify(mockCache, times(2)).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
    }

    @Test
    public void testReturn404OnProcessingException() throws IOException, URISyntaxException {
        File regularFile = new File(getClass().getResource(TEST_IMAGE).toURI());
        mediaFile.setContent(regularFile);
        WebloggerTest.logExpectedException(log, "IOException");
        when(mockResponse.getOutputStream()).thenThrow(new IOException());
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockResponse).reset();
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();

        // don't send error code if response committed
        Mockito.clearInvocations(mockResponse);
        when(mockResponse.isCommitted()).thenReturn(true);
        processor.getMediaFile(mockRequest, mockResponse);
        verify(mockResponse, never()).sendError(SC_NOT_FOUND);
        verify(mockResponse, never()).reset();
    }
}
