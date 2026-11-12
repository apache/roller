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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * The blogroll page opens a blogroll that has links, reorders and moves links
 * within the weblog's blogrolls, and keeps the default blogroll.
 */
class BookmarksTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<CacheManager> cache;
    private Weblogger weblogger;
    private BookmarkManager bookmarks;
    private Weblog weblog;
    private WeblogBookmarkFolder defaultFolder;
    private WeblogBookmarkFolder friends;
    private WeblogBookmarkFolder apache;
    private WeblogBookmark roller;
    private WeblogBookmark asf;
    private WeblogBookmark tomcat;

    @BeforeEach
    void setUp() throws Exception {
        bookmarks = mock(BookmarkManager.class);
        weblogger = mock(Weblogger.class);
        when(weblogger.getBookmarkManager()).thenReturn(bookmarks);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        cache = mockStatic(CacheManager.class);

        weblog = mock(Weblog.class);
        defaultFolder = folder("f-default", "default");
        friends = folder("f-friends", "Friends");
        apache = folder("f-apache", "Apache");
        roller = link(apache, "b-roller", "Roller", 0);
        asf = link(apache, "b-asf", "ASF", 1);
        tomcat = link(apache, "b-tomcat", "Tomcat", 2);
        // in the order the database returns them
        when(bookmarks.getAllFolders(weblog)).thenReturn(Arrays.asList(friends, apache, defaultFolder));
        when(bookmarks.getDefaultFolder(weblog)).thenReturn(defaultFolder);
    }

    @AfterEach
    void tearDown() {
        cache.close();
        factory.close();
    }

    private WeblogBookmarkFolder folder(String id, String name) throws Exception {
        WeblogBookmarkFolder folder = new WeblogBookmarkFolder();
        folder.setId(id);
        folder.setName(name);
        folder.setWeblog(weblog);
        when(bookmarks.getFolderById(weblog, id)).thenReturn(folder);
        return folder;
    }

    private WeblogBookmark link(WeblogBookmarkFolder folder, String id, String name, int priority) {
        WeblogBookmark link = new WeblogBookmark();
        link.setId(id);
        link.setName(name);
        link.setUrl("https://example.com/" + id);
        link.setPriority(priority);
        folder.addBookmark(link);
        return link;
    }

    private Bookmarks action(String folderId) {
        Bookmarks action = spy(new Bookmarks());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString(), anyString());
        action.setActionWeblog(weblog);
        action.setFolderId(folderId);
        action.myPrepare();
        return action;
    }

    @Test
    void listsTheDefaultBlogrollFirst() {
        Bookmarks action = action(null);

        assertEquals(List.of(defaultFolder, apache, friends), action.getAllFolders());
    }

    @Test
    void opensTheFirstBlogrollWithLinksWhenTheDefaultOneIsEmpty() {
        Bookmarks action = action(null);

        assertEquals(apache, action.getFolder());
        assertEquals("f-apache", action.getFolderId());
    }

    @Test
    void opensTheDefaultBlogrollWhenItHasLinks() {
        link(defaultFolder, "b-home", "Home", 0);

        assertEquals(defaultFolder, action(null).getFolder());
    }

    @Test
    void movesToAnyBlogrollButTheOneShown() {
        assertEquals(List.of(defaultFolder, friends), action("f-apache").getMoveTargets());
    }

    @Test
    void savesNewOrder() throws Exception {
        Bookmarks action = action("f-apache");
        action.setBookmarkIds(List.of("b-tomcat", "b-roller", "b-asf"));

        action.reorder();

        assertFalse(action.hasActionErrors());
        assertEquals(0, tomcat.getPriority());
        assertEquals(1, roller.getPriority());
        assertEquals(2, asf.getPriority());
        assertEquals(List.of(tomcat, roller, asf), apache.getBookmarks());
        verify(weblogger).flush();
    }

    @Test
    void rejectsAnOrderThatLeavesOutALink() throws Exception {
        Bookmarks action = action("f-apache");
        action.setBookmarkIds(List.of("b-tomcat", "b-roller"));

        action.reorder();

        assertTrue(action.hasActionErrors());
        assertEquals(2, tomcat.getPriority());
        verify(bookmarks, never()).saveBookmark(any());
    }

    @Test
    void movesLinksToTheEndOfAnotherBlogroll() throws Exception {
        WeblogBookmark existing = link(friends, "b-alice", "Alice", 4);
        doAnswer(call -> {
            WeblogBookmark saved = call.getArgument(0);
            if (saved.getPriority() == null) {
                saved.calculatePriority();
            }
            return null;
        }).when(bookmarks).saveBookmark(any());
        Bookmarks action = action("f-apache");
        action.setSelectedBookmarks(new String[] {"b-roller"});
        action.setTargetFolderId("f-friends");

        action.move();

        assertFalse(action.hasActionErrors());
        assertEquals(List.of(existing, roller), friends.getBookmarks());
        assertEquals(friends, roller.getFolder());
        assertEquals(5, roller.getPriority());
        assertEquals(List.of(asf, tomcat), apache.getBookmarks());
    }

    @Test
    void refusesToMoveALinkFromAnotherBlogroll() throws Exception {
        WeblogBookmark alice = link(friends, "b-alice", "Alice", 0);
        Bookmarks action = action("f-apache");
        action.setSelectedBookmarks(new String[] {"b-alice"});
        action.setTargetFolderId("f-default");

        action.move();

        assertTrue(action.hasActionErrors());
        assertEquals(friends, alice.getFolder());
        verify(bookmarks, never()).saveBookmark(any());
    }

    @Test
    void deletesOnlyLinksOfTheBlogrollShown() throws Exception {
        Bookmarks action = action("f-apache");
        action.setSelectedBookmarks(new String[] {"b-asf", "b-elsewhere"});

        action.delete();

        assertTrue(action.hasActionErrors());
        verify(bookmarks, never()).removeBookmark(any());

        action.clearErrorsAndMessages();
        action.setSelectedBookmarks(new String[] {"b-asf"});
        action.delete();

        assertFalse(action.hasActionErrors());
        verify(bookmarks).removeBookmark(asf);
        assertEquals(List.of(roller, tomcat), apache.getBookmarks());
    }

    @Test
    void keepsTheDefaultBlogroll() throws Exception {
        link(defaultFolder, "b-home", "Home", 0);
        Bookmarks action = action("f-default");

        action.deleteFolder();

        assertTrue(action.hasActionErrors());
        verify(bookmarks, never()).removeFolder(any());
    }

    @Test
    void deletesAnotherBlogrollAndShowsTheDefaultOne() throws Exception {
        Bookmarks action = action("f-friends");

        action.deleteFolder();

        assertFalse(action.hasActionErrors());
        verify(bookmarks).removeFolder(friends);
        assertEquals(defaultFolder, action.getFolder());
        assertFalse(action.getAllFolders().contains(friends));
    }
}
