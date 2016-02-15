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
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.StaticTemplate;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.roller.weblogger.ui.rendering.util.PlanetRequest;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.weblogger.ui.rendering.util.cache.ExpiringCache;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Responsible for displaying Planet newsfeeds
 */
@RestController
@RequestMapping(path="/planetrss/**")
public class PlanetFeedProcessor {

    private static Log log = LogFactory.getLog(PlanetFeedProcessor.class);

    @Autowired
    private ExpiringCache planetCache;

    public void setPlanetCache(ExpiringCache planetCache) {
        this.planetCache = planetCache;
    }

    @Autowired
    private PlanetManager planetManager;

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    @Autowired
    private RendererManager rendererManager = null;

    public void setRendererManager(RendererManager rendererManager) {
        this.rendererManager = rendererManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Entering");

        PlanetRequest planetRequest;
        Planet planet;

        try {
            planetRequest = new PlanetRequest(request);
            planet = planetManager.getPlanet(planetRequest.getPlanet());
            if (planet == null) {
                log.debug("Planet not found: " + planetRequest.getPlanet());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (Exception e) {
            // some kind of error parsing the request
            log.debug("error creating planet request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // figure planet last modified date
        Date lastModified = planet.getLastUpdated();

        // Respond with 304 Not Modified if it is not modified.
        if (ModDateHeaderUtil.respondIfNotModified(request, response,
                lastModified.getTime(), planetRequest.getDeviceType())) {
            return;
        }

        // set content type
        String accepts = request.getHeader("Accept");
        String userAgent = request.getHeader("User-Agent");
        if (accepts != null && userAgent != null
                && accepts.contains("*/*")
                && userAgent.startsWith("Mozilla")) {
            // client is a browser and now that we offer styled feeds we want
            // browsers to load the page rather than popping up the download
            // dialog, so we provide a content-type that browsers will display
            response.setContentType("text/xml");
        } else {
            response.setContentType("application/rss+xml; charset=utf-8");
        }

        // set last-modified date
        ModDateHeaderUtil.setLastModifiedHeader(response,
                lastModified.getTime(), planetRequest.getDeviceType());

        // cached content checking
        String cacheKey = generateKey(planetRequest);
        CachedContent entry = (CachedContent) planetCache.get(cacheKey);
        if (entry != null) {
            response.setContentLength(entry.getContent().length);
            response.getOutputStream().write(entry.getContent());
            return;
        }

        // looks like we need to render content
        HashMap<String, Object> model = new HashMap<>();
        try {

            // populate the rendering model
            if (request.getParameter("planet") != null) {
                model.put("group", planetManager.getPlanet(request.getParameter("planet")));
            }

            model.put("planet", planetManager);
            model.put("date", new Date());
            model.put("utils", new UtilitiesModel());
            model.put("lastModified", lastModified);

            if (StringUtils.isNotEmpty(WebloggerRuntimeConfig
                    .getProperty("planet.site.absoluteurl"))) {
                model.put("absoluteSite",
                        WebloggerRuntimeConfig.getProperty("planet.site.absoluteurl"));
            } else {
                model.put("absoluteSite",
                        WebloggerRuntimeConfig.getAbsoluteContextURL());
            }

            model.put("feedStyle", WebloggerRuntimeConfig
                    .getBooleanProperty("site.newsfeeds.styledFeeds"));

            int numEntries = WebloggerRuntimeConfig
                    .getIntProperty("site.newsfeeds.defaultEntries");

            int entryCount = numEntries;
            String sCount = request.getParameter("count");
            if (sCount != null) {
                try {
                    entryCount = Integer.parseInt(sCount);
                } catch (NumberFormatException e) {
                    log.warn("Improperly formatted count parameter");
                }
                if (entryCount > numEntries) {
                    entryCount = numEntries;
                }
                if (entryCount < 0) {
                    entryCount = 0;
                }
            }
            model.put("entryCount", entryCount);
        } catch (Exception ex) {
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
            Template template = new StaticTemplate(
                    "templates/planet/planetrss.vm", TemplateLanguage.VELOCITY);
            renderer = rendererManager.getRenderer(template, DeviceType.mobile);
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for planet rss", e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content
        CachedContent rendererOutput = new CachedContent(WebloggerCommon.TWENTYFOUR_KB_IN_BYTES);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for planet rss", e);

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

        // cache rendered content.
        this.planetCache.put(cacheKey, rendererOutput);

        log.debug("Exiting");
    }

    /**
     * Generate a cache key from a parsed planet request. This generates a key
     * of the form ...
     *
     * <context>/<type>/<language>[/user] or
     * <context>/<type>[/flavor]/<language>[/excerpts]
     *
     * examples:
     * planet/page/en
     * planet/feed/rss/en/excerpts
     */
    private String generateKey(PlanetRequest planetRequest) {

        StringBuilder key = new StringBuilder();
        key.append("planet.key").append(":");
        key.append(planetRequest.getContext());
        key.append("/");
        key.append(planetRequest.getType());

        if (planetRequest.getFlavor() != null) {
            key.append("/").append(planetRequest.getFlavor());
        }

        // add language
        key.append("/").append(planetRequest.getLanguage());

        if (planetRequest.getFlavor() != null) {
            // add excerpts
            if (planetRequest.isExcerpts()) {
                key.append("/excerpts");
            }
        } else {
            // add login state
            if (planetRequest.getAuthenticUser() != null) {
                key.append("/user=").append(planetRequest.getAuthenticUser());
            }
        }

        // add planet name
        if (planetRequest.getPlanet() != null) {
            key.append("/planet=").append(planetRequest.getPlanet());
        }

        return key.toString();
    }
}