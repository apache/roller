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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.apache.roller.weblogger.ui.rendering.util.WeblogRequest.MAX_PAGE_NUM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Page numbers past MAX_PAGE_NUM are read as MAX_PAGE_NUM, so a pager on the
 * last allowed page must not offer a Next link that would only lead back to
 * the same page.
 */
public class PagerNextLinkLimitTest {

    @BeforeAll
    public static void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    private static AbstractPager<Object> itemsPager(int page) {
        return new AbstractPager<>(mock(URLStrategy.class), "http://localhost/blog", page) {
            @Override
            public List<Object> getItems() {
                return Collections.emptyList();
            }

            @Override
            public boolean hasMoreItems() {
                return true;
            }
        };
    }

    private static AbstractWeblogEntriesPager entriesPager(int page) {
        URLStrategy urls = mock(URLStrategy.class);
        when(urls.getWeblogCollectionURL(any(), any(), any(), any(), any(), anyInt(), anyBoolean()))
                .thenAnswer(call -> "http://localhost/blog?page=" + call.getArgument(5));
        return new AbstractWeblogEntriesPager(urls, new Weblog(), "en",
                null, null, null, null, null, page) {
            @Override
            public Map<Date, ? extends Collection<WeblogEntryWrapper>> getEntries() {
                return Collections.emptyMap();
            }

            @Override
            public boolean hasMoreEntries() {
                return true;
            }
        };
    }

    private static SearchResultsPager searchPager(int page) {
        URLStrategy urls = mock(URLStrategy.class);
        when(urls.getWeblogSearchURL(any(), any(), anyString(), any(), anyInt(), anyBoolean()))
                .thenAnswer(call -> "http://localhost/blog/search?page=" + call.getArgument(4));
        WeblogSearchRequest request = new WeblogSearchRequest();
        request.setLocale("en");
        request.setQuery("roller");
        request.setPageNum(page);
        return new SearchResultsPager(urls, request, Collections.emptyMap(), true);
    }

    @Test
    public void itemsPagerStopsAtTheLimit() {
        assertEquals("http://localhost/blog?page=" + MAX_PAGE_NUM,
                itemsPager(MAX_PAGE_NUM - 1).getNextLink());
        assertNotNull(itemsPager(MAX_PAGE_NUM - 1).getNextName());
        assertNull(itemsPager(MAX_PAGE_NUM).getNextLink());
        assertNull(itemsPager(MAX_PAGE_NUM).getNextName());
    }

    @Test
    public void entriesPagerStopsAtTheLimit() {
        assertEquals("http://localhost/blog?page=" + MAX_PAGE_NUM,
                entriesPager(MAX_PAGE_NUM - 1).getNextLink());
        assertNotNull(entriesPager(MAX_PAGE_NUM - 1).getNextName());
        assertNull(entriesPager(MAX_PAGE_NUM).getNextLink());
        assertNull(entriesPager(MAX_PAGE_NUM).getNextName());
    }

    @Test
    public void searchPagerStopsAtTheLimit() {
        assertEquals("http://localhost/blog/search?page=" + MAX_PAGE_NUM,
                searchPager(MAX_PAGE_NUM - 1).getNextLink());
        assertNull(searchPager(MAX_PAGE_NUM).getNextLink());
        assertNull(searchPager(MAX_PAGE_NUM).getNextName());
    }
}
