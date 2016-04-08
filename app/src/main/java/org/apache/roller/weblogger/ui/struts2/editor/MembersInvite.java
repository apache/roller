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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.business.MailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


/**
 * Allows weblog owner to invite other members to edit the website.
 */
@RestController
public class MembersInvite extends UIAction {
    
    private static Log log = LogFactory.getLog(MembersInvite.class);

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    // user being invited
    private String userName = null;
    
    // permissions being given to user
    private String permissionString = null;

    // max length of users to display in select box
    private static final int MAX_LENGTH = 50;

    public MembersInvite() {
        this.actionName = "invite";
        this.desiredMenu = "editor";
        this.pageTitle = "inviteMember.title";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public String execute() {
        
        // if group blogging is disabled then you can't change permissions
        if (!WebloggerStaticConfig.getBooleanProperty("groupblogging.enabled")) {
            addError("inviteMember.disabled");
            return SUCCESS;
        }
        
        log.debug("Showing weblog invitation form");
        
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
        User user = null;
        try {
            user = userManager.getUserByScreenName(getUserName());
            if (user == null) {
                addError("inviteMember.error.userNotFound");
            }
        } catch(WebloggerException ex) {
            log.error("Error looking up user by id - "+getUserName(), ex);
            addError("Error looking up invitee");
        }
        
        // if we already have an error then bail now
        if(hasActionErrors()) {
            return INPUT;
        }
        
        // check for existing permissions or invitation
        try {
            UserWeblogRole perm = userManager.getWeblogRoleIncludingPending(user, getActionWeblog());

            if (perm != null && perm.isPending()) {
                addError("inviteMember.error.userAlreadyInvited");
            } else if (perm != null) {
                addError("inviteMember.error.userAlreadyMember");
            }
            
        } catch (WebloggerException ex) {
            log.error("Error looking up permissions for weblog - "+getActionWeblog().getHandle(), ex);
            addError("Error checking existing permissions");
        }
        
        // if no errors then send the invitation
        if(!hasActionErrors()) {
            try {
                userManager.grantPendingWeblogRole(user, getActionWeblog(),
                        WeblogRole.valueOf(getPermissionString()));
                WebloggerFactory.flush();

                addMessage("inviteMember.userInvited");

                if (mailManager.isMailConfigured()) {
                    try {
                        mailManager.sendWeblogInvitation(getActionWeblog(), user);
                    } catch (WebloggerException e) {
                        // TODO: this should be an error except that struts2 misbehaves
                        // when we chain this action to the next one thinking that an error
                        // means that validation broke during the chain
                        addMessage("error.untranslated", e.getMessage());
                    }
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
    
    /**
     * Cancel.
     * 
     * @return the string
     */
    public String cancel() {
        return CANCEL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userId) {
        this.userName = userId;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permission) {
        this.permissionString = permission;
    }

    @RequestMapping(path="/tb-ui/authoring/rest/userlist", method=RequestMethod.GET)
    public List<UserData> getUserList(Principal p, HttpServletRequest request,
                            HttpServletResponse response) throws ServletException {
        try {
            User authenticatedUser = userManager.getUserByUserName(p.getName());
            if (authenticatedUser.hasEffectiveGlobalRole(GlobalRole.BLOGGER)) {
                String startsWith = request.getParameter("startsWith");
                Boolean enabledOnly = null;
                int offset = 0;
                int length = MAX_LENGTH;
                if ("true".equals(request.getParameter("enabled"))) {
                    enabledOnly = Boolean.TRUE;
                }
                if ("false".equals(request.getParameter("enabled"))) {
                    enabledOnly = Boolean.FALSE;
                }
                try {
                    offset = Integer.parseInt(request.getParameter("offset"));
                } catch (Exception ignored) {
                }
                try {
                    length = Integer.parseInt(request.getParameter("length"));
                } catch (Exception ignored) {
                }

                try {
                    List<SafeUser> users = userManager.getUsers(startsWith, enabledOnly, offset, length);
                    List<UserData> userDataList = new ArrayList<>();
                    for (SafeUser user : users) {
                        UserData ud = new UserData();
                        ud.setScreenName(user.getScreenName());
                        if (authenticatedUser.isGlobalAdmin()) {
                            ud.setAdditionalInfo(user.getEmailAddress());
                        }
                        userDataList.add(ud);
                    }
                    return userDataList;
                } catch (WebloggerException e) {
                    throw new ServletException(e.getMessage());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private static class UserData {
        public UserData() {}

        private String screenName;
        private String additionalInfo;

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }

        public void setAdditionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
        }
    }
}
