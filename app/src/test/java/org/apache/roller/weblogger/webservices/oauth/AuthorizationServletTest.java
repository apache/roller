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

package org.apache.roller.weblogger.webservices.oauth;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthProblemException;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies which identity {@link AuthorizationServlet} authorizes a request
 * token for.
 *
 * <p>The servlet runs the browser consent step: a logged-in user approves a
 * consumer's pending request token. The identity being approved is a property
 * of the session, so these tests pin it there and check that values arriving
 * in the request body cannot redirect the approval onto a different account.
 */
public class AuthorizationServletTest {

    private static final String CONSUMER_KEY = "test-consumer-key";
    private static final String REQUEST_TOKEN = "test-request-token";

    private AuthorizationServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private RollerSession rollerSession;

    @Mock
    private Weblogger weblogger;

    @Mock
    private OAuthManager oauthManager;

    private OAuthAccessor accessor;
    private StringWriter responseBody;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new AuthorizationServlet();

        // A site-wide consumer: no "userId" property bound to the key. This is
        // the configuration in which the servlet has no consumer-side identity
        // to compare against and must fall back on the session.
        OAuthConsumer consumer =
                new OAuthConsumer("http://example.com/callback", CONSUMER_KEY, "secret", null);
        accessor = new OAuthAccessor(consumer);
        accessor.requestToken = REQUEST_TOKEN;

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL())
                .thenReturn(new StringBuffer("https://example.com/roller-services/oauth/authorize"));
        when(request.getLocalName()).thenReturn("example.com");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        when(weblogger.getOAuthManager()).thenReturn(oauthManager);
        when(oauthManager.getAccessor(any())).thenReturn(accessor);
    }

    private User user(String userName) {
        User u = new User();
        u.setUserName(userName);
        u.setEnabled(Boolean.TRUE);
        return u;
    }

    /**
     * Assert that no approval was recorded for {@code userName}, through either
     * of the manager's authorizing entry points. Checking both matters: a test
     * that named only one of them would pass whenever the servlet happened to
     * use the other.
     */
    private void verifyNothingAuthorizedFor(String userName) throws Exception {
        verify(oauthManager, never()).markAsAuthorized(any(), eq(userName));
        verify(oauthManager, never()).authorizeRequestToken(anyString(), anyString(), eq(userName));
    }

    /**
     * Assert that no approval was recorded at all.
     */
    private void verifyNothingAuthorized() throws Exception {
        verify(oauthManager, never()).markAsAuthorized(any(), anyString());
        verify(oauthManager, never()).authorizeRequestToken(anyString(), anyString(), anyString());
    }

    /**
     * The session belongs to "alice" but the posted form names "admin". The
     * approval must not be recorded for "admin".
     */
    @Test
    public void postedUserIdDoesNotChooseTheIdentity() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(user("alice"));

            when(request.getParameter("userId")).thenReturn("admin");

            servlet.doPost(request, response);

            verifyNothingAuthorizedFor("admin");
        }
    }

    /**
     * Same shape, using the alternate parameter name the servlet also reads.
     */
    @Test
    public void postedRequestorIdDoesNotChooseTheIdentity() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(user("alice"));

            when(request.getParameter("xoauth_requestor_id")).thenReturn("admin");

            servlet.doPost(request, response);

            verifyNothingAuthorizedFor("admin");
        }
    }

    /**
     * With nobody logged in there is no identity to approve, so nothing may be
     * recorded no matter what the request body says.
     */
    @Test
    public void noSessionAuthorizesNobody() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(null);

            when(request.getParameter("userId")).thenReturn("admin");

            servlet.doPost(request, response);

            verifyNothingAuthorized();
        }
    }

    /**
     * A disabled account cannot approve anything, even with a live session.
     */
    @Test
    public void disabledUserAuthorizesNobody() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            User disabled = user("alice");
            disabled.setEnabled(Boolean.FALSE);
            when(rollerSession.getAuthenticatedUser()).thenReturn(disabled);
            when(request.getParameter("userId")).thenReturn("alice");

            servlet.doPost(request, response);

            verifyNothingAuthorized();
        }
    }

    /**
     * The ordinary path: the logged-in user approves, and the approval is
     * recorded against the session identity and the pending token.
     */
    @Test
    public void sessionUserIsTheAuthorizedIdentity() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(user("alice"));
            when(oauthManager.authorizeRequestToken(CONSUMER_KEY, REQUEST_TOKEN, "alice"))
                    .thenReturn(Boolean.TRUE);

            servlet.doPost(request, response);

            verify(oauthManager).authorizeRequestToken(CONSUMER_KEY, REQUEST_TOKEN, "alice");
        }
    }

    /**
     * A legacy client may still post the parameter; when it agrees with the
     * session it is simply redundant and the flow proceeds.
     */
    @Test
    public void matchingPostedUserIdIsTolerated() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(user("alice"));
            when(request.getParameter("userId")).thenReturn("alice");
            when(oauthManager.authorizeRequestToken(CONSUMER_KEY, REQUEST_TOKEN, "alice"))
                    .thenReturn(Boolean.TRUE);

            servlet.doPost(request, response);

            verify(oauthManager).authorizeRequestToken(CONSUMER_KEY, REQUEST_TOKEN, "alice");
        }
    }

    /**
     * A consumer key bound to one user may only be approved by that user, even
     * though the identity now comes from the session rather than the request.
     */
    @Test
    public void consumerBoundToAnotherUserIsRefused() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            accessor.consumer.setProperty("userId", "bob");

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(user("alice"));
            when(request.getParameter("userId")).thenReturn("bob");

            servlet.doPost(request, response);

            verifyNothingAuthorized();
        }
    }

    /**
     * Failure of the one-shot persistence transition must not reveal why the
     * pending token was not claimed.
     */
    @Test
    public void unmatchedPendingTokenIsRefusedGenerically() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<RollerSession> session = mockStatic(RollerSession.class)) {

            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            session.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            when(rollerSession.getAuthenticatedUser()).thenReturn(user("alice"));
            when(oauthManager.authorizeRequestToken(CONSUMER_KEY, REQUEST_TOKEN, "alice"))
                    .thenReturn(Boolean.FALSE);

            servlet.doPost(request, response);

            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            assertEquals("oauth_problem=permission_denied\n", responseBody.toString());
            verify(weblogger, never()).flush();
        }
    }

    /**
     * Accessor lookup failures use the same response as a failed conditional
     * transition, so callers cannot distinguish token states.
     */
    @Test
    public void unknownTokenIsRefusedGenerically() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            when(oauthManager.getAccessor(any()))
                    .thenThrow(new OAuthProblemException("token_expired"));

            servlet.doPost(request, response);

            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            assertEquals("oauth_problem=permission_denied\n", responseBody.toString());
            verifyNothingAuthorized();
        }
    }
}
