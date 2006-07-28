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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.PlanetEntryDataWrapper;

/**
 * Paging for planet entries.
 */
public class PlanetEntriesPager extends AbstractPager {
    private String feedURL;
    private String groupHandle;
    private List entries;    
    protected static Log log =
            LogFactory.getFactory().getInstance(PlanetEntriesPager.class);
    
    /** Creates a new instance of CommentPager */
    public PlanetEntriesPager(   
            String         feedURL,
            String         groupHandle,  
            WebsiteData    weblog,
            WeblogTemplate weblogPage,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        super(weblog, weblogPage, locale, sinceDays, page, length);
        this.feedURL = feedURL;
        this.groupHandle = groupHandle;
        getEntries();
    }
    
    public List getEntries() {
        if (entries == null) {
            List results = new ArrayList();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);
            Date startDate = cal.getTime();        
            try {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planetManager = roller.getPlanetManager();
                PlanetGroupData group = planetManager.getGroup(groupHandle);
                List rawEntries = null; 
                if (feedURL != null) {
                    rawEntries = planetManager.getFeedEntries(feedURL, offset, length);
                } else if (groupHandle == null) {
                    rawEntries = planetManager.getAggregation(startDate, null, offset, length);
                } else {
                    rawEntries = planetManager.getAggregation(group, startDate, null, offset, length); 
                }
                int count = 0;
                for (Iterator it = rawEntries.iterator(); it.hasNext();) {
                    PlanetEntryData entry = (PlanetEntryData) it.next();
                    if (count++ < length) {
                        PlanetEntryDataWrapper wrapped = PlanetEntryDataWrapper.wrap(entry);
                        results.add(wrapped);
                    } else {
                        more = true;
                    }    
                }
            } catch (Exception e) {
                log.error("ERROR: get aggregation", e);
            }
            entries = results;
        }
        return entries;
    }   
}
