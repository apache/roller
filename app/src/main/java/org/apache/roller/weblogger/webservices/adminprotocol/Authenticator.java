/*
 * Copyright 2005 David M Johnson (For RSS and Atom In Action)
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
package org.apache.roller.weblogger.webservices.adminprotocol;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.util.Utilities;

/**
 * TODO
 *
 * @author jtb
 */
abstract class Authenticator {
    private HttpServletRequest request;
    private Weblogger             roller;
    private String             userName;
    
    /** Creates a new instance of HttpBasicAuthenticator */
    public Authenticator(HttpServletRequest req) {
        setRequest(req);
        setRoller(WebloggerFactory.getWeblogger());
    }
    
    public abstract void authenticate() throws HandlerException;
    
    /**
     * This method should be called by extensions of this class within their
     * implementation of authenticate().
     */
    protected void verifyUser(String userName, String password) throws HandlerException {
        try {
            User ud = getUserData(userName);
            String realpassword = ud.getPassword();
        
        boolean encrypted = Boolean.valueOf(WebloggerConfig.getProperty("passwds.encryption.enabled"));
        if (encrypted) {
            password = Utilities.encodePassword(password, WebloggerConfig.getProperty("passwds.encryption.algorithm"));
        }
        
            if (!userName.trim().equals(ud.getUserName())) {
                throw new UnauthorizedException("ERROR: User is not authorized: " + userName);
            }
            if (!password.trim().equals(realpassword)) {
                throw new UnauthorizedException("ERROR: User is not authorized: " + userName);
            }
            List<String> adminActions = new ArrayList<String>();
            adminActions.add("admin");
            GlobalPermission adminPerm = new GlobalPermission(ud, adminActions);
            if (!WebloggerFactory.getWeblogger().getUserManager().checkPermission(adminPerm, ud)) {
                throw new UnauthorizedException("ERROR: User must have the admin role to use the RAP endpoint: " + userName);
            }
            if (!ud.getEnabled()) {
                throw new UnauthorizedException("ERROR: User is disabled: " + userName);
            }
        } catch (WebloggerException ex) {
            throw new UnauthorizedException("ERROR: User must have the admin role to use the RAP endpoint: " + userName);
        }
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }
    
    protected void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public String getUserName() {
        return userName;
    }
    
    protected void setUserName(String userId) {
        this.userName = userId;
    }
    
    protected Weblogger getRoller() {
        return roller;
    }
    
    protected void setRoller(Weblogger roller) {
        this.roller = roller;
    }
    
    protected User getUserData(String name) throws NotFoundException, InternalException {
        try {
            UserManager mgr = getRoller().getUserManager();
            User ud = mgr.getUserByUserName(name, Boolean.TRUE);
            if (ud == null) {
                ud = mgr.getUserByUserName(name, Boolean.FALSE);
            }
            if (ud == null) {
                throw new NotFoundException("ERROR: Unknown user: " + name);
            }
            
            return ud;
        } catch (WebloggerException re) {
            throw new InternalException("ERROR: Could not get user: " + name, re);
        }
    }
    
}
