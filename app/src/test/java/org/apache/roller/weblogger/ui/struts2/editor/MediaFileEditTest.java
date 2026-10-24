/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The media file editor shows the original path only when
 * {@code mediafile.originalPathEdit.enabled} is true, and saves it on the same
 * terms.
 */
class MediaFileEditTest {

    private static final String EDIT_ENABLED = "mediafile.originalPathEdit.enabled";

    private MockedStatic<WebloggerFactory> factory;
    private MediaFileManager media;
    private Weblog weblog;
    private MediaFile mediaFile;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getMediaFileManager()).thenReturn(media);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

        weblog = mock(Weblog.class);
        MediaFileDirectory directory = mock(MediaFileDirectory.class);
        when(directory.getId()).thenReturn("dir-1");

        mediaFile = new MediaFile();
        mediaFile.setId("file-1");
        mediaFile.setName("site.css");
        mediaFile.setDirectory(directory);
        mediaFile.setOriginalPath("/css/site.css");
        when(media.getMediaFile(weblog, "file-1")).thenReturn(mediaFile);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void saveKeepsTheOriginalPathWhenEditingIsOff() throws Exception {
        MediaFileEdit action = action(false, "/page.html");

        assertEquals(MediaFileEdit.SUCCESS, action.save());
        assertEquals("/css/site.css", mediaFile.getOriginalPath());
        verify(media).updateMediaFile(weblog, mediaFile);
    }

    @Test
    void saveSetsTheOriginalPathWhenEditingIsOn() throws Exception {
        MediaFileEdit action = action(true, "/css/main.css");

        assertEquals(MediaFileEdit.SUCCESS, action.save());
        assertEquals("/css/main.css", mediaFile.getOriginalPath());
    }

    private MediaFileEdit action(boolean editEnabled, String postedPath) {
        MediaFileEdit action = spy(new MediaFileEdit());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doReturn(editEnabled).when(action).getBooleanProp(EDIT_ENABLED);
        action.setActionWeblog(weblog);
        action.setMediaFileId("file-1");
        action.setDirectory(mediaFile.getDirectory());

        MediaFileBean bean = action.getBean();
        bean.setName("site.css");
        bean.setTagsAsString("");
        bean.setDirectoryId("dir-1");
        bean.setOriginalPath(postedPath);
        return action;
    }
}
