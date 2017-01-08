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
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Test File Management business layer operations.
 */
public class FileContentManagerTest extends WebloggerTest {

    User testUser = null;
    Weblog testWeblog = null;

    @Resource
    private FileContentManager fileContentManager;

    public void setFileContentManager(FileContentManager fileContentManager) {
        this.fileContentManager = fileContentManager;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("FCMTestUserName1");
        testWeblog = setupWeblog("FCMTest-handle1", testUser);
        endSession(true);
    }

    @After
    public void tearDown() throws Exception {
        WebloggerProperties props = strategy.getWebloggerProperties();
        props.setMaxFileUploadsSizeMb(30000);
        props.setDisallowedFileExtensions("");
        props.setAllowedFileExtensions("");
        props.setUsersUploadMediaFiles(true);
        strategy.store(props);
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
    }

    /**
     * Test simple file save/delete.
     */
    @Test
    public void testFileCRUD() throws Exception {
        WebloggerProperties props = strategy.getWebloggerProperties();
        props.setUsersUploadMediaFiles(true);
        props.setMaxFileUploadsSizeMb(1);
        props.setAllowedFileExtensions("opml");
        strategy.store(props);
        endSession(true);

        /* NOTE: upload dir for unit tests is set in tightblog-custom.properties */

        // File should not exist initially
        try {
            fileContentManager.getFileContent(testWeblog, "bookmarks-file-id");
            assertTrue("Non-existent file retrieved without any exception", false);
        } catch (FileNotFoundException e) {
            assertTrue("Exception thrown for non-existent file as expected", true);
        }

        // store a file
        InputStream is = getClass().getResourceAsStream("/jetty.xml");
        fileContentManager.saveFileContent(testWeblog, "bookmarks-file-id", is);

        // make sure file was stored successfully
        File fileContent1 = fileContentManager.getFileContent(testWeblog, "bookmarks-file-id");
        assertNotNull(fileContent1);

        // delete file
        fileContentManager.deleteFile(testWeblog, "bookmarks-file-id");

        // File should not exist after delete
        try {
            fileContentManager.getFileContent(testWeblog, "bookmarks-file-id");
            assertTrue("Non-existent file retrieved without any exception", false);
        } catch (FileNotFoundException e) {
            assertTrue("Exception thrown for non-existant file as expected", true);
        }
    }

    /**
     * Test FileContentManager.saveFile() checks.
     *
     * This should test all conditions where a save should fail.
     */
    @Test
    @Ignore
    public void testCanSave() throws Exception {
        WebloggerProperties props = strategy.getWebloggerProperties();
        props.setMaxFileSizeMb(1);
        props.setDisallowedFileExtensions("");
        props.setAllowedFileExtensions("");
        props.setUsersUploadMediaFiles(true);
        strategy.store(props);
        endSession(true);

        boolean canSave = fileContentManager.canSave(testWeblog, "test.gif", "text/plain", 2500000, null);
        assertFalse(canSave);

        props.setDisallowedFileExtensions("gif");
        strategy.store(props);
        endSession(true);

        // forbidden types check should fail
        canSave = fileContentManager.canSave(testWeblog, "test.gif", "text/plain", 10, null);
        assertFalse(canSave);

        props.setUsersUploadMediaFiles(false);
        strategy.store(props);
        endSession(true);

        // uploads disabled should fail
        canSave = fileContentManager.canSave(testWeblog, "test.gif", "text/plain", 10, null);
        assertFalse(canSave);
    }
}
