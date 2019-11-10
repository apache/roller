/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.bloggerui.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.WebAttributes;
import org.tightblog.service.EmailService;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.UserStatus;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.UserWeblogRoleDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.bloggerui.model.Menu;
import org.tightblog.service.MenuService;
import org.tightblog.security.MultiFactorAuthenticationProvider.InvalidVerificationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/tb-ui/app")
public class UIController {

    private WeblogDao weblogDao;
    private UserManager userManager;
    private UserDao userDao;
    private UserWeblogRoleDao userWeblogRoleDao;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private WeblogEntryManager weblogEntryManager;
    private EmailService emailService;
    private MenuService menuHelper;
    private MessageSource messages;
    private URLService urlService;

    @Autowired
    public UIController(WeblogDao weblogDao, UserManager userManager, UserDao userDao,
                        WeblogEntryManager weblogEntryManager, UserWeblogRoleDao userWeblogRoleDao,
                        EmailService emailService, MenuService menuHelper, MessageSource messages,
                        WebloggerPropertiesDao webloggerPropertiesDao, URLService urlService) {
        this.weblogDao = weblogDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.userManager = userManager;
        this.userDao = userDao;
        this.userWeblogRoleDao = userWeblogRoleDao;
        this.weblogEntryManager = weblogEntryManager;
        this.urlService = urlService;
        this.emailService = emailService;
        this.menuHelper = menuHelper;
        this.messages = messages;
    }

    @Value("${mfa.enabled:true}")
    private boolean mfaEnabled;

    @Value("${weblogger.version}")
    private String tightblogVersion;

    @Value("${weblogger.revision}")
    private String tightblogRevision;

    @Value("${media.file.showTab}")
    boolean showMediaFileTab;

    @RequestMapping(value = "/login")
    public ModelAndView login(@RequestParam(required = false) String activationCode,
                              @RequestParam(required = false) Boolean error,
                              HttpServletRequest request) {

        Map<String, Object> myMap = new HashMap<>();

        if (Boolean.TRUE.equals(error)) {
            Object maybeError = request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            String errorMessage;
            if (maybeError instanceof InvalidVerificationCodeException) {
                errorMessage = messages.getMessage("login.invalidAuthenticatorCode", null, request.getLocale());
            } else {
                errorMessage = messages.getMessage("error.password.mismatch", null, request.getLocale());
            }
            myMap.put("actionError", errorMessage);
        } else if (activationCode != null) {
            User user = userDao.findByActivationCode(activationCode);

            if (user != null) {
                user.setActivationCode(null);
                user.setStatus(UserStatus.EMAILVERIFIED);
                myMap.put("actionMessage", messages.getMessage("welcome.user.account.need.approval", null,
                        request.getLocale()));
                userDao.saveAndFlush(user);
                emailService.sendRegistrationApprovalRequest(user);
            } else {
                myMap.put("actionError", messages.getMessage("error.activate.user.invalidActivationCode", null,
                        request.getLocale()));
            }

        }
        return tightblogModelAndView("login", myMap, null, null);
    }

    @RequestMapping(value = "/unsubscribe")
    public ModelAndView unsubscribe(@RequestParam String commentId) {

        Pair<String, Boolean> results = weblogEntryManager.stopNotificationsForCommenter(commentId);
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("found", results.getRight());
        myMap.put("weblogEntryTitle", results.getLeft());

        return tightblogModelAndView("unsubscribed", myMap, null, null);
    }

    @RequestMapping(value = "/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        response.sendRedirect(request.getContextPath() + "/");
    }

