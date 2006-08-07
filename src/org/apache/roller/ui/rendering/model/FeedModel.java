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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.rendering.util.WeblogFeedRequest;


/**
 * Model which provides information needed to render a feed.
 */
public class FeedModel implements Model {
    
    private static Log log = LogFactory.getLog(FeedModel.class); 
    
    private WeblogFeedRequest feedRequest = null;
    private WebsiteData weblog = null;
    
    
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a feedRequest object
        this.feedRequest = (WeblogFeedRequest) initData.get("feedRequest");
        if(this.feedRequest == null) {
            throw new RollerException("expected feedRequest from init data");
        }
        
        weblog = feedRequest.getWeblog();
    }
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "model";
    }
    
    
    /**
     * Get the weblog locale used to render this page, null if no locale.
     */
    public String getLocale() {
        return feedRequest.getLocale();
    }
    
    
    /**
     * Get weblog being displayed.
     */
    public WebsiteDataWrapper getWeblog() {
        return WebsiteDataWrapper.wrap(weblog);
    }
    
    
    /**
     * Get category path or name specified by request.
     */
    public boolean getExcerpts() {
        return feedRequest.isExcerpts();
    }
    
    
    /**
     * Get category path or name specified by request.
     */
    public String getCategoryPath() {
        return feedRequest.getWeblogCategoryName();
    }
    
    
    /**
     * Gets most recent entries limited by: weblog and category specified in 
     * request plus the weblog.entryDisplayCount.
     */
    public List getWeblogEntries() {
        
        // all feeds get the site-wide default # of entries
        int entryCount =
                RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
        
        List results = new ArrayList();
        try {             
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            List entries = wmgr.getWeblogEntries(
                    weblog,
                    null, 
                    null, 
                    new Date(), 
                    feedRequest.getWeblogCategoryName(), 
                    WeblogEntryData.PUBLISHED, 
                    "pubTime", 
                    feedRequest.getLocale(), 
                    0, 
                    entryCount);
            for (Iterator it = entries.iterator(); it.hasNext();) {
                WeblogEntryData entry = (WeblogEntryData) it.next();
                results.add(WeblogEntryDataWrapper.wrap(entry));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
    
    
    /**
     * Gets most recent comments limited by: weblog specified in request and 
     * the weblog.entryDisplayCount.
     */
    public List getComments() {
            
        // all feeds get the site-wide default # of entries
        int entryCount =
                RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
        
        List recentComments = new ArrayList();
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List recent = wmgr.getComments(
                    RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle()) ? null : weblog,
                    null,          // weblog entry
                    null,          // search String
                    null,          // startDate
                    new Date(),    // endDate
                    null,          // pending
                    Boolean.TRUE,  // approved only
                    Boolean.FALSE, // no spam
                    true,          // we want reverse chrono order
                    0,             // offset
                    entryCount); // length
            
            // wrap pojos
            recentComments = new ArrayList(recent.size());
            Iterator it = recent.iterator();
            while(it.hasNext()) {
                recentComments.add(CommentDataWrapper.wrap((CommentData) it.next()));
            }
        } catch (RollerException e) {
            log.error("ERROR: getting comments", e);
        }
        return recentComments;
    }
    
}
