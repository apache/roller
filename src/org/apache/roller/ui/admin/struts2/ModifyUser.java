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

package org.apache.roller.ui.admin.struts2;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.util.struts2.UIAction;


/**
 * Action that allows an admin to modify a users profile.
 */
public class ModifyUser extends UIAction {
    
    private static Log log = LogFactory.getLog(ModifyUser.class);
    
    // user we are modifying
    private UserData user = new UserData();
    
    // a bean to store our form data
    private CreateUserBean bean = new CreateUserBean();
    
    private String userName = null;
    
    
    public ModifyUser() {
        this.actionName = "modifyUser";
        this.desiredMenu = "admin";
        this.pageTitle = "userAdmin.title.editUser";
    }
    
    
    // admin role required
    public String requiredUserRole() {
        return "admin";
    }
    
    // no weblog required
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    // prepare for action by loading user object we are modifying
    public void myPrepare() {
        
        // load the user object we are modifying
        if(getUserName() != null) {
            try {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                setUser(mgr.getUserByUserName(getUserName()));
            } catch(Exception e) {
                log.error("Error looking up user - "+getUserName(), e);
            }
        } else if(getBean().getId() != null) {
            try {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                setUser(mgr.getUser(getBean().getId()));
            } catch(Exception e) {
                log.error("Error looking up user - "+getBean().getId(), e);
            }
        }
    }
    
    
    /**
     * Show admin user edit page.
     */
    public String execute() {
        
        if(getUser().getId() != null) {
            // populate form data from user profile data
            getBean().copyFrom(getUser(), getLocale());
        } else {
            // TODO: i18n
            addError("No user specified");
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
                    getUser().resetPassword(RollerFactory.getRoller(),
                            getBean().getPassword(),
                            getBean().getPassword());
                } catch (RollerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }
            
            try {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                
                // grant/revoke admin role if needed
                if(getUser().hasRole("admin") && !getBean().isAdministrator()) {
                    // revoke role
                    mgr.revokeRole("admin", getUser());
                } else if(!getUser().hasRole("admin") && getBean().isAdministrator()) {
                    // grant role
                    getUser().grantRole("admin");
                }
            
                // save the updated profile
                mgr.saveUser(getUser());
                RollerFactory.getRoller().flush();
                
                // TODO: i18n
                addMessage("user updated.");
                
                return INPUT;
                
            } catch (RollerException ex) {
                log.error("ERROR in action", ex);
                // TODO: i18n
                addError("unexpected error doing profile save");
            }
            
        }
        
        return INPUT;
    }
    
    
    public String cancel() {
        return "cancel";
    }
    
    
    // TODO: replace with struts2 validation
    private void myValidate() {
        
        if(getUser().getId() == null) {
            // TODO: i18n
            addError("user not found");
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

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
}
