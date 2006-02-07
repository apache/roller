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
package org.roller.presentation.atomadminapi;

import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import com.sun.syndication.io.impl.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.model.Roller;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;

/**
 * This class implements HTTP basic authentication for roller.
 *
 * @author jtb
 */
class BasicAuthenticator extends Authenticator {
    private static Log logger = LogFactory.getFactory().getInstance(BasicAuthenticator.class);
 
    /** Creates a new instance of HttpBasicAuthenticator */
    public BasicAuthenticator(HttpServletRequest req) {
        super(req);
    }
       
    public boolean authenticate() {
        boolean valid = false;
        String id = null;
        String password = null;
        try {
            String authHeader = getRequest().getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();
                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = st.nextToken();
                        String userPass = new String(Base64.decode(credentials));
                        int p = userPass.indexOf(":");
                        if (p != -1) {
                            id = userPass.substring(0, p);
                            UserData user = getRoller().getUserManager().getUser(id);
                            String realpassword = user.getPassword();
                            password = userPass.substring(p+1);
                            if (    (id.trim().equals(user.getUserName()))
                            && (password.trim().equals(realpassword))) {
                                valid = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { //TODO: not good to be catching "Exception"
            logger.debug(e);
        }        
        if (valid) { 
            setUserId(id);
        } else {
            // clear out old value, if second call
            setUserId(null); 
        }
        return valid;
    }
}
