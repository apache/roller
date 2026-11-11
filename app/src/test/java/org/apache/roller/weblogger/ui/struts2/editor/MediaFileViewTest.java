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


import java.util.Collections;

import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The media library opens a weblog's default folder. Weblogs created by old
 * Roller versions may not have one.
 */
class MediaFileViewTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<WebloggerRuntimeConfig> runtimeConfig;
    private Weblogger weblogger;
    private MediaFileManager media;
    private Weblog weblog;
    private MediaFileView action;

    @BeforeEach
    void setUp() throws Exception {
        media = mock(MediaFileManager.class);
        weblogger = mock(Weblogger.class);
        when(weblogger.getMediaFileManager()).thenReturn(media);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        runtimeConfig = mockStatic(WebloggerRuntimeConfig.class);
        runtimeConfig.when(WebloggerRuntimeConfig::getRelativeContextURL).thenReturn("/roller");

        when(media.getMediaFileCount(any(), any())).thenReturn(0L);
        when(media.searchMediaFiles(any(), any())).thenReturn(Collections.emptyList());

        weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("oldblog");

        action = spy(new MediaFileView());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        action.setActionWeblog(weblog);
    }

    @AfterEach
    void tearDown() {
        runtimeConfig.close();
        factory.close();
    }

    @Test
    void createsMissingDefaultFolder() throws Exception {
        MediaFileDirectory created = new MediaFileDirectory(weblog, "default", "default directory");
        when(media.getDefaultMediaFileDirectory(weblog)).thenReturn(null);
        when(media.createDefaultMediaFileDirectory(weblog)).thenReturn(created);

        action.execute();

        assertFalse(action.hasActionErrors());
        assertSame(created, action.getCurrentDirectory());
        verify(weblogger).flush();
    }

    @Test
    void usesExistingDefaultFolder() throws Exception {
        MediaFileDirectory existing = new MediaFileDirectory(weblog, "default", "default directory");
        when(media.getDefaultMediaFileDirectory(weblog)).thenReturn(existing);

        action.execute();

        assertFalse(action.hasActionErrors());
        assertSame(existing, action.getCurrentDirectory());
        verify(media, never()).createDefaultMediaFileDirectory(any());
    }
}
