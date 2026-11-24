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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.File;
import java.nio.file.Files;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.ThemeMediaFiles;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A media file with an original path is served as part of the weblog's
 * custom theme. Authors keep managing ordinary media, but only the weblog's
 * Admins may replace, move or delete theme files.
 */
class ThemeMediaFileChangeTest {

    private static final String LOCKED = "mediaFile.themeFileAdminOnly";

    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager media;
    private Weblog weblog;
    private MediaFileDirectory jsDir;
    private MediaFileDirectory otherDir;
    private MediaFile themeFile;
    private MediaFile upload;
    private User author;
    private User admin;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getMediaFileManager()).thenReturn(media);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

        weblog = mock(Weblog.class);
        author = new User();
        author.setUserName("author");
        admin = new User();
        admin.setUserName("admin");
        when(weblog.hasUserPermission(author, WeblogPermission.ADMIN)).thenReturn(false);
        when(weblog.hasUserPermission(admin, WeblogPermission.ADMIN)).thenReturn(true);

        jsDir = directory("dir-js");
        otherDir = directory("dir-other");
        when(media.getMediaFileDirectory(weblog, "dir-other")).thenReturn(otherDir);

        themeFile = file("theme-1", "jquery.js", "/js/jquery.js");
        upload = file("upload-1", "photo.jpg", null);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void aFileWithAnOriginalPathIsAThemeFile() {
        assertTrue(ThemeMediaFiles.isThemeFile(themeFile));
        assertFalse(ThemeMediaFiles.isThemeFile(upload));
        assertFalse(ThemeMediaFiles.mayChange(weblog, author, themeFile));
        assertFalse(ThemeMediaFiles.mayChange(weblog, null, themeFile));
        assertTrue(ThemeMediaFiles.mayChange(weblog, admin, themeFile));
        assertTrue(ThemeMediaFiles.mayChange(weblog, author, upload));
    }

    @Test
    void authorCannotReplaceAThemeFile() throws Exception {
        MediaFileEdit action = editing(themeFile, author, "dir-js");
        action.setUploadedFile(tempFile());
        action.setUploadedFileName("jquery.js");
        assertEquals(MediaFileEdit.INPUT, action.save());
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).updateMediaFile(any(), any(), any());
        verify(media, never()).updateMediaFile(any(), any());
    }

    @Test
    void authorCannotMoveAThemeFileFromTheEditor() throws Exception {
        MediaFileEdit action = editing(themeFile, author, "dir-other");
        assertEquals(MediaFileEdit.INPUT, action.save());
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).moveMediaFile(any(), any());
    }

    @Test
    void authorCanStillEditAThemeFilesDescription() throws Exception {
        MediaFileEdit action = editing(themeFile, author, "dir-js");
        assertEquals(MediaFileEdit.SUCCESS, action.save());
        verify(media).updateMediaFile(weblog, themeFile);
    }

    @Test
    void adminIsNotStoppedFromReplacingAThemeFile() throws Exception {
        MediaFileEdit action = editing(themeFile, admin, "dir-js");
        action.setUploadedFile(tempFile());
        action.setUploadedFileName("jquery.js");
        action.save();
        assertFalse(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
    }

    @Test
    void authorCannotDeleteAThemeFile() throws Exception {
        MediaFileBase action = base(author);
        action.setMediaFileId("theme-1");
        action.doDeleteMediaFile();
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).removeMediaFile(any(), any());
    }

    @Test
    void authorCannotDeleteSelectedFilesThatIncludeAThemeFile() throws Exception {
        MediaFileBase action = base(author);
        action.setSelectedMediaFiles(new String[] {"upload-1", "theme-1"});
        action.doDeleteSelected();
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).removeMediaFile(any(), any());
    }

    @Test
    void authorCannotMoveAThemeFile() throws Exception {
        MediaFileBase action = base(author);
        action.setSelectedMediaFiles(new String[] {"theme-1"});
        action.setSelectedDirectory("dir-other");
        action.doMoveSelected();
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).moveMediaFile(any(), any());
    }

    @Test
    void authorCanStillDeleteAndMoveOrdinaryMedia() throws Exception {
        MediaFileBase deleting = base(author);
        deleting.setMediaFileId("upload-1");
        deleting.doDeleteMediaFile();
        assertFalse(deleting.hasActionErrors(), deleting.getActionErrors().toString());
        verify(media).removeMediaFile(weblog, upload);

        MediaFileBase moving = base(author);
        moving.setSelectedMediaFiles(new String[] {"upload-1"});
        moving.setSelectedDirectory("dir-other");
        moving.doMoveSelected();
        assertFalse(moving.hasActionErrors(), moving.getActionErrors().toString());
        verify(media).moveMediaFile(upload, otherDir);
    }

    @Test
    void adminCanDeleteAThemeFile() throws Exception {
        MediaFileBase action = base(admin);
        action.setMediaFileId("theme-1");
        action.doDeleteMediaFile();
        assertFalse(action.getActionErrors().contains(LOCKED));
        verify(media).removeMediaFile(weblog, themeFile);
    }

    private MediaFileDirectory directory(String id) {
        MediaFileDirectory dir = mock(MediaFileDirectory.class);
        when(dir.getId()).thenReturn(id);
        return dir;
    }

    private MediaFile file(String id, String name, String originalPath) throws Exception {
        MediaFile file = new MediaFile();
        file.setId(id);
        file.setName(name);
        file.setDirectory(jsDir);
        file.setOriginalPath(originalPath);
        when(media.getMediaFile(weblog, id)).thenReturn(file);
        return file;
    }

    private static File tempFile() throws Exception {
        File file = File.createTempFile("theme-file-test", ".js");
        file.deleteOnExit();
        Files.write(file.toPath(), "window.x = 1;\n".getBytes("UTF-8"));
        return file;
    }

    private MediaFileEdit editing(MediaFile file, User user, String directoryId) {
        MediaFileEdit action = spy(new MediaFileEdit());
        stubText(action);
        doReturn(false).when(action).getBooleanProp(anyString());
        action.setActionWeblog(weblog);
        action.setAuthenticatedUser(user);
        action.setMediaFileId(file.getId());
        action.setDirectory(file.getDirectory());
        MediaFileBean bean = action.getBean();
        bean.setName(file.getName());
        bean.setTagsAsString("");
        bean.setDirectoryId(directoryId);
        return action;
    }

    private MediaFileBase base(User user) {
        MediaFileBase action = spy(new MediaFileBase());
        stubText(action);
        action.setActionWeblog(weblog);
        action.setAuthenticatedUser(user);
        return action;
    }

    private static void stubText(MediaFileBase action) {
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString());
        doAnswer(call -> call.getArgument(0)).when(action)
                .getText(anyString(), anyString(), anyString());
    }
}
