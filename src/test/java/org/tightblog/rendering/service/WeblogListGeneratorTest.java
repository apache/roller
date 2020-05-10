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
package org.tightblog.rendering.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;

import org.tightblog.rendering.service.WeblogListGenerator.WeblogListData;
import org.tightblog.dao.WeblogDao;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeblogListGeneratorTest {

    private WeblogListGenerator weblogListGenerator;
    private List<Weblog> weblogList;
    private Instant now = Instant.now();
    private Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);
    private WeblogDao mockWeblogDao;

    @Before
    public void initialize() {
        mockWeblogDao = mock(WeblogDao.class);
        weblogListGenerator = new WeblogListGenerator(mockWeblogDao);

        User user1 = new User();
        user1.setScreenName("user1");

        User user2 = new User();
        user2.setScreenName("user2");

        weblogList = new ArrayList<>();

        Weblog weblog1 = new Weblog();
        weblog1.setName("Most Popular Blog");
        weblog1.setHandle("mostpop1");
        weblog1.setAbout("About most popular 1");
        weblog1.setCreator(user1);
        weblog1.setLastModified(now);
        weblog1.setHitsToday(200);
        weblogList.add(weblog1);

        Weblog weblog2 = new Weblog();
        weblog2.setName("Second-Most Popular Blog");
        weblog2.setHandle("mostpop2");
        weblog2.setAbout("About most popular 2");
        weblog2.setCreator(user2);
        weblog2.setLastModified(twoDaysAgo);
        weblog2.setHitsToday(150);
        weblogList.add(weblog2);
    }

    @Test
    public void testGetHotWeblogs() {
        when(mockWeblogDao.findByVisibleTrueAndHitsTodayGreaterThanOrderByHitsTodayDesc(eq(0), any()))
                .thenReturn(weblogList);
        List<WeblogListGenerator.WeblogData> weblogDataList = weblogListGenerator.getHotWeblogs(12);
        assertEquals(2, weblogDataList.size());
        WeblogListGenerator.WeblogData firstBlog = weblogDataList.get(0);
        assertEquals("Most Popular Blog", firstBlog.getName());
        assertEquals("mostpop1", firstBlog.getHandle());
        assertEquals("About most popular 1", firstBlog.getAbout());
        assertEquals("user1", firstBlog.getCreatorScreenName());
        assertEquals(now, firstBlog.getLastModified());
        assertEquals(200, firstBlog.getHitsToday());
        assertEquals("Second-Most Popular Blog", weblogDataList.get(1).getName());
    }

    @Test
    public void testGetWeblogsByLetter() {
        List<Weblog> oneWeblogList = new ArrayList<>();
        oneWeblogList.add(weblogList.get(0));

        when(mockWeblogDao.findByLetterOrderByHandle('M', PageRequest.of(30, 11)))
                .thenReturn(oneWeblogList);
        when(mockWeblogDao.findByVisibleTrueOrderByHandle(any())).thenReturn(weblogList);

        // test if letter and pageNum > 0 get one weblog and prev link
        WeblogListData data = weblogListGenerator.getWeblogsByLetter("http://www.foo.com", 'M',
                3, 10);
        assertEquals(1, data.getWeblogs().size());
        assertEquals("mostpop1", data.getWeblogs().get(0).getHandle());
        assertEquals("http://www.foo.com?letter=M&page=2", data.getPrevLink());
        assertNull(data.getNextLink());

        // test if no letter get two weblogs with no next or previous
        data = weblogListGenerator.getWeblogsByLetter("http://www.foo.com", null,
                0, 10);
        assertEquals(2, data.getWeblogs().size());
        assertEquals("mostpop2", data.getWeblogs().get(1).getHandle());
        assertNull(data.getPrevLink());
        assertNull(data.getNextLink());

        // test if no letter & length 1 get just one back (first page - no prev link)
        data = weblogListGenerator.getWeblogsByLetter("http://www.foo.com", null,
                0, 1);
        assertEquals(1, data.getWeblogs().size());
        assertEquals("mostpop1", data.getWeblogs().get(0).getHandle());
        assertNull(data.getPrevLink());
        assertEquals("http://www.foo.com?page=1", data.getNextLink());

        // test if no letter & length 1 get just one back (non-first page)
        data = weblogListGenerator.getWeblogsByLetter("http://www.foo.com", null,
                2, 1);
        assertEquals(1, data.getWeblogs().size());
        assertEquals("mostpop1", data.getWeblogs().get(0).getHandle());
        assertEquals("http://www.foo.com?page=1", data.getPrevLink());
        assertEquals("http://www.foo.com?page=3", data.getNextLink());
    }
}
