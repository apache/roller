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

package org.apache.roller.ui.rendering.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.pojos.ThemeTemplate;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.ui.rendering.pagers.Pager;
import org.apache.roller.ui.rendering.pagers.PlanetEntriesPager;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.URLUtilities;


/**
 * Model that provides access to planet aggregations, feeds and subscriptions.
 */
public class PlanetModel implements Model {
    
    private static Log log = LogFactory.getLog(PlanetModel.class);
    
    private WeblogRequest  weblogRequest = null;
    private String pageLink = null;
    private int            pageNum = 0;
    private Weblog    weblog = null;
    
    
    public String getModelName() {
        return "planet";
    }
    
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a weblogRequest object
        this.weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(this.weblogRequest == null) {
            throw new RollerException("expected weblogRequest from init data");
        }
        
        if (weblogRequest instanceof WeblogPageRequest) {
            ThemeTemplate weblogPage = ((WeblogPageRequest)weblogRequest).getWeblogPage();
            pageLink = (weblogPage != null) ? weblogPage.getLink() : null;
            pageNum = ((WeblogPageRequest)weblogRequest).getPageNum();
        }  
        
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
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
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
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
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
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new PlanetEntriesPager(
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
            PlanetData defaultPlanet = planetManager.getPlanet("zzz_default_planet_zzz");
            PlanetGroupData planetGroup = planetManager.getGroup(defaultPlanet, groupHandle);
            List subs = planetManager.getTopSubscriptions(planetGroup, 0, length);
            for (Iterator it = subs.iterator(); it.hasNext();) {
                PlanetSubscriptionData sub = (PlanetSubscriptionData) it.next();
                // TODO needs pojo wrapping from planet
                list.add(sub); 
            }
        } catch (Exception e) {
            log.error("ERROR: get ranked blogs", e);
        }
        return list;
    }
    
}
