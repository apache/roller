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

package org.apache.roller.weblogger.planet.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.fetcher.FetcherException;
import org.apache.roller.planet.business.fetcher.RomeFeedFetcher;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Extends Roller Planet's feed fetcher to fetch local feeds directly from 
 * Weblogger so we don't waste time with lots of feed processing.
 *
 * We expect local feeds to have urls of the style ... weblogger:<blog handle>
 */
public class WebloggerRomeFeedFetcher extends RomeFeedFetcher {
    
    private static Log log = LogFactory.getLog(WebloggerRomeFeedFetcher.class); 
    
    
    /**
     * Creates a new instance of WebloggerRomeFeedFetcher
     */
    public WebloggerRomeFeedFetcher() {
        super();
    }
    
    
    @Override
    public Subscription fetchSubscription(String feedURL, Date lastModified)
            throws FetcherException {
        
        if(feedURL == null) {
            throw new IllegalArgumentException("feed url cannot be null");
        }
        
        // we handle special weblogger planet integrated subscriptions which have
        // feedURLs defined as ... weblogger:<blog handle>
        if(!feedURL.startsWith("weblogger:")) {
            log.debug("Feed is remote, letting parent handle it - "+feedURL);            
            return super.fetchSubscription(feedURL, lastModified);
        }
        
        // extract blog handle from our special feed url
        String weblogHandle = null;
        String[] items = feedURL.split(":", 2);
        if(items != null && items.length > 1) {
            weblogHandle = items[1];
        }
        
        log.debug("Handling LOCAL feed - "+feedURL);
        
        Weblog localWeblog;
        try {
            localWeblog = WebloggerFactory.getWeblogger().getWeblogManager()
                    .getWeblogByHandle(weblogHandle);
            if (localWeblog == null) {
                throw new FetcherException("Local feed - "+feedURL+" no longer exists in weblogger");
            }
            
        } catch (WebloggerException ex) {
            throw new FetcherException("Problem looking up local weblog - "+weblogHandle, ex);
        }
        
        // if weblog hasn't changed since last fetch then bail
        if(lastModified != null && !localWeblog.getLastModified().after(lastModified)) {
            log.debug("Skipping unmodified LOCAL weblog");
            return null;
        }
        
        // build planet subscription from weblog
        Subscription newSub = new Subscription();
        newSub.setFeedURL(feedURL);
        newSub.setSiteURL(WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(localWeblog, null, true));
        newSub.setTitle(localWeblog.getName());
        newSub.setAuthor(localWeblog.getName());
        newSub.setLastUpdated(localWeblog.getLastModified());
        
        // must have a last updated time
        if(newSub.getLastUpdated() == null) {
            newSub.setLastUpdated(new Date());
        }
        
        // lookup recent entries from weblog and add them to the subscription
        try {
            int entryCount = WebloggerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");

            if (log.isDebugEnabled()) {
                log.debug("Seeking up to " + entryCount + " entries from " + localWeblog.getHandle());
            }
            
            // grab recent entries for this weblog
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            List<WeblogEntry> entries = wmgr.getWeblogEntries(
                    localWeblog,                 // weblog
                    null,                        // user
                    null,                        // startDate
                    null,                        // endDate
                    null,                        // catName
                    null,                        // tags
                    WeblogEntry.PUBLISHED,       // status
                    null,                        // text
                    null,                        // sortby (null means pubTime)
                    null,                        // sortOrder
                    null,                        // locale
                    0,                           // offset
                    entryCount);                 // range
            
            log.debug("Found " + entries.size());

            // Populate subscription object with new entries
            PluginManager ppmgr = WebloggerFactory.getWeblogger().getPluginManager();
            Map pagePlugins = ppmgr.getWeblogEntryPlugins(localWeblog);
            for ( WeblogEntry rollerEntry : entries ) {
                SubscriptionEntry entry = new SubscriptionEntry();
                String content = "";
                if (!StringUtils.isEmpty(rollerEntry.getText())) {
                    content = rollerEntry.getText();
                } else {
                    content = rollerEntry.getSummary();
                }
                content = ppmgr.applyWeblogEntryPlugins(pagePlugins, rollerEntry, content);
                
                entry.setAuthor(rollerEntry.getCreator().getScreenName());
                entry.setTitle(rollerEntry.getTitle());
                entry.setPubTime(rollerEntry.getPubTime());
                entry.setText(content);
                entry.setPermalink(rollerEntry.getPermalink());
                entry.setCategoriesString(rollerEntry.getCategory().getPath());
                
                newSub.addEntry(entry);
            }
            
        } catch (WebloggerException ex) {
            throw new FetcherException("Error processing entries for local weblog - "+weblogHandle, ex);
        }
        
        // all done
        return newSub;
    }
        
}
