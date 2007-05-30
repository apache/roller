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

import java.io.InputStream;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Test File Management business layer operations.
 */
public class FileManagerTest extends TestCase {
    
    private static Log log = LogFactory.getLog(FileManagerTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    public FileManagerTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(FileManagerTest.class);
    }
    
    
    public void setUp() throws Exception {
        
        try {
            testUser = TestUtils.setupUser("FileManagerTest_userName");
            testWeblog = TestUtils.setupWeblog("FileManagerTest_handle", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
    
    
    /**
     * Test simple file save/delete.
     */
    public void testFileCRUD() throws Exception {
        
        // update roller properties to prepare for test
        PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
        Map config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.enabled")).setValue("true");
        ((RuntimeConfigProperty)config.get("uploads.types.allowed")).setValue("opml");
        ((RuntimeConfigProperty)config.get("uploads.dir.maxsize")).setValue("1.00");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        /* NOTE: upload dir for unit tests is set in
               roller/testdata/roller-custom.properties */
        FileManager fmgr = RollerFactory.getRoller().getFileManager();
        
        // we should be starting with 0 files
        assertEquals(0, fmgr.getFiles(testWeblog, null).length);
        
        // store a file
        InputStream is = getClass().getResourceAsStream("/bookmarks.opml");
        fmgr.saveFile(testWeblog, "bookmarks.opml", "text/plain", 1545, is);
        
        // make sure file was stored successfully
        assertEquals("bookmarks.opml", fmgr.getFile(testWeblog, "bookmarks.opml").getName());
        assertEquals(1, fmgr.getFiles(testWeblog, null).length);
        
        // delete file
        fmgr.deleteFile(testWeblog, "bookmarks.opml");
        
        // make sure delete was successful
        assertEquals(0, fmgr.getFiles(testWeblog, null).length);
    }
    
    
    /**
     * Test simple directory create/delete.
     */
    public void testDirectoryCRUD() throws Exception {
        
        // update roller properties to prepare for test
        PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
        Map config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.enabled")).setValue("true");
        ((RuntimeConfigProperty)config.get("uploads.types.allowed")).setValue("opml");
        ((RuntimeConfigProperty)config.get("uploads.dir.maxsize")).setValue("1.00");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        FileManager fmgr = RollerFactory.getRoller().getFileManager();
        
        // we should be starting with 0 directories
        assertEquals(0, fmgr.getDirectories(testWeblog).length);
        
        // create a directory
        fmgr.createDirectory(testWeblog, "bucket0");
        
        // make sure directory was created
        ThemeResource[] dirs = fmgr.getDirectories(testWeblog);
        assertNotNull(dirs);
        assertEquals(1, dirs.length);
        assertEquals("bucket0", dirs[0].getName());
        
        // cleanup
        fmgr.deleteFile(testWeblog, "bucket0");
        
        // make sure delete was successful
        assertEquals(0, fmgr.getDirectories(testWeblog).length);
    }
    
    
    /**
     * Test save/delete of files in a directory.
     */
    public void testFilesInDirectoriesCRUD() throws Exception {
        
        // update roller properties to prepare for test
        PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
        Map config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.enabled")).setValue("true");
        ((RuntimeConfigProperty)config.get("uploads.types.allowed")).setValue("opml");
        ((RuntimeConfigProperty)config.get("uploads.dir.maxsize")).setValue("1.00");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        FileManager fmgr = RollerFactory.getRoller().getFileManager();
        
        // we should be starting with 0 files and 0 directories
        assertEquals(0, fmgr.getFiles(testWeblog, null).length);
        assertEquals(0, fmgr.getDirectories(testWeblog).length);
        
        // create a directory
        fmgr.createDirectory(testWeblog, "bucket1");
        
        // make sure directory was created
        ThemeResource[] dirs = fmgr.getDirectories(testWeblog);
        assertNotNull(dirs);
        assertEquals(1, dirs.length);
        assertEquals("bucket1", dirs[0].getName());
        
        // store a file into a subdirectory
        InputStream is = getClass().getResourceAsStream("/bookmarks.opml");
        fmgr.saveFile(testWeblog, "bucket1/bookmarks.opml", "text/plain", 1545, is);
        
        // make sure file was stored successfully
        assertEquals("bucket1/bookmarks.opml", 
                fmgr.getFile(testWeblog, "bucket1/bookmarks.opml").getPath());
        assertEquals(1, fmgr.getFiles(testWeblog, "bucket1").length);
        
        // cleanup
        fmgr.deleteFile(testWeblog, "bucket1/bookmarks.opml");
        fmgr.deleteFile(testWeblog, "bucket1");
        
        // we should be back to 0 files and 0 directories
        assertEquals(0, fmgr.getFiles(testWeblog, null).length);
        assertEquals(0, fmgr.getDirectories(testWeblog).length);
    }
    
    
    /**
     * Test FileManager.saveFile() checks.
     *
     * This should test all conditions where a save should fail.
     */
    public void testCanSave() throws Exception {
        
        FileManager fmgr = RollerFactory.getRoller().getFileManager();
        PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
        Map config = config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.dir.maxsize")).setValue("1.00");
        ((RuntimeConfigProperty)config.get("uploads.types.forbid")).setValue("");
        ((RuntimeConfigProperty)config.get("uploads.types.allowed")).setValue("");
        ((RuntimeConfigProperty)config.get("uploads.enabled")).setValue("true");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        Exception exception = null;
        InputStream is = null;
        
        try {
            // path check should fail
            fmgr.saveFile(testWeblog, "some/path/foo.gif", "text/plain", 10, is);
        } catch (Exception ex) {
            log.error(ex);
            exception = ex;
        }
        assertNotNull(exception);
        exception = null;
        
        config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.dir.maxsize")).setValue("1.00");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        try {
            // quota check should fail
            fmgr.saveFile(testWeblog, "test.gif", "text/plain", 2500000, is);
        } catch (Exception ex) {
            log.error(ex);
            exception = ex;
        }
        assertNotNull(exception);
        exception = null;
        
        
        config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.types.forbid")).setValue("gif");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        try {
            // forbidden types check should fail
            fmgr.saveFile(testWeblog, "test.gif", "text/plain", 10, is);
        } catch (Exception ex) {
            log.error(ex);
            exception = ex;
        }
        assertNotNull(exception);
        exception = null;
        
        
        config = pmgr.getProperties();
        ((RuntimeConfigProperty)config.get("uploads.enabled")).setValue("false");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        try {
            // uploads disabled should fail
            fmgr.saveFile(testWeblog, "test.gif", "text/plain", 10, is);
        } catch (Exception ex) {
            log.error(ex);
            exception = ex;
        }
        assertNotNull(exception);
        exception = null;
    }
    
}
