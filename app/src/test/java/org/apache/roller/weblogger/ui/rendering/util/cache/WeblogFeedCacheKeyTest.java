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
}
