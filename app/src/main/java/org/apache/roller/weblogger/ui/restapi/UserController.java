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
package org.apache.roller.weblogger.ui.restapi;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.security.LDAPRegistrationHelper;
import org.apache.roller.weblogger.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

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

    @Autowired
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Autowired
    private LDAPRegistrationHelper ldapRegistrationHelper;

    public void setLdapRegistrationHelper(LDAPRegistrationHelper ldapRegistrationHelper) {
        this.ldapRegistrationHelper = ldapRegistrationHelper;
    }

    public UserController() {
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
                SafeUser su = potentialIter.next();
                ListIterator<UserWeblogRole> alreadyIter = currentUserList.listIterator();
                while (alreadyIter.hasNext()) {
                    UserWeblogRole au = alreadyIter.next();
                    if (su.getId().equals(au.getUser().getId())) {
                        potentialIter.remove();
                        alreadyIter.remove();
                        break;
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
        Map<String, String> userMap = new TreeMap<>();
        for (SafeUser user : users) {
            userMap.put(user.getId(), user.getScreenName() + " (" + user.getEmailAddress() + ")");
        }
        Map<String, String> sortedMap = userMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (e1, e2) -> e2, LinkedHashMap::new));
        return sortedMap;
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}", method = RequestMethod.GET)
    public User getUserData(@PathVariable String id, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        if (user != null) {
            user.setPassword(null);
            user.setPasswordText(null);
            user.setPasswordConfirm(null);
            return user;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/userprofile/{id}", method = RequestMethod.GET)
    public User getProfileData(@PathVariable String id, Principal p, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        User authenticatedUser = userManager.getUserByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            user.setPassword(null);
            user.setPasswordText(null);
            user.setPasswordConfirm(null);
            return user;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/users", method = RequestMethod.PUT)
    public ResponseEntity addUser(@Valid @RequestBody User newData, Principal p, HttpServletResponse response) throws ServletException {
        User user = new User();
        ValidationError maybeError = advancedValidate(newData, true);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }
        user.setId(WebloggerCommon.generateUUID());
        user.setUserName(newData.getUserName());
        user.setDateCreated(new java.util.Date());
        return saveUser(user, newData, p, response);
    }

    @RequestMapping(value = "/tb-ui/register/rest/ldapdata", method = RequestMethod.GET)
    public ResponseEntity getLDAPData(HttpServletRequest request) throws ServletException {
        if (WebloggerStaticConfig.getAuthMethod() == WebloggerCommon.AuthMethod.LDAP) {
            // See if user is already logged in via Spring Security
            User fromSSOUser = ldapRegistrationHelper.getUserDetailsFromAuthentication(request);
            if (fromSSOUser != null) {
                // Copy user details from Spring Security, including LDAP attributes
                User user = new User();
                user.setId(fromSSOUser.getId());
                user.setUserName(fromSSOUser.getUserName());
                user.setScreenName(fromSSOUser.getScreenName());
                user.setEmailAddress(fromSSOUser.getEmailAddress());
                user.setLocale(fromSSOUser.getLocale());
                return ResponseEntity.ok().body(user);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/tb-ui/register/rest/registeruser", method = RequestMethod.PUT)
    public ResponseEntity registerUser(@Valid @RequestBody User newData, HttpServletResponse response) throws ServletException {
        if (propertiesManager.getBooleanProperty("users.registration.enabled") || userManager.getUserCount() == 0) {
            boolean mustActivate = propertiesManager.getBooleanProperty("user.account.email.activation");
            newData.setEnabled(!mustActivate);
            if (mustActivate) {
                String activationCode = UUID.randomUUID().toString();
                newData.setActivationCode(activationCode);
            }
            ResponseEntity re = addUser(newData, null, response);
            if (re.getStatusCode() == HttpStatus.OK && mustActivate) {
                try {
                    mailManager.sendUserActivationEmail((User) re.getBody());
                } catch (MessagingException ignored) {
                }
            }
            return re;
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/userprofile/{id}", method = RequestMethod.POST)
    public ResponseEntity updateUserProfile(@PathVariable String id, @Valid @RequestBody User newData, Principal p,
                                            HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        User authenticatedUser = userManager.getUserByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            ValidationError maybeError = advancedValidate(newData, false);
            if (maybeError != null) {
                return ResponseEntity.badRequest().body(maybeError);
            }
            return saveUser(user, newData, p, response);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}", method = RequestMethod.PUT)
    public ResponseEntity updateUser(@PathVariable String id, @Valid @RequestBody User newData, Principal p,
                                     HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        ValidationError maybeError = advancedValidate(newData, false);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }
        return saveUser(user, newData, p, response);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}/weblogs", method = RequestMethod.GET)
    public List<UserWeblogRole> getUsersWeblogs(@PathVariable String id, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        List<UserWeblogRole> uwrs = userManager.getWeblogRolesIncludingPending(user);
        for (UserWeblogRole uwr : uwrs) {
            uwr.setUser(null);
        }
        return uwrs;
    }

    private ResponseEntity saveUser(User user, User newData, Principal p, HttpServletResponse response) throws ServletException {
        try {
            if (user != null) {
                user.setScreenName(newData.getScreenName().trim());
                user.setEmailAddress(newData.getEmailAddress().trim());
                user.setLocale(newData.getLocale());
                user.setEnabled(newData.getEnabled());
                if (!user.getEnabled() && StringUtils.isNotEmpty(newData.getActivationCode())) {
                    user.setActivationCode(newData.getActivationCode());
                }
                if (p == null) {
                    // user self-registration
                    user.setGlobalRole(GlobalRole.BLOGGER);
                } else if (!user.getUserName().equals(p.getName())) {
                    user.setGlobalRole(newData.isGlobalAdmin() ? GlobalRole.ADMIN : GlobalRole.BLOGGER);
                }

                // reset password if set
                if (!StringUtils.isEmpty(newData.getPassword())) {
                    user.resetPassword(newData.getPassword().trim());
                }

                try {
                    userManager.saveUser(user);
                    WebloggerFactory.flush();
                    user.setPassword(null);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (RollbackException e) {
                    return ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body("Persistence Problem");
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private ValidationError advancedValidate(User data, boolean isAdd) {
        BindException be = new BindException(data, "new data object");

        ValidationError.fromBindingErrors(be);

        if (!StringUtils.isEmpty(data.getPassword()) && !data.getPassword().equals(data.getPasswordConfirm())) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.passwordConfirmFail")));
        }

        if (isAdd &&
                WebloggerStaticConfig.getAuthMethod() == WebloggerCommon.AuthMethod.DATABASE &&
                StringUtils.isEmpty(data.getPassword())) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.missingPassword")));
        }

        User testUser = userManager.getUserByUserName(data.getUserName(), null);
        if (testUser != null && !testUser.getId().equals(data.getId())) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.userNameInUse")));
        }

        testUser = userManager.getUserByScreenName(data.getScreenName());
        if (testUser != null && !testUser.getId().equals(data.getId())) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.screenNameInUse")));
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

}
