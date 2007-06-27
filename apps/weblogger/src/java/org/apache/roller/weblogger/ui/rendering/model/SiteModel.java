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

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogHitCount;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.UserWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.rendering.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.UsersPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesListPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogsPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.apache.roller.weblogger.util.URLUtilities;


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
    
    
    public String getModelName() {
        return "site";
    }
    
    public void init(Map initData) throws WebloggerException {
        
        // we expect the init data to contain a weblogRequest object
        this.weblogRequest = (WeblogRequest) initData.get("weblogRequest");
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
        
        String pagerUrl = null;
        
        if (feedRequest != null) {
            pagerUrl = URLUtilities.getWeblogFeedURL(weblog, 
                    weblogRequest.getLocale(), feedRequest.getType(),
                    feedRequest.getFormat(), feedRequest.getWeblogCategoryName(), null,
                    feedRequest.getTags(), feedRequest.isExcerpts(), true);
        } else {        
            pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, tags, 0, false);
        }
        
        return new WeblogEntriesListPager(
            pagerUrl, null, null, null,
            tags,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }
    
       
    /**
     * Get pager ofWeblogEntry objects across all weblogs,
     * in reverse chrono order by pubTime.
     * @param queryWeblog Restrict to this weblog
     * @param sinceDays   Limit to past X days in past (or -1 for no limit)
     * @param length      Max number of results to return
     */   
    public Pager getWeblogEntriesPager(Weblog queryWeblog, int sinceDays, int length) {
        return getWeblogEntriesPager(queryWeblog, null, null, sinceDays, length);
    }

    /**
     * Get pager ofWeblogEntry objects across all weblogs,
     * in reverse chrono order by pubTime.
     * @param queryWeblog Restrict to this weblog
     * @param user        Restrict to this user
     * @param sinceDays   Limit to past X days in past (or -1 for no limit)
     * @param length      Max number of results to return
     */   
    public Pager getWeblogEntriesPager(Weblog queryWeblog, User user, int sinceDays, int length) {
        return getWeblogEntriesPager(queryWeblog, user, null, sinceDays, length);
    }

    /**
     * Get pager ofWeblogEntry objects across all weblogs,
     * in reverse chrono order by pubTime.
     * @param queryWeblog Restrict to this weblog
     * @param user        Restrict to this user
     * @param cat         Restrict to this category
     * @param sinceDays   Limit to past X days in past (or -1 for no limit)
     * @param length      Max number of results to return
     */   
    public Pager getWeblogEntriesPager(Weblog queryWeblog, User user, String cat, int sinceDays, int length) {
        
        String pagerUrl = null;
        if (feedRequest != null) {
            pagerUrl = URLUtilities.getWeblogFeedURL(weblog, 
                    weblogRequest.getLocale(), feedRequest.getType(),
                    feedRequest.getFormat(), feedRequest.getWeblogCategoryName(), null,
                    feedRequest.getTags(), feedRequest.isExcerpts(), true);
        } else {
            pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, tags, 0, false);
        }
       
        return new WeblogEntriesListPager(
            pagerUrl, queryWeblog, user, cat,
            tags,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }    
    
    
    /*
     * Get pager of most recent Comment objects across all weblogs,
     * in reverse chrono order by postTime.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getCommentsPager(int sinceDays, int length) {
        
        String pagerUrl = null;
        if (feedRequest != null) {
            pagerUrl = URLUtilities.getWeblogFeedURL(weblog, 
                    weblogRequest.getLocale(), feedRequest.getType(),
                    feedRequest.getFormat(), null, null, null,
                    feedRequest.isExcerpts(), true);
        } else {        
            pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        }
        
        return new CommentsPager(
            pagerUrl,
            null,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }     
    
    
    /* Get pager of users whose names begin with specified letter */
    public Pager getUsersByLetterPager(String letter, int sinceDays, int length) {
        
        String pagerUrl = null;
        if (feedRequest != null) {
            pagerUrl = URLUtilities.getWeblogFeedURL(weblog, 
                    weblogRequest.getLocale(), feedRequest.getType(),
                    feedRequest.getFormat(), null, null, null, feedRequest.isExcerpts(), true);
        } else {        
            pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        }        
        
        if(letter != null && StringUtils.isEmpty(letter)) {
            letter = null;
        }
        
        return new UsersPager(
            pagerUrl,
            letter,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }      
    
    
    /** Get pager of weblogs whose handles begin with specified letter */
    public Pager getWeblogsByLetterPager(String letter, int sinceDays, int length) {
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        if(letter != null && StringUtils.isEmpty(letter)) {
            letter = null;
        }
        
        return new WeblogsPager(
            pagerUrl,
            letter,
            weblogRequest.getLocale(),
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
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            results = umgr.getUserNameLetterMap();
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
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            results = umgr.getWeblogHandleLetterMap();
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
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            User user = umgr.getUserByUserName(userName);
            List perms = umgr.getAllPermissions(user);
            for (Iterator it = perms.iterator(); it.hasNext();) {
                WeblogPermission perm = (WeblogPermission) it.next();
                results.add(WeblogWrapper.wrap(perm.getWebsite()));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
    
    
    /** 
     * Return list of users that belong to website.
     */
    public List getWeblogsUsers(String handle) {
        List results = new ArrayList();
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            Weblog website = umgr.getWebsiteByHandle(handle);
            List perms = umgr.getAllPermissions(website);
            for (Iterator it = perms.iterator(); it.hasNext();) {
                WeblogPermission perm = (WeblogPermission) it.next();
                results.add(UserWrapper.wrap(perm.getUser()));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
    
    
    
    /** Get User object by username */
    public UserWrapper getUser(String username) {
        UserWrapper wrappedUser = null;
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            User user = umgr.getUserByUserName(username, Boolean.TRUE);
            wrappedUser = UserWrapper.wrap(user);
        } catch (Exception e) {
            log.error("ERROR: fetching users by letter", e);
        }
        return wrappedUser;
    }
    
    
    /** Get Website object by handle */
    public WeblogWrapper getWeblog(String handle) {
        WeblogWrapper wrappedWebsite = null;
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            Weblog website = umgr.getWebsiteByHandle(handle);
            wrappedWebsite = WeblogWrapper.wrap(website);
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
    public List getNewWeblogs(int sinceDays, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List weblogs = umgr.getWebsites(
                null, Boolean.TRUE, Boolean.TRUE, startDate, null, 0, length);
            for (Iterator it = weblogs.iterator(); it.hasNext();) {
                Weblog website = (Weblog) it.next();
                results.add(WeblogWrapper.wrap(website));
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
    public List getNewUsers(int sinceDays, int length) {
        List results = new ArrayList();
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List users = umgr.getUsers(null, Boolean.TRUE, null, null, 0, length);
            for (Iterator it = users.iterator(); it.hasNext();) {
                User user = (User) it.next();
                results.add(UserWrapper.wrap(user));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }   
    
    
    /**
     * Get list of WebsiteDisplay objects, ordered by number of hits.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param len      Max number of results to return
     */
    public List getHotWeblogs(int sinceDays, int length) {
        
        List results = new ArrayList();
        try {
            WeblogManager mgr = WebloggerFactory.getRoller().getWeblogManager();
            List hotBlogs = mgr.getHotWeblogs(sinceDays, 0, length);
            
            Iterator hitCounts = hotBlogs.iterator();
            while (hitCounts.hasNext()) {
                WeblogHitCount hitCount = (WeblogHitCount) hitCounts.next();
                StatCount statCount = new StatCount(
                    hitCount.getWeblog().getId(),
                    hitCount.getWeblog().getHandle(),
                    hitCount.getWeblog().getName(),
                    "statCount.weblogDayHits",
                    hitCount.getDailyHits());
                statCount.setWeblogHandle(hitCount.getWeblog().getHandle());
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
     * @param offset   Offset into results (for paging)
     * @param length   Max number of results to return
     */
    public List getMostCommentedWeblogs(int sinceDays , int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            results = umgr.getMostCommentedWebsites(
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
     * @param len      Max number of results to return
     */
    public List getMostCommentedWeblogEntries(
            List cats, int sinceDays, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            results = wmgr.getMostCommentedWeblogEntries(
                    null, startDate, new Date(), 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching commented weblog entries list", e);
        }
        return results;
    }
    
    /**
     * Get pinned entries.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param length    Max number of results to return
     */
    public List getPinnedWeblogEntries(int length) {
        List results = new ArrayList();
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            List weblogs = wmgr.getWeblogEntriesPinnedToMain(new Integer(length));
            for (Iterator it = weblogs.iterator(); it.hasNext();) {
                WeblogEntry entry = (WeblogEntry) it.next();
                results.add(WeblogEntryWrapper.wrap(entry));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching pinned weblog entries", e);
        }
        return results;
    }
        
    /**
     * 
     * @param sinceDays
     * @param length
     * @return
     */
    public List getPopularTags(int sinceDays, int length) {
        List results = new ArrayList();
        Date startDate = null;
        if(sinceDays > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);        
            startDate = cal.getTime();     
        }
        
        try {            
            Weblogger roller = WebloggerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            results = wmgr.getPopularTags(null, startDate, length);
        } catch (Exception e) {
            log.error("ERROR: fetching site tags list", e);
        }
        return results;
    }   
    
    
    public long getCommentCount() {
        long count = 0;
        try {
            Weblogger roller = WebloggerFactory.getRoller();
            WeblogManager mgr = roller.getWeblogManager();
            count = mgr.getCommentCount();            
        } catch (WebloggerException e) {
            log.error("Error getting comment count for site ", e);
        }
        return count;
    }
    
    
    public long getEntryCount() {
        long count = 0;
        try {
            Weblogger roller = WebloggerFactory.getRoller();
            WeblogManager mgr = roller.getWeblogManager();
            count = mgr.getEntryCount();            
        } catch (WebloggerException e) {
            log.error("Error getting entry count for site", e);
        }
        return count;
    }
    
    
    public long getWeblogCount() {
        long count = 0;
        try {
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager mgr = roller.getUserManager();
            count = mgr.getWeblogCount();            
        } catch (WebloggerException e) {
            log.error("Error getting weblog count for site", e);
        }
        return count;
    } 
    
    
    public long getUserCount() {
        long count = 0;
        try {
            Weblogger roller = WebloggerFactory.getRoller();
            UserManager mgr = roller.getUserManager();
            count = mgr.getUserCount();            
        } catch (WebloggerException e) {
            log.error("Error getting user count for site", e);
        }
        return count;
    }
    
}
