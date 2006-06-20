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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.pojos.wrapper.PlanetEntryDataWrapper;
import org.apache.roller.pojos.wrapper.PlanetSubscriptionDataWrapper;

/**
 * Page model that provides access to planet aggregations, feeds and 
 * subscriptions for display on a frontpage weblog.
 */
public class PlanetPageModel implements PageModel {
    protected static Log log = 
            LogFactory.getFactory().getInstance(PlanetPageModel.class);
    
    public String getModelName() {
        return "planetPageModel";
    }
    
    public void init(Map map) {
        // no-op for now
    } 
    
    /**
     * Get move recent PlanetEntry objects from 'all' and
     * 'exernal' Planet groups. in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getAggregation(int sinceDays, int offset, int len) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();        
        try {
            Roller roller = RollerFactory.getRoller();
            PlanetManager planetManager = roller.getPlanetManager();
            List entries = planetManager.getAggregation(startDate, null, offset, len);
            for (Iterator it = entries.iterator(); it.hasNext();) {
                PlanetEntryData entry = (PlanetEntryData) it.next();
                PlanetEntryDataWrapper wrapped = PlanetEntryDataWrapper.wrap(entry);
                results.add(wrapped);
            }
        } catch (Exception e) {
            log.error("ERROR: get aggregation", e);
        }
        return results;
    }
    
    /**
     * Get move recent WeblogEntry objects from specified
     * Planet groups in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getAggregation(String groupHandle, int sinceDays, int offset, int len) {
        List list = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {
            Roller roller = RollerFactory.getRoller();
            PlanetManager planetManager = roller.getPlanetManager();
            PlanetGroupData group = planetManager.getGroup(groupHandle);
            if (group != null) {
                list = planetManager.getAggregation(group, startDate, null, offset, len);
            }
        } catch (Exception e) {
            log.error("ERROR: get aggregation", e);
        }
        return list;
    }
    
    /**
     * Get move recent WeblogEntry objects from specified
     * Planet subscription in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getFeed(String feedUrl, int offset, int len) {
        List list = new ArrayList();
        try {
            Roller roller = RollerFactory.getRoller();
            PlanetManager planetManager = roller.getPlanetManager();
            PlanetSubscriptionData sub = planetManager.getSubscription(feedUrl);
            if (sub != null) {
                list = sub.getEntries();
            }
        } catch (Exception e) {
            log.error("ERROR: get feed", e);
        }
        return list;
    }
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getRankedSubscriptions(int sinceDays, int offset, int length) {
        return getRankedSubscriptions(null, sinceDays, offset, length);
    }
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param groupHandle Only consider weblogs updated in the last sinceDays
     * @param sinceDays   Only consider weblogs updated in the last sinceDays
     * @param offset      Offset into results (for paging)
     * @param len         Max number of results to return
     */
    public List getRankedSubscriptions(String groupHandle, int sinceDays, int offset, int length) {
        List list = new ArrayList();
        try {
            Roller roller = RollerFactory.getRoller();
            PlanetManager planetManager = roller.getPlanetManager();
            List subs = planetManager.getTopSubscriptions(groupHandle, offset, length);
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
