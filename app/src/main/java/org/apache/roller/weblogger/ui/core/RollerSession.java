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

package org.apache.roller.weblogger.ui.core;

import com.opensymphony.xwork2.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.security.AutoProvision;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionListener;
import java.io.Serializable;
import java.security.Principal;


/**
 * Roller session handles session startup and shutdown.
 */
public class RollerSession implements HttpSessionListener, HttpSessionActivationListener, Serializable {
    
    private static final long serialVersionUID = 5890132909166913727L;
    private static final Log log;

    // the id of the user represented by this session
    private String userName = null;

    private SessionManager sessionManager;

    public static final String ROLLER_SESSION = "org.apache.roller.weblogger.rollersession";

    static{
        WebloggerConfig.init(); // must be called before calls to logging APIs
        log = LogFactory.getLog(RollerSession.class);
    }

    @Inject
    public RollerSession(SessionManager sessionManager, HttpServletRequest request) {
        this.sessionManager = sessionManager;

        // No session exists yet, nothing to do
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        // Get or create roller session in HTTP session
        RollerSession storedSession = (RollerSession)session.getAttribute(ROLLER_SESSION);
        if (storedSession == null) {
            session.setAttribute(ROLLER_SESSION, this);
        }
        // If stored session exists with authenticated user but not in cache, override it
        else if (storedSession.getAuthenticatedUser() != null
                && sessionManager.get(storedSession.getAuthenticatedUser().getUserName()) == null) {
            session.setAttribute(ROLLER_SESSION, this);
        }

        Principal principal = request.getUserPrincipal();

        // Skip authentication if no principal, user already authenticated, or system not bootstrapped
        if (getAuthenticatedUser() != null || principal == null || !WebloggerFactory.isBootstrapped()) {
            return;
        }

        try {
            UserManager userManager = WebloggerFactory.getWeblogger().getUserManager();
            User user = authenticateUser(principal, userManager);

            // Try auto-provisioning if LDAP enabled and user not found
            if (user == null && WebloggerConfig.getBooleanProperty("users.ldap.autoProvision.enabled")) {
                user = attemptAutoProvision(request, principal, userManager);
            }

            // Set authenticated user if found and enabled
            if (user != null && user.getEnabled()) {
                setAuthenticatedUser(user);
            }
        } catch (WebloggerException e) {
            log.error("Error authenticating user", e);
        }
    }

    /**
     * Attempts to authenticate user via username or OpenID URL
     */
    private User authenticateUser(Principal principal, UserManager userManager) throws WebloggerException {
        // Try regular username first
        User user = userManager.getUserByUserName(principal.getName());

        // If not found, try OpenID URL
        if (user == null && principal.getName() != null && principal.getName().startsWith("http://")) {
            String openidUrl = principal.getName();
            if (openidUrl.endsWith("/")) {
                openidUrl = openidUrl.substring(0, openidUrl.length() - 1);
            }
            user = userManager.getUserByOpenIdUrl(openidUrl);
        }
        return user;
    }

    /**
     * Attempts to auto-provision user via LDAP if enabled
     */
    private User attemptAutoProvision(HttpServletRequest request, Principal principal,
            UserManager userManager) throws WebloggerException {
        AutoProvision provisioner = RollerContext.getAutoProvision();
        if (provisioner != null && provisioner.execute(request)) {
            return userManager.getUserByUserName(principal.getName());
        }
        return null;
    }
    /**
     * Authenticated user associated with this session.
     */
    public User getAuthenticatedUser() {
        
        User authenticUser = null;
        if (userName != null) {
            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                authenticUser = mgr.getUserByUserName(userName);
            } catch (WebloggerException ex) {
                log.warn("Error looking up authenticated user "+userName, ex);
            }
        }
        
        return authenticUser;
    }

    /**
     * Authenticated user associated with this session.
     */
    public void setAuthenticatedUser(User authenticatedUser) {
        this.userName = authenticatedUser.getUserName();
        sessionManager.register(authenticatedUser.getUserName(), this);
    }
}
