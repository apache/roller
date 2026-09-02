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
/*
 * Created on Apr 11, 2003
 */
package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;

/**
 * Base API handler does user validation, provides exception types, etc.
 * @author David M Johnson
 */
public class BaseAPIHandler implements Serializable {
    static final long serialVersionUID = -698186274794937582L;
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(BaseAPIHandler.class);
    
    public static final int AUTHORIZATION_EXCEPTION = 0001;
    public static final String AUTHORIZATION_EXCEPTION_MSG =
            "Invalid Username and/or Password";
    
    public static final int UNKNOWN_EXCEPTION = 1000;
    public static final String UNKNOWN_EXCEPTION_MSG =
            "An error occured processing your request";
    
    public static final int UNSUPPORTED_EXCEPTION = 1001;
    public static final String UNSUPPORTED_EXCEPTION_MSG =
            "Unsupported method - Roller does not support this method";
    
    public static final int USER_DISABLED = 1002;
    public static final String USER_DISABLED_MSG =
            "User is disabled";
    
    public static final int WEBLOG_NOT_FOUND = 1003;
    public static final String WEBLOG_NOT_FOUND_MSG =
            "Weblog is not found or is disabled";
    
    public static final int WEBLOG_DISABLED = 1004;
    public static final String WEBLOG_DISABLED_MSG =
            "Weblog is not found or is disabled";
    
    public static final int BLOGGERAPI_DISABLED = 1005;
    public static final String BLOGGERAPI_DISABLED_MSG =
            "Weblog does not exist or XML-RPC disabled in web";
    
    public static final int BLOGGERAPI_INCOMPLETE_POST = 1006;
    public static final String BLOGGERAPI_INCOMPLETE_POST_MSG =
            "Incomplete weblog entry";
    
    public static final int INVALID_POSTID = 2000;
    public static final String INVALID_POSTID_MSG =
            "The entry postid you submitted is invalid";
    
    //public static final int NOBLOGS_EXCEPTION = 3000;
    //public static final String NOBLOGS_EXCEPTION_MSG =
    //"There are no categories defined for your user";
    
    public static final int UPLOAD_DENIED_EXCEPTION = 4000;
    public static final String UPLOAD_DENIED_EXCEPTION_MSG =
            "Upload denied";
    
    //------------------------------------------------------------------------
    public BaseAPIHandler() {
    }
    
    //------------------------------------------------------------------------
    //public void prep( HttpServletRequest req )
    //{
    //mRoller = RollerContext.getWeblogger(req);
    //mContextUrl = RollerContext.getRollerContext(req).getAbsoluteContextUrl(req);
    //
    
    //------------------------------------------------------------------------
    /**
     * Returns a weblog only when the authenticated user has the requested
     * permission and XML-RPC access is enabled for that weblog.
     */
    protected Weblog validate(String blogid, String username, String password,
            String requiredAction) throws Exception {
        User user = validateUser(username, password);
        return validateWeblog(blogid, user, requiredAction);
    }

    /**
     * Validate a weblog for an already authenticated user.
     */
    protected Weblog validateWeblog(String blogid, User user,
            String requiredAction) throws Exception {
        try {
            WeblogManager weblogMgr = WebloggerFactory.getWeblogger()
                    .getWeblogManager();
            Weblog website = weblogMgr.getWeblogByHandle(blogid);

            // Use one response for missing, unavailable, and inaccessible weblogs.
            if (!isWeblogAvailable(website)
                    || !website.hasUserPermission(user, requiredAction)) {
                throw new XmlRpcNotAuthorizedException(WEBLOG_DISABLED_MSG);
            }
            return website;
        } catch (XmlRpcNotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            mLogger.error("ERROR internal error validating weblog", e);
            throw new XmlRpcNotAuthorizedException(WEBLOG_DISABLED_MSG);
        }
    }
    
    //------------------------------------------------------------------------
    /**
     * Returns the authenticated user if username/password are valid and the
     * user is not disabled.
     * @param username Username sent in request
     * @param password Password sent in request
     */
    protected User validateUser(String username, String password)
            throws Exception {
        User user = null;
        boolean authenticated = false;
        try {
            UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
            user = userMgr.getUserByUserName(username);
            if (user != null && RollerContext.getPasswordEncoder() != null) {
                authenticated = RollerContext.getPasswordEncoder().matches(
                        password, user.getPassword());
            }
        } catch (Exception e) {
            mLogger.error("ERROR internal error validating user", e);
        }

        if (!authenticated) {
            throw new XmlRpcNotAuthorizedException(AUTHORIZATION_EXCEPTION_MSG);
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new XmlRpcNotAuthorizedException(USER_DISABLED_MSG);
        }

        if (!WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc")) {
            throw new XmlRpcNotAuthorizedException(BLOGGERAPI_DISABLED_MSG);
        }

        return user;
    }

    /**
     * Returns an entry only when it belongs to an available XML-RPC weblog and
     * the user may edit it. An optional additional weblog action can be
     * required for transitions such as publishing.
     */
    protected WeblogEntry validateEntry(String postid, User user,
            String additionalAction) throws Exception {
        WeblogEntry entry = getEntryForWrite(postid, user);
        if (entry == null) {
            throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
        }
        if (additionalAction != null) {
            try {
                if (!entry.getWebsite().hasUserPermission(user, additionalAction)) {
                    throw new XmlRpcNotAuthorizedException(
                            AUTHORIZATION_EXCEPTION_MSG);
                }
            } catch (XmlRpcNotAuthorizedException e) {
                throw e;
            } catch (Exception e) {
                mLogger.error("ERROR internal error validating entry action", e);
                throw new XmlRpcNotAuthorizedException(
                        AUTHORIZATION_EXCEPTION_MSG);
            }
        }
        return entry;
    }

    /**
     * Nullable form used by Blogger.deletePost(), whose public contract
     * returns false when the entry is unavailable.
     */
    protected WeblogEntry getEntryForWrite(String postid, User user)
            throws Exception {
        try {
            WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager();
            WeblogEntry entry = entryMgr.getWeblogEntry(postid);
            if (entry == null || !isWeblogAvailable(entry.getWebsite())
                    || !entry.getWebsite().hasUserPermission(
                            user, WeblogPermission.EDIT_DRAFT)
                    || !entry.hasWritePermissions(user)) {
                return null;
            }
            return entry;
        } catch (Exception e) {
            mLogger.error("ERROR internal error validating weblog entry", e);
            throw new XmlRpcNotAuthorizedException(AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    private boolean isWeblogAvailable(Weblog website) {
        if (website == null) {
            return false;
        }
        return Boolean.TRUE.equals(website.getVisible())
                && Boolean.TRUE.equals(website.getEnableBloggerApi());
    }
    
    //------------------------------------------------------------------------
    protected void flushPageCache(Weblog website) throws Exception {
        CacheManager.invalidate(website);
    }
}
