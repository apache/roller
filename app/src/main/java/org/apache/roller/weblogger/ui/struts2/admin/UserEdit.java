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
package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon.AuthMethod;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.core.Register;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Action that allows an admin to modify a users profile.
 */
public class UserEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(UserEdit.class);

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    // a bean to store our form data
    private User bean = new User();

    // user we are creating or modifying
    private User user = null;
    
    private AuthMethod authMethod = WebloggerStaticConfig.getAuthMethod();

    public UserEdit() {
        this.desiredMenu = "admin";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    // prepare for action by loading user object we are modifying
    public void prepare() {
        if (isAdd()) {
            user = new User();
        } else {
            try {
                // load the user object we are modifying
                if (bean.getId() != null) {
                    // action came from CreateUser or return from ModifyUser
                    user = userManager.getUser(bean.getId());
                } else if (bean.getUserName() != null) {
                    // action came from UserAdmin screen.
                    user = userManager.getUserByScreenName(bean.getUserName());
                }
            } catch (Exception e) {
                log.error("Error looking up user (id/username) :" + bean.getId() + "/" + bean.getUserName(), e);
            }
        }
    }

    /**
     * Show admin user edit page.
     */
    @SkipValidation
    public String execute() {
        if (isAdd()) {
            // initial user create
            bean.setLocale(Locale.getDefault().toString());
            bean.setTimeZone(TimeZone.getDefault().getID());
        } else {
            // populate form data from user profile data
            bean.setId(user.getId());
            bean.setUserName(user.getUserName());
            bean.setPassword(user.getPassword());
            bean.setScreenName(user.getScreenName());
            bean.setEmailAddress(user.getEmailAddress());
            bean.setLocale(user.getLocale());
            bean.setTimeZone(user.getTimeZone());
            bean.setEnabled(user.getEnabled());
            bean.setActivationCode(user.getActivationCode());
            bean.setGlobalRole(user.getGlobalRole());
        }
        return INPUT;
    }

    /**
     * Post user created message after first save.
     */
    @SkipValidation
    public String firstSave() {
        addMessage("createUser.add.success", bean.getUserName());
        return execute();
    }

    /**
     * Save modified user profile.
     */
    public String save() {
        myValidate();
        
        if (!hasActionErrors()) {
            user.setScreenName(bean.getScreenName().trim());
            user.setEmailAddress(bean.getEmailAddress().trim());
            user.setLocale(bean.getLocale());
            user.setTimeZone(bean.getTimeZone());
            user.setEnabled(bean.getEnabled());
            user.setActivationCode(bean.getActivationCode());

            // reset password if set
            if (!StringUtils.isEmpty(bean.getPassword())) {
                try {
                    user.resetPassword(bean.getPassword().trim());
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }

            try {
                if (isAdd()) {
                    user.setUserName(bean.getUserName().trim());
                    user.setDateCreated(new java.util.Date());
                    user.setGlobalRole(bean.getGlobalRole());
                    // save new user
                    userManager.addUser(user);
                } else {
                    if (!isUserEditingSelf()) {
                        user.setGlobalRole(bean.getGlobalRole());
                    } else if (user.getGlobalRole() != bean.getGlobalRole()) {
                        addError("userAdmin.cantChangeOwnRole");
                    }
                    userManager.saveUser(user);
                }

                WebloggerFactory.flush();
                if (isAdd()) {
                    // now that user is saved we have an id value
                    // store it back in bean for use in next action
                    bean.setId(user.getId());
                    // route to edit mode, saveFirst() provides the success message.
                    return SUCCESS;
                } else {
                    addMessage("userAdmin.userSaved");
                    return INPUT;
                }
            } catch (WebloggerException ex) {
                log.error("ERROR in action", ex);
                addError("generic.error.check.logs");
            }
        }
        return INPUT;
    }
    
    private boolean isAdd() {
        return actionName.equals("createUser");
    }

    @Validations(
            emails = { @EmailValidator(fieldName="bean.emailAddress", key="Register.error.emailAddressBad")}
    )
    private void myValidate() {
        if (StringUtils.isEmpty(bean.getUserName())) {
            addError("error.add.user.missingUserName");
        }
        if (StringUtils.isEmpty(bean.getScreenName())) {
            addError("Register.error.screenNameNull");
        }
        if (StringUtils.isEmpty(bean.getEmailAddress())) {
            addError("Register.error.emailAddressNull");
        }
        if (isAdd()) {
            String allowed = WebloggerStaticConfig.getProperty("username.allowedChars");
            if(allowed == null || allowed.trim().length() == 0) {
                allowed = Register.DEFAULT_ALLOWED_CHARS;
            }
            String safe = CharSetUtils.keep(bean.getUserName(), allowed);
            if (!safe.equals(bean.getUserName()) ) {
                addError("error.add.user.badUserName");
            }
            if (authMethod == AuthMethod.DATABASE && StringUtils.isEmpty(bean.getPassword())) {
                addError("error.add.user.missingPassword");
            }
        }
    }

    public User getBean() {
        return bean;
    }

    public void setBean(User bean) {
        this.bean = bean;
    }

    public boolean isUserEditingSelf() {
        return user.equals(getAuthenticatedUser());
    }

    public List<UserWeblogRole> getPermissions() {
        try {
            return userManager.getWeblogRoles(user);
        } catch (WebloggerException ex) {
            log.error("ERROR getting permissions for user " + user.getUserName(), ex);
        }
        return new ArrayList<>();
    }

    public String getAuthMethod() {
        return authMethod.name();
    }
}
