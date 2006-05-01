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
package org.apache.roller.presentation.atomadminapi;

import javax.servlet.http.HttpServletRequest;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
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
    protected void verifyUser() throws HandlerException {
        try {
            UserData user = getRoller().getUserManager().getUserByUsername(getUserName());
            if (user != null && user.hasRole("admin") && user.getEnabled().booleanValue()) {
                // success! no exception
            } else {
                throw new UnauthorizedException("ERROR: User must have the admin role to use the AAPP endpoint: " + getUserName());
            }
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not verify user: " + getUserName(), re);
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
}
