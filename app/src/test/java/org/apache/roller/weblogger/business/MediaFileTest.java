/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.business;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * Test media file related business operations.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaFileTest extends TestCase {

    public static Log log = LogFactory.getLog(MediaFileTest.class);
    // static final String runtimeEnv;
    public final static String TEST_IMAGE = "/hawk.jpg";

    public MediaFileTest() {
    }

    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
       // assertEquals(0L, WebloggerFactory.getWeblogger().getWeblogManager()
       //         .getWeblogCount());
    }

    public void tearDown() throws Exception {
    }

    /**
     * Test creation of directory by path
     */
    public void testCreateMediaFileDirectoryByPath() throws Exception {
        User testUser;
        Weblog testWeblog;

        // TODO: Setup code, to be moved to setUp method.
        log.info("Before setting up weblogger");
        // setup weblogger
        try {
            testUser = TestUtils.setupUser("mediaFileTestUser8");
            testWeblog = TestUtils
                    .setupWeblog("mediaFileTestWeblog8", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }

        /**
         * Real test starts here.
         */
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        try {
            mfMgr.createMediaFileDirectory(testWeblog, "");
            assertTrue(false);
        } catch (WebloggerException e) {
            assertTrue(true);
        }

        try {
            mfMgr.createMediaFileDirectory(testWeblog, "default");
            assertTrue(false);
        } catch (WebloggerException e) {
            assertTrue(true);
        }

        MediaFileDirectory newDirectory1 = mfMgr
                .createMediaFileDirectory(testWeblog, "test1");
        MediaFileDirectory newDirectory2 = mfMgr
                .createMediaFileDirectory(testWeblog, "test2");
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        MediaFileDirectory newDirectory1ById = mfMgr
                .getMediaFileDirectory(newDirectory1.getId());
        assertEquals(newDirectory1, newDirectory1ById);

        MediaFileDirectory newDirectory2ById = mfMgr
                .getMediaFileDirectory(newDirectory2.getId());
        assertEquals("test2", newDirectory2ById.getName());

        // show throw error when creating directory that already exists
        try {
            mfMgr.createMediaFileDirectory(testWeblog, "test1");
            assertTrue(false);
        } catch (WebloggerException e) {
            assertTrue(true);
        }

        TestUtils.endSession(true);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
        TestUtils.endSession(true);
    }

    /**
     * Test directory creation
     */
    public void testCreateMediaFileDirectory() throws Exception {
        User testUser;
        Weblog testWeblog;

        // TODO: Setup code, to be moved to setUp method.
        log.info("Before setting up weblogger");
        // setup weblogger
        try {
            testUser = TestUtils.setupUser("mediaFileTestUser");
            testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }

        /**
         * Real test starts here.
         */
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // no need to create root directory, that is done automatically now
        MediaFileDirectory directory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        TestUtils.endSession(true);

        MediaFileDirectory directoryById = mfMgr
                .getMediaFileDirectory(directory.getId());
        assertEquals(directory, directoryById);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);
        assertEquals(directory, rootDirectory);

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
        TestUtils.endSession(true);
    }

    /**
     * Test getting list of all directories for a given user.
     */
    public void testGetMediaFileDirectories() throws Exception {

        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser2");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog2", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        MediaFileDirectory directory1 = new MediaFileDirectory(testWeblog,
                "dir1");
        mfMgr.createMediaFileDirectory(directory1);

        MediaFileDirectory directory2 = new MediaFileDirectory(testWeblog,
                "dir2");
        mfMgr.createMediaFileDirectory(directory2);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List<MediaFileDirectory> directories = mfMgr
                .getMediaFileDirectories(testWeblog);
        assertNotNull(directories);
        assertEquals(3, directories.size());
        assertTrue(containsName(directories, "default"));
        assertTrue(containsName(directories, "dir1"));
        assertTrue(containsName(directories, "dir2"));

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
        TestUtils.endSession(true);
    }

    /**
     * Test utility to determine whether the given list of directories contains
     * a directory of given path.
     * 
     */
    private boolean containsName(Collection<MediaFileDirectory> directories,
            String name) {
        for (MediaFileDirectory directory : directories) {
            if (name.equals(directory.getName())) {
                return true;
            }
        }
        return false;

    }

    /**
     * Test utility to determine whether the list of files contains a file with
     * given name.
     * 
     */
    private boolean containsFileWithName(Collection<MediaFile> files,
            String name) {
        for (MediaFile file : files) {
            if (name.equals(file.getName())) {
                return true;
            }
        }
        return false;

    }

    /**
     * Test deletion of media file
     */
    public void testDeleteMediaFile() throws Exception {
        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser4");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog4", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setName("test4.jpg");
        mediaFile.setNotes("This is a test image 4");
        mediaFile.setLength(3000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setContentType("image/jpeg");
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));

        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
        String id = mediaFile.getId();
        TestUtils.endSession(true);
        assertNotNull(id);
        assertNotNull(id.length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(id);

        assertEquals("test4.jpg", mediaFile1.getName());

        try {
            mfMgr.removeMediaFile(testWeblog, mediaFile1);
        } catch (Exception ignorable) {
            log.debug("ERROR removing media file", ignorable);
        }
        TestUtils.endSession(true);

        MediaFile mediaFile2 = mfMgr.getMediaFile(id);
        assertNull(mediaFile2);

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());

        String uploadsDirName = WebloggerConfig.getProperty("mediafiles.storage.dir");
        File flag = new File(uploadsDirName + File.separator
                + "migration-status.properties");
        flag.delete();
        
        TestUtils.endSession(true);
    }

    /**
     * Test creation of media file.
     */
    public void testCreateMediaFile() throws Exception {

        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser3");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog3", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setName("test.jpg");
        mediaFile.setNotes("This is a test image");
        mediaFile.setLength(2000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile.setContentType("image/jpeg");
        rootDirectory.getMediaFiles().add(mediaFile);

        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
        TestUtils.endSession(true);
        assertNotNull(mediaFile.getId());
        assertNotNull(mediaFile.getId().length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(mediaFile.getId());
        assertEquals("test.jpg", mediaFile1.getName());
        assertEquals("This is a test image", mediaFile1.getNotes());
        assertEquals(2000, mediaFile1.getLength());

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    /**
     * Test media file update
     */
    public void testUpdateMediaFile() throws Exception {
        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser5");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog5", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setName("test5.jpg");
        mediaFile.setNotes("This is a test image 5");
        mediaFile.setLength(3000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile.setContentType("image/jpeg");

        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());

        rootDirectory.getMediaFiles().add(mediaFile);
        String id = mediaFile.getId();
        TestUtils.endSession(true);
        assertNotNull(id);
        assertNotNull(id.length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(id);
        mediaFile1.setName("updated.gif");
        mediaFile1.setNotes("updated desc");
        mediaFile1.setContentType("image/gif");
        mfMgr.updateMediaFile(testWeblog, mediaFile1);
        TestUtils.endSession(true);

        MediaFile mediaFile2 = mfMgr.getMediaFile(id);
        assertEquals("updated.gif", mediaFile2.getName());
        assertEquals("updated desc", mediaFile2.getNotes());
        assertEquals("image/gif", mediaFile2.getContentType());

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    /**
     * Test media file and directory gets
     */
    public void testGetDirectoryContents() throws Exception {
        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser6");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog6", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        MediaFileDirectory directory1 = new MediaFileDirectory(testWeblog,
                "dir1");
        mfMgr.createMediaFileDirectory(directory1);

        MediaFileDirectory directory2 = new MediaFileDirectory(testWeblog,
                "dir2");
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(testWeblog,
                "dir3");
        mfMgr.createMediaFileDirectory(directory3);

        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setName("test6_1.jpg");
        mediaFile.setNotes("This is a test image 6.1");
        mediaFile.setLength(4000);
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setName("test6_2.jpg");
        mediaFile2.setNotes("This is a test image 6.2");
        mediaFile2.setLength(4000);
        mediaFile2.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile2.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile2, new RollerMessages());

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        List<MediaFileDirectory> childDirectories = testWeblog
                .getMediaFileDirectories();
        assertEquals(4, childDirectories.size());
        assertTrue(containsName(childDirectories, "dir1"));
        assertTrue(containsName(childDirectories, "dir2"));
        assertTrue(containsName(childDirectories, "dir3"));

        Set<MediaFile> mediaFiles = rootDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test6_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test6_2.jpg"));
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        assertTrue(rootDirectory.hasMediaFile("test6_1.jpg"));
        assertTrue(rootDirectory.hasMediaFile("test6_2.jpg"));

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    /**
     * Test moving files across directories.
     */
    public void testMoveDirectoryContents() throws Exception {

        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser11");
        testWeblog = TestUtils.setupWeblog("mediaFileTestUser11", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        try {

            MediaFileDirectory directory1 = new MediaFileDirectory(
                    testWeblog, "dir1");
            mfMgr.createMediaFileDirectory(directory1);
            String dir1Id = directory1.getId();

            MediaFileDirectory directory2 = new MediaFileDirectory(
                    testWeblog, "dir2");
            mfMgr.createMediaFileDirectory(directory2);

            MediaFileDirectory directory3 = new MediaFileDirectory(
                    testWeblog, "dir3");
            mfMgr.createMediaFileDirectory(directory3);
            //rootDirectory.getChildDirectories().add(directory3);
            
            TestUtils.endSession(true);
            
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

            MediaFile mediaFile = new MediaFile();
            mediaFile.setDirectory(rootDirectory);
            mediaFile.setName("test7_1.jpg");
            mediaFile.setNotes("This is a test image 7.1");
            mediaFile.setLength(4000);
            mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
            mediaFile.setContentType("image/jpeg");
            mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());

            MediaFile mediaFile2 = new MediaFile();
            mediaFile2.setDirectory(rootDirectory);
            mediaFile2.setName("test7_2.jpg");
            mediaFile2.setNotes("This is a test image 7.2");
            mediaFile2.setLength(4000);
            mediaFile2.setInputStream(getClass()
                    .getResourceAsStream(TEST_IMAGE));
            mediaFile2.setContentType("image/jpeg");
            mfMgr.createMediaFile(testWeblog, mediaFile2, new RollerMessages());

            TestUtils.endSession(true);

            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

            Set<MediaFile> mediaFiles = rootDirectory.getMediaFiles();
            assertEquals(2, mediaFiles.size());
            assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
            assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));

            MediaFileDirectory targetDirectory = mfMgr
                    .getMediaFileDirectory(dir1Id);
            mfMgr.moveMediaFiles(mediaFiles, targetDirectory);
            TestUtils.endSession(true);

            rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
            targetDirectory = mfMgr.getMediaFileDirectory(dir1Id);

            mediaFiles = targetDirectory.getMediaFiles();
            assertEquals(2, mediaFiles.size());
            assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
            assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));

            mediaFiles = rootDirectory.getMediaFiles();
            assertEquals(0, mediaFiles.size());

        } finally {
            TestUtils.endSession(true);
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        }
    }

    /**
     * Test deletion of media file folder association with named queries
     * 
     * This test fails but it should not, so Z'ed out not to run.
     */
    public void ZtestDirectoryDeleteAssociation() throws Exception {

        User testUser;
        Weblog testWeblog;
        testUser = TestUtils.setupUser("mediaFileTestUser12");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog12", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        MediaFileDirectory directory1 = new MediaFileDirectory(testWeblog,
                "dir1");
        mfMgr.createMediaFileDirectory(directory1);

        MediaFileDirectory directory2 = new MediaFileDirectory(testWeblog,
                "dir2");
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(testWeblog,
                "dir3");
        mfMgr.createMediaFileDirectory(directory3);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        List<MediaFileDirectory> childDirectories = testWeblog.getMediaFileDirectories();

        assertEquals(3, childDirectories.size());

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // Delete folder
        MediaFileDirectory directoryById = mfMgr
                .getMediaFileDirectory(directory1.getId());

        mfMgr.removeMediaFileDirectory(directoryById);
        TestUtils.endSession(true);

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
        TestUtils.endSession(true);
    }
}
