/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.pagers;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LinkPagerTest {

    private static final String URL = "/roller-ui/authoring/entries.rol?weblog=roller";

    private static LinkPager<String> pager(int page, long total) {
        return new LinkPager<>(URL, page, Collections.emptyList(), total, 30);
    }

    @Test
    public void testEmptyResultIsOnePage() {
        LinkPager<String> p = pager(0, 0);
        assertEquals(1, p.getPageNumber());
        assertEquals(1, p.getPageCount());
        assertFalse(p.isMoreItems());
        assertNull(p.getFirstLink());
        assertNull(p.getPrevLink());
        assertNull(p.getNextLink());
        assertNull(p.getLastLink());
    }

    @Test
    public void testFirstPage() {
        LinkPager<String> p = pager(0, 65);
        assertEquals(1, p.getPageNumber());
        assertEquals(3, p.getPageCount());
        assertTrue(p.isMoreItems());
        assertNull(p.getFirstLink());
        assertNull(p.getPrevLink());
        assertEquals(URL + "&bean.page=1", p.getNextLink());
        assertEquals(URL + "&bean.page=2", p.getLastLink());
    }

    @Test
    public void testMiddlePage() {
        LinkPager<String> p = pager(1, 65);
        assertEquals(2, p.getPageNumber());
        assertEquals(URL + "&bean.page=0", p.getFirstLink());
        assertEquals(URL + "&bean.page=0", p.getPrevLink());
        assertEquals(URL + "&bean.page=2", p.getNextLink());
        assertEquals(URL + "&bean.page=2", p.getLastLink());
    }

    @Test
    public void testLastPage() {
        LinkPager<String> p = pager(2, 65);
        assertEquals(3, p.getPageNumber());
        assertFalse(p.isMoreItems());
        assertEquals(URL + "&bean.page=0", p.getFirstLink());
        assertEquals(URL + "&bean.page=1", p.getPrevLink());
        assertNull(p.getNextLink());
        assertNull(p.getLastLink());
    }

    @Test
    public void testExactMultipleOfPageSize() {
        // 60 rows is exactly two pages, not three
        assertEquals(2, pager(0, 60).getPageCount());
        assertFalse(pager(1, 60).isMoreItems());
        assertEquals(URL + "&bean.page=1", pager(0, 60).getLastLink());
    }

    @Test
    public void testBaseUrlWithoutQueryString() {
        LinkPager<String> p = new LinkPager<>("/entries.rol", 0, Collections.emptyList(), 31, 30);
        assertEquals("/entries.rol?bean.page=1", p.getNextLink());
    }

}
