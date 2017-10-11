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
 * Rendering processor that provides access to weblog pages.
 * <p>
 * General approach of most rendering processors including this one:
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

    private WeblogPageRequest.Creator weblogPageRequestCreator;

    public PageProcessor() {
        this.weblogPageRequestCreator = new WeblogPageRequest.Creator();
    }

    public void setWeblogPageRequestCreator(WeblogPageRequest.Creator creator) {
        this.weblogPageRequestCreator = creator;
    }

    /**
     * Handle requests for weblog pages. GETs are for standard read-only retrieval of blog pages,
     * POSTs are for handling responses from the CommentProcessor, those will have a commentForm
     * attribute that translates to a WeblogEntryComment instance containing the comment.
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void getPage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        WeblogPageRequest incomingRequest = weblogPageRequestCreator.create(request);

        Weblog weblog = weblogManager.getWeblogByHandle(incomingRequest.getWeblogHandle(), true);
        if (weblog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblog(weblog);
        }

        // is this the site-wide weblog?
        boolean isSiteWide = themeManager.getSharedTheme(incomingRequest.getWeblog().getTheme()).isSiteWide();

        // 304 Not Modified handling.
        // We skip this for logged in users to avoid the scenario where a user
        // views their weblog, logs in, then gets a 304 without the 'edit' links
        Instant lastModified = (isSiteWide) ? siteWideCache.getLastModified() : weblog.getLastModified();

        if (!incomingRequest.isLoggedIn()) {
            if (respondIfNotModified(request, response, lastModified, incomingRequest.getDeviceType())) {
                return;
            } else {
                // set last-modified date
                setLastModifiedHeader(response, lastModified, incomingRequest.getDeviceType());
            }
        }
// HERE #1
        // generate cache key
        String cacheKey = generateKey(incomingRequest);

        // Check cache for content except during comment feedback/preview (i.e., commentForm present)
        WeblogEntryComment commentForm = (WeblogEntryComment) request.getAttribute("commentForm");

        if (commentForm == null) {

            CachedContent cachedContent;
            if (isSiteWide) {
                cachedContent = (CachedContent) siteWideCache.get(cacheKey);
            } else {
                cachedContent = (CachedContent) weblogPageCache.get(cacheKey, lastModified);
            }

            if (cachedContent != null) {
                log.debug("HIT {}", cacheKey);

                // allow for hit counting
                if (incomingRequest.isWeblogPageHit()) {
                    weblogManager.incrementHitCount(weblog);
                }

                response.setContentLength(cachedContent.getContent().length);
                response.setContentType(cachedContent.getContentType());
                response.getOutputStream().write(cachedContent.getContent());
                return;
            } else {
                log.debug("MISS {}", cacheKey);
            }
        }
// HERE #2

        // not using cache so need to generate page from scratch
        // figure out what template to use
        if ("page".equals(incomingRequest.getContext())) {
            Template template = themeManager.getWeblogTheme(weblog).getTemplateByPath(incomingRequest.getWeblogTemplateName());

            // block internal custom pages from appearance
            if (!StringUtils.isEmpty(template.getRelativePath())) {
                incomingRequest.setTemplate(template);
            }
        } else {
            boolean invalid = false;

            if (incomingRequest.getWeblogEntryAnchor() != null) {
                WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, incomingRequest.getWeblogEntryAnchor());

                if (entry == null || !entry.isPublished() || Instant.now().isBefore(entry.getPubTime())) {
                    invalid = true;
                } else {
                    incomingRequest.setWeblogEntry(entry);
                    incomingRequest.setTemplate(themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.PERMALINK));
                }
            }

            // use default template for other contexts (or, for entries, if PERMALINK template is undefined)
            if (!invalid && incomingRequest.getTemplate() == null) {
                incomingRequest.setTemplate(themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.WEBLOG));
            }
        }

        if (incomingRequest.getTemplate() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

// HERE #3

        // allow for hit counting
        if (incomingRequest.isWeblogPageHit()) {
            weblogManager.incrementHitCount(weblog);
        }

// HERE #4

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", incomingRequest);

        // if we're handling comments, add the comment form
        if (commentForm != null) {
            initData.put("commentForm", commentForm);
        }

        Map<String, Object> model = getModelMap("pageModelSet", initData);

        // Load special models for site-wide blog
        if (isSiteWide) {
            model.putAll(getModelMap("siteModelSet", initData));
        }

// HERE #5

        try {
            // lookup Renderer we are going to use
            Renderer renderer = rendererManager.getRenderer(incomingRequest.getTemplate(), incomingRequest.getDeviceType());

            // render content
            String contentType = incomingRequest.getTemplate().getRole().getContentType() + "; charset=utf-8";
            CachedContent rendererOutput = new CachedContent(Utilities.TWENTYFOUR_KB_IN_BYTES, contentType);
            renderer.render(model, rendererOutput.getCachedWriter());
            rendererOutput.flush();
            rendererOutput.close();

            // flush rendered content to response
            log.debug("Flushing response output");
            response.setContentType(contentType);
            response.setContentLength(rendererOutput.getContent().length);
            response.getOutputStream().write(rendererOutput.getContent());

// HERE #6

            // if not comment handling, add rendered content to cache
            if (commentForm == null) {
                log.debug("PUT {}", cacheKey);

                if (isSiteWide) {
                    siteWideCache.put(cacheKey, rendererOutput);
                } else {
                    weblogPageCache.put(cacheKey, rendererOutput);
                }
            }

        } catch (Exception e) {
// HERE #5
            log.error("Error during rendering for page {}", incomingRequest.getTemplate().getId(), e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    /**
     * Generate a cache key from a parsed weblog page request.
     */
    static String generateKey(WeblogPageRequest request) {
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
