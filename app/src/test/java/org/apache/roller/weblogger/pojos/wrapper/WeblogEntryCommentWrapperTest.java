/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.pojos.wrapper;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.CommentAuthorUrl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WeblogEntryCommentWrapperTest {

    @Test
    void exposesEscapedHttpAndHttpsAuthorUrls() {
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setUrl(" https://example.org/profile?a=1&b=2 ");

        WeblogEntryCommentWrapper wrapper = WeblogEntryCommentWrapper.wrap(comment, null);

        assertEquals("https://example.org/profile?a=1&amp;b=2", wrapper.getUrl());
        assertEquals("https://example.org/profile?a=1&b=2", comment.getSafeUrl());
    }

    @Test
    void acceptsLocalAndInternationalizedAuthorUrls() {
        assertEquals("http://localhost:8080/profile",
                CommentAuthorUrl.normalize("http://localhost:8080/profile"));
        assertEquals("https://intranet/profile",
                CommentAuthorUrl.normalize("https://intranet/profile"));
        assertEquals("http://my_host.example.com/profile",
                CommentAuthorUrl.normalize("http://my_host.example.com/profile"));
        assertEquals("https://例え.テスト/profile",
                CommentAuthorUrl.normalize("https://例え.テスト/profile"));
        assertEquals("http://Example.org/CaseSensitive",
                CommentAuthorUrl.normalizeInput("Example.org/CaseSensitive"));
    }

    @Test
    void omitsUnsupportedOrMalformedAuthorUrls() {
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setUrl("javascript:alert(1)");
        assertEquals("", WeblogEntryCommentWrapper.wrap(comment, null).getUrl());
        assertNull(comment.getSafeUrl());

        comment.setUrl("//example.org/profile");
        assertEquals("", WeblogEntryCommentWrapper.wrap(comment, null).getUrl());

        comment.setUrl("not a url");
        assertEquals("", WeblogEntryCommentWrapper.wrap(comment, null).getUrl());

        comment.setUrl("  ");
        assertEquals("", WeblogEntryCommentWrapper.wrap(comment, null).getUrl());

        assertNull(CommentAuthorUrl.normalize("https://user@example.org/profile"));
        assertNull(CommentAuthorUrl.normalize("https://example.org:99999/profile"));
    }
}
