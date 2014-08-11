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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Represents a permission that applies globally to the entire web application.
 */
public class GlobalPermission extends RollerPermission {
    protected String actions;

    /** Allowed to login and edit profile */
    public static final String LOGIN  = "login";
    
    /** Allowed to login and do weblogging */
    public static final String WEBLOG = "weblog";

    /** Allowed to login and do everything, including site-wide admin */
    public static final String ADMIN  = "admin";
    
    /**
     * Create global permission for one specific user initialized with the 
     * actions that are implied by the user's roles.
     * @param user User of permission.
     * @throws org.apache.roller.weblogger.WebloggerException
     */
    public GlobalPermission(User user) throws WebloggerException {
        super("GlobalPermission user: " + user.getUserName());
        
        // loop through user's roles, adding actions implied by each
        List<String> roles = WebloggerFactory.getWeblogger().getUserManager().getRoles(user);
        List<String> actionsList = new ArrayList<String>();        
        for (String role : roles) {
            String impliedActions = WebloggerConfig.getProperty("role.action." + role);
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
        
    /** 
     * Create global permission with the actions specified by array.
     * @param actions actions to add to permission
     * @throws org.apache.roller.weblogger.WebloggerException
     */
    public GlobalPermission(List<String> actions) throws WebloggerException {
        super("GlobalPermission user: N/A");
        setActionsAsList(actions);
    }
        
    /** 
     * Create global permission for one specific user initialized with the 
     * actions specified by array.
     * @param user User of permission.
     * @throws org.apache.roller.weblogger.WebloggerException
     */
    public GlobalPermission(User user, List<String> actions) throws WebloggerException {
        super("GlobalPermission user: " + user.getUserName());
        setActionsAsList(actions);
    }
        
    public boolean implies(Permission perm) {
        if (getActionsAsList().isEmpty()) {
            // new, unsaved user.
            return false;
        }
        if (perm instanceof WeblogPermission) {
            if (hasAction(ADMIN)) {
                // admin implies all other permissions
                return true;                
            } 
        } else if (perm instanceof RollerPermission) {
            RollerPermission rperm = (RollerPermission)perm;            
            if (hasAction(ADMIN)) {
                // admin implies all other permissions
                return true;
                
            } else if (hasAction(WEBLOG)) {
                // Best we've got is WEBLOG, so make sure perm doesn't specify ADMIN
                for (String action : rperm.getActionsAsList()) {
                    if (action.equals(ADMIN)) {
                        return false;
                    }
                }
                
            } else if (hasAction(LOGIN)) {
                // Best we've got is LOGIN, so make sure perm doesn't specify anything else
                for (String action : rperm.getActionsAsList()) {
                    if (action.equals(WEBLOG)) {
                        return false;
                    }
                    if (action.equals(ADMIN)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean actionImplies(String action1, String action2) {
        return action1.equals(ADMIN) || (action1.equals(WEBLOG) && action2.equals(LOGIN));
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GlobalPermission: ");
        for (String action : getActionsAsList()) { 
            sb.append(" ").append(action).append(" ");
        }
        return sb.toString();
    }

    @Override
    public void setActions(String actions) {
        this.actions = actions;
    }

    @Override
    public String getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof GlobalPermission)) {
            return false;
        }
        GlobalPermission o = (GlobalPermission) other;
        return new EqualsBuilder()
                .append(getActions(), o.getActions())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getActions())
                .toHashCode();
    }

}
