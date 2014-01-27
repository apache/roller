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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserAttribute;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Allows user to edit his/her profile.
 * TODO: check on the impact of deleting that cookieLogin stuff
 */
public class Profile extends UIAction {
    private static Log log = LogFactory.getLog(Profile.class);
    
    private ProfileBean bean = new ProfileBean();
    private String openIdConfiguration = 
        WebloggerConfig.getProperty("authentication.openid");
    private boolean usingSso = 
        WebloggerConfig.getBooleanProperty("users.sso.enabled");
            
    
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
        getBean().setPasswordText(null);
        getBean().setPasswordConfirm(null);
        getBean().setLocale(ud.getLocale());
        getBean().setTimeZone(ud.getTimeZone());
        
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        try {
            UserAttribute openIdUrl = mgr.getUserAttribute(
                ud.getUserName(), UserAttribute.Attributes.OPENID_URL.toString());
            if (openIdUrl != null) {
                getBean().setOpenIdUrl(openIdUrl.getValue());
            }
        } catch (Exception ex) {
            log.error("Unexpected error loading user OpenID url", ex);
            addError("error in action", ex.toString());
        }
        return INPUT;
    }

    
    public String save() {

        myValidate();

        if (!hasActionErrors()) {
            
            // We ONLY modify the user currently logged in
            User existingUser = getAuthenticatedUser();

            // We want to be VERY selective about what data gets updated
            existingUser.setScreenName(getBean().getScreenName());
            existingUser.setFullName(getBean().getFullName());
            existingUser.setEmailAddress(getBean().getEmailAddress());
            existingUser.setLocale(getBean().getLocale());
            existingUser.setTimeZone(getBean().getTimeZone());
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            
            if (StringUtils.isNotEmpty(getBean().getOpenIdUrl())) { 
                try {
                    String openidurl = getBean().getOpenIdUrl();
                    if (openidurl != null && openidurl.endsWith("/")) {
                        openidurl = openidurl.substring(0, openidurl.length() - 1);
                    }
                    mgr.setUserAttribute(existingUser.getUserName(), 
                        UserAttribute.Attributes.OPENID_URL.toString(), openidurl);
                } catch (Exception ex) {
                    log.error("Unexpected error saving user OpenID URL", ex);
                    addError("Error in action", ex.toString());
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
                mgr.saveUser(existingUser);
                WebloggerFactory.getWeblogger().flush();

                // TODO: i18n
                addMessage("profile updated.");

                return SUCCESS;

            } catch (WebloggerException ex) {
                log.error("ERROR in action", ex);
                // TODO: i18n
                addError("unexpected error doing profile save");
            }

        }

        return INPUT;
    }

    
    public void myValidate() {

        // check that passwords match if they were specified
        if (!StringUtils.isEmpty(getBean().getPasswordText())) {
            if (!getBean().getPasswordText().equals(getBean().getPasswordConfirm())) {
                addError("Register.error.passowordMismatch");
            }
        }
    }

    public String getOpenIdConfiguration() {
        return openIdConfiguration;
    }
    
    public ProfileBean getBean() {
        return bean;
    }

    public void setBean(ProfileBean bean) {
        this.bean = bean;
    }
    
    public boolean getUsingSso() {
        return this.usingSso;
    }
}
