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
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.PlanetEntriesPager;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;

/**
 * Model that provides access to planet aggregations, feeds and subscriptions.
 */
public class PlanetModel implements Model {
    
    private static Log log = LogFactory.getLog(PlanetModel.class);
    
    private WeblogRequest  weblogRequest = null; 
    private String         pageLink = null;
    private int            pageNum = 0;
    private Weblog         weblog = null;
    
    private URLStrategy    urlStrategy = null;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    private PlanetManager planetManager;

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }


    public String getModelName() {
        return "planet";
    }
    
    public void init(Map initData) throws WebloggerException {

        if (!WebloggerStaticConfig.getBooleanProperty("planet.aggregator.enabled")) {
            return;
        }
        
        // we expect the init data to contain a weblogRequest object
        this.weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if(this.weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        if (weblogRequest instanceof WeblogPageRequest) {
            Template weblogPage = ((WeblogPageRequest)weblogRequest).getWeblogPage();
            pageLink = (weblogPage != null) ? weblogPage.getRelativePath() : null;
            pageNum = ((WeblogPageRequest)weblogRequest).getPageNum();
        }  
        
        // extract weblog object
        weblog = weblogRequest.getWeblog();
    } 
    
    
    /**
     * Get pager for PlanetEntry objects from 'all' and
     * 'exernal' Planet groups. in reverse chrono order.
     * @param length      Max number of results to return
     */
    public Pager getAggregationPager(int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, null,
                pageLink,
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
            planetManager,
            urlStrategy,
            null,
            null,    
            pagerUrl,
            sinceDays,
            pageNum, 
            length);
    }
    
    
    /**
     * Get pager for WeblogEntry objects from specified
     * Planet groups in reverse chrono order.
     * @param length      Max number of results to return
     */
    public Pager getAggregationPager(String groupHandle, int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, null,
                pageLink,
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
            planetManager,
            urlStrategy,
            null,
            groupHandle,
            pagerUrl,
            sinceDays,
            pageNum, 
            length);
    }
    
    
    /**
     * Get pager for WeblogEntry objects from specified
     * Planet feed in reverse chrono order.
     * @param length      Max number of results to return
     */
    public Pager getFeedPager(String feedURL, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, null,
                pageLink,
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
            planetManager,
            urlStrategy,
            feedURL,
            null,
            pagerUrl,
            -1,
            pageNum, 
            length);
    }
    
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param length      Max number of results to return
     */
    public List<Subscription> getRankedSubscriptions(int sinceDays, int length) {
        return getRankedSubscriptions(null, sinceDays, length);
    }
    
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param planetHandle Only consider weblogs updated in the last sinceDays
     * @param sinceDays   Only consider weblogs updated in the last sinceDays
     * @param length         Max number of results to return
     */
    public List<Subscription> getRankedSubscriptions(String planetHandle, int sinceDays, int length) {
        List<Subscription> list = new ArrayList<Subscription>();
        try {
            Planet planet = planetManager.getPlanet(planetHandle);
            List<Subscription> subs = planetManager.getTopSubscriptions(planet, 0, length);
            for (Subscription sub : subs) {
                // TODO needs pojo wrapping from planet
                list.add(sub);
            }
        } catch (Exception e) {
            log.error("ERROR: get ranked blogs", e);
        }
        return list;
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
