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

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogSearchRequest;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CachedContent;
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
    private RendererManager rendererManager = null;

    public void setRendererManager(RendererManager rendererManager) {
        this.rendererManager = rendererManager;
    }

    @Autowired
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
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

            // now make sure the specified weblog really exists
            weblog = searchRequest.getWeblog();
            if (weblog == null) {
                log.info("Weblog not found: {}", searchRequest.getWeblogHandle());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (Exception e) {
            // invalid search request format or weblog doesn't exist
            log.debug("Error creating weblog search request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // lookup template to use for rendering
        Template page = null;
        try {

            // try looking for a specific search page
            page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.SEARCH);

            // if not found then fall back on default page
            if (page == null) {
                page = themeManager.getWeblogTheme(weblog).getTemplateByAction(Template.ComponentType.WEBLOG);
            }

            // if still null then that's a problem
            if (page == null) {
                throw new IllegalStateException("Could not lookup default page for weblog " + weblog.getHandle());
            }
        } catch (Exception e) {
            log.error("Error getting default page for weblog {}", weblog.getHandle(), e);
        }

        // set the content type
        response.setContentType("text/html; charset=utf-8");

        // looks like we need to render content
        Map<String, Object> model;
        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("request", request);

        // We need the 'parsedRequest' to be a pageRequest so other models
        // used in a search are properly loaded, which means that searchRequest
        // needs its own custom initData property aside from the standard
        // weblogRequest.
        WeblogPageRequest pageRequest = new WeblogPageRequest();
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

        // lookup Renderer we are going to use
        Renderer renderer;
        try {
            log.debug("Looking up renderer");
            renderer = rendererManager.getRenderer(page, searchRequest.getDeviceType());
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for rsd template", e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content
        CachedContent rendererOutput = new CachedContent(Utilities.EIGHT_KB_IN_BYTES);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for rsd template", e);

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

        log.debug("Exiting");
    }

}
