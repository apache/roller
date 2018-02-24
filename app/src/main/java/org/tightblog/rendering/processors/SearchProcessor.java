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
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Weblog;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.rendering.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles search queries for weblogs.
 */
@RestController
@RequestMapping(path = "/tb-ui/rendering/search/**")
public class SearchProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(SearchProcessor.class);

    public static final String PATH = "/tb-ui/rendering/search";

    @Autowired
    @Qualifier("blogRenderer")
    private ThymeleafRenderer thymeleafRenderer = null;

    public void setThymeleafRenderer(ThymeleafRenderer thymeleafRenderer) {
        this.thymeleafRenderer = thymeleafRenderer;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    protected ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing SearchProcessor...");
    }

    @RequestMapping(method = RequestMethod.GET)
    public void getSearchResults(HttpServletRequest request, HttpServletResponse response) throws IOException {

        log.debug("Entering");

        Weblog weblog;
        WeblogSearchRequest searchRequest;

        // first off lets parse the incoming request and validate it
        try {
            searchRequest = new WeblogSearchRequest(request);

            weblog = weblogManager.getWeblogByHandle(searchRequest.getWeblogHandle(), true);
            if (weblog == null) {
                log.info("Weblog not found: {}", searchRequest.getWeblogHandle());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            } else {
                searchRequest.setWeblog(weblog);
            }

        } catch (Exception e) {
            // invalid search request format or weblog doesn't exist
            log.debug("Error creating weblog search request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // lookup template to use for rendering, look for search results override first
        // try looking for a specific search page
        Template page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.SEARCH_RESULTS);

        // if not found then fall back on default page
        if (page == null) {
            page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.WEBLOG);
        }

        // if still null then that's a problem
        if (page == null) {
            throw new IllegalStateException("Could not lookup default page for weblog " + weblog.getHandle());
        }

        // set the content type
        response.setContentType("text/html; charset=utf-8");

        // looks like we need to render content
        Map<String, Object> model;
        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("request", request);

        // We need the 'parsedRequest' to be a WeblogPageRequest so other models
        // used in a search are properly loaded, which means that searchRequest
        // needs its own custom initData property aside from the standard
        // weblogRequest.
        WeblogPageRequest pageRequest = new WeblogPageRequest();
        pageRequest.setWeblog(searchRequest.getWeblog());
        pageRequest.setWeblogHandle(searchRequest.getWeblogHandle());
        pageRequest.setWeblogCategoryName(searchRequest.getWeblogCategoryName());
        pageRequest.setDeviceType(searchRequest.getDeviceType());
        initData.put("parsedRequest", pageRequest);
        initData.put("searchRequest", searchRequest);

        // Load models for pages
        model = getModelMap("searchModelSet", initData);

        // Load special models for site-wide blog
        if (themeManager.getSharedTheme(weblog.getTheme()).isSiteWide()) {
            model.putAll(getModelMap("siteModelSet", initData));
        }

        // render content
        try {
            CachedContent rendererOutput = thymeleafRenderer.render(page, model, "text/html");
            response.setContentType(rendererOutput.getContentType());
            response.setContentLength(rendererOutput.getContent().length);
            response.getOutputStream().write(rendererOutput.getContent());
        } catch (Exception e) {
            log.error("Error during rendering for search template", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
