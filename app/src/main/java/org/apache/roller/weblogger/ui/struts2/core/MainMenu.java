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

package org.apache.roller.weblogger.ui.struts2.core;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Allows user to view and pick from list of his/her websites.
 */
public class MainMenu extends UIAction {
    
    private static Log log = LogFactory.getLog(MainMenu.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private String websiteId = null;
    private String inviteId = null;

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public MainMenu() {
        this.pageTitle = "yourWebsites.title";
    }

    
    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }
    
    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public String execute() {
        
        return SUCCESS;
    }
    
    
    public String accept() {
        
        try {
            Weblog weblog = weblogManager.getWeblog(getInviteId());
            userManager.acceptWeblogInvitation(getAuthenticatedUser(), weblog);
            WebloggerFactory.flush();

        } catch (WebloggerException ex) {
            log.error("Error handling invitation accept weblog id - "+getInviteId(), ex);
            addError("yourWebsites.permNotFound");
        }
        
        return SUCCESS;
    }
    
    
    public String decline() {
        
        try {
            Weblog weblog = weblogManager.getWeblog(getInviteId());
            String handle = weblog.getHandle();                       
            // TODO ROLLER_2.0: notify inviter that invitee has declined invitation
            // TODO EXCEPTIONS: better exception handling here
            userManager.declineWeblogInvitation(getAuthenticatedUser(), weblog);
            WebloggerFactory.flush();
            addMessage("yourWebsites.declined", handle);

        } catch (WebloggerException ex) {
            log.error("Error handling invitation decline weblog id - "+getInviteId(), ex);
            addError("yourWebsites.permNotFound");
        }
        
        return SUCCESS;
    }

    public List<UserWeblogRole> getExistingPermissions() {
        try {
            return userManager.getWeblogRoles(getAuthenticatedUser());
        } catch(Exception e) {
            return Collections.emptyList();
        }
    }
    
    public List<UserWeblogRole> getPendingPermissions() {
        try {
            return userManager.getPendingWeblogRoles(getAuthenticatedUser());
        } catch(Exception e) {
            return Collections.emptyList();
        }
    }
    
    public String getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(String websiteId) {
        this.websiteId = websiteId;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }

    public boolean isUserIsAdmin() {
        try {
            return userManager.isGlobalAdmin(getAuthenticatedUser());
        } catch (Exception e) {}
        return false;
    }
}
