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
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.WeblogCustomTheme;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPreviewResourceRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Preview finds a resource either in the theme being previewed or in the
 * weblog's own theme. With a custom theme, both reach the media library.
 * Only an enabled user with a role in the weblog gets a preview.
 */
class PreviewResourceServletTest {

    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager media;
    private UserManager users;
    private User user;
    private Weblog weblog;
    private PreviewResourceServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        Weblogger roller = mock(Weblogger.class);
        when(roller.getMediaFileManager()).thenReturn(media);
        users = mock(UserManager.class);
        when(roller.getUserManager()).thenReturn(users);
        user = mock(User.class);
        when(user.getEnabled()).thenReturn(Boolean.TRUE);
        when(users.checkPermission(any(WeblogPermission.class), eq(user))).thenReturn(true);
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

        servlet = new PreviewResourceServlet();
        servlet.init(config);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void uploadedDocumentFoundByPreviewedThemeIsADownload() throws Exception {
        HttpServletResponse response = serve("custom", "/page.html", "<p>page</p>");

        verify(response).setContentType("application/octet-stream");
        verify(response).setHeader("Content-Disposition",
                "attachment; filename=\"/page.html\"");
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response, never()).setContentType("text/html");
    }

    @Test
    void uploadedDocumentFoundByWeblogThemeIsADownload() throws Exception {
        HttpServletResponse response = serve(null, "/page.html", "<p>page</p>");

        verify(response).setContentType("application/octet-stream");
        verify(response, never()).setContentType("text/html");
    }

    @Test
    void customThemeStylesheetKeepsItsType() throws Exception {
        HttpServletResponse response = serve(null, "/css/site.css", "body {}");

        verify(response).setContentType("text/css");
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response, never()).setHeader(eq("Content-Disposition"), anyString());
    }

    @Test
    void userWithoutWeblogRoleGetsNotFound() throws Exception {
        when(users.checkPermission(any(WeblogPermission.class), eq(user))).thenReturn(false);
        HttpServletResponse response = serve(null, "/css/site.css", "body {}", "");

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).setContentType(anyString());
    }

    @Test
    void disabledUserGetsNotFound() throws Exception {
        when(user.getEnabled()).thenReturn(Boolean.FALSE);
        HttpServletResponse response = serve(null, "/css/site.css", "body {}", "");

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private HttpServletResponse serve(String themeName, String path, String body)
            throws Exception {
        return serve(themeName, path, body, body);
    }

    private HttpServletResponse serve(String themeName, String path, String body, String expected)
            throws Exception {
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

        try (MockedConstruction<WeblogPreviewResourceRequest> parsed = mockConstruction(
                WeblogPreviewResourceRequest.class, (mocked, ctx) -> {
                    when(mocked.getWeblog()).thenReturn(weblog);
                    when(mocked.getUser()).thenReturn(user);
                    when(mocked.getResourcePath()).thenReturn(path);
                    when(mocked.getThemeName()).thenReturn(themeName);
                    if (themeName != null) {
                        when(mocked.getTheme()).thenReturn(new WeblogCustomTheme(weblog));
                    }
                })) {
            servlet.doGet(request, response);
        }

        assertEquals(expected, written.toString(StandardCharsets.UTF_8.name()));
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
