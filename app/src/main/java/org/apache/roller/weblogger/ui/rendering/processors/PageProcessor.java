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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.HitCountQueue;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.model.Model;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.weblogger.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rendering processor that provides access to weblog pages.
 *
 * General approach of most rendering processor, including this one:
 * <ul>
 * <li>Create a request object to parse the request</li>
 * <li>Determine last modified time, return not-modified (HTTP 304) if possible</li>
 * <li>If not, return content from cache if possible</li>
 * <li>If not, load model objects into a map suitable for renderer & call renderer to create content</li>
 * </ul>
 */
@RestController
@RequestMapping(path="/roller-ui/rendering/page/**")
public class PageProcessor {

    private static Log log = LogFactory.getLog(PageProcessor.class);

    public static final String PATH = "/roller-ui/rendering/page";

    private boolean excludeOwnerPages = false;
    private boolean processReferrers = true;
    private WeblogPageCache weblogPageCache = null;
    private SiteWideCache siteWideCache = null;

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @PostConstruct
    public void init() {
        this.excludeOwnerPages = WebloggerConfig.getBooleanProperty("cache.excludeOwnerEditPages");
        this.weblogPageCache = WeblogPageCache.getInstance();
        this.siteWideCache = SiteWideCache.getInstance();
        this.processReferrers = WebloggerConfig.getBooleanProperty("site.blacklist.check.referrers");
        log.info("PageProcessor: Referrer spam check enabled = " + this.processReferrers);
    }

    /**
     * Handle GET requests for weblog pages.
     */
    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Weblog weblog;
        boolean isSiteWide;

