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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.MessageUtilities;
import org.apache.roller.util.Utilities;


/**
 *
 */
public class WeblogEntriesPermalinkPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesPermalinkPager.class);
    
    WeblogEntryData currEntry = null;
    WeblogEntryData nextEntry = null;
    WeblogEntryData prevEntry = null;
    
    // collection for the pager
    Map entries = null;
    
    
    public WeblogEntriesPermalinkPager(
            WebsiteData        weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath,
            List               tags,
            int                page) {
        
        super(weblog, locale, pageLink, entryAnchor, dateString, catPath, tags, page);
        
        getEntries();
    }
    
    
    public Map getEntries() {
        if (entries == null) try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            currEntry = wmgr.getWeblogEntryByAnchor(weblog, entryAnchor);
            if (currEntry != null && currEntry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                entries = new TreeMap();
                entries.put(new Date(currEntry.getPubTime().getTime()),
                        Collections.singletonList(WeblogEntryDataWrapper.wrap(currEntry)));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching entry");
        }
        
        return entries;
    }
    
    
    public String getHomeLink() {
        return createURL(0, 0, weblog, locale, pageLink, null, dateString, catPath, tags);
    }
    
    
    public String getHomeName() {
        return MessageUtilities.getString("weblogEntriesPager.single.home");
    }
    
    
    public String getNextLink() {
        if (getNextEntry() != null) {
            return createURL(0, 0, weblog, locale, pageLink, nextEntry.getAnchor(), dateString, catPath, tags);
        }
        return null;
    }
    
    
    public String getNextName() {
        if (getNextEntry() != null) {
            String title = Utilities.truncateNicely(getNextEntry().getTitle(), 15, 20, "...");
            return MessageUtilities.getString("weblogEntriesPager.single.next", new Object[] {title});
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (getPrevEntry() != null) {
            return createURL(0, 0, weblog, locale, pageLink, prevEntry.getAnchor(), dateString, catPath, tags);
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (getPrevEntry() != null) {
            String title = Utilities.truncateNicely(getPrevEntry().getTitle(), 15, 20, "...");
            return MessageUtilities.getString("weblogEntriesPager.single.prev", new Object[] {title});
        }
        return null;
    }
    
    
    private WeblogEntryData getNextEntry() {
        if (nextEntry == null) try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            nextEntry = wmgr.getNextEntry(currEntry, null, locale);
            // make sure that entry is published and not to future
            if (nextEntry != null && nextEntry.getPubTime().after(new Date())
            && nextEntry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                nextEntry = null;
            }
        } catch (RollerException e) {
            log.error("ERROR: getting next entry", e);
        }
        return nextEntry;
    }
    
    
    private WeblogEntryData getPrevEntry() {
        if (prevEntry == null) try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            prevEntry = wmgr.getPreviousEntry(currEntry, null, locale);
            // make sure that entry is published and not to future
            if (prevEntry != null && prevEntry.getPubTime().after(new Date())
            && prevEntry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                prevEntry = null;
            }
        } catch (RollerException e) {
            log.error("ERROR: getting prev entry", e);
        }
        return prevEntry;
    }
    
}
