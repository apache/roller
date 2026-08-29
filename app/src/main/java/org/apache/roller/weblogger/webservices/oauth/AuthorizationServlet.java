/*
 * Copyright 2007 AOL, LLC.
 * Portions Copyright 2009 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.webservices.oauth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;

/**
 * Authorization request handler.
 *
 * @author Praveen Alavilli
 * @author Dave Johnson (adapted for Roller)
 */
public class AuthorizationServlet extends HttpServlet {
    protected static final Log log = LogFactory.getFactory().getInstance(AuthorizationServlet.class);

    /**
     * One response for every refusal, so the endpoint reveals nothing about
     * tokens the caller does not hold.
     */
    private static final String PERMISSION_DENIED = "permission_denied";


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        try{
            OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            
            OAuthManager omgr = WebloggerFactory.getWeblogger().getOAuthManager();
            OAuthAccessor accessor = omgr.getAccessor(requestMessage);
           
            if (Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
                // already authorized send the user back
                returnToConsumer(request, response, accessor);
            } else {
                sendToAuthorizePage(request, response, accessor);
            }
        
        } catch (Exception e){
            handleException(e, request, response, true);
        }
    }
    
    @Override 
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        try{
            OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);

            OAuthManager omgr = WebloggerFactory.getWeblogger().getOAuthManager();
            OAuthAccessor accessor = omgr.getAccessor(requestMessage);
            if (accessor == null || accessor.consumer == null || accessor.requestToken == null) {
                denyPermission(response);
                return;
            }

            // The approving identity comes from the browser session, consistent
            // with the rest of the UI. Without a session there is nobody to
            // approve on behalf of, so send the caller through the login flow.
            User user = getAuthenticatedUser(request);
            if (user == null) {
                sendToAuthorizePage(request, response, accessor);
                return;
            }
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                denyPermission(response);
                return;
            }
            String userId = user.getUserName();

            // A consumer key bound to one user may only be approved by that
            // user. A site-wide key has no bound user and is approved as
            // whoever is logged in.
            String consumerUserId = (String)accessor.consumer.getProperty("userId");
            if (consumerUserId != null && !consumerUserId.equals(userId)) {
                denyPermission(response);
                return;
            }

            // Older clients still post the identity; accept it only when it
            // agrees with the session.
            String submittedUserId = request.getParameter("userId");
            if (submittedUserId == null) {
                submittedUserId = request.getParameter("xoauth_requestor_id");
            }
            if (submittedUserId != null && !submittedUserId.equals(userId)) {
                denyPermission(response);
                return;
            }

            // Claim the pending request token in one conditional statement, so
            // approval is one-shot. A token that is missing, belongs to another
            // consumer, or has already been approved or exchanged all produce
            // the same answer here and the same response below.
            if (!omgr.authorizeRequestToken(
                    accessor.consumer.consumerKey, accessor.requestToken, userId)) {
                denyPermission(response);
                return;
            }
            WebloggerFactory.getWeblogger().flush();

            accessor.setProperty("userId", userId);
            accessor.setProperty("authorized", Boolean.TRUE);

            returnToConsumer(request, response, accessor);

        } catch (OAuthProblemException e) {
            denyPermission(response);
        } catch (Exception e){
            handleException(e, request, response, true);
        }
    }

    /**
     * The Roller user behind this request's session, or null if there is none.
     */
    private User getAuthenticatedUser(HttpServletRequest request) {
        RollerSession rollerSession = RollerSession.getRollerSession(request);
        return rollerSession == null ? null : rollerSession.getAuthenticatedUser();
    }

    /**
     * Refuse the approval, in the OAuth problem-reporting form and with the
     * same body for every reason. Written directly rather than thrown so the
     * response does not vary with how the library happens to render a given
     * exception.
     */
    private void denyPermission(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter()) {
            out.println("oauth_problem=" + PERMISSION_DENIED);
        }
    }

    private void sendToAuthorizePage(HttpServletRequest request, 
            HttpServletResponse response, OAuthAccessor accessor)
    throws IOException, ServletException{
        String callback = request.getParameter("oauth_callback");
        if(callback == null || callback.length() <=0) {
            callback = "none";
        }
        String consumer_description = (String)accessor.consumer.getProperty("description");
        request.setAttribute("CONS_DESC", consumer_description);
        request.setAttribute("CALLBACK", callback);
        request.setAttribute("TOKEN", accessor.requestToken);
        request.getRequestDispatcher("/roller-ui/oauthAuthorize.rol").forward(request, response);
    }
    
    private void returnToConsumer(HttpServletRequest request, 
            HttpServletResponse response, OAuthAccessor accessor)
        throws IOException, ServletException {

        // send the user back to site's callBackUrl
        String callback = request.getParameter("oauth_callback");
        if ("none".equals(callback)
            && accessor.consumer.callbackURL != null 
                && accessor.consumer.callbackURL.length() > 0){
            // first check if we have something in our properties file
            callback = accessor.consumer.callbackURL;
        }
        
        if ( "none".equals(callback) ) {
            // no call back it must be a client
            response.setContentType("text/plain");
            try (PrintWriter out = response.getWriter()) {
                out.println("You have successfully authorized for consumer key '"
                        + accessor.consumer.consumerKey
                        + "'. Please close this browser window and click continue"
                                + " in the client.");
            }
        } else {
            // if callback is not passed in, use the callback from config
            if(callback == null || callback.length() <=0 ) {
                callback = accessor.consumer.callbackURL;
            }
            String token = accessor.requestToken;
            if (token != null && callback != null) {
                callback = OAuth.addParameters(callback, "oauth_token", token);
            }

            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", callback);
        }
    }

    public void handleException(Exception e, HttpServletRequest request,
            HttpServletResponse response, boolean sendBody)
            throws IOException, ServletException {
        log.debug("ERROR authorizing token", e);
        String realm = (request.isSecure())?"https://":"http://";
        realm += request.getLocalName();
        OAuthServlet.handleException(response, e, realm, sendBody);
    }
}
