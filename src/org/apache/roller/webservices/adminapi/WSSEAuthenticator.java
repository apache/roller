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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.UserData;
import org.apache.roller.util.WSSEUtilities;

/**
 * This class implements HTTP basic authentication for roller.
 *
 * @author jtb
 */
class WSSEAuthenticator extends Authenticator {
    /** Creates a new instance of HttpBasicAuthenticator */
    public WSSEAuthenticator(HttpServletRequest req) {
        super(req);
    }
    
    public void authenticate() throws HandlerException {
        setUserName(null);
        String wsseHeader = getRequest().getHeader("X-WSSE");
        if (wsseHeader == null) {
            throw new UnauthorizedException("ERROR: WSSE header was not set");
        };
        
        String userName = null;
        String created = null;
        String nonce = null;
        String passwordDigest = null;
        String[] tokens = wsseHeader.split(",");
        
        for (int i = 0; i < tokens.length; i++) {
            int index = tokens[i].indexOf('=');
            if (index != -1) {
                String key = tokens[i].substring(0, index).trim();
                String value = tokens[i].substring(index + 1).trim();
                value = value.replaceAll("\"", "");
                if (key.startsWith("UsernameToken")) {
                    userName = value;
                } else if (key.equalsIgnoreCase("nonce")) {
                    nonce = value;
                } else if (key.equalsIgnoreCase("passworddigest")) {
                    passwordDigest = value;
                } else if (key.equalsIgnoreCase("created")) {
                    created = value;
                }
            }
        }
        
        try {
            UserData user = getRoller().getUserManager().getUser(userName);
            if (user == null) {
                throw new UnauthorizedException("ERROR: User does not exist: " + userName);
            }
            String digest = WSSEUtilities.generateDigest(WSSEUtilities.base64Decode(nonce), created.getBytes("UTF-8"), user.getPassword().getBytes("UTF-8"));
            if (digest.equals(passwordDigest)) {
                setUserName(userName);
            } else {
                throw new UnauthorizedException("ERROR: User is not authorized to use the AAPP endpoint: " + userName);
            }
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get roller user: " + userName, re);
        } catch (IOException ioe) {
            throw new InternalException("ERROR: Could not get roller user: " + userName, ioe);
        }
        
        // make sure the user has the admin role
        verifyUser();
    }
}
