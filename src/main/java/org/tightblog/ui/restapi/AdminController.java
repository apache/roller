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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.comment.BlacklistCommentValidator;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Controller for weblogger backend tasks, e.g., cache and system runtime configuration.
 * For message resolution, Locale is not used for Admin actions to force messages to English
 * (Only sysadmins have access to this functionality and keeping message strings to one
 * language helps in searching issues on the Web.)
 */
@RestController
@RequestMapping(path = "/tb-ui/admin/rest/server")
public class AdminController {

    private static Logger log = LoggerFactory.getLogger(AdminController.class);

    private Set<LazyExpiringCache> cacheSet;
    private WeblogManager weblogManager;
    private IndexManager indexManager;
    private BlacklistCommentValidator blacklistCommentValidator;
    private WeblogRepository weblogRepository;
    private JPAPersistenceStrategy persistenceStrategy;
    private MessageSource messages;

    @Autowired
    public AdminController(Set<LazyExpiringCache> cacheSet, WeblogManager weblogManager, IndexManager indexManager,
                           BlacklistCommentValidator blacklistCommentValidator, WeblogRepository weblogRepository,
                           JPAPersistenceStrategy persistenceStrategy, MessageSource messages) {
        this.cacheSet = cacheSet;
        this.weblogManager = weblogManager;
        this.indexManager = indexManager;
        this.blacklistCommentValidator = blacklistCommentValidator;
        this.weblogRepository = weblogRepository;
        this.persistenceStrategy = persistenceStrategy;
        this.messages = messages;
    }

    @GetMapping(value = "/caches")
    public Map<String, LazyExpiringCache> getCacheData() throws ServletException {
        Map<String, LazyExpiringCache> cacheMap = new HashMap<>();
        cacheSet.forEach(c -> cacheMap.put(c.getCacheHandlerId(), c));
        return cacheMap;
    }

    @PostMapping(value = "/cache/{cacheName}/clear", produces = "text/plain")
    public ResponseEntity<String> emptyOneCache(@PathVariable String cacheName)
            throws ServletException {
        Optional<LazyExpiringCache> maybeCache = cacheSet.stream()
                .filter(c -> c.getCacheHandlerId().equalsIgnoreCase(cacheName)).findFirst();
        maybeCache.ifPresent(LazyExpiringCache::invalidateAll);
        return ResponseEntity.ok(messages.getMessage("cachedData.message.cache.cleared",
                new Object[] {cacheName}, null));
    }

    @PostMapping(value = "/resethitcount", produces = "text/plain")
    public ResponseEntity<String> resetHitCount() {
        try {
            weblogRepository.updateDailyHitCountZero();
            log.info("daily hit counts manually reset by administrator");
            return ResponseEntity.ok(messages.getMessage("cachedData.message.reset", null, null));
        } catch (Exception ex) {
            log.error("Error resetting weblog hit count - {}", ex);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                    body(messages.getMessage("generic.error.check.logs", null, null));
        }
    }

    @GetMapping(value = "/webloglist")
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

    @PostMapping(value = "/weblog/{handle}/rebuildindex", produces = "text/plain")
    public ResponseEntity<String> rebuildIndex(@PathVariable String handle) {
        try {
            Weblog weblog = weblogRepository.findByHandleAndVisibleTrue(handle);
            if (weblog != null) {
                indexManager.updateIndex(weblog, false);
                return ResponseEntity.ok(messages.getMessage("cachedData.message.indexed", new Object[]{handle}, null));
            } else {
                return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).
                        body(messages.getMessage("generic.error.check.logs", null, null));
            }
        } catch (Exception ex) {
            log.error("Error doing index rebuild", ex);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).
                    body(messages.getMessage("generic.error.check.logs", null, null));
        }
    }

    @GetMapping(value = "/webloggerproperties")
    public WebloggerProperties getWebloggerProperties(HttpServletResponse response) throws ServletException {
        return persistenceStrategy.load(WebloggerProperties.class, "1");
    }

    @PostMapping(value = "/webloggerproperties", produces = "text/plain")
    public ResponseEntity updateProperties(@Valid @RequestBody WebloggerProperties properties) {

        // maintain last weblog change
        WebloggerProperties oldProperties = persistenceStrategy.load(WebloggerProperties.class, "1");
        properties.setLastWeblogChange(oldProperties.getLastWeblogChange());
        persistenceStrategy.merge(properties);
        persistenceStrategy.flush();
        blacklistCommentValidator.setGlobalCommentFilter(properties.getCommentSpamFilter());
        return ResponseEntity.ok(messages.getMessage("generic.changes.saved", null, null));
    }

    @GetMapping(value = "/globalconfigmetadata")
    public GlobalConfigMetadata getGlobalConfigMetadata(HttpServletResponse response) {

        GlobalConfigMetadata gcm = new GlobalConfigMetadata();

        List<Weblog> weblogs = weblogManager.getWeblogs(true, 0, -1);

        gcm.weblogList = weblogs.stream()
                        .sorted(Comparator.comparing(Weblog::getHandle))
                        .collect(Utilities.toLinkedHashMap(Weblog::getId, Weblog::getHandle));

        gcm.registrationOptions = Arrays.stream(WebloggerProperties.RegistrationPolicy.values())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.RegistrationPolicy::name,
                    e -> messages.getMessage(e.getDescription(), null, null)));

        gcm.blogHtmlLevels = Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> !r.equals(HTMLSanitizer.Level.NONE))
                .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                    e -> messages.getMessage(e.getDescription(), null, null)));

        gcm.commentHtmlLevels = Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> !r.equals(HTMLSanitizer.Level.NONE))
                .filter(r -> r.getSanitizingLevel() <= HTMLSanitizer.Level.BASIC_IMAGES.getSanitizingLevel())
                .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                    e -> messages.getMessage(e.getDescription(), null, null)));

        gcm.commentOptions = Arrays.stream(WebloggerProperties.CommentPolicy.values())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.CommentPolicy::name,
                    e -> messages.getMessage(e.getSiteDescription(), null, null)));

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
