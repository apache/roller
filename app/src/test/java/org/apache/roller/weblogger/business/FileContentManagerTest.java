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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;
import org.junit.After;
import org.junit.Before;
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
        testWeblog = setupWeblog("FCMTest_handle1", testUser);
        endSession(true);
    }

    @After
    public void tearDown() throws Exception {
        Map<String, RuntimeConfigProperty> config = propertiesManager.getProperties();
        config.get("uploads.dir.maxsize").setValue("30000");
        config.get("uploads.types.forbid").setValue("");
        config.get("uploads.types.allowed").setValue("");
        config.get("uploads.enabled").setValue("true");
        propertiesManager.saveProperties(config);
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getUserName());
        endSession(true);
    }

    /**
     * Test simple file save/delete.
     */
    @Test
    public void testFileCRUD() throws Exception {
        // update roller properties to prepare for test
        Map<String, RuntimeConfigProperty> config = propertiesManager.getProperties();
        config.get("uploads.enabled").setValue("true");
        config.get("uploads.types.allowed").setValue("opml");
        config.get("uploads.dir.maxsize").setValue("1.00");
        propertiesManager.saveProperties(config);
        endSession(true);

        /* NOTE: upload dir for unit tests is set in tightblog-custom.properties */

        // File should not exist initially
        try {
            FileContent fileContent = fileContentManager.getFileContent(testWeblog, "bookmarks-file-id");
            assertTrue("Non-existent file retrieved without any exception", false);
        } catch (FileNotFoundException e) {
            assertTrue("Exception thrown for non-existent file as expected", true);
        }

        // store a file
        InputStream is = getClass().getResourceAsStream("/jetty.xml");
        fileContentManager.saveFileContent(testWeblog, "bookmarks-file-id", is);

        // make sure file was stored successfully
        FileContent fileContent1 = fileContentManager.getFileContent(testWeblog, "bookmarks-file-id");
        assertEquals("bookmarks-file-id", fileContent1.getFileId());


        // delete file
        fileContentManager.deleteFile(testWeblog, "bookmarks-file-id");

        // File should not exist after delete
        try {
            FileContent fileContent = fileContentManager.getFileContent(testWeblog, "bookmarks-file-id");
            assertTrue("Non-existant file retrieved without any exception", false);
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
    public void testCanSave() throws Exception {
        Map<String, RuntimeConfigProperty> config = propertiesManager.getProperties();
        config.get("uploads.dir.maxsize").setValue("1.00");
        config.get("uploads.types.forbid").setValue("");
        config.get("uploads.types.allowed").setValue("");
        config.get("uploads.enabled").setValue("true");
        propertiesManager.saveProperties(config);
        endSession(true);

        config = propertiesManager.getProperties();
        config.get("uploads.dir.maxsize").setValue("1.00");
        propertiesManager.saveProperties(config);
        endSession(true);

        RollerMessages msgs = new RollerMessages();
        boolean canSave = fileContentManager.canSave(testWeblog, "test.gif", "text/plain", 2500000, msgs);
        assertFalse(canSave);

        config = propertiesManager.getProperties();
        config.get("uploads.types.forbid").setValue("gif");
        propertiesManager.saveProperties(config);
        endSession(true);

        // forbidden types check should fail
        msgs = new RollerMessages();
        canSave = fileContentManager.canSave(testWeblog, "test.gif", "text/plain", 10, msgs);
        assertFalse(canSave);

        config = propertiesManager.getProperties();
        config.get("uploads.enabled").setValue("false");
        propertiesManager.saveProperties(config);
        endSession(true);

        // uploads disabled should fail
        msgs = new RollerMessages();
        canSave = fileContentManager.canSave(testWeblog, "test.gif", "text/plain", 10, msgs);
        assertFalse(canSave);
    }
}
