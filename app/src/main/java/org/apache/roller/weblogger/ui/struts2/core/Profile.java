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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon.AuthMethod;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Allows user to edit his/her profile.
 */
public class Profile extends UIAction {
    private static Log log = LogFactory.getLog(Profile.class);
    
    private User bean = new User();
    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public Profile() {
        this.pageTitle = "yourProfile.title";
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @SkipValidation
    public String execute() {
        User ud = getAuthenticatedUser();
        // load up the form from the users existing profile data
        bean.setId(ud.getId());
        bean.setUserName(ud.getUserName());
        bean.setScreenName(ud.getScreenName());
        bean.setFullName(ud.getFullName());
        bean.setEmailAddress(ud.getEmailAddress());
        bean.setLocale(ud.getLocale());
        bean.setTimeZone(ud.getTimeZone());
        return INPUT;
    }

    public String save() {
        myValidate();

        if (!hasActionErrors()) {

            // We ONLY modify the user currently logged in
            User existingUser = getAuthenticatedUser();

            // copy updated attributes
            existingUser.setScreenName(bean.getScreenName());
            existingUser.setFullName(bean.getFullName());
            existingUser.setEmailAddress(bean.getEmailAddress());
            existingUser.setLocale(bean.getLocale());
            existingUser.setTimeZone(bean.getTimeZone());

            // If user set both password and passwordConfirm then reset password
            if (!StringUtils.isEmpty(bean.getPasswordText()) &&
                    !StringUtils.isEmpty(bean.getPasswordConfirm())) {
                try {
                    existingUser.resetPassword(bean.getPasswordText());
                    bean.setPasswordText(null);
                    bean.setPasswordConfirm(null);
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }

            try {
                // save the updated profile
                userManager.saveUser(existingUser);
                WebloggerFactory.flush();
                addMessage("generic.changes.saved");
                return SUCCESS;
            } catch (WebloggerException ex) {
                log.error("ERROR in action", ex);
                addError("Unexpected error doing profile save");
            }
        }
        return INPUT;
    }

    public void myValidate() {
        // check that passwords match if they were specified (w/StringUtils.equals, null == null)
        if (!StringUtils.equals(bean.getPasswordText(), bean.getPasswordConfirm())) {
            addError("userRegister.error.mismatchedPasswords");
        }
    }

    public String getAuthMethod() {
        return authMethod.name();
    }
    
    public User getBean() {
        return bean;
    }

    public void setBean(User bean) {
        this.bean = bean;
    }
}
