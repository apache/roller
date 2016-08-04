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

import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.ParameterAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows weblog admin to list/modify member permissions.
 */
public class Members extends UIAction implements ParameterAware {

    private static Logger log = LoggerFactory.getLogger(Members.class);
    
    // raw parameters from request
    private Map parameters = Collections.EMPTY_MAP;

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    // user being invited
    private String userId = null;

    // permissions being given to invited user
    private String permissionString = null;

    public Members() {
        log.debug("Instantiating members action");
        
        this.actionName = "members";
        this.desiredMenu = "editor";
        this.pageTitle = "memberPermissions.title";
    }
    
    public String execute() {
        return LIST;
    }
    
    public String save() {
        log.debug("Attempting to process weblog permissions updates");
        int numAdmins = 0; // make sure at least one admin
        int removed = 0;
        int changed = 0;
        List<UserWeblogRole> roleList = new ArrayList<UserWeblogRole>();
        try {
            List<UserWeblogRole> permsFromDB = userManager.getWeblogRolesIncludingPending(getActionWeblog());

            // we have to copy the permissions list so that when we remove permissions
            // below we don't get ConcurrentModificationExceptions
            for (UserWeblogRole perm : permsFromDB) {
                roleList.add(perm);
            }

            /* Check to see at least one admin would remain defined as a result of the save.
             * Not normally a problem, as only a blog admin can access this page and admins can't
             * demote themselves. However, the blog server admin can always access this page and
             * remove everyone even if not a member of the blog, causing orphan blogs unless this
             * check is in place.
             *
             * Also checking here to make sure an Admin is not demoting or removing himself.
             */
            User loggedInUser = getAuthenticatedUser();
            boolean error = false;
            for (UserWeblogRole role : roleList) {
                String sval = getParameter("role-" + role.getUser().getId());
                if (sval != null) {
                    if (sval.equals(WeblogRole.OWNER.name()) && !role.isPending()) {
                        numAdmins++;
                    }
                    if (role.getUser().getId().equals(loggedInUser.getId())) {
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
            for (UserWeblogRole role : roleList) {

                String sval = getParameter("role-" + role.getUser().getId());
                if (sval != null) {
                    if (!error && !role.getWeblogRole().name().equals(sval)) {
                        if ("-1".equals(sval)) {
                            userManager.revokeWeblogRole(role);
                            removed++;
                        } else {
                            userManager.revokeWeblogRole(role);
                            userManager.grantWeblogRole(
                                    role.getUser(), role.getWeblog(), WeblogRole.valueOf(sval));
                            changed++;
                        }
                    }
                }
            }
            
            if (removed > 0 || changed > 0) {
                log.debug("Weblog permissions updated, flushing changes");                
                persistenceStrategy.flush();
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
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.OWNER;
    }


    public List<UserWeblogRole> getWeblogRoles() {
        return userManager.getWeblogRolesIncludingPending(getActionWeblog());
    }

    /**
     * Save the new invitation and notify the user.
     */
    public String invite() {

        log.debug("Attempting to process weblog invitation");

        // user being invited
        User user = userManager.getUser(getUserId());
        if (user == null) {
            addError("inviteMember.error.userNotFound");
        }

        // if we already have an error then bail now
        if(hasActionErrors()) {
            return INPUT;
        }

        // check for existing permissions or invitation
        UserWeblogRole perm = userManager.getWeblogRoleIncludingPending(user, getActionWeblog());

        if (perm != null && perm.isPending()) {
            addError("inviteMember.error.userAlreadyInvited");
        } else if (perm != null) {
            addError("inviteMember.error.userAlreadyMember");
        }

        // if no errors then send the invitation
        if(!hasActionErrors()) {
            try {
                userManager.grantPendingWeblogRole(user, getActionWeblog(),
                        WeblogRole.valueOf(getPermissionString()));
                persistenceStrategy.flush();

                addMessage("inviteMember.userInvited");

                mailManager.sendWeblogInvitation(user, getActionWeblog());

                log.debug("Invitation successfully recorded");

                return LIST;

            } catch (Exception ex) {
                log.error("Error creating user invitation", ex);
                addError("Error creating user invitation - check TightBlog logs");
            }
        }

        log.debug("Invitation had errors, giving user another chance");

        return INPUT;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permission) {
        this.permissionString = permission;
    }
}
