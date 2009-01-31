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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.ant.StartDerbyTask;
import org.apache.roller.weblogger.ant.StopDerbyTask;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileTag;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
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
    	startTask.setDatabase("C:/Ganesh/edu/Project/sources/roller-svn/apps/weblogger/build/tests/derby-system/roller");
    	startTask.setPort("3219");
    	startTask.execute();
    }

    public static void initDatabaseShort() throws Exception {
    	Properties props = new Properties();
    	props.load(new FileInputStream("C:/Ganesh/edu/Project/sources/roller-svn/apps/weblogger/build.properties"));

    	SQLExec sqlTask1 = new SQLExec();
    	sqlTask1.setDriver(props.getProperty("test.db.driver"));
    	sqlTask1.setUrl(props.getProperty("test.db.url"));
    	sqlTask1.setUserid(props.getProperty("test.db.username"));
    	sqlTask1.setPassword(props.getProperty("test.db.password"));
    	sqlTask1.setSrc(new File("C:/Ganesh/edu/Project/sources/roller-svn/apps/weblogger/src/sql/media_file_delete_data.sql"));
    	SQLExec.OnError onError1 = new SQLExec.OnError();
    	onError1.setValue("continue");
    	sqlTask1.setOnerror(onError1);
    	//sqlTask1.setClasspath(new Path());
    	sqlTask1.setProject(new Project());
    	sqlTask1.execute();
    }

    public static void initDatabase() throws Exception {
    	
    	Properties props = new Properties();
    	props.load(new FileInputStream("C:/Ganesh/edu/Project/sources/roller-svn/apps/weblogger/build.properties"));

    	SQLExec sqlTask1 = new SQLExec();
    	sqlTask1.setDriver(props.getProperty("test.db.driver"));
    	sqlTask1.setUrl(props.getProperty("test.db.url"));
    	sqlTask1.setUserid(props.getProperty("test.db.username"));
    	sqlTask1.setPassword(props.getProperty("test.db.password"));
    	sqlTask1.setSrc(new File("C:/Ganesh/edu/Project/sources/roller-svn/apps/weblogger/build/webapp/WEB-INF/classes/dbscripts/droptables.sql"));
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
    	sqlTask2.setSrc(new File("C:/Ganesh/edu/Project/sources/roller-svn/apps/weblogger/build/webapp/WEB-INF/classes/dbscripts/derby/createdb.sql"));
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
    public void testCreateMediaFileDirectory() throws Exception {
        User testUser = null;
        Weblog testWeblog = null;
        
    	// TODO: Setup code, to be moved to setUp method.
    	System.out.println("Before setting up weblogger");
        // setup weblogger
        TestUtils.setupWeblogger();
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
        
        mfMgr.removeMediaFile(mediaFile1);
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
