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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;


/**
 * A pager over a collection of recent weblog entries.
 */
public class WeblogEntriesLatestPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesLatestPager.class);
    
    // collection for the pager
    private Map entries = null;
    
    // are there more pages?
    private boolean more = false;
    
    
    public WeblogEntriesLatestPager(
            URLStrategy        strat,
            Weblog             weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath,
            List               tags,
            int                page) {
        
        super(strat, weblog, locale, pageLink, entryAnchor, dateString, catPath, tags, page);
        
        // initialize the pager collection
        getEntries();
    }
    
    
    public Map getEntries() {
        
        if (entries == null) {
            entries = new TreeMap(new ReverseComparator());
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                WeblogEntryManager wmgr = roller.getWeblogEntryManager();
                Map mmap = WebloggerFactory.getWeblogger().getWeblogEntryManager().getWeblogEntryObjectMap(
                        weblog,
                        null,
                        new Date(),
                        catPath,
                        tags,
                        WeblogEntry.PUBLISHED,
                        locale,
                        offset,
                        length + 1);
                
                // need to wrap pojos
                int count = 0;
                java.util.Date key = null;
                Iterator days = mmap.keySet().iterator();
                while(days.hasNext()) {
                    key = (java.util.Date)days.next();
                    
                    // now we need to go through each entry in a day and wrap
                    List wrapped = new ArrayList();
                    List unwrapped= (List) mmap.get(key);
                    for(int i=0; i < unwrapped.size(); i++) {
                        if (count++ < length) {
                            wrapped.add(i,WeblogEntryWrapper.wrap((WeblogEntry)unwrapped.get(i), urlStrategy));
                        } else {
                            more = true;
                        }
                    }
                    
                    // done with that day, put it in the map
                    if(wrapped.size() > 0) {
                        entries.put(key, wrapped);
                    }
                }
            } catch (Exception e) {
                log.error("ERROR: getting entry month map", e);
            }
        }
        
        return entries;
    }
    
    
    public boolean hasMoreEntries() {
        return more;
    }
    
}
