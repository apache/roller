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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.UserDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.rendering.pagers.CommentsPager;
import org.apache.roller.ui.rendering.pagers.Pager;
import org.apache.roller.ui.rendering.pagers.UsersPager;
import org.apache.roller.ui.rendering.pagers.WeblogEntriesListPager;
import org.apache.roller.ui.rendering.pagers.WeblogsPager;
import org.apache.roller.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.URLUtilities;


/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
public class SiteModel implements Model {
    
    private static Log log = LogFactory.getLog(SiteModel.class);   
    
    private WebsiteData weblog = null;
    private WeblogRequest weblogRequest = null;
    private List tags = new ArrayList();
    private String pageLink = null;
    private int pageNum = 0;
    
    
    public String getModelName() {
        return "site";
    }
    
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a weblogRequest object
        this.weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(this.weblogRequest == null) {
            throw new RollerException("expected weblogRequest from init data");
        }
        
        if (weblogRequest instanceof WeblogPageRequest) {
            Template weblogPage = ((WeblogPageRequest)weblogRequest).getWeblogPage();
            pageLink = (weblogPage != null) ? weblogPage.getLink() : null;
            pageNum = ((WeblogPageRequest)weblogRequest).getPageNum();
            tags = ((WeblogPageRequest)weblogRequest).getTags();
        } else if (weblogRequest instanceof WeblogFeedRequest) {
            tags = ((WeblogFeedRequest)weblogRequest).getTags();
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
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
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
    public Pager getWeblogEntriesPager(WebsiteData queryWeblog, int sinceDays, int length) {
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
    public Pager getWeblogEntriesPager(WebsiteData queryWeblog, UserData user, int sinceDays, int length) {
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
    public Pager getWeblogEntriesPager(WebsiteData queryWeblog, UserData user, String cat, int sinceDays, int length) {
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
       
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
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
        return new CommentsPager(
            pagerUrl,
            weblogRequest.getLocale(),
            sinceDays,
            pageNum, 
            length);
    }     
    
    
    /* Get pager of users whose names begin with specified letter */
    public Pager getUsersByLetterPager(String letter, int sinceDays, int length) {
        
        String pagerUrl = URLUtilities.getWeblogPageURL(weblog, 
                weblogRequest.getLocale(), pageLink, 
                null, null, null, null, 0, false);
        
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
            Roller roller = RollerFactory.getRoller();
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
            Roller roller = RollerFactory.getRoller();
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
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            UserData user = umgr.getUserByUserName(userName);
            List perms = umgr.getAllPermissions(user);
            for (Iterator it = perms.iterator(); it.hasNext();) {
                PermissionsData perm = (PermissionsData) it.next();
                results.add(WebsiteDataWrapper.wrap(perm.getWebsite()));
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
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            WebsiteData website = umgr.getWebsiteByHandle(handle);
            List perms = umgr.getAllPermissions(website);
            for (Iterator it = perms.iterator(); it.hasNext();) {
                PermissionsData perm = (PermissionsData) it.next();
                results.add(UserDataWrapper.wrap(perm.getUser()));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
        }
        return results;
    }
    
    
    
    /** Get User object by username */
    public UserDataWrapper getUser(String username) {
        UserDataWrapper wrappedUser = null;
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            UserData user = umgr.getUserByUserName(username, Boolean.TRUE);
            wrappedUser = UserDataWrapper.wrap(user);
        } catch (Exception e) {
            log.error("ERROR: fetching users by letter", e);
        }
        return wrappedUser;
    }
    
    
    /** Get Website object by handle */
    public WebsiteDataWrapper getWeblog(String handle) {
        WebsiteDataWrapper wrappedWebsite = null;
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            WebsiteData website = umgr.getWebsiteByHandle(handle);
            wrappedWebsite = WebsiteDataWrapper.wrap(website);
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
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List weblogs = umgr.getWebsites(
                null, Boolean.TRUE, Boolean.TRUE, startDate, null, 0, length);
            for (Iterator it = weblogs.iterator(); it.hasNext();) {
                WebsiteData website = (WebsiteData) it.next();
                results.add(WebsiteDataWrapper.wrap(website));
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
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List users = umgr.getUsers(0, length);
            for (Iterator it = users.iterator(); it.hasNext();) {
                UserData user = (UserData) it.next();
                results.add(UserDataWrapper.wrap(user));
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
            Roller roller = RollerFactory.getRoller();
            RefererManager rmgr = roller.getRefererManager();
            results = rmgr.getHotWeblogs(sinceDays, 0, length);
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
            Roller roller = RollerFactory.getRoller();
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
            Roller roller = RollerFactory.getRoller();
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
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            List weblogs = wmgr.getWeblogEntriesPinnedToMain(new Integer(length));
            for (Iterator it = weblogs.iterator(); it.hasNext();) {
                WeblogEntryData entry = (WeblogEntryData) it.next();
                results.add(WeblogEntryDataWrapper.wrap(entry));
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
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            results = wmgr.getPopularTags(null, startDate, length);
        } catch (Exception e) {
            log.error("ERROR: fetching site tags list", e);
        }
        return results;
    }    
    
}
