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
package org.tightblog.ui.restapi;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.WebAttributes;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.MailManager;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserSearchCriteria;
import org.tightblog.pojos.UserStatus;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.ui.menu.Menu;
import org.tightblog.ui.menu.MenuHelper;
import org.tightblog.ui.security.MultiFactorAuthenticationProvider.InvalidVerificationCodeException;
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
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
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
    private MenuHelper menuHelper;

    public void setMenuHelper(MenuHelper menuHelper) {
        this.menuHelper = menuHelper;
    }

    @Autowired
    private MessageSource messages;

    @Value("${mfa.enabled:true}")
    private boolean mfaEnabled;

    @Value("${weblogger.version}")
    private String tightblogVersion;

    @Value("${weblogger.revision}")
    private String tightblogRevision;

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
            UserSearchCriteria usc = new UserSearchCriteria();
            usc.setActivationCode(activationCode);
            List<User> users = userManager.getUsers(usc);

            if (users.size() == 1) {
                User user = users.get(0);
                user.setActivationCode(null);
                user.setStatus(UserStatus.EMAILVERIFIED);
                myMap.put("actionMessage", messages.getMessage("welcome.user.account.need.approval", null,
                        request.getLocale()));
                userManager.saveUser(user);
                mailManager.sendRegistrationApprovalRequest(user);
            } else {
                myMap.put("actionError", messages.getMessage("error.activate.user.invalidActivationCode", null,
                        request.getLocale()));
            }

        }
        return tightblogModelAndView("login", myMap, (User) null, null);
    }

    @RequestMapping(value = "/unsubscribe")
    public ModelAndView unsubscribe(@RequestParam String commentId) throws IOException {

        Pair<String, Boolean> results = weblogEntryManager.stopNotificationsForCommenter(commentId);
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("found", results.getRight());
        myMap.put("weblogEntryTitle", results.getLeft());

        return tightblogModelAndView("unsubscribed", myMap, (User) null, null);
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
            User user = userManager.getEnabledUserByUserName(principal.getName());

            if (mfaEnabled && ((UsernamePasswordAuthenticationToken) principal).getAuthorities().stream().anyMatch(
                            role -> GlobalRole.MISSING_MFA_SECRET.name().equals(role.getAuthority()))) {
                response.sendRedirect(request.getContextPath() + "/tb-ui/app/scanCode");
            } else if (!GlobalRole.ADMIN.equals(user.getGlobalRole())) {
                response.sendRedirect(request.getContextPath() + "/tb-ui/app/home");
            } else {
                List<UserWeblogRole> roles = userManager.getWeblogRoles(user);

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
    public ModelAndView scanAuthenticatorSecret(Principal principal, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = userManager.getEnabledUserByUserName(principal.getName());
        String qrCode = userManager.generateMFAQRUrl(user);
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("qrCode", qrCode);

        return tightblogModelAndView("scanCode", myMap, (User) null, null);
    }

    @RequestMapping(value = "/get-default-blog")
    public void getDefaultBlog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Weblog defaultBlog = persistenceStrategy.getWebloggerProperties().getMainBlog();
        String path;

        if (defaultBlog != null) {
            path = '/' + defaultBlog.getHandle();
        } else {
            // new install?  Redirect to register or login page based on whether a user has already been created.
            long userCount = userManager.getUserCount();
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
        return getAdminPage(principal, "cachedData");
    }

    @RequestMapping(value = "/admin/globalConfig")
    public ModelAndView globalConfig(Principal principal) {
        return getAdminPage(principal, "globalConfig");
    }

    @RequestMapping(value = "/admin/userAdmin")
    public ModelAndView userAdmin(Principal principal) {
        return getAdminPage(principal, "userAdmin");
    }

    @RequestMapping(value = "/profile")
    public ModelAndView profile(Principal principal) {
        return tightblogModelAndView("profile", null, principal, null);
    }

    @RequestMapping(value = "/register")
    public ModelAndView register() {
        return tightblogModelAndView("register", null, (User) null, null);
    }

    @RequestMapping(value = "/createWeblog")
    public ModelAndView createWeblog(Principal principal) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("globalCommentPolicy", persistenceStrategy.getWebloggerProperties().getCommentPolicy());
        return tightblogModelAndView("createWeblog", myMap, principal, null);
    }

    @RequestMapping(value = "/authoring/weblogConfig")
    public ModelAndView weblogConfig(Principal principal, @RequestParam String weblogId) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("globalCommentPolicy", persistenceStrategy.getWebloggerProperties().getCommentPolicy());
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

    @RequestMapping(value = "/authoring/bookmarks")
    public ModelAndView bookmarks(Principal principal, @RequestParam String weblogId) {
        return getBlogOwnerPage(principal, null, weblogId, "bookmarks");
    }

    @RequestMapping(value = "/authoring/categories")
    public ModelAndView categories(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, null, weblogId, "categories");
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
        return getBlogPublisherPage(principal, null, weblogId, "mediaFileView");
    }

    @RequestMapping(value = "/authoring/mediaFileAdd")
    public ModelAndView mediaFileAdd(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, null, weblogId, "mediaFileAdd");
    }

    @RequestMapping(value = "/authoring/mediaFileEdit")
    public ModelAndView mediaFileEdit(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, null, weblogId, "mediaFileEdit");
    }

    @RequestMapping(value = "/authoring/entryAdd")
    public ModelAndView entryAdd(Principal principal, @RequestParam String weblogId) {
        return getBlogContributorPage(principal, null, weblogId, "entryAdd");
    }

    @RequestMapping(value = "/authoring/entryEdit")
    public ModelAndView entryEdit(Principal principal, @RequestParam String weblogId) {
        return getBlogContributorPage(principal, null, weblogId, "entryEdit");
    }

    @RequestMapping(value = "/authoring/entries")
    public ModelAndView entries(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, null, weblogId, "entries");
    }

    @RequestMapping(value = "/authoring/comments")
    public ModelAndView comments(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, null, weblogId, "comments");
    }

    @RequestMapping(value = "/authoring/tags")
    public ModelAndView tags(Principal principal, @RequestParam String weblogId) {
        return getBlogPublisherPage(principal, null, weblogId, "tags");
    }

    private ModelAndView getBlogOwnerPage(Principal principal, Map<String, Object> map, String weblogId, String actionName) {
        return getBlogPage(principal, map, weblogId, actionName, WeblogRole.OWNER);
    }

    private ModelAndView getBlogPublisherPage(Principal principal, Map<String, Object> map, String weblogId, String actionName) {
        return getBlogPage(principal, map, weblogId, actionName, WeblogRole.POST);
    }

    private ModelAndView getBlogContributorPage(Principal principal, Map<String, Object> map, String weblogId,
                                                String actionName) {
        return getBlogPage(principal, map, weblogId, actionName, WeblogRole.EDIT_DRAFT);
    }

    private ModelAndView getBlogPage(Principal principal, Map<String, Object> map, String weblogId, String actionName,
                                     WeblogRole requiredRole) {
        User user = userManager.getEnabledUserByUserName(principal.getName());
        Weblog weblog = weblogManager.getWeblog(weblogId);

        boolean isAdmin = user.hasEffectiveGlobalRole(GlobalRole.ADMIN);
        UserWeblogRole weblogRole = userManager.getWeblogRole(user, weblog);
        if (isAdmin || (weblogRole != null && weblogRole.hasEffectiveWeblogRole(requiredRole))) {
            if (map == null) {
                map = new HashMap<>();
            }

            WeblogRole menuRole = isAdmin ? WeblogRole.OWNER : weblogRole.getWeblogRole();
            map.put("menu", getMenu(user, actionName, menuRole));
            map.put("weblogId", weblogId);
            return tightblogModelAndView(actionName, map, user, weblog);
        } else {
            return tightblogModelAndView("denied", null, (User) null, null);
        }
    }

    @RequestMapping(value = "/home")
    public ModelAndView home(Principal principal) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("usersCustomizeThemes", persistenceStrategy.getWebloggerProperties().isUsersCustomizeThemes());
        return tightblogModelAndView("mainMenu", myMap, principal, null);
    }

    private ModelAndView getAdminPage(Principal principal, String actionName) {
        User user = userManager.getEnabledUserByUserName(principal.getName());
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("menu", getMenu(user, actionName, WeblogRole.NOBLOGNEEDED));
        return tightblogModelAndView(actionName, myMap, user, null);
    }

    private ModelAndView tightblogModelAndView(String actionName, Map<String, Object> map, Principal principal, Weblog weblog) {
        User user = userManager.getEnabledUserByUserName(principal.getName());
        return tightblogModelAndView(actionName, map, user, weblog);
    }

    private ModelAndView tightblogModelAndView(String actionName, Map<String, Object> map, User user, Weblog weblog) {
        if (map == null) {
            map = new HashMap<>();
        }
        map.put("authenticatedUser", user);
        map.put("actionWeblog", weblog);
        map.put("userIsAdmin", user != null && GlobalRole.ADMIN.equals(user.getGlobalRole()));
        map.put("pageTitleKey", actionName + ".title");
        map.put("mfaEnabled", mfaEnabled);
        map.put("tightblogVersion", tightblogVersion);
        map.put("tightblogRevision", tightblogRevision);
        map.put("registrationPolicy", persistenceStrategy.getWebloggerProperties().getRegistrationPolicy());
        return new ModelAndView("." + actionName, map);
    }

    private Menu getMenu(User user, String actionName, WeblogRole requiredRole) {
        return menuHelper.getMenu(user.getGlobalRole(), requiredRole, actionName);
    }

}
