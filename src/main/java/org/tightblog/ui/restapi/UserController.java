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
package org.tightblog.ui.restapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.business.MailManager;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserCredentials;
import org.tightblog.pojos.UserSearchCriteria;
import org.tightblog.pojos.UserStatus;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.util.Utilities;
import org.tightblog.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.pojos.WebloggerProperties;

import javax.mail.MessagingException;
import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);

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
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Autowired
    private MessageSource messages;

    public UserController() {
        pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/userlist")
    public Map<String, String> getUserEditList() throws ServletException {
        UserSearchCriteria usc = new UserSearchCriteria();
        return createUserMap(userManager.getUsers(usc));
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval")
    public List<User> getRegistrationsNeedingApproval() throws ServletException {
        UserSearchCriteria usc = new UserSearchCriteria();
        usc.setStatus(UserStatus.EMAILVERIFIED);
        return userManager.getUsers(usc);
    }

    @PostMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval/{id}/approve")
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

    @PostMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval/{id}/reject")
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

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/potentialmembers")
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

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}")
    public UserData getUserData(@PathVariable String id, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);

        if (user != null) {
            UserData data = new UserData();
            UserCredentials creds = userManager.getCredentialsByUserName(user.getUserName());
            data.setUser(user);
            data.setCredentials(creds);
            return data;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @GetMapping(value = "/tb-ui/authoring/rest/userprofile/{id}")
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

    @PostMapping(value = "/tb-ui/register/rest/registeruser")
    public ResponseEntity registerUser(@Valid @RequestBody UserData newData, Locale locale, HttpServletResponse response)
            throws ServletException {
        ValidationError maybeError = advancedValidate(null, newData, true, locale);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }

        long userCount = userManager.getUserCount();
        WebloggerProperties.RegistrationPolicy option = persistenceStrategy.getWebloggerProperties().getRegistrationPolicy();
        if (userCount == 0 || !WebloggerProperties.RegistrationPolicy.DISABLED.equals(option)) {
            boolean mustActivate = userCount > 0;
            if (mustActivate) {
                newData.user.setActivationCode(UUID.randomUUID().toString());
                newData.user.setStatus(UserStatus.REGISTERED);
            } else {
                // initial user is the Admin, is automatically enabled.
                newData.user.setStatus(UserStatus.ENABLED);
            }

            User user = new User();
            user.setUserName(newData.user.getUserName());
            user.setDateCreated(Instant.now());

            ResponseEntity re = saveUser(user, newData, null, response, true);

            if (re.getStatusCode() == HttpStatus.OK && mustActivate) {
                try {
                    UserData data = (UserData) re.getBody();
                    mailManager.sendUserActivationEmail(data.getUser());
                } catch (MessagingException ignored) {
                }
            }
            return re;
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/userprofile/{id}")
    public ResponseEntity updateUserProfile(@PathVariable String id, @Valid @RequestBody UserData newData, Principal p,
                                            Locale locale, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        User authenticatedUser = userManager.getEnabledUserByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            ValidationError maybeError = advancedValidate(null, newData, false, locale);
            if (maybeError != null) {
                return ResponseEntity.badRequest().body(maybeError);
            }
            return saveUser(user, newData, p, response, false);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}")
    public ResponseEntity updateUser(@PathVariable String id, @Valid @RequestBody UserData newData, Principal p,
                                     Locale locale, HttpServletResponse response) throws ServletException {
        User user = userManager.getUser(id);
        ValidationError maybeError = advancedValidate(user, newData, false, locale);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }
        return saveUser(user, newData, p, response, false);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class UserData {
        @Valid
        User user;

        UserCredentials credentials;

        UserData() {
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

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/members")
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

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/memberupdate", produces = "text/plain")
    public ResponseEntity updateWeblogMembership(@PathVariable String weblogId, Principal p, Locale locale,
                                                 @RequestBody List<UserWeblogRole> roles)
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
                return ResponseEntity.badRequest().body(messages.getMessage("members.oneAdminRequired", null, locale));
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
            String msg = messages.getMessage("members.membersChanged", null, locale);
            return ResponseEntity.ok(msg);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private ResponseEntity saveUser(User user, UserData newData, Principal p, HttpServletResponse response, boolean add)
            throws ServletException {
        try {
            if (user != null) {
                user.setScreenName(newData.user.getScreenName().trim());
                user.setEmailAddress(newData.user.getEmailAddress().trim());

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
                    if (newData.credentials != null) {
                        if (!StringUtils.isEmpty(newData.credentials.getPasswordText())) {
                            userManager.updateCredentials(user.getId(), newData.credentials.getPasswordText());
                        }
                        // reset MFA secret if requested
                        if (newData.credentials.isEraseMfaSecret()) {
                            userManager.eraseMFASecret(user.getId());
                        }
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (RollbackException e) {
                    return ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body("Persistence Problem");
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UserData data = new UserData();
            data.setUser(user);
            UserCredentials creds = userManager.getCredentialsByUserName(user.getUserName());
            data.setCredentials(creds);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error updating user", e);
            throw new ServletException(e.getMessage());
        }
    }

    private ValidationError advancedValidate(User currentUser, UserData data, boolean isAdd, Locale locale) {
        BindException be = new BindException(data, "new data object");

        UserSearchCriteria usc1 = new UserSearchCriteria();
        usc1.setUserName(data.user.getUserName());
        List<User> users = userManager.getUsers(usc1);
        if (users.size() > 1 || (users.size() == 1 && !users.get(0).getId().equals(data.user.getId()))) {
            be.addError(new ObjectError("User object", messages.getMessage("error.add.user.userNameInUse",
                    null, locale)));
        }

        UserSearchCriteria usc2 = new UserSearchCriteria();
        usc2.setScreenName(data.user.getScreenName());
        users = userManager.getUsers(usc2);
        if (users.size() > 1 || (users.size() == 1 && !users.get(0).getId().equals(data.user.getId()))) {
            be.addError(new ObjectError("User object", messages.getMessage("error.add.user.screenNameInUse",
                    null, locale)));
        }

        if (currentUser != null) {
            UserStatus currentStatus = currentUser.getStatus();
            if (currentStatus != data.getUser().getStatus()) {
                switch (currentStatus) {
                    case ENABLED:
                        if (data.getUser().getStatus() != UserStatus.DISABLED) {
                            be.addError(new ObjectError("User object",
                                    messages.getMessage("error.useradmin.enabled.only.disabled", null, locale)));
                        }
                        break;
                    case DISABLED:
                        if (data.getUser().getStatus() != UserStatus.ENABLED) {
                            be.addError(new ObjectError("User object",
                                    messages.getMessage("error.useradmin.disabled.only.enabled", null, locale)));
                        }
                        break;
                    case REGISTERED:
                    case EMAILVERIFIED:
                        if (data.getUser().getStatus() != UserStatus.ENABLED) {
                            be.addError(new ObjectError("User object",
                                    messages.getMessage("error.useradmin.nonenabled.only.enabled", null, locale)));
                        }
                        break;
                    default:
                }
            }
        }

        if (data.credentials != null) {
            String maybePassword = data.credentials.getPasswordText();
            if (!StringUtils.isEmpty(maybePassword)) {
                if (!maybePassword.equals(data.credentials.getPasswordConfirm())) {
                    be.addError(new ObjectError("User object",
                            messages.getMessage("error.add.user.passwordConfirmFail", null, locale)));
                } else {
                    if (!pattern.matcher(maybePassword).matches()) {
                        be.addError(new ObjectError("User object",
                                messages.getMessage("error.add.user.passwordComplexityFail", null, locale)));
                    }
                }
            } else {
                if (!StringUtils.isEmpty(data.credentials.getPasswordConfirm())) {
                    // confirm provided but password field itself not filled out
                    be.addError(new ObjectError("User object",
                            messages.getMessage("error.add.user.passwordConfirmFail", null, locale)));
                }
            }

            if (isAdd && StringUtils.isEmpty(data.credentials.getPasswordText())) {
                be.addError(new ObjectError("User object",
                        messages.getMessage("error.add.user.missingPassword", null, locale)));
            }
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}/weblogs")
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

    @GetMapping(value = "/tb-ui/authoring/rest/loggedinuser/weblogs")
    public List<UserWeblogRole> getLoggedInUsersWeblogs(Principal p, HttpServletResponse response)
            throws ServletException {
        User user = userManager.getEnabledUserByUserName(p.getName());
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        return getUsersWeblogs(user.getId(), response);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/user/{userId}/role/{role}/invite", produces = "text/plain")
    public ResponseEntity inviteUser(@PathVariable String weblogId, @PathVariable String userId,
                                     @PathVariable WeblogRole role, Principal p, Locale locale) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        User invitee = userManager.getUser(userId);
        User invitor = userManager.getEnabledUserByUserName(p.getName());

        if (weblog != null && invitee != null && invitor != null &&
                userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

            UserWeblogRole roleChk = userManager.getWeblogRoleIncludingPending(invitee, weblog);
            if (roleChk != null) {
                return ResponseEntity.badRequest().body(messages.getMessage(roleChk.isPending() ?
                        "members.userAlreadyInvited" : "members.userAlreadyMember", null, locale));
            }
            userManager.grantWeblogRole(invitee, weblog, role, true);
            persistenceStrategy.flush();
            mailManager.sendWeblogInvitation(invitee, weblog);
            return ResponseEntity.ok(messages.getMessage("members.userInvited", null, locale));
        } else {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogrole/{id}/attach")
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

    @PostMapping(value = "/tb-ui/authoring/rest/weblogrole/{id}/detach")
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

    @GetMapping(value = "/tb-ui/register/rest/useradminmetadata")
    public UserAdminMetadata getUserAdminMetadata() {
        UserAdminMetadata metadata = new UserAdminMetadata();

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
