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

package org.apache.roller.ui.rendering.pagers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.wrapper.PlanetEntryDataWrapper;


/**
 * Paging through a collection of planet entries.
 */
public class PlanetEntriesPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(PlanetEntriesPager.class);
    
    private String feedURL = null;
    private String groupHandle = null;
    private String locale = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // the collection for the pager
    private List entries = null;
    
    // are there more items?
    private boolean more = false;
    
    
    public PlanetEntriesPager(
            String         feedURL,
            String         groupHandle,
            String         baseUrl,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(baseUrl, page);
        
        this.feedURL = feedURL;
        this.groupHandle = groupHandle;
        this.locale = locale;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public List getItems() {
        
        if (entries == null) {
            // calculate offset
            int offset = getPage() * length;
            
            Date startDate = null;
            if(sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            List results = new ArrayList();
            try {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planetManager = roller.getPlanetManager();
                
                List rawEntries = null;
                if (feedURL != null) {
                    rawEntries = planetManager.getFeedEntries(feedURL, offset, length+1);
                } else if (groupHandle != null) {
                    PlanetGroupData group = planetManager.getGroup(groupHandle);
                    rawEntries = planetManager.getAggregation(group, startDate, null, offset, length+1);
                } else {
                    rawEntries = planetManager.getAggregation(startDate, null, offset, length+1);
                }
                
                // check if there are more results for paging
                if(rawEntries.size() > length) {
                    more = true;
                    rawEntries.remove(rawEntries.size() - 1);
                }
                
                // wrap 'em
                for (Iterator it = rawEntries.iterator(); it.hasNext();) {
                    PlanetEntryData entry = (PlanetEntryData) it.next();
                    results.add(PlanetEntryDataWrapper.wrap(entry));
                }
                
            } catch (Exception e) {
                log.error("ERROR: get aggregation", e);
            }
            
            entries = results;
        }
        
        return entries;
    }
    
    
    public boolean hasMoreItems() {
        return more;
    }
    
}
