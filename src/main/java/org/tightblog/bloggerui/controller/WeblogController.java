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
package org.tightblog.bloggerui.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.bloggerui.model.WeblogConfigMetadata;
import org.tightblog.service.WeblogManager;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.Utilities;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.domain.WebloggerProperties;

import javax.validation.Valid;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
public class WeblogController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

    private UserDao userDao;
    private WeblogManager weblogManager;
    private ThemeManager themeManager;
    private DynamicProperties dp;
    private WeblogDao weblogDao;
    private WeblogEntryDao weblogEntryDao;
    private MessageSource messages;
    private WebloggerPropertiesDao webloggerPropertiesDao;

    @Value("${site.pages.maxEntries:30}")
    private int maxEntriesPerPage;

    @Autowired
    public WeblogController(UserDao userDao, WeblogManager weblogManager,
                            ThemeManager themeManager, DynamicProperties dp,
                            WeblogDao weblogDao, MessageSource messages,
                            WebloggerPropertiesDao webloggerPropertiesDao,
                            WeblogEntryDao weblogEntryDao) {
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.userDao = userDao;
        this.weblogManager = weblogManager;
        this.themeManager = themeManager;
        this.dp = dp;
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/loggedin")
    public boolean loggedIn() {
        return true;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity getWeblogData(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.getOne(id);
        return ResponseEntity.ok(weblog);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblogs")
    public ResponseEntity addWeblog(@Valid @RequestBody Weblog newData, Principal p) {

        User user = userDao.findEnabledByUserName(p.getName());

        if (!user.hasEffectiveGlobalRole(GlobalRole.BLOGCREATOR)) {
            return ResponseEntity.status(403).body(messages.getMessage("weblogConfig.createNotAuthorized",
                    null, Locale.getDefault()));
        }

        if (weblogDao.findByHandle(newData.getHandle()) != null) {
            return ValidationErrorResponse.badRequest(messages.getMessage("weblogConfig.error.handleExists",
                    null, Locale.getDefault()));
        }

        Weblog weblog = new Weblog(
                newData.getHandle().trim(),
                user,
                newData.getName().trim(),
                newData.getTheme());

        return saveWeblog(weblog, newData, true);
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity updateWeblog(@PathVariable String id, @Valid @RequestBody Weblog newData, Principal p) {
        Weblog weblog = weblogDao.getOne(id);
        return saveWeblog(weblog, newData, false);
    }

    private ResponseEntity saveWeblog(Weblog weblog, Weblog newData, boolean newWeblog) {
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
            weblog.setSpamPolicy(newData.getSpamPolicy());
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
                log.info("New weblog {} created by user {}", weblog, weblog.getCreator());
            } else {
                weblogManager.saveWeblog(weblog, true);
            }

            // ROL-1050: apply comment defaults to existing entries
            if (newData.isApplyCommentDefaults()) {
                weblogEntryDao.applyDefaultCommentDaysToWeblogEntries(weblog, weblog.getDefaultCommentDays());
            }

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(weblog);
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/weblog/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public ResponseEntity deleteWeblog(@PathVariable String id, Principal p) {
        Weblog weblog = weblogDao.getOne(id);
        weblogManager.removeWeblog(weblog);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblogconfig/metadata")
    public WeblogConfigMetadata getWeblogConfigMetadata(Locale locale, Principal p) {
        User user = userDao.findEnabledByUserName(p.getName());

        WeblogConfigMetadata metadata = new WeblogConfigMetadata();

        metadata.setAbsoluteSiteURL(dp.getAbsoluteUrl());

        metadata.setUsersOverrideAnalyticsCode(
                webloggerPropertiesDao.findOrNull().isUsersOverrideAnalyticsCode());

        metadata.setUsersCommentNotifications(
                webloggerPropertiesDao.findOrNull().isUsersCommentNotifications());

        metadata.getEditFormats().putAll(Arrays.stream(Weblog.EditFormat.values())
                .collect(Utilities.toLinkedHashMap(Weblog.EditFormat::name,
                        eF -> messages.getMessage(eF.getDescriptionKey(), null, locale))));

        metadata.getLocales().putAll(Arrays.stream(Locale.getAvailableLocales())
                .sorted(Comparator.comparing(Locale::getDisplayName))
                .collect(Utilities.toLinkedHashMap(Locale::toString, Locale::getDisplayName)));

        metadata.getTimezones().putAll(Arrays.stream(TimeZone.getAvailableIDs())
                .sorted(Comparator.comparing(tz -> tz))
                .collect(Utilities.toLinkedHashMap(tz -> tz, tz -> tz)));

        WebloggerProperties.CommentPolicy globalCommentPolicy =
                webloggerPropertiesDao.findOrNull().getCommentPolicy();

        metadata.getCommentOptions().putAll(Arrays.stream(WebloggerProperties.CommentPolicy.values())
                .filter(co -> co.getLevel() <= globalCommentPolicy.getLevel())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.CommentPolicy::name,
                        co -> messages.getMessage(co.getLabel(), null, locale))));

        WebloggerProperties.SpamPolicy globalSpamPolicy =
                webloggerPropertiesDao.findOrNull().getSpamPolicy();

        metadata.getSpamOptions().putAll(Arrays.stream(WebloggerProperties.SpamPolicy.values())
                .filter(opt -> opt.getLevel() >= globalSpamPolicy.getLevel())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.SpamPolicy::name,
                        opt -> messages.getMessage(opt.getLabel(), null, locale))));

        metadata.getCommentDayOptions().putAll(Arrays.stream(WeblogEntry.CommentDayOption.values())
                .collect(Utilities.toLinkedHashMap(cdo -> Integer.toString(cdo.getDays()),
                        cdo -> messages.getMessage(cdo.getDescriptionKey(), null, locale))));

        metadata.getSharedThemeMap().putAll(themeManager.getEnabledSharedThemesList().stream()
                // Remove sitewide theme options for non-admins, if desired admin can create a sitewide blog
                // and assign a non-admin user ownership of it on the members page.
                .filter(theme -> !theme.isSiteWide() || user.hasEffectiveGlobalRole(GlobalRole.ADMIN))
                .collect(Utilities.toLinkedHashMap(SharedTheme::getId, st -> st)));

        return metadata;
    }
}
