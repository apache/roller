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

package org.apache.roller.weblogger.util;

import java.util.Locale;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class MailUtilCommentNotificationTest {

    /** The owner's HTML notification shows the commenter's name and email as text. */
    @Test
    public void htmlNotificationEscapesCommenter() throws Exception {
        User owner = mock(User.class);
        when(owner.getEmailAddress()).thenReturn("owner@example.org");
        Weblog weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("blog");
        WeblogEntry entry = mock(WeblogEntry.class);
        when(entry.getWebsite()).thenReturn(weblog);
        when(entry.getCreator()).thenReturn(owner);
        when(entry.getTitle()).thenReturn("Post");
        when(entry.getId()).thenReturn("entry-id");

        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setWeblogEntry(entry);
        comment.setStatus(ApprovalStatus.PENDING);
        comment.setName("<img src=x onerror=alert(1)");
        comment.setEmail("a@example.org\"><img src=x onerror=alert(2)>");
        comment.setContent("Hello");

        URLStrategy urls = mock(URLStrategy.class);
        when(urls.getWeblogCommentsURL(any(), isNull(), any(), anyBoolean()))
                .thenReturn("https://example.org/blog/entry/post#comments");
        when(urls.getActionURL(anyString(), anyString(), anyString(), anyMap(), anyBoolean()))
                .thenReturn("https://example.org/roller-ui/authoring/comments");
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getUrlStrategy()).thenReturn(urls);

        ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class);
             // records the message; sending it fails without a mail provider,
             // which sendEmailNotification logs and ignores
             MockedStatic<MailUtil> mail = mockStatic(MailUtil.class, CALLS_REAL_METHODS)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("users.comments.htmlenabled"))
                    .thenReturn(true);

            MailUtil.sendEmailNotification(comment, new RollerMessages(),
                    I18nMessages.getMessages(Locale.ENGLISH), false);

            mail.verify(() -> MailUtil.sendHTMLMessage(any(), any(), any(), any(), any(), content.capture()));
        }

        String html = content.getValue();
        assertTrue(html.contains("&lt;img src=x onerror=alert(1)"), html);
        assertTrue(html.contains("a@example.org&quot;&gt;&lt;img src=x onerror=alert(2)&gt;"), html);
        assertFalse(html.contains("<img"), html);
    }
}
