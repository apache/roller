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
package org.tightblog.bloggerui.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.bloggerui.model.SuccessResponse;
import org.tightblog.bloggerui.model.UserAdminMetadata;
import org.tightblog.bloggerui.model.UserData;
import org.tightblog.bloggerui.model.Violation;
import org.tightblog.service.EmailService;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.UserCredentials;
import org.tightblog.domain.UserStatus;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.tightblog.dao.UserCredentialsDao;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.UserWeblogRoleDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.Utilities;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.domain.WebloggerProperties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
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

    private static final Pattern PWD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$");

    private WeblogDao weblogDao;
    private UserManager userManager;
    private UserWeblogRoleDao userWeblogRoleDao;
    private UserDao userDao;
    private UserCredentialsDao userCredentialsDao;
    private EmailService emailService;
    private MessageSource messages;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private WeblogEntryCommentDao weblogEntryCommentDao;
    private URLService urlService;
    private UserAdminMetadata userAdminMetadata = new UserAdminMetadata();

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserController(WeblogDao weblogDao, UserManager userManager,
                          UserWeblogRoleDao userWeblogRoleDao, MessageSource messageSource,
                          EmailService emailService, UserDao userDao,
                          UserCredentialsDao userCredentialsDao, URLService urlService,
                          WeblogEntryCommentDao weblogEntryCommentDao,
                          WebloggerPropertiesDao webloggerPropertiesDao) {
        this.weblogDao = weblogDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.userManager = userManager;
        this.userWeblogRoleDao = userWeblogRoleDao;
        this.userDao = userDao;
        this.userCredentialsDao = userCredentialsDao;
        this.weblogEntryCommentDao = weblogEntryCommentDao;
        this.urlService = urlService;
        this.emailService = emailService;
        this.messages = messageSource;
    }

    @PostConstruct
    public void init() {
        userAdminMetadata.getUserStatuses().putAll(Arrays.stream(UserStatus.values())
                .collect(Utilities.toLinkedHashMap(UserStatus::name, UserStatus::name)));

        userAdminMetadata.getGlobalRoles().putAll(Arrays.stream(GlobalRole.values())
                .filter(r -> r != GlobalRole.NOAUTHNEEDED)
                .collect(Utilities.toLinkedHashMap(GlobalRole::name, GlobalRole::name)));
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/userlist")
    public Map<String, String> getUserEditList() {
        return createUserMap(userDao.findAll());
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval")
    public List<User> getRegistrationsNeedingApproval() {
        return userDao.findUsersToApprove();
    }

    @PostMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval/{id}/approve")
    public void approveRegistration(@PathVariable String id, HttpServletResponse response) {
        User acceptedUser = userDao.findByIdOrNull(id);
        if (acceptedUser != null) {
            if (!UserStatus.ENABLED.equals(acceptedUser.getStatus())) {
                acceptedUser.setStatus(UserStatus.ENABLED);
                userDao.saveAndFlush(acceptedUser);
                userDao.evictUser(acceptedUser);
                emailService.sendRegistrationApprovedNotice(acceptedUser);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping(value = "/tb-ui/admin/rest/useradmin/registrationapproval/{id}/reject")
    public void rejectRegistration(@PathVariable String id, HttpServletResponse response) {
        User rejectedUser = userDao.findByIdOrNull(id);
        if (rejectedUser != null) {
            emailService.sendRegistrationRejectedNotice(rejectedUser);
            userManager.removeUser(rejectedUser);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/potentialmembers")
    public Map<String, String> getPotentialNewBlogMembers(@PathVariable String weblogId, Principal p,
                                                          HttpServletResponse response) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
            // member list excludes inactive accounts
            List<User> potentialUsers = userDao.findByStatusEnabled();

            // filter out people already members
            ListIterator<User> potentialIter = potentialUsers.listIterator();
            List<UserWeblogRole> currentUserList = userWeblogRoleDao.findByWeblog(weblog);
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
        return userMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}")
    public UserData getUserData(@PathVariable String id, HttpServletResponse response) {
        User user = userDao.findByIdOrNull(id);

        if (user != null) {
            UserData data = new UserData();
            UserCredentials creds = userCredentialsDao.findByUserName(user.getUserName());
            data.setUser(user);
            data.setCredentials(creds);
            return data;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @GetMapping(value = "/tb-ui/authoring/rest/userprofile/{id}")
    public User getProfileData(@PathVariable String id, Principal p, HttpServletResponse response) {
        User user = userDao.findByIdOrNull(id);
        User authenticatedUser = userDao.findEnabledByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            return user;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @PostMapping(value = "/tb-ui/register/rest/registeruser")
    public ResponseEntity registerUser(@Valid @RequestBody UserData newData, Locale locale, HttpServletResponse response) {
        List<Violation> errors = validateUser(null, newData, true, locale);
        if (errors.size() > 0) {
            return ValidationErrorResponse.badRequest(errors);
        }

        long userCount = userDao.count();
        WebloggerProperties.RegistrationPolicy option = webloggerPropertiesDao.findOrNull().getRegistrationPolicy();
        if (userCount == 0 || !WebloggerProperties.RegistrationPolicy.DISABLED.equals(option)) {
            boolean mustActivate = userCount > 0;
            if (mustActivate) {
                newData.getUser().setActivationCode(UUID.randomUUID().toString());
                newData.getUser().setStatus(UserStatus.REGISTERED);
            } else {
                // initial user is the Admin, is automatically enabled.
                newData.getUser().setStatus(UserStatus.ENABLED);
            }

            User user = new User();
            user.setUserName(newData.getUser().getUserName());
            user.setDateCreated(Instant.now());

            ResponseEntity re = saveUser(user, newData, null, response, true);

            if (re.getStatusCode() == HttpStatus.OK && mustActivate) {
                UserData data = (UserData) re.getBody();
                if (data != null) {
                    emailService.sendUserActivationEmail(data.getUser());
                }
            }
            return re;
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/userprofile/{id}")
    public ResponseEntity updateUserProfile(@PathVariable String id, @Valid @RequestBody UserData newData, Principal p,
                                            Locale locale, HttpServletResponse response) {
        User user = userDao.findByIdOrNull(id);
        User authenticatedUser = userDao.findEnabledByUserName(p.getName());

        if (user != null && user.getId().equals(authenticatedUser.getId())) {
            List<Violation> errors = validateUser(null, newData, false, locale);
            if (errors.size() > 0) {
                return ValidationErrorResponse.badRequest(errors);
            }
            return saveUser(user, newData, p, response, false);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}")
    public ResponseEntity updateUser(@PathVariable String id, @Valid @RequestBody UserData newData, Principal p,
                                     Locale locale, HttpServletResponse response) {
        User user = userDao.findByIdOrNull(id);
        List<Violation> errors = validateUser(user, newData, false, locale);
        if (errors.size() > 0) {
            return ValidationErrorResponse.badRequest(errors);
        }
        return saveUser(user, newData, p, response, false);
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/members")
    public List<UserWeblogRole> getWeblogMembers(@PathVariable String weblogId, Principal p, HttpServletResponse response) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
            return userWeblogRoleDao.findByWeblog(weblog);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/user/{userId}/role/{role}/attach")
    public ResponseEntity addUserToWeblog(@PathVariable String weblogId, @PathVariable String userId,
                                          @PathVariable WeblogRole role, Principal p, Locale locale) {

        User requestor = userDao.findEnabledByUserName(p.getName());
        User newMember = userDao.findByIdOrNull(userId);
        Weblog weblog = weblogDao.findById(weblogId).orElse(null);

        if (weblog != null && newMember != null && requestor != null &&
                requestor.hasEffectiveGlobalRole(GlobalRole.ADMIN)) {
            userManager.grantWeblogRole(newMember, weblog, role);
            return SuccessResponse.textMessage(messages.getMessage("members.userAdded", null, locale));
        } else {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).build();
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/memberupdate")
    public ResponseEntity updateWeblogMembership(@PathVariable String weblogId, Principal p, Locale locale,
                                                 @RequestBody List<UserWeblogRole> uwrs) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        User user = userDao.findEnabledByUserName(p.getName());
        if (user != null && weblog != null && user.hasEffectiveGlobalRole(GlobalRole.ADMIN)) {

            // must remain at least one admin
            List<UserWeblogRole> owners = uwrs.stream()
                    .filter(r -> r.getWeblogRole().equals(WeblogRole.OWNER))
                    .collect(Collectors.toList());
            if (owners.size() < 1) {
                return ValidationErrorResponse.badRequest(
                        messages.getMessage("members.oneAdminRequired", null, locale));
            }

            // one iteration for each line (user) in the members table
            for (UserWeblogRole uwr : uwrs) {
                if (WeblogRole.NOBLOGNEEDED.equals(uwr.getWeblogRole())) {
                    userManager.deleteUserWeblogRole(uwr);
                } else {
                    userManager.grantWeblogRole(
                            uwr.getUser(), uwr.getWeblog(), uwr.getWeblogRole());
                }
            }
            return SuccessResponse.textMessage(
                    messages.getMessage("members.membersChanged", null, locale));
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private ResponseEntity saveUser(User user, UserData newData, Principal p, HttpServletResponse response, boolean add) {

        if (user != null) {
            user.setScreenName(newData.getUser().getScreenName().trim());
            user.setEmailAddress(newData.getUser().getEmailAddress().trim());

            if (!UserStatus.ENABLED.equals(user.getStatus()) && StringUtils.isNotEmpty(
                    newData.getUser().getActivationCode())) {
                user.setActivationCode(newData.getUser().getActivationCode());
            }

            if (add) {
                user.setStatus(newData.getUser().getStatus());
                if (userDao.count() == 0) {
                    // first person in is always an admin
                    user.setGlobalRole(GlobalRole.ADMIN);
                } else {
                    user.setGlobalRole(webloggerPropertiesDao.findOrNull().isUsersCreateBlogs() ?
                            GlobalRole.BLOGCREATOR : GlobalRole.BLOGGER);
                }
            } else {
                // users can't alter own roles or status
                if (!user.getUserName().equals(p.getName())) {
                    user.setGlobalRole(newData.getUser().getGlobalRole());
                    user.setStatus(newData.getUser().getStatus());
                }
            }

            try {
                userDao.saveAndFlush(user);
                userDao.evictUser(user);
                // reset password if set
                if (newData.getCredentials() != null) {
                    UserCredentials credentials = newData.getCredentials();

                    if (!StringUtils.isEmpty(credentials.getPasswordText())) {
                        userManager.updateCredentials(user.getId(), credentials.getPasswordText());
                    }
                    // reset MFA secret if requested
                    if (credentials.isEraseMfaSecret()) {
                        userCredentialsDao.eraseMfaCode(user.getId());
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
        UserCredentials creds = userCredentialsDao.findByUserName(user.getUserName());
        data.setCredentials(creds);
        return ResponseEntity.ok(data);
    }

    private List<Violation> validateUser(User currentUser, UserData data, boolean isAdd, Locale locale) {
        List<Violation> errors = new ArrayList<>();

        User testHasUserName = userDao.findByUserName(data.getUser().getUserName());
        if (testHasUserName != null && !testHasUserName.getId().equals(data.getUser().getId())) {
            errors.add(new Violation(messages.getMessage("error.add.user.userNameInUse",
                    null, locale)));
        }

        User testHasScreenName = userDao.findByScreenName(data.getUser().getScreenName());
        if (testHasScreenName != null && !testHasScreenName.getId().equals(data.getUser().getId())) {
            errors.add(new Violation(messages.getMessage("error.add.user.screenNameInUse",
                    null, locale)));
        }

        if (currentUser != null) {
            UserStatus currentStatus = currentUser.getStatus();
            if (currentStatus != data.getUser().getStatus()) {
                switch (currentStatus) {
                    case ENABLED:
                        if (data.getUser().getStatus() != UserStatus.DISABLED) {
                            errors.add(new Violation(messages.getMessage(
                                    "error.useradmin.enabled.only.disabled", null, locale)));
                        }
                        break;
                    case DISABLED:
                        if (data.getUser().getStatus() != UserStatus.ENABLED) {
                            errors.add(new Violation(messages.getMessage(
                                    "error.useradmin.disabled.only.enabled", null, locale)));
                        }
                        break;
                    case REGISTERED:
                    case EMAILVERIFIED:
                        if (data.getUser().getStatus() != UserStatus.ENABLED) {
                            errors.add(new Violation(messages.getMessage(
                                    "error.useradmin.nonenabled.only.enabled", null, locale)));
                        }
                        break;
                    default:
                }
            }
        }

        if (data.getCredentials() != null) {
            UserCredentials credentials = data.getCredentials();
            String maybePassword = credentials.getPasswordText();
            if (!StringUtils.isEmpty(maybePassword)) {
                if (!maybePassword.equals(credentials.getPasswordConfirm())) {
                    errors.add(new Violation(messages.getMessage(
                            "error.add.user.passwordConfirmFail", null, locale)));
                } else {
                    if (!PWD_PATTERN.matcher(maybePassword).matches()) {
                        errors.add(new Violation(messages.getMessage(
                                "error.add.user.passwordComplexityFail", null, locale)));
                    }
                }
            } else {
                if (!StringUtils.isEmpty(credentials.getPasswordConfirm())) {
                    // confirm provided but password field itself not filled out
                    errors.add(new Violation(
                            messages.getMessage("error.add.user.passwordConfirmFail", null, locale)));
                }
            }

            if (isAdd && StringUtils.isEmpty(credentials.getPasswordText())) {
                errors.add(new Violation(
                        messages.getMessage("error.add.user.missingPassword", null, locale)));
            }
        }

        return errors;
    }

    @GetMapping(value = "/tb-ui/admin/rest/useradmin/user/{id}/weblogs")
    public List<UserWeblogRole> getUsersWeblogs(@PathVariable String id, HttpServletResponse response) {
        User user = userDao.findByIdOrNull(id);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        List<UserWeblogRole> uwrs = userWeblogRoleDao.findByUser(user);
        for (UserWeblogRole uwr : uwrs) {
            entityManager.detach(uwr); // uwr now a DTO
            uwr.getWeblog().setAbsoluteURL(urlService.getWeblogURL(uwr.getWeblog()));
            uwr.setUser(null);
        }
        return uwrs;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/loggedinuser/weblogs")
    public List<UserWeblogRole> getLoggedInUsersWeblogs(Principal p, HttpServletResponse response) {
        User user = userDao.findEnabledByUserName(p.getName());
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        List<UserWeblogRole> uwrs = userWeblogRoleDao.findByUser(user);
        for (UserWeblogRole uwr : uwrs) {
            entityManager.detach(uwr); // uwr now a DTO
            uwr.getWeblog().setAbsoluteURL(urlService.getWeblogURL(uwr.getWeblog()));
            uwr.getWeblog().setUnapprovedComments(
                    weblogEntryCommentDao.countByWeblogAndStatusUnapproved(uwr.getWeblog()));
            uwr.setUser(null);
        }

        return uwrs;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogrole/{id}/emails/{emailComments}")
    public void setEmailCommentsForWeblog(@PathVariable String id, @PathVariable boolean emailComments, Principal p,
                                       HttpServletResponse response) {
        UserWeblogRole uwr = userWeblogRoleDao.findByIdOrNull(id);
        if (uwr != null && uwr.getUser().getUserName().equals(p.getName())) {
            uwr.setEmailComments(emailComments);
            userWeblogRoleDao.saveAndFlush(uwr);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogrole/{id}/detach")
    public void resignFromWeblog(@PathVariable String id, Principal p, HttpServletResponse response) {
        UserWeblogRole uwr = userWeblogRoleDao.findByIdOrNull(id);
        if (uwr != null && uwr.getUser().getUserName().equals(p.getName())) {
            userManager.deleteUserWeblogRole(uwr);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping(value = "/tb-ui/register/rest/useradminmetadata")
    public UserAdminMetadata getUserAdminMetadata() {
        return userAdminMetadata;
    }

}
