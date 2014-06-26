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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;


/**
 * A pager over a collection of recent weblog entries.
 */
public class WeblogEntriesLatestPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesLatestPager.class);
    
    // collection for the pager
    private Map<Date, List<WeblogEntryWrapper>> entries = null;
    
    // are there more pages?
    private boolean more = false;
    
    
    public WeblogEntriesLatestPager(
            URLStrategy        strat,
            Weblog             weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catName,
            List               tags,
            int                page) {
        
        super(strat, weblog, locale, pageLink, entryAnchor, dateString, catName, tags, page);
        
        // initialize the pager collection
        getEntries();
    }
    
    
    public Map<Date, List<WeblogEntryWrapper>> getEntries() {
        
        if (entries == null) {
            entries = new TreeMap<Date, List<WeblogEntryWrapper>>(Collections.reverseOrder());
            try {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(weblog);
                wesc.setEndDate(new Date());
                wesc.setCatName(catName);
                wesc.setTags(tags);
                wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                wesc.setLocale(locale);
                wesc.setOffset(offset);
                wesc.setMaxResults(length+1);
                Map<Date, List<WeblogEntry>> mmap = WebloggerFactory.getWeblogger().getWeblogEntryManager().getWeblogEntryObjectMap(wesc);

                // need to wrap pojos
                int count = 0;
                for (Map.Entry<Date, List<WeblogEntry>> entry : mmap.entrySet()) {
                    // now we need to go through each entry in a day and wrap
                    List<WeblogEntryWrapper> wrapped = new ArrayList<WeblogEntryWrapper>();
                    List<WeblogEntry> unwrapped = entry.getValue();
                    for (int i=0; i < unwrapped.size(); i++) {
                        if (count++ < length) {
                            wrapped.add(i,WeblogEntryWrapper.wrap(unwrapped.get(i), urlStrategy));
                        } else {
                            more = true;
                        }
                    }
                    
                    // done with that day, put it in the map
                    if (wrapped.size() > 0) {
                        entries.put(entry.getKey(), wrapped);
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
