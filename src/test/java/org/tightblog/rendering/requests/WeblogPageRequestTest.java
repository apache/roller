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
import org.tightblog.rendering.service.CalendarGenerator;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
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
    private WeblogEntryManager mockWEM;
    private ThemeManager mockTM;
    private WeblogEntryListGenerator mockWELG;

    @Before
    public void initializeMocks() {
        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();

        mockUM = mock(UserManager.class);
        WeblogManager mockWM = mock(WeblogManager.class);
        mockWEM = mock(WeblogEntryManager.class);
        mockTM = mock(ThemeManager.class);
        mockWELG = mock(WeblogEntryListGenerator.class);
        CalendarGenerator mockCG = mock(CalendarGenerator.class);
        pageModel = new PageModel(mockUM, mockWM, mockWEM, mockTM, mockWELG, mockCG, 20);
    }

    @Test
    public void testGetCommentForm() {
        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel);

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
        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel);
        Weblog weblog = new Weblog();
        wpr.setWeblog(weblog);

        when(pageModel.getAnalyticsTrackingCode(wpr.getWeblog(), false)).thenReturn("tracking code");
        assertEquals("tracking code", wpr.getAnalyticsTrackingCode());

        // return empty string if preview
        when(pageModel.getAnalyticsTrackingCode(wpr.getWeblog(), true)).thenReturn("");
        wpr = new WeblogPageRequest("myblog", null, pageModel, true);
        assertEquals("", wpr.getAnalyticsTrackingCode());
    }

    @Test
    public void testGetTemplateIdByName() {
        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel);
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
        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel);

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
        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel);

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
    public void testTimePagerReturned() {
        mockRequest = TestUtils.createMockServletRequestForWeblogHomePageRequest();
        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel);
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

        WeblogPageRequest wpr = new WeblogPageRequest("myblog", null, pageModel, true);

        // if preview, always false
        assertFalse(wpr.isUserBlogOwner());
        assertFalse(wpr.isUserBlogPublisher());

        // authenticated user is null, so both should be false
        wpr = new WeblogPageRequest("myblog", null, pageModel);
        assertFalse(wpr.isUserBlogOwner());
        assertFalse(wpr.isUserBlogPublisher());

        // authenticated user has neither role
        when(mockPrincipal.getName()).thenReturn("bob");
        wpr = new WeblogPageRequest("myblog", mockPrincipal, pageModel);
        Weblog weblog = new Weblog();
        wpr.setWeblog(weblog);
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.POST)).thenReturn(false);
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.OWNER)).thenReturn(false);
        assertFalse(wpr.isUserBlogOwner());
        assertFalse(wpr.isUserBlogPublisher());

        // authenticated user has lower role
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.POST)).thenReturn(true);
        wpr = new WeblogPageRequest("myblog", mockPrincipal, pageModel);
        wpr.setWeblog(weblog);
        assertFalse(wpr.isUserBlogOwner());
        assertTrue(wpr.isUserBlogPublisher());

        // authenticated user has owner role
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.OWNER)).thenReturn(true);
        wpr = new WeblogPageRequest("myblog", mockPrincipal, pageModel);
        wpr.setWeblog(weblog);
        assertTrue(wpr.isUserBlogOwner());
        assertTrue(wpr.isUserBlogPublisher());
    }
}
