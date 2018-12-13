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
package org.tightblog.service;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.tightblog.WebloggerTest;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WebloggerProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test File Management business layer operations.
 */
public class FileServiceIT extends WebloggerTest {

    private static Logger log = LoggerFactory.getLogger(FileServiceIT.class);

    private User testUser;
    private Weblog testWeblog;

    @Value("${mediafiles.storage.dir}")
    private String storageDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("FCMTestUserName1");
        testWeblog = setupWeblog("fcm-test-handle1", testUser);
    }

    @After
    public void tearDown() {
        WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
        props.setMaxFileUploadsSizeMb(30000);
        props.setUsersUploadMediaFiles(true);
        webloggerPropertiesRepository.saveAndFlush(props);
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    /**
     * Test simple file save/delete.
     */
    @Test
    public void testFileCRUD() throws Exception {
        WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
        props.setUsersUploadMediaFiles(true);
        props.setMaxFileUploadsSizeMb(1);
        webloggerPropertiesRepository.saveAndFlush(props);

        FileService fileService = new FileService(webloggerPropertiesRepository,
                storageDir,
                Set.of("opml"), Set.of(), 3);

        // File should not exist initially
        WebloggerTest.logExpectedException(log, "FileNotFoundException");
        File test = fileService.getFileContent(testWeblog, "bookmarks-file-id");
        assertNull("Non-existent file retrieved", test);

        // store a file
        InputStream is = getClass().getResourceAsStream("/hawk.jpg");
        fileService.saveFileContent(testWeblog, "bookmarks-file-id", is);

        // make sure file was stored successfully
        WebloggerTest.logExpectedException(log, "FileNotFoundException");
        File fileContent1 = fileService.getFileContent(testWeblog, "bookmarks-file-id");
        assertNotNull(fileContent1);

        // delete file
        fileService.deleteFile(testWeblog, "bookmarks-file-id");

        // File should not exist after delete
        test = fileService.getFileContent(testWeblog, "bookmarks-file-id");
        assertNull("Non-existent file retrieved", test);
    }

    /**
     * Test FileService.saveFile() checks.
     *
     * This should test all conditions where a save should fail.
     */
    @Test
    public void testCanSave() {
        WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
        props.setUsersUploadMediaFiles(true);
        webloggerPropertiesRepository.saveAndFlush(props);

        FileService fileService1 = new FileService(webloggerPropertiesRepository,
                storageDir,
                Set.of(), Set.of(), 1);

        // file too big
        boolean canSave = fileService1.canSave(testWeblog, "test.gif", "text/plain", 2500000, null);
        assertFalse(canSave);

        // file right size
        canSave = fileService1.canSave(testWeblog, "test.gif", "text/plain", 500000, null);
        assertTrue(canSave);

        FileService fileService2 = new FileService(webloggerPropertiesRepository,
                storageDir,
                Set.of(), Set.of("gif"), 1);

        webloggerPropertiesRepository.saveAndFlush(props);

        // forbidden types check should fail
        canSave = fileService2.canSave(testWeblog, "test.gif", "text/plain", 10, null);
        assertFalse(canSave);

        // ok types should pass
        canSave = fileService2.canSave(testWeblog, "test.png", "text/plain", 10, null);
        assertTrue(canSave);

        props.setUsersUploadMediaFiles(false);
        webloggerPropertiesRepository.saveAndFlush(props);

        // uploads disabled should fail
        canSave = fileService1.canSave(testWeblog, "test.gif", "text/plain", 10, null);
        assertFalse(canSave);
    }
}
