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
package org.apache.roller.ui.rendering.velocity;

import java.util.ArrayList;
import java.util.List;

import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.ui.core.RollerRequest;

/**
 * Allow Roller page templates to get the main Planet aggregation (the 'all'
 * and 'external' group), custom aggregations, specified by handle, and
 * subscription entries (specified by feedUrl).
 * @author Dave Johnson
 */
public class PlanetPageModel extends PageModel {
    PlanetManager planetManager = null;
    
    public void init(RollerRequest rreq) {
        super.init(rreq);
        try {
            planetManager = RollerFactory.getRoller().getPlanetManager();
        } catch (Exception e) {
            mLogger.error("ERROR initializing page model",e);
        }
    }
    
    /**
     * Get move recent WeblogEntry objects from 'all' and
     * 'exernal' Planet groups. in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getAggregation(int offset, int len) {
        //return planetManager.getAggregation(offset, len);
        return null;
    }
    
    /**
     * Get move recent WeblogEntry objects from specified
     * Planet groups in reverse chrono order.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getAggregation(String group, int offset, int len) {
        List list = new ArrayList();
        //PlanetGroupData group = planetManager.getGroup(group);
        //if (group != null) {
        //    list = planetManager.getAggregation(group, offset, len);
        //}
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
        //PlanetSubscriptionData sub = planetManager.getSubscription(feedUrl);
        //if (sub != null) {
            //list = sub.getEntries();
        //}
        return list;
    }
    
    /**
     * Get PlanetSubscription objects in descending order by Planet ranking.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getRankedBlogs(int sinceDays, int offset, int len) {
        return null;
    }
    
}
