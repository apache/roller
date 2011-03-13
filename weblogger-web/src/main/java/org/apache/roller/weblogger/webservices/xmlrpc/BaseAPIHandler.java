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
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.Utilities;
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
     * Returns website, but only if user authenticates and is authorized to edit.
     * @param blogid   Blogid sent in request (used as website's hanldle)
     * @param username Username sent in request
     * @param password Password sent in requeset
     */
    protected Weblog validate(String blogid, String username, String password)
    throws Exception {
        boolean authenticated = false;
        boolean userEnabled = false;
        boolean weblogEnabled = false;
        boolean apiEnabled = false;
        boolean weblogFound = false;
        Weblog website = null;
        User user = null;
        try {
            UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
            WeblogManager weblogMgr = WebloggerFactory.getWeblogger().getWeblogManager();
            user = userMgr.getUserByUserName(username);
            userEnabled = user.getEnabled().booleanValue();
            
            website = weblogMgr.getWeblogByHandle(blogid);
            if (website != null) {
                weblogFound = true;
                weblogEnabled = website.getEnabled().booleanValue();
                apiEnabled = website.getEnableBloggerApi().booleanValue() 
                	&& WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc");
            }
            
            if (user != null) {
                // are passwords encrypted
                String encrypted =
                        WebloggerConfig.getProperty("passwds.encryption.enabled");
                //System.out.print("password was [" + password + "] ");
                if ("true".equalsIgnoreCase(encrypted)) {
                    password = Utilities.encodePassword(password,
                            WebloggerConfig.getProperty("passwds.encryption.algorithm"));
                }
                authenticated = password.equals(user.getPassword());
            }
        } catch (Exception e) {
            mLogger.error("ERROR internal error validating user", e);
        }
        
        if ( !authenticated ) {
            throw new XmlRpcNotAuthorizedException(AUTHORIZATION_EXCEPTION_MSG);
        }
        if ( !userEnabled ) {
            throw new XmlRpcNotAuthorizedException(USER_DISABLED_MSG);
        }
        if ( !weblogEnabled ) {
            throw new XmlRpcNotAuthorizedException(WEBLOG_DISABLED_MSG);
        }
        if ( !weblogFound ) {
            throw new XmlRpcException(WEBLOG_NOT_FOUND, WEBLOG_NOT_FOUND_MSG);
        }
        if ( !apiEnabled ) {
            throw new XmlRpcNotAuthorizedException(BLOGGERAPI_DISABLED_MSG);
        }
        return website;
    }
    
    //------------------------------------------------------------------------
    /**
     * Returns true if username/password are valid and user is not disabled.
     * @param username Username sent in request
     * @param password Password sent in requeset
     */
    protected boolean validateUser(String username, String password)
    throws Exception {
        boolean authenticated = false;
        boolean enabled = false;
        boolean apiEnabled = false;
        User user = null;
        try {
            
            UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
            user = userMgr.getUserByUserName(username);
            
            enabled = user.getEnabled().booleanValue();
            if (enabled) {
                // are passwords encrypted?
                String encrypted =
                        WebloggerConfig.getProperty("passwds.encryption.enabled");
                //System.out.print("password was [" + password + "] ");
                if ("true".equalsIgnoreCase(encrypted)) {
                    password = Utilities.encodePassword(password,
                            WebloggerConfig.getProperty("passwds.encryption.algorithm"));
                }
                //System.out.println("is now [" + password + "]");
                authenticated = user.getPassword().equals(password);
                
                apiEnabled = WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc");
            }
        } catch (Exception e) {
            mLogger.error("ERROR internal error validating user", e);
        }
        
        if ( !enabled ) {
            throw new XmlRpcNotAuthorizedException(USER_DISABLED_MSG);
        }
        
        if ( !authenticated ) {
            throw new XmlRpcNotAuthorizedException(AUTHORIZATION_EXCEPTION_MSG);
        }
        
        if ( !apiEnabled ) {
            throw new XmlRpcNotAuthorizedException(BLOGGERAPI_DISABLED_MSG);
        }        
        
        return authenticated;
    }
    
    //------------------------------------------------------------------------
    protected void flushPageCache(Weblog website) throws Exception {
        CacheManager.invalidate(website);
    }
}
