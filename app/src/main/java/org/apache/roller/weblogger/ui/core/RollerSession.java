/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.core;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.security.AutoProvision;
import org.apache.roller.weblogger.ui.core.security.RollerOidcUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;


/**
 * Roller session handles session startup and shutdown.
 */
public class RollerSession 
        implements HttpSessionListener, HttpSessionActivationListener, Serializable {
    
    private static final long serialVersionUID = 5890132909166913727L;

    // the id of the user represented by this session
    private String userName = null;

    // digest of the user's stored password hash when this session logged in
    private String passwordDigest = null;

    // digest of a password hash this session's own request is saving
    private volatile String savingPasswordDigest = null;
    
    private static final Log log;
    
    public static final String ROLLER_SESSION = "org.apache.roller.weblogger.rollersession";

    // user name and password digest of the login, kept beside the Roller
    // session so they outlive it when the container passivates the session
    private static final String LOGIN_PASSWORD = ROLLER_SESSION + ".login";

    // the longest chain of re-encodings a session is carried across, and how
    // many re-encodings are remembered
    private static final int MAX_REENCODINGS = 8;
    private static final int MAX_REENCODED = 1000;

    // digests of password hashes that a login stored as a new encoding of an
    // unchanged password, by user, each with the digest of the hash it
    // replaced. A session that saw the older hash stays logged in. Entries
    // that are dropped end those sessions instead.
    private static final Map<String, String> REENCODED = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_REENCODED;
                }
            });

    // request attribute that marks a request whose session was ended
    private static final String SESSION_ENDED = ROLLER_SESSION + ".ended";

    static{
        WebloggerConfig.init(); // must be called before calls to logging APIs
        log = LogFactory.getLog(RollerSession.class);
    }
   
    /**
     * Get RollerSession from request (and add user if not already present).
     */
    public static RollerSession getRollerSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        try {
            return getRollerSession(request, session);
        } catch (IllegalStateException e) {
            if (session == null || !isInvalidated(session)) {
                throw e;
            }
            // another request of the same browser ended the session while
            // this one was using it, so this one is logged out too
            return endSession(request, session, null);
        }
    }

    private static RollerSession getRollerSession(HttpServletRequest request, HttpSession session) {
        RollerSession rollerSession = null;
        if (session != null) {
            rollerSession = (RollerSession)session.getAttribute(ROLLER_SESSION);

            if (rollerSession == null) {
                // Create new session if none exists
                rollerSession = new RollerSession();
                session.setAttribute(ROLLER_SESSION, rollerSession);
            } else if (rollerSession.userName != null) {
                // A session that logged in ends once the user's password has
                // changed or the user was disabled or removed, so changing a
                // password also logs out the user's other browsers. This runs
                // before the cache check below, which rebuilds the session
                // from the login and would take on the new password.
                if (!rollerSession.isCurrent(session)) {
                    return endSession(request, session, rollerSession.userName);
                }
                // Check if session is still valid in cache
                RollerLoginSessionManager manager = RollerLoginSessionManager.getInstance();
                String username = rollerSession.userName;
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
                    User user = findUser(umgr, principal);

                    // try one time to auto-provision, only happens if user==null
                    // which means the installation uses LDAP (authentication.method=ldap)
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
                        // a login whose password changed since it was first
                        // seen ends here too, even with no Roller session left
                        if (!sameLoginPassword(session, user)) {
                            return endSession(request, session, user.getUserName());
                        }
                        rollerSession.setAuthenticatedUser(user);
                        rememberLoginPassword(session, rollerSession);
                    } else if (session.getAttribute(LOGIN_PASSWORD) != null) {
                        // the account this login was recorded for has been
                        // disabled or removed since
                        return endSession(request, session, principal.getName());
                    }
                    
                } catch (WebloggerException e) {
                    log.error("ERROR: getting user object",e);
                }
            }
        }
        
        return rollerSession;
    }

    /**
     * The enabled user a login belongs to, or null if there is none.
     */
    private static User findUser(UserManager umgr, Principal principal) throws WebloggerException {
        User user = umgr.getUserByUserName(principal.getName());

        // For OIDC-authenticated users, look up by OIDC subject
        if (user == null && principal instanceof OAuth2AuthenticationToken oauthToken) {
            Object oauthPrincipal = oauthToken.getPrincipal();
            if (oauthPrincipal instanceof OidcUser oidcUser) {
                String oidcSubject = RollerOidcUserService.toOidcSubject(oidcUser);
                user = umgr.getUserByOpenIdUrl(oidcSubject);
            }
        }
        return user;
    }

    /**
     * Records a login in its session as soon as Spring Security accepts it:
     * the user's password as it is now, so that a later password change ends
     * the session even if Roller's pages are not opened in between. A login in
     * a session that already had one replaces what that one left behind.
     */
    public static void loggedIn(HttpServletRequest request, Principal login) {
        try {
            HttpSession session = request.getSession(true);
            session.removeAttribute(ROLLER_SESSION);
            session.removeAttribute(LOGIN_PASSWORD);
            if (WebloggerFactory.isBootstrapped()) {
                UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
                User user = findUser(umgr, login);
                if (user != null) {
                    session.setAttribute(LOGIN_PASSWORD, user.getUserName() + "\n" + digest(user));
                }
            }
        } catch (WebloggerException e) {
            log.error("ERROR: getting user object", e);
        }
    }

    /**
     * Keeps the current session's other requests logged in while it saves a
     * new password for its own user. Call it before the new password is
     * saved, and refreshPassword after.
     */
    public static void savingPassword(HttpServletRequest request, User user) {
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session == null || user == null) {
            return;
        }
        try {
            Object attribute = session.getAttribute(ROLLER_SESSION);
            if (attribute instanceof RollerSession rollerSession
                    && user.getUserName().equals(rollerSession.userName)) {
                rollerSession.savingPasswordDigest = digest(user);
            }
        } catch (IllegalStateException e) {
            if (!isInvalidated(session)) {
                throw e;
            }
            // another request ended the session, there is no login to keep
        }
    }

    /**
     * Keeps the current session logged in after its own user changed their
     * password, or an administrator changed their own. Call it after the new
     * password is saved; the user's other sessions still end.
     */
    public static void refreshPassword(HttpServletRequest request, User user) {
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session == null || user == null) {
            return;
        }
        try {
            Object attribute = session.getAttribute(ROLLER_SESSION);
            if (attribute instanceof RollerSession rollerSession
                    && user.getUserName().equals(rollerSession.userName)) {
                rollerSession.setAuthenticatedUser(user);
                rollerSession.savingPasswordDigest = null;
                rememberLoginPassword(session, rollerSession);
            }
        } catch (IllegalStateException e) {
            if (!isInvalidated(session)) {
                throw e;
            }
            // another request ended the session, there is no login to keep
        }
    }

    /**
     * Whether the login in this session first saw the user's current password,
     * or the session has not recorded one for this user yet.
     */
    private static boolean sameLoginPassword(HttpSession session, User user) {
        Object recorded = session.getAttribute(LOGIN_PASSWORD);
        String prefix = user.getUserName() + "\n";
        if (!(recorded instanceof String login) || !login.startsWith(prefix)) {
            return true;
        }
        return samePassword(user, login.substring(prefix.length()));
    }

    /**
     * Records that a login stored a new encoding of the user's unchanged
     * password, so that the user's sessions which saw the old one stay
     * logged in. Call it before the new encoding is saved.
     */
    public static void passwordReencoded(String userName, String oldPassword, String newPassword) {
        REENCODED.put(userName + "\n" + digest(newPassword), digest(oldPassword));
    }

    /**
     * Whether the user's password is the one a session saw, given as
     * passwordDigest, or a re-encoding of it.
     */
    private static boolean samePassword(User user, String passwordDigest) {
        String current = digest(user);
        for (int i = 0; current != null && i <= MAX_REENCODINGS; i++) {
            if (current.equals(passwordDigest)) {
                return true;
            }
            current = REENCODED.get(user.getUserName() + "\n" + current);
        }
        return false;
    }

    private static void rememberLoginPassword(HttpSession session, RollerSession rollerSession) {
        session.setAttribute(LOGIN_PASSWORD, rollerSession.userName + "\n" + rollerSession.passwordDigest);
    }

    /**
     * Whether the user this session logged in as is still enabled and still
     * has the password the session logged in with. A failed lookup keeps the
     * session, so a database error does not log everyone out. A session that
     * saw the password before it was re-encoded, or is saving a new password
     * for its user, takes on the new one once it is stored.
     */
    private boolean isCurrent(HttpSession session) {
        User user;
        try {
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            // only finds enabled users
            user = mgr.getUserByUserName(userName);
        } catch (WebloggerException ex) {
            log.warn("Error looking up authenticated user " + userName, ex);
            return true;
        }
        if (user == null) {
            return false;
        }
        if (digest(user).equals(savingPasswordDigest)) {
            // the password this session's own request saved
            savingPasswordDigest = null;
        } else if (!samePassword(user, passwordDigest)) {
            return false;
        }
        if (!digest(user).equals(passwordDigest)) {
            passwordDigest = digest(user);
            rememberLoginPassword(session, this);
        }
        return true;
    }

    private static boolean isInvalidated(HttpSession session) {
        try {
            session.getCreationTime();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    /**
     * Ends a session whose login no longer holds: invalidates it, clears the
     * login from this request, and gives the rest of the request a new
     * session with no user, as any visitor who is not logged in gets. The
     * user name is null when another request already ended the session.
     */
    private static RollerSession endSession(HttpServletRequest request, HttpSession session, String userName) {
        if (userName != null) {
            log.info("Ending a session of user " + userName
                    + ", whose password changed or whose account was disabled or removed");
        } else {
            log.debug("Logging out a request whose session another request ended");
        }
        // The session's other requests share this security context, so clear
        // the login from it before the session ends; otherwise one of them
        // could carry the login into a new session.
        SecurityContextHolder.getContext().setAuthentication(null);
        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
            // already invalidated
        }
        SecurityContextHolder.clearContext();
        request.setAttribute(SESSION_ENDED, Boolean.TRUE);
        RollerSession rollerSession = new RollerSession();
        try {
            request.getSession(true).setAttribute(ROLLER_SESSION, rollerSession);
        } catch (IllegalStateException e) {
            // response already committed, the next request gets a new session
            log.debug("Could not start a new session", e);
        }
        return rollerSession;
    }

    /**
     * Whether this request's session was ended because its login no longer
     * holds, so the request now has no user.
     */
    public static boolean isSessionEnded(HttpServletRequest request) {
        return request.getAttribute(SESSION_ENDED) != null;
    }

    /** Digest of the stored password hash, so the session does not hold it. */
    private static String digest(User user) {
        return digest(user.getPassword());
    }

    private static String digest(String storedPassword) {
        String password = storedPassword != null ? storedPassword : "";
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java spec", e);
        }
    }

    /** Create session's Roller instance */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        RollerSession rollerSession = new RollerSession();
        se.getSession().setAttribute(ROLLER_SESSION, rollerSession);
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        clearSession(se);
    }

    /**
     * Purge session before passivation. Because Roller currently does not
     * support session recovery, failover, migration, or whatever you want
     * to call it when sessions are saved and then restored at some later
     * point in time.
     */
    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        clearSession(se);
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
        this.passwordDigest = digest(authenticatedUser);
        RollerLoginSessionManager sessionManager = RollerLoginSessionManager.getInstance();
        sessionManager.register(authenticatedUser.getUserName(), this);
    }

    private void clearSession(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        try {
            session.removeAttribute(ROLLER_SESSION);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                // ignore purge exceptions
                log.debug("EXCEPTION PURGING session attributes",e);
            }
        }
    }
}
