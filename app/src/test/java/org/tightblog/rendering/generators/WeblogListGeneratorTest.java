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
package org.tightblog.rendering.generators;

import org.junit.Test;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeblogListGeneratorTest {

    @Test
    public void testGetHotWeblogs() {
        WeblogManager mockWeblogManager = mock(WeblogManager.class);
        WeblogListGenerator generator = new WeblogListGenerator();
        generator.setWeblogManager(mockWeblogManager);

        User user1 = new User();
        user1.setScreenName("user1");

        User user2 = new User();
        user2.setScreenName("user2");

        Instant now = Instant.now();
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);

        List<Weblog> weblogList = new ArrayList<>();

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

        when(mockWeblogManager.getHotWeblogs(0, 12)).thenReturn(weblogList);
        List<WeblogListGenerator.WeblogData> weblogDataList = generator.getHotWeblogs(12);
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
}
