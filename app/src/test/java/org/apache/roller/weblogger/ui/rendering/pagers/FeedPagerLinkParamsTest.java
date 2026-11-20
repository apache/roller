/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.model.FeedModel;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The feed pagers add the request's search term, category, tags and excerpts
 * flag to the page parameter of their next and previous links.
 */
public class FeedPagerLinkParamsTest {

    private static final String FEED_URL = "http://localhost/blog/feed/entries/atom";

    @BeforeAll
    public static void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    private static WeblogFeedRequest feedRequest() {
        WeblogFeedRequest request = new WeblogFeedRequest();
        request.setWeblog(new Weblog());
        request.setLocale("en");
        request.setType("entries");
        request.setFormat("atom");
        request.setWeblogCategoryName("Tech");
        request.setTags(List.of("java"));
        request.setExcerpts(true);
        request.setPage(1);
        return request;
    }

    private static FeedModel feedModel(WeblogFeedRequest request) throws Exception {
        URLStrategy urls = mock(URLStrategy.class);
        when(urls.getWeblogFeedURL(any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean())).thenReturn(FEED_URL);
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", request);
        initData.put("urlStrategy", urls);
        FeedModel model = new FeedModel();
        model.init(initData);
        return model;
    }

    private static void assertLink(String link, int page, String... params) {
        assertNotNull(link);
        assertTrue(link.startsWith(FEED_URL + "?"), link);
        String[] actual = link.substring(FEED_URL.length() + 1).split("&");
        assertEquals(params.length + 1, actual.length, link);
        List<String> actualParams = List.of(actual);
        assertTrue(actualParams.contains("page=" + page), link);
        for (String param : params) {
            assertTrue(actualParams.contains(param), link);
        }
    }

    @Test
    public void searchFeedPagerLinksKeepTheRequestParams() {
        WeblogFeedRequest request = feedRequest();
        request.setTerm("roller");
        SearchResultsFeedPager pager = new SearchResultsFeedPager(mock(URLStrategy.class),
                FEED_URL, 1, request, Collections.emptyList(), true);
        assertLink(pager.getPrevLink(), 0, "q=roller", "cat=Tech", "tags=java", "excerpts=true");
        assertLink(pager.getNextLink(), 2, "q=roller", "cat=Tech", "tags=java", "excerpts=true");
    }

    @Test
    public void entriesFeedPagerLinksKeepTheRequestParams() throws Exception {
        WeblogFeedRequest request = feedRequest();
        AbstractPager<?> pager = feedModel(request).new FeedEntriesPager(request) {
            @Override
            public boolean hasMoreItems() {
                return true;
            }
        };
        assertLink(pager.getPrevLink(), 0, "cat=Tech", "tags=java", "excerpts=true");
        assertLink(pager.getNextLink(), 2, "cat=Tech", "tags=java", "excerpts=true");
    }

    @Test
    public void commentsFeedPagerLinksKeepTheRequestParams() throws Exception {
        WeblogFeedRequest request = feedRequest();
        AbstractPager<?> pager = feedModel(request).new FeedCommentsPager(request) {
            @Override
            public boolean hasMoreItems() {
                return true;
            }
        };
        assertLink(pager.getPrevLink(), 0, "cat=Tech", "tags=java", "excerpts=true");
        assertLink(pager.getNextLink(), 2, "cat=Tech", "tags=java", "excerpts=true");
    }

    @Test
    public void filesFeedPagerLinksKeepTheRequestParams() throws Exception {
        WeblogFeedRequest request = feedRequest();
        AbstractPager<?> pager = feedModel(request).new FeedFilesPager(request) {
            @Override
            public boolean hasMoreItems() {
                return true;
            }
        };
        assertLink(pager.getPrevLink(), 0, "cat=Tech", "tags=java", "excerpts=true");
        assertLink(pager.getNextLink(), 2, "cat=Tech", "tags=java", "excerpts=true");
    }
}
