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

import org.apache.commons.lang3.StringUtils;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.rendering.Renderer;
import org.tightblog.rendering.RendererManager;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.Utilities;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.cache.SiteWideCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Rendering processor that provides access to weblog pages.
 * <p>
 * General approach of most rendering processor, including this one:
 * <ul>
 * <li>Create a request object to parse the request</li>
 * <li>Determine last modified time, return not-modified (HTTP 304) if possible</li>
 * <li>If not, return content from cache if possible</li>
 * <li>If not, load model objects into a map suitable for renderer & call renderer to create content</li>
 * </ul>
 */
@RestController
@RequestMapping(path = "/tb-ui/rendering/page/**")
public class PageProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(PageProcessor.class);

    public static final String PATH = "/tb-ui/rendering/page";

    @Autowired
    private LazyExpiringCache weblogPageCache = null;

    public void setWeblogPageCache(LazyExpiringCache weblogPageCache) {
        this.weblogPageCache = weblogPageCache;
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
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private RendererManager rendererManager = null;

    public void setRendererManager(RendererManager rendererManager) {
        this.rendererManager = rendererManager;
    }

    @Autowired
    protected ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    // Weblog pages shown to logged-in users are frequently different from unauthed readers (e.g., have
    // different left-side menus allowing for blog administration) so need to be cached additionally.
    // Set cacheLoggedInPages to "false" to not cache these pages.  Recommended to keep true,
    // but false can be useful when debugging templates and want to trace the page rendering instead
    // of just retrieving the page from the cache, or if it is otherwise undesired to have logged-in
    // pages consuming any part of the WeblogPageCache.
    private boolean cacheLoggedInPages = true;

    @Autowired(required = false)
    public void setCacheLoggedInPages(@Qualifier("cache.cacheLoggedInPages") boolean boolVal) {
        cacheLoggedInPages = boolVal;
    }

    /**
     * Handle GET requests for weblog pages.
     */
    @RequestMapping(method = RequestMethod.GET)
    public void getPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Weblog weblog;
        boolean isSiteWide;

        WeblogPageRequest pageRequest;
        try {
            pageRequest = new WeblogPageRequest(request);

            weblog = pageRequest.getWeblog();
            if (weblog == null) {
                throw new IllegalStateException("unable to lookup weblog: " + pageRequest.getWeblogHandle());
            }

            // is this the site-wide weblog?
            isSiteWide = themeManager.getSharedTheme(pageRequest.getWeblog().getTheme()).isSiteWide();

        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // determine the lastModified date for this content
        long lastModified = Clock.systemDefaultZone().millis();
        if (isSiteWide) {
            lastModified = siteWideCache.getLastModified().toEpochMilli();
        } else if (weblog.getLastModified() != null) {
            lastModified = weblog.getLastModified().toEpochMilli();
        }

        // 304 Not Modified handling.
        // We skip this for logged in users to avoid the scenario where a user
        // views their weblog, logs in, then gets a 304 without the 'edit' links
        if (!pageRequest.isLoggedIn()) {
            if (Utilities.respondIfNotModified(request, response, lastModified, pageRequest.getDeviceType())) {
                return;
            } else {
                // set last-modified date
                Utilities.setLastModifiedHeader(response, lastModified, pageRequest.getDeviceType());
            }
        }

        // generate cache key
        String cacheKey = generateKey(pageRequest);

        // cached content checking, bypass cache if a comment form is present
        // i.e., request came from CommentProcessor and comment submission feedback/preview,
        // etc. is needed.
        WeblogEntryComment commentForm = (WeblogEntryComment) request.getAttribute("commentForm");

        if (commentForm == null && (cacheLoggedInPages || !pageRequest.isLoggedIn())) {

            CachedContent cachedContent;
            if (isSiteWide) {
                cachedContent = (CachedContent) siteWideCache.get(cacheKey);
            } else {
                cachedContent = (CachedContent) weblogPageCache.get(cacheKey, lastModified);
            }

            if (cachedContent != null) {
                log.debug("HIT {}", cacheKey);

                // allow for hit counting
                if (!isSiteWide && pageRequest.isWeblogPageHit()) {
                    this.processHit(weblog);
                }

                response.setContentLength(cachedContent.getContent().length);
                response.setContentType(cachedContent.getContentType());
                response.getOutputStream().write(cachedContent.getContent());
                return;
            } else {
                log.debug("MISS {}", cacheKey);
            }
        }

        log.debug("Looking for template to use for rendering");

        // figure out what template to use
        Template page = null;

        if ("page".equals(pageRequest.getContext())) {
            page = pageRequest.getWeblogTemplate();

            // if we don't have this page then 404, we don't let
            // this one fall through to the default template
            if (page == null) {
                if (!response.isCommitted()) {
                    response.reset();
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

        // If request specified tags section index, then look for custom template
        } else if ("tags".equals(pageRequest.getContext()) && pageRequest.getTag() != null) {
            try {
                page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.TAGSINDEX);
            } catch (Exception e) {
                log.error("Error getting weblog page for action 'tagsIndex'", e);
            }

        // If this is a permalink then look for a permalink template
        } else if (pageRequest.getWeblogEntryAnchor() != null) {
            try {
                page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.PERMALINK);
            } catch (Exception e) {
                log.error("Error getting weblog page for action 'permalink'", e);
            }
        }

        // if we haven't found a page yet then try our default page
        if (page == null) {
            try {
                page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.WEBLOG);
            } catch (Exception e) {
                log.error("Error getting default page for weblog = {}", weblog.getHandle(), e);
            }
        }

        // Still no page? Then that is a 404
        if (page == null) {
            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        log.debug("page found, dealing with it");

        // validation. make sure that request input makes sense.
        boolean invalid = false;
        if (pageRequest.getWeblogTemplateName() != null && StringUtils.isEmpty(page.getRelativePath())) {
            invalid = true;
        }
        if (pageRequest.getWeblogEntryAnchor() != null) {

            // permalink specified.
            // entry must exist and be published before current time
            WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, pageRequest.getWeblogEntryAnchor());

            if (entry == null) {
                invalid = true;
            } else if (!entry.isPublished()) {
                invalid = true;
            } else if (Instant.now().isBefore(entry.getPubTime())) {
                invalid = true;
            }
        } else if (pageRequest.getWeblogCategoryName() != null) {
            // category specified. category must exist.
            WeblogCategory test = weblogManager.getWeblogCategoryByName(pageRequest.getWeblog(),
                    pageRequest.getWeblogCategoryName());
            if (test == null) {
                invalid = true;
            }
        } else if (pageRequest.getTag() != null) {
            // tags specified. make sure they exist.
            invalid = !weblogManager.getTagExists((isSiteWide) ? null : weblog, pageRequest.getTag());
        }

        if (invalid) {
            log.debug("page failed validation, bailing out");
            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // allow for hit counting
        if (!isSiteWide && pageRequest.isWeblogPageHit()) {
            this.processHit(weblog);
        }

        // looks like we need to render content
        String contentType = page.getRole().getContentType() + "; charset=utf-8";

        Map<String, Object> model;

        // special hack for menu tag
        request.setAttribute("pageRequest", pageRequest);

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("requestParameters", request.getParameterMap());
        initData.put("parsedRequest", pageRequest);

        // if this GET is for a comment preview, store the comment form
        if (commentForm != null) {
            initData.put("commentForm", commentForm);
        }

        model = getModelMap("pageModelSet", initData);

        // Load special models for site-wide blog
        if (isSiteWide) {
            model.putAll(getModelMap("siteModelSet", initData));
        }

        // lookup Renderer we are going to use
        Renderer renderer;
        try {
            log.debug("Looking up renderer");
            renderer = rendererManager.getRenderer(page, pageRequest.getDeviceType());
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for page {}", page.getId(), e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content
        CachedContent rendererOutput = new CachedContent(
                Utilities.TWENTYFOUR_KB_IN_BYTES, contentType);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for page {}", page.getId(), e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // post rendering process
        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentType(contentType);
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());

        // cache rendered content. only cache if user is not logged in?
        if ((cacheLoggedInPages || !pageRequest.isLoggedIn()) && request.getAttribute("skipCache") == null) {
            log.debug("PUT {}", cacheKey);

            // put it in the right cache
            if (isSiteWide) {
                siteWideCache.put(cacheKey, rendererOutput);
            } else {
                weblogPageCache.put(cacheKey, rendererOutput);
            }
        } else {
            log.debug("SKIPPED {}", cacheKey);
        }

        log.debug("Exiting");
    }

    /**
     * Handle POST requests.
     * <p>
     * We have this here because the comment servlet actually forwards some of
     * its requests on to us to render some pages with custom messaging. We may
     * want to revisit this approach in the future and see if we can do this in
     * a different way, but for now this is the easy way.
     */
    @RequestMapping(method = RequestMethod.POST)
    public void getPageViaPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // make sure caching is disabled
        request.setAttribute("skipCache", "true");

        // handle just like a GET request
        this.getPage(request, response);
    }

    /**
     * Notify the hit tracker that it has an incoming page hit.
     */
    private void processHit(Weblog weblog) {
        weblogManager.incrementHitCount(weblog);
    }

    /**
     * Generate a cache key from a parsed weblog page request.
     */
    String generateKey(WeblogPageRequest request) {
        StringBuilder key = new StringBuilder();

        key.append("weblogpage.key").append(":");
        key.append(request.getWeblogHandle());

        if (request.getWeblogEntryAnchor() != null) {
            key.append("/entry/").append(request.getWeblogEntryAnchor());
        } else {
            if (request.getWeblogTemplateName() != null) {
                key.append("/page/").append(request.getWeblogTemplateName());
            }

            if (request.getWeblogDate() != null) {
                key.append("/date/").append(request.getWeblogDate());
            }

            if (request.getWeblogCategoryName() != null) {
                String cat = request.getWeblogCategoryName();
                cat = Utilities.encode(cat);
                key.append("/cat/").append(cat);
            }

            if (request.getTag() != null) {
                String tag = request.getTag();
                tag = Utilities.encode(tag);
                key.append("/tag/").append(tag);
            }

            if (request.getPageNum() > 0) {
                key.append("/page=").append(request.getPageNum());
            }
        }

        // add login state
        if (request.getAuthenticatedUser() != null) {
            key.append("/user=").append(request.getAuthenticatedUser());
        }

        key.append("/deviceType=").append(request.getDeviceType().toString());

        return key.toString();
    }
}
