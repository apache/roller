/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.pojos.WebloggerProperties.RegistrationPolicy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserCredentials;
import org.apache.roller.weblogger.pojos.UserSearchCriteria;
import org.apache.roller.weblogger.pojos.UserStatus;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";

    private Pattern pattern;

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
    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    public UserController() {
        pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/userlist", method = RequestMethod.GET)
    public Map<String, String> getUserEditList() throws ServletException {
        UserSearchCriteria usc = new UserSearchCriteria();
        return createUserMap(userManager.getUsers(usc));
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval", method = RequestMethod.GET)
    public List<User> getRegistrationsNeedingApproval() throws ServletException {
        UserSearchCriteria usc = new UserSearchCriteria();
        usc.setStatus(UserStatus.EMAILVERIFIED);
        return userManager.getUsers(usc);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval/{id}/approve", method = RequestMethod.POST)
    public void approveRegistration(@PathVariable String id, HttpServletResponse response) {
        User acceptedUser = userManager.getUser(id);
        if (acceptedUser != null) {
            if (!UserStatus.ENABLED.equals(acceptedUser.getStatus())) {
                acceptedUser.setStatus(UserStatus.ENABLED);
                userManager.saveUser(acceptedUser);
                mailManager.sendRegistrationApprovedNotice(acceptedUser);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval/{id}/reject", method = RequestMethod.POST)
    public void rejectRegistration(@PathVariable String id, HttpServletResponse response) {
        User rejectedUser = userManager.getUser(id);
        if (rejectedUser != null) {
            mailManager.sendRegistrationRejectedNotice(rejectedUser);
            userManager.removeUser(rejectedUser);
            persistenceStrategy.flush();
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/potentialmembers", method = RequestMethod.GET)
    public Map<String, String> getPotentialNewBlogMembers(@PathVariable String weblogId, Principal p,
                                                          HttpServletResponse response)
            throws ServletException {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
            // member list excludes inactive accounts
            UserSearchCriteria usc = new UserSearchCriteria();
            usc.setStatus(UserStatus.ENABLED);
            usc.setOffset(0);
            List<User> potentialUsers = userManager.getUsers(usc);

            // filter out people already members
            ListIterator<User> potentialIter = potentialUsers.listIterator();
            List<UserWeblogRole> currentUserList = userManager.getWeblogRolesIncludingPending(weblog);
            while (potentialIter.hasNext() && !currentUserList.isEmpty()) {
                User su = potentialIter.next();
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

    private Map<String, String> createUserMap(List<User> users) {
        Map<String, String> userMap = new TreeMap<>();
        for (User user : users) {
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
            return user;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/userprofile/{id}", method = RequestMethod.GET)
    public User getProfileData(@PathVariable String id, Principal p, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        User authenticatedUser = userManager.getEnabledUserByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            return user;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/register/rest/ldapdata", method = RequestMethod.GET)
    public ResponseEntity getLDAPData(Principal p) throws ServletException {

        // See if user is already logged in via Spring Security
        if (WebloggerStaticConfig.getAuthMethod() == WebloggerStaticConfig.AuthMethod.LDAP && p != null) {

            UsernamePasswordAuthenticationToken p2 = (UsernamePasswordAuthenticationToken) p;
            LdapUserDetails userDetails = (LdapUserDetails) p2.getPrincipal();
            if (userDetails.isEnabled()) {
                // Copy username from LDAP
                User user = new User();
                user.setUserName(userDetails.getUsername());
                user.setLocale(Locale.getDefault().toString());
                return ResponseEntity.ok().body(user);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/tb-ui/register/rest/registeruser", method = RequestMethod.POST)
    public ResponseEntity registerUser(@Valid @RequestBody UserData newData, HttpServletResponse response) throws ServletException {
        ValidationError maybeError = advancedValidate(null, newData, true);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }

        long userCount = userManager.getUserCount();
        RegistrationPolicy option = persistenceStrategy.getWebloggerProperties().getRegistrationPolicy();
        if (userCount == 0 || !RegistrationPolicy.DISABLED.equals(option)) {
            boolean mustActivate = userCount > 0;
            if (mustActivate) {
                newData.user.setActivationCode(UUID.randomUUID().toString());
                newData.user.setStatus(UserStatus.REGISTERED);
            } else {
                // initial user is the Admin, is automatically enabled.
                newData.user.setStatus(UserStatus.ENABLED);
            }

            User user = new User();
            user.setId(Utilities.generateUUID());
            user.setUserName(newData.user.getUserName());
            user.setDateCreated(Instant.now());

            ResponseEntity re = saveUser(user, newData, null, response, true);

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
    public ResponseEntity updateUserProfile(@PathVariable String id, @Valid @RequestBody UserData newData, Principal p,
                                            HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        User authenticatedUser = userManager.getEnabledUserByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            ValidationError maybeError = advancedValidate(null, newData, false);
            if (maybeError != null) {
                return ResponseEntity.badRequest().body(maybeError);
            }
            return saveUser(user, newData, p, response, false);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}", method = RequestMethod.PUT)
    public ResponseEntity updateUser(@PathVariable String id, @Valid @RequestBody UserData newData, Principal p,
                                     HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        ValidationError maybeError = advancedValidate(user, newData, false);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }
        return saveUser(user, newData, p, response, false);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class UserData {
        @Valid
        User user;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        UserCredentials credentials;

        public UserData() {
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public UserCredentials getCredentials() {
            return credentials;
        }

        public void setCredentials(UserCredentials credentials) {
            this.credentials = credentials;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/members", method = RequestMethod.GET)
    public List<UserWeblogRole> getWeblogMembers(@PathVariable String weblogId, Principal p, HttpServletResponse response)
            throws ServletException {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
            List<UserWeblogRole> uwrs = userManager.getWeblogRolesIncludingPending(weblog);
            return uwrs;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/memberupdate", method = RequestMethod.POST)
    public ResponseEntity updateWeblogMembership(@PathVariable String weblogId, Principal p, @RequestBody List<UserWeblogRole> roles)
            throws ServletException {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        User user = userManager.getEnabledUserByUserName(p.getName());
        if (user != null && weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

            // must remain at least one admin
            List<UserWeblogRole> owners = roles.stream()
                    .filter(r -> r.getWeblogRole().equals(WeblogRole.OWNER))
                    .filter(r -> !r.isPending())
                    .collect(Collectors.toList());
            if (owners.size() < 1) {
                return ResponseEntity.badRequest().body(user.getI18NMessages().getString("members.oneAdminRequired"));
            }

            // one iteration for each line (user) in the members table
            for (UserWeblogRole role : roles) {
                if (WeblogRole.NOBLOGNEEDED.equals(role.getWeblogRole())) {
                    userManager.revokeWeblogRole(role);
                } else {
                    userManager.grantWeblogRole(
                            role.getUser(), role.getWeblog(), role.getWeblogRole(), role.isPending());
                }
            }
            persistenceStrategy.flush();
            String msg = user.getI18NMessages().getString("members.membersChanged");
            return ResponseEntity.ok(msg);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private ResponseEntity saveUser(User user, UserData newData, Principal p, HttpServletResponse response, boolean add) throws ServletException {
        try {
            if (user != null) {
                user.setScreenName(newData.user.getScreenName().trim());
                user.setEmailAddress(newData.user.getEmailAddress().trim());
                user.setLocale(newData.user.getLocale());
                if (!UserStatus.ENABLED.equals(user.getStatus()) && StringUtils.isNotEmpty(newData.user.getActivationCode())) {
                    user.setActivationCode(newData.user.getActivationCode());
                }

                if (add) {
                    user.setStatus(newData.user.getStatus());
                    if (userManager.getUserCount() == 0) {
                        // first person in is always an admin
                        user.setGlobalRole(GlobalRole.ADMIN);
                    } else {
                        user.setGlobalRole(persistenceStrategy.getWebloggerProperties().isUsersCreateBlogs() ?
                                GlobalRole.BLOGCREATOR : GlobalRole.BLOGGER);
                    }
                } else {
                    // users can't alter own roles or status
                    if (!user.getUserName().equals(p.getName())) {
                        user.setGlobalRole(newData.user.getGlobalRole());
                        user.setStatus(newData.user.getStatus());
                    }
                }

                try {
                    userManager.saveUser(user);
                    // reset password if set
                    if (!StringUtils.isEmpty(newData.credentials.getPasswordText())) {
                        userManager.updateCredentials(user.getId(), newData.credentials.getPasswordText());
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (RollbackException e) {
                    return ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body("Persistence Problem");
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error updating user", e);
            throw new ServletException(e.getMessage());
        }
    }

    private ValidationError advancedValidate(User currentUser, UserData data, boolean isAdd) {
        BindException be = new BindException(data, "new data object");

        UserSearchCriteria usc1 = new UserSearchCriteria();
        usc1.setUserName(data.user.getUserName());
        List<User> users = userManager.getUsers(usc1);
        if (users.size() > 1 || (users.size() == 1 && !users.get(0).getId().equals(data.user.getId()))) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.userNameInUse")));
        }

        UserSearchCriteria usc2 = new UserSearchCriteria();
        usc2.setScreenName(data.user.getScreenName());
        users = userManager.getUsers(usc2);
        if (users.size() > 1 || (users.size() == 1 && !users.get(0).getId().equals(data.user.getId()))) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.screenNameInUse")));
        }

        if (currentUser != null) {
            UserStatus currentStatus = currentUser.getStatus();
            if (currentStatus != data.getUser().getStatus()) {
                switch (currentStatus) {
                    case ENABLED:
                        if (data.getUser().getStatus() != UserStatus.DISABLED) {
                            be.addError(new ObjectError("User object", bundle.getString("error.useradmin.enabled.only.disabled")));
                        }
                        break;
                    case DISABLED:
                        if (data.getUser().getStatus() != UserStatus.ENABLED) {
                            be.addError(new ObjectError("User object", bundle.getString("error.useradmin.disabled.only.enabled")));
                        }
                        break;
                    case REGISTERED:
                    case EMAILVERIFIED:
                        if (data.getUser().getStatus() != UserStatus.ENABLED) {
                            be.addError(new ObjectError("User object", bundle.getString("error.useradmin.nonenabled.only.enabled")));
                        }
                        break;
                    default:
                }
            }
        }

        String maybePassword = data.credentials.getPasswordText();
        if (!StringUtils.isEmpty(maybePassword)) {
            if (!maybePassword.equals(data.credentials.getPasswordConfirm())) {
                be.addError(new ObjectError("User object", bundle.getString("error.add.user.passwordConfirmFail")));
            } else {
                if (!pattern.matcher(maybePassword).matches()) {
                    be.addError(new ObjectError("User object", bundle.getString("error.add.user.passwordComplexityFail")));
                }
            }
        } else {
            if (!StringUtils.isEmpty(data.credentials.getPasswordConfirm())) {
                // confirm provided but password field itself not filled out
                be.addError(new ObjectError("User object", bundle.getString("error.add.user.passwordConfirmFail")));
            }
        }

        if (isAdd &&
                WebloggerStaticConfig.getAuthMethod() == WebloggerStaticConfig.AuthMethod.DB &&
                StringUtils.isEmpty(data.credentials.getPasswordText())) {
            be.addError(new ObjectError("User object", bundle.getString("error.add.user.missingPassword")));
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
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

    @RequestMapping(value = "/tb-ui/authoring/rest/loggedinuser/weblogs", method = RequestMethod.GET)
    public List<UserWeblogRole> getLoggedInUsersWeblogs(Principal p, HttpServletResponse response)
            throws ServletException {
        User user = userManager.getEnabledUserByUserName(p.getName());
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        return getUsersWeblogs(user.getId(), response);
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/user/{userId}/role/{role}/invite",
            method = RequestMethod.POST)
    public ResponseEntity inviteUser(@PathVariable String weblogId, @PathVariable String userId,
                                     @PathVariable WeblogRole role, Principal p) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        User invitee = userManager.getUser(userId);
        User invitor = userManager.getEnabledUserByUserName(p.getName());

        if (weblog != null && invitee != null && invitor != null &&
                userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

            I18nMessages messages = invitor.getI18NMessages();
            UserWeblogRole roleChk = userManager.getWeblogRoleIncludingPending(invitee, weblog);
            if (roleChk != null) {
                return ResponseEntity.badRequest().body(messages.getString(roleChk.isPending() ?
                        "members.userAlreadyInvited" : "members.userAlreadyMember"));
            }
            userManager.grantWeblogRole(invitee, weblog, role, true);
            persistenceStrategy.flush();
            mailManager.sendWeblogInvitation(invitee, weblog);
            return ResponseEntity.ok(messages.getString("members.userInvited"));
        } else {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblogrole/{id}/attach", method = RequestMethod.POST)
    public void acceptWeblogInvitation(@PathVariable String id, Principal p, HttpServletResponse response) {
        UserWeblogRole uwr = userManager.getUserWeblogRole(id);
        if (uwr != null && uwr.getUser().getUserName().equals(p.getName())) {
            userManager.acceptWeblogInvitation(uwr);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        persistenceStrategy.flush();
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblogrole/{id}/detach", method = RequestMethod.POST)
    public void resignFromWeblog(@PathVariable String id, Principal p, HttpServletResponse response) {
        UserWeblogRole uwr = userManager.getUserWeblogRole(id);
        if (uwr != null && uwr.getUser().getUserName().equals(p.getName())) {
            userManager.revokeWeblogRole(uwr);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        persistenceStrategy.flush();
    }

    @RequestMapping(value = "/tb-ui/register/rest/useradminmetadata", method = RequestMethod.GET)
    public UserAdminMetadata getUserAdminMetadata() {
        UserAdminMetadata metadata = new UserAdminMetadata();

        List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());

        metadata.locales = locales.stream()
                .sorted(Comparator.comparing(Locale::getDisplayName))
                .collect(Utilities.toLinkedHashMap(Locale::toString, Locale::getDisplayName));

        metadata.userStatuses = Arrays.stream(UserStatus.values())
                .collect(Utilities.toLinkedHashMap(UserStatus::name, UserStatus::name));

        metadata.globalRoles = Arrays.stream(GlobalRole.values())
                .filter(r -> r != GlobalRole.NOAUTHNEEDED)
                .collect(Utilities.toLinkedHashMap(GlobalRole::name, GlobalRole::name));

        return metadata;
    }

    public class UserAdminMetadata {
        Map<String, String> locales;
        Map<String, String> userStatuses;
        Map<String, String> globalRoles;

        public Map<String, String> getLocales() {
            return locales;
        }

        public Map<String, String> getUserStatuses() {
            return userStatuses;
        }

        public Map<String, String> getGlobalRoles() {
            return globalRoles;
        }
    }

}
