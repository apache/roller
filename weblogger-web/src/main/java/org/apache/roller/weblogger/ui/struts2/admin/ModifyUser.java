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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Action that allows an admin to modify a users profile.
 */
public class ModifyUser extends UIAction {
    
    private static Log log = LogFactory.getLog(ModifyUser.class);

    private static final boolean isCMA = WebloggerConfig.getBooleanProperty("authentication.cma.enabled");
    
    // user we are modifying
    private User user = new User();
    
    // a bean to store our form data
    private CreateUserBean bean = new CreateUserBean();
    
    private String userName = null;
    
    
    public ModifyUser() {
        this.actionName = "modifyUser";
        this.desiredMenu = "admin";
        this.pageTitle = "userAdmin.title.editUser";
    }
    
    
    // admin role required
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }
    
    // no weblog required
    public boolean isWeblogRequired() { 
        return false;
    }
    
    
    // prepare for action by loading user object we are modifying
    public void myPrepare() {
        
        // load the user object we are modifying
        if (getUserName() != null) {
            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                
                // use enabled = 'null' to get both enabled and disabled users
                setUser(mgr.getUserByUserName(getUserName(), null));
                
            } catch(Exception e) {
                log.error("Error looking up user - "+getUserName(), e);
            }
        } else if (getBean().getId() != null) {
            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                setUser(mgr.getUserByUserName(getBean().getUserName(), null));
            } catch (Exception e) {
                log.error("Error looking up user - " + getBean().getId(), e);
            }
        }
    }
    
    
    /**
     * Show admin user edit page.
     */
    public String execute() {
        
        if (getUser() != null && getUser().getUserName() != null) {
            // populate form data from user profile data
            getBean().copyFrom(getUser(), getLocale());
        } else {
            addError("userAdmin.error.userNotFound");
            return ERROR;
        }

        return INPUT;
    }
    
    
    /**
     * Save modified user profile.
     */
    public String save() {
        
        // custom validation
        myValidate();
        
        if (!hasActionErrors()) {
            
            getBean().copyTo(getUser(), getLocale());
            
            // reset password if set
            if (!StringUtils.isEmpty(getBean().getPassword())) {
                try {
                    getUser().resetPassword(getBean().getPassword());
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }
            
            try {
                boolean hasAdmin = false;
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                GlobalPermission adminPerm = 
                    new GlobalPermission(Collections.singletonList(GlobalPermission.ADMIN));
                if (mgr.checkPermission(adminPerm, getUser())) {
                    hasAdmin = true;
                }  
                
                // grant/revoke admin role if needed
                if (hasAdmin && !getBean().isAdministrator()) {
                    
                    if (!isUserEditingSelf()) {
                        // revoke role
                        mgr.revokeRole("admin", getUser());
                    } else {
                        addError("userAdmin.cantChangeOwnRole");
                    }
                    
                } else if(!hasAdmin && getBean().isAdministrator()) {
                    
                    if (!isUserEditingSelf()) {
                        // grant role
                        mgr.grantRole("admin", getUser());
                    } else {
                        addError("userAdmin.cantChangeOwnRole"); 
                    }
                    
                }
            
                if (!isCMA) {
                    RollerContext.flushAuthenticationUserCache(getUser().getUserName());
                }

                // save the updated profile
                mgr.saveUser(getUser());
                WebloggerFactory.getWeblogger().flush();
                
                addMessage("userAdmin.userSaved");
                                
                return INPUT;
                
            } catch (WebloggerException ex) {
                log.error("ERROR in action", ex);
                // TODO: i18n
                addError("userAdmin.error.unexpectedError");
            }
            
        }
        
        return INPUT;
    }
    
    
    // TODO: replace with struts2 validation
    private void myValidate() {
        
        if(getUser().getUserName() == null) {
            addError("userAdmin.error.userNotFound");
        }
        if (StringUtils.isEmpty(getBean().getEmailAddress())) {
            addError("error.add.user.missingEmailAddress");
        }
    }
    
    
    public CreateUserBean getBean() {
        return bean;
    }

    public void setBean(CreateUserBean bean) {
        this.bean = bean;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public boolean isUserEditingSelf() {
        return getUser().equals(getAuthenticatedUser());
    }
    
}
