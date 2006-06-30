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

package org.apache.roller.ui.rendering.util;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;


/**
 * We're showing one weblog entry.
 * Next/prev return permalinks of next and previous weblog entries.
 * In this mode there's no prev/next collection.
 */
public class WeblogEntriesPermalinkPager extends WeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesPermalinkPager.class);
    
    private Map entries = null;
    private String entryAnchor = null;
    private WebsiteData weblog = null;
    private WeblogEntryData entry = null;
    private WeblogEntryData nextEntry = null;
    private WeblogEntryData prevEntry = null;
    
    
    public WeblogEntriesPermalinkPager(WebsiteData weblog, String anchor) {
        this.weblog = weblog;
        this.entryAnchor = anchor;
    }
    
    
    /**
     * Wrap entry up in map of lists.
     */
    public Map getEntries() {
        
        if(this.entries == null) {
            entry = getEntry();
            if(entry != null) {
                entries = new HashMap();
                entries.put(new Date(entry.getPubTime().getTime()),
                        Collections.singletonList(WeblogEntryDataWrapper.wrap(entry)));
            }
        }
        
        return this.entries;
    }
    
    
    public String getNextLink() {
        String ret = null;
        if (getNextEntry() != null) {
            ret = getNextEntry().getPermalink();
        }
        return ret;
    }
    
    
    public String getNextLinkName() {
        String ret = null;
        if (getNextEntry() != null) {
            ret = getNextEntry().getTitle();
        }
        return ret;
    }
    
    
    public String getPrevLink() {
        String ret = null;
        if (getPrevEntry() != null) {
            ret = getPrevEntry().getPermalink();
        }
        return ret;
    }
    
    
    public String getPrevLinkName() {
        String ret = null;
        if (getPrevEntry() != null) {
            ret = getPrevEntry().getTitle();
        }
        return ret;
    }
    
    
    private WeblogEntryData getEntry() {
        
        if (entry == null) try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            entry = wmgr.getWeblogEntryByAnchor(weblog, entryAnchor);
            
            // make sure that entry is published and not to future
            if(entry == null || 
                    !entry.getStatus().equals(WeblogEntryData.PUBLISHED) ||
                    entry.getPubTime().after(new Date())) {
                entry = null;
            }
        } catch (RollerException e) {
            log.error("ERROR: getting entry", e);
        }
        
        return entry;
    }
    
    
    private WeblogEntryData getNextEntry() {
        
        if (nextEntry == null) try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            //nextEntry = wmgr.getNextEntry(entry, chosenCatPath);
            nextEntry = wmgr.getNextEntry(getEntry(), null);
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
            //prevEntry = wmgr.getPreviousEntry(entry, chosenCatPath);
            nextEntry = wmgr.getPreviousEntry(getEntry(), null);
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
