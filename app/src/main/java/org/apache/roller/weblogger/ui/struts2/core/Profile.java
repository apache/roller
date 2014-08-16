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
package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Allows user to edit his/her profile.
 */
public class Profile extends UIAction {
    private static Log log = LogFactory.getLog(Profile.class);
    
    private ProfileBean bean = new ProfileBean();
    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

    public Profile() {
        this.pageTitle = "yourProfile.title";
    }
    
    
    // override default security, we do not require an action weblog
    public boolean isWeblogRequired() {
        return false;
    }


    @SkipValidation
    public String execute() {
        User ud = getAuthenticatedUser();
        // load up the form from the users existing profile data
        getBean().copyFrom(ud);
        return INPUT;
    }

    public String save() {
        myValidate();

        if (!hasActionErrors()) {

            // We ONLY modify the user currently logged in
            User existingUser = getAuthenticatedUser();

            // copy updated attributes
            getBean().copyTo(existingUser);

            if (StringUtils.isNotEmpty(getBean().getOpenIdUrl())) { 
                try {
                    String openidurl = getBean().getOpenIdUrl();
                    if (openidurl != null && openidurl.endsWith("/")) {
                        openidurl = openidurl.substring(0, openidurl.length() - 1);
                    }
                    existingUser.setOpenIdUrl(openidurl);
                } catch (Exception ex) {
                    log.error("Unexpected error saving user OpenID URL", ex);
                    addError("generic.error.check.logs");
                    return INPUT;
                }
            }

            if (authMethod == AuthMethod.DB_OPENID) {
                if (StringUtils.isEmpty(existingUser.getPassword())
                        && StringUtils.isEmpty(bean.getPasswordText())
                        && StringUtils.isEmpty(bean.getOpenIdUrl())) {
                    addError("userRegister.error.missingOpenIDOrPassword");
                    return INPUT;
                } else if (StringUtils.isNotEmpty(bean.getOpenIdUrl())
                        && StringUtils.isNotEmpty(bean.getPasswordText())) {
                    addError("userRegister.error.bothOpenIDAndPassword");
                    return INPUT;
                }
            }

            // User.password does not allow null, so generate one
            if (authMethod.equals(AuthMethod.OPENID) ||
                    (authMethod.equals(AuthMethod.DB_OPENID) && !StringUtils.isEmpty(bean.getOpenIdUrl()))) {
                String randomString = RandomStringUtils.randomAlphanumeric(255);
                try {
                    existingUser.resetPassword(randomString);
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }

            // If user set both password and passwordConfirm then reset password
            if (!StringUtils.isEmpty(getBean().getPasswordText()) &&
                    !StringUtils.isEmpty(getBean().getPasswordConfirm())) {
                try {
                    existingUser.resetPassword(getBean().getPasswordText());
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }

            try {
                // save the updated profile
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                mgr.saveUser(existingUser);
                WebloggerFactory.getWeblogger().flush();
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
        if (StringUtils.isEmpty(getBean().getOpenIdUrl())) {
            // check that passwords match if they were specified (w/StringUtils.equals, null == null)
            if (!StringUtils.equals(getBean().getPasswordText(), getBean().getPasswordConfirm())) {
                addError("userRegister.error.mismatchedPasswords");
            }
            if (authMethod == AuthMethod.OPENID) {
                addError("userRegister.error.missingOpenID");
            }
        } else {
            // check that OpenID, if provided, is not taken
            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                User user = mgr.getUserByOpenIdUrl(bean.getOpenIdUrl());
                if (user != null && !(user.getUserName().equals(bean.getUserName()))) {
                    addError("error.add.user.openIdInUse");
                }
            } catch (WebloggerException ex) {
                log.error("error checking OpenID URL", ex);
                addError("generic.error.check.logs");
            }
        }
    }

    public String getAuthMethod() {
        return authMethod.name();
    }
    
    public ProfileBean getBean() {
        return bean;
    }

    public void setBean(ProfileBean bean) {
        this.bean = bean;
    }
}
