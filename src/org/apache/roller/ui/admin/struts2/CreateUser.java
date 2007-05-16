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

import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.struts2.Register;
import org.apache.roller.ui.core.util.struts2.UIAction;


/**
 * Action for admins to manually create new user accounts.
 */
public class CreateUser extends UIAction {
    
    private static Log log = LogFactory.getLog(CreateUser.class);
    
    // a bean to store our form data
    private CreateUserBean bean = new CreateUserBean();
    
    
    public CreateUser() {
        this.actionName = "createUser";
        this.desiredMenu = "admin";
        this.pageTitle = "userAdmin.title.createNewUser";
    }
    
    
    // admin role required
    public String requiredUserRole() {
        return "admin";
    }
    
    // no weblog required
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    /**
     * Show admin user creation form.
     */
    public String execute() {
        
        // defaults for locale and timezone
        getBean().setLocale(Locale.getDefault().toString());
        getBean().setTimeZone(TimeZone.getDefault().getID());
        
        return INPUT;
    }
    
    
    /**
     * Create a new user.
     */
    public String save() {
        
        // run some validation
        myValidate();
        
        if (!hasActionErrors()) try {
            
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            
            // copy form data into new user pojo
            UserData newUser = new UserData();
            getBean().copyTo(newUser, getLocale()); // doesn't copy password
            newUser.setDateCreated(new java.util.Date());
            
            // set username and password
            newUser.setUserName(getBean().getUserName());
            newUser.resetPassword(RollerFactory.getRoller(),
                    getBean().getPassword(), getBean().getPassword());
            
            // are we granting the user admin rights?
            if(((CreateUserBean)getBean()).isAdministrator()) {
                newUser.grantRole("admin");
            }
            
            // save new user
            mgr.addUser(newUser);
            RollerFactory.getRoller().flush();
            
            // TODO: i18n
            addMessage("New user created");
            
            return INPUT;
            
        } catch (RollerException e) {
            log.error("Error adding new user", e);
            // TODO: i18n
            addError("Error creating user");
        }
        
        return INPUT;
    }
    
    
    public String cancel() {
        return "cancel";
    }
    
    
    // TODO: replace with struts2 validation
    protected void myValidate() {
        
        String allowed = RollerConfig.getProperty("username.allowedChars");
        if(allowed == null || allowed.trim().length() == 0) {
            allowed = Register.DEFAULT_ALLOWED_CHARS;
        }
        String safe = CharSetUtils.keep(getBean().getUserName(), allowed);
        
        if (StringUtils.isEmpty(getBean().getUserName())) {
            addError("error.add.user.missingUserName");
        } else if (!safe.equals(getBean().getUserName()) ) {
            addError("error.add.user.badUserName");
        }
        
        if (StringUtils.isEmpty(getBean().getEmailAddress())) {
            addError("error.add.user.missingEmailAddress");
        }
        
        if (StringUtils.isEmpty(getBean().getPassword()) && 
                StringUtils.isEmpty(getBean().getPassword())) {
            addError("error.add.user.missingPassword");
        }
    }
    
    
    public CreateUserBean getBean() {
        return bean;
    }

    public void setBean(CreateUserBean bean) {
        this.bean = bean;
    }
    
}
