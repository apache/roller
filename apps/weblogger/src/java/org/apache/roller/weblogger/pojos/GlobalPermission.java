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

package org.apache.roller.weblogger.pojos; 

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.util.Utilities;


/**
 * 
 */
public class GlobalPermission extends RollerPermission {
        
    public GlobalPermission(User user) throws WebloggerException {
        super("GlobalPermission user: " + user.getUserName());
        List<String> roles = WebloggerFactory.getWeblogger().getUserManager().getRoles(user);
        List<String> actionsList = new ArrayList<String>();
        
        // loop through user's roles, adding actions implied by each
        for (String role : roles) {
            String impliedActions = WebloggerRuntimeConfig.getProperty("role.action." + role);
            if (impliedActions != null) {
                List<String> toAdds = Utilities.stringToStringList(impliedActions, ",");
                for (String toAdd : toAdds) {
                    if (!actionsList.contains(toAdd)) {
                        actionsList.add(toAdd);
                    }
                }
            }
        }
        setActionsAsList(actionsList);
    }
        
    public boolean implies(Permission perm) {
        if (perm instanceof WeblogPermission) {
            if (hasAction("admin")) return true;
        }
        return false;
    }
    
    public boolean equals(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