        WeblogPageRequest pageRequest;
        try {
            pageRequest = new WeblogPageRequest(request);

            weblog = pageRequest.getWeblog();
            if (weblog == null) {
                throw new WebloggerException("unable to lookup weblog: " + pageRequest.getWeblogHandle());
            }

            // is this the site-wide weblog?
            isSiteWide = WebloggerRuntimeConfig.isSiteWideWeblog(pageRequest.getWeblogHandle());

            if (this.processReferrers && !isSiteWide) {
                boolean spam = processReferrer(request, pageRequest);
                if (spam) {
                    log.debug("evaluated to be a spammer, returning a 403");
                    if (!response.isCommitted()) {
                        response.reset();
                    }
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
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

        // 304 Not Modified handling.
        // We skip this for logged in users to avoid the scenario where a user
        // views their weblog, logs in, then gets a 304 without the 'edit' links
        if (!pageRequest.isLoggedIn()) {
            if (ModDateHeaderUtil.respondIfNotModified(request, response,
                    lastModified, pageRequest.getDeviceType())) {
                return;
            } else {
                // set last-modified date
                ModDateHeaderUtil.setLastModifiedHeader(response, lastModified,
                        pageRequest.getDeviceType());
            }
        }

        // generate cache key
        String cacheKey;
        if (isSiteWide) {
            cacheKey = siteWideCache.generateKey(pageRequest);
        } else {
            cacheKey = weblogPageCache.generateKey(pageRequest);
        }

        // cached content checking
        if ((!this.excludeOwnerPages || !pageRequest.isLoggedIn())
                && request.getAttribute("skipCache") == null
                && request.getParameter("skipCache") == null) {

            CachedContent cachedContent;
            if (isSiteWide) {
                cachedContent = (CachedContent) siteWideCache.get(cacheKey);
            } else {
                cachedContent = (CachedContent) weblogPageCache.get(cacheKey,
                        lastModified);
            }

            if (cachedContent != null) {
                log.debug("HIT " + cacheKey);

                // allow for hit counting
                if (!isSiteWide
                        && (pageRequest.isWebsitePageHit() || pageRequest
                                .isOtherPageHit())) {
                    this.processHit(weblog);
                }

                response.setContentLength(cachedContent.getContent().length);
                response.setContentType(cachedContent.getContentType());
                response.getOutputStream().write(cachedContent.getContent());
                return;
            } else {
                log.debug("MISS " + cacheKey);
            }
        }

        log.debug("Looking for template to use for rendering");

        // figure out what template to use
        ThemeTemplate page = null;

        if ("page".equals(pageRequest.getContext())) {
            page = pageRequest.getWeblogPage();

            // if we don't have this page then 404, we don't let
            // this one fall through to the default template
            if (page == null) {
                if (!response.isCommitted()) {
                    response.reset();
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // If request specified tags section index, then look for custom
            // template
        } else if ("tags".equals(pageRequest.getContext())
                && pageRequest.getTags() != null) {
            try {
                page = weblog.getTheme().getTemplateByAction(
                        ComponentType.TAGSINDEX);
            } catch (Exception e) {
                log.error("Error getting weblog page for action 'tagsIndex'", e);
            }

            // if we don't have a custom tags page then 404, we don't let
            // this one fall through to the default template
            if (page == null) {
                if (!response.isCommitted()) {
                    response.reset();
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // If this is a permalink then look for a permalink template
        } else if (pageRequest.getWeblogAnchor() != null) {
            try {
                page = weblog.getTheme().getTemplateByAction(
                        ComponentType.PERMALINK);
            } catch (Exception e) {
                log.error("Error getting weblog page for action 'permalink'", e);
            }
        }

        // if we haven't found a page yet then try our default page
        if (page == null) {
            try {
                page = weblog.getTheme().getDefaultTemplate();
            } catch (Exception e) {
                log.error(
                        "Error getting default page for weblog = "
                                + weblog.getHandle(), e);
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
        if (pageRequest.getWeblogPageName() != null && page.isHidden()) {
            invalid = true;
        }
        if (pageRequest.getWeblogAnchor() != null) {

            // permalink specified.
            // entry must exist and be published before current time
            WeblogEntry entry = pageRequest.getWeblogEntry();
            if (entry == null) {
                invalid = true;
            } else if (!entry.isPublished()) {
                invalid = true;
            } else if (new Date().before(entry.getPubTime())) {
                invalid = true;
            }
        } else if (pageRequest.getWeblogCategoryName() != null) {

            // category specified. category must exist.
            if (pageRequest.getWeblogCategory() == null) {
                invalid = true;
            }
        } else if (pageRequest.getTags() != null
                && pageRequest.getTags().size() > 0) {

            try {
                // tags specified. make sure they exist.
                invalid = !weblogEntryManager.getTagComboExists(pageRequest.getTags(),
                        (isSiteWide) ? null : weblog);
            } catch (WebloggerException ex) {
                invalid = true;
            }
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
        if (!isSiteWide
                && (pageRequest.isWebsitePageHit() || pageRequest
                        .isOtherPageHit())) {
            this.processHit(weblog);
        }

        // looks like we need to render content
        // set the content deviceType
        String contentType;
        if (StringUtils.isNotEmpty(page.getOutputContentType())) {
            contentType = page.getOutputContentType() + "; charset=utf-8";
        } else {
            final String defaultContentType = "text/html; charset=utf-8";
            if (page.getLink() == null) {
                contentType = defaultContentType;
            } else {
                String mimeType = RollerContext.getServletContext().getMimeType(
                        page.getLink());
                if (mimeType != null) {
                    // we found a match ... set the content deviceType
                    contentType = mimeType + "; charset=utf-8";
                } else {
                    contentType = defaultContentType;
                }
            }
        }

        Map<String, Object> model;
        try {
            // special hack for menu tag
            request.setAttribute("pageRequest", pageRequest);

            // populate the rendering model
            Map<String, Object> initData = new HashMap<>();
            initData.put("requestParameters", request.getParameterMap());
            initData.put("parsedRequest", pageRequest);

            // if this was a comment posting, check for comment form
            WeblogEntryCommentForm commentForm = (WeblogEntryCommentForm) request
                    .getAttribute("commentForm");
            if (commentForm != null) {
                initData.put("commentForm", commentForm);
            }

            model = Model.getModelMap("pageModelSet", initData);

            // Load special models for site-wide blog
            if (isSiteWide) {
                model.putAll(Model.getModelMap("siteModelSet", initData));
            }

        } catch (WebloggerException ex) {
            log.error("Error loading model objects for page", ex);

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
            renderer = RendererManager.getRenderer(page,
                    pageRequest.getDeviceType());
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for page " + page.getId(), e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content
        CachedContent rendererOutput = new CachedContent(
                WebloggerCommon.TWENTYFOUR_KB_IN_BYTES, contentType);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for page " + page.getId(), e);

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
        if ((!this.excludeOwnerPages || !pageRequest.isLoggedIn())
                && request.getAttribute("skipCache") == null) {
            log.debug("PUT " + cacheKey);

            // put it in the right cache
            if (isSiteWide) {
                siteWideCache.put(cacheKey, rendererOutput);
            } else {
                weblogPageCache.put(cacheKey, rendererOutput);
            }
        } else {
            log.debug("SKIPPED " + cacheKey);
        }

        log.debug("Exiting");
    }

    /**
     * Handle POST requests.
     * 
     * We have this here because the comment servlet actually forwards some of
     * its requests on to us to render some pages with custom messaging. We may
     * want to revisit this approach in the future and see if we can do this in
     * a different way, but for now this is the easy way.
     */
    @RequestMapping(method = RequestMethod.POST)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // make sure caching is disabled
        request.setAttribute("skipCache", "true");

        // handle just like a GET request
        this.doGet(request, response);
    }

    /**
     * Notify the hit tracker that it has an incoming page hit.
     */
    private void processHit(Weblog weblog) {
        HitCountQueue counter = HitCountQueue.getInstance();
        counter.processHit(weblog);
    }

    /**
     * Process the incoming request to extract referrer info and pass it on to
     * the referrer processing queue for tracking.
     * 
     * @return true if referrer was spam, false otherwise
     */
    private boolean processReferrer(HttpServletRequest request, WeblogPageRequest pageRequest) {
        log.debug("processing referrer for " + request.getRequestURI());

        String referrerUrl = request.getHeader("Referer");
        StringBuffer reqsb = request.getRequestURL();
        if (request.getQueryString() != null) {
            reqsb.append("?");
            reqsb.append(request.getQueryString());
        }
        String requestUrl = reqsb.toString();
        log.debug("referrer = " + referrerUrl);

        // if this came from persons own blog then don't process it
        String selfSiteFragment = "/" + pageRequest.getWeblogHandle();
        if (referrerUrl != null && referrerUrl.contains(selfSiteFragment)) {
            log.debug("skipping referrer from own blog");
            return false;
        }

        // Base page URLs, with and without www.
        String basePageUrlWWW = WebloggerRuntimeConfig.getAbsoluteContextURL() + "/" + pageRequest.getWeblogHandle();
        String basePageUrl = basePageUrlWWW;
        if (basePageUrlWWW.startsWith("http://www.")) {
            // chop off the http://www.
            basePageUrl = "http://" + basePageUrlWWW.substring(11);
        }

        // ignore referrers coming from users own blog
        if (referrerUrl != null && !referrerUrl.startsWith(basePageUrl)
                && !referrerUrl.startsWith(basePageUrlWWW)) {

            // treat editor referral as direct
            int lastSlash = requestUrl.indexOf('/', 8);
            if (lastSlash == -1) {
                lastSlash = requestUrl.length();
            }
            String requestSite = requestUrl.substring(0, lastSlash);

            if (!referrerUrl.matches(requestSite + ".*\\.rol.*") &&
                    checkReferrer(pageRequest.getWeblog(), referrerUrl)) {
                return true;
            }
        } else {
            log.debug("Ignoring referer = " + referrerUrl);
            return false;
        }

        return false;
    }

    private boolean checkReferrer(Weblog weblog, String referrerURL) {
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Blacklist.populateSpamRules(
                weblog.getBlacklist(), WebloggerRuntimeConfig.getProperty("spam.blacklist"), stringRules, regexRules);
        return Blacklist.isBlacklisted(referrerURL, stringRules, regexRules);
    }

}
