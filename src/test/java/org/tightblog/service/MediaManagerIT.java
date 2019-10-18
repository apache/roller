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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.tightblog.WebloggerTest;
import org.tightblog.domain.MediaDirectory;
import org.tightblog.domain.MediaFile;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MediaManagerIT extends WebloggerTest {

    private User testUser;
    private Weblog testWeblog;
    private MediaDirectory defaultDirectory;
    private static InputStream hawkInputStream;

    @Value("${mediafiles.storage.dir}")
    private String mediafileDir;

    @Resource
    private MediaManager mediaManager;

    public void setMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @BeforeClass
    public static void beforeClass() {
        hawkInputStream = MediaManagerIT.class.getResourceAsStream("/hawk.jpg");
    }

    @AfterClass
    public static void afterClass() throws IOException {
        hawkInputStream.close();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("mediaFileTestUser");
        testWeblog = setupWeblog("media-file-test-weblog", testUser);
        defaultDirectory = mediaDirectoryDao.findByWeblogAndName(testWeblog, "default");
    }

    @After
    public void tearDown() {
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    @Test
    public void testCreateMediaDirectory() {

        try {
            mediaManager.createMediaDirectory(testWeblog, "");
            fail("did not fail with invalid name");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            mediaManager.createMediaDirectory(testWeblog, "default");
            fail("did not fail with duplicate name");
        } catch (IllegalArgumentException ignored) {
        }

        MediaDirectory newDirectory1 = mediaManager.createMediaDirectory(testWeblog, "test1");

        // show throw error when creating directory that already exists
        try {
            mediaManager.createMediaDirectory(testWeblog, "test1");
            fail("should not have allowed creation of directory with same name");
        } catch (IllegalArgumentException ignored) {
        }

        MediaDirectory newDirectory2 = mediaManager.createMediaDirectory(testWeblog, "test2");

        MediaDirectory newDirectory1ById = mediaDirectoryDao.findByIdOrNull(newDirectory1.getId());
        assertEquals(newDirectory1, newDirectory1ById);

        MediaDirectory newDirectory2ById = mediaDirectoryDao.findByIdOrNull(newDirectory2.getId());
        assertEquals("test2", newDirectory2ById.getName());

    }

    @Test
    public void testGetMediaDirectories() {
        mediaManager.createMediaDirectory(testWeblog, "dir1");
        mediaManager.createMediaDirectory(testWeblog, "dir2");

        List<MediaDirectory> directories = mediaDirectoryDao.findByWeblog(testWeblog);
        assertNotNull(directories);
        assertEquals(3, directories.size());
        assertTrue(containsName(directories, "default"));
        assertTrue(containsName(directories, "dir1"));
        assertTrue(containsName(directories, "dir2"));
    }

    private MultipartFile createMockMultipartFile() throws IOException {
        MultipartFile mockMultipartFile = mock(MockMultipartFile.class);
        when(mockMultipartFile.getSize()).thenReturn(3000L);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getName()).thenReturn("hawk.jpg");
        when(mockMultipartFile.getInputStream()).thenReturn(hawkInputStream);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("hawk.jpg");
        return mockMultipartFile;
    }

    @Test
    public void testCreateAndDeleteMediaFile() throws Exception {
        MultipartFile mockMultipartFile = createMockMultipartFile();

        MediaFile mediaFile = new MediaFile();
        mediaFile.setDirectory(defaultDirectory);
        mediaFile.setCreator(testUser);
        mediaFile.setName(mockMultipartFile.getName());
        mediaFile.setNotes("This is a test image");
        mediaFile.setLength(mockMultipartFile.getSize());
        mediaFile.setContentType(mockMultipartFile.getContentType());
        defaultDirectory.getMediaFiles().add(mediaFile);

        Map<String, List<String>> errors = new HashMap<>();
        mediaManager.saveMediaFile(mediaFile, mockMultipartFile, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        assertNotNull(mediaFile.getId());
        assertTrue(mediaFile.getId().length() > 0);

        // test values saved
        MediaFile mediaFile1 = mediaFileDao.findByIdOrNull(mediaFile.getId());
        assertEquals(defaultDirectory, mediaFile1.getDirectory());
        assertEquals(testUser, mediaFile1.getCreator());
        assertEquals(mockMultipartFile.getName(), mediaFile1.getName());
        assertEquals("This is a test image", mediaFile1.getNotes());
        assertEquals(mockMultipartFile.getSize(), mediaFile1.getLength());
        assertEquals(mockMultipartFile.getContentType(), mediaFile1.getContentType());

        // test delete
        MediaFile mediaFile2 = mediaFileDao.findByIdOrNull(mediaFile1.getId());
        assertEquals(mockMultipartFile.getName(), mediaFile2.getName());
        mediaManager.removeMediaFile(testWeblog, mediaFile2);

        MediaFile mediaFile3 = mediaFileDao.findByIdOrNull(mediaFile1.getId());
        assertNull(mediaFile3);
    }

    @Test
    public void testUpdateMediaFile() throws Exception {
        MultipartFile mockMultipartFile = createMockMultipartFile();

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setName("test5.jpg");
        mediaFile.setNotes("This is a test image 5");
        mediaFile.setLength(3000);
        mediaFile.setDirectory(defaultDirectory);
        mediaFile.setContentType("image/jpeg");

        Map<String, List<String>> errors = new HashMap<>();
        mediaManager.saveMediaFile(mediaFile, mockMultipartFile, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        defaultDirectory.getMediaFiles().add(mediaFile);
        String id = mediaFile.getId();
        assertNotNull(id);
        assertTrue(id.length() > 0);

        MediaFile mediaFile1 = mediaFileDao.findByIdOrNull(id);
        mediaFile1.setName("updated.gif");
        mediaFile1.setNotes("updated desc");
        mediaFile1.setContentType("image/gif");

        mediaManager.saveMediaFile(mediaFile1, null, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        MediaFile mediaFile2 = mediaFileDao.findByIdOrNull(id);
        assertEquals("updated.gif", mediaFile2.getName());
        assertEquals("updated desc", mediaFile2.getNotes());
        assertEquals("image/gif", mediaFile2.getContentType());
    }

    @Test
    public void testGetDirectoryContents() throws Exception {
        mediaManager.createMediaDirectory(testWeblog, "dir1");
        mediaManager.createMediaDirectory(testWeblog, "dir2");
        mediaManager.createMediaDirectory(testWeblog, "dir3");

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setDirectory(defaultDirectory);
        mediaFile.setName("test6_1.jpg");
        mediaFile.setNotes("This is a test image 6.1");
        mediaFile.setLength(4000);
        mediaFile.setContentType("image/jpeg");

        Map<String, List<String>> errors = new HashMap<>();
        MultipartFile mockMultipartFile = createMockMultipartFile();
        mediaManager.saveMediaFile(mediaFile, mockMultipartFile, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setCreator(testUser);
        mediaFile2.setDirectory(defaultDirectory);
        mediaFile2.setName("test6_2.jpg");
        mediaFile2.setNotes("This is a test image 6.2");
        mediaFile2.setLength(4000);
        mediaFile2.setContentType("image/jpeg");

        mediaManager.saveMediaFile(mediaFile2, mockMultipartFile, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        List<MediaDirectory> childDirectories = testWeblog.getMediaDirectories();
        assertEquals(4, childDirectories.size());
        assertTrue(containsName(childDirectories, "dir1"));
        assertTrue(containsName(childDirectories, "dir2"));
        assertTrue(containsName(childDirectories, "dir3"));

        Set<MediaFile> mediaFiles = defaultDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test6_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test6_2.jpg"));

        MediaDirectory testDirectory = mediaDirectoryDao.findByIdOrNull(defaultDirectory.getId());
        assertTrue(testDirectory.hasMediaFile("test6_1.jpg"));
        assertTrue(testDirectory.hasMediaFile("test6_2.jpg"));
    }

    @Test
    public void testMoveDirectoryContents() throws Exception {
        MediaDirectory dir1 = mediaManager.createMediaDirectory(testWeblog, "dir1");
        mediaManager.createMediaDirectory(testWeblog, "dir2");
        mediaManager.createMediaDirectory(testWeblog, "dir3");

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setDirectory(defaultDirectory);
        mediaFile.setName("test7_1.jpg");
        mediaFile.setNotes("This is a test image 7.1");
        mediaFile.setLength(4000);
        mediaFile.setContentType("image/jpeg");

        Map<String, List<String>> errors = new HashMap<>();
        MultipartFile mockMultipartFile = createMockMultipartFile();
        mediaManager.saveMediaFile(mediaFile, mockMultipartFile, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setCreator(testUser);
        mediaFile2.setDirectory(defaultDirectory);
        mediaFile2.setName("test7_2.jpg");
        mediaFile2.setNotes("This is a test image 7.2");
        mediaFile2.setLength(4000);
        mediaFile2.setContentType("image/jpeg");

        mediaManager.saveMediaFile(mediaFile2, mockMultipartFile, testUser, errors);
        assertTrue(ObjectUtils.isEmpty(errors));

        MediaDirectory sourceDirectory = mediaDirectoryDao.findByIdOrNull(defaultDirectory.getId());

        Set<MediaFile> mediaFiles = sourceDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));

        MediaDirectory targetDirectory = mediaDirectoryDao.findByIdOrNull(dir1.getId());
        mediaManager.moveMediaFiles(mediaFiles, targetDirectory);

        sourceDirectory = mediaDirectoryDao.findByIdOrNull(defaultDirectory.getId());
        targetDirectory = mediaDirectoryDao.findByIdOrNull(dir1.getId());

        mediaFiles = targetDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));

        mediaFiles = sourceDirectory.getMediaFiles();
        assertEquals(0, mediaFiles.size());
    }

    @Test
    public void testDeleteDirectory() {
        MediaDirectory dir1 = mediaManager.createMediaDirectory(testWeblog, "dir1");
        mediaManager.createMediaDirectory(testWeblog, "dir2");
        mediaManager.createMediaDirectory(testWeblog, "dir3");

        List<MediaDirectory> directories = testWeblog.getMediaDirectories();
        assertEquals(4, directories.size());

        // Delete folder
        MediaDirectory directoryById = mediaDirectoryDao.findByIdOrNull(dir1.getId());
        testWeblog.getMediaDirectories().remove(directoryById);
        mediaManager.removeAllFiles(directoryById);
        weblogManager.saveWeblog(testWeblog, false);

        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertEquals(3, testWeblog.getMediaDirectories().size());
    }

    private static boolean containsName(Collection<MediaDirectory> directories, String name) {
        return directories.stream().anyMatch(dir -> name.equals(dir.getName()));
    }

    private static boolean containsFileWithName(Collection<MediaFile> files, String name) {
        return files.stream().anyMatch(file -> name.equals(file.getName()));
    }
}
