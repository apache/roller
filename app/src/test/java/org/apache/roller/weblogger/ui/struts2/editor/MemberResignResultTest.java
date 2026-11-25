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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.ActionProxy;
import org.apache.struts2.ActionProxyFactory;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.interceptor.MessageStoreInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Resigning from a weblog goes back to the main menu, which shows whether
 * the resignation worked. Runs the action through Struts, so the configured
 * results and interceptors are used.
 */
class MemberResignResultTest {

    private Dispatcher dispatcher;
    private String ownerName;
    private String memberName;
    private String weblogId;
    private String handle;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        User owner = TestUtils.setupUser("resignresultowner");
        User member = TestUtils.setupUser("resignresultmember");
        ownerName = owner.getUserName();
        memberName = member.getUserName();
        // the creator becomes the weblog's administrator
        Weblog weblog = TestUtils.setupWeblog("resignresulttest", owner);
        weblogId = weblog.getId();
        handle = weblog.getHandle();
        TestUtils.endSession(true);

        // building action objects through Spring needs a running webapp; the
        // results and interceptors don't depend on which factory creates
        // actions
        dispatcher = new Dispatcher(mock(ServletContext.class),
                Collections.singletonMap("struts.objectFactory", "struts"));
        dispatcher.init();
        Dispatcher.setInstance(dispatcher);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            dispatcher.cleanup();
            Dispatcher.setInstance(null);
        } finally {
            TestUtils.endSession(true);
            TestUtils.teardownWeblog(weblogId);
            TestUtils.teardownUser(ownerName);
            TestUtils.teardownUser(memberName);
            TestUtils.endSession(true);
        }
    }

    @Test
    void lastAdministratorIsToldWhyTheyCannotResign() throws Exception {
        Map<String, Object> session = new HashMap<>();
        HttpServletResponse response = resign(ownerName, session);

        assertRedirectedToMainMenu(response);
        Collection<?> errors = (Collection<?>) session.get(MessageStoreInterceptor.actionErrorsSessionKey);
        assertNotNull(errors, "the error is kept for the main menu");
        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().toString().contains(handle), errors::toString);
        TestUtils.endSession(true);
        assertNotNull(permission(ownerName), "the weblog keeps its last administrator");
    }

    @Test
    void memberIsToldThatTheyResigned() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        Map<String, Object> session = new HashMap<>();
        HttpServletResponse response = resign(memberName, session);

        assertRedirectedToMainMenu(response);
        Collection<?> messages = (Collection<?>) session.get(MessageStoreInterceptor.actionMessagesSessionKey);
        assertNotNull(messages, "the message is kept for the main menu");
        assertEquals(1, messages.size());
        TestUtils.endSession(true);
        assertNull(permission(memberName));
    }

    @Test
    void resigningFromAWeblogThatDoesNotExistIsDenied() throws Exception {
        // the access-denied page is a Tiles result, which needs a running
        // webapp, so only the action's result code is checked
        assertEquals(UIAction.DENIED, invoke(ownerName, "resignresultnosuchweblog", new HashMap<>(),
                mock(HttpServletResponse.class), false));
        TestUtils.endSession(true);
        assertNotNull(permission(ownerName));
    }

    @Test
    void aUserWhoIsNotAMemberCannotResign() throws Exception {
        assertEquals(UIAction.DENIED, invoke(memberName, handle, new HashMap<>(),
                mock(HttpServletResponse.class), false));
    }

    private static void assertRedirectedToMainMenu(HttpServletResponse response) throws Exception {
        ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(location.capture());
        assertTrue(location.getValue().startsWith("/roller-ui/menu"), location.getValue());
    }

    // posts the resignation form for the weblog as the named user, keeping
    // the HTTP session's attributes in the given map
    private HttpServletResponse resign(String name, Map<String, Object> sessionAttributes) throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.encodeRedirectURL(anyString())).thenAnswer(call -> call.getArgument(0));
        invoke(name, handle, sessionAttributes, response, true);
        return response;
    }

    // posts the resignation form for the given weblog handle and returns the
    // action's result code
    private String invoke(String name, String weblogHandle, Map<String, Object> sessionAttributes,
            HttpServletResponse response, boolean executeResult) throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("resign-session");
        when(session.getAttribute(anyString())).thenAnswer(call -> sessionAttributes.get(call.getArgument(0)));
        doAnswer(call -> sessionAttributes.put(call.getArgument(0), call.getArgument(1)))
                .when(session).setAttribute(anyString(), any());

        Map<String, Object> requestAttributes = new HashMap<>();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        Principal principal = () -> name;
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.getParameterMap()).thenReturn(Map.of("weblog", new String[] {weblogHandle}));
        when(request.getParameter("weblog")).thenReturn(weblogHandle);
        when(request.getLocale()).thenReturn(Locale.ENGLISH);
        when(request.getAttribute(anyString())).thenAnswer(call -> requestAttributes.get(call.getArgument(0)));
        doAnswer(call -> requestAttributes.put(call.getArgument(0), call.getArgument(1)))
                .when(request).setAttribute(anyString(), any());
        when(request.getServletPath()).thenReturn("/roller-ui/authoring/memberResign!resign.rol");
        when(request.getContextPath()).thenReturn("");

        ActionMapping mapping = new ActionMapping("memberResign", "/roller-ui/authoring", "resign", new HashMap<>());
        Map<String, Object> context = dispatcher.createContextMap(request, response, mapping);
        ActionProxy proxy = dispatcher.getContainer().getInstance(ActionProxyFactory.class)
                .createActionProxy("/roller-ui/authoring", "memberResign", "resign", context, executeResult, false);
        return proxy.execute();
    }

    private WeblogPermission permission(String name) throws Exception {
        return users().getWeblogPermissionIncludingPending(weblog(), user(name));
    }

    private static UserManager users() {
        return WebloggerFactory.getWeblogger().getUserManager();
    }

    private Weblog weblog() throws Exception {
        return WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(weblogId);
    }

    private static User user(String name) throws Exception {
        return users().getUserByUserName(name, null);
    }
}
