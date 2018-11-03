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
import org.tightblog.service.ThemeManager;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;
import org.tightblog.rendering.requests.WeblogPageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PageModelTest {

    private UserManager mockUserManager;
    private WeblogEntryManager mockWeblogEntryManager;
    private WeblogEntryListGenerator mockGenerator;
    private ThemeManager mockThemeManager;
    private PageModel pageModel;
    private WeblogPageRequest pageRequest;
    private Map<String, Object> initData;
    private WeblogManager mockWeblogManager;
    private Weblog weblog;

    @Before
    public void initialize() {
        mockGenerator = mock(WeblogEntryListGenerator.class);
        mockUserManager = mock(UserManager.class);
        mockWeblogManager = mock(WeblogManager.class);
        mockWeblogEntryManager = mock(WeblogEntryManager.class);
        mockThemeManager = mock(ThemeManager.class);
        pageModel = new PageModel();
        pageModel.setUserManager(mockUserManager);
        pageModel.setWeblogManager(mockWeblogManager);
        pageModel.setWeblogEntryManager(mockWeblogEntryManager);
        pageModel.setThemeManager(mockThemeManager);
        pageModel.setWeblogEntryListGenerator(mockGenerator);
        weblog = new Weblog();
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
    public void testGetAnalyticsTrackingCode() {
        when(mockWeblogManager.getAnalyticsTrackingCode(pageRequest.getWeblog())).thenReturn("tracking code");
        assertEquals("tracking code", pageModel.getAnalyticsTrackingCode());

        // return empty string if preview
        pageModel.setPreview(true);
        assertEquals("", pageModel.getAnalyticsTrackingCode());
    }

    @Test
    public void testGetTemplateIdByName() {
        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(pageRequest.getWeblog())).thenReturn(mockTheme);
        when(mockTheme.getTemplateByName(any())).thenReturn(null);
        assertNull(pageModel.getTemplateIdByName("abc"));

        SharedTemplate abc = new SharedTemplate();
        abc.setId("abcId");
        abc.setName("abcTemplate");
        when(mockTheme.getTemplateByName("abcTemplate")).thenReturn(abc);
        assertEquals("abcId", pageModel.getTemplateIdByName("abcTemplate"));
    }

    @Test
    public void testGetRecentWeblogEntries() {
        List<WeblogEntry> weblogEntryList = pageModel.getRecentWeblogEntries(null, -5);
        assertEquals(0, weblogEntryList.size());

        // testWeblogEntrySearchCriteria object correctly populated
        pageModel.getRecentWeblogEntries("stamps", 50);
        ArgumentCaptor<WeblogEntrySearchCriteria> captor = ArgumentCaptor.forClass(WeblogEntrySearchCriteria.class);
        verify(mockWeblogEntryManager).getWeblogEntries(captor.capture());
        WeblogEntrySearchCriteria wesc = captor.getValue();
        assertEquals(pageRequest.getWeblog(), wesc.getWeblog());
        assertEquals("stamps", wesc.getCategoryName());
        assertEquals(WeblogEntry.PubStatus.PUBLISHED, wesc.getStatus());
        assertEquals(50, wesc.getMaxResults());
        assertTrue(wesc.isCalculatePermalinks());

        // test limit of MAX_ENTRIES
        Mockito.clearInvocations(mockWeblogEntryManager);
        pageModel.getRecentWeblogEntries(null, PageModel.MAX_ENTRIES + 20);
        verify(mockWeblogEntryManager).getWeblogEntries(captor.capture());
        wesc = captor.getValue();
        assertEquals(PageModel.MAX_ENTRIES, wesc.getMaxResults());
    }

    @Test
    public void testGetRecentComments() {
        // test length < 1 returns empty set
        List<WeblogEntryComment> commentList = pageModel.getRecentComments(-5);
        assertEquals(0, commentList.size());

        // test CommentSearchCriteria object correctly populated
        pageModel.getRecentComments(50);
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
    public void testTimePagerReturned() {
        WeblogEntryListData pager = pageModel.getWeblogEntriesPager();
        verify(mockGenerator).getChronoPager(any(), any(), any(), any(), eq(0), eq(-1), eq(false));
        verify(mockGenerator, never()).getPermalinkPager(any(), any(), any());
    }

    @Test
    public void testPermalinkPagerReturned() {
        pageRequest.setWeblogEntryAnchor("blog-entry");
        WeblogEntryListData pager = pageModel.getWeblogEntriesPager();
        verify(mockGenerator).getPermalinkPager(any(), any(), any());
        verify(mockGenerator, never()).getChronoPager(any(), any(), any(), any(), eq(0), eq(-1), eq(false));
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
        when(mockUserManager.checkWeblogRole("bob", weblog, WeblogRole.POST)).thenReturn(false);
        when(mockUserManager.checkWeblogRole("bob", weblog, WeblogRole.OWNER)).thenReturn(false);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user has lower role
        when(mockUserManager.checkWeblogRole("bob", weblog, WeblogRole.POST)).thenReturn(true);
        assertFalse(pageModel.isUserBlogOwner());
        assertTrue(pageModel.isUserBlogPublisher());

        // authenticated user has both roles
        when(mockUserManager.checkWeblogRole("bob", weblog, WeblogRole.OWNER)).thenReturn(true);
        assertTrue(pageModel.isUserBlogOwner());
        assertTrue(pageModel.isUserBlogPublisher());
    }
}
