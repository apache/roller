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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.util.Utilities;

public class WeblogEntriesPermalinkPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesPermalinkPager.class);
    
    WeblogEntry currEntry = null;
    WeblogEntry nextEntry = null;
    WeblogEntry prevEntry = null;
    
    // collection for the pager
    Map<Date, List<WeblogEntryWrapper>> entries = null;

    public WeblogEntriesPermalinkPager(
            WeblogEntryManager weblogEntryManager,
            URLStrategy        strat,
            Weblog             weblog,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catName,
            List<String>       tags,
            int                page) {
        
        super(weblogEntryManager, strat, weblog, pageLink, entryAnchor, dateString, catName, tags, page);
        getEntries();
    }
    
    
    public Map getEntries() {
        if (entries == null) {
            try {
                currEntry = weblogEntryManager.getWeblogEntryByAnchor(weblog, entryAnchor);
                if (currEntry != null && currEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                    entries = new TreeMap<>();
                    entries.put(new Date(currEntry.getPubTime().getTime()),Collections.singletonList(WeblogEntryWrapper.wrap(currEntry, urlStrategy)));
                }
            } catch (Exception e) {
                log.error("ERROR: fetching entry");
            }
        }


        
        return entries;
    }
    
    
    public String getHomeLink() {
        return createURL(0, 0, weblog, pageLink, null, dateString, catName, tags);
    }
    
    
    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager.single.home");
    }
    
    
    public String getNextLink() {
        if (getNextEntry() != null) {
            return createURL(0, 0, weblog, pageLink, nextEntry.getAnchor(), dateString, catName, tags);
        }
        return null;
    }
    
    
    public String getNextName() {
        if (getNextEntry() != null) {
            String title = Utilities.truncateNicely(getNextEntry().getTitle(), 15, 20, "...");
            return messageUtils.getString("weblogEntriesPager.single.next", new Object[] {title});
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (getPrevEntry() != null) {
            return createURL(0, 0, weblog, pageLink, prevEntry.getAnchor(), dateString, catName, tags);
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (getPrevEntry() != null) {
            String title = Utilities.truncateNicely(getPrevEntry().getTitle(), 15, 20, "...");
            return messageUtils.getString("weblogEntriesPager.single.prev", new Object[] {title});
        }
        return null;
    }
    
    
    private WeblogEntry getNextEntry() {
        if (nextEntry == null) {
            try {
                nextEntry = weblogEntryManager.getNextEntry(currEntry, null);
                // make sure that entry is published and not to future
                if (nextEntry != null && nextEntry.getPubTime().after(new Date())
                        && nextEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                    nextEntry = null;
                }
            } catch (WebloggerException e) {
                log.error("ERROR: getting next entry", e);
            }
        }

        return nextEntry;
    }
    
    
    private WeblogEntry getPrevEntry() {
        if (prevEntry == null) {
            try {
                prevEntry = weblogEntryManager.getPreviousEntry(currEntry, null);
                // make sure that entry is published and not to future
                if (prevEntry != null && prevEntry.getPubTime().after(new Date())
                        && prevEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                    prevEntry = null;
                }
            } catch (WebloggerException e) {
                log.error("ERROR: getting prev entry", e);
            }
        }

        return prevEntry;
    }
    
}
