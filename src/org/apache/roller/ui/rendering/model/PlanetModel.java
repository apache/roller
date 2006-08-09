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
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.PlanetSubscriptionDataWrapper;
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
    private Template       weblogPage = null;
    private int            pageNum = 0;
    private WebsiteData    weblog = null;
    
    
    public String getModelName() {
        return "planet";
    }
    
    public void init(Map initData) throws RollerException {
        // we expect the init data to contain a pageRequest object
        this.weblogRequest = (WeblogPageRequest) initData.get("pageRequest");
        if(this.weblogRequest == null) {
            throw new RollerException("expected pageRequest from init data");
        }
        
        // TODO 3.0: is it better to reparse URL to get these?
        if (weblogRequest instanceof WeblogPageRequest) {
            weblogPage = ((WeblogPageRequest)weblogRequest).getWeblogPage();
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
                weblogRequest.getLocale(), weblogPage.getLink(), 
                null, null, null, 0, false);
        
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
                weblogRequest.getLocale(), weblogPage.getLink(), 
                null, null, null, 0, false);
        
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
                weblogRequest.getLocale(), weblogPage.getLink(), 
                null, null, null, 0, false);
        
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
            Roller roller = RollerFactory.getRoller();
            PlanetManager planetManager = roller.getPlanetManager();
            List subs = planetManager.getTopSubscriptions(groupHandle, 0, length);
            for (Iterator it = subs.iterator(); it.hasNext();) {
                PlanetSubscriptionData sub = (PlanetSubscriptionData) it.next();
                list.add(PlanetSubscriptionDataWrapper.wrap(sub)); 
            }
        } catch (Exception e) {
            log.error("ERROR: get ranked blogs", e);
        }
        return list;
    }
    
}
