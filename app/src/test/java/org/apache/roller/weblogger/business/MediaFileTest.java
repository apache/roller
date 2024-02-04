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
 */
package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.jpa.JPAMediaFileManagerImpl;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.*;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
import org.apache.roller.weblogger.pojos.MediaFileFilter.SizeFilterType;
import org.apache.roller.weblogger.util.RollerMessages;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test media file related business operations.
 */
public class MediaFileTest  {

    public static Log log = LogFactory.getLog(MediaFileTest.class);
    // static final String runtimeEnv;
    public final static String TEST_IMAGE = "/hawk.jpg";

    public MediaFileTest() {
    }

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    /**
     * Test creation of directory by path
     */
    @Test
    public void testCreateMediaFileDirectoryByPath() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;

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

        /*
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
    @Test
    public void testCreateMediaFileDirectory() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;

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
    @Test
    public void testGetMediaFileDirectories() throws Exception {

        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser2");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog2", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        MediaFileDirectory directory2 = new MediaFileDirectory(testWeblog,
                "dir2", "directory 2" );
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(testWeblog,
                "dir3", "directory 3");
        mfMgr.createMediaFileDirectory(directory3);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List<MediaFileDirectory> directories = mfMgr
                .getMediaFileDirectories(testWeblog);
        assertNotNull(directories);
        assertEquals(3, directories.size());
        assertTrue(containsName(directories, "default"));
        assertTrue(containsName(directories, "dir2"));
        assertTrue(containsName(directories, "dir3"));

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
    @Test
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
        mediaFile.setDescription("This is a test image 4");
        mediaFile.setCopyrightText("test 4 copyright text");
        mediaFile.setSharedForGallery(false);
        mediaFile.setLength(3000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setWeblog(testWeblog);
        mediaFile.setContentType("image/jpeg");
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));

        // Add tags
        mediaFile.setTagsAsString("tst4work tst4home");

        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
        String id = mediaFile.getId();
        TestUtils.endSession(true);
        assertNotNull(id);
        assertNotNull(id.length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(id);

        assertEquals("test4.jpg", mediaFile1.getName());
        assertNotNull(mediaFile1.getTags());
        assertEquals(2, mediaFile1.getTags().size());

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

        String uploadsDirName = WebloggerConfig.getProperty("uploads.dir");
        File flag = new File(uploadsDirName + File.separator
                + "migration-status.properties");
        flag.delete();
        
        TestUtils.endSession(true);
    }

    /**
     * Test creation of media file.
     */
    @Test
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
        mediaFile.setDescription("This is a test image");
        mediaFile.setCopyrightText("test copyright text");
        mediaFile.setSharedForGallery(true);
        mediaFile.setLength(2000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setWeblog(testWeblog);
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile.setContentType("image/jpeg");
        rootDirectory.getMediaFiles().add(mediaFile);

        // Add tags
        mediaFile.setTagsAsString("work home");

        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
        TestUtils.endSession(true);
        assertNotNull(mediaFile.getId());
        assertNotNull(mediaFile.getId().length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(mediaFile.getId());
        assertEquals("test.jpg", mediaFile1.getName());
        assertEquals("This is a test image", mediaFile1.getDescription());
        assertEquals("test copyright text", mediaFile1.getCopyrightText());
        assertTrue(mediaFile1.getSharedForGallery());
        assertEquals(2000, mediaFile1.getLength());

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    /**
     * Test searching media file.
     */
    @Test
    public void testSearchMediaFile() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser7");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog7", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        try {

            String id1 = null;
            {
                MediaFile mf = new MediaFile();
                mf.setName("test_work.jpg");
                mf.setDescription("This is a test image");
                mf.setCopyrightText("test copyright text");
                mf.setSharedForGallery(true);
                mf.setLength(2000);
                mf.setDirectory(rootDirectory);
                mf.setWeblog(testWeblog);
                mf.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
                mf.setContentType("image/jpeg");
                rootDirectory.getMediaFiles().add(mf);

                mfMgr.createMediaFile(testWeblog, mf, new RollerMessages());

                // Add tags
                mf.setTagsAsString("work");
                mfMgr.updateMediaFile(testWeblog, mf);

                mfMgr.createMediaFile(testWeblog, mf, new RollerMessages());
                TestUtils.endSession(true);
                id1 = mf.getId();
                assertNotNull(mf.getId());
                assertNotNull(mf.getId().length() > 0);
            }

            String id2 = null;
            {
                testWeblog = TestUtils.getManagedWebsite(testWeblog);
                rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory
                        .getId());

                MediaFile mf = new MediaFile();
                mf = new MediaFile();
                mf.setName("test_home.jpg");
                mf.setDescription("This is a test image");
                mf.setCopyrightText("test copyright text");
                mf.setSharedForGallery(true);
                mf.setLength(3000);
                mf.setDirectory(rootDirectory);
                mf.setWeblog(testWeblog);
                mf.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
                mf.setContentType("image/jpeg");
                rootDirectory.getMediaFiles().add(mf);

                mfMgr.createMediaFile(testWeblog, mf, new RollerMessages());

                // Add tags
                mf.setTagsAsString("home");
                mfMgr.updateMediaFile(testWeblog, mf);

                TestUtils.endSession(true);
                id2 = mf.getId();
                assertNotNull(mf.getId());
                assertNotNull(mf.getId().length() > 0);
            }

            String id3 = null;
            {
                testWeblog = TestUtils.getManagedWebsite(testWeblog);
                rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory
                        .getId());

                MediaFile mf = new MediaFile();
                mf = new MediaFile();
                mf.setName("test_pers.jpg");
                mf.setDescription("This is a personal test image");
                mf.setCopyrightText("test pers copyright text");
                mf.setSharedForGallery(true);
                mf.setLength(4000);
                mf.setWeblog(testWeblog);
                mf.setDirectory(rootDirectory);
                mf.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
                mf.setContentType("image/jpeg");
                rootDirectory.getMediaFiles().add(mf);

                mfMgr.createMediaFile(testWeblog, mf, new RollerMessages());

                // Add tags
                mf.setTagsAsString("home");
                mfMgr.updateMediaFile(testWeblog, mf);

                TestUtils.endSession(true);
                id3 = mf.getId();
                assertNotNull(mf.getId());
                assertNotNull(mf.getId().length() > 0);
            }

            testWeblog = TestUtils.getManagedWebsite(testWeblog);

            List<MediaFile> searchResults;

            // search by name

            MediaFileFilter filter1 = new MediaFileFilter();
            filter1.setName("mytest.jpg");
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter1);
            assertTrue(searchResults.isEmpty());

