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
package org.apache.roller.webservices.adminapi;

import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import com.sun.syndication.io.impl.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.UserData;

/**
 * This class implements HTTP basic authentication for roller.
 *
 * @author jtb
 */
class BasicAuthenticator extends Authenticator {
    /** Creates a new instance of HttpBasicAuthenticator */
    public BasicAuthenticator(HttpServletRequest req) {
        super(req);
    }
    
    public void authenticate() throws HandlerException {
        setUserName(null);
        
        String userName = null;
        String password = null;
        String authHeader = getRequest().getHeader("Authorization");
        if (authHeader == null) {
            throw new UnauthorizedException("ERROR: Authorization header was not set");
        }
        
        try {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();
                if (basic.equalsIgnoreCase("Basic")) {
                    String credentials = st.nextToken();
                    String userPass = new String(Base64.decode(credentials));
                    int p = userPass.indexOf(":");
                    if (p != -1) {
                        userName = userPass.substring(0, p);
                        UserData user = getRoller().getUserManager().getUserByUserName(userName);
                        if (user == null) {
                            throw new UnauthorizedException("ERROR: User does not exist: " + userName);
                        }
                        String realpassword = user.getPassword();
                        password = userPass.substring(p+1);
                        if ((userName.trim().equals(user.getUserName())) && (password.trim().equals(realpassword))) {
                            setUserName(userName);
                        }
                    }
                }
            }
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not authorize user: " + userName, re);
        }
        if (getUserName() == null) {
            throw new UnauthorizedException("ERROR: User is not authorized to use the AAPP endpoint: " + userName);
        }
        
        // make sure the user has the admin role
        verifyUser();
    }
}
