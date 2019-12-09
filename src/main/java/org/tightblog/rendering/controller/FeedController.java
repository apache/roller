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
package org.tightblog.rendering.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.rendering.model.FeedModel;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.requests.WeblogFeedRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.dao.WeblogDao;
import org.tightblog.util.Utilities;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for rendering weblog feeds.
 */
@RestController
@EnableConfigurationProperties(DynamicProperties.class)
@RequestMapping(path = FeedController.PATH)
public class FeedController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(FeedController.class);

    public static final String PATH = "/tb-ui/rendering/feed";

    private WeblogDao weblogDao;
    private LazyExpiringCache weblogFeedCache;
    private ThymeleafRenderer thymeleafRenderer;
    private ThemeManager themeManager;
    private FeedModel feedModel;
    private DynamicProperties dp;

    @Autowired
    public FeedController(WeblogDao weblogDao, LazyExpiringCache weblogFeedCache,
                          @Qualifier("standardRenderer") ThymeleafRenderer thymeleafRenderer,
                          ThemeManager themeManager, FeedModel feedModel, DynamicProperties dp) {
        this.weblogDao = weblogDao;
        this.weblogFeedCache = weblogFeedCache;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.feedModel = feedModel;
        this.dp = dp;
    }

    @GetMapping("/{weblogHandle}")
    ResponseEntity<Resource> getFeed(@PathVariable String weblogHandle,
                                     @RequestParam(value = "category", required = false) String category,
                                     @RequestParam(value = "tag", required = false) String tag,
                                     @RequestParam(value = "page", required = false) Integer page,
                                     Principal principal, HttpServletRequest request) {

        WeblogFeedRequest feedRequest = new WeblogFeedRequest(feedModel);
        feedRequest.setWeblogHandle(weblogHandle);

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(weblogHandle);
        if (weblog == null) {
            return ResponseEntity.notFound().build();
        } else {
            feedRequest.setWeblog(weblog);
        }

        if (category != null) {
            feedRequest.setCategoryName(Utilities.decode(category));
        } else if (tag != null) {
            feedRequest.setTag(Utilities.decode(tag));
        }

        if (page != null) {
            feedRequest.setPageNum(page);
        }

        weblogFeedCache.incrementIncomingRequests();

        // Is this the site-wide weblog? If so, make a combined feed using all blogs...
        feedRequest.setSiteWide(themeManager.getSharedTheme(weblog.getTheme()).isSiteWide());

        // determine the lastModified date for this content
        Instant objectLastChanged = (feedRequest.isSiteWide()) ? dp.getLastSitewideChange() : weblog.getLastModified();

        // DB stores last modified in millis, browser if-modified-since in seconds, so need to truncate millis from the former.
        long inDb = objectLastChanged.truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        long inBrowser = getBrowserCacheExpireDate(request);

        if (inDb <= inBrowser) {
            weblogFeedCache.incrementRequestsHandledBy304();
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        // check cache before manually generating
        String cacheKey = generateKey(feedRequest, feedRequest.isSiteWide());
        CachedContent rendererOutput = weblogFeedCache.get(cacheKey, objectLastChanged);

        boolean newContent = false;
        try {
            if (rendererOutput != null) {
                log.trace("HIT {}", cacheKey);
            } else {
                log.trace("MISS {}", cacheKey);
                newContent = true;

                // not in cache so need to generate content
                Map<String, Object> model = new HashMap<>();
                model.put("model", feedRequest);
                Template template = new SharedTemplate("entries-atom", Template.Role.ATOMFEED);
                rendererOutput = thymeleafRenderer.render(template, model);
            }

            if (newContent) {
                log.debug("PUT {}", cacheKey);
                weblogFeedCache.put(cacheKey, rendererOutput);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(rendererOutput.getRole().getContentType()))
                    .contentLength(rendererOutput.getContent().length)
                    .lastModified(objectLastChanged.toEpochMilli())
                    .cacheControl(CacheControl.noCache())
                    .body(new ByteArrayResource(rendererOutput.getContent()));

        } catch (Exception e) {
            log.error("Error rendering Atom feed for {}", feedRequest.getWeblog().getHandle(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     * <handle>/[[/cat/{category}]|[/tag/{tag}]]
     * Examples:
     * foo
     * foo/cat/technology
     * foo/tag/travel
     */
    String generateKey(WeblogFeedRequest feedRequest, boolean isSiteWide) {
        StringBuilder key = new StringBuilder();
        key.append(feedRequest.getWeblogHandle());

        if (feedRequest.getCategoryName() != null) {
            key.append("/cat/").append(Utilities.encode(feedRequest.getCategoryName()));
        } else if (feedRequest.getTag() != null) {
            key.append("/tag/").append(Utilities.encode(feedRequest.getTag()));
        }

        if (feedRequest.getPageNum() > 0) {
            key.append("/page=").append(feedRequest.getPageNum());
        }

        // site wide feeds must be aware of the last update date of any weblog
        // as they get refreshed whenever any of blogs do.
        if (isSiteWide) {
            key.append("/lastUpdate=").append(dp.getLastSitewideChange().toEpochMilli());
        }

        return key.toString();
    }
}
