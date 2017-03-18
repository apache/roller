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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Controller for weblogger backend tasks, e.g., cache and system runtime configuration.
 */
@RestController
@RequestMapping(path = "/tb-ui/admin/rest/server")
public class AdminController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

    private I18nMessages messages = I18nMessages.getMessages(Locale.getDefault());

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public AdminController() {
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    @RequestMapping(value = "/caches", method = RequestMethod.GET)
    public Map<String, CacheStats> getCacheData() throws ServletException {
        return cacheManager.getStats();
    }

    @RequestMapping(value = "/cache/{cacheName}/clear", method = RequestMethod.POST)
    public Map<String, CacheStats> emptyOneCache(@PathVariable String cacheName) throws ServletException {
        cacheManager.clear(cacheName);
        Map<String, CacheStats> temp = new HashMap<>();
        temp.put(cacheName, cacheManager.getStats(cacheName));
        return temp;
    }

    @RequestMapping(value = "/caches/clear", method = RequestMethod.POST)
    public Map<String, CacheStats> emptyAllCaches() throws ServletException {
        cacheManager.clear();
        return getCacheData();
    }

    @RequestMapping(value = "/resethitcount", method = RequestMethod.POST)
    public ResponseEntity<String> resetHitCount() {
        try {
            weblogManager.resetAllHitCounts();
            return ResponseEntity.ok(messages.getString("maintenance.message.reset"));
        } catch (Exception ex) {
            log.error("Error resetting weblog hit count - {}", ex);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                    body(messages.getString("generic.error.check.logs"));
        }
    }

    @RequestMapping(value = "/webloglist", method = RequestMethod.GET)
    public List<String> getWeblogHandles(HttpServletResponse response) throws ServletException {
        try {
            List<String> weblogHandles = new ArrayList<>();
            List<Weblog> weblogs = weblogManager.getWeblogs(null, 0, -1);
            for (Weblog weblog : weblogs) {
                weblogHandles.add(weblog.getHandle());
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return weblogHandles;
        } catch (Exception ex) {
            log.error("Error retrieving weblog handle list", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @RequestMapping(value = "/weblog/{handle}/rebuildindex", method = RequestMethod.POST)
    public ResponseEntity<String> rebuildIndex(@PathVariable String handle) {
        try {
            Weblog weblog = weblogManager.getWeblogByHandle(handle);
            if (weblog != null) {
                indexManager.rebuildWeblogIndex(weblog);
                return ResponseEntity.ok(messages.getString("maintenance.message.indexed", handle));
            } else {
                return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).
                        body(messages.getString("generic.error.check.logs"));
            }
        } catch (Exception ex) {
            log.error("Error doing index rebuild", ex);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                    body(messages.getString("generic.error.check.logs"));
        }
    }

    @RequestMapping(value = "/webloggerproperties", method = RequestMethod.GET)
    public WebloggerProperties getWebloggerProperties(HttpServletResponse response) throws ServletException {
        return persistenceStrategy.load(WebloggerProperties.class, "1");
    }

    @RequestMapping(value = "/webloggerproperties", method = RequestMethod.POST)
    public ResponseEntity updateProperties(Principal p, @Valid @RequestBody WebloggerProperties properties) {
        User user = userManager.getEnabledUserByUserName(p.getName());
        I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

        persistenceStrategy.merge(properties);
        persistenceStrategy.flush();

        weblogManager.setSiteBlacklist(new Blacklist(properties.getCommentSpamFilter(), null));

        return ResponseEntity.ok(messages.getString("generic.changes.saved"));
    }

    @RequestMapping(value = "/globalconfigmetadata", method = RequestMethod.GET)
    public GlobalConfigMetadata getGlobalConfigMetadata(Principal principal, HttpServletResponse response) {

        User user = userManager.getEnabledUserByUserName(principal.getName());
        I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

        GlobalConfigMetadata gcm = new GlobalConfigMetadata();

        List<Weblog> weblogs = weblogManager.getWeblogs(true, 0, -1);

        gcm.weblogList = weblogs.stream()
                        .sorted(Comparator.comparing(Weblog::getHandle))
                        .collect(Utilities.toLinkedHashMap(Weblog::getId, Weblog::getHandle));

        gcm.registrationOptions = Arrays.stream(WebloggerProperties.RegistrationPolicy.values())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.RegistrationPolicy::name,
                         e -> messages.getString(e.getDescription())));

        gcm.blogHtmlLevels = Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> (!r.equals(HTMLSanitizer.Level.NONE)))
                .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                        e -> messages.getString(e.getDescription())));

        gcm.commentHtmlLevels = Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> (!r.equals(HTMLSanitizer.Level.NONE)))
                .filter(r -> (r.getSanitizingLevel() <= HTMLSanitizer.Level.BASIC_IMAGES.getSanitizingLevel()))
                .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                        e -> messages.getString(e.getDescription())));

        gcm.commentOptions = Arrays.stream(WebloggerProperties.CommentPolicy.values())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.CommentPolicy::name,
                        e -> messages.getString(e.getSiteDescription())));

        return gcm;
    }

    public class GlobalConfigMetadata {
        Map<String, String> weblogList;
        Map<String, String> registrationOptions;
        Map<String, String> blogHtmlLevels;
        Map<String, String> commentOptions;
        Map<String, String> commentHtmlLevels;

        public Map<String, String> getWeblogList() {
            return weblogList;
        }

        public Map<String, String> getRegistrationOptions() {
            return registrationOptions;
        }

        public Map<String, String> getBlogHtmlLevels() {
            return blogHtmlLevels;
        }

        public Map<String, String> getCommentOptions() {
            return commentOptions;
        }

        public Map<String, String> getCommentHtmlLevels() {
            return commentHtmlLevels;
        }
    }

}
