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
package org.apache.roller.weblogger.ui.struts2.ajax;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserDataServletTest {

    @Test
    public void testNonAdminSearchesUserNamesOnly() throws Exception {
        User caller = mock(User.class);
        when(caller.hasGlobalPermission("admin")).thenReturn(false);
        UserManager users = userManager();

        String body = search(caller, users);

        verify(users).getUsersWithUserNameStartingWith("ali", Boolean.TRUE, 0, 50);
        verify(users, never()).getUsersStartingWith(anyString(), any(), anyInt(), anyInt());
        assertEquals("alice,Alice\n", body.replace("\r", ""));
    }

    @Test
    public void testAdminSearchesUserNamesAndEmail() throws Exception {
        User caller = mock(User.class);
        when(caller.hasGlobalPermission("admin")).thenReturn(true);
        UserManager users = userManager();

        String body = search(caller, users);

        verify(users).getUsersStartingWith("ali", Boolean.TRUE, 0, 50);
        verify(users, never()).getUsersWithUserNameStartingWith(anyString(), any(), anyInt(), anyInt());
        assertEquals("alice,alice@example.com\n", body.replace("\r", ""));
    }

    private static UserManager userManager() throws Exception {
        User alice = mock(User.class);
        when(alice.getUserName()).thenReturn("alice");
        when(alice.getScreenName()).thenReturn("Alice");
        when(alice.getEmailAddress()).thenReturn("alice@example.com");
        UserManager users = mock(UserManager.class);
        when(users.getUsersStartingWith("ali", Boolean.TRUE, 0, 50)).thenReturn(List.of(alice));
        when(users.getUsersWithUserNameStartingWith("ali", Boolean.TRUE, 0, 50)).thenReturn(List.of(alice));
        return users;
    }

    private static String search(User caller, UserManager users) throws Exception {
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getUserManager()).thenReturn(users);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("startsWith")).thenReturn("ali");
        when(request.getParameter("enabled")).thenReturn("true");
        when(request.getParameter("length")).thenReturn("50");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body, true));

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedConstruction<WeblogRequest> weblogRequests = mockConstruction(WeblogRequest.class,
                     (weblogRequest, context) -> when(weblogRequest.getUser()).thenReturn(caller))) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            new UserDataServlet().doGet(request, response);
        }
        return body.toString();
    }
}
