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
package org.apache.roller.webservices.adminprotocol;

import javax.servlet.http.HttpServletRequest;
import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.UserData;

/**
 * TODO
 *
 * @author jtb
 */
abstract class Authenticator {
    private HttpServletRequest request;
    private Roller             roller;
    private String             userName;
    
    /** Creates a new instance of HttpBasicAuthenticator */
    public Authenticator(HttpServletRequest req) {
        setRequest(req);
        setRoller(RollerFactory.getRoller());
    }
    
    public abstract void authenticate() throws HandlerException;
    
    /**
     * This method should be called by extensions of this class within their
     * implementation of authenticate().
     */
    protected void verifyUser(String userName, String password) throws HandlerException {
        UserData ud = getUserData(userName);
        String realpassword = ud.getPassword();

        if (!userName.trim().equals(ud.getUserName())) {
            throw new UnauthorizedException("ERROR: User is not authorized: " + userName);
        }
        if (!password.trim().equals(realpassword)) {
            throw new UnauthorizedException("ERROR: User is not authorized: " + userName);
        }
        
        if (!ud.hasRole("admin")) {
            throw new UnauthorizedException("ERROR: User must have the admin role to use the RAP endpoint: " + userName);
        }
        if (!ud.getEnabled().booleanValue()) {
            throw new UnauthorizedException("ERROR: User is disabled: " + userName);
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
    
    protected Roller getRoller() {
        return roller;
    }
    
    protected void setRoller(Roller roller) {
        this.roller = roller;
    }
    
    protected UserData getUserData(String name) throws NotFoundException, InternalException {
        try {
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUserByUserName(name, Boolean.TRUE);
            if (ud == null) {
                ud = mgr.getUserByUserName(name, Boolean.FALSE);
            }
            if (ud == null) {
                throw new NotFoundException("ERROR: Unknown user: " + name);
            }
            
            return ud;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get user: " + name, re);
        }
    }
    
}
