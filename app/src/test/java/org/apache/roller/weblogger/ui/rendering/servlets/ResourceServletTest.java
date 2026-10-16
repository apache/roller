/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.WeblogCustomTheme;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogResourceRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A weblog with a custom theme finds its resources in the media library, so a
 * resource URL can reach any uploaded file whose original path matches.
 */
class ResourceServletTest {

    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager media;
    private Weblog weblog;
    private ResourceServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        Weblogger roller = mock(Weblogger.class);
        when(roller.getMediaFileManager()).thenReturn(media);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

        weblog = mock(Weblog.class);
        when(weblog.getTheme()).thenReturn(new WeblogCustomTheme(weblog));

        ServletContext context = mock(ServletContext.class);
        when(context.getMimeType("page.html")).thenReturn("text/html");
        when(context.getMimeType("/page.html")).thenReturn("text/html");
        when(context.getMimeType("/css/site.css")).thenReturn("text/css");
        ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(context);

        servlet = new ResourceServlet();
        servlet.init(config);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void uploadedDocumentFoundByCustomThemeIsADownload() throws Exception {
        HttpServletResponse response = serve("/page.html",
                "<p>page</p>");

        verify(response).setContentType("application/octet-stream");
        verify(response).setHeader("Content-Disposition",
                "attachment; filename=\"/page.html\"");
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response, never()).setContentType("text/html");
    }

    @Test
    void customThemeStylesheetKeepsItsType() throws Exception {
        HttpServletResponse response = serve("/css/site.css", "body {}");

        verify(response).setContentType("text/css");
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response, never()).setHeader(eq("Content-Disposition"), anyString());
    }

    private HttpServletResponse serve(String path, String body) throws Exception {
        MediaFile file = mock(MediaFile.class);
        when(file.getOriginalPath()).thenReturn(path);
        when(file.getInputStream()).thenReturn(
                new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(media.getMediaFileByOriginalPath(weblog, path)).thenReturn(file);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1L);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream written = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new CapturingStream(written));

        try (MockedConstruction<WeblogResourceRequest> parsed = mockConstruction(
                WeblogResourceRequest.class, (mocked, ctx) -> {
                    when(mocked.getWeblog()).thenReturn(weblog);
                    when(mocked.getResourcePath()).thenReturn(path);
                })) {
            servlet.doGet(request, response);
        }

        assertEquals(body, written.toString(StandardCharsets.UTF_8.name()));
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
