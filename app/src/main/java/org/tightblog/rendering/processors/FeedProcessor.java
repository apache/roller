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
package org.tightblog.rendering.processors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.SharedTemplate;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.rendering.requests.WeblogFeedRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.util.Utilities;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for rendering weblog feeds.
 */
@RestController
@RequestMapping(path = "/tb-ui/rendering/feed/**")
public class FeedProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(FeedProcessor.class);

    public static final String PATH = "/tb-ui/rendering/feed";

    @Autowired
    private LazyExpiringCache weblogFeedCache = null;

    public void setWeblogFeedCache(LazyExpiringCache weblogFeedCache) {
        this.weblogFeedCache = weblogFeedCache;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    @Qualifier("rssRenderer")
    private ThymeleafRenderer thymeleafRenderer = null;

    public void setThymeleafRenderer(ThymeleafRenderer thymeleafRenderer) {
        this.thymeleafRenderer = thymeleafRenderer;
    }

    @Autowired
    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @Autowired
    protected JPAPersistenceStrategy strategy;

    public void setStrategy(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void getFeed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Weblog weblog;

        WeblogFeedRequest feedRequest;
        try {
            // parse the incoming request and extract the relevant data
            feedRequest = new WeblogFeedRequest(request);

            weblog = weblogManager.getWeblogByHandle(feedRequest.getWeblogHandle(), true);
            if (weblog == null) {
                throw new IllegalStateException("unable to lookup weblog: " + feedRequest.getWeblogHandle());
            } else {
                feedRequest.setWeblog(weblog);
            }

            // Is this the site-wide weblog? If so, make a combined feed using all blogs...
            feedRequest.setSiteWideFeed(themeManager.getSharedTheme(weblog.getTheme()).isSiteWide());

        } catch (Exception e) {
            // invalid feed request format or weblog doesn't exist
            log.debug("error creating weblog feed request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // determine the lastModified date for this content
        Instant lastModified = (feedRequest.isSiteWideFeed()) ? strategy.getWebloggerProperties().getLastWeblogChange()
                : weblog.getLastModified();

        // Respond with 304 Not Modified if it is not modified.
        if (respondIfNotModified(request, response, lastModified, feedRequest.getDeviceType())) {
            return;
        }

        // set last-modified date
        setLastModifiedHeader(response, lastModified, feedRequest.getDeviceType());

        // set content type
        response.setContentType("application/atom+xml; charset=utf-8");

        // cached content checking
        String cacheKey = generateKey(feedRequest, feedRequest.isSiteWideFeed());
        CachedContent cachedContent = (CachedContent) (feedRequest.isSiteWideFeed() ?
                strategy.getWebloggerProperties().getLastWeblogChange() : weblogFeedCache.get(cacheKey, lastModified));

        if (cachedContent != null) {
            log.debug("HIT {}", cacheKey);
            response.setContentLength(cachedContent.getContent().length);
            response.getOutputStream().write(cachedContent.getContent());
            return;
        } else {
            log.debug("MISS {}", cacheKey);
        }

        // Validation. To save DB processing time, detect some of the common queries that would return no rows.
        boolean invalid = false;

        if (!feedRequest.isSiteWideFeed() && feedRequest.getCategoryName() != null) {
            // for single-weblog search, category must be defined for the weblog
            WeblogCategory test = weblogManager.getWeblogCategoryByName(feedRequest.getWeblog(),
                    feedRequest.getCategoryName());
            if (test == null) {
                invalid = true;
            }
        } else if (feedRequest.getTag() != null) {
            // tags specified. make sure they exist.
            invalid = !weblogManager.getTagExists((feedRequest.isSiteWideFeed()) ? null : weblog, feedRequest.getTag());
        } else if (!"entries".equals(feedRequest.getType()) && !"comments".equals(feedRequest.getType())) {
            invalid = true;
        }

        if (invalid) {
            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // looks like we need to render content
        Map<String, Object> model;
        String pageId;

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", feedRequest);
        model = getModelMap("feedModelSet", initData);
        pageId = feedRequest.getType() + "-atom";

        try {
            Template template = new SharedTemplate(pageId);
            CachedContent rendererOutput = thymeleafRenderer.render(template, model, "application/atom+xml");
            response.setContentType(rendererOutput.getContentType());
            response.setContentLength(rendererOutput.getContent().length);
            response.getOutputStream().write(rendererOutput.getContent());

            // cache rendered content.
            log.debug("PUT {}", cacheKey);
            weblogFeedCache.put(cacheKey, rendererOutput);
        } catch (Exception e) {
            log.error("Error during rendering for page {}", pageId, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     * <p>
     * <handle>/<type>[[/cat/{category}]|[/tag/{tag}]]
     * <p>
     * examples ...
     * <p>
     * foo/entries
     * foo/comments/cat/technology
     * foo/entries/tag/travel
     */
    protected String generateKey(WeblogFeedRequest feedRequest, boolean isSiteWide) {
        StringBuilder key = new StringBuilder();

        key.append("weblogfeed.key").append(":");
        key.append(feedRequest.getWeblogHandle());

        key.append("/").append(feedRequest.getType());

        if (feedRequest.getCategoryName() != null) {
            String cat = feedRequest.getCategoryName();
            cat = Utilities.encode(cat);
            key.append("/cat/").append(cat);
        } else if (feedRequest.getTag() != null) {
            String tag = feedRequest.getTag();
            tag = Utilities.encode(tag);
            key.append("/tag/").append(tag);
        }

        // site wide feeds must be cognizant of the last update date of any weblog or weblog entry
        // as they get refreshed whenever another blog or blog entry does.
        if (isSiteWide) {
            key.append("/lastUpdate=").append(strategy.getWebloggerProperties().getLastWeblogChange());
        }

        return key.toString();
    }
}
