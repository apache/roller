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
package org.apache.roller.weblogger.ui.rendering.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogsPager;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;


/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
public class SiteModel implements Model {
    
    private static Log log = LogFactory.getLog(SiteModel.class);   
    
    private WeblogPageRequest pageRequest = null;
    private String pageLink = null;
    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Override
    public String getModelName() {
        return "site";
    }

    /** Init page model, requires a WeblogPageRequest object. */
    @Override
    public void init(Map initData) throws WebloggerException {
        this.pageRequest = (WeblogPageRequest) initData.get("parsedRequest");

        if (pageRequest == null) {
            throw new WebloggerException("Missing WeblogPageRequest object");
        }

        Template weblogPage = pageRequest.getWeblogTemplate();
        pageLink = (weblogPage != null) ? weblogPage.getRelativePath() : null;
    }
    

    /**
     * Get pager of WeblogEntry objects across all weblogs, in reverse chrono order by pubTime.
     * @param sinceDays Limit to past X days in past (or -1 for no limit)
     * @param length    Max number of results to return
     */
    public Pager getWeblogEntriesPager(int sinceDays, int length) {
        return getWeblogEntriesPager(null, null, sinceDays, length);
    }
    
    /**
     * @param queryWeblog Restrict to this weblog
     * @param cat         Restrict to this category
     * @param sinceDays   Limit to past X days in past (or -1 for no limit)
     * @param length      Max number of results to return
     */   
    public Pager getWeblogEntriesPager(Weblog queryWeblog, String cat, int sinceDays, int length) {
        return new WeblogEntriesTimePager(
                weblogEntryManager,
                propertiesManager,
                urlStrategy,
                queryWeblog,
                cat,
                pageRequest.getTag(),
                pageRequest.getPageNum(),
                length,
                sinceDays,
                pageRequest.getWeblog());
    }

    /*
     * Get pager of most recent Comment objects across all weblogs,
     * in reverse chrono order by postTime.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getCommentsPager(int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(pageRequest.getWeblog(), null,
                pageLink, null, null, null, null, 0, false);

        return new CommentsPager(
            weblogEntryManager,
            urlStrategy,
            pagerUrl,
            null,
            null,
            sinceDays,
            pageRequest.getPageNum(),
            length);
    }


    /** Get pager of weblogs whose handles begin with specified letter */
    public Pager getWeblogsByLetterPager(String letter, int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(pageRequest.getWeblog(), null,
                pageLink, null, null, null, null, 0, false);
        
        if(letter != null && StringUtils.isEmpty(letter)) {
            letter = null;
        }
        
        return new WeblogsPager(weblogManager,
            urlStrategy,
            pagerUrl,
            letter,
            sinceDays,
            pageRequest.getPageNum(),
            length);
    }   
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map getWeblogHandleLetterMap() {
        Map results = new HashMap();
        try {            
            results = weblogManager.getWeblogHandleLetterMap();
        } catch (Exception e) {
            log.error("ERROR: fetching weblog handle letter map", e);
        }
        return results;
    }


    /** Get Website object by handle */
    public Weblog getWeblog(String handle) {
        Weblog weblog = null;
        try {            
            weblog = weblogManager.getWeblogByHandle(handle);
        } catch (Exception e) {
            log.error("ERROR: fetching users by letter", e);
        }
        return weblog;
    }
    
        
    /*
     * Get most recent collection of Website objects,
     * in reverse chrono order by creationDate.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List<Weblog> getNewWeblogs(int sinceDays, int length) {
        List<Weblog> results = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            List<Weblog> weblogs = weblogManager.getWeblogs(true, true, startDate, null, 0, length);
            for (Weblog weblog : weblogs) {
                results.add(weblog);
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
           

    /**
     * Get list of WebsiteDisplay objects, ordered by number of hits.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param length      Max number of results to return
     */
    public List<StatCount> getHotWeblogs(int sinceDays, int length) {
        
        List<StatCount> results = new ArrayList<>();
        try {
            List<Weblog> hotBlogs = weblogManager.getHotWeblogs(sinceDays, 0, length);

            for (Weblog weblog : hotBlogs) {
                StatCount statCount = new StatCount(
                  weblog.getId(), weblog.getHandle(), weblog.getName(), "statCount.weblogDayHits", weblog.getHitsToday()
                );
                statCount.setWeblogHandle(weblog.getHandle());
                results.add(statCount);
            }
        } catch (Exception e) {
            log.error("ERROR: fetching hot weblog list", e);
        }
        
        return results;
    }
    
    
    /**
     * Get pinned entries.
     * @param length    Max number of results to return
     */
    public List<WeblogEntry> getPinnedWeblogEntries(int length) {
        List<WeblogEntry> results = new ArrayList<>();
        try {            
            List<WeblogEntry> weblogEntries = weblogEntryManager.getWeblogEntriesPinnedToMain(length);
            for (WeblogEntry entry : weblogEntries) {
                results.add(entry);
            }
        } catch (Exception e) {
            log.error("ERROR: fetching pinned weblog entries", e);
        }
        return results;
    }
        
}
