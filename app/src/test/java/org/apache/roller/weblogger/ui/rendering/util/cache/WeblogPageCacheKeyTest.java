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

package org.apache.roller.weblogger.ui.rendering.util.cache;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Requests that can render different content must get different page cache
 * keys, in the weblog page cache and in the site-wide cache that the front
 * page weblog uses.
 */
public class WeblogPageCacheKeyTest {

    private static WeblogPageRequest request(String context, String anchor,
            String pageName, String... tags) {
        WeblogPageRequest request = new WeblogPageRequest();
        request.setWeblogHandle("blog");
        request.setContext(context);
        request.setWeblogAnchor(anchor);
        request.setWeblogPageName(pageName);
        if (tags.length > 0) {
            request.setTags(Arrays.asList(tags));
        }
        return request;
    }

    private static final List<Function<WeblogPageRequest, String>> CACHES = List.of(
            request -> WeblogPageCache.getInstance().generateKey(request),
            request -> SiteWideCache.getInstance().generateKey(request));

    @Test
    public void customPageNamingAnEntryHasItsOwnKey() {
        for (Function<WeblogPageRequest, String> key : CACHES) {
            String permalink = key.apply(request("entry", "first-post", null));
            String customPage = key.apply(request("page", "first-post", "archives"));
            assertNotEquals(permalink, customPage);
        }
    }

    @Test
    public void tagsOnACustomPageAreInTheKey() {
        for (Function<WeblogPageRequest, String> key : CACHES) {
            String plain = key.apply(request("page", null, "archives"));
            String tagged = key.apply(request("page", null, "archives", "java"));
            String otherTag = key.apply(request("page", null, "archives", "roller"));
            assertNotEquals(plain, tagged);
            assertNotEquals(tagged, otherTag);
        }
    }

    @Test
    public void tagsOnACustomPageNamingAnEntryAreInTheKey() {
        for (Function<WeblogPageRequest, String> key : CACHES) {
            String untagged = key.apply(request("page", "first-post", "archives"));
            String tagged = key.apply(request("page", "first-post", "archives", "java"));
            assertNotEquals(untagged, tagged);
        }
    }

    @Test
    public void tagOrderDoesNotChangeTheKey() {
        for (Function<WeblogPageRequest, String> key : CACHES) {
            assertEquals(key.apply(request("tags", null, null, "java", "roller")),
                    key.apply(request("tags", null, null, "roller", "java")));
        }
    }
}
