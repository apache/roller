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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.tightblog.WebloggerTest;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WebloggerProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        webloggerPropertiesRepository.saveAndFlush(props);
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    @Test
    public void testFileSaveAndDelete() throws Exception {
        WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
        props.setMaxFileUploadsSizeMb(1);
        webloggerPropertiesRepository.saveAndFlush(props);

        FileService fileService = new FileService(webloggerPropertiesRepository,
                true, storageDir, Set.of("image/jpeg"), 3);

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

    @Test
    public void testCanSave() {
        FileService fileService = new FileService(webloggerPropertiesRepository,
                true, storageDir, Set.of("image/*"), 1);

        MultipartFile mockMultipartFile = mock(MockMultipartFile.class);
        when(mockMultipartFile.getSize()).thenReturn(2500000L);
        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        when(mockMultipartFile.getName()).thenReturn("test.gif");
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.gif");

        boolean canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        // file too big
        assertFalse(canSave);

        // file right size
        when(mockMultipartFile.getSize()).thenReturn(500000L);
        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertTrue(canSave);

        // gifs no longer allowed
        fileService = new FileService(webloggerPropertiesRepository,
                true, storageDir, Set.of("image/png"), 1);

        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertFalse(canSave);

        // right-side wildcards work
        fileService = new FileService(webloggerPropertiesRepository,
                true, storageDir, Set.of("image/*"), 1);

        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertTrue(canSave);

        // uploads disabled should fail
        fileService = new FileService(webloggerPropertiesRepository,
                false, storageDir, Set.of("image/png"), 1);

        canSave = fileService.canSave(mockMultipartFile, testWeblog.getHandle(), null);
        assertFalse(canSave);
    }
}
