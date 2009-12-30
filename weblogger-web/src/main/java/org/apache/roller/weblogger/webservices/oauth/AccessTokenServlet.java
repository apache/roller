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
import java.io.OutputStream;

import javax.servlet.ServletConfig;
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

/**
 * Access Token request handler
 *
 * @author Praveen Alavilli
 * @author Dave Johnson (adapted for Roller)
 */
public class AccessTokenServlet extends HttpServlet {
    protected static Log log =
            LogFactory.getFactory().getInstance(AccessTokenServlet.class);
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // nothing at this point
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
        
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try{
            OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            
            OAuthManager omgr = WebloggerFactory.getWeblogger().getOAuthManager();
            OAuthAccessor accessor = omgr.getAccessor(requestMessage);
            omgr.getValidator().validateMessage(requestMessage, accessor);
            
            // make sure token is authorized
            if (!Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
                 OAuthProblemException problem = new OAuthProblemException("permission_denied");
                throw problem;
            }
            // generate access token and secret
            if (accessor.accessToken == null) {
                omgr.generateAccessToken(accessor);
                WebloggerFactory.getWeblogger().flush();
            }

            response.setContentType("text/plain");
            OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList(
                "oauth_token", accessor.accessToken,
                "oauth_token_secret", accessor.tokenSecret), out);
            out.close();
            
        } catch (Exception e){
            handleException(e, request, response, true);
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
