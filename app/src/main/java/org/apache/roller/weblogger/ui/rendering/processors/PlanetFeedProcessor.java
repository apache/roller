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
import java.time.Instant;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.business.themes.SharedTemplate;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.ExpiringCache;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.DeviceType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Responsible for displaying Planet newsfeeds
 */
@RestController
@RequestMapping(path="/planetrss/**")
public class PlanetFeedProcessor {

    private static Logger log = LoggerFactory.getLogger(PlanetFeedProcessor.class);

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

    @Autowired
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void getPlanetFeed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Entering");

        Planet planet;
        String planetHandle;
        DeviceType deviceType = Utilities.getDeviceType(request);

        try {
            // parse the request object and figure out what we've got
            log.debug("parsing url: {}", request.getRequestURL());

            // planet to include
            planetHandle = request.getParameter("planet");
            if (planetHandle == null) {
                throw new IllegalArgumentException("Planet handle not provided in URL: " + request.getRequestURL());
            }

            planet = planetManager.getPlanetByHandle(planetHandle);
            if (planet == null) {
                throw new IllegalArgumentException("Could not find planet with handle: " + planetHandle);
            }
        } catch (Exception e) {
            // some kind of error parsing the request
            log.debug("error creating planet request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // figure planet last modified date
        Instant lastModified = planet.getLastUpdated();

        // Respond with 304 Not Modified if it is not modified.
        if (Utilities.respondIfNotModified(request, response, lastModified.toEpochMilli(), deviceType)) {
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
            response.setContentType("application/atom+xml; charset=utf-8");
        }

        // set last-modified date
        Utilities.setLastModifiedHeader(response, lastModified.toEpochMilli(), deviceType);

        int page = 1;
        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch(NumberFormatException ignored) {
            }
            page = Math.max(page, 1);
        }

        // cached content checking
        String cacheKey = generateKey(planetHandle, page);
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
            model.put("planetManager", planetManager);
            model.put("planet", planet);
            model.put("utils", new UtilitiesModel());
            model.put("lastModified", lastModified);
            model.put("absoluteSite", WebloggerStaticConfig.getAbsoluteContextURL());
            model.put("generatorVersion", WebloggerStaticConfig.getProperty("weblogger.version", "Unknown"));
            model.put("maxEntries", propertiesManager.getIntProperty("site.newsfeeds.maxEntries"));
            model.put("page", page);

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
            Template template = new SharedTemplate("templates/feeds/planet-atom.vm", TemplateRendition.Parser.VELOCITY);
            renderer = rendererManager.getRenderer(template, DeviceType.NORMAL);
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
        CachedContent rendererOutput = new CachedContent(Utilities.TWENTYFOUR_KB_IN_BYTES);
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
     * of the form planet.key:{planetname}/feed/{page}
     *
     * example: planet.key:testplanet/feed/2
     */
    private String generateKey(String planet, int page) {
        String key = "planet.key:";
        key += planet;
        key += "/feed/";
        key += page;
        return key;
    }
}
