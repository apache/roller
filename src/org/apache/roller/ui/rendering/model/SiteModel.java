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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.UserDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;


/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
public class SiteModel implements Model {
    
    private static Log log = LogFactory.getLog(SiteModel.class);
    
    
    public String getModelName() {
        return "sitePageModel";
    }
    
    public void init(Map map) throws RollerException {
        // no-op for now
    }
    
    
    /**
     * Get most collection of Website objects,
     * in reverse chrono order by creationDate.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getWeblogs(int sinceDays, int offset, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List weblogs = umgr.getWebsites(
                null, Boolean.TRUE, Boolean.TRUE, startDate, null, offset, length);
            for (Iterator it = weblogs.iterator(); it.hasNext();) {
                WebsiteData website = (WebsiteData) it.next();
                results.add(WebsiteDataWrapper.wrap(website));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblog list", e);
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
    
    
    /**
     * Get most collection of most commented websites, as StatCount objects,
     * in descending order by number of comments.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param offset   Offset into results (for paging)
     * @param length   Max number of results to return
     */
    public List getMostCommentedWeblogs(int sinceDays , int offset, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            results = umgr.getMostCommentedWebsites(
                    startDate, new Date(), offset, length);
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
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getMostCommentedWeblogEntries(
            List cats, int sinceDays, int offset, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            results = wmgr.getMostCommentedWeblogEntries(
                    null, startDate, new Date(), offset, length);
        } catch (Exception e) {
            log.error("ERROR: fetching commented weblog entries list", e);
        }
        return results;
    }
    
    
    /**
     * Get most recent WeblogEntry objects across all weblogs,
     * in reverse chrono order by pubTime.
     * @param handle    Restrict to this weblog handle (or 'nil' for all)
     * @param userName  Restrict to this userName (or 'nil' for all)
     * @param cat       Restrict to category (or 'nil' for all)
     * @param sinceDays Limit to past X days in past (or -1 for no limit)
     * @param offset    Offset into results (for paging)
     * @param length    Max number of results to return
     */
    public List getWeblogEntries(String handle, String userName, String cat, int sinceDays, int offset, int length) {
        List results = new ArrayList();
        if (handle != null && handle.equals("nil")) handle = null;
        if (userName != null && userName.equals("nil")) userName = null;
        if (cat != null && cat.equals("nil")) cat = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            UserManager umgr = roller.getUserManager();
            WebsiteData website = null;
            if (handle != null) {
                website = umgr.getWebsiteByHandle(handle);
            }
            UserData user = null;
            if (userName != null) {
                user = umgr.getUserByUserName(userName);
            }
            // TODO 3.0: we may want this to be constrained by a locale
            List entries = wmgr.getWeblogEntries( 
                website, user, startDate, new Date(), cat, WeblogEntryData.PUBLISHED, "pubTime", offset, length, null);
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
     * Get most recent Comment objects across all weblogs,
     * in reverse chrono order by postTime.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getComments(int sinceDays, int offset, int length) {
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {            
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            List entries = wmgr.getComments( 
                null, null, null, startDate, new Date(), 
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, true, offset, length);
            for (Iterator it = entries.iterator(); it.hasNext();) {
                CommentData comment = (CommentData) it.next();
                results.add(CommentDataWrapper.wrap(comment));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching comment list", e);
        }
        return results;
    }
    
    
    /**
     * Get most recent User objects, in reverse chrono order by creationDate.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getUsers(int sinceDays, int offset, int length) {
        List results = new ArrayList();
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List users = umgr.getUsers(offset, length);
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
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getHotWeblogs(int sinceDays, int offset, int length) {
        List results = new ArrayList();
        try {            
            Roller roller = RollerFactory.getRoller();
            RefererManager rmgr = roller.getRefererManager();
            results = rmgr.getHotWeblogs(sinceDays, offset, length);
        } catch (Exception e) {
            log.error("ERROR: fetching hot weblog list", e);
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
    
    
    /** Get collection of users whose names begin with specified letter */
    public List getUsersByLetter(String letter, int offset, int length) {
        List results = new ArrayList();
        letter = letter.toUpperCase();
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List users = umgr.getUsersByLetter(letter.charAt(0), offset, length);
            for (Iterator it = users.iterator(); it.hasNext();) {
                UserData user = (UserData) it.next();
                results.add(UserDataWrapper.wrap(user));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching users by letter", e);
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
    
    
    /** Get collection of weblogs whose handles begin with specified letter */
    public List getWeblogsByLetter(String letter, int offset, int length) {
        List results = new ArrayList();
        letter = letter.toUpperCase();
        try {            
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            List weblogs = umgr.getWeblogsByLetter(letter.charAt(0), offset, length);
            for (Iterator it = weblogs.iterator(); it.hasNext();) {
                WebsiteData website = (WebsiteData) it.next();
                results.add(WebsiteDataWrapper.wrap(website));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching weblogs by letter", e);
        }
        return results;
    }
    
}
