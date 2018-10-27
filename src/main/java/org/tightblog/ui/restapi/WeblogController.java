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
package org.tightblog.ui.restapi;

import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.config.DynamicProperties;
import org.tightblog.pojos.SharedTheme;
import org.tightblog.business.ThemeManager;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.WeblogEntryRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
public class WeblogController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

    private UserRepository userRepository;
    private WeblogManager weblogManager;
    private ThemeManager themeManager;
    private UserManager userManager;
    private DynamicProperties dp;
    private WeblogRepository weblogRepository;
    private WeblogEntryRepository weblogEntryRepository;
    private MessageSource messages;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;

    @Value("${site.pages.maxEntries:30}")
    private int maxEntriesPerPage;

    @Autowired
    public WeblogController(UserRepository userRepository, WeblogManager weblogManager, ThemeManager themeManager,
                            UserManager userManager, DynamicProperties dp,
                            WeblogRepository weblogRepository, MessageSource messages,
                            WebloggerPropertiesRepository webloggerPropertiesRepository,
                            WeblogEntryRepository weblogEntryRepository) {
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.userRepository = userRepository;
        this.weblogManager = weblogManager;
        this.themeManager = themeManager;
        this.userManager = userManager;
        this.dp = dp;
        this.weblogRepository = weblogRepository;
        this.weblogEntryRepository = weblogEntryRepository;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/loggedin")
    public boolean loggedIn() {
        return true;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    public Weblog getWeblogData(@PathVariable String id, Principal p, HttpServletResponse response) throws ServletException {
        ResponseEntity maybeError = checkIfOwnerOfValidWeblog(id, p);
        if (maybeError == null) {
            return weblogRepository.findById(id).orElse(null);
        } else {
            response.setStatus(maybeError.getStatusCode().value());
            return null;
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogs")
    public ResponseEntity addWeblog(@Valid @RequestBody Weblog newData, Principal p, HttpServletResponse response)
            throws ServletException {

        User user = userRepository.findEnabledByUserName(p.getName());

        if (!user.hasEffectiveGlobalRole(GlobalRole.BLOGCREATOR)) {
            return ResponseEntity.status(403).body(bundle.getString("weblogConfig.createNotAuthorized"));
        }

        ValidationError maybeError = advancedValidate(newData, true);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }

        Weblog weblog = new Weblog(
                newData.getHandle().trim(),
                user,
                newData.getName().trim(),
                newData.getTheme());

        return saveWeblog(weblog, newData, response, true);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    public ResponseEntity updateWeblog(@PathVariable String id, @Valid @RequestBody Weblog newData, Principal p,
                                       HttpServletResponse response) throws ServletException {
        ResponseEntity maybeError = checkIfOwnerOfValidWeblog(id, p);
        if (maybeError != null) {
            return maybeError;
        }
        Weblog weblog = weblogRepository.findById(id).orElse(null);
        ValidationError maybeValError = advancedValidate(newData, false);
        if (maybeValError != null) {
            return ResponseEntity.badRequest().body(maybeValError);
        }
        return saveWeblog(weblog, newData, response, false);
    }

    private ResponseEntity saveWeblog(Weblog weblog, Weblog newData, HttpServletResponse response, boolean newWeblog)
            throws ServletException {
        try {
            if (weblog != null) {

                if (newData.getAnalyticsCode() != null) {
                    newData.setAnalyticsCode(newData.getAnalyticsCode().trim());
                }

                weblog.setName(newData.getName());
                weblog.setTagline(StringUtils.trimToEmpty(newData.getTagline()));
                weblog.setEditFormat(newData.getEditFormat());
                weblog.setVisible(newData.getVisible());
                weblog.setEntriesPerPage(newData.getEntriesPerPage());
                weblog.setBlacklist(newData.getBlacklist());
                weblog.setAllowComments(newData.getAllowComments());
                weblog.setEmailComments(newData.getEmailComments());
                weblog.setLocale(newData.getLocale());
                weblog.setTimeZone(newData.getTimeZone());

                // make sure user didn't enter an invalid entry display count
                if (newData.getEntriesPerPage() > maxEntriesPerPage) {
                    newData.setEntriesPerPage(maxEntriesPerPage);
                }
                weblog.setEntriesPerPage(newData.getEntriesPerPage());

                weblog.setAbout(newData.getAbout());
                weblog.setAnalyticsCode(newData.getAnalyticsCode());
                weblog.setDefaultCommentDays(newData.getDefaultCommentDays());

                // save config
                if (newWeblog) {
                    weblogManager.addWeblog(weblog);
                } else {
                    weblogManager.saveWeblog(weblog);
                }

                // ROL-1050: apply comment defaults to existing entries
                if (newData.isApplyCommentDefaults()) {
                    weblogEntryRepository.applyDefaultCommentDaysToWeblogEntries(weblog, weblog.getDefaultCommentDays());
                }

                // flush and clear cache
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(weblog);
        } catch (Exception e) {
            log.error("Error updating weblog", e);
            throw new ServletException(e.getMessage());
        }
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    public void deleteWeblog(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {
        ResponseEntity maybeError = checkIfOwnerOfValidWeblog(id, p);
        if (maybeError == null) {
            Weblog weblog = weblogRepository.findById(id).orElse(null);
            try {
                weblogManager.removeWeblog(weblog);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                log.error("Error removing weblog - {}", weblog.getHandle(), ex);
            }
        } else {
            response.setStatus(maybeError.getStatusCode().value());
        }
    }

    private ValidationError advancedValidate(Weblog data, boolean isAdd) {
        BindException be = new BindException(data, "new data object");

        // make sure handle isn't already taken
        if (isAdd) {
            if (weblogRepository.findByHandleAndVisibleTrue(data.getHandle()) != null) {
                be.addError(new ObjectError("Weblog object", bundle.getString("weblogConfig.error.handleExists")));
            }
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

    private ResponseEntity checkIfOwnerOfValidWeblog(String weblogId, Principal p) {
        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
        if (weblog != null) {
            if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                return null;
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblogconfig/metadata")
    public WeblogConfigMetadata getWeblogConfigMetadata(Locale locale, Principal p) {
        User user = userRepository.findEnabledByUserName(p.getName());

        WeblogConfigMetadata metadata = new WeblogConfigMetadata();

        metadata.absoluteSiteURL = dp.getAbsoluteUrl();

        metadata.usersOverrideAnalyticsCode =
                webloggerPropertiesRepository.findOrNull().isUsersOverrideAnalyticsCode();

        metadata.usersCommentNotifications =
                webloggerPropertiesRepository.findOrNull().isUsersCommentNotifications();

        metadata.sharedThemeMap = themeManager.getEnabledSharedThemesList().stream()
                // Remove sitewide theme options for non-admins, if desired admin can create a sitewide blog
                // and assign a non-admin user ownership of it on the members page.
                .filter(theme -> !theme.isSiteWide() || user.hasEffectiveGlobalRole(GlobalRole.ADMIN))
                .collect(Utilities.toLinkedHashMap(SharedTheme::getId, st -> st));

        metadata.editFormats = Arrays.stream(Weblog.EditFormat.values())
                .collect(Utilities.toLinkedHashMap(Weblog.EditFormat::name,
                        eF -> messages.getMessage(eF.getDescriptionKey(), null, locale)));

        metadata.locales = Arrays.stream(Locale.getAvailableLocales())
                .sorted(Comparator.comparing(Locale::getDisplayName))
                .collect(Utilities.toLinkedHashMap(Locale::toString, Locale::getDisplayName));

        metadata.timezones = Arrays.stream(TimeZone.getAvailableIDs())
                .sorted(Comparator.comparing(tz -> tz))
                .collect(Utilities.toLinkedHashMap(tz -> tz, tz -> tz));

        WebloggerProperties.CommentPolicy globalCommentPolicy =
                webloggerPropertiesRepository.findOrNull().getCommentPolicy();

        metadata.commentOptions = Arrays.stream(WebloggerProperties.CommentPolicy.values())
                .filter(co -> co.getLevel() <= globalCommentPolicy.getLevel())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.CommentPolicy::name,
                        co -> messages.getMessage(co.getWeblogDescription(), null, locale)));

        metadata.commentDayOptions = Arrays.stream(WeblogEntry.CommentDayOption.values())
                .collect(Utilities.toLinkedHashMap(cdo -> Integer.toString(cdo.getDays()),
                        cdo -> messages.getMessage(cdo.getDescriptionKey(), null, locale)));

        return metadata;
    }

    public class WeblogConfigMetadata {
        Map<String, SharedTheme> sharedThemeMap;
        Map<String, String> editFormats;
        Map<String, String> locales;
        Map<String, String> timezones;
        Map<String, String> commentOptions;
        Map<String, String> commentDayOptions;

        String absoluteSiteURL;
        boolean usersOverrideAnalyticsCode;
        boolean usersCommentNotifications;

        public Map<String, String> getEditFormats() {
            return editFormats;
        }

        public Map<String, String> getLocales() {
            return locales;
        }

        public Map<String, String> getTimezones() {
            return timezones;
        }

        public Map<String, String> getCommentOptions() {
            return commentOptions;
        }

        public Map<String, String> getCommentDayOptions() {
            return commentDayOptions;
        }

        public String getAbsoluteSiteURL() {
            return absoluteSiteURL;
        }

        public Map<String, SharedTheme> getSharedThemeMap() {
            return sharedThemeMap;
        }

        public boolean isUsersOverrideAnalyticsCode() {
            return usersOverrideAnalyticsCode;
        }

        public boolean isUsersCommentNotifications() {
            return usersCommentNotifications;
        }
    }

}
