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


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.ant.StartDerbyTask;
import org.apache.roller.weblogger.ant.StopDerbyTask;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileTag;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
import org.apache.roller.weblogger.pojos.MediaFileFilter.SizeFilterType;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test User related business operations.
 */
public class MediaFileTest extends TestCase {
    
    public static Log log = LogFactory.getLog(MediaFileTest.class);
    static final String runtimeEnv;

    public MediaFileTest() {
    	// TODO: create setup and teardown classes and move test user/weblgo to them after resolving the issue with teardown.
    }
    
    public MediaFileTest(String name) {
    	super(name);
    }
    
    static {
    	runtimeEnv = System.getProperty("runtime-environment");
    	try {
			classInit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @BeforeClass
    public static void classInit() throws Exception {
    	if ("eclipse".equals(runtimeEnv)) {
        	startDatabase();
        	initDatabase();
    	}
    	else {
    		if ("true".equalsIgnoreCase(System.getProperty("debug-enabled"))) {
        		System.out.println("Sleeping for 5 seconds, you might want to start your debugger");
        		Thread.sleep(5000);
    		}
    	}
    }
    
    @AfterClass
    public static void classTearDown() {
    	if ("eclipse".equals(runtimeEnv)) {
        	stopDatabase();
    	}
    }

    public static void startDatabase() throws Exception {
    	StartDerbyTask startTask = new StartDerbyTask();
    	startTask.setDatabase("C:/Ganesh/edu/Project/sources/media-blogging-branch/apps/weblogger/build/tests/derby-system/roller");
    	startTask.setPort("3219");
    	startTask.execute();
    }

    public static void initDatabaseShort() throws Exception {
    	Properties props = new Properties();
    	props.load(new FileInputStream("C:/Ganesh/edu/Project/sources/media-blogging-branch/apps/weblogger/build.properties"));

    	SQLExec sqlTask1 = new SQLExec();
    	sqlTask1.setDriver(props.getProperty("test.db.driver"));
    	sqlTask1.setUrl(props.getProperty("test.db.url"));
    	sqlTask1.setUserid(props.getProperty("test.db.username"));
    	sqlTask1.setPassword(props.getProperty("test.db.password"));
    	sqlTask1.setSrc(new File("C:/Ganesh/edu/Project/sources/media-blogging-branch/apps/weblogger/src/sql/media_file_delete_data.sql"));
    	SQLExec.OnError onError1 = new SQLExec.OnError();
    	onError1.setValue("continue");
    	sqlTask1.setOnerror(onError1);
    	//sqlTask1.setClasspath(new Path());
    	sqlTask1.setProject(new Project());
    	sqlTask1.execute();
    }

    public static void initDatabase() throws Exception {
    	
    	Properties props = new Properties();
    	props.load(new FileInputStream("C:/Ganesh/edu/Project/sources/media-blogging-branch/apps/weblogger/build.properties"));

    	SQLExec sqlTask1 = new SQLExec();
    	sqlTask1.setDriver(props.getProperty("test.db.driver"));
    	sqlTask1.setUrl(props.getProperty("test.db.url"));
    	sqlTask1.setUserid(props.getProperty("test.db.username"));
    	sqlTask1.setPassword(props.getProperty("test.db.password"));
    	sqlTask1.setSrc(new File("C:/Ganesh/edu/Project/sources/media-blogging-branch/apps/weblogger/build/webapp/WEB-INF/classes/dbscripts/droptables.sql"));
    	SQLExec.OnError onError1 = new SQLExec.OnError();
    	onError1.setValue("continue");
    	sqlTask1.setOnerror(onError1);
    	//sqlTask1.setClasspath(new Path());
    	sqlTask1.setProject(new Project());
    	sqlTask1.execute();
    	
    	SQLExec sqlTask2 = new SQLExec();
    	sqlTask2.setDriver(props.getProperty("test.db.driver"));
    	sqlTask2.setUrl(props.getProperty("test.db.url"));
    	sqlTask2.setUserid(props.getProperty("test.db.username"));
    	sqlTask2.setPassword(props.getProperty("test.db.password"));
    	sqlTask2.setSrc(new File("C:/Ganesh/edu/Project/sources/media-blogging-branch/apps/weblogger/build/webapp/WEB-INF/classes/dbscripts/derby/createdb.sql"));
    	SQLExec.OnError onError2 = new SQLExec.OnError();
    	onError2.setValue("continue");
    	sqlTask2.setOnerror(onError2);
    	//sqlTask2.setClasspath(new Path());
    	sqlTask2.setProject(new Project());
    	sqlTask2.execute();
    }
    
    public static void stopDatabase() {
    	StopDerbyTask stopTask = new StopDerbyTask();
    	stopTask.setPort("3219");
    	stopTask.execute();
    }

    @Before
    public void setUpApp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    	/*
    	try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(this.testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        */
        
    }
    
    /**
     * Test directory creation
     */
    @Test
    public void testCreateMediaFileDirectoryByPath() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        
    	// TODO: Setup code, to be moved to setUp method.
    	System.out.println("Before setting up weblogger");
        // setup weblogger
        try {
            testUser = TestUtils.setupUser("mediaFileTestUser8");
            testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog8", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }

        /**
         * Real test starts here.
         */
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);
        assertNotNull(rootDirectory.getId() != null);
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        try {
            mfMgr.createMediaFileDirectoryByPath(testWeblog, "");
        	assertTrue(false);
        }
        catch (WebloggerException e) {
        	assertTrue(true);
        }
        
        try {
            mfMgr.createMediaFileDirectoryByPath(testWeblog, "/");
        	assertTrue(false);
        }
        catch (WebloggerException e) {
        	assertTrue(true);
        }
        
        MediaFileDirectory newDirectory1 = mfMgr.createMediaFileDirectoryByPath(testWeblog, "/test1");
        MediaFileDirectory newDirectory2 = mfMgr.createMediaFileDirectoryByPath(testWeblog, "/test2/");
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        MediaFileDirectory newDirectory1ById = mfMgr.getMediaFileDirectory(newDirectory1.getId());
        assertEquals(newDirectory1, newDirectory1ById);
        
        MediaFileDirectory newDirectory2ById = mfMgr.getMediaFileDirectory(newDirectory2.getId());
        assertEquals("test2", newDirectory2ById.getName());

        try {
            mfMgr.createMediaFileDirectoryByPath(testWeblog, "test1");
        	assertTrue(false);
        }
        catch (WebloggerException e) {
        	assertTrue(true);
        }

        try {
            mfMgr.createMediaFileDirectoryByPath(testWeblog, "/test1/test2/test3");
        	assertTrue(false);
        }
        catch (WebloggerException e) {
        	assertTrue(true);
        }
        
        MediaFileDirectory newDirectory3 = mfMgr.createMediaFileDirectoryByPath(testWeblog, "/test1/test2");
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory newDirectory3ById = mfMgr.getMediaFileDirectory(newDirectory3.getId());
        assertEquals(newDirectory3, newDirectory3ById);
        
        MediaFileDirectory newDirectory4 = mfMgr.createMediaFileDirectoryByPath(testWeblog, "/test1/test2/test3");
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory newDirectory4ById = mfMgr.getMediaFileDirectory(newDirectory4.getId());
        assertEquals(newDirectory4, newDirectory4ById);

        try {
            mfMgr.createMediaFileDirectoryByPath(testWeblog, "/test1/test2/test3");
        	assertTrue(false);
        }
        catch (WebloggerException e) {
        	assertTrue(true);
        }
    }
    
