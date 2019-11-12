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
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.model.SearchResultsModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.rendering.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles search queries for weblogs.
 */
@RestController
@RequestMapping(path = "/tb-ui/rendering/search/**")
public class SearchProcessor extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(SearchProcessor.class);

    public static final String PATH = "/tb-ui/rendering/search";

    private WeblogDao weblogDao;
    private ThymeleafRenderer thymeleafRenderer;
    private ThemeManager themeManager;
    private SearchResultsModel searchResultsModel;
    private Function<WeblogPageRequest, SiteModel> siteModelFactory;

    @Autowired
    SearchProcessor(WeblogDao weblogDao,
                    @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer, ThemeManager themeManager,
                    SearchResultsModel searchResultsModel,
                    Function<WeblogPageRequest, SiteModel> siteModelFactory) {
        this.weblogDao = weblogDao;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.searchResultsModel = searchResultsModel;
        this.siteModelFactory = siteModelFactory;
    }

    @RequestMapping(method = RequestMethod.GET)
    void getSearchResults(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WeblogPageRequest searchRequest = WeblogSearchRequest.create(request, searchResultsModel);

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(searchRequest.getWeblogHandle());
        if (weblog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            searchRequest.setWeblog(weblog);
        }

        // determine template to use for rendering, look for search results override first
        WeblogTheme weblogTheme = themeManager.getWeblogTheme(weblog);

        searchRequest.setTemplate(weblogTheme.getTemplateByRole(Template.Role.SEARCH_RESULTS));

        if (searchRequest.getTemplate() == null) {
            searchRequest.setTemplate(weblogTheme.getTemplateByRole(Template.Role.WEBLOG));
        }

        if (searchRequest.getTemplate() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", searchRequest);

        Map<String, Object> model = getModelMap("pageModelSet", initData);

        // Load special models for site-wide blog
        if (themeManager.getSharedTheme(weblog.getTheme()).isSiteWide()) {
            model.put("site", siteModelFactory.apply(searchRequest));
        }
        model.put("model", searchRequest);

        // render content
        try {
            CachedContent rendererOutput = thymeleafRenderer.render(searchRequest.getTemplate(), model);
            response.setContentType(rendererOutput.getRole().getContentType());
            response.setContentLength(rendererOutput.getContent().length);
            response.getOutputStream().write(rendererOutput.getContent());
        } catch (Exception e) {
            log.error("Error during rendering of template {}", searchRequest.getTemplate().getId(), e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
