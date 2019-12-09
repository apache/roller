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
package org.tightblog.rendering.controller;

import org.apache.commons.lang3.StringUtils;
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
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.dao.UserDao;
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
import org.tightblog.dao.WeblogDao;
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
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
@RequestMapping(path = PageController.PATH)
public class PageController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(PageController.class);

    public static final String PATH = "/tb-ui/rendering/page";

    private UserDao userDao;
    private WeblogDao weblogDao;
    private LazyExpiringCache weblogPageCache;
    private WeblogManager weblogManager;
    private WeblogEntryManager weblogEntryManager;
    private ThymeleafRenderer thymeleafRenderer;
    protected ThemeManager themeManager;
    private PageModel pageModel;
    private Function<WeblogPageRequest, SiteModel> siteModelFactory;
    private DynamicProperties dp;

    @Autowired
    PageController(WeblogDao weblogDao, LazyExpiringCache weblogPageCache,
                   WeblogManager weblogManager, WeblogEntryManager weblogEntryManager,
                   @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer,
                   ThemeManager themeManager, PageModel pageModel,
                   Function<WeblogPageRequest, SiteModel> siteModelFactory,
                   UserDao userDao, DynamicProperties dp) {
        this.userDao = userDao;
        this.weblogDao = weblogDao;
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

    @GetMapping(path = "/{weblogHandle}")
    ResponseEntity<Resource> getHomePage(@PathVariable String weblogHandle,
                                              HttpServletRequest request, Principal principal) {
        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);

        // weblog template
        return handleRequest(incomingRequest, null, request);
    }

    @RequestMapping(path = "/{weblogHandle}/entry/{anchor}", method = {RequestMethod.GET, RequestMethod.POST})
    ResponseEntity<Resource> getByEntry(@PathVariable String weblogHandle, @PathVariable String anchor,
                                        HttpServletRequest request, Principal principal) {
        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);
        incomingRequest.setWeblogEntryAnchor(Utilities.decode(anchor));

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());

        if (weblog == null) {
            return ResponseEntity.notFound().build();
        } else {
            incomingRequest.setWeblog(weblog);

            WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog,
                    incomingRequest.getWeblogEntryAnchor());

            if (entry == null || !entry.isPublished()) {
                log.warn("For weblog {}, invalid or not yet published entry {} requested, returning home page instead.",
                        weblogHandle, anchor);
                return getHomePage(weblogHandle, request, principal);
            } else {
                incomingRequest.setWeblogEntry(entry);
                incomingRequest.setTemplate(
                        themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.PERMALINK));
            }

            return handleRequest(incomingRequest, null, request);
        }
    }

    @GetMapping(path = "/{weblogHandle}/date/{date}")
    ResponseEntity<Resource> getByDate(@PathVariable String weblogHandle, @PathVariable String date,
                                       @RequestParam(value = "page", required = false) Integer page,
                                       HttpServletRequest request, Principal principal) {
        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);
        // discourage date-based URLs from appearing in search engines
        incomingRequest.setNoIndex(true);
        if (isValidDateString(date)) {
            incomingRequest.setWeblogDate(date);
        }

        return handleRequest(incomingRequest, null, request);
    }

    @GetMapping(path = "/{weblogHandle}/category/{category}")
    ResponseEntity<Resource> getByCategory(@PathVariable String weblogHandle, @PathVariable String category,
                                           @RequestParam(value = "page", required = false) Integer page,
                                           @RequestParam(value = "tag", required = false) String tag,
                                           HttpServletRequest request, Principal principal) {
        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);

        // TODO: Validate category
        incomingRequest.setCategory(category);
        if (tag != null) {
            incomingRequest.setTag(tag);
        }

        return handleRequest(incomingRequest, page, request);
    }

    @GetMapping(path = "/{weblogHandle}/tag/{tag}")
    ResponseEntity<Resource> getByTag(@PathVariable String weblogHandle, @PathVariable String tag,
                                      @RequestParam(value = "page", required = false) Integer page,
                                      HttpServletRequest request, Principal principal) {
        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);
        incomingRequest.setTag(tag);

        return handleRequest(incomingRequest, page, request);
    }

    @GetMapping(path = "/{weblogHandle}/page/{customPage}")
    ResponseEntity<Resource> getByCustomPage(@PathVariable String weblogHandle, @PathVariable String customPage,
                                             @RequestParam(value = "date", required = false) String date,
                                             HttpServletRequest hsr, Principal principal) {
        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);
        incomingRequest.setQueryString(hsr.getQueryString());
        incomingRequest.setRequest(hsr);

        incomingRequest.setCustomPageName(customPage);
        if (date != null && isValidDateString(date)) {
            incomingRequest.setWeblogDate(date);
        }

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());

        if (weblog == null) {
            return ResponseEntity.notFound().build();
        } else {
            incomingRequest.setWeblog(weblog);
            Template template = themeManager.getWeblogTheme(weblog).getTemplateByName(
                    incomingRequest.getCustomPageName());

            // block internal custom pages from appearing directly
            if (template != null && template.getRole().isAccessibleViaUrl()) {
                incomingRequest.setTemplate(template);
                return handleRequest(incomingRequest, null, hsr);
            } else {
                log.warn("For weblog {}, invalid or non-external page {} requested, returning home page instead.",
                        weblogHandle, customPage);
                return getHomePage(weblogHandle, hsr, principal);
            }
        }
    }

    private ResponseEntity<Resource> handleRequest(WeblogPageRequest incomingRequest, Integer pageNum,
                                                   HttpServletRequest request) {

        if (incomingRequest.getWeblog() == null) {
            Weblog weblog = weblogDao.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());

            if (weblog == null) {
                return ResponseEntity.notFound().build();
            } else {
                incomingRequest.setWeblog(weblog);
            }
        }

        weblogPageCache.incrementIncomingRequests();
        incomingRequest.setDeviceType(Utilities.getDeviceType(request));

        // TODO: handle pagenums in the callers
        if (pageNum != null) {
            incomingRequest.setPageNum(pageNum);
            if (pageNum > 0) {
                // only index first pages (i.e., those without this parameter)
                incomingRequest.setNoIndex(true);
            }
        }

        // is this the site-wide weblog?
        incomingRequest.setSiteWide(themeManager.getSharedTheme(incomingRequest.getWeblog().getTheme()).isSiteWide());
        Instant objectLastChanged = (incomingRequest.isSiteWide()) ?
                dp.getLastSitewideChange() : incomingRequest.getWeblog().getLastModified();

        // Respond with 304 Not Modified if it is not modified.
        // DB stores last modified in millis, browser if-modified-since in seconds, so need to truncate millis from the former.
        long inDb = objectLastChanged.truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        long inBrowser = getBrowserCacheExpireDate(request);

        if (inDb <= inBrowser) {
            weblogPageCache.incrementRequestsHandledBy304();
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        if (incomingRequest.getAuthenticatedUser() != null) {
            incomingRequest.setBlogger(userDao.findEnabledByUserName(incomingRequest.getAuthenticatedUser()));
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
            rendererOutput = weblogPageCache.get(cacheKey, objectLastChanged);
        }

        if (rendererOutput == null) {
            newContent = true;

            // use default template if not yet earlier determined
            if (incomingRequest.getTemplate() == null) {
                incomingRequest.setTemplate(
                        themeManager.getWeblogTheme(incomingRequest.getWeblog()).getTemplateByRole(Role.WEBLOG));
            }

            if (incomingRequest.getTemplate() == null) {
                log.warn("For weblog {}, no WEBLOG template defined, returning 404",
                        incomingRequest.getWeblog());
                return ResponseEntity.notFound().build();
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

            try {
                // render content
                rendererOutput = thymeleafRenderer.render(incomingRequest.getTemplate(), model);
            } catch (Exception e) {
                log.error("Rendering error for {}", incomingRequest, e);
            }
        }

        if (rendererOutput != null) {
            if (commentForm == null && rendererOutput.getRole().isIncrementsHitCount()) {
                weblogManager.incrementHitCount(incomingRequest.getWeblog());
            }

            if (newContent && cacheKey != null) {
                log.debug("PUT {}", cacheKey);
                weblogPageCache.put(cacheKey, rendererOutput);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(rendererOutput.getRole().getContentType()))
                    .contentLength(rendererOutput.getContent().length)
                    .lastModified(objectLastChanged.toEpochMilli())
                    // no-cache: browser may cache but must validate with server each time before using (check for 304 response)
                    .cacheControl(CacheControl.noCache())
                    .body(new ByteArrayResource(rendererOutput.getContent()));
        } else {
            log.error("Unable to rendering anything for {}, returning 404", incomingRequest);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Generate a cache key from a parsed weblog page request.
     * TODO: Handle in the calling methods
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

    public static boolean isValidDateString(String dateString) {
        boolean valid = false;

        if (StringUtils.isNumeric(dateString) && (dateString.length() == 6 || dateString.length() == 8)) {
            try {
                if (dateString.length() == 6) {
                    LocalDate.parse(dateString + "01", Utilities.YMD_FORMATTER);
                } else {
                    LocalDate.parse(dateString, Utilities.YMD_FORMATTER);
                }
                valid = true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return valid;
    }

}
