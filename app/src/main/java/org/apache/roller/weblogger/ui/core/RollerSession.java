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

import java.io.Serializable;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.security.AutoProvision;


/**
 * Roller session handles session startup and shutdown.
 */
public class RollerSession 
        implements HttpSessionListener, HttpSessionActivationListener, Serializable {
    
    private static final long serialVersionUID = 5890132909166913727L;

    // the id of the user represented by this session
    private String userName = null;
    
    private static final Log log;
    
    public static final String ROLLER_SESSION = "org.apache.roller.weblogger.rollersession";

    static{
        WebloggerConfig.init(); // must be called before calls to logging APIs
        log = LogFactory.getLog(RollerSession.class);
    }
   
    /**
     * Get RollerSession from request (and add user if not already present).
     */
    public static RollerSession getRollerSession(HttpServletRequest request) {
        RollerSession rollerSession = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            rollerSession = (RollerSession)session.getAttribute(ROLLER_SESSION);

            if (rollerSession == null) {
                // Create new session if none exists
                rollerSession = new RollerSession();
                session.setAttribute(ROLLER_SESSION, rollerSession);
            } else if (rollerSession.getAuthenticatedUser() != null) {
                // Check if session is still valid in cache
                RollerSessionManager manager = RollerSessionManager.getInstance();
                String username = rollerSession.getAuthenticatedUser().getUserName();
                if (manager.get(username) == null) {
                    rollerSession = new RollerSession();
                    session.setAttribute(ROLLER_SESSION, rollerSession);
                }
            }
            Principal principal = request.getUserPrincipal();

            // If we've got a principal but no user object, then attempt to get
            // user object from user manager but *only* do this if we have been 
            // bootstrapped because under an SSO scenario we may have a 
            // principal even before we have been bootstrapped.
            if (rollerSession.getAuthenticatedUser() == null && principal != null && WebloggerFactory.isBootstrapped()) { 
                try {
                    
                    UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
                    User user = umgr.getUserByUserName(principal.getName());
                    
                    // check for OpenID username (in the form of a URL)
                    if (user == null && principal.getName() != null && principal.getName().startsWith("http://")) {
                        String openidurl = principal.getName();
                        if (openidurl.endsWith("/")) {
                            openidurl = openidurl.substring(0, openidurl.length() - 1);
                        }
                        user = umgr.getUserByOpenIdUrl(openidurl);
                    }
                    
                    // try one time to auto-provision, only happens if user==null
                    // which means installation has LDAP enabled in security.xml
                    if (user == null && WebloggerConfig.getBooleanProperty("users.ldap.autoProvision.enabled")) {
                        
                        // provisioning enabled, get provisioner and execute
                        AutoProvision provisioner = RollerContext.getAutoProvision();
                        if(provisioner != null) {
                            boolean userProvisioned = provisioner.execute(request);
                            if (userProvisioned) {
                                // try lookup again real quick
                                user = umgr.getUserByUserName(principal.getName());
                            }
                        }
                    }
                    // only set authenticated user if user is enabled
                    if (user != null && user.getEnabled()) {
                        rollerSession.setAuthenticatedUser(user);
                    }
                    
                } catch (WebloggerException e) {
                    log.error("ERROR: getting user object",e);
                }
            }
        }
        
        return rollerSession;
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
        RollerSessionManager sessionManager = RollerSessionManager.getInstance();
        sessionManager.register(authenticatedUser.getUserName(), this);
    }
}
