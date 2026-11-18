/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MailProvider;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Emails only link to the site through site.absoluteurl, never through the
 * host name of the request that caused them.
 */
class MailUtilSiteUrlTest {

    private static final String REQUEST_URL = "http://forged.example.net";
    private static final String SITE_URL = "https://blog.example.org";

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<WebloggerStartup> startup;
    private PropertiesManager properties;
    private WeblogManager weblogs;
    private final List<String> sent = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        properties = mock(PropertiesManager.class);
        siteUrl("");
        when(properties.getProperty("users.comments.emailnotify")).thenAnswer(
                invocation -> new RuntimeConfigProperty("users.comments.emailnotify", "true"));

        URLStrategy urls = mock(URLStrategy.class);
        when(urls.getWeblogCommentsURL(any(), isNull(), anyString(), anyBoolean())).thenAnswer(
                invocation -> WebloggerRuntimeConfig.getAbsoluteContextURL() + "/blog/entry/first#comments");
        when(urls.getActionURL(eq("comments"), anyString(), anyString(), any(), anyBoolean())).thenAnswer(
                invocation -> WebloggerRuntimeConfig.getAbsoluteContextURL() + "/roller-ui/authoring/comments.rol");
        when(urls.getEntryEditURL(anyString(), anyString(), anyBoolean())).thenAnswer(
                invocation -> WebloggerRuntimeConfig.getAbsoluteContextURL() + "/roller-ui/authoring/entryEdit.rol");

        weblogs = mock(WeblogManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        when(weblogger.getUrlStrategy()).thenReturn(urls);
        when(weblogger.getWeblogManager()).thenReturn(weblogs);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

        Transport transport = mock(Transport.class);
        doAnswer(invocation -> {
            sent.add(String.valueOf(((Message) invocation.getArgument(0)).getContent()));
            return null;
        }).when(transport).sendMessage(any(Message.class), any(Address[].class));
        MailProvider mail = mock(MailProvider.class);
        when(mail.getSession()).thenReturn(Session.getInstance(new Properties()));
        when(mail.getTransport()).thenReturn(transport);
        startup = mockStatic(WebloggerStartup.class);
        startup.when(WebloggerStartup::getMailProvider).thenReturn(mail);

        WebloggerRuntimeConfig.setRequestAbsoluteContextURL(REQUEST_URL);
    }

    @AfterEach
    void tearDown() {
        WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();
        startup.close();
        factory.close();
    }

    private void siteUrl(String url) throws Exception {
        when(properties.getProperty("site.absoluteurl")).thenAnswer(
                invocation -> new RuntimeConfigProperty("site.absoluteurl", url));
    }

    private static User user() {
        User user = mock(User.class);
        when(user.getUserName()).thenReturn("owner");
        when(user.getFullName()).thenReturn("Blog Owner");
        when(user.getEmailAddress()).thenReturn("owner@example.org");
        when(user.getLocale()).thenReturn("en");
        when(user.getActivationCode()).thenReturn("code");
        return user;
    }

    private static Weblog weblog() {
        Weblog weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("blog");
        when(weblog.getName()).thenReturn("Blog");
        when(weblog.getEmailAddress()).thenReturn("blog@example.org");
        when(weblog.getLocaleInstance()).thenReturn(Locale.ENGLISH);
        when(weblog.getEmailComments()).thenReturn(true);
        return weblog;
    }

    private static WeblogEntryComment pendingComment() {
        Weblog weblog = weblog();
        User owner = user();
        WeblogEntry entry = mock(WeblogEntry.class);
        when(entry.getWebsite()).thenReturn(weblog);
        when(entry.getCreator()).thenReturn(owner);
        when(entry.getAnchor()).thenReturn("first");
        when(entry.getTitle()).thenReturn("First");
        when(entry.getId()).thenReturn("entry-id");
        WeblogEntryComment comment = mock(WeblogEntryComment.class);
        when(comment.getWeblogEntry()).thenReturn(entry);
        when(comment.getPending()).thenReturn(true);
        when(comment.getApproved()).thenReturn(false);
        when(comment.getName()).thenReturn("Visitor");
        when(comment.getEmail()).thenReturn("visitor@example.org");
        when(comment.getContent()).thenReturn("Nice post");
        return comment;
    }

    private static void sendCommentNotification() throws Exception {
        MailUtil.sendEmailNotification(pendingComment(), new RollerMessages(),
                I18nMessages.getMessages(Locale.ENGLISH), true);
    }

    @Test
    void commentNotificationsLeaveOutLinksWhileTheSiteUrlIsUnset() throws Exception {
        sendCommentNotification();

        assertEquals(1, sent.size(), "the owner is still told about the comment");
        assertTrue(sent.get(0).contains("Nice post"));
        assertFalse(sent.get(0).contains("forged.example.net"), sent.get(0));
    }

    @Test
    void commentNotificationsLinkToTheSiteUrlOnceSet() throws Exception {
        siteUrl(SITE_URL);

        sendCommentNotification();

        assertEquals(1, sent.size());
        assertTrue(sent.get(0).contains(SITE_URL + "/blog/entry/first#comments"), sent.get(0));
        assertTrue(sent.get(0).contains(SITE_URL + "/roller-ui/authoring/comments.rol"), sent.get(0));
        assertFalse(sent.get(0).contains("forged.example.net"));
    }

    @Test
    void approvalNotificationsLeaveOutLinksWhileTheSiteUrlIsUnset() throws Exception {
        MailUtil.sendEmailApprovalNotification(pendingComment(),
                I18nMessages.getMessages(Locale.ENGLISH));

        assertEquals(1, sent.size());
        assertFalse(sent.get(0).contains("forged.example.net"), sent.get(0));
    }

    @Test
    void invitationsAreNotEmailedWhileTheSiteUrlIsUnset() {
        assertThrows(WebloggerException.class,
                () -> MailUtil.sendWeblogInvitation(weblog(), user()));
        assertTrue(sent.isEmpty(), String.valueOf(sent));
    }

    @Test
    void activationsAreNotEmailedWhileTheSiteUrlIsUnset() {
        assertThrows(WebloggerException.class, () -> MailUtil.sendUserActivationEmail(user()));
        assertTrue(sent.isEmpty(), String.valueOf(sent));
    }

    @Test
    void activationsLinkToTheSiteUrlOnceSet() throws Exception {
        siteUrl(SITE_URL);

        MailUtil.sendUserActivationEmail(user());

        assertEquals(1, sent.size());
        assertTrue(sent.get(0).contains(SITE_URL + "/roller-ui/register!activate.rol"), sent.get(0));
    }

    @Test
    void pendingEntryNoticesAreNotEmailedWhileTheSiteUrlIsUnset() throws Exception {
        Weblog weblog = weblog();
        User author = user();
        when(weblog.hasUserPermission(any(User.class), eq(WeblogPermission.POST))).thenReturn(true);
        when(weblogs.getWeblogUsers(weblog, true)).thenReturn(List.of(author));
        WeblogEntry entry = mock(WeblogEntry.class);
        when(entry.getWebsite()).thenReturn(weblog);
        when(entry.getCreator()).thenReturn(author);
        when(entry.getId()).thenReturn("entry-id");

        MailUtil.sendPendingEntryNotice(entry);

        assertTrue(sent.isEmpty(), String.valueOf(sent));
    }
}
