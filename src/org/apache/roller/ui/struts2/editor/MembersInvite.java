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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.User;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.MailUtil;


/**
 * Allows website admin to invite new members to website.
 *
 * TODO: handle 'disabled' result
 */
public class MembersInvite extends UIAction {
    
    private static Log log = LogFactory.getLog(MembersInvite.class);
    
    // user being invited
    private String userName = null;
    
    // permissions being given to user
    private String permissionsMask = null;
    
    
    public MembersInvite() {
        this.actionName = "invite";
        this.desiredMenu = "editor";
        this.pageTitle = "inviteMember.title";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    public String execute() {
        
        // if group blogging is disabled then you can't change permissions
        if (!RollerConfig.getBooleanProperty("groupblogging.enabled")) {
            // TODO: i18n
            addError("invitations disabled");
            return SUCCESS;
        }
        
        log.debug("Showing weblog inivitation form");
        
        return INPUT;
    }
    
    
    /**
     * Save the new invitation and notify the user.
     */
    public String save() {
        
        // if group blogging is disabled then you can't change permissions
        if (!RollerConfig.getBooleanProperty("groupblogging.enabled")) {
            // TODO: i18n
            addError("invitations disabled");
            return SUCCESS;
        }
        
        log.debug("Attempting to process weblog invitation");
        
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        
        // user being invited
        User user = null;
        try {
            user = umgr.getUserByUserName(getUserName());
            if (user == null) {
                addError("inviteMember.error.userNotFound");
            }
        } catch(RollerException ex) {
            log.error("Error looking up user by id - "+getUserName(), ex);
            // TODO: i18n
            addError("Error looking up invitee");
        }
        
        // if we already have an error then bail now
        if(hasActionErrors()) {
            return INPUT;
        }
        
        // check for existing permissions or invitation
        try {
            WeblogPermission perms = umgr.getPermissions(getActionWeblog(), user);
            
            if (perms != null && perms.isPending()) {
                addError("inviteMember.error.userAlreadyInvited");
            } else if (perms != null) {
                addError("inviteMember.error.userAlreadyMember");
            }
            
        } catch (RollerException ex) {
            log.error("Error looking up permissions for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error checking existing permissions");
        }
        
        // if no errors then send the invitation
        if(!hasActionErrors()) try {
            
            umgr.inviteUser(getActionWeblog(), user, Short.parseShort(getPermissionsMask()));
            RollerFactory.getRoller().flush();
            
            addMessage("inviteMember.userInvited");
            
            try {
                MailUtil.sendWeblogInvitation(getActionWeblog(), user);
            } catch (RollerException e) {
                // TODO: this should be an error except that struts2 misbehaves
                // when we chain this action to the next one thinking that an error
                // means that validation broke during the chain
                addMessage("error.untranslated", e.getMessage());
            }
            
            log.debug("Invitation successfully recorded");
            
            return SUCCESS;
            
        } catch (Exception ex) {
            log.error("Error creating user invitation", ex);
            // TODO: i18n
            addError("Error creating user invitation");
        }
        
        log.debug("Invitation had errors, giving user another chance");
        
        return INPUT;
    }
    

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userId) {
        this.userName = userId;
    }

    public String getPermissionsMask() {
        return permissionsMask;
    }

    public void setPermissionsMask(String permission) {
        this.permissionsMask = permission;
    }
    
}
