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

package org.apache.roller.ui.struts2.editor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.User;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.ParameterAware;


/**
 * Allows weblog admin to list/modify member permissions.
 *
 * TODO: fix bug in UserManager which doesn't remove permissions from the
 * website.permissions collection when a permission is deleted.
 */
public class Members extends UIAction implements ParameterAware {
    
    private static Log log = LogFactory.getLog(Members.class);
    
    // raw parameters from request
    private Map parameters = Collections.EMPTY_MAP;
    
    
    public Members() {
        log.debug("Instantiating members action");
        
        this.actionName = "members";
        this.desiredMenu = "editor";
        this.pageTitle = "memberPermissions.title";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    public String execute() {
        
        log.debug("Showing weblog members page");
        
        return LIST;
    }
    
    
    public String save() {
        
        log.debug("Attempting to processing weblog permissions updates");
        
        UserManager userMgr = RollerFactory.getRoller().getUserManager();
        
        List<WeblogPermission> permissions = getActionWeblog().getPermissions();
        
        int removed = 0;
        int changed = 0;
        try {
            for( WeblogPermission perms : permissions ) {
                
                String sval = getParameter("perm-" + perms.getId());
                if (sval != null) {
                    short val = Short.parseShort(sval);
                    User user = getAuthenticatedUser();
                    if (perms.getUser().getId().equals(user.getId()) && 
                            val < perms.getPermissionMask()) {
                        addError("memberPermissions.noSelfDemotions");
                    } else if (val != perms.getPermissionMask()) {
                        if (val == -1) {
                            userMgr.removePermissions(perms);
                            removed++;
                        } else {
                            perms.setPermissionMask(val);
                            userMgr.savePermissions(perms);
                            changed++;
                        }
                    }
                }
            }
            
            if (removed > 0 || changed > 0) {
                log.debug("Weblog permissions updated, flushing changes");
                
                RollerFactory.getRoller().flush();
            }
        } catch (Exception ex) {
            log.error("Error saving permissions on weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error saving permissions");
        }
        
        if (removed > 0) {
            addMessage("memberPermissions.membersRemoved", ""+removed);
        }
        if (changed > 0) {
            addMessage("memberPermissions.membersChanged", ""+changed);
        }
        
        return LIST;
    }
    
    
    // convenience for accessing a single parameter with a single value
    public String getParameter(String key) {
        if(key != null) {
            String[] value = (String[]) getParameters().get(key);
            if(value != null && value.length > 0) {
                return value[0];
            }
        }
        return null;
    }
    
    
    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }
}
