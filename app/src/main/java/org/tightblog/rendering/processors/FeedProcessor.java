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

import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.SharedTemplate;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.TemplateRendition;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.rendering.Renderer;
import org.tightblog.rendering.RendererManager;
import org.tightblog.rendering.requests.WeblogFeedRequest;
import org.tightblog.util.Utilities;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.cache.SiteWideCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.DeviceType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
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
    private SiteWideCache siteWideCache = null;

    public void setSiteWideCache(SiteWideCache siteWideCache) {
        this.siteWideCache = siteWideCache;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private RendererManager rendererManager = null;

    public void setRendererManager(RendererManager rendererManager) {
        this.rendererManager = rendererManager;
    }

    @Autowired
    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void getFeed(HttpServletRequest request, HttpServletResponse response) throws IOException {

        log.debug("Entering");

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
        long lastModified = Clock.systemDefaultZone().millis();
        if (feedRequest.isSiteWideFeed()) {
            lastModified = siteWideCache.getLastModified().toEpochMilli();
        } else if (weblog.getLastModified() != null) {
            lastModified = weblog.getLastModified().toEpochMilli();
        }

        // Respond with 304 Not Modified if it is not modified.
        if (respondIfNotModified(request, response, lastModified, feedRequest.getDeviceType())) {
            return;
        }

        // set last-modified date
        setLastModifiedHeader(response, lastModified, feedRequest.getDeviceType());

        // set content type
        response.setContentType("application/atom+xml; charset=utf-8");

        // cached content checking
        String cacheKey = generateKey(feedRequest);
        CachedContent cachedContent = (CachedContent) (feedRequest.isSiteWideFeed() ?
                siteWideCache.get(cacheKey) : weblogFeedCache.get(cacheKey, lastModified));

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
        pageId = "templates/feeds/" + feedRequest.getType() + "-atom.vm";

        // lookup Renderer we are going to use
        Renderer renderer;
        try {
            log.debug("Looking up renderer");
            Template template = new SharedTemplate(pageId, TemplateRendition.Parser.VELOCITY);
            renderer = rendererManager.getRenderer(template, DeviceType.NORMAL);
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find render feed for page {}", pageId, e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content. use default size of 24K for a standard page
        CachedContent rendererOutput = new CachedContent(Utilities.TWENTYFOUR_KB_IN_BYTES);
        try {
            log.debug("Rendering...");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for page {}", pageId, e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // post rendering process

        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());

        // cache rendered content. only cache if user is not logged in?
        log.debug("PUT {}", cacheKey);
        if (feedRequest.isSiteWideFeed()) {
            siteWideCache.put(cacheKey, rendererOutput);
        } else {
            weblogFeedCache.put(cacheKey, rendererOutput);
        }

        log.debug("Exiting");
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
    protected String generateKey(WeblogFeedRequest feedRequest) {
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

        return key.toString();
    }
}
