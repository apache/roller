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
class WSSEAuthenticator extends Authenticator {
    private static Log logger = LogFactory.getFactory().getInstance(WSSEAuthenticator.class);
 
    /** Creates a new instance of HttpBasicAuthenticator */
    public WSSEAuthenticator(HttpServletRequest req) {
        super(req);
    }
       
    public boolean authenticate() {
        String wsseHeader = getRequest().getHeader("X-WSSE");
        if (wsseHeader == null) return false;
        
        String id = null;
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
        String digest = null;
        try {
            UserData user = getRoller().getUserManager().getUser(userName);
            digest = WSSEUtilities.generateDigest(
                    WSSEUtilities.base64Decode(nonce),
                    created.getBytes("UTF-8"),
                    user.getPassword().getBytes("UTF-8"));
            if (digest.equals(passwordDigest)) {
                id = userName;
            }
        } catch (Exception e) {
            logger.error("ERROR in wsseAuthenticataion: " + e.getMessage(), e);
        }
        
        if (id != null) {
            setUserId(id);
            return true;
        } else {
            setUserId(null);
            return false;
        }
    }
}
