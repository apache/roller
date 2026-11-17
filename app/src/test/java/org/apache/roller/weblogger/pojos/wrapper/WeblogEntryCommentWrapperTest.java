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

import java.util.List;

import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.comment.HTMLSubsetPlugin;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.CommentAuthorUrl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

    private static String content(String contentType, String commentPlugins,
            List<WeblogEntryCommentPlugin> configured) {
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setContentType(contentType);
        comment.setPlugins(commentPlugins);
        comment.setContent("<b>Hi</b><img src=x onerror=alert(1)>");

        // apply the configured plugins the comment lists, like PluginManagerImpl
        PluginManager plugins = mock(PluginManager.class);
        when(plugins.getCommentPlugins()).thenReturn(configured);
        when(plugins.applyCommentPlugins(any(), anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(1);
            for (WeblogEntryCommentPlugin plugin : configured) {
                if (commentPlugins != null && commentPlugins.contains(plugin.getId())) {
                    text = plugin.render(comment, text);
                }
            }
            return text;
        });
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPluginManager()).thenReturn(plugins);

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            return WeblogEntryCommentWrapper.wrap(comment, null).getContent();
        }
    }

    @Test
    void htmlCommentIsEscapedWithoutHtmlSubsetPlugin() {
        String escaped = "&lt;b&gt;Hi&lt;/b&gt;&lt;img src=x onerror=alert(1)&gt;";
        assertEquals(escaped, content("text/html", null, List.of()));
        assertEquals(escaped, content("text/html", "AutoFormat", List.of()));
        // listed on the comment, but no longer configured
        assertEquals(escaped, content("text/html", "HTMLSubset", List.of()));
        // configured, but not listed on the comment
        assertEquals(escaped, content("text/html", "", List.of(new HTMLSubsetPlugin())));
    }

    @Test
    void htmlCommentKeepsHtmlSubsetOutput() {
        assertEquals("<b>Hi</b>&lt;img src=x onerror=alert(1)&gt;",
                content("text/html", "HTMLSubset", List.of(new HTMLSubsetPlugin())));
    }

    @Test
    void plainTextCommentIsEscapedOnce() {
        assertEquals("&lt;b&gt;Hi&lt;/b&gt;&lt;img src=x onerror=alert(1)&gt;",
                content("text/plain", "HTMLSubset", List.of(new HTMLSubsetPlugin())));
    }
}
