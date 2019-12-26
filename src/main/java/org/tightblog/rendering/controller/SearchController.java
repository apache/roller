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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.model.SearchResultsModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.service.ThymeleafRenderer;
import org.tightblog.rendering.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogDao;
import org.tightblog.util.Utilities;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles search queries for weblogs.
 */
@RestController
@RequestMapping(path = SearchController.PATH)
public class SearchController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(SearchController.class);

    public static final String PATH = "/tb-ui/rendering/search";

    private WeblogDao weblogDao;
    private ThymeleafRenderer thymeleafRenderer;
    private ThemeManager themeManager;
    private SearchResultsModel searchResultsModel;
    private Function<WeblogPageRequest, SiteModel> siteModelFactory;

    @Autowired
    SearchController(WeblogDao weblogDao,
                     @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer, ThemeManager themeManager,
                     SearchResultsModel searchResultsModel,
                     Function<WeblogPageRequest, SiteModel> siteModelFactory) {
        this.weblogDao = weblogDao;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.searchResultsModel = searchResultsModel;
        this.siteModelFactory = siteModelFactory;
    }

    @GetMapping(path = "/{weblogHandle}")
    ResponseEntity<Resource> getSearchResults(@PathVariable String weblogHandle,
                                              @RequestParam(value = "q") String query,
                                              @RequestParam(value = "cat", required = false) String category,
                                              @RequestParam(value = "page", required = false) Integer pageNum,
                                              Principal principal, Device device) {
        WeblogSearchRequest searchRequest = new WeblogSearchRequest(weblogHandle, principal, searchResultsModel);
        searchRequest.setDeviceType(Utilities.getDeviceType(device));

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(searchRequest.getWeblogHandle());

        if (weblog == null) {
            return ResponseEntity.notFound().build();
        } else {
            searchRequest.setWeblog(weblog);
        }

        searchRequest.setCategory(category);
        searchRequest.setSearchPhrase(query);
        searchRequest.setNoIndex(true);

        if (pageNum != null) {
            searchRequest.setPageNum(pageNum);
        }

        // determine template to use for rendering, look for search results override first
        WeblogTheme weblogTheme = themeManager.getWeblogTheme(weblog);

        searchRequest.setTemplate(weblogTheme.getTemplateByRole(Template.Role.SEARCH_RESULTS));

        if (searchRequest.getTemplate() == null) {
            searchRequest.setTemplate(weblogTheme.getTemplateByRole(Template.Role.WEBLOG));
        }

        if (searchRequest.getTemplate() == null) {
            return ResponseEntity.notFound().build();
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
            return ResponseEntity.ok().contentType(MediaType.valueOf(rendererOutput.getRole().getContentType()))
                    .contentLength(rendererOutput.getContent().length)
                    .body(new ByteArrayResource(rendererOutput.getContent()));
        } catch (Exception e) {
            log.error("Error during rendering of template {}", searchRequest.getTemplate().getId(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
