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
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.business.themes.SharedTemplate;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.roller.weblogger.util.cache.ExpiringCache;
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

    @Autowired
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Entering");

        Planet planet;
        String planetName;
        DeviceType deviceType = MobileDeviceRepository.getRequestType(request);

        try {
            // parse the request object and figure out what we've got
            log.debug("parsing url: " + request.getRequestURL());

            // planet to include
            planetName = request.getParameter("planet");
            if (planetName == null) {
                throw new IllegalArgumentException("Planet name not provided in URL: " + request.getRequestURL());
            }

            planet = planetManager.getPlanet(planetName);
            if (planet == null) {
                throw new IllegalArgumentException("Could not find planet named: " + planetName);
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
                lastModified.getTime(), deviceType)) {
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
                lastModified.getTime(), deviceType);

        // cached content checking
        String cacheKey = generateKey(planetName);
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

            if (StringUtils.isNotEmpty(propertiesManager.getStringProperty("planet.site.absoluteurl"))) {
                model.put("absoluteSite",
                        propertiesManager.getStringProperty("planet.site.absoluteurl"));
            } else {
                model.put("absoluteSite",
                        WebloggerStaticConfig.getAbsoluteContextURL());
            }

            model.put("feedStyle", propertiesManager.getBooleanProperty("site.newsfeeds.styledFeeds"));

            int numEntries = propertiesManager.getIntProperty("site.newsfeeds.defaultEntries");

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
            Template template = new SharedTemplate(
                    "templates/planet/planetrss.vm", TemplateLanguage.VELOCITY);
            renderer = rendererManager.getRenderer(template, DeviceType.standard);
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
     * of the form planet.key:{planetname}/{feed flavor}
     *
     * example: planet.key:testplanet/rss
     */
    private String generateKey(String planet) {
        String key = "planet.key:";
        key += planet;
        key += "/rss";
        return key;
    }
}
