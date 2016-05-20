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

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerCommon.AuthMethod;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.core.Register;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserEdit extends UIAction {

    private static Logger log = LoggerFactory.getLogger(UserEdit.class);

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    // a bean to store our form data
    private User bean = new User();

    // user we are creating or modifying
    private User user = null;
    
    public UserEdit() {
        this.desiredMenu = "admin";
        this.requiredWeblogRole = WeblogRole.NOBLOGNEEDED;
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/userlist", method = RequestMethod.GET)
    public Map<String, String> getUserEditList() throws ServletException {
        return createUserMap(userManager.getUsers(null, null, 0, -1));
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/{weblogHandle}/potentialmembers", method = RequestMethod.GET)
    public Map<String, String> getPotentialNewMembers(@PathVariable String weblogHandle, Principal p,
                                                      HttpServletResponse response)
            throws ServletException {

        if (userManager.checkWeblogRole(p.getName(), weblogHandle, WeblogRole.OWNER)) {
            // member list excludes inactive accounts
            List<SafeUser> potentialUsers = userManager.getUsers(null, true, 0, -1);

            // filter out people already members
            Weblog weblog = weblogManager.getWeblogByHandle(weblogHandle);
            ListIterator<SafeUser> potentialIter = potentialUsers.listIterator();
            List<UserWeblogRole> currentUserList = userManager.getWeblogRolesIncludingPending(weblog);
            while (potentialIter.hasNext() && !currentUserList.isEmpty()) {
                ListIterator<UserWeblogRole> alreadyIter = currentUserList.listIterator();
                while (alreadyIter.hasNext()) {
                    UserWeblogRole au = alreadyIter.next();
                    SafeUser su = potentialIter.next();
                    if (su.getId().equals(au.getUser().getId())) {
                        potentialIter.remove();
                        alreadyIter.remove();
                    }
                }
            }
            return createUserMap(potentialUsers);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    private Map<String, String> createUserMap(List<SafeUser> users) {
        Map<String, String> userMap = new HashMap<>();
        for (SafeUser user : users) {
            userMap.put(user.getId(), user.getScreenName() + " (" + user.getEmailAddress() + ")");
        }
        return userMap;
    }

    // prepare for action by loading user object we are modifying
    public void prepare() {
        if (isAdd()) {
            user = new User();
        } else {
            try {
                if (bean.getId() != null) {
                    user = userManager.getUser(bean.getId());
                }
            } catch (Exception e) {
                log.error("Error looking up user id: {} username: {}", bean.getId(), bean.getUserName());
                log.error("Exception: ", e);
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
                user.resetPassword(bean.getPassword().trim());
            }
            if (isAdd()) {
                user.setId(WebloggerCommon.generateUUID());
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
            if (WebloggerStaticConfig.getAuthMethod() == AuthMethod.DATABASE && StringUtils.isEmpty(bean.getPassword())) {
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
        return userManager.getWeblogRoles(user);
    }
}
