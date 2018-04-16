/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.rendering.pagers.WeblogEntriesPager;
import org.tightblog.rendering.pagers.WeblogEntriesPermalinkPager;
import org.tightblog.rendering.pagers.WeblogEntriesTimePager;
import org.tightblog.rendering.requests.WeblogPageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PageModelTest {

    private UserManager mockUserManager;
    private WeblogEntryManager mockWeblogEntryManager;
    private PageModel pageModel;
    private WeblogPageRequest pageRequest;
    private Map<String, Object> initData;

    @Before
    public void initialize() {
        mockUserManager = mock(UserManager.class);
        WeblogManager mockWeblogManager = mock(WeblogManager.class);
        mockWeblogEntryManager = mock(WeblogEntryManager.class);
        pageModel = new PageModel();
        pageModel.setUserManager(mockUserManager);
        pageModel.setWeblogManager(mockWeblogManager);
        pageModel.setWeblogEntryManager(mockWeblogEntryManager);
        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");
        weblog.setLocale("EN_US");
        pageRequest = new WeblogPageRequest();
        pageRequest.setWeblog(weblog);
        pageRequest.setWeblogHandle(weblog.getHandle());
        initData = new HashMap<>();
        initData.put("parsedRequest", pageRequest);
        pageModel.init(initData);
    }

    @Test
    public void testGetRecentComments() {
        // test length < 1 returns empty set
        List<WeblogEntryComment> commentList = pageModel.getRecentComments(-5);
        assertEquals(0, commentList.size());
        pageModel.getRecentComments(50);

        // test CommentSearchCriteria object correctly populated
        ArgumentCaptor<CommentSearchCriteria> cscCaptor = ArgumentCaptor.forClass(CommentSearchCriteria.class);
        verify(mockWeblogEntryManager).getComments(cscCaptor.capture());
        CommentSearchCriteria csc = cscCaptor.getValue();
        assertEquals(pageRequest.getWeblog(), csc.getWeblog());
        assertEquals(WeblogEntryComment.ApprovalStatus.APPROVED, csc.getStatus());
        assertEquals(50, csc.getMaxResults());

        // test comment limit of MAX_ENTRIES
        Mockito.clearInvocations(mockWeblogEntryManager);
        pageModel.getRecentComments(PageModel.MAX_ENTRIES + 20);
        verify(mockWeblogEntryManager).getComments(cscCaptor.capture());
        csc = cscCaptor.getValue();
        assertEquals(PageModel.MAX_ENTRIES, csc.getMaxResults());
    }

    @Test
    public void testCorrectPagerReturned() {
        WeblogEntriesPager pager = pageModel.getWeblogEntriesPager();
        assertTrue(pager instanceof WeblogEntriesTimePager);
        pageRequest.setWeblogEntryAnchor("blog-entry");
        pager = pageModel.getWeblogEntriesPager();
        assertTrue(pager instanceof WeblogEntriesPermalinkPager);
    }

    @Test
    public void testGetCommentForm() {
        // no comment form provided at request time
        WeblogEntryComment comment = pageModel.getCommentForm();
        assertNotNull(comment);
        assertEquals("", comment.getName());
        assertEquals("", comment.getEmail());
        assertEquals("", comment.getUrl());
        assertEquals("", comment.getContent());

        // comment form at request time
        WeblogEntryComment comment2 = new WeblogEntryComment();
        comment2.setName("Bob");
        comment2.setEmail("bob@email.com");
        comment2.setUrl("https://www.google.com");
        comment2.setContent("This is my comment.");
        initData.put("commentForm", comment2);
        pageModel.init(initData);

        comment = pageModel.getCommentForm();
        assertNotNull(comment);
        assertEquals("Bob", comment.getName());
        assertEquals("bob@email.com", comment.getEmail());
        assertEquals("https://www.google.com", comment.getUrl());
        assertEquals("This is my comment.", comment.getContent());
    }

    @Test
    public void testCheckUserRights() {
        // if preview, always false
        pageModel.setPreview(true);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user is null, so both should be false
        pageModel.setPreview(false);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user has neither role
        pageRequest.setAuthenticatedUser("bob");
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.POST)).thenReturn(false);
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.OWNER)).thenReturn(false);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user has lower role
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.POST)).thenReturn(true);
        assertFalse(pageModel.isUserBlogOwner());
        assertTrue(pageModel.isUserBlogPublisher());

        // authenticated user has both roles
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.OWNER)).thenReturn(true);
        assertTrue(pageModel.isUserBlogOwner());
        assertTrue(pageModel.isUserBlogPublisher());
    }
}
