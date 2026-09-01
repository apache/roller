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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommentDataServletTest {

    @Test
    public void testRepeatedSavesReturnFreshResponseSalts() throws Exception {
        Weblogger weblogger = mock(Weblogger.class);
        WeblogEntryManager manager = mock(WeblogEntryManager.class);
        WeblogEntryComment comment = mock(WeblogEntryComment.class);
        WeblogEntry entry = mock(WeblogEntry.class);
        Weblog weblog = mock(Weblog.class);
        RollerSession session = mock(RollerSession.class);
        User user = mock(User.class);

        when(weblogger.getWeblogEntryManager()).thenReturn(manager);
        when(manager.getComment("comment-id")).thenReturn(comment);
        when(comment.getWeblogEntry()).thenReturn(entry);
        when(entry.getWebsite()).thenReturn(weblog);
        when(session.getAuthenticatedUser()).thenReturn(user);
        when(weblog.hasUserPermission(user, WeblogPermission.POST)).thenReturn(true);
        when(comment.getId()).thenReturn("comment-id");
        when(comment.getContent()).thenReturn("updated");

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> sessions = mockStatic(RollerSession.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

            HttpServletRequest firstRequest = saveRequest("first-response-salt");
            HttpServletResponse firstResponse = mock(HttpServletResponse.class);
            StringWriter firstBody = responseBody(firstResponse);
            sessions.when(() -> RollerSession.getRollerSession(firstRequest)).thenReturn(session);

            new CommentDataServlet().doPost(firstRequest, firstResponse);

            HttpServletRequest secondRequest = saveRequest("second-response-salt");
            HttpServletResponse secondResponse = mock(HttpServletResponse.class);
            StringWriter secondBody = responseBody(secondResponse);
            sessions.when(() -> RollerSession.getRollerSession(secondRequest)).thenReturn(session);

            new CommentDataServlet().doPost(secondRequest, secondResponse);

            assertTrue(firstBody.toString().contains(
                    "\"salt\":\"first-response-salt\""));
            assertTrue(secondBody.toString().contains(
                    "\"salt\":\"second-response-salt\""));
            verify(manager, times(2)).saveComment(comment);
        }
    }

    @Test
    public void testHandledErrorReturnsFreshResponseSalt() throws Exception {
        Weblogger weblogger = mock(Weblogger.class);
        WeblogEntryManager manager = mock(WeblogEntryManager.class);
        when(weblogger.getWeblogEntryManager()).thenReturn(manager);
        when(manager.getComment("missing-id")).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("id")).thenReturn("missing-id");
        when(request.getAttribute("salt")).thenReturn("replacement-salt");
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

            new CommentDataServlet().doPost(request, response);
        }

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(writer).print("{\"salt\":\"replacement-salt\"}");
    }

    private HttpServletRequest saveRequest(String responseSalt) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("id")).thenReturn("comment-id");
        when(request.getAttribute("salt")).thenReturn(responseSalt);
        when(request.getInputStream()).thenReturn(
                new ByteArrayServletInputStream("updated"));
        return request;
    }

    private StringWriter responseBody(HttpServletResponse response) throws Exception {
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        return body;
    }

    private static class ByteArrayServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream input;

        ByteArrayServletInputStream(String value) {
            input = new ByteArrayInputStream(
                    value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }
    }
}
