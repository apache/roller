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
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;
import org.tightblog.rendering.model.FeedModel;
import org.tightblog.service.URLService;

import javax.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeblogFeedRequestTest {

    private HttpServletRequest mockRequest;
    private FeedModel mockFeedModel;
    private Weblog weblog;
    private URLService mockURLService;
    private WeblogFeedRequest.Creator creator;

    @Before
    public void initialize() {
        mockURLService = mock(URLService.class);
        mockFeedModel = mock(FeedModel.class);
        when(mockFeedModel.getURLService()).thenReturn(mockURLService);
        mockRequest = mock(HttpServletRequest.class);
        weblog = new Weblog();
        creator = new WeblogFeedRequest.Creator();
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/feed/myblog/feed");
    }

    @Test
    public void testParseFullFeed() {
        WeblogFeedRequest feedRequest = creator.create(mockRequest, mockFeedModel);
        feedRequest.setWeblog(weblog);
        assertEquals("myblog", feedRequest.getWeblogHandle());
        assertNull(feedRequest.getCategoryName());
        assertNull(feedRequest.getTag());

        when(mockURLService.getAtomFeedURL(weblog)).thenReturn("http://atomFeedURL");
        String test = feedRequest.getAtomFeedURL();
        assertEquals("http://atomFeedURL", test);
    }

    @Test
    public void testParseCategoryFeed() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/feed/myblog/feed/category/mycategory");
        when(mockRequest.getParameter("page")).thenReturn("6");
        WeblogFeedRequest feedRequest = creator.create(mockRequest, mockFeedModel);
        feedRequest.setWeblog(weblog);
        assertEquals("myblog", feedRequest.getWeblogHandle());
        assertEquals("mycategory", feedRequest.getCategoryName());
        assertNull(feedRequest.getTag());

        when(mockURLService.getAtomFeedURLForCategory(weblog, "mycategory"))
                .thenReturn("http://atomCategoryFeedURL");
        String test = feedRequest.getAtomFeedURL();
        assertEquals("http://atomCategoryFeedURL", test);

        WeblogEntryListData data = new WeblogEntryListData();
        when(mockFeedModel.getWeblogEntriesPager(weblog, "mycategory", null, 6, false))
                .thenReturn(data);
        assertEquals(data, feedRequest.getWeblogEntriesPager());
    }

    @Test
    public void testParseTagFeed() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/feed/myblog/feed/tag/mytag");
        WeblogFeedRequest feedRequest = creator.create(mockRequest, mockFeedModel);
        feedRequest.setWeblog(weblog);
        assertEquals("myblog", feedRequest.getWeblogHandle());
        assertEquals("mytag", feedRequest.getTag());
        assertNull(feedRequest.getCategoryName());

        when(mockURLService.getAtomFeedURLForTag(weblog, "mytag")).thenReturn("http://atomTagFeedURL");
        String test = feedRequest.getAtomFeedURL();
        assertEquals("http://atomTagFeedURL", test);

        WeblogEntryListData data = new WeblogEntryListData();
        when(mockFeedModel.getWeblogEntriesPager(weblog, null, "mytag", 0, false))
                .thenReturn(data);
        assertEquals(data, feedRequest.getWeblogEntriesPager());
    }

    @Test
    public void testInvalidPathArguments() {
        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/feed/myblog/invalid/mytag");
        WeblogFeedRequest feedRequest = creator.create(mockRequest, mockFeedModel);
        assertNull(feedRequest.getCategoryName());
        assertNull(feedRequest.getTag());

        when(mockRequest.getServletPath()).thenReturn("/tb-ui/rendering/feed/myblog/tag/mytag/invalid");
        feedRequest = creator.create(mockRequest, mockFeedModel);
        assertNull(feedRequest.getCategoryName());
        assertNull(feedRequest.getTag());
    }

    @Test
    public void testSiteWideCheck() {
        WeblogFeedRequest feedRequest = creator.create(mockRequest, mockFeedModel);
        feedRequest.setWeblog(weblog);

        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant threeDaysAgo = twoDaysAgo.minus(1, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);
        when(mockFeedModel.getLastSitewideChange()).thenReturn(threeDaysAgo);

        feedRequest.setSiteWide(true);
        String test = feedRequest.getLastUpdated();
        assertEquals("last sitewide change not captured for a sitewide feed",
                feedRequest.formatIsoOffsetDateTime(threeDaysAgo), test);

        feedRequest.setSiteWide(false);
        test = feedRequest.getLastUpdated();
        assertEquals("weblog last updated not used for a non-sitewide feed",
                feedRequest.formatIsoOffsetDateTime(twoDaysAgo), test);
    }

    @Test
    public void testFeedModelPassThroughMethods() {
        WeblogFeedRequest feedRequest = creator.create(mockRequest, mockFeedModel);
        feedRequest.setWeblog(weblog);

        when(mockURLService.getWeblogURL(weblog)).thenReturn("http://weblogURL");
        String test = feedRequest.getAlternateURL();
        assertEquals("http://weblogURL", test);

        WeblogEntry testEntry = new WeblogEntry();
        when(mockURLService.getWeblogEntryURL(testEntry)).thenReturn("http://entryURL");
        test = feedRequest.getWeblogEntryURL(testEntry);
        assertEquals(test, "http://entryURL");

        when(mockFeedModel.getSystemVersion()).thenReturn("1.1");
        test = feedRequest.getSystemVersion();
        assertEquals("1.1", test);

        when(mockFeedModel.render(Weblog.EditFormat.COMMONMARK, "mainText"))
                .thenReturn("transformedMainText");
        when(mockFeedModel.render(Weblog.EditFormat.HTML, "summary"))
                .thenReturn("transformedSummary");

        testEntry.setEditFormat(Weblog.EditFormat.COMMONMARK);
        testEntry.setText("mainText");
        test = feedRequest.getTransformedText(testEntry);
        assertEquals("transformedMainText", test);
        testEntry.setEditFormat(Weblog.EditFormat.HTML);
        testEntry.setSummary("summary");
        test = feedRequest.getTransformedSummary(testEntry);
        assertEquals("transformedSummary", test);
    }
}
