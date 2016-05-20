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

import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.business.MailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows weblog owner to invite other members to edit the website.
 */
public class MembersInvite extends UIAction {

    private static Logger log = LoggerFactory.getLogger(MembersInvite.class);

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    // user being invited
    private String userId = null;
    
    // permissions being given to user
    private String permissionString = null;

    public MembersInvite() {
        this.actionName = "invite";
        this.desiredMenu = "editor";
        this.pageTitle = "inviteMember.title";
    }

    @Override
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public String execute() {
        // if group blogging is disabled then you can't change permissions
        if (!WebloggerStaticConfig.getBooleanProperty("groupblogging.enabled")) {
            addError("inviteMember.disabled");
            return SUCCESS;
        }
        return INPUT;
    }
    
    /**
     * Save the new invitation and notify the user.
     */
    public String save() {
        
        // if group blogging is disabled then you can't change permissions
        if (!WebloggerStaticConfig.getBooleanProperty("groupblogging.enabled")) {
            addError("inviteMember.disabled");
            return SUCCESS;
        }
        
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
                WebloggerFactory.flush();

                addMessage("inviteMember.userInvited");

                if (mailManager.isMailConfigured()) {
                    mailManager.sendWeblogInvitation(getActionWeblog(), user);
                }

                log.debug("Invitation successfully recorded");

                return SUCCESS;

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
