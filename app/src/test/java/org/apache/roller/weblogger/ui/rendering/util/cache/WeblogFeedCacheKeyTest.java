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

package org.apache.roller.weblogger.ui.rendering.util.cache;

import java.util.List;
import java.util.function.Function;

import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Feed requests for different pages of entries render different content, so
 * they must get different keys in the weblog feed cache and in the site-wide
 * cache.
 */
public class WeblogFeedCacheKeyTest {

    private static WeblogFeedRequest request(int page) {
        WeblogFeedRequest request = new WeblogFeedRequest();
        request.setWeblogHandle("blog");
        request.setType("entries");
        request.setFormat("atom");
        request.setPage(page);
        return request;
    }

    private static WeblogFeedRequest search(String term, int page) {
        WeblogFeedRequest request = request(page);
        request.setTerm(term);
        return request;
    }

    private static WeblogFeedRequest tagged(int page, String... tags) {
        WeblogFeedRequest request = request(page);
        request.setTags(List.of(tags));
        return request;
    }

    private static final List<Function<WeblogFeedRequest, String>> CACHES = List.of(
            request -> WeblogFeedCache.getInstance().generateKey(request),
            request -> SiteWideCache.getInstance().generateKey(request));

    @Test
    public void pageNumberIsInTheKey() {
        for (Function<WeblogFeedRequest, String> key : CACHES) {
            assertNotEquals(key.apply(request(0)), key.apply(request(9)));
            assertNotEquals(key.apply(request(1)), key.apply(request(9)));
        }
    }

    @Test
    public void firstPageKeyIsUnchanged() {
        assertEquals("cache.weblogfeed:blog/entries/atom",
                WeblogFeedCache.getInstance().generateKey(request(0)));
        assertEquals("cache.sitewide:feed/blog/entries/atom",
                SiteWideCache.getInstance().generateKey(request(0)));
    }

    @Test
    public void searchTermsCannotLookLikeOtherKeyParts() {
        for (Function<WeblogFeedRequest, String> key : CACHES) {
            assertNotEquals(key.apply(search("foo/page=2", 0)),
                    key.apply(search("foo", 2)));
            // ?q=java%2Fpage%3D3 is decoded to the term "java/page=3"
            assertNotEquals(key.apply(search("java/page=3", 0)),
                    key.apply(search("java", 3)));
            assertNotEquals(key.apply(search("foo/excerpts", 0)),
                    excerpts(key, search("foo", 0)));
            WeblogFeedRequest tagged = search("foo", 0);
            tagged.setTags(List.of("x"));
            assertNotEquals(key.apply(search("foo/tags/x", 0)),
                    key.apply(tagged));
        }
    }

    @Test
    public void tagsCannotLookLikeOtherKeyParts() {
        for (Function<WeblogFeedRequest, String> key : CACHES) {
            assertNotEquals(key.apply(tagged(0, "java/page=2")),
                    key.apply(tagged(2, "java")));
            assertNotEquals(key.apply(tagged(0, "a+b")),
                    key.apply(tagged(0, "a", "b")));
        }
    }

    private static String excerpts(Function<WeblogFeedRequest, String> key,
            WeblogFeedRequest request) {
        request.setExcerpts(true);
        return key.apply(request);
    }
}
