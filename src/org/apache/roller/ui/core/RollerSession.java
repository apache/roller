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

package org.apache.roller.ui.core;

import java.io.Serializable;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.security.AutoProvision;


/**
 * Roller session handles session startup and shutdown.
 *
 * @web.listener
 */
public class RollerSession 
        implements HttpSessionListener, HttpSessionActivationListener, Serializable {
    
    static final long serialVersionUID = 5890132909166913727L;
    
    // the id of the user represented by this session
    private String userId = null;
    
    private static Log log = LogFactory.getLog(RollerSession.class);
    
    public static final String ROLLER_SESSION = "org.apache.roller.rollersession";
    public static final String ERROR_MESSAGE   = "rollererror_message";
    public static final String STATUS_MESSAGE  = "rollerstatus_message";
    
    
    /**
     * Get RollerSession from request (and add user if not already present).
     */
    public static RollerSession getRollerSession(HttpServletRequest request) {
        RollerSession rollerSession = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            rollerSession = (RollerSession)session.getAttribute(ROLLER_SESSION);
            if (rollerSession == null) {
                // HttpSession with no RollerSession?
                // Must be a session that was de-serialized from a previous run.
                rollerSession = new RollerSession();
                session.setAttribute(ROLLER_SESSION, rollerSession);
            }
            Principal principal = request.getUserPrincipal();
            if (rollerSession.getAuthenticatedUser() == null && principal != null) {
                try {
                    UserManager umgr = RollerFactory.getRoller().getUserManager();
                    UserData user = umgr.getUserByUserName(principal.getName());
                    
                    // try one time to auto-provision, only happens if user==null
                    // which means installation has SSO-enabled in security.xml
                    if(user == null && RollerConfig.getBooleanProperty("users.sso.autoProvision.enabled")) {
                        // provisioning enabled, get provisioner and execute
                        AutoProvision provisioner = RollerContext.getAutoProvision();
                        if(provisioner != null) {
                            boolean userProvisioned = provisioner.execute();
                            if(userProvisioned) {
                                // try lookup again real quick
                                user = umgr.getUserByUserName(principal.getName());
                            }
                        }
                    }
                    // only set authenticated user if user is enabled
                    if(user != null && user.getEnabled().booleanValue()) {
                        rollerSession.setAuthenticatedUser(user);
                    }
                } catch (RollerException e) {
                    log.error("ERROR: getting user object",e);
                }
            }
        }
        
        return rollerSession;
    }
    
    
    /** Create session's Roller instance */
    public void sessionCreated(HttpSessionEvent se) {
        RollerSession rollerSession = new RollerSession();
        se.getSession().setAttribute(ROLLER_SESSION, rollerSession);
    }
    
    
    public void sessionDestroyed(HttpSessionEvent se) {
        RollerContext rctx = RollerContext.getRollerContext();
        clearSession(se);
    }
    
    
    /** Init session as if it was new */
    public void sessionDidActivate(HttpSessionEvent se) {
    }
    
    
    /** 
     * Purge session before passivation. Because Roller currently does not
     * support session recovery, failover, migration, or whatever you want
     * to call it when sessions are saved and then restored at some later
     * point in time.
     */
    public void sessionWillPassivate(HttpSessionEvent se) {
        clearSession(se);
    }
    
    
    /**
     * Authenticated user associated with this session.
     */
    public UserData getAuthenticatedUser() {
        
        UserData authenticUser = null;
        if(userId != null) {
            try {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                authenticUser = mgr.getUser(userId);
            } catch (RollerException ex) {
                log.warn("Error looking up authenticated user "+userId, ex);
            }
        }
        
        return authenticUser;
    }
    
    
    /**
     * Authenticated user associated with this session.
     */
    public void setAuthenticatedUser(UserData authenticatedUser) {
        this.userId = authenticatedUser.getId();
    }
    
    
    /**
     * Does our authenticated user have the global admin role?
     */
    public boolean isGlobalAdminUser() throws RollerException {
        
        UserData user = getAuthenticatedUser();
        if (user != null && user.hasRole("admin")
        && user.getEnabled().booleanValue()) return true;
        return false;
    }
    
    
    /**
     * Is session's authenticated user authorized to work in current website?
     */
    public boolean isUserAuthorized(WebsiteData website)
            throws RollerException {
        
        UserData user = getAuthenticatedUser();
        if (user != null && user.getEnabled().booleanValue())
            return hasPermissions(website, PermissionsData.LIMITED);
        return false;
    }
    
    
    /**
     * Is session's authenticated user authorized to post in current weblog?
     */
    public boolean isUserAuthorizedToAuthor(WebsiteData website)
            throws RollerException {
        
        UserData user = getAuthenticatedUser();
        if (user != null && user.getEnabled().booleanValue())
            return hasPermissions(website, PermissionsData.AUTHOR);
        return false;
    }
    
    
    /**
     * Is session's authenticated user authorized to admin current weblog?
     */
    public boolean isUserAuthorizedToAdmin(WebsiteData website)
            throws RollerException {
        
        UserData user = getAuthenticatedUser();
        if (user != null && user.getEnabled().booleanValue())
            return hasPermissions(website, PermissionsData.ADMIN);
        return false;
    }
    
    
    private boolean hasPermissions(WebsiteData website, short mask) {
        
        UserData user = getAuthenticatedUser();
        if (website != null && user != null) {
            return website.hasUserPermissions(user, mask);
        }
        return false;
    }
    
    private void clearSession(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        try {
            session.removeAttribute(ROLLER_SESSION);
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                // ignore purge exceptions
                log.debug("EXCEPTION PURGING session attributes",e);
            }
        }
    }
    
}
