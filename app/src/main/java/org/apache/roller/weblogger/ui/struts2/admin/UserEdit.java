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

import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.RandomStringUtils;
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

    // a bean to store our form data
    private CreateUserBean bean = new CreateUserBean();

    // user we are creating or modifying
    private User user = null;
    
    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

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
    public void myPrepare() {
        if (isAdd()) {
            // create new User
            user = new User();
        } else {
            try {
                // load the user object we are modifying
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                if (bean.getId() != null) {
                    // action came from CreateUser or return from ModifyUser
                    user = mgr.getUser(getBean().getId());
                } else if (bean.getUserName() != null) {
                    // action came from UserAdmin screen.
                    user = mgr.getUserByUserName(getBean().getUserName(), null);
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
            getBean().setLocale(Locale.getDefault().toString());
            getBean().setTimeZone(TimeZone.getDefault().getID());
        } else {
            // populate form data from user profile data
            getBean().copyFrom(user);
        }
        return INPUT;
    }

    /**
     * Post user created message after first save.
     */
    @SkipValidation
    public String firstSave() {
        addMessage("createUser.add.success", getBean().getUserName());
        return execute();
    }

    /**
     * Save modified user profile.
     */
    public String save() {
        myValidate();
        
        if (!hasActionErrors()) {
            getBean().copyTo(user);

            // User.password does not allow null, so generate one
            if (authMethod.equals(AuthMethod.OPENID)) {
                try {
                    String randomString = RandomStringUtils.randomAlphanumeric(255);
                    user.resetPassword(randomString);
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }

            // reset password if set
            if (!StringUtils.isEmpty(getBean().getPassword())) {
                try {
                    user.resetPassword(getBean().getPassword());
                } catch (WebloggerException e) {
                    addMessage("yourProfile.passwordResetError");
                }
            }

            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                if (isAdd()) {
                    // fields not copied over from above copyTo():
                    user.setUserName(getBean().getUserName());
                    user.setDateCreated(new java.util.Date());
                    user.setGlobalRole(getBean().isAdministrator() ? GlobalRole.ADMIN : GlobalRole.BLOGGER);
                    // save new user
                    mgr.addUser(user);
                } else {
                    if (!isUserEditingSelf()) {
                        user.setGlobalRole(getBean().isAdministrator() ? GlobalRole.ADMIN : GlobalRole.BLOGGER);
                    } else if (mgr.isGlobalAdmin(user) != getBean().isAdministrator()) {
                        addError("userAdmin.cantChangeOwnRole");
                    }
                    mgr.saveUser(user);
                }

                WebloggerFactory.getWeblogger().flush();
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

    private void myValidate() {
        if (isAdd()) {
            String allowed = WebloggerConfig.getProperty("username.allowedChars");
            if(allowed == null || allowed.trim().length() == 0) {
                allowed = Register.DEFAULT_ALLOWED_CHARS;
            }
            String safe = CharSetUtils.keep(getBean().getUserName(), allowed);

            if (StringUtils.isEmpty(getBean().getUserName())) {
                addError("error.add.user.missingUserName");
            } else if (!safe.equals(getBean().getUserName()) ) {
                addError("error.add.user.badUserName");
            }
            if (authMethod == AuthMethod.ROLLERDB
                    && StringUtils.isEmpty(getBean().getPassword())) {
                addError("error.add.user.missingPassword");
            }
        }
        else {
            if (user.getUserName() == null) {
                addError("userAdmin.error.userNotFound");
            }
        }
        if ((authMethod == AuthMethod.OPENID) && StringUtils.isEmpty(getBean().getOpenIdUrl())) {
            addError("userRegister.error.missingOpenID");
        }

        // check that OpenID, if provided, is not taken
        if (!StringUtils.isEmpty(getBean().getOpenIdUrl())) {
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

    public CreateUserBean getBean() {
        return bean;
    }

    public void setBean(CreateUserBean bean) {
        this.bean = bean;
    }

    public boolean isUserEditingSelf() {
        return user.equals(getAuthenticatedUser());
    }

    public List<UserWeblogRole> getPermissions() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getWeblogRoles(user);
        } catch (WebloggerException ex) {
            log.error("ERROR getting permissions for user " + user.getUserName(), ex);
        }
        return new ArrayList<UserWeblogRole>();
    }

    public String getAuthMethod() {
        return authMethod.name();
    }
}
