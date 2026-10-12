/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FolderEditSaveTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<CacheManager> cache;
    private BookmarkManager bookmarks;
    private Weblog weblog;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        factory = mockStatic(WebloggerFactory.class);
        cache = mockStatic(CacheManager.class);
        bookmarks = mock(BookmarkManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getBookmarkManager()).thenReturn(bookmarks);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        weblog = mock(Weblog.class);
        response = mock(HttpServletResponse.class);
    }

    @AfterEach
    void tearDown() {
        cache.close();
        factory.close();
    }

    @Test
    void renamingAFolderSavesItAndSendsItsId() throws Exception {
        WeblogBookmarkFolder folder = new WeblogBookmarkFolder();
        folder.setId("folder-1");
        folder.setName("old");
        folder.setWeblog(weblog);
        when(bookmarks.getFolderById(weblog, "folder-1")).thenReturn(folder);

        FolderEdit action = action("folderEdit");
        action.getBean().setId("folder-1");
        action.getBean().setName("new");
        action.myPrepare();

        assertEquals(FolderEdit.SUCCESS, action.save());
        assertFalse(action.hasActionErrors());
        assertEquals("new", folder.getName());
        verify(bookmarks).saveFolder(folder);
        verify(response).addHeader("folderId", "folder-1");
    }

    @Test
    void addingAFolderSendsTheNewId() throws Exception {
        doAnswer(call -> {
            call.<WeblogBookmarkFolder>getArgument(0).setId("new-folder");
            return null;
        }).when(bookmarks).saveFolder(any());

        FolderEdit action = action("folderAdd");
        action.getBean().setName("added");
        action.myPrepare();

        assertEquals(FolderEdit.SUCCESS, action.save());
        verify(response).addHeader("folderId", "new-folder");
    }

    private FolderEdit action(String actionName) {
        FolderEdit action = spy(new FolderEdit());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        action.setActionName(actionName);
        action.setActionWeblog(weblog);
        action.withServletResponse(response);
        return action;
    }
}
