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
package org.apache.roller.weblogger.pojos.wrapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Blogroll links reach templates only when they are relative or use a scheme
 * a blogroll link needs.
 */
class WeblogBookmarkWrapperTest {

    @Test
    void keepsWebAndRelativeLinks() {
        assertEquals("https://example.com/a", WeblogBookmarkWrapper.linkOrEmpty("https://example.com/a"));
        assertEquals("HTTP://example.com/", WeblogBookmarkWrapper.linkOrEmpty("HTTP://example.com/"));
        assertEquals("ftp://example.com/f", WeblogBookmarkWrapper.linkOrEmpty("ftp://example.com/f"));
        assertEquals("mailto:reader@example.com", WeblogBookmarkWrapper.linkOrEmpty("mailto:reader@example.com"));
        assertEquals("/blog/page", WeblogBookmarkWrapper.linkOrEmpty("/blog/page"));
        assertEquals("page?a=b:c", WeblogBookmarkWrapper.linkOrEmpty("page?a=b:c"));
        assertNull(WeblogBookmarkWrapper.linkOrEmpty(null));
    }

    @Test
    void dropsOtherSchemes() {
        assertEquals("", WeblogBookmarkWrapper.linkOrEmpty("data:text/plain,x"));
        assertEquals("", WeblogBookmarkWrapper.linkOrEmpty("FILE:///etc/x"));
        assertEquals("", WeblogBookmarkWrapper.linkOrEmpty(" fi\tle:///etc/x"));
    }

    @Test
    void dropsCharacterReferencesBeforeThePath() {
        assertEquals("", WeblogBookmarkWrapper.linkOrEmpty("x&#58;y"));
        assertEquals("", WeblogBookmarkWrapper.linkOrEmpty("x&colon;y"));
        assertEquals("", WeblogBookmarkWrapper.linkOrEmpty("&#120;:y"));
        assertEquals("/a?b=1&c=2", WeblogBookmarkWrapper.linkOrEmpty("/a?b=1&c=2"));
    }
}
