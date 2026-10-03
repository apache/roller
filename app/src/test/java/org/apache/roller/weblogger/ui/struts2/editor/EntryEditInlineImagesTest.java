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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.roller.weblogger.business.FileContentManager;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.RollerMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EntryEditInlineImagesTest {

    private static final String PNG = "data:image/png;base64,"
            + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

    private MockedStatic<WebloggerConfig> config;
    private MockedStatic<WebloggerRuntimeConfig> runtimeConfig;
    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager mediaManager;
    private FileContentManager contentManager;
    private Weblog weblog;
    private EntryEdit action;
    private final List<MediaFile> created = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        config = mockStatic(WebloggerConfig.class);
        runtimeConfig = mockStatic(WebloggerRuntimeConfig.class);
        factory = mockStatic(WebloggerFactory.class);
        runtimeConfig.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled"))
                .thenReturn(true);
        runtimeConfig.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"))
                .thenReturn("1");

        mediaManager = mock(MediaFileManager.class);
        contentManager = mock(FileContentManager.class);
        URLStrategy urls = mock(URLStrategy.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getMediaFileManager()).thenReturn(mediaManager);
        when(weblogger.getFileContentManager()).thenReturn(contentManager);
        when(weblogger.getUrlStrategy()).thenReturn(urls);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        when(urls.getMediaFileURL(any(), anyString(), anyBoolean()))
                .thenAnswer(call -> "https://blog.example/media/" + call.getArgument(1));
        when(mediaManager.getDefaultMediaFileDirectory(any()))
                .thenReturn(new MediaFileDirectory());
        when(contentManager.canSave(any(), anyString(), anyString(), anyLong(), any()))
                .thenReturn(true);

        weblog = mock(Weblog.class);
        when(weblog.hasUserPermission(any(), eq(WeblogPermission.POST))).thenReturn(true);

        action = spy(new EntryEdit());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        action.setActionWeblog(weblog);
        action.setAuthenticatedUser(new User());
    }

    @AfterEach
    void tearDown() {
        factory.close();
        runtimeConfig.close();
        config.close();
    }

    @Test
    void uploadsEachDistinctImageOnceAndRewritesBothFields() throws Exception {
        action.getBean().setText("<p>x</p><img src=\"" + PNG + "\">");
        action.getBean().setSummary("<img alt=a src='" + PNG + "'>");

        assertTrue(action.prepareInlineImages(created));

        assertEquals(1, created.size());
        verify(mediaManager, times(1)).createMediaFile(eq(weblog), any(), any());
        String url = "https://blog.example/media/" + created.get(0).getId();
        assertEquals("<p>x</p><img src=\"" + url + "\">", action.getBean().getText());
        assertEquals("<img alt=a src=\"" + url + "\">", action.getBean().getSummary());
        assertEquals("image/png", created.get(0).getContentType());
    }

    @Test
    void keepsImagesInlineWhenTheAuthorCannotUpload() throws Exception {
        when(weblog.hasUserPermission(any(), eq(WeblogPermission.POST))).thenReturn(false);
        action.getBean().setText("<img src='" + PNG + "'>");

        assertTrue(action.prepareInlineImages(created));

        assertEquals("<img src=\"" + PNG + "\">", action.getBean().getText());
        verify(mediaManager, never()).createMediaFile(any(), any(), any());
    }

    @Test
    void keepsImagesInlineWhenPreferred() throws Exception {
        config.when(() -> WebloggerConfig.getBooleanProperty("weblog.inlineImages.preferInline"))
                .thenReturn(true);
        action.getBean().setText("<img src=\"" + PNG + "\">");

        assertTrue(action.prepareInlineImages(created));

        assertEquals("<img src=\"" + PNG + "\">", action.getBean().getText());
        verify(mediaManager, never()).createMediaFile(any(), any(), any());
    }

    @Test
    void refusesInlineFieldsOverTheLimit() throws Exception {
        runtimeConfig.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled"))
                .thenReturn(false);
        config.when(() -> WebloggerConfig.getProperty("weblog.inlineImages.maxFieldBytes"))
                .thenReturn("150");
        String text = "<p>" + "x".repeat(100) + "</p><img src=\"" + PNG + "\">";
        action.getBean().setText(text);

        assertFalse(action.prepareInlineImages(created));

        assertTrue(action.getActionErrors().contains("weblogEdit.inlineImageTooLarge"));
        assertEquals(text, action.getBean().getText());
    }

    @Test
    void refusesInvalidImageData() throws Exception {
        String text = "<img src=\"data:image/svg+xml;base64,"
                + Base64.getEncoder().encodeToString("<svg/>".getBytes()) + "\">";
        action.getBean().setText(text);

        assertFalse(action.prepareInlineImages(created));

        assertTrue(action.getActionErrors().contains("weblogEdit.inlineImageInvalid"));
        assertEquals(text, action.getBean().getText());
        verify(mediaManager, never()).createMediaFile(any(), any(), any());
    }

    @Test
    void refusesImagesOverTheUploadLimit() throws Exception {
        runtimeConfig.when(() -> WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"))
                .thenReturn("0.00001");
        action.getBean().setText("<img src=\"" + PNG + "\">");

        assertFalse(action.prepareInlineImages(created));

        assertTrue(action.getActionErrors().contains("weblogEdit.inlineImageUploadTooLarge"));
        verify(mediaManager, never()).createMediaFile(any(), any(), any());
    }

    @Test
    void refusedUploadRestoresTheFieldsAndRemovesEarlierUploads() throws Exception {
        String gif = "data:image/gif;base64,"
                + Base64.getEncoder().encodeToString("GIF89a!".getBytes());
        when(contentManager.canSave(any(), anyString(), eq("image/gif"), anyLong(), any()))
                .thenAnswer(call -> {
                    RollerMessages errors = call.getArgument(4);
                    errors.addError("error.upload.forbiddenFile");
                    return false;
                });
        String text = "<img src=\"" + PNG + "\">";
        String summary = "<img src=\"" + gif + "\">";
        action.getBean().setText(text);
        action.getBean().setSummary(summary);

        assertFalse(action.prepareInlineImages(created));

        assertEquals(text, action.getBean().getText());
        assertEquals(summary, action.getBean().getSummary());
        assertEquals(1, created.size());
        verify(mediaManager).removeMediaFile(weblog, created.get(0));
    }
}
