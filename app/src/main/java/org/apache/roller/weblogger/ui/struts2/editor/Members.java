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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
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
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.ParameterAware;


/**
 * Allows weblog admin to list/modify member permissions.
 */
public class Members extends UIAction implements ParameterAware {
    
    private static Log log = LogFactory.getLog(Members.class);
    
    // raw parameters from request
    private Map parameters = Collections.EMPTY_MAP;

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public Members() {
        log.debug("Instantiating members action");
        
        this.actionName = "members";
        this.desiredMenu = "editor";
        this.pageTitle = "memberPermissions.title";
    }
    
    public String execute() {
        log.debug("Showing weblog members page");
        return LIST;
    }
    
    
    public String save() {
        
        log.debug("Attempting to process weblog permissions updates");
        int numAdmins = 0; // make sure at least one admin
        int removed = 0;
        int changed = 0;
        List<UserWeblogRole> permsList = new ArrayList<UserWeblogRole>();
        try {
            List<UserWeblogRole> permsFromDB = userManager.getWeblogRolesIncludingPending(getActionWeblog());

            // we have to copy the permissions list so that when we remove permissions
            // below we don't get ConcurrentModificationExceptions
            for (UserWeblogRole perm : permsFromDB) {
                permsList.add(perm);
            }

            /* Check to see at least one admin would remain defined as a result of the save.
             * Not normally a problem, as only a blog admin can access this page and admins can't
             * demote themselves. However, the blog server admin can always access this page and
             * remove everyone even if not a member of the blog, causing orphan blogs unless this
             * check is in place.
             *
             * Also checking here to make sure an Admin is not demoting or removing himself.
             */
            User user = getAuthenticatedUser();
            boolean error = false;
            for (UserWeblogRole perms : permsList) {
                String sval = getParameter("perm-" + perms.getUser().getId());
                if (sval != null) {
                    if (sval.equals(WeblogRole.OWNER.name()) && !perms.isPending()) {
                        numAdmins++;
                    }
                    if (perms.getUser().getUserName().equals(user.getUserName())) {
                        // can't modify self
                        if (!sval.equals(WeblogRole.OWNER.name())) {
                            error = true;
                            addError("memberPermissions.noSelfModifications");
                        }
                    }
                }
            }
            if (numAdmins == 0) {
                addError("memberPermissions.oneAdminRequired");
                error = true;
            }
            // one iteration for each line (user) in the members table
            for (UserWeblogRole perms : permsList) {

                String sval = getParameter("perm-" + perms.getUser().getId());
                if (sval != null) {
                    if (!error && !perms.getWeblogRole().name().equals(sval)) {
                        if ("-1".equals(sval)) {
                            userManager.revokeWeblogRole(
                                     perms.getUser(), perms.getWeblog());
                            removed++;
                        } else {
                            userManager.revokeWeblogRole(
                                    perms.getUser(), perms.getWeblog());
                            userManager.grantWeblogRole(
                                    perms.getUser(), perms.getWeblog(), WeblogRole.valueOf(sval));
                            changed++;
                        }
                    }
                }
            }
            
            if (removed > 0 || changed > 0) {
                log.debug("Weblog permissions updated, flushing changes");                
                WebloggerFactory.flush();
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

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.OWNER;
    }


    public List<UserWeblogRole> getWeblogRoles() {
        try {
            return userManager.getWeblogRolesIncludingPending(getActionWeblog());
        } catch (WebloggerException ex) {
            // serious problem, but not much we can do here
            log.error("ERROR getting weblog roles", ex);
        }
        return new ArrayList<UserWeblogRole>();
    }
}
