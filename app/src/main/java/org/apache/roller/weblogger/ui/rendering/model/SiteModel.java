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
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.UsersPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogsPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;


/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
public class SiteModel implements Model {
    
    private static Log log = LogFactory.getLog(SiteModel.class);   
    
    private Weblog weblog = null;
    private WeblogRequest weblogRequest = null;
    private WeblogFeedRequest feedRequest = null;
    private List tags = new ArrayList();
    private String pageLink = null;
    private int pageNum = 0;
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

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String getModelName() {
        return "site";
    }
    
    public void init(Map initData) throws WebloggerException {
        
        // we expect the init data to contain a weblogRequest object
        this.weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if(this.weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        if (weblogRequest instanceof WeblogPageRequest) {
            ThemeTemplate weblogPage = ((WeblogPageRequest)weblogRequest).getWeblogPage();
            pageLink = (weblogPage != null) ? weblogPage.getLink() : null;
            pageNum = ((WeblogPageRequest)weblogRequest).getPageNum();
            tags = ((WeblogPageRequest)weblogRequest).getTags();
        } else if (weblogRequest instanceof WeblogFeedRequest) {
            this.feedRequest = (WeblogFeedRequest) weblogRequest;
            tags = feedRequest.getTags();
            pageNum = feedRequest.getPage();
        }
        
        // extract weblog object
        weblog = weblogRequest.getWeblog();
    }
    
    //----------------------------------------------------------------- Pagers
    
    /**
     * Get pager ofWeblogEntry objects across all weblogs, in reverse chrono order by pubTime.
     * @param sinceDays Limit to past X days in past (or -1 for no limit)
     * @param length    Max number of results to return
     */
    public Pager getWeblogEntriesPager(int sinceDays, int length) {
        return getWeblogEntriesPager(null, null, null, sinceDays, length);
    }
    
    /**
     * @param queryWeblog Restrict to this weblog
     * @param user        Restrict to this user
     * @param cat         Restrict to this category
     * @param sinceDays   Limit to past X days in past (or -1 for no limit)
     * @param length      Max number of results to return
     */   
    public Pager getWeblogEntriesPager(Weblog queryWeblog, User user, String cat, int sinceDays, int length) {
        return new WeblogEntriesTimePager(
                weblogEntryManager,
                urlStrategy,
                queryWeblog,
                user,
                cat,
                tags,
                pageNum,
                length,
                sinceDays,
                weblog);
    }
    
    
    /*
     * Get pager of most recent Comment objects across all weblogs,
     * in reverse chrono order by postTime.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getCommentsPager(int sinceDays, int length) {
        
        String pagerUrl;
        if (feedRequest != null) {
            pagerUrl = urlStrategy.getWeblogFeedURL(weblog, 
                    feedRequest.getType(),
                    feedRequest.getFormat(), null, null, null,
                    feedRequest.isExcerpts(), true);
        } else {        
            pagerUrl = urlStrategy.getWeblogPageURL(weblog, null,
                pageLink,
                null, null, null, null, 0, false);
        }
        
        return new CommentsPager(
            weblogEntryManager,
            urlStrategy,
            pagerUrl,
            null,
            sinceDays,
            pageNum, 
            length);
    }     
    
    
    /* Get pager of users whose names begin with specified letter */
    public Pager getUsersByLetterPager(String letter, int sinceDays, int length) {
        
        String pagerUrl;
        if (feedRequest != null) {
            pagerUrl = urlStrategy.getWeblogFeedURL(weblog, 
                    feedRequest.getType(),
                    feedRequest.getFormat(), null, null, null, feedRequest.isExcerpts(), true);
        } else {        
            pagerUrl = urlStrategy.getWeblogPageURL(weblog, null,
                pageLink,
                null, null, null, null, 0, false);
        }        
        
        if(letter != null && StringUtils.isEmpty(letter)) {
            letter = null;
        }
        
        return new UsersPager(userManager,
            urlStrategy,
            pagerUrl,
            letter,
            sinceDays,
            pageNum, 
            length);
    }      
    
    
    /** Get pager of weblogs whose handles begin with specified letter */
    public Pager getWeblogsByLetterPager(String letter, int sinceDays, int length) {
        
        String pagerUrl = urlStrategy.getWeblogPageURL(weblog, null,
                pageLink,
                null, null, null, null, 0, false);
        
        if(letter != null && StringUtils.isEmpty(letter)) {
            letter = null;
        }
        
        return new WeblogsPager(weblogManager,
            urlStrategy,
            pagerUrl,
            letter,
            sinceDays,
            pageNum, 
            length);
    }   
    
    //--------------------------------------------------- User/weblog directory 

    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of users whose
     * names start with each letter.
     */
    public Map getUserNameLetterMap() {
        Map results = new HashMap();
        try {            
            results = userManager.getUserNameLetterMap();
        } catch (Exception e) {
            log.error("ERROR: fetching username letter map", e);
        }
        return results;
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
    

    /** 
     * Return list of weblogs that user belongs to.
     */
    public List getUsersWeblogs(String userName) {
        List results = new ArrayList();
        try {            
            User user = userManager.getUserByUserName(userName);
            List<UserWeblogRole> roles = userManager.getWeblogRoles(user);
            for (UserWeblogRole role : roles) {
                results.add(role.getWeblog().templateCopy());
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
    
    
    /** 
     * Return list of users that belong to website.
     */
    public List<User> getWeblogsUsers(String handle) {
        List<User> results = new ArrayList<>();
        try {            
            Weblog website = weblogManager.getWeblogByHandle(handle);
            List<UserWeblogRole> roles = userManager.getWeblogRoles(website);
            for (UserWeblogRole role : roles) {
                results.add(role.getUser().templateCopy());
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }

    /** Get User object by username */
    public User getUser(String username) {
        User wrappedUser = null;
        try {            
            User user = userManager.getUserByUserName(username, Boolean.TRUE);
            wrappedUser = user.templateCopy();
        } catch (Exception e) {
            log.error("ERROR: fetching users by letter", e);
        }
        return wrappedUser;
    }
    
    
    /** Get Website object by handle */
    public Weblog getWeblog(String handle) {
        Weblog wrappedWebsite = null;
        try {            
            Weblog weblog = weblogManager.getWeblogByHandle(handle);
            wrappedWebsite = weblog.templateCopy();
        } catch (Exception e) {
            log.error("ERROR: fetching users by letter", e);
        }
        return wrappedWebsite;
    }
    
        
    //------------------------------------------------------- Small collections
    
    /*
     * Get most collection of Website objects,
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
            List<Weblog> weblogs = weblogManager.getWeblogs(
                Boolean.TRUE, Boolean.TRUE, startDate, null, 0, length);
            for (Weblog weblog : weblogs) {
                results.add(weblog.templateCopy());
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
           

    /*
     * Get most recent User objects, in reverse chrono order by creationDate.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List<User> getNewUsers(int sinceDays, int length) {
        List<User> results = new ArrayList<>();
        try {            
            List<User> users = userManager.getUsers(Boolean.TRUE, null, null, 0, length);
            for (User user : users) {
                results.add(user.templateCopy());
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
        
        List<StatCount> results = new ArrayList<StatCount>();
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
     * Get most collection of most commented websites, as StatCount objects,
     * in descending order by number of comments.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param length   Max number of results to return
     */
    public List getMostCommentedWeblogs(int sinceDays , int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            results = weblogManager.getMostCommentedWeblogs(
                    startDate, new Date(), 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching commented weblog list", e);
        }
        return results;
    }
    
    
    /**
     * Get most commented weblog entries across all weblogs, as StatCount 
     * objects, in descending order by number of comments.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param cats     To limit results to list of category names
     * @param length      Max number of results to return
     */
    public List getMostCommentedWeblogEntries(
            List cats, int sinceDays, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            results = weblogEntryManager.getMostCommentedWeblogEntries(
                    null, startDate, new Date(), 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching commented weblog entries list", e);
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
                results.add(entry.templateCopy());
            }
        } catch (Exception e) {
            log.error("ERROR: fetching pinned weblog entries", e);
        }
        return results;
    }
        
    /**
     * @param length number of tags to return
     * @return List of most popular tags
     */
    public List<TagStat> getPopularTags(int length) {
        List results = new ArrayList();
        try {
            results = weblogEntryManager.getPopularTags(null, 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching site tags list", e);
        }
        return results;
    }   
    
    
    public long getCommentCount() {
        long count = 0;
        try {
            count = weblogEntryManager.getCommentCount();
        } catch (WebloggerException e) {
            log.error("Error getting comment count for site ", e);
        }
        return count;
    }
    
    
    public long getEntryCount() {
        long count = 0;
        try {
            count = weblogEntryManager.getEntryCount();
        } catch (WebloggerException e) {
            log.error("Error getting entry count for site", e);
        }
        return count;
    }
    
    
    public long getWeblogCount() {
        long count = 0;
        try {
            count = weblogManager.getWeblogCount();
        } catch (WebloggerException e) {
            log.error("Error getting weblog count for site", e);
        }
        return count;
    } 
    
    
    public long getUserCount() {
        long count = 0;
        try {
            count = userManager.getUserCount();
        } catch (WebloggerException e) {
            log.error("Error getting user count for site", e);
        }
        return count;
    }
    
}
