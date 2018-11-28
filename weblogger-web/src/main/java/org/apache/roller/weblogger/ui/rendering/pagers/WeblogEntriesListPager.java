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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;


/**
 * Simple pager for list of weblog entries.
 */
public class WeblogEntriesListPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesListPager.class);
    
    private String locale = null;
    private int sinceDays = -1;
    private int length = 0;
    
    private Weblog queryWeblog = null;
    private User queryUser = null;
    private String queryCat = null;
    private List queryTags = null;
    
    // entries for the pager
    private List entries;
    
    // are there more entries?
    private boolean more = false;
    
    // most recent update time of current set of entries
    private Date lastUpdated = null;    
    
    
    public WeblogEntriesListPager(
            URLStrategy    strat,
            String         baseUrl,
            Weblog         queryWeblog,
            User           queryUser,
            String         queryCat,
            List           queryTags,
            String         locale,
            int            sinceDays,
            int            pageNum,
            int            length) {
        
        super(strat, baseUrl, pageNum);
        
        // store the data
        this.queryWeblog = queryWeblog;
        this.queryUser = queryUser;
        this.queryCat = queryCat;
        this.queryTags = queryTags;
        this.locale = locale;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the pager collection
        getItems();
    }
    
    
    public List getItems() {
        
        if (entries == null) {
            // calculate offset
            int offset = getPage() * length;
            
            List results = new ArrayList();
            
            Date startDate = null;
            if(sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                WeblogEntryManager wmgr = roller.getWeblogEntryManager();
                UserManager umgr = roller.getUserManager();
                List rawEntries = wmgr.getWeblogEntries(
                        
                        queryWeblog,
                        queryUser,
                        startDate,
                        null,
                        queryCat,
                        queryTags,WeblogEntry.PUBLISHED,
                        null,
                        "pubTime",
                        null,
                        locale,
                        offset,
                        length + 1);
                                
                // wrap the results
                int count = 0;
                for (Iterator it = rawEntries.iterator(); it.hasNext();) {
                    WeblogEntry entry = (WeblogEntry) it.next();
                    if (count++ < length) {
                        results.add(WeblogEntryWrapper.wrap(entry, urlStrategy));
                    }
                }
                if (rawEntries.size() > length) more = true;
                
            } catch (Exception e) {
                log.error("ERROR: fetching weblog entries list", e);
            }
            
            entries = results;
        }
        
        return entries;
    }
    
    
    public boolean hasMoreItems() {
        return more;
    }

    /** Get last updated time from items in pager */
    public Date getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by pubtime, so first might not be last updated
            List<WeblogEntryWrapper> items = (List<WeblogEntryWrapper>)getItems();
            if (getItems() != null && getItems().size() > 0) {
                Timestamp newest = ((WeblogEntryWrapper)getItems().get(0)).getUpdateTime();
                for (WeblogEntryWrapper e : items) {
                    if (e.getUpdateTime().after(newest)) {
                        newest = e.getPubTime();
                    }
                }
                lastUpdated = new Date(newest.getTime());
            } else {
                // no update so we assume it's brand new
                lastUpdated = new Date();
            }
        }
        return lastUpdated;
    }
}