    @RequestMapping(value = "/relogin")
    public void logoutAndLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        response.sendRedirect(request.getContextPath() + "/tb-ui/app/login-redirect");
    }

    @RequestMapping(value = "/login-redirect")
    public void loginRedirect(Principal principal, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (principal == null) {
            // trigger call to login page
            response.sendRedirect(request.getContextPath() + "/tb-ui/app/home");
        } else {
            User user = userDao.findEnabledByUserName(principal.getName());

            if (mfaEnabled && ((UsernamePasswordAuthenticationToken) principal).getAuthorities().stream().anyMatch(
                            role -> GlobalRole.MISSING_MFA_SECRET.name().equals(role.getAuthority()))) {
                response.sendRedirect(request.getContextPath() + "/tb-ui/app/scanCode");
            } else if (!GlobalRole.ADMIN.equals(user.getGlobalRole())) {
                response.sendRedirect(request.getContextPath() + "/tb-ui/app/home");
            } else {
                List<UserWeblogRole> roles = userWeblogRoleDao.findByUser(user);

                if (roles.size() > 0) {
                    response.sendRedirect(request.getContextPath() + "/tb-ui/app/home");
                } else {
                    // admin has no blog yet, possibly initial setup.
                    response.sendRedirect(request.getContextPath() + "/tb-ui/app/admin/globalConfig");
                }
            }
        }
    }

    @RequestMapping(value = "/scanCode")
    public ModelAndView scanAuthenticatorSecret(Principal principal, HttpServletRequest request,
                                                HttpServletResponse response) {

        User user = userDao.findEnabledByUserName(principal.getName());
        String qrCode = userManager.generateMFAQRUrl(user);
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("qrCode", qrCode);

        return tightblogModelAndView("scanCode", myMap, null, null);
    }

    @RequestMapping(value = "/get-default-blog")
    public void getDefaultBlog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Weblog defaultBlog = webloggerPropertiesDao.findOrNull().getMainBlog();
        String path;

        if (defaultBlog != null) {
            path = '/' + defaultBlog.getHandle();
        } else {
            // new install?  Redirect to register or login page based on whether a user has already been created.
            long userCount = userDao.count();
            if (userCount == 0) {
                path = "/tb-ui/app/register";
            } else {
                path = "/tb-ui/app/login-redirect";
            }
        }

        String redirect = request.getContextPath() + path;
        response.sendRedirect(redirect);
    }

    @RequestMapping(value = "/admin/cachedData")
    public ModelAndView cachedData(Principal principal) {
        return getAdminPage(principal, "cachedData", null);
    }

    @RequestMapping(value = "/admin/globalConfig")
    public ModelAndView globalConfig(Principal principal) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("showMediaFileTab", showMediaFileTab);
        return getAdminPage(principal, "globalConfig", myMap);
    }

    @RequestMapping(value = "/admin/userAdmin")
    public ModelAndView userAdmin(Principal principal) {
        return getAdminPage(principal, "userAdmin", null);
    }

    @RequestMapping(value = "/profile")
    public ModelAndView profile(Principal principal) {
        return tightblogModelAndView("profile", null, principal);
    }

    @RequestMapping(value = "/register")
    public ModelAndView register() {
        return tightblogModelAndView("register", null, null, null);
    }

    @RequestMapping(value = "/createWeblog")
    public ModelAndView createWeblog(Principal principal) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("globalCommentPolicy", webloggerPropertiesDao.findOrNull().getCommentPolicy());
        return tightblogModelAndView("createWeblog", myMap, principal);
    }

    @RequestMapping(value = "/authoring/weblogConfig")
    public ModelAndView weblogConfig(Principal principal, @RequestParam String weblogId) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("globalCommentPolicy", webloggerPropertiesDao.findOrNull().getCommentPolicy());
        return getBlogOwnerPage(principal, myMap, weblogId, "weblogConfig");
    }

    @RequestMapping(value = "/authoring/themeEdit")
    public ModelAndView themeEdit(Principal principal, @RequestParam String weblogId) {
        return getBlogOwnerPage(principal, null, weblogId, "themeEdit");
    }

    @RequestMapping(value = "/authoring/templates")
    public ModelAndView templates(Principal principal, @RequestParam String weblogId) {
        return getBlogOwnerPage(principal, null, weblogId, "templates");
    }

    @RequestMapping(value = "/authoring/members")
    public ModelAndView memberAdmin(Principal principal, @RequestParam String weblogId) {
        return getBlogOwnerPage(principal, null, weblogId, "members");
    }

    @RequestMapping(value = "/authoring/blogroll")
    public ModelAndView blogroll(Principal principal, @RequestParam String weblogId) {
        return getBlogOwnerPage(principal, null, weblogId, "blogroll");
    }

    @RequestMapping(value = "/authoring/categories")
    public ModelAndView categories(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "categories");
    }

    @RequestMapping(value = "/authoring/templateEdit")
    public ModelAndView templateEdit(Principal principal, @RequestParam String weblogId) {
        return getBlogOwnerPage(principal, null, weblogId, "templateEdit");
    }

    @RequestMapping(value = "/authoring/mediaFileChooser")
    public ModelAndView mediaFileChooser(Principal principal, @RequestParam String weblogId) {
        return getBlogContributorPage(principal, null, weblogId, "mediaFileChooser");
    }

    @RequestMapping(value = "/authoring/mediaFileView")
    public ModelAndView mediaFileView(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "mediaFileView");
    }

    @RequestMapping(value = "/authoring/mediaFileAdd")
    public ModelAndView mediaFileAdd(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "mediaFileAdd");
    }

    @RequestMapping(value = "/authoring/mediaFileEdit")
    public ModelAndView mediaFileEdit(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "mediaFileEdit");
    }

    @RequestMapping(value = "/authoring/entryAdd")
    public ModelAndView entryAdd(Principal principal, @RequestParam String weblogId) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("showMediaFileTab", showMediaFileTab);
        return getBlogContributorPage(principal, myMap, weblogId, "entryAdd");
    }

    @RequestMapping(value = "/authoring/entryEdit")
    public ModelAndView entryEdit(Principal principal, @RequestParam String weblogId) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("showMediaFileTab", showMediaFileTab);
        return getBlogContributorPage(principal, myMap, weblogId, "entryEdit");
    }

    @RequestMapping(value = "/authoring/entries")
    public ModelAndView entries(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "entries");
    }

    @RequestMapping(value = "/authoring/comments")
    public ModelAndView comments(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "comments");
    }

    @RequestMapping(value = "/authoring/tags")
    public ModelAndView tags(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, weblogId, "tags");
    }

    private ModelAndView getBlogOwnerPage(Principal principal, Map<String, Object> map, String weblogId, String actionName) {
        return getBlogPage(principal, map, weblogId, actionName, WeblogRole.OWNER);
    }

    private ModelAndView getBlogPublisherPage(Principal principal, String weblogId, String actionName) {
        return getBlogPage(principal, null, weblogId, actionName, WeblogRole.POST);
    }

    private ModelAndView getBlogContributorPage(Principal principal, Map<String, Object> map, String weblogId,
                                                String actionName) {
        return getBlogPage(principal, map, weblogId, actionName, WeblogRole.EDIT_DRAFT);
    }

    private ModelAndView getBlogPage(Principal principal, Map<String, Object> map, String weblogId, String actionName,
                                     WeblogRole requiredRole) {
        User user = userDao.findEnabledByUserName(principal.getName());
        Weblog weblog = weblogDao.findById(weblogId).orElse(null);

        boolean isAdmin = user.hasEffectiveGlobalRole(GlobalRole.ADMIN);
        UserWeblogRole weblogRole = userWeblogRoleDao.findByUserAndWeblog(user, weblog);
        if (isAdmin || (weblogRole != null && weblogRole.hasEffectiveWeblogRole(requiredRole))) {
            if (map == null) {
                map = new HashMap<>();
            }

            WeblogRole menuRole = isAdmin ? WeblogRole.OWNER : weblogRole.getWeblogRole();
            map.put("menu", getMenu(user, actionName, menuRole));
            map.put("weblogId", weblogId);
            return tightblogModelAndView(actionName, map, user, weblog);
        } else {
            return tightblogModelAndView("denied", null, null, null);
        }
    }

    @RequestMapping(value = "/home")
    public ModelAndView home(Principal principal) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("usersCustomizeThemes", webloggerPropertiesDao.findOrNull().isUsersCustomizeThemes());
        return tightblogModelAndView("mainMenu", myMap, principal);
    }

    private ModelAndView getAdminPage(Principal principal, String actionName, Map<String, Object> propertyMap) {
        User user = userDao.findEnabledByUserName(principal.getName());
        Map<String, Object> myMap = (propertyMap == null) ? new HashMap<>() : propertyMap;
        myMap.put("menu", getMenu(user, actionName, WeblogRole.NOBLOGNEEDED));
        return tightblogModelAndView(actionName, myMap, user, null);
    }

    private ModelAndView tightblogModelAndView(String actionName, Map<String, Object> map, Principal principal) {
        User user = userDao.findEnabledByUserName(principal.getName());
        return tightblogModelAndView(actionName, map, user, null);
    }

    private ModelAndView tightblogModelAndView(String actionName, Map<String, Object> map, User user, Weblog weblog) {
        if (map == null) {
            map = new HashMap<>();
        }
        map.put("authenticatedUser", user);
        map.put("actionWeblog", weblog);
        if (weblog != null) {
            map.put("actionWeblogURL", urlService.getWeblogURL(weblog));
        }
        map.put("userIsAdmin", user != null && GlobalRole.ADMIN.equals(user.getGlobalRole()));
        map.put("pageTitleKey", actionName + ".title");
        map.put("mfaEnabled", mfaEnabled);
        map.put("tightblogVersion", tightblogVersion);
        map.put("tightblogRevision", tightblogRevision);
        map.put("registrationPolicy", webloggerPropertiesDao.findOrNull().getRegistrationPolicy());
        return new ModelAndView("." + actionName, map);
    }

    private Menu getMenu(User user, String actionName, WeblogRole requiredRole) {
        return menuHelper.getMenu(user.getGlobalRole(), requiredRole, actionName);
    }

}