            MediaFileFilter filter2 = new MediaFileFilter();
            filter2.setName("test_home.jpg");
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter2);
            assertFalse(searchResults.isEmpty());
            assertEquals(id2, (searchResults.get(0)).getId());
            assertNotNull((searchResults.get(0)).getDirectory());
            assertEquals("default", searchResults.get(0).getDirectory().getName());

            MediaFileFilter filter3 = new MediaFileFilter();
            filter3.setName("test_work.jpg");
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter3);
            assertFalse(searchResults.isEmpty());
            assertEquals(id1, searchResults.get(0).getId());

            // search by tag

            // must be tickling an OpenJPA bug. this tag query works the
            // first time and then fails the second time it is run

            // MediaFileFilter filter5 = new MediaFileFilter();
            // filter5.setTags(Arrays.asList("home"));
            // searchResults = mfMgr.searchMediaFiles(testWeblog, filter5);
            // assertFalse(searchResults.isEmpty());
            // assertEquals(2, searchResults.size());
            //
            // MediaFileFilter filter51 = new MediaFileFilter();
            // filter51.setTags(Arrays.asList("home"));
            // searchResults = mfMgr.searchMediaFiles(testWeblog, filter51);
            // assertFalse(searchResults.isEmpty());
            // assertEquals(2, searchResults.size());

            MediaFileFilter filter4 = new MediaFileFilter();
            filter4.setTags(Arrays.asList("work"));
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter4);
            assertFalse(searchResults.isEmpty());
            assertEquals(1, searchResults.size());
            assertEquals("test_work.jpg", searchResults.get(0).getName());

            // search by size

            MediaFileFilter filter6 = new MediaFileFilter();
            filter6.setSize(3000);
            filter6.setSizeFilterType(MediaFileFilter.SizeFilterType.LT);
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter6);
            assertFalse(searchResults.isEmpty());
            assertEquals(1, searchResults.size());
            assertEquals("test_work.jpg", searchResults.get(0).getName());

            MediaFileFilter filter7 = new MediaFileFilter();
            filter7.setSize(3000);
            filter7.setSizeFilterType(MediaFileFilter.SizeFilterType.EQ);
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter7);
            assertFalse(searchResults.isEmpty());
            assertEquals(1, searchResults.size());
            assertEquals("test_home.jpg", searchResults.get(0).getName());

            MediaFileFilter filter8 = new MediaFileFilter();
            filter8.setSize(3000);
            filter8.setSizeFilterType(MediaFileFilter.SizeFilterType.GT);
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter8);
            assertFalse(searchResults.isEmpty());
            assertEquals(1, searchResults.size());
            assertEquals("test_pers.jpg", searchResults.get(0).getName());

            MediaFileFilter filter9 = new MediaFileFilter();
            filter9.setSize(3000);
            filter9.setSizeFilterType(MediaFileFilter.SizeFilterType.LTE);
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter9);
            assertFalse(searchResults.isEmpty());
            assertEquals(2, searchResults.size());

            MediaFileFilter filter10 = new MediaFileFilter();
            filter10.setSize(3000);
            filter10.setSizeFilterType(MediaFileFilter.SizeFilterType.GTE);
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter10);
            assertFalse(searchResults.isEmpty());
            assertEquals(2, searchResults.size());

            // search by type

            MediaFileFilter filter11 = new MediaFileFilter();
            filter11.setType(MediaFileType.IMAGE);
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter11);
            assertFalse(searchResults.isEmpty());
            assertEquals(3, searchResults.size());

            MediaFileFilter filter12 = new MediaFileFilter();
            filter12.setType(MediaFileType.IMAGE);
            filter12.setTags(Arrays.asList("home"));
            searchResults = mfMgr.searchMediaFiles(testWeblog, filter12);
            assertFalse(searchResults.isEmpty());
            assertEquals(2, searchResults.size());

        } finally {
            TestUtils.endSession(true);
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
        }

    }

    /**
     * Test searching media file with paging logic.
     */
    @Test
    public void testSearchMediaFilePaging() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser9");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog9", testUser);

        try {
            MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();

            // no need to create root directory, that is done automatically now
            MediaFileDirectory rootDirectory = mfMgr
                    .getDefaultMediaFileDirectory(testWeblog);

            for (int i = 0; i < 15; i++) {
                rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory
                        .getId());
                testWeblog = TestUtils.getManagedWebsite(testWeblog);
                MediaFile mediaFile = new MediaFile();
                mediaFile.setName("test_file<index>.jpg".replace("<index>", i
                        + ""));
                mediaFile.setDescription("This is a test image");
                mediaFile.setCopyrightText("test copyright text");
                mediaFile.setSharedForGallery(true);
                mediaFile.setLength(2000);
                mediaFile.setWeblog(testWeblog);
                mediaFile.setInputStream(getClass().getResourceAsStream(
                        TEST_IMAGE));
                mediaFile.setContentType("image/jpeg");
                mediaFile.setDirectory(rootDirectory);
                mfMgr.createMediaFile(testWeblog, mediaFile,
                        new RollerMessages());
                rootDirectory.getMediaFiles().add(mediaFile);
                assertNotNull(mediaFile.getId());
                assertNotNull(mediaFile.getId().length() > 0);
                TestUtils.endSession(true);
            }

            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            MediaFileFilter filter1 = new MediaFileFilter();
            filter1.setSize(1000);
            filter1.setSizeFilterType(SizeFilterType.GT);
            List<MediaFile> searchResults1 = mfMgr.searchMediaFiles(testWeblog,
                    filter1);
            assertFalse(searchResults1.isEmpty());
            assertEquals(15, searchResults1.size());

            MediaFileFilter filter2 = new MediaFileFilter();
            filter2.setSize(1000);
            filter2.setSizeFilterType(SizeFilterType.GT);
            filter2.setStartIndex(5);
            filter2.setLength(3);
            List<MediaFile> searchResults2 = mfMgr.searchMediaFiles(testWeblog,
                    filter2);
            assertFalse(searchResults2.isEmpty());
            assertEquals(3, searchResults2.size());
            assertEquals("test_file13.jpg", searchResults2.get(0).getName());

            MediaFileFilter filter3 = new MediaFileFilter();
            filter3.setSize(1000);
            filter3.setSizeFilterType(SizeFilterType.GT);
            filter3.setStartIndex(13);
            filter3.setLength(6);
            List<MediaFile> searchResults3 = mfMgr.searchMediaFiles(testWeblog,
                    filter3);
            assertFalse(searchResults3.isEmpty());
            assertEquals(2, searchResults3.size());
            assertEquals("test_file8.jpg", searchResults3.get(0).getName());

            MediaFileFilter filter4 = new MediaFileFilter();
            filter4.setSize(1000);
            filter4.setSizeFilterType(SizeFilterType.GT);
            filter4.setStartIndex(14);
            filter4.setLength(1);
            List<MediaFile> searchResults4 = mfMgr.searchMediaFiles(testWeblog,
                    filter4);
            assertFalse(searchResults4.isEmpty());
            assertEquals(1, searchResults4.size());
            assertEquals("test_file9.jpg", searchResults4.get(0).getName());

            TestUtils.endSession(true);
        } finally {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
        }
    }

    /**
     * Test searching media file with paging logic.
     */
    @Test
    public void testSearchMediaFileOrderBy() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser10");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog10", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        String[] contentTypes = { "image/gif", "image/jpeg", "image/bmp" };
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(System.currentTimeMillis()));
        for (int i = 0; i < 3; i++) {
            MediaFile mediaFile = new MediaFile();
            mediaFile
                    .setName("test_file<index>.jpg".replace("<index>", i + ""));
            mediaFile.setDescription("This is a test image");
            mediaFile.setCopyrightText("test copyright text");
            mediaFile.setSharedForGallery(true);
            mediaFile.setLength(2000);
            mediaFile.setDirectory(rootDirectory);
            mediaFile.setWeblog(testWeblog);
            mediaFile
                    .setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
            mediaFile.setContentType(contentTypes[i]);

            mediaFile.setDateUploaded(new Timestamp(cal.getTimeInMillis()));
            // Add one second for date sql on mysql
            cal.add(Calendar.SECOND, 1);
            mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
            rootDirectory.getMediaFiles().add(mediaFile);
            assertNotNull(mediaFile.getId());
            assertNotNull(mediaFile.getId().length() > 0);
        }
        TestUtils.endSession(true);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        MediaFileFilter filter1 = new MediaFileFilter();
        filter1.setSize(1000);
        filter1.setSizeFilterType(SizeFilterType.GT);
        filter1.setOrder(MediaFileOrder.NAME);
        List<MediaFile> searchResults1 = mfMgr.searchMediaFiles(testWeblog,
                filter1);
        assertFalse(searchResults1.isEmpty());
        assertEquals(3, searchResults1.size());
        assertEquals("test_file0.jpg", searchResults1.get(0).getName());
        assertEquals("test_file1.jpg", searchResults1.get(1).getName());
        assertEquals("test_file2.jpg", searchResults1.get(2).getName());

        MediaFileFilter filter2 = new MediaFileFilter();
        filter2.setSize(1000);
        filter2.setSizeFilterType(SizeFilterType.GT);
        filter2.setOrder(MediaFileOrder.TYPE);
        List<MediaFile> searchResults2 = mfMgr.searchMediaFiles(testWeblog,
                filter2);
        assertFalse(searchResults2.isEmpty());
        assertEquals(3, searchResults2.size());
        assertEquals("test_file2.jpg", searchResults2.get(0).getName());
        assertEquals("test_file0.jpg", searchResults2.get(1).getName());
        assertEquals("test_file1.jpg", searchResults2.get(2).getName());

        MediaFileFilter filter3 = new MediaFileFilter();
        filter3.setSize(1000);
        filter3.setSizeFilterType(SizeFilterType.GT);
        filter3.setOrder(MediaFileOrder.DATE_UPLOADED);
        List<MediaFile> searchResults3 = mfMgr.searchMediaFiles(testWeblog,
                filter3);
        assertFalse(searchResults3.isEmpty());
        assertEquals(3, searchResults3.size());
        assertEquals("test_file0.jpg", searchResults3.get(0).getName());
        assertEquals("test_file1.jpg", searchResults3.get(1).getName());
        assertEquals("test_file2.jpg", searchResults3.get(2).getName());

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    /**
     * Test media file update
     */
    @Test
    public void testUpdateMediaFile() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
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
        mediaFile.setDescription("This is a test image 5");
        mediaFile.setCopyrightText("test 5 copyright text");
        mediaFile.setSharedForGallery(false);
        mediaFile.setLength(3000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setWeblog(testWeblog);
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile.setContentType("image/jpeg");

        // Add tags
        mediaFile.setTagsAsString("tst5work tst5home");

        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
        rootDirectory.getMediaFiles().add(mediaFile);
        String id = mediaFile.getId();
        TestUtils.endSession(true);
        assertNotNull(id);
        assertNotNull(id.length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(id);
        mediaFile1.setWeblog(testWeblog);
        mediaFile1.setName("updated.gif");
        mediaFile1.setDescription("updated desc");
        mediaFile1.setCopyrightText("updated copyright");
        mediaFile1.setContentType("image/gif");
        mediaFile1.setSharedForGallery(true);
        mfMgr.updateMediaFile(testWeblog, mediaFile1);
        TestUtils.endSession(true);

        MediaFile mediaFile2 = mfMgr.getMediaFile(id);
        assertEquals("updated.gif", mediaFile2.getName());
        assertEquals("updated desc", mediaFile2.getDescription());
        assertEquals("updated copyright", mediaFile2.getCopyrightText());
        assertEquals("image/gif", mediaFile2.getContentType());
        assertTrue(mediaFile2.getSharedForGallery());
        assertNotNull(mediaFile2.getTags());
        assertEquals(2, mediaFile2.getTags().size());

        TestUtils.endSession(true);
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    /**
     * Test media file and directory gets
     */
    @Test
    public void testGetDirectoryContents() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser6");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog6", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        MediaFileDirectory directory1 = new MediaFileDirectory(testWeblog,
                "dir1", "directory 1");
        mfMgr.createMediaFileDirectory(directory1);

        MediaFileDirectory directory2 = new MediaFileDirectory(testWeblog,
                "dir2", "directory 2");
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(testWeblog,
                "dir3", "directory 3");
        mfMgr.createMediaFileDirectory(directory3);

        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setWeblog(testWeblog);
        mediaFile.setName("test6_1.jpg");
        mediaFile.setDescription("This is a test image 6.1");
        mediaFile.setCopyrightText("test 6.1 copyright text");
        mediaFile.setSharedForGallery(false);
        mediaFile.setLength(4000);
        mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
        //rootDirectory.getMediaFiles().add(mediaFile);

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setWeblog(testWeblog);
        mediaFile2.setName("test6_2.jpg");
        mediaFile2.setDescription("This is a test image 6.2");
        mediaFile2.setCopyrightText("test 6.2 copyright text");
        mediaFile2.setSharedForGallery(true);
        mediaFile2.setLength(4000);
        mediaFile2.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
        mediaFile2.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile2, new RollerMessages());
        //rootDirectory.getMediaFiles().add(mediaFile2);

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
    @Test
    public void testMoveDirectoryContents() throws Exception {

        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser11");
        testWeblog = TestUtils.setupWeblog("mediaFileTestUser11", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        try {

            MediaFileDirectory directory1 = new MediaFileDirectory(
                    testWeblog, "dir1", "directory 1");
            mfMgr.createMediaFileDirectory(directory1);
            String dir1Id = directory1.getId();

            MediaFileDirectory directory2 = new MediaFileDirectory(
                    testWeblog, "dir2", "directory 2");
            mfMgr.createMediaFileDirectory(directory2);

            MediaFileDirectory directory3 = new MediaFileDirectory(
                    testWeblog, "dir3", "directory 3");
            mfMgr.createMediaFileDirectory(directory3);
            //rootDirectory.getChildDirectories().add(directory3);
            
            TestUtils.endSession(true);
            
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

            MediaFile mediaFile = new MediaFile();
            mediaFile.setDirectory(rootDirectory);
            mediaFile.setWeblog(testWeblog);
            mediaFile.setName("test7_1.jpg");
            mediaFile.setDescription("This is a test image 7.1");
            mediaFile.setCopyrightText("test 7.1 copyright text");
            mediaFile.setSharedForGallery(false);
            mediaFile.setLength(4000);
            mediaFile.setInputStream(getClass().getResourceAsStream(TEST_IMAGE));
            mediaFile.setContentType("image/jpeg");
            mfMgr.createMediaFile(testWeblog, mediaFile, new RollerMessages());
            //rootDirectory.getMediaFiles().add(mediaFile);

            MediaFile mediaFile2 = new MediaFile();
            mediaFile2.setDirectory(rootDirectory);
            mediaFile2.setWeblog(testWeblog);
            mediaFile2.setName("test7_2.jpg");
            mediaFile2.setDescription("This is a test image 7.2");
            mediaFile2.setCopyrightText("test 7.2 copyright text");
            mediaFile2.setSharedForGallery(true);
            mediaFile2.setLength(4000);
            mediaFile2.setInputStream(getClass()
                    .getResourceAsStream(TEST_IMAGE));
            mediaFile2.setContentType("image/jpeg");
            mfMgr.createMediaFile(testWeblog, mediaFile2, new RollerMessages());
            //rootDirectory.getMediaFiles().add(mediaFile2);

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

    @Test
    public void testStorageUpgrade() throws Exception {
        User testUser = null;
        Weblog testWeblog1 = null;
        Weblog testWeblog2 = null;
        String oldmax = "4";
        PropertiesManager pmgr = WebloggerFactory.getWeblogger()
                .getPropertiesManager();
        try {
            // set dir max limit high so we won't bump into it
            RuntimeConfigProperty prop = pmgr
                    .getProperty("uploads.dir.maxsize");
            oldmax = prop.getValue();
            prop.setValue("20");
            pmgr.saveProperty(prop);
            TestUtils.endSession(true);

            testUser = TestUtils.setupUser("mediaFileTestUser");
            testWeblog1 = TestUtils.setupWeblog("testblog1", testUser);
            testWeblog2 = TestUtils.setupWeblog("testblog2", testUser);
            
            TestUtils.endSession(true);

            MediaFileManager mgr = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            JPAMediaFileManagerImpl mmgr = (JPAMediaFileManagerImpl) mgr;

            assertTrue(mmgr.isFileStorageUpgradeRequired(), "Upgrade required" );

            mmgr.upgradeFileStorage();
            TestUtils.endSession(true);

            assertFalse(mmgr.isFileStorageUpgradeRequired(), "Upgrade required" );

            // now, let's check to see if migration was successful

            MediaFileDirectory root1 = mgr
                    .getDefaultMediaFileDirectory(testWeblog1);
            assertNotNull(root1, "testblog1's mediafile dir exists" );
            assertNotNull(mgr.getMediaFileByPath(testWeblog1, "/sub1/hawk.jpg"));
            assertNotNull(mgr.getMediaFileByPath(testWeblog1,
                    "/sub2/nasa.jpg"));
            assertNotNull(mgr.getMediaFileByPath(testWeblog1,
                    "/roller50-prop.png"));

            assertNotNull(mgr.getMediaFileByOriginalPath(testWeblog1,
                    "/sub1/hawk.jpg"));

            MediaFileDirectory root2 = mgr
                    .getDefaultMediaFileDirectory(testWeblog2);
            assertNotNull(root2, "testblog2's mediafile dir exists");
            assertNotNull(root2.getMediaFile("amsterdam.jpg"));
            assertNotNull(root2.getMediaFile("p47-thunderbolt.jpg"));
            assertNotNull(root2.getMediaFile("rollerwiki.png"));

        } finally {

            File statusFile = new File(
                    WebloggerConfig.getProperty("uploads.dir") + File.separator
                            + JPAMediaFileManagerImpl.MIGRATION_STATUS_FILENAME);
            statusFile.delete();

            // reset dir max to old value
            RuntimeConfigProperty prop = pmgr
                    .getProperty("uploads.dir.maxsize");
            prop.setValue(oldmax);
            pmgr.saveProperty(prop);

            TestUtils.endSession(true);
            TestUtils.teardownWeblog(testWeblog1.getId());
            TestUtils.teardownWeblog(testWeblog2.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        }
    }

    /**
     * Test deletion of media file folder association with named queries
     * 
     * This test fails but it should not, so Z'ed out not to run.
     */
    public void testDirectoryDeleteAssociation() throws Exception {

        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser12");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog12", testUser);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        // no need to create root directory, that is done automatically now
        MediaFileDirectory rootDirectory = mfMgr
                .getDefaultMediaFileDirectory(testWeblog);

        MediaFileDirectory directory1 = new MediaFileDirectory(testWeblog,
                "dir1", "directory 1");
        mfMgr.createMediaFileDirectory(directory1);

        MediaFileDirectory directory2 = new MediaFileDirectory(testWeblog,
                "dir2", "directory 2");
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(testWeblog,
                "dir3", "directory 3");
        mfMgr.createMediaFileDirectory(directory3);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

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