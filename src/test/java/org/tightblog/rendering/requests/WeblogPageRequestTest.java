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
package org.tightblog.rendering.requests;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.tightblog.TestUtils;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.generators.CalendarGenerator;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.service.ThemeManager;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WeblogPageRequestTest {

    private HttpServletRequest mockRequest;
    private PageModel pageModel;
    private UserManager mockUM;
    private WeblogManager mockWM;
    private WeblogEntryManager mockWEM;
    private ThemeManager mockTM;
    private WeblogEntryListGenerator mockWELG;
    private CalendarGenerator mockCG;

    @Before
    public void initializeMocks() {
        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();

        mockUM = mock(UserManager.class);
        mockWM = mock(WeblogManager.class);
        mockWEM = mock(WeblogEntryManager.class);
        mockTM = mock(ThemeManager.class);
        mockWELG = mock(WeblogEntryListGenerator.class);
        mockCG = mock(CalendarGenerator.class);
        pageModel = new PageModel(mockUM, mockWM, mockWEM, mockTM, mockWELG, mockCG, 20);
    }

    @Test
    public void testEntryPermalinkPage() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("entry/entry-anchor", wpr.getExtraPathInfo());
        assertEquals(TestUtils.BLOG_HANDLE, wpr.getWeblogHandle());
        assertEquals("entry", wpr.getContext());
        assertEquals("entry-anchor", wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertTrue(wpr.isPermalink());
        // pageNum = 0 so index
        assertFalse(wpr.isNoIndex());
        assertFalse(wpr.isSearchResults());
    }

    @Test
    public void testCategoryPageNoTag() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/category/stamps");
        when(mockRequest.getParameter("page")).thenReturn("1");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("category/stamps", wpr.getExtraPathInfo());
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("category", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertEquals("stamps", wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertFalse(wpr.isPermalink());
        // pageNum > 0 so no index
        assertTrue(wpr.isNoIndex());
    }

    @Test
    public void testCategoryPageWithTag() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/category/stamps/tag/semipostals");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("category/stamps/tag/semipostals", wpr.getExtraPathInfo());
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("category", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertEquals("stamps", wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertEquals("semipostals", wpr.getTag());
    }

    @Test
    public void testExtraPathsIgnored() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/category/stamps/coins");
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals(wpr.getWeblogHandle(), "myblog");
        assertEquals(wpr.getCategory(), "stamps");
    }

    @Test
    public void testTagPage() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/tag/commemoratives");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("tag", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertEquals("commemoratives", wpr.getTag());
    }

    @Test
    public void testMonthDatePage() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/date/201804");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("date", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals("201804", wpr.getWeblogDate());
        assertNull(wpr.getTag());
    }

    @Test
    public void testDayDatePage() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/date/20180402");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("date", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals("20180402", wpr.getWeblogDate());
        assertNull(wpr.getTag());
    }

    @Test
    public void testInvalidDatesIgnored() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/date/20181946");
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertNull(wpr.getWeblogDate());
    }

    @Test
    public void testCustomPage() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/page/myblog/page/first-day-covers");
        when(mockRequest.getParameter("date")).thenReturn("20181002");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("page", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertEquals("first-day-covers", wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals("20181002", wpr.getWeblogDate());
        assertNull(wpr.getTag());
    }

    @Test
    public void testIsValidDateString() {
        assertTrue(WeblogPageRequest.isValidDateString("20160229"));
        assertFalse(WeblogPageRequest.isValidDateString("20170229"));
        assertTrue(WeblogPageRequest.isValidDateString("201805"));
        assertFalse(WeblogPageRequest.isValidDateString("201815"));
        assertFalse(WeblogPageRequest.isValidDateString("20180547"));
        assertFalse(WeblogPageRequest.isValidDateString("2018"));
        assertFalse(WeblogPageRequest.isValidDateString("201805011"));
        assertFalse(WeblogPageRequest.isValidDateString("pumpkin"));
    }

    @Test
    public void testGetCommentForm() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);

        // no comment form provided at request time
        WeblogEntryComment comment = wpr.getCommentForm();
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
        wpr.setCommentForm(comment2);

        assertEquals(comment2, wpr.getCommentForm());
    }

    @Test
    public void testGetAnalyticsTrackingCode() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        Weblog weblog = new Weblog();
        wpr.setWeblog(weblog);

        when(pageModel.getAnalyticsTrackingCode(wpr.getWeblog(), false)).thenReturn("tracking code");
        assertEquals("tracking code", wpr.getAnalyticsTrackingCode());

        // return empty string if preview
        when(pageModel.getAnalyticsTrackingCode(wpr.getWeblog(), true)).thenReturn("");
        wpr = WeblogPageRequest.Creator.createPreview(mockRequest, pageModel);
        assertEquals("", wpr.getAnalyticsTrackingCode());
    }

    @Test
    public void testGetTemplateIdByName() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        Weblog weblog = new Weblog();
        wpr.setWeblog(weblog);

        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockTM.getWeblogTheme(wpr.getWeblog())).thenReturn(mockTheme);
        when(mockTheme.getTemplateByName(any())).thenReturn(null);
        assertNull(wpr.getTemplateIdByName("abc"));

        SharedTemplate abc = new SharedTemplate();
        abc.setId("abcId");
        abc.setName("abcTemplate");
        when(mockTheme.getTemplateByName("abcTemplate")).thenReturn(abc);
        assertEquals("abcId", wpr.getTemplateIdByName("abcTemplate"));
    }

    @Test
    public void testGetRecentWeblogEntries() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);

        List<WeblogEntry> weblogEntryList = wpr.getRecentWeblogEntries(null, -5);
        assertEquals(0, weblogEntryList.size());

        // testWeblogEntrySearchCriteria object correctly populated
        wpr.getRecentWeblogEntries("stamps", 50);
        ArgumentCaptor<WeblogEntrySearchCriteria> captor = ArgumentCaptor.forClass(WeblogEntrySearchCriteria.class);
        verify(mockWEM).getWeblogEntries(captor.capture());
        WeblogEntrySearchCriteria wesc = captor.getValue();
        assertEquals(wpr.getWeblog(), wesc.getWeblog());
        assertEquals("stamps", wesc.getCategoryName());
        assertEquals(WeblogEntry.PubStatus.PUBLISHED, wesc.getStatus());
        assertEquals(50, wesc.getMaxResults());
        assertTrue(wesc.isCalculatePermalinks());

        // test limit of MAX_ENTRIES
        Mockito.clearInvocations(mockWEM);
        wpr.getRecentWeblogEntries(null, WeblogPageRequest.MAX_ENTRIES + 20);
        verify(mockWEM).getWeblogEntries(captor.capture());
        wesc = captor.getValue();
        assertEquals(WeblogPageRequest.MAX_ENTRIES, wesc.getMaxResults());
    }

    @Test
    public void testGetRecentComments() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);

        // test length < 1 returns empty set
        List<WeblogEntryComment> commentList = wpr.getRecentComments(-5);
        assertEquals(0, commentList.size());

        // test CommentSearchCriteria object correctly populated
        wpr.getRecentComments(50);
        ArgumentCaptor<CommentSearchCriteria> cscCaptor = ArgumentCaptor.forClass(CommentSearchCriteria.class);
        verify(mockWEM).getComments(cscCaptor.capture());
        CommentSearchCriteria csc = cscCaptor.getValue();
        assertEquals(wpr.getWeblog(), csc.getWeblog());
        assertEquals(WeblogEntryComment.ApprovalStatus.APPROVED, csc.getStatus());
        assertEquals(50, csc.getMaxResults());

        // test comment limit of MAX_ENTRIES
        Mockito.clearInvocations(mockWEM);
        wpr.getRecentComments(WeblogPageRequest.MAX_ENTRIES + 20);
        verify(mockWEM).getComments(cscCaptor.capture());
        csc = cscCaptor.getValue();
        assertEquals(WeblogPageRequest.MAX_ENTRIES, csc.getMaxResults());
    }

    @Test
    public void testPermalinkPagerReturned() {
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);

        wpr.getWeblogEntriesPager();
        verify(mockWELG).getPermalinkPager(any(), any(), any());
        verify(mockWELG, never()).getChronoPager(any(), any(), any(), any(), eq(0), eq(-1), eq(false));
    }

    @Test
    public void testTimePagerReturned() {
        mockRequest = TestUtils.createMockServletRequestForWeblogHomePageRequest();
        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        Weblog weblog = new Weblog();
        weblog.setEntriesPerPage(12);
        wpr.setWeblog(weblog);

        wpr.getWeblogEntriesPager();
        verify(mockWELG).getChronoPager(any(), any(), any(), any(), eq(0), eq(12), eq(false));
        verify(mockWELG, never()).getPermalinkPager(any(), any(), any());
    }

    @Test
    public void testCheckUserRights() {
        Principal mockPrincipal = mock(Principal.class);
        when(mockRequest.getUserPrincipal()).thenReturn(mockPrincipal);

        WeblogPageRequest wpr = WeblogPageRequest.Creator.createPreview(mockRequest, pageModel);

        // if preview, always false
        assertFalse(wpr.isUserBlogOwner());
        assertFalse(wpr.isUserBlogPublisher());

        // authenticated user is null, so both should be false
        wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        assertFalse(wpr.isUserBlogOwner());
        assertFalse(wpr.isUserBlogPublisher());

        // authenticated user has neither role
        when(mockPrincipal.getName()).thenReturn("bob");
        wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        Weblog weblog = new Weblog();
        wpr.setWeblog(weblog);
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.POST)).thenReturn(false);
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.OWNER)).thenReturn(false);
        assertFalse(wpr.isUserBlogOwner());
        assertFalse(wpr.isUserBlogPublisher());

        // authenticated user has lower role
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.POST)).thenReturn(true);
        wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        wpr.setWeblog(weblog);
        assertFalse(wpr.isUserBlogOwner());
        assertTrue(wpr.isUserBlogPublisher());

        // authenticated user has both roles
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.OWNER)).thenReturn(true);
        wpr = WeblogPageRequest.Creator.create(mockRequest, pageModel);
        wpr.setWeblog(weblog);
        assertTrue(wpr.isUserBlogOwner());
        assertTrue(wpr.isUserBlogPublisher());
    }
}
