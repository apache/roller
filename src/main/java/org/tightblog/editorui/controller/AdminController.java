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
package org.tightblog.editorui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.editorui.model.GlobalConfigMetadata;
import org.tightblog.editorui.model.ValidationErrorResponse;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.comment.BlacklistCommentValidator;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
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
    private LuceneIndexer luceneIndexer;
    private BlacklistCommentValidator blacklistCommentValidator;
    private WeblogDao weblogDao;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private MessageSource messages;

    @Autowired
    public AdminController(Set<LazyExpiringCache> cacheSet, LuceneIndexer luceneIndexer,
                           BlacklistCommentValidator blacklistCommentValidator, WeblogDao weblogDao,
                           MessageSource messages,
                           WebloggerPropertiesDao webloggerPropertiesDao) {
        this.cacheSet = cacheSet;
        this.luceneIndexer = luceneIndexer;
        this.blacklistCommentValidator = blacklistCommentValidator;
        this.weblogDao = weblogDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.messages = messages;
    }

    @GetMapping(value = "/caches")
    public Map<String, LazyExpiringCache> getCacheData() {
        Map<String, LazyExpiringCache> cacheMap = new HashMap<>();
        cacheSet.forEach(c -> cacheMap.put(c.getCacheHandlerId(), c));
        return cacheMap;
    }

    @PostMapping(value = "/cache/{cacheName}/clear", produces = "text/plain")
    public String emptyOneCache(@PathVariable String cacheName, Locale locale) {
        Optional<LazyExpiringCache> maybeCache = cacheSet.stream()
                .filter(c -> c.getCacheHandlerId().equalsIgnoreCase(cacheName)).findFirst();
        maybeCache.ifPresent(LazyExpiringCache::invalidateAll);
        return messages.getMessage("cachedData.message.cache.cleared",
                new Object[] {cacheName}, locale);
    }

    @PostMapping(value = "/resethitcount", produces = "text/plain")
    public String resetHitCount(Locale locale) {
        weblogDao.resetDailyHitCounts();
        log.info("daily hit counts manually reset by administrator");
        return messages.getMessage("cachedData.message.reset", null, locale);
    }

    @GetMapping(value = "/webloglist")
    public List<String> getWeblogHandles() {
        List<String> weblogHandles = new ArrayList<>();
        Page<Weblog> weblogs = weblogDao.findAll(Pageable.unpaged());
        for (Weblog weblog : weblogs) {
            weblogHandles.add(weblog.getHandle());
        }
        return weblogHandles;
    }

    @PostMapping(value = "/weblog/{handle}/rebuildindex", produces = "text/plain")
    public ResponseEntity rebuildIndex(@PathVariable String handle, Locale locale) {
        Weblog weblog = weblogDao.findByHandle(handle);
        if (weblog != null) {
            luceneIndexer.updateIndex(weblog, false);
            return ResponseEntity.ok(
                    messages.getMessage("cachedData.message.indexed", new Object[]{handle}, locale));
        } else {
            return ValidationErrorResponse.badRequest(
                    messages.getMessage("generic.weblog.not.found", new Object[]{handle}, locale));
        }
    }

    @GetMapping(value = "/webloggerproperties")
    public WebloggerProperties getWebloggerProperties() {
        return webloggerPropertiesDao.findOrNull();
    }

    @PostMapping(value = "/webloggerproperties", produces = "text/plain")
    public String updateProperties(@Valid @RequestBody WebloggerProperties properties, Locale locale) {
        webloggerPropertiesDao.saveAndFlush(properties);
        blacklistCommentValidator.setGlobalCommentFilter(properties.getCommentSpamFilter());
        return messages.getMessage("generic.changes.saved", null, locale);
    }

    @GetMapping(value = "/globalconfigmetadata")
    public GlobalConfigMetadata getGlobalConfigMetadata() {

        GlobalConfigMetadata gcm = new GlobalConfigMetadata();

        Page<Weblog> weblogs = weblogDao.findAll(Pageable.unpaged());

        gcm.getWeblogList().putAll(weblogs.stream()
                        .sorted(Comparator.comparing(Weblog::getHandle))
                        .collect(Utilities.toLinkedHashMap(Weblog::getId, Weblog::getHandle)));

        gcm.getRegistrationOptions().putAll(Arrays.stream(WebloggerProperties.RegistrationPolicy.values())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.RegistrationPolicy::name,
                    e -> messages.getMessage(e.getDescription(), null, null))));

        gcm.getBlogHtmlLevels().putAll(Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> !r.equals(HTMLSanitizer.Level.NONE))
                .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                    e -> messages.getMessage(e.getDescription(), null, null))));

        gcm.getCommentHtmlLevels().putAll(Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> !r.equals(HTMLSanitizer.Level.NONE))
                .filter(r -> r.getSanitizingLevel() <= HTMLSanitizer.Level.BASIC_IMAGES.getSanitizingLevel())
                .collect(Utilities.toLinkedHashMap(HTMLSanitizer.Level::name,
                    e -> messages.getMessage(e.getDescription(), null, null))));

        gcm.getCommentOptions().putAll(Arrays.stream(WebloggerProperties.CommentPolicy.values())
                .collect(Utilities.toLinkedHashMap(WebloggerProperties.CommentPolicy::name,
                    e -> messages.getMessage(e.getSiteDescription(), null, null))));

        return gcm;
    }

}