    /**
     * Test directory creation
     */
    @Test
    public void testCreateMediaFileDirectory() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        
    	// TODO: Setup code, to be moved to setUp method.
    	System.out.println("Before setting up weblogger");
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
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory directory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(directory);
        assertEquals("/", directory.getPath());
        assertNotNull(directory.getId() != null);
        System.out.println("The directory id is " + directory.getId());
        TestUtils.endSession(true);
        
        MediaFileDirectory directoryById = mfMgr.getMediaFileDirectory(directory.getId());
        assertEquals(directory, directoryById);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFileDirectory rootDirectory = mfMgr.getMediaFileRootDirectory(testWeblog);
        assertEquals(directory, rootDirectory);

    }
    
    
    
    /**
     * Test getting list of all directories for a given user.
     */
    public void testGetMediaFileDirectories() throws Exception {
        
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser2");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog2", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

        MediaFileDirectory directory2 = new MediaFileDirectory(rootDirectory, "dir2", "directory 2", testWeblog);
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(rootDirectory, "dir3", "directory 3", testWeblog);
        mfMgr.createMediaFileDirectory(directory3);
        
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List<MediaFileDirectory> directories = mfMgr.getMediaFileDirectories(testWeblog);
        assertNotNull(directories);
        assertEquals(3, directories.size());
        assertTrue(containsPath(directories, "/"));
        assertTrue(containsPath(directories, "/dir2"));
        assertTrue(containsPath(directories, "/dir3"));
        
    }
    
    private boolean containsPath(Collection<MediaFileDirectory> directories, String path) {
    	for (MediaFileDirectory directory: directories) {
    		if (path.equals(directory.getPath())) return true;
    	}
    	return false;
    	
    }
    
    private boolean containsFileWithName(Collection<MediaFile> files, String name) {
    	for (MediaFile file: files) {
    		if (name.equals(file.getName())) return true;
    	}
    	return false;
    	
    }
    
    /**
     * Test deletion of media file
     */
    public void testDeleteMediaFile() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser4");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog4", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

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
        mediaFile.setContentType("image/jpeg");
        mediaFile.setInputStream(getClass().getResourceAsStream("/test4.jpg"));
        
        MediaFileTag tag1 = new MediaFileTag("tst4work", mediaFile);
        MediaFileTag tag2 = new MediaFileTag("tst4home", mediaFile);
        Set<MediaFileTag> tags = new HashSet<MediaFileTag>();
        tags.add(tag1);
        tags.add(tag2);
        mediaFile.setTags(tags);
        
        mfMgr.createMediaFile(testWeblog, mediaFile);
        String id = mediaFile.getId();
        TestUtils.endSession(true);
        assertNotNull(id);
        assertNotNull(id.length() > 0);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(id);
        
        assertEquals("test4.jpg", mediaFile1.getName());
        assertNotNull(mediaFile1.getTags());
        assertEquals(2, mediaFile1.getTags().size());
        
        mfMgr.removeMediaFile(testWeblog, mediaFile1);
        TestUtils.endSession(true);
        
        MediaFile mediaFile2 = mfMgr.getMediaFile(id);
        assertNull(mediaFile2);
    	
    }

    /**
     * Test creation of media file.
     */
    public void testCreateMediaFile() throws Exception {
        
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser3");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog3", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

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
        mediaFile.setInputStream(getClass().getResourceAsStream("/test.jpg"));
        mediaFile.setContentType("image/jpeg");
        
        MediaFileTag tag1 = new MediaFileTag("work", mediaFile);
        MediaFileTag tag2 = new MediaFileTag("home", mediaFile);
        Set<MediaFileTag> tags = new HashSet<MediaFileTag>();
        tags.add(tag1);
        tags.add(tag2);
        mediaFile.setTags(tags);
        
        mfMgr.createMediaFile(testWeblog, mediaFile);
        TestUtils.endSession(true);
        assertNotNull(mediaFile.getId());
        assertNotNull(mediaFile.getId().length() > 0);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(mediaFile.getId());
        assertEquals("test.jpg", mediaFile1.getName());
        assertEquals("This is a test image", mediaFile1.getDescription());
        assertEquals("test copyright text", mediaFile1.getCopyrightText());
        assertTrue(mediaFile1.isSharedForGallery());
        assertEquals(2000, mediaFile1.getLength());
    }

    /**
     * Test searching media file.
     */
    public void testSearchMediaFile() throws Exception {
        
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser7");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog7", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        
        MediaFile mediaFile = new MediaFile();
        mediaFile.setName("test_work.jpg");
        mediaFile.setDescription("This is a test image");
        mediaFile.setCopyrightText("test copyright text");
        mediaFile.setSharedForGallery(true);
        mediaFile.setLength(2000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setInputStream(getClass().getResourceAsStream("/test.jpg"));
        mediaFile.setContentType("image/jpeg");
        
        MediaFileTag tag1 = new MediaFileTag("work", mediaFile);
        Set<MediaFileTag> tags = new HashSet<MediaFileTag>();
        tags.add(tag1);
        mediaFile.setTags(tags);
        
        mfMgr.createMediaFile(testWeblog, mediaFile);
        TestUtils.endSession(true);
        assertNotNull(mediaFile.getId());
        assertNotNull(mediaFile.getId().length() > 0);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = new MediaFile();
        mediaFile1 = new MediaFile();
        mediaFile1.setName("test_home.jpg");
        mediaFile1.setDescription("This is a test image");
        mediaFile1.setCopyrightText("test copyright text");
        mediaFile1.setSharedForGallery(true);
        mediaFile1.setLength(3000);
        mediaFile1.setDirectory(rootDirectory);
        mediaFile1.setInputStream(getClass().getResourceAsStream("/test.jpg"));
        mediaFile1.setContentType("image/jpeg");
        
        MediaFileTag tag2 = new MediaFileTag("home", mediaFile1);
        tags = new HashSet<MediaFileTag>();
        tags.add(tag2);
        mediaFile1.setTags(tags);
        
        mfMgr.createMediaFile(testWeblog, mediaFile1);
        TestUtils.endSession(true);
        assertNotNull(mediaFile1.getId());
        assertNotNull(mediaFile1.getId().length() > 0);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile2 = new MediaFile();
        mediaFile2 = new MediaFile();
        mediaFile2.setName("test_pers.jpg");
        mediaFile2.setDescription("This is a personal test image");
        mediaFile2.setCopyrightText("test pers copyright text");
        mediaFile2.setSharedForGallery(true);
        mediaFile2.setLength(4000);
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setInputStream(getClass().getResourceAsStream("/test.jpg"));
        mediaFile2.setContentType("image/jpeg");
        
        MediaFileTag tag3 = new MediaFileTag("home", mediaFile2);
        tags = new HashSet<MediaFileTag>();
        tags.add(tag3);
        mediaFile2.setTags(tags);
        
        mfMgr.createMediaFile(testWeblog, mediaFile2);
        TestUtils.endSession(true);
        assertNotNull(mediaFile2.getId());
        assertNotNull(mediaFile2.getId().length() > 0);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        List<MediaFile> searchResults;
        
        MediaFileFilter filter1 = new MediaFileFilter();
        filter1.setName("mytest.jpg");
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter1);
        assertTrue(searchResults.isEmpty());
        
        MediaFileFilter filter2 = new MediaFileFilter();
        filter2.setName("test_home.jpg");
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter2);
        assertFalse(searchResults.isEmpty());
        assertEquals(mediaFile1.getId(), ((MediaFile)searchResults.get(0)).getId());
        assertNotNull(((MediaFile)searchResults.get(0)).getDirectory());
        assertEquals("/", ((MediaFile)searchResults.get(0)).getDirectory().getPath());

        MediaFileFilter filter3 = new MediaFileFilter();
        filter3.setName("test_work.jpg");
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter3);
        assertFalse(searchResults.isEmpty());
        assertEquals(mediaFile.getId(), ((MediaFile)searchResults.get(0)).getId());

        MediaFileFilter filter4 = new MediaFileFilter();
        filter4.setTags(Arrays.asList("work"));
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter4);
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
        assertEquals("test_work.jpg", ((MediaFile)searchResults.get(0)).getName());
        
        MediaFileFilter filter5 = new MediaFileFilter();
        filter5.setTags(Arrays.asList("home"));
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter5);
        assertFalse(searchResults.isEmpty());
        assertEquals(2, searchResults.size());

        MediaFileFilter filter6 = new MediaFileFilter();
        filter6.setSize(3000);
        filter6.setSizeFilterType(MediaFileFilter.SizeFilterType.LT);
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter6);
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
        assertEquals("test_work.jpg", ((MediaFile)searchResults.get(0)).getName());

        MediaFileFilter filter7 = new MediaFileFilter();
        filter7.setSize(3000);
        filter7.setSizeFilterType(MediaFileFilter.SizeFilterType.EQ);
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter7);
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
        assertEquals("test_home.jpg", ((MediaFile)searchResults.get(0)).getName());

        MediaFileFilter filter8 = new MediaFileFilter();
        filter8.setSize(3000);
        filter8.setSizeFilterType(MediaFileFilter.SizeFilterType.GT);
        searchResults = mfMgr.searchMediaFiles(testWeblog, filter8);
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
        assertEquals("test_pers.jpg", ((MediaFile)searchResults.get(0)).getName());

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
    }

    /**
     * Test searching media file with paging logic.
     */
    public void testSearchMediaFilePaging() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser9");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog9", testUser);
    	
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());

        for (int i = 0; i < 15; i ++) {
            MediaFile mediaFile = new MediaFile();
            mediaFile.setName("test_file<index>.jpg".replace("<index>", i + ""));
            mediaFile.setDescription("This is a test image");
            mediaFile.setCopyrightText("test copyright text");
            mediaFile.setSharedForGallery(true);
            mediaFile.setLength(2000);
            mediaFile.setDirectory(rootDirectory);
            mediaFile.setInputStream(getClass().getResourceAsStream("/test.jpg"));
            mediaFile.setContentType("image/jpeg");
            mfMgr.createMediaFile(testWeblog, mediaFile);
            assertNotNull(mediaFile.getId());
            assertNotNull(mediaFile.getId().length() > 0);
        }
        TestUtils.endSession(true);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        
        MediaFileFilter filter1 = new MediaFileFilter();
        filter1.setSize(1000);
        filter1.setSizeFilterType(SizeFilterType.GT);
        List<MediaFile> searchResults1 = mfMgr.searchMediaFiles(testWeblog, filter1);
        assertFalse(searchResults1.isEmpty());
        assertEquals(15, searchResults1.size());
        
        MediaFileFilter filter2 = new MediaFileFilter();
        filter2.setSize(1000);
        filter2.setSizeFilterType(SizeFilterType.GT);
        filter2.setStartIndex(5);
        filter2.setLength(3);
        List<MediaFile> searchResults2 = mfMgr.searchMediaFiles(testWeblog, filter2);
        assertFalse(searchResults2.isEmpty());
        assertEquals(3, searchResults2.size());
        assertEquals("test_file5.jpg", searchResults2.get(0).getName());

        MediaFileFilter filter3 = new MediaFileFilter();
        filter3.setSize(1000);
        filter3.setSizeFilterType(SizeFilterType.GT);
        filter3.setStartIndex(13);
        filter3.setLength(6);
        List<MediaFile> searchResults3 = mfMgr.searchMediaFiles(testWeblog, filter3);
        assertFalse(searchResults3.isEmpty());
        assertEquals(2, searchResults3.size());
        assertEquals("test_file13.jpg", searchResults3.get(0).getName());

        MediaFileFilter filter4 = new MediaFileFilter();
        filter4.setSize(1000);
        filter4.setSizeFilterType(SizeFilterType.GT);
        filter4.setStartIndex(14);
        filter4.setLength(1);
        List<MediaFile> searchResults4 = mfMgr.searchMediaFiles(testWeblog, filter4);
        assertFalse(searchResults4.isEmpty());
        assertEquals(1, searchResults4.size());
        assertEquals("test_file14.jpg", searchResults4.get(0).getName());
    }
    
    /**
     * Test searching media file with paging logic.
     */
    public void testSearchMediaFileOrderBy() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser10");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog10", testUser);
    	
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        String[] contentTypes = {"image/gif", "image/jpeg", "image/bmp"};
        for (int i = 0; i < 3; i ++) {
            MediaFile mediaFile = new MediaFile();
            mediaFile.setName("test_file<index>.jpg".replace("<index>", i + ""));
            mediaFile.setDescription("This is a test image");
            mediaFile.setCopyrightText("test copyright text");
            mediaFile.setSharedForGallery(true);
            mediaFile.setLength(2000);
            mediaFile.setDirectory(rootDirectory);
            mediaFile.setInputStream(getClass().getResourceAsStream("/test.jpg"));
            mediaFile.setContentType(contentTypes[i]);
            mfMgr.createMediaFile(testWeblog, mediaFile);
            assertNotNull(mediaFile.getId());
            assertNotNull(mediaFile.getId().length() > 0);
        }
        TestUtils.endSession(true);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        
        MediaFileFilter filter1 = new MediaFileFilter();
        filter1.setSize(1000);
        filter1.setSizeFilterType(SizeFilterType.GT);
        filter1.setOrder(MediaFileOrder.NAME);
        List<MediaFile> searchResults1 = mfMgr.searchMediaFiles(testWeblog, filter1);
        assertFalse(searchResults1.isEmpty());
        assertEquals(3, searchResults1.size());
        assertEquals("test_file0.jpg", searchResults1.get(0).getName());
        assertEquals("test_file1.jpg", searchResults1.get(1).getName());
        assertEquals("test_file2.jpg", searchResults1.get(2).getName());

        MediaFileFilter filter2 = new MediaFileFilter();
        filter2.setSize(1000);
        filter2.setSizeFilterType(SizeFilterType.GT);
        filter2.setOrder(MediaFileOrder.TYPE);
        List<MediaFile> searchResults2 = mfMgr.searchMediaFiles(testWeblog, filter2);
        assertFalse(searchResults2.isEmpty());
        assertEquals(3, searchResults2.size());
        assertEquals("test_file2.jpg", searchResults2.get(0).getName());
        assertEquals("test_file1.jpg", searchResults2.get(1).getName());
        assertEquals("test_file0.jpg", searchResults2.get(2).getName());

        MediaFileFilter filter3 = new MediaFileFilter();
        filter3.setSize(1000);
        filter3.setSizeFilterType(SizeFilterType.GT);
        filter3.setOrder(MediaFileOrder.DATE_UPLOADED);
        List<MediaFile> searchResults3 = mfMgr.searchMediaFiles(testWeblog, filter3);
        assertFalse(searchResults3.isEmpty());
        assertEquals(3, searchResults3.size());
        assertEquals("test_file0.jpg", searchResults3.get(0).getName());
        assertEquals("test_file1.jpg", searchResults3.get(1).getName());
        assertEquals("test_file2.jpg", searchResults3.get(2).getName());
    }
    
    /**
     * Test media file update
     */
    public void testUpdateMediaFile() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser5");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog5", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

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
        mediaFile.setInputStream(getClass().getResourceAsStream("/test5.jpg"));
        mediaFile.setContentType("image/jpeg");
        
        
        MediaFileTag tag1 = new MediaFileTag("tst5work", mediaFile);
        MediaFileTag tag2 = new MediaFileTag("tst5home", mediaFile);
        Set<MediaFileTag> tags = new HashSet<MediaFileTag>();
        tags.add(tag1);
        tags.add(tag2);
        mediaFile.setTags(tags);
        
        mfMgr.createMediaFile(testWeblog, mediaFile);
        String id = mediaFile.getId();
        TestUtils.endSession(true);
        assertNotNull(id);
        assertNotNull(id.length() > 0);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        MediaFile mediaFile1 = mfMgr.getMediaFile(id);
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
        assertTrue(mediaFile2.isSharedForGallery());
        assertNotNull(mediaFile2.getTags());
        assertEquals(2, mediaFile2.getTags().size());
    }

    /**
     * Test media file update
     */
    public void testGetDirectoryContents() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser6");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog6", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

        MediaFileDirectory directory1 = new MediaFileDirectory(rootDirectory, "dir1", "directory 1", testWeblog);
        mfMgr.createMediaFileDirectory(directory1);

        MediaFileDirectory directory2 = new MediaFileDirectory(rootDirectory, "dir2", "directory 2", testWeblog);
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(rootDirectory, "dir3", "directory 3", testWeblog);
        mfMgr.createMediaFileDirectory(directory3);
        
        
        MediaFile mediaFile = new MediaFile();
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setName("test6_1.jpg");
        mediaFile.setDescription("This is a test image 6.1");
        mediaFile.setCopyrightText("test 6.1 copyright text");
        mediaFile.setSharedForGallery(false);
        mediaFile.setLength(4000);
        mediaFile.setInputStream(getClass().getResourceAsStream("/test6.jpg"));
        mediaFile.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile);

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setName("test6_2.jpg");
        mediaFile2.setDescription("This is a test image 6.2");
        mediaFile2.setCopyrightText("test 6.2 copyright text");
        mediaFile2.setSharedForGallery(true);
        mediaFile2.setLength(4000);
        mediaFile2.setInputStream(getClass().getResourceAsStream("/test6.jpg"));
        mediaFile2.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile2);
        
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        
        Set<MediaFileDirectory> childDirectories = rootDirectory.getChildDirectories();
        assertEquals(3, childDirectories.size());
        assertTrue(containsPath(childDirectories, "/dir1"));
        assertTrue(containsPath(childDirectories, "/dir2"));
        assertTrue(containsPath(childDirectories, "/dir3"));
        
        Set<MediaFile> mediaFiles = rootDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test6_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test6_2.jpg"));
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        assertTrue(rootDirectory.hasMediaFile("test6_1.jpg"));
        assertTrue(rootDirectory.hasMediaFile("test6_2.jpg"));
        
    }

    /**
     * Test media file update
     */
    public void testMoveDirectoryContents() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        testUser = TestUtils.setupUser("mediaFileTestUser7");
        testWeblog = TestUtils.setupWeblog("mediaFileTestWeblog7", testUser);
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root d", testWeblog);
        mfMgr.createMediaFileDirectory(rootDirectory);

        MediaFileDirectory directory1 = new MediaFileDirectory(rootDirectory, "dir1", "directory 1", testWeblog);
        mfMgr.createMediaFileDirectory(directory1);
        String dir1Id = directory1.getId();

        MediaFileDirectory directory2 = new MediaFileDirectory(rootDirectory, "dir2", "directory 2", testWeblog);
        mfMgr.createMediaFileDirectory(directory2);

        MediaFileDirectory directory3 = new MediaFileDirectory(rootDirectory, "dir3", "directory 3", testWeblog);
        mfMgr.createMediaFileDirectory(directory3);
        
        
        MediaFile mediaFile = new MediaFile();
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setName("test7_1.jpg");
        mediaFile.setDescription("This is a test image 7.1");
        mediaFile.setCopyrightText("test 7.1 copyright text");
        mediaFile.setSharedForGallery(false);
        mediaFile.setLength(4000);
        mediaFile.setInputStream(getClass().getResourceAsStream("/test7.jpg"));
        mediaFile.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile);

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setName("test7_2.jpg");
        mediaFile2.setDescription("This is a test image 7.2");
        mediaFile2.setCopyrightText("test 7.2 copyright text");
        mediaFile2.setSharedForGallery(true);
        mediaFile2.setLength(4000);
        mediaFile2.setInputStream(getClass().getResourceAsStream("/test7.jpg"));
        mediaFile2.setContentType("image/jpeg");
        mfMgr.createMediaFile(testWeblog, mediaFile2);
        
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rootDirectory = mfMgr.getMediaFileDirectory(rootDirectory.getId());
        
        Set<MediaFile> mediaFiles = rootDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));
        
        MediaFileDirectory targetDirectory = mfMgr.getMediaFileDirectory(dir1Id);
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
        
    }
}
