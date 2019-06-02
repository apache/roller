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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.tightblog.config.DynamicProperties;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.repository.UserRepository;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Template.Role;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.repository.WeblogRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
@EnableConfigurationProperties(DynamicProperties.class)
@RequestMapping(path = "/tb-ui/rendering/page/**")
public class PageProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(PageProcessor.class);

    public static final String PATH = "/tb-ui/rendering/page";

    private UserRepository userRepository;
    private WeblogRepository weblogRepository;
    private LazyExpiringCache weblogPageCache;
    private WeblogManager weblogManager;
    private WeblogEntryManager weblogEntryManager;
    private ThymeleafRenderer thymeleafRenderer;
    protected ThemeManager themeManager;
    private PageModel pageModel;
    private Function<WeblogPageRequest, SiteModel> siteModelFactory;
    private DynamicProperties dp;

    @Autowired
    PageProcessor(WeblogRepository weblogRepository, LazyExpiringCache weblogPageCache,
                  WeblogManager weblogManager, WeblogEntryManager weblogEntryManager,
                  @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer,
                  ThemeManager themeManager, PageModel pageModel,
                  Function<WeblogPageRequest, SiteModel> siteModelFactory,
                  UserRepository userRepository, DynamicProperties dp) {
        this.userRepository = userRepository;
        this.weblogRepository = weblogRepository;
        this.weblogPageCache = weblogPageCache;
        this.weblogManager = weblogManager;
        this.weblogEntryManager = weblogEntryManager;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.pageModel = pageModel;
        this.siteModelFactory = siteModelFactory;
        this.dp = dp;
    }

    /**
     * Handle requests for weblog pages. GETs are for standard read-only retrieval of blog pages,
     * POSTs are for handling responses from the CommentProcessor, those will have a commentForm
     * attribute that translates to a WeblogEntryComment instance containing the comment.
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WeblogPageRequest incomingRequest = WeblogPageRequest.Creator.create(request, pageModel);

        Weblog weblog = weblogRepository.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());
        if (weblog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblog(weblog);
        }

        weblogPageCache.incrementIncomingRequests();

        // is this the site-wide weblog?
        incomingRequest.setSiteWide(themeManager.getSharedTheme(incomingRequest.getWeblog().getTheme()).isSiteWide());
        Instant lastModified = (incomingRequest.isSiteWide()) ? dp.getLastSitewideChange() : weblog.getLastModified();

        // Respond with 304 Not Modified if it is not modified.
        // DB stores last modified in millis, browser if-modified-since in seconds, so need to truncate millis from the former.
        long inDb = lastModified.truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        long inBrowser = getBrowserCacheExpireDate(request);

        if (inDb <= inBrowser) {
            weblogPageCache.incrementRequestsHandledBy304();
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        if (incomingRequest.getAuthenticatedUser() != null) {
            incomingRequest.setBlogger(userRepository.findEnabledByUserName(incomingRequest.getAuthenticatedUser()));
        }

        // cache key to retrieve earlier generated content
        String cacheKey = null;

        // Check cache for content except during comment feedback/preview (i.e., commentForm present)
        WeblogEntryComment commentForm = (WeblogEntryComment) request.getAttribute("commentForm");

        // pages containing user-specific comment forms aren't cached
        CachedContent rendererOutput = null;
        boolean newContent = false;
        if (commentForm == null) {
            cacheKey = generateKey(incomingRequest);
            rendererOutput = weblogPageCache.get(cacheKey, lastModified);
        }

        try {
            if (rendererOutput == null) {
                newContent = true;

                // not using cache so need to generate page from scratch
                // figure out what template to use
                if (incomingRequest.getCustomPageName() != null) {
                    Template template = themeManager.getWeblogTheme(weblog).getTemplateByName(
                            incomingRequest.getCustomPageName());

                    // block internal custom pages from appearing directly
                    if (template != null && template.getRole().isAccessibleViaUrl()) {
                        incomingRequest.setTemplate(template);
                    }
                } else {
                    boolean invalid = false;

                    if (incomingRequest.getWeblogEntryAnchor() != null) {
                        WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog,
                                incomingRequest.getWeblogEntryAnchor());

                        if (entry == null || !entry.isPublished()) {
                            invalid = true;
                        } else {
                            incomingRequest.setWeblogEntry(entry);
                            incomingRequest.setTemplate(
                                    themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.PERMALINK));
                        }
                    }

                    // use default template for other contexts (or, for entries, if PERMALINK template is undefined)
                    if (!invalid && incomingRequest.getTemplate() == null) {
                        incomingRequest.setTemplate(
                                themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.WEBLOG));
                    }
                }

                if (incomingRequest.getTemplate() == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // populate the rendering model
                Map<String, Object> initData = new HashMap<>();
                initData.put("parsedRequest", incomingRequest);

                // if we're handling comments, add the comment form
                if (commentForm != null) {
                    incomingRequest.setCommentForm(commentForm);
                }

                Map<String, Object> model = getModelMap("pageModelSet", initData);
                model.put("model", incomingRequest);

                // Load special models for site-wide blog
                if (incomingRequest.isSiteWide()) {
                    model.put("site", siteModelFactory.apply(incomingRequest));
                }

                // render content
                rendererOutput = thymeleafRenderer.render(incomingRequest.getTemplate(), model);
            }

            // write rendered content to response
            response.setContentType(rendererOutput.getRole().getContentType());
            response.setContentLength(rendererOutput.getContent().length);
            // no-cache: browser may cache but must validate with server each time before using (check for 304 response)
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Last-Modified", lastModified.toEpochMilli());
            response.getOutputStream().write(rendererOutput.getContent());

            if (rendererOutput.getRole().isIncrementsHitCount()) {
                weblogManager.incrementHitCount(weblog);
            }

            if (newContent && cacheKey != null) {
                log.debug("PUT {}", cacheKey);
                weblogPageCache.put(cacheKey, rendererOutput);
            }

        } catch (Exception e) {
            log.error("Error during rendering for {}", incomingRequest, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Generate a cache key from a parsed weblog page request.
     */
    String generateKey(WeblogPageRequest request) {
        StringBuilder key = new StringBuilder();
        key.append(request.getWeblogHandle());

        if (request.getWeblogEntryAnchor() != null) {
            key.append("/entry/").append(request.getWeblogEntryAnchor());
        } else {
            if (request.getCustomPageName() != null) {
                key.append("/page/").append(request.getCustomPageName());
            }

            if (request.getWeblogDate() != null) {
                key.append("/date/").append(request.getWeblogDate());
            }

            if (request.getCategory() != null) {
                String cat = request.getCategory();
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

            if (!StringUtils.isBlank(request.getQueryString())) {
                key.append("/query=").append(request.getQueryString());
            }
        }

        // add login state
        if (request.getAuthenticatedUser() != null) {
            key.append("/user=").append(request.getAuthenticatedUser());
        }

        key.append("/deviceType=").append(request.getDeviceType().toString());

        // site wide feeds must be aware of the last update date of any weblog
        // as they get refreshed whenever any of blogs do.
        if (request.isSiteWide()) {
            key.append("/lastUpdate=").append(dp.getLastSitewideChange().toEpochMilli());
        }
        return key.toString();
    }
}
