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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

import org.apache.roller.weblogger.business.MailProvider;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * A subject built from an entry title or weblog name stays one header line.
 */
class MailUtilSubjectTest {

    private MockedStatic<WebloggerStartup> startup;
    private final List<MimeMessage> sent = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        Transport transport = mock(Transport.class);
        doAnswer(invocation -> {
            sent.add((MimeMessage) invocation.getArgument(0));
            return null;
        }).when(transport).sendMessage(any(Message.class), any(Address[].class));
        MailProvider mail = mock(MailProvider.class);
        when(mail.getSession()).thenReturn(Session.getInstance(new Properties()));
        when(mail.getTransport()).thenReturn(transport);
        startup = mockStatic(WebloggerStartup.class);
        startup.when(WebloggerStartup::getMailProvider).thenReturn(mail);
    }

    @AfterEach
    void tearDown() {
        startup.close();
    }

    @Test
    void lineBreaksInTheSubjectDoNotStartNewHeaders() throws Exception {
        MailUtil.sendTextMessage("blog@example.org", new String[] {"owner@example.org"}, null, null,
                "Pending: Notice\r\nReply-To: other@example.net\r\nContent-Type: text/html\r\n\r\nText",
                "body");

        assertEquals(1, sent.size());
        MimeMessage message = sent.get(0);
        assertEquals("Pending: Notice Reply-To: other@example.net Content-Type: text/html Text",
                message.getSubject());
        assertNull(message.getHeader("Reply-To"));

        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        message.writeTo(raw);
        String headers = raw.toString(StandardCharsets.UTF_8.name()).split("\r\n\r\n", 2)[0];
        assertTrue(headers.lines().noneMatch(line -> line.startsWith("Reply-To")), headers);
        assertEquals(1, headers.lines().filter(line -> line.startsWith("Content-Type")).count(), headers);
        assertTrue(headers.contains("Content-Type: text/plain; charset=utf-8"), headers);
    }

    @Test
    void otherControlCharactersBecomeSpaces() {
        assertEquals("a b c", MailUtil.singleLine("a\tb\u0000\u001fc\n"));
        assertEquals("Plain title", MailUtil.singleLine("Plain title"));
    }
}
