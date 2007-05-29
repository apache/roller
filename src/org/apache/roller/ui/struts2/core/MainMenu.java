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

package org.apache.roller.ui.struts2.core;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.User;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.ui.struts2.util.UIAction;


/**
 * Allows user to view and pick from list of his/her websites.
 */
public class MainMenu extends UIAction {
    
    private static Log log = LogFactory.getLog(MainMenu.class);
    
    private String websiteId = null;
    private String inviteId = null;
    
    
    public MainMenu() {
        this.pageTitle = "yourWebsites.title";
    }
    
    
    // override default security, we do not require an action weblog
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    public String execute() {
        
        return SUCCESS;
    }
    
    
    public String accept() {
        
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            WeblogPermission perms = userMgr.getPermissions(getInviteId());
            if (perms != null) {        
                // TODO ROLLER_2.0: notify inviter that invitee has accepted invitation
                // TODO EXCEPTIONS: better exception handling
                perms.setPending(false);
                userMgr.savePermissions(perms);
                RollerFactory.getRoller().flush();

                addMessage("yourWebsites.accepted", perms.getWebsite().getHandle());
            } else {
                addError("yourWebsites.permNotFound");
            }
        } catch (RollerException ex) {
            log.error("Error handling invitation accept - "+getInviteId(), ex);
            // TODO: i18n
            addError("invite accept failed.");
        }
        
        return SUCCESS;
    }
    
    
    public String decline() {
        
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            WeblogPermission perms = userMgr.getPermissions(getInviteId());
            if (perms != null) {
                // TODO ROLLER_2.0: notify inviter that invitee has declined invitation
                // TODO EXCEPTIONS: better exception handling here
                userMgr.removePermissions(perms);
                RollerFactory.getRoller().flush();

                addMessage("yourWebsites.declined", perms.getWebsite().getHandle());
            } else {
                addError("yourWebsites.permNotFound");
            }
        } catch (RollerException ex) {
            log.error("Error handling invitation decline - "+getInviteId(), ex);
            // TODO: i18n
            addError("invite decline failed.");
        }
        
        return SUCCESS;
    }
    
    
    public String resign() {
        
        User user = getAuthenticatedUser();
        
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            Weblog website = mgr.getWebsite(getWebsiteId());
            
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            WeblogPermission perms = userMgr.getPermissions(website, user);
            
            if (perms != null) {
                // TODO ROLLER_2.0: notify website members that user has resigned
                // TODO EXCEPTIONS: better exception handling
                userMgr.removePermissions(perms);
                RollerFactory.getRoller().flush();
            }
            
            addMessage("yourWebsites.resigned", perms.getWebsite().getHandle());
        } catch (RollerException ex) {
            log.error("Error doing weblog resign - "+getWebsiteId(), ex);
            // TODO: i18n
            addError("resignation failed.");
        }
        
        return SUCCESS;
    }
    
    
    public List getExistingPermissions() {
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            return mgr.getAllPermissions(getAuthenticatedUser());
        } catch(Exception e) {
            return Collections.EMPTY_LIST;
        }
    }
    
    public List getPendingPermissions() {
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            return mgr.getPendingPermissions(getAuthenticatedUser());
        } catch(Exception e) {
            return Collections.EMPTY_LIST;
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
    
}
