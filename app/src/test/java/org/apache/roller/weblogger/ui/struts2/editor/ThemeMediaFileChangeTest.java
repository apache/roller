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

import org.apache.roller.weblogger.webservices.atomprotocol.AtomNotAuthorizedException;
import org.apache.roller.weblogger.webservices.atomprotocol.AtomNotFoundException;
import org.apache.roller.weblogger.webservices.atomprotocol.AtomRequest;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.ThemeMediaFiles;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.webservices.atomprotocol.MediaCollection;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A custom theme serves the scripts and stylesheets it keeps in the media
 * library inline at their original path. Authors keep managing ordinary media,
 * including uploads migrated from older versions, but only the weblog's Admins
 * may replace, rename, move or delete those theme files, or change any file's
 * original path.
 */
class ThemeMediaFileChangeTest {

    private static final String LOCKED = "mediaFile.themeFileAdminOnly";
    private static final String PATH_LOCKED = "mediaFile.originalPathAdminOnly";

    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager media;
    private Weblog weblog;
    private MediaFileDirectory jsDir;
    private MediaFileDirectory otherDir;
    private MediaFile themeFile;
    private MediaFile upload;
    private MediaFile migratedUpload;
    private User author;
    private User admin;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getMediaFileManager()).thenReturn(media);
        WeblogManager weblogs = mock(WeblogManager.class);
        when(weblogger.getWeblogManager()).thenReturn(weblogs);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

        weblog = mock(Weblog.class);
        author = new User();
        author.setUserName("author");
        admin = new User();
        admin.setUserName("admin");
        when(weblog.hasUserPermission(author, WeblogPermission.ADMIN)).thenReturn(false);
        when(weblog.hasUserPermission(admin, WeblogPermission.ADMIN)).thenReturn(true);
        when(weblog.hasUserPermission(author, WeblogPermission.POST)).thenReturn(true);
        when(weblog.hasUserPermission(admin, WeblogPermission.POST)).thenReturn(true);
        when(weblogs.getWeblogByHandle("blog")).thenReturn(weblog);

        jsDir = directory("dir-js");
        otherDir = directory("dir-other");
        when(media.getMediaFileDirectory(weblog, "dir-other")).thenReturn(otherDir);

        themeFile = file("theme-1", "jquery.js", "/js/jquery.js");
        upload = file("upload-1", "photo.jpg", null);
        // what the upgrade from the old uploads directory leaves behind
        migratedUpload = file("upload-2", "holiday.jpg", "/images/holiday.jpg");
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void aScriptOrStylesheetWithAnOriginalPathIsAThemeFile() throws Exception {
        assertTrue(ThemeMediaFiles.isThemeFile(themeFile));
        assertTrue(ThemeMediaFiles.isThemeFile(file("theme-2", "site.css", "/css/SITE.CSS")));
        assertTrue(ThemeMediaFiles.isThemeFile(file("theme-3", "app.mjs", "/js/app.mjs")));
        assertFalse(ThemeMediaFiles.isThemeFile(upload));
        assertFalse(ThemeMediaFiles.isThemeFile(migratedUpload));
        assertFalse(ThemeMediaFiles.mayChange(weblog, author, themeFile));
        assertFalse(ThemeMediaFiles.mayChange(weblog, null, themeFile));
        assertTrue(ThemeMediaFiles.mayChange(weblog, admin, themeFile));
        assertTrue(ThemeMediaFiles.mayChange(weblog, author, upload));
        assertTrue(ThemeMediaFiles.mayChange(weblog, author, migratedUpload));
    }

    @Test
    void authorCannotReplaceAThemeFile() throws Exception {
        MediaFileEdit action = editing(themeFile, author, "dir-js");
        action.withUploadedFiles(List.of(uploaded("jquery.js")));
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
        action.save();
        assertFalse(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media).updateMediaFile(weblog, themeFile);
    }

    @Test
    void adminIsNotStoppedFromReplacingAThemeFile() throws Exception {
        MediaFileEdit action = editing(themeFile, admin, "dir-js");
        action.withUploadedFiles(List.of(uploaded("jquery.js")));
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

    @Test
    void authorCanStillReplaceAndDeleteAMigratedUpload() throws Exception {
        MediaFileEdit replacing = editing(migratedUpload, author, "dir-js");
        replacing.withUploadedFiles(List.of(uploaded("holiday.jpg")));
        replacing.save();
        assertFalse(replacing.getActionErrors().contains(LOCKED),
                replacing.getActionErrors().toString());

        MediaFileBase deleting = base(author);
        deleting.setMediaFileId("upload-2");
        deleting.doDeleteMediaFile();
        assertFalse(deleting.hasActionErrors(), deleting.getActionErrors().toString());
        verify(media).removeMediaFile(weblog, migratedUpload);
    }

    @Test
    void authorCannotRenameAThemeFile() throws Exception {
        MediaFileEdit action = editing(themeFile, author, "dir-js");
        action.getBean().setName("other.js");
        assertEquals(MediaFileEdit.INPUT, action.save());
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).updateMediaFile(any(), any());
    }

    @Test
    void authorCannotClearAThemeFilesOriginalPath() throws Exception {
        MediaFileEdit action = editingWithOriginalPath(themeFile, author, "");
        assertEquals(MediaFileEdit.INPUT, action.save());
        assertTrue(action.getActionErrors().contains(PATH_LOCKED),
                action.getActionErrors().toString());
        assertEquals("/js/jquery.js", themeFile.getOriginalPath());
        verify(media, never()).updateMediaFile(any(), any());
    }

    @Test
    void authorCannotGiveAnUploadAThemePath() throws Exception {
        MediaFileEdit action = editingWithOriginalPath(upload, author, "/js/jquery.js");
        assertEquals(MediaFileEdit.INPUT, action.save());
        assertTrue(action.getActionErrors().contains(PATH_LOCKED),
                action.getActionErrors().toString());
        assertEquals(null, upload.getOriginalPath());
        verify(media, never()).updateMediaFile(any(), any());
    }

    @Test
    void authorCanSaveWhenTheOriginalPathIsUnchanged() throws Exception {
        MediaFileEdit action = editingWithOriginalPath(migratedUpload, author, "/images/holiday.jpg");
        action.save();
        assertFalse(action.getActionErrors().contains(PATH_LOCKED),
                action.getActionErrors().toString());
        verify(media).updateMediaFile(weblog, migratedUpload);
    }

    @Test
    void adminCanChangeAnOriginalPath() throws Exception {
        MediaFileEdit action = editingWithOriginalPath(upload, admin, "/js/extra.js");
        action.save();
        assertFalse(action.getActionErrors().contains(PATH_LOCKED),
                action.getActionErrors().toString());
        assertEquals("/js/extra.js", upload.getOriginalPath());
    }

    @Test
    void authorCannotDeleteAFolderHoldingAThemeFile() throws Exception {
        when(jsDir.getMediaFiles()).thenReturn(new HashSet<>(Arrays.asList(themeFile, upload)));
        when(media.getMediaFileDirectory(weblog, "dir-js")).thenReturn(jsDir);
        MediaFileView action = folderView(author);
        action.setDirectoryId("dir-js");
        action.deleteFolder();
        assertTrue(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media, never()).removeMediaFileDirectory(any());
    }

    @Test
    void authorCanDeleteAFolderOfOrdinaryMedia() throws Exception {
        when(otherDir.getMediaFiles()).thenReturn(
                new HashSet<>(Arrays.asList(upload, migratedUpload)));
        when(media.getOrCreateDefaultMediaFileDirectory(weblog)).thenReturn(jsDir);
        MediaFileView action = folderView(author);
        action.setDirectoryId("dir-other");
        action.deleteFolder();
        assertFalse(action.getActionErrors().contains(LOCKED), action.getActionErrors().toString());
        verify(media).removeMediaFileDirectory(otherDir);
    }

    @Test
    void atomRefusesAnAuthorReplacingAThemeFile() throws Exception {
        when(media.getMediaFileByPath(eq(weblog), anyString())).thenReturn(themeFile);
        MediaCollection atom = new MediaCollection(author, "http://localhost/roller-services/app");
        assertThrows(AtomNotAuthorizedException.class,
                () -> atom.putMedia(atomRequest("blog/resources/js/jquery.js")));
        verify(media, never()).updateMediaFile(any(), any(), any());
    }

    @Test
    void atomRefusesAnAuthorDeletingAThemeFile() throws Exception {
        when(media.getMediaFileByPath(eq(weblog), anyString())).thenReturn(themeFile);
        MediaCollection atom = new MediaCollection(author, "http://localhost/roller-services/app");
        assertThrows(AtomNotAuthorizedException.class,
                () -> atom.deleteEntry(atomRequest("blog/resources/js/jquery.js.media-link")));
        verify(media, never()).removeMediaFile(any(), any());
    }

    @Test
    void atomLetsAnAdminDeleteAThemeFile() throws Exception {
        when(media.getMediaFileByPath(eq(weblog), anyString())).thenReturn(themeFile);
        MediaCollection atom = new MediaCollection(admin, "http://localhost/roller-services/app");
        atom.deleteEntry(atomRequest("blog/resources/js/jquery.js.media-link"));
        verify(media).removeMediaFile(weblog, themeFile);
    }

    @Test
    void atomReportsAMissingFileOrWeblogAsNotFound() throws Exception {
        MediaCollection atom = new MediaCollection(author, "http://localhost/roller-services/app");
        assertThrows(AtomNotFoundException.class,
                () -> atom.deleteEntry(atomRequest("blog/resources/gone.js.media-link")));
        assertThrows(AtomNotFoundException.class,
                () -> atom.putMedia(atomRequest("nosuchblog/resources/js/jquery.js")));
        verify(media, never()).removeMediaFile(any(), any());
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

    private static UploadedFile uploaded(String name) throws Exception {
        File content = tempFile();
        UploadedFile file = mock(UploadedFile.class);
        when(file.getContent()).thenReturn(content);
        when(file.getOriginalName()).thenReturn(name);
        when(file.getContentType()).thenReturn("text/javascript");
        when(file.length()).thenReturn(content.length());
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

    private MediaFileEdit editingWithOriginalPath(MediaFile file, User user, String newPath) {
        MediaFileEdit action = editing(file, user, "dir-js");
        doReturn(true).when(action).getBooleanProp("mediafile.originalPathEdit.enabled");
        action.getBean().setOriginalPath(newPath);
        return action;
    }

    private MediaFileView folderView(User user) {
        MediaFileView action = spy(new MediaFileView());
        stubText(action);
        doReturn(MediaFileView.SUCCESS).when(action).execute();
        action.setActionWeblog(weblog);
        action.setAuthenticatedUser(user);
        return action;
    }

    private static AtomRequest atomRequest(String pathInfo) {
        AtomRequest request = mock(AtomRequest.class);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getContentType()).thenReturn("text/javascript");
        return request;
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
