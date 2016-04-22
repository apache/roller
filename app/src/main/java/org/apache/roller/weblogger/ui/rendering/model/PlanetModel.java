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
package org.apache.roller.weblogger.ui.rendering.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.PlanetEntriesPager;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;

/**
 * Model that provides access to planet aggregations, feeds and subscriptions.
 */
public class PlanetModel implements Model {
    
    private static Log log = LogFactory.getLog(PlanetModel.class);
    
    private WeblogPageRequest pageRequest = null;
    private String         pageLink = null;

    private URLStrategy    urlStrategy = null;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    private PlanetManager planetManager;

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    @Override
    public String getModelName() {
        return "planet";
    }

    /** Init page model, requires a WeblogPageRequest object. */
    @Override
    public void init(Map initData) throws WebloggerException {
        this.pageRequest = (WeblogPageRequest) initData.get("parsedRequest");

        if (pageRequest == null) {
            throw new WebloggerException("Missing WeblogPageRequest object");
        }

        Template weblogPage = pageRequest.getWeblogTemplate();
        pageLink = (weblogPage != null) ? weblogPage.getRelativePath() : null;
    }
    
    /**
     * Get pager for WeblogEntry objects from specified Planet group in reverse chrono order.
     * @param length Max number of results to return
     */
    public Pager getAggregationPager(String planetHandle, int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(pageRequest.getWeblog(), null,
                pageLink, null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(planetManager, urlStrategy, null,
            planetHandle, pagerUrl, sinceDays, pageRequest.getPageNum(), length);
    }
    
    
    /**
     * Get pager for WeblogEntry objects from specified
     * Planet feed in reverse chrono order.
     * @param length      Max number of results to return
     */
    public Pager getFeedPager(String groupHandle, String feedURL, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(pageRequest.getWeblog(), null,
                pageLink, null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(planetManager, urlStrategy, feedURL,
            groupHandle, pagerUrl, -1, pageRequest.getPageNum(), length);
    }
    
    /**
     * Get Planets defined.
     * @return List of Planet groups defined.
     */
    public List<Planet> getPlanets() {
        List<Planet> list = new ArrayList<>();
        try {
            list = planetManager.getPlanets();
        } catch (Exception e) {
            log.error("ERROR: getting groups", e);
        }
        return list;
    }

    /**
     * Get Planet by handle.
     * @param groupHandle Handle of Planet to fetch.
     * @return PlaneGroup specified by handle.
     */
    public Planet getPlanet(String groupHandle) {
        Planet group = null;
        try {
            group = planetManager.getPlanet(groupHandle);
        } catch (Exception e) {
            log.error("ERROR: getting group", e);
        }
        return group;        
    }

    /**
     * Get RSS webpage of specified planet
     * @param planet planet handle
     * @return RSS webpage of planet
     */
    public String getPlanetURL(String planet) {
        return urlStrategy.getPlanetURL(planet);
    }

}
