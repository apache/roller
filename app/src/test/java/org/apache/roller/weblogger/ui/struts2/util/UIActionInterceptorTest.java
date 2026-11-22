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
package org.apache.roller.weblogger.ui.struts2.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.jpa.JPAWeblogManagerImpl;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.LogCapture;
import org.apache.struts2.StrutsStatics;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * The interceptor must set the authenticated user and the action weblog from
 * trusted sources only, overwriting anything bound from request parameters.
 */
class UIActionInterceptorTest {

    @Test
    void weblogLookupKeepsTheHandleOnOneLineInTheLog() throws Exception {
        TestAction action = new TestAction();
        action.setWeblog("team\r\nnotes");
        ActionInvocation invocation = mock(ActionInvocation.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(invocation.getAction()).thenReturn(action);
        when(invocation.getInvocationContext()).thenReturn(new ActionContext(
                new HashMap<>(Map.of(StrutsStatics.HTTP_REQUEST, request))));
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getWeblogManager()).thenReturn(mock(JPAWeblogManagerImpl.class, CALLS_REAL_METHODS));

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             LogCapture log = new LogCapture(UIActionInterceptor.class, Level.ERROR)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            new UIActionInterceptor().doIntercept(invocation);

            assertEquals(1, log.events().size());
            LogEvent event = log.events().get(0);
            assertAll(
                    () -> assertEquals("Error looking up action weblog - team\\r\\nnotes",
                            event.getMessage().getFormattedMessage()),
                    () -> assertEquals("Invalid handle: 'team\\r\\nnotes'", event.getThrown().getMessage()));
        }
    }

    static class TestAction extends UIAction {
        TestAction() { this.actionName = "test"; }
        @Override public String execute() { return SUCCESS; }
    }

    @Test
    void parameterBoundUserAndWeblogAreOverwrittenWhenThereIsNoSession() throws Exception {
        // An attacker-controlled request with no session: getSession(false) is null,
        // so RollerSession.getRollerSession returns null.
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        // The action arrives with a user and weblog already bound from parameters.
        TestAction action = new TestAction();
        User forgedUser = new User();
        forgedUser.setUserName("siteadmin");
        action.setAuthenticatedUser(forgedUser);
        Weblog forgedWeblog = new Weblog();
        forgedWeblog.setHandle("team");
        action.setActionWeblog(forgedWeblog);
        // No weblog handle parameter, so no weblog can resolve.
        action.setWeblog(null);

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put(StrutsStatics.HTTP_REQUEST, request);
        ActionContext context = new ActionContext(contextMap);

        ActionInvocation invocation = mock(ActionInvocation.class);
        when(invocation.getAction()).thenReturn(action);
        when(invocation.getInvocationContext()).thenReturn(context);
        when(invocation.invoke()).thenReturn(UIAction.SUCCESS);

        new UIActionInterceptor().doIntercept(invocation);

        assertNull(action.getAuthenticatedUser());
        assertNull(action.getActionWeblog());
    }
}
