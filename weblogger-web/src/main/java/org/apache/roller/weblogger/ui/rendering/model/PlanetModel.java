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
 */

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.PlanetEntriesPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;

/**
 * Model that provides access to planet aggregations, feeds and subscriptions.
 */
public class PlanetModel implements Model {
    
    public static final String DEFAULT_PLANET_HANDLE = "default";   
    
    private static Log log = LogFactory.getLog(PlanetModel.class);
    
    private WeblogRequest  weblogRequest = null;
    private String         pageLink = null;
    private int            pageNum = 0;
    private Weblog         weblog = null;
    
    private URLStrategy    urlStrategy = null;
    private org.apache.roller.planet.business.URLStrategy planetUrlStrategy = null;

    
    public String getModelName() {
        return "planet";
    }
    
    public void init(Map initData) throws WebloggerException {

        if (!WebloggerConfig.getBooleanProperty("planet.aggregator.enabled")) return;
        
        // we expect the init data to contain a weblogRequest object
        this.weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if(this.weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        if (weblogRequest instanceof WeblogPageRequest) {
            ThemeTemplate weblogPage = ((WeblogPageRequest)weblogRequest).getWeblogPage();
            pageLink = (weblogPage != null) ? weblogPage.getLink() : null;
            pageNum = ((WeblogPageRequest)weblogRequest).getPageNum();
        }  
        
        // look for url strategy
        urlStrategy = (URLStrategy) initData.get("urlStrategy");
        if(urlStrategy == null) {
            urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();
        }
        
        planetUrlStrategy = PlanetFactory.getPlanet().getURLStrategy();
        
        // extract weblog object
        weblog = weblogRequest.getWeblog();
    } 
    
    
    /**
     * Get pager for PlanetEntry objects from 'all' and
     * 'exernal' Planet groups. in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getAggregationPager(int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
            urlStrategy,
            null,
            null,    
            pagerUrl,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }
    
    
    /**
     * Get pager for WeblogEntry objects from specified
     * Planet groups in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getAggregationPager(String groupHandle, int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
            urlStrategy,
            null,
            groupHandle,
            pagerUrl,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }
    
    
    /**
     * Get pager for WeblogEntry objects from specified
     * Planet feed in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getFeedPager(String feedURL, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
            urlStrategy,
            feedURL,
            null,
            pagerUrl,
            weblogRequest.getLocale(),
            -1,
            pageNum, 
            length);
    }
    
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getRankedSubscriptions(int sinceDays, int length) {
        return getRankedSubscriptions(null, sinceDays, length);
    }
    
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param groupHandle Only consider weblogs updated in the last sinceDays
     * @param sinceDays   Only consider weblogs updated in the last sinceDays
     * @param offset      Offset into results (for paging)
     * @param len         Max number of results to return
     */
    public List getRankedSubscriptions(String groupHandle, int sinceDays, int length) {
        List list = new ArrayList();
        try {
            PlanetManager planetManager = PlanetFactory.getPlanet().getPlanetManager();
            Planet defaultPlanet = planetManager.getPlanet(DEFAULT_PLANET_HANDLE);
            PlanetGroup planetGroup = planetManager.getGroup(defaultPlanet, groupHandle);
            List subs = planetManager.getTopSubscriptions(planetGroup, 0, length);
            for (Iterator it = subs.iterator(); it.hasNext();) {
                Subscription sub = (Subscription) it.next();
                // TODO needs pojo wrapping from planet
                list.add(sub); 
            }
        } catch (Exception e) {
            log.error("ERROR: get ranked blogs", e);
        }
        return list;
    }
    
    
    /**
     * Get PlanetGroups defined.
     * @return List of Planet groups defined.
     */
    public List<PlanetGroup> getGroups() {
        List list = new ArrayList<PlanetGroup>();
        try {
            PlanetManager planetManager = PlanetFactory.getPlanet().getPlanetManager();
            Planet defaultPlanet = planetManager.getPlanet(DEFAULT_PLANET_HANDLE);
            Set<PlanetGroup> groups = (Set<PlanetGroup>)defaultPlanet.getGroups();
            for (PlanetGroup group : groups) {
                // TODO needs pojo wrapping from planet
                list.add(group); 
            }
        } catch (Exception e) {
            log.error("ERROR: getting groups", e);
        }
        return list;        
    }
    
    
    /**
     * Get PlanetGroup by handle.
     * @param groupHandle Handle of PlanetGroup to fetch.
     * @return PlaneGroup specified by handle.
     */
    public PlanetGroup getGroup(String groupHandle) {
        PlanetGroup group = null;
        try {
            PlanetManager planetManager = PlanetFactory.getPlanet().getPlanetManager();
            Planet defaultPlanet = planetManager.getPlanet(DEFAULT_PLANET_HANDLE);            
            // TODO needs pojo wrapping from planet
            group = planetManager.getGroup(defaultPlanet, groupHandle);            
        } catch (Exception e) {
            log.error("ERROR: getting group", e);
        }
        return group;        
    }
    
    
    public String getPlanetURL() {
        return planetUrlStrategy.getPlanetURL("ignored");
    }

    
    public String getPlanetGroupURL(String group, int pageNum) {
        return planetUrlStrategy.getPlanetGroupURL("ignored", group, pageNum);
    }
    
    
    public String getPlanetFeedURL(String group, String format) {
        return planetUrlStrategy.getPlanetGroupFeedURL("ignored", group, format);
    }
}
