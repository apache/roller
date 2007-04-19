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

package org.apache.roller.ui.core.struts2;

import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.util.struts2.UIAction;


/**
 * Allows user to edit his/her profile.
 *
 * TODO: check on the impact of deleting that cookieLogin stuff
 */
public class ProfileForm extends UIAction {
    
    private static Log log = LogFactory.getLog(ProfileForm.class);
    
    private RegisterFormBean bean = new RegisterFormBean();
    
    
    public ProfileForm() {
        this.pageTitle = "yourProfile.title";
    }
    
    // override default security, we do not require an action weblog
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    public String execute() {
        
        UserData ud = getAuthenticatedUser();
        
        // load up the form from the users existing profile data
        getBean().copyFrom(ud, getLocale());
        getBean().setPasswordText(null);
        getBean().setPasswordConfirm(null);
        getBean().setLocale(ud.getLocale());
        getBean().setTimeZone(ud.getTimeZone());

        return INPUT;
    }
    
    
    public String cancel() {
        return "cancel";
    }
    
    
    public String save() {
        
        // custom validation
        myValidate();
        
        if (!hasActionErrors()) {
            // We ONLY modify the user currently logged in
            UserData existingUser = getAuthenticatedUser();
            
            // We want to be VERY selective about what data gets updated
            existingUser.setFullName(getBean().getFullName());
            existingUser.setEmailAddress(getBean().getEmailAddress());
            existingUser.setLocale(getBean().getLocale());
            existingUser.setTimeZone(getBean().getTimeZone());
            
            // If user set both password and passwordConfirm then reset password
            if (!StringUtils.isEmpty(getBean().getPasswordText()) && 
                    !StringUtils.isEmpty(getBean().getPasswordConfirm())) {
                try {
                    existingUser.resetPassword(RollerFactory.getRoller(),
                            getBean().getPasswordText(),
                            getBean().getPasswordConfirm());
                } catch (RollerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }
            
            try {
                // save the updated profile
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                mgr.saveUser(existingUser);
                RollerFactory.getRoller().flush();
                
                // TODO: i18n
                addMessage("profile updated.");
                
                return SUCCESS;
                
            } catch (RollerException ex) {
                log.error("ERROR in action", ex);
                // TODO: i18n
                addError("unexpected error doing profile save");
            }
            
        }
        
        return INPUT;
    }
    
    
    // TODO: replace with struts2 validation
    private void myValidate() {
        
        if (StringUtils.isEmpty(getBean().getEmailAddress())) {
            addError("error.add.user.missingEmailAddress");
        }
    }
    
    
    public RegisterFormBean getBean() {
        return bean;
    }

    public void setBean(RegisterFormBean bean) {
        this.bean = bean;
    }
    
}
