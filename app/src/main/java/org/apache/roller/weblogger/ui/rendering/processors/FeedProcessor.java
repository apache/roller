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
package org.apache.roller.weblogger.ui.rendering.processors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.StaticTemplate;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.model.Model;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.ui.rendering.util.cache.LazyExpiringCache;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Responsible for rendering weblog feeds.
 */
@RestController
@RequestMapping(path="/roller-ui/rendering/feed/**")
public class FeedProcessor {

    private static Log log = LogFactory.getLog(FeedProcessor.class);

    public static final String PATH = "/roller-ui/rendering/feed";

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
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        log.debug("Entering");

        Weblog weblog;
        boolean isSiteWide;

        WeblogFeedRequest feedRequest;
        try {
            // parse the incoming request and extract the relevant data
            feedRequest = new WeblogFeedRequest(request);

            weblog = feedRequest.getWeblog();
            if (weblog == null) {
                throw new WebloggerException("unable to lookup weblog: " + feedRequest.getWeblogHandle());
            }

            // is this the site-wide weblog?
            isSiteWide = WebloggerRuntimeConfig.isSiteWideWeblog(weblog.getHandle());

        } catch (Exception e) {
            // invalid feed request format or weblog doesn't exist
            log.debug("error creating weblog feed request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // determine the lastModified date for this content
        long lastModified = System.currentTimeMillis();
        if (isSiteWide) {
            lastModified = siteWideCache.getLastModified().getTime();
        } else if (weblog.getLastModified() != null) {
            lastModified = weblog.getLastModified().getTime();
        }

        // Respond with 304 Not Modified if it is not modified.
        if (ModDateHeaderUtil.respondIfNotModified(request, response,
                lastModified, feedRequest.getDeviceType())) {
            return;
        }

        // set last-modified date
        ModDateHeaderUtil.setLastModifiedHeader(response, lastModified,
                feedRequest.getDeviceType());

        // set content type
        String accepts = request.getHeader("Accept");
        String userAgent = request.getHeader("User-Agent");
        if (WebloggerRuntimeConfig
                .getBooleanProperty("site.newsfeeds.styledFeeds")
                && accepts != null
                && accepts.contains("*/*")
                && userAgent != null && userAgent.startsWith("Mozilla")) {
            // client is a browser and feed style is enabled so we want
            // browsers to load the page rather than popping up the download
            // dialog, so we provide a content-type that browsers will display
            response.setContentType("text/xml");
        } else if ("rss".equals(feedRequest.getFormat())) {
            response.setContentType("application/rss+xml; charset=utf-8");
        } else if ("atom".equals(feedRequest.getFormat())) {
            response.setContentType("application/atom+xml; charset=utf-8");
        }

        // generate cache key
        String cacheKey = generateKey(feedRequest);

        // cached content checking
        CachedContent cachedContent;
        if (isSiteWide) {
            cachedContent = (CachedContent) siteWideCache.get(cacheKey);
        } else {
            cachedContent = (CachedContent) weblogFeedCache.get(cacheKey,
                    lastModified);
        }

        if (cachedContent != null) {
            log.debug("HIT " + cacheKey);

            response.setContentLength(cachedContent.getContent().length);
            response.getOutputStream().write(cachedContent.getContent());
            return;

        } else {
            log.debug("MISS " + cacheKey);
        }

        // validation. make sure that request input makes sense.
        boolean invalid = false;

        if (feedRequest.getWeblogCategoryName() != null) {

            // category specified. category must exist.
            if (feedRequest.getWeblogCategory() == null) {
                invalid = true;
            }

        } else if (feedRequest.getTags() != null
                && feedRequest.getTags().size() > 0) {

            try {
                // tags specified. make sure they exist.
                invalid = !weblogEntryManager.getTagComboExists(feedRequest.getTags(),
                        (isSiteWide) ? null : weblog);
            } catch (WebloggerException ex) {
                invalid = true;
            }
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
        try {
            // populate the rendering model
            Map<String, Object> initData = new HashMap<>();
            initData.put("parsedRequest", feedRequest);
            model = Model.getModelMap("feedModelSet", initData);

            if (isSiteWide) {
                pageId = "site-" + feedRequest.getType() + "-" + feedRequest.getFormat() + ".vm";
                // Load special models for site-wide blog
                model.putAll(Model.getModelMap("siteModelSet", initData));
            } else {
                pageId = "weblog-" + feedRequest.getType() + "-" + feedRequest.getFormat() + ".vm";
            }

            // Load search models if search feed
            if ("entries".equals(feedRequest.getType()) && feedRequest.getTerm() != null) {
                model.putAll(Model.getModelMap("searchFeedModelSet", initData));
            }
        } catch (WebloggerException ex) {
            log.error("ERROR loading model for page", ex);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // lookup Renderer we are going to use
        Renderer renderer;
        try {
            log.debug("Looking up renderer");
            Template template = new StaticTemplate(pageId, TemplateLanguage.VELOCITY);
            renderer = RendererManager.getRenderer(template,
                    MobileDeviceRepository.DeviceType.standard);
        } catch (Exception e) {
            // nobody wants to render my content :(

            // TODO: this log message has been disabled because it fills up
            // the logs with useless errors due to the fact that the way these
            // template ids are formed comes directly from the request and it
            // often gets bunk data causing invalid template ids.
            // at some point we should have better validation on the input so
            // that we can quickly dispatch invalid feed requests and only
            // get this far if we expect the template to be found
            // log.error("Couldn't find renderer for page "+pageId, e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content. use default size of 24K for a standard page
        CachedContent rendererOutput = new CachedContent(WebloggerCommon.TWENTYFOUR_KB_IN_BYTES);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for page " + pageId, e);

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
        log.debug("PUT " + cacheKey);
        if (isSiteWide) {
            siteWideCache.put(cacheKey, rendererOutput);
        } else {
            weblogFeedCache.put(cacheKey, rendererOutput);
        }

        log.debug("Exiting");
    }

    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     *
     * <handle>/<type>/<format>/<term>[/category][/language][/excerpts]
     *
     * examples ...
     *
     * foo/entries/rss/en
     * foo/comments/rss/MyCategory/en
     * foo/entries/atom/en/excerpts
     *
     */
    public String generateKey(WeblogFeedRequest feedRequest) {

        StringBuilder key = new StringBuilder();

        key.append("weblogfeed.key").append(":");
        key.append(feedRequest.getWeblogHandle());

        key.append("/").append(feedRequest.getType());
        key.append("/").append(feedRequest.getFormat());

        if (feedRequest.getTerm() != null) {
            key.append("/search/").append(feedRequest.getTerm());
        }

        if(feedRequest.getWeblogCategoryName() != null) {
            String cat = feedRequest.getWeblogCategoryName();
            try {
                cat = URLEncoder.encode(cat, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // should never happen, utf-8 is always supported
            }

            key.append("/").append(cat);
        }

        if(feedRequest.isExcerpts()) {
            key.append("/excerpts");
        }

        if(feedRequest.getTags() != null && feedRequest.getTags().size() > 0) {
            Set<String> ordered = new TreeSet<>(feedRequest.getTags());
            String[] tags = ordered.toArray(new String[ordered.size()]);
            key.append("/tags/").append(Utilities.stringArrayToString(tags,"+"));
        }

        return key.toString();
    }
}
