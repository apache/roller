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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
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
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }
    
    
    public String execute() {
        
        log.debug("Showing weblog members page");
        
        return LIST;
    }
    
    
    public String save() {
        
        log.debug("Attempting to process weblog permissions updates");
        
        int removed = 0;
        int changed = 0;
        List<WeblogPermission> permsList = new ArrayList<WeblogPermission>();
        try {
            UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();   
            List<WeblogPermission> permissions = userMgr.getWeblogPermissionsIncludingPending(getActionWeblog());

            // we have to copy the permissions list so that when we remove permissions
            // below we don't get ConcurrentModificationExceptions
            for (WeblogPermission perm : permissions) {
                permsList.add(perm);
            }
            // one iteration for each line (user) in the members table
            for (WeblogPermission perms : permsList) {
                
                String sval = getParameter("perm-" + perms.getUser().getId());
                if (sval != null) {
                    boolean error = false;
                    User user = getAuthenticatedUser();
                    if (perms.getUser().getUserName().equals(user.getUserName())) {
                        // can't modify self
                        if (!sval.equals(WeblogPermission.ADMIN)) {
                            error = true;
                            addError("memberPermissions.noSelfModifications");
                        }
                    }
                    if (!error && !perms.hasAction(sval)) {
                        if ("-1".equals(sval)) {
                             userMgr.revokeWeblogPermission(
                                    perms.getWeblog(), perms.getUser(), WeblogPermission.ALL_ACTIONS);
                            removed++;
                        } else {
                            userMgr.revokeWeblogPermission(
                                    perms.getWeblog(), perms.getUser(), WeblogPermission.ALL_ACTIONS);
                            userMgr.grantWeblogPermission(
                                    perms.getWeblog(), perms.getUser(), Utilities.stringToStringList(sval, ","));
                            changed++;
                        }
                    }
                }
            }
            
            if (removed > 0 || changed > 0) {
                log.debug("Weblog permissions updated, flushing changes");                
                WebloggerFactory.getWeblogger().flush();
            }
            
        } catch (Exception ex) {
            log.error("Error saving permissions on weblog - "+getActionWeblog().getHandle(), ex);
            addError("memberPermissions.saveError");
        }
        
        if (removed > 0) {
            addMessage("memberPermissions.membersRemoved", Integer.toString(removed));
        }
        if (changed > 0) {
            addMessage("memberPermissions.membersChanged", Integer.toString(changed));
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
    
    public List<WeblogPermission> getWeblogPermissions() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getWeblogPermissionsIncludingPending(getActionWeblog());
        } catch (WebloggerException ex) {
            // serious problem, but not much we can do here
            log.error("ERROR getting weblog permissions", ex);
        }
        return new ArrayList<WeblogPermission>();
    }
}
