
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
package org.apache.roller.weblogger.business.jpa;

import java.sql.Timestamp;
import javax.persistence.NoResultException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Comparator;
import javax.persistence.Query;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;

/*
 * JPAUserManagerImpl.java
 *
 * Created on May 29, 2006, 3:15 PM
 *
 */
@com.google.inject.Singleton
public class JPAUserManagerImpl implements UserManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(JPAUserManagerImpl.class);
    
    private static final Comparator statCountCountReverseComparator =
            Collections.reverseOrder(StatCountCountComparator.getInstance());
    
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable();
    
    // cached mapping of userNames -> userIds
    private Map userNameToIdMap = new Hashtable();
    
    
    @com.google.inject.Inject
    protected JPAUserManagerImpl(Weblogger roller, JPAPersistenceStrategy strat) {
        log.debug("Instantiating JPA User Manager");
        this.roller = roller;
        this.strategy = strat;
    }
    
    /**
     * Update existing website.
     */
    public void saveWebsite(Weblog website) throws WebloggerException {
        
        website.setLastModified(new java.util.Date());
        strategy.store(website);
    }
    
    public void removeWebsite(Weblog weblog) throws WebloggerException {
        
        // remove contents first, then remove website
        this.removeWebsiteContents(weblog);
        this.strategy.remove(weblog);
        
        // remove entry from cache mapping
        this.weblogHandleToIdMap.remove(weblog.getHandle());
    }
    
    /**
     * convenience method for removing contents of a weblog.
     * TODO BACKEND: use manager methods instead of queries here
     */
    private void removeWebsiteContents(Weblog website)
    throws  WebloggerException {
        
        BookmarkManager bmgr = roller.getBookmarkManager();
        WeblogManager wmgr = roller.getWeblogManager();
        
        // remove tags
        Query tagQuery = strategy.getNamedQuery("WeblogEntryTag.getByWeblog");
        tagQuery.setParameter(1, website);
        List results = tagQuery.getResultList();
        
        for(Iterator iter = results.iterator(); iter.hasNext();) {
            WeblogEntryTag tagData = (WeblogEntryTag) iter.next();
            this.strategy.remove(tagData);
        }
        
        // remove site tag aggregates
        List tags = wmgr.getTags(website, null, null, -1);
        updateTagAggregates(tags);
        
        // delete all weblog tag aggregates
        Query removeAggs= strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.removeByWeblog");
        removeAggs.setParameter(1, website);
        removeAggs.executeUpdate();
        
        // delete all bad counts
        Query removeCounts = strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.removeByTotalLessEqual");
        removeCounts.setParameter(1, new Integer(0));
        removeCounts.executeUpdate();
        
        
        // Remove the website's ping queue entries
        Query q = strategy.getNamedQuery("PingQueueEntry.getByWebsite");
        q.setParameter(1, website);
        List queueEntries = q.getResultList();
        Iterator it = queueEntries.iterator();
        while(it.hasNext()) {
            this.strategy.remove((PingQueueEntry) it.next());
        }
        
        // Remove the website's auto ping configurations
        AutoPingManager autoPingMgr = roller
        .getAutopingManager();
        List autopings = autoPingMgr.getAutoPingsByWebsite(website);
        it = autopings.iterator();
        while(it.hasNext()) {
            this.strategy.remove((AutoPing) it.next());
        }
        
        // Remove the website's custom ping targets
        PingTargetManager pingTargetMgr = roller.getPingTargetManager();
        List pingtargets = pingTargetMgr.getCustomPingTargets(website);
        it = pingtargets.iterator();
        while(it.hasNext()) {
            this.strategy.remove((PingTarget) it.next());
        }
        
        // remove associated referers
        Query refQuery2 = strategy.getNamedQuery("WeblogReferrer.getByWebsite");
        refQuery2.setParameter(1, website);
        List referers = refQuery2.getResultList();
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            WeblogReferrer referer = (WeblogReferrer) iter.next();
            this.strategy.remove(referer);
        }
        
        // remove associated pages
        Query pageQuery = strategy.getNamedQuery("WeblogTemplate.getByWebsite");
        pageQuery.setParameter(1, website);
        List pages = pageQuery.getResultList();
        for (Iterator iter = pages.iterator(); iter.hasNext();) {
            WeblogTemplate page = (WeblogTemplate) iter.next();
            this.strategy.remove(page);
        }
        
        // remove folders (including bookmarks)
        WeblogBookmarkFolder rootFolder = bmgr.getRootFolder(website);
        if (null != rootFolder) {
            this.strategy.remove(rootFolder);
        }
        
        this.strategy.flush();

        // remove entries
        Query refQuery = strategy.getNamedQuery("WeblogEntry.getByWebsite");
        refQuery.setParameter(1, website);
        List entries = refQuery.getResultList();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            WeblogEntry entry = (WeblogEntry) iter.next();
            wmgr.removeWeblogEntry(entry);
        }
        
        // remove categories
        WeblogCategory rootCat = wmgr.getRootWeblogCategory(website);
        if (null != rootCat) {
            this.strategy.remove(rootCat);
        }
        
        // remove permissions
        // make sure that both sides of the relationship are maintained
        for (Iterator iterator = website.getPermissions().iterator(); iterator.hasNext();) {
            WeblogPermission perms = (WeblogPermission) iterator.next();
            
            //Remove it from website
            iterator.remove();
            
            //Remove it from corresponding user
            User user = perms.getUser();
            user.getPermissions().remove(perms);

            //Remove it from database
            this.strategy.remove(perms);
        }
        
        // flush the changes before returning. This is required as there is a
        // circular dependency between WeblogCategory and Weblog
        this.strategy.flush();
        
        // remove uploaded files
        FileManager fmgr = WebloggerFactory.getWeblogger().getFileManager();
        fmgr.deleteAllFiles(website);
    }
    
    protected void updateTagAggregates(List tags) throws WebloggerException {
        for(Iterator iter = tags.iterator(); iter.hasNext();) {
            TagStat stat = (TagStat) iter.next();            
            Query query = strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.getByName&WebsiteNullOrderByLastUsedDesc");
            query.setParameter(1, stat.getName());
            try {
                WeblogEntryTagAggregate agg = (WeblogEntryTagAggregate)query.getSingleResult();
                agg.setTotal(agg.getTotal() - stat.getCount());
            } catch (NoResultException ignored) {} // no agg to be updated
        }
    }
    
    public void saveUser(User data) throws WebloggerException {
        this.strategy.store(data);
    }
    
    public void removeUser(User user) throws WebloggerException {
        //remove permissions
        // make sure that both sides of the relationship are maintained
        for (Iterator iterator = user.getPermissions().iterator(); iterator.hasNext();) {
            WeblogPermission perms = (WeblogPermission) iterator.next();
            //Remove it from database
            this.strategy.remove(perms);
            //Remove it from website
            iterator.remove();
            //Remove it from corresponding user
            Weblog website = perms.getWebsite();
            website.getPermissions().remove(perms);
        }
        
        this.strategy.remove(user);
        
        // remove entry from cache mapping
        this.userNameToIdMap.remove(user.getUserName());
    }
    
    public void savePermissions(WeblogPermission perms)
    throws WebloggerException {
        //if (getPermissions(perms.getWebsite(), perms.getUser()) != null) {
            //throw new WebloggerException("ERROR: user already has permission in weblog");
        //}
        if (getPermissions(perms.getId()) == null) { 
            // This is a new object make sure that relationship is set on managed
            // copy of other side
            Weblog website = perms.getWebsite(); //(Weblog) getManagedObject(perms.getWebsite());
            website.getPermissions().add(perms);
            
            User user = perms.getUser(); //(User) getManagedObject(perms.getUser());
            user.getPermissions().add(perms);
        }
        this.strategy.store(perms);
    }
    
    public void removePermissions(WeblogPermission perms)
    throws WebloggerException {
        this.strategy.remove(perms);
        // make sure that relationship is set on managed
        // copy of other side
        Weblog website = perms.getWebsite(); //Weblog) getManagedObject(perms.getWebsite());
        website.getPermissions().remove(perms);
        
        User user = perms.getUser(); //(User) getManagedObject(perms.getUser());
        user.getPermissions().remove(perms);
    }
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#storePage(org.apache.roller.weblogger.pojos.WeblogTemplate)
     */
    public void savePage(WeblogTemplate page) throws WebloggerException {
        this.strategy.store(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getUserManager()
        .saveWebsite(page.getWebsite());
    }
    
    public void removePage(WeblogTemplate page) throws WebloggerException {
        this.strategy.remove(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getUserManager().saveWebsite(page.getWebsite());
    }
    
    public void addUser(User newUser) throws WebloggerException {
        
        if(newUser == null)
            throw new WebloggerException("cannot add null user");
        
        // TODO BACKEND: we must do this in a better fashion, like getUserCnt()?
        boolean adminUser = false;
        List existingUsers = this.getUsers(null, Boolean.TRUE, null, null, 0, 1);
        if(existingUsers.size() == 0) {
            // Make first user an admin
            adminUser = true;
            
            //if user was disabled (because of activation user 
            // account with e-mail property), enable it for admin user
            newUser.setEnabled(Boolean.TRUE);
            newUser.setActivationCode(null);
        }
        
        if(getUserByUserName(newUser.getUserName()) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new WebloggerException("error.add.user.userNameInUse");
        }
        
        newUser.grantRole("editor");
        if(adminUser) {
            newUser.grantRole("admin");
        }
        
        this.strategy.store(newUser);
    }
    
    public void addWebsite(Weblog newWeblog) throws WebloggerException {
        
        this.strategy.store(newWeblog);
        this.strategy.flush();
        this.addWeblogContents(newWeblog);
    }
    
    private void addWeblogContents(Weblog newWeblog)
    throws WebloggerException {
        
        // grant weblog creator ADMIN permissions
        WeblogPermission perms = new WeblogPermission();
        perms.setUser(newWeblog.getCreator());
        perms.setWebsite(newWeblog);
        perms.setPending(false);
        perms.setPermissionMask(WeblogPermission.ADMIN);
        savePermissions(perms);
        
        // add default category
        WeblogCategory rootCat = new WeblogCategory(
                newWeblog, // newWeblog
                null,      // parent
                "root",    // name
                "root",    // description
                null );    // image
        this.strategy.store(rootCat);
        
        String cats = WebloggerConfig.getProperty("newuser.categories");
        WeblogCategory firstCat = rootCat;
        if (cats != null && cats.trim().length() > 0) {
            String[] splitcats = cats.split(",");
            for (int i=0; i<splitcats.length; i++) {
                WeblogCategory c = new WeblogCategory(
                        newWeblog,       // newWeblog
                        rootCat,         // parent
                        splitcats[i],    // name
                        splitcats[i],    // description
                        null );          // image
                if (i == 0) firstCat = c;
                this.strategy.store(c);
            }
        }
        
        // Use first category as default for Blogger API
        newWeblog.setBloggerCategory(firstCat);
        
        // But default category for weblog itself should be  root
        newWeblog.setDefaultCategory(rootCat);
        
        this.strategy.store(newWeblog);
        
        // add default bookmarks
        WeblogBookmarkFolder root = new WeblogBookmarkFolder(
                null, "root", "root", newWeblog);
        this.strategy.store(root);
        
        Integer zero = new Integer(0);
        String blogroll = WebloggerConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (int i=0; i<splitroll.length; i++) {
                String[] rollitems = splitroll[i].split("\\|");
                if (rollitems != null && rollitems.length > 1) {
                    WeblogBookmark b = new WeblogBookmark(
                            root,                // parent
                            rollitems[0],        // name
                            "",                  // description
                            rollitems[1].trim(), // url
                            null,                // feedurl
                            zero,                // weight
                            zero,                // priority
                            null);               // image
                    this.strategy.store(b);
                }
            }
        }
        
        // add any auto enabled ping targets
        PingTargetManager pingTargetMgr = roller
        .getPingTargetManager();
        AutoPingManager autoPingMgr = roller
        .getAutopingManager();
        
        Iterator pingTargets = pingTargetMgr.getCommonPingTargets().iterator();
        PingTarget pingTarget = null;
        while(pingTargets.hasNext()) {
            pingTarget = (PingTarget) pingTargets.next();
            
            if(pingTarget.isAutoEnabled()) {
                AutoPing autoPing = new AutoPing(
                        null, pingTarget, newWeblog);
                autoPingMgr.saveAutoPing(autoPing);
            }
        }
    }
    
    /**
     * Creates and stores a pending WeblogPermission for user and website specified.
     * TODO BACKEND: do we really need this?  can't we just use storePermissions()?
     */
    public WeblogPermission inviteUser(Weblog website,
            User user, short mask) throws WebloggerException {
        
        if (website == null) throw new WebloggerException("Website cannot be null");
        if (user == null) throw new WebloggerException("User cannot be null");
        
        WeblogPermission perms = new WeblogPermission();
        perms.setWebsite(website);
        perms.setUser(user);
        perms.setPermissionMask(mask);
        savePermissions(perms);
        
        return perms;
    }
    
    /**
     * Remove user permissions from a website.
     *
     * TODO: replace this with a domain model method like weblog.retireUser(user)
     */
    public void retireUser(Weblog website, User user) throws WebloggerException {
        
        if (website == null) throw new WebloggerException("Website cannot be null");
        if (user == null) throw new WebloggerException("User cannot be null");
        
        Iterator perms = website.getPermissions().iterator();
        WeblogPermission target = null;
        while (perms.hasNext()) {
            WeblogPermission pd = (WeblogPermission)perms.next();
            if (pd.getUser().getId().equals(user.getId())) {
                target = pd;
                break;
            }
        }
        if (target == null) throw new WebloggerException("User not member of website");
        
        website.removePermission(target);
        this.strategy.remove(target);
    }
    
    public void revokeRole(String roleName, User user) throws WebloggerException {
        Collection roles = user.getRoles();
        Iterator iter = roles.iterator();
        while (iter.hasNext()) {
            UserRole role = (UserRole) iter.next();
            if (role.getRole().equals(roleName)) {
                this.strategy.remove(role);
                iter.remove();
            }
        }
    }
    
    public Weblog getWebsite(String id) throws WebloggerException {
        return (Weblog) this.strategy.load(Weblog.class, id);
    }
    
    public Weblog getWebsiteByHandle(String handle) throws WebloggerException {
        return getWebsiteByHandle(handle, Boolean.TRUE);
    }
    
    /**
     * Return website specified by handle.
     */
    public Weblog getWebsiteByHandle(String handle, Boolean enabled)
    throws WebloggerException {
        
        if (handle==null )
            throw new WebloggerException("Handle cannot be null");
        
        // check cache first
        // NOTE: if we ever allow changing handles then this needs updating
        if(this.weblogHandleToIdMap.containsKey(handle)) {
            
            Weblog weblog = this.getWebsite(
                    (String) this.weblogHandleToIdMap.get(handle));
            if(weblog != null) {
                // only return weblog if enabled status matches
                if(enabled == null || enabled.equals(weblog.getEnabled())) {
                    log.debug("weblogHandleToId CACHE HIT - "+handle);
                    return weblog;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.weblogHandleToIdMap.remove(handle);
            }
        }
        
        Query query = strategy.getNamedQuery("Weblog.getByHandle");
        query.setParameter(1, handle);
        Weblog website = null;
        try {
            website = (Weblog)query.getSingleResult();
        } catch (NoResultException e) {
            website = null;
        }
        
        // add mapping to cache
        if(website != null) {
            log.debug("weblogHandleToId CACHE MISS - "+handle);
            this.weblogHandleToIdMap.put(website.getHandle(), website.getId());
        }
        
        if(website != null &&
                (enabled == null || enabled.equals(website.getEnabled()))) {
            return website;
        } else {
            return null;
        }
    }
    
    /**
     * Get websites of a user
     */
    public List getWebsites(
            User user, Boolean enabled, Boolean active,
            Date startDate, Date endDate, int offset, int length) throws WebloggerException {
        
        //if (endDate == null) endDate = new Date();
                      
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        StringBuffer whereClause = new StringBuffer();
        
        //queryString.append("SELECT w FROM Weblog w WHERE ");
        if (user == null) { // OpenJPA likes JOINs
            queryString.append("SELECT w FROM Weblog w WHERE ");
        } else {
            queryString.append("SELECT w FROM Weblog w JOIN w.permissions p WHERE ");
            params.add(size++, user);
            whereClause.append(" p.user = ?" + size);
            params.add(size++, Boolean.FALSE);
            whereClause.append(" AND p.pending = ?" + size);
        }
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            if (whereClause.length() > 0) whereClause.append(" AND ");
            params.add(size++, start);
            whereClause.append(" w.dateCreated > ?" + size);
        }
        if (endDate != null) {
            Timestamp end = new Timestamp(endDate.getTime());
            if (whereClause.length() > 0) whereClause.append(" AND ");
            params.add(size++, end);
            whereClause.append(" w.dateCreated < ?" + size);
        }
        if (enabled != null) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            params.add(size++, enabled);
            whereClause.append(" w.enabled = ?" + size);
        }
        if (active != null) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            params.add(size++, active);
            whereClause.append(" w.active = ?" + size);
        }      
        /*if (user != null) { // Toplink likes sub-selects    
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append(" EXISTS (SELECT p from WeblogPermission p WHERE p.website = w ");
            params.add(size++, user);         
            whereClause.append("    AND p.user = ?" + size);
            params.add(size++, Boolean.FALSE);
            whereClause.append("    AND p.pending = ?" + size + ")");   
        }*/
                
        whereClause.append(" ORDER BY w.dateCreated DESC");
        
        Query query = strategy.getDynamicQuery(queryString.toString() + whereClause.toString());
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        for (int i=0; i<params.size(); i++) {
           query.setParameter(i+1, params.get(i));
        }
        
        return query.getResultList();
    }
    
    public User getUser(String id) throws WebloggerException {
        return (User)this.strategy.load(User.class, id);
    }
    
    public User getUserByUserName(String userName) throws WebloggerException {
        return getUserByUserName(userName, Boolean.TRUE);
    }
    
    public User getUserByUserName(String userName, Boolean enabled)
    throws WebloggerException {
        
        if (userName==null )
            throw new WebloggerException("userName cannot be null");
        
        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {
            
            User user = this.getUser(
                    (String) this.userNameToIdMap.get(userName));
            if(user != null) {
                // only return the user if the enabled status matches
                if(enabled == null || enabled.equals(user.getEnabled())) {
                    log.debug("userNameToIdMap CACHE HIT - "+userName);
                    return user;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.userNameToIdMap.remove(userName);
            }
        }
        
        // cache failed, do lookup
        Query query;
        Object[] params;
        if (enabled != null) {
            query = strategy.getNamedQuery(
                    "User.getByUserName&Enabled");
            params = new Object[] {userName, enabled};
        } else {
            query = strategy.getNamedQuery(
                    "User.getByUserName");
            params = new Object[] {userName};
        }
        for (int i=0; i<params.length; i++) {
            query.setParameter(i+1, params[i]);
        }
        User user = null;
        try {
            user = (User)query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }
        
        // add mapping to cache
        if(user != null) {
            log.debug("userNameToIdMap CACHE MISS - "+userName);
            this.userNameToIdMap.put(user.getUserName(), user.getId());
        }
        
        return user;
    }
    
    public List getUsers(Weblog weblog, Boolean enabled, Date startDate,
            Date endDate, int offset, int length)
            throws WebloggerException {
        Query query = null;
        List results = null;
        
        // if we are doing date range then we must have an end date
        if (startDate != null && endDate == null) {
            endDate = new Date();
        }

        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        StringBuffer whereClause = new StringBuffer();
                            
        if (weblog != null) {
            queryString.append("SELECT u FROM User u JOIN u.permissions p ");
            params.add(size++, weblog);
            whereClause.append(" WHERE p.website = ?" + size);   
        } else {
            queryString.append("SELECT u FROM User u ");
        }         

        if (enabled != null) {
            if (whereClause.length() == 0) {
                whereClause.append(" WHERE ");
            } else {
                whereClause.append(" AND ");
            }
            params.add(size++, enabled);
            whereClause.append("u.enabled = ?" + size);  
        }

        if (startDate != null) {
            if (whereClause.length() == 0) {
                whereClause.append(" WHERE ");
            } else {
                whereClause.append(" AND ");
            }

            // if we are doing date range then we must have an end date
            if(endDate == null) {
                endDate = new Date();
            }
            Timestamp start = new Timestamp(startDate.getTime());
            Timestamp end = new Timestamp(endDate.getTime());
            params.add(size++, start);
            whereClause.append("u.dateCreated > ?" + size);
            params.add(size++, end);
            whereClause.append(" AND u.dateCreated < ?" + size);
        }
        whereClause.append(" ORDER BY u.dateCreated DESC");
        query = strategy.getDynamicQuery(queryString.toString() + whereClause.toString());

        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        for (int i=0; i<params.size(); i++) {
           query.setParameter(i+1, params.get(i));
        }
        return query.getResultList();
    }
    
    public List getUsers(int offset, int length) throws WebloggerException {
        return getUsers(Boolean.TRUE, null, null, offset, length);
    }
    
    public List getUsers(Boolean enabled, Date startDate, Date endDate,
            int offset, int length)
            throws WebloggerException {
        Query query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;
        
        if (endDate == null) endDate = new Date();
        
        if (enabled != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "User.getByEnabled&EndDate&StartDateOrderByStartDateDesc");
                query.setParameter(1, enabled);
                query.setParameter(2, end);
                query.setParameter(3, start);
            } else {
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "User.getByEnabled&EndDateOrderByStartDateDesc");
                query.setParameter(1, enabled);
                query.setParameter(2, end);
            }
        } else {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "User.getByEndDate&StartDateOrderByStartDateDesc");
                query.setParameter(1, end);
                query.setParameter(2, start);
            } else {
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "User.getByEndDateOrderByStartDateDesc");
                query.setParameter(1, end);
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }
    
    /**
     * Get users of a website
     */
    public List getUsers(Weblog website, Boolean enabled, int offset, int length) throws WebloggerException {
        Query query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;
        
        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }
        
        if (enabled != null) {
            if (website != null) {
                query = strategy.getNamedQuery("User.getByEnabled&Permissions.website");
                query.setParameter(1, enabled);
                query.setParameter(2, website);
            } else {
                query = strategy.getNamedQuery("User.getByEnabled");
                query.setParameter(1, enabled);
            }
        } else {
            if (website != null) {
                query = strategy.getNamedQuery("User.getByPermissions.website");
                query.setParameter(1, website);
            } else {
                query = strategy.getNamedQuery("User.getAll");
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }
    
    public List getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws WebloggerException {
        Query query = null;
        
        if (enabled != null) {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "User.getByEnabled&UserNameOrEmailAddressStartsWith");
                query.setParameter(1, enabled);
                query.setParameter(2, startsWith + '%');
                query.setParameter(3, startsWith + '%');
            } else {
                query = strategy.getNamedQuery(
                        "User.getByEnabled");
                query.setParameter(1, enabled);
            }
        } else {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "User.getByUserNameOrEmailAddressStartsWith");
                query.setParameter(1, startsWith +  '%');
            } else {
                query = strategy.getNamedQuery("User.getAll");
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }
    
    public WeblogTemplate getPage(String id) throws WebloggerException {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) return null;
        
        return (WeblogTemplate)this.strategy.load(WeblogTemplate.class,id);
    }
    
    /**
     * Use JPA directly because Weblogger's Query API does too much allocation.
     */
    public WeblogTemplate getPageByLink(Weblog website, String pagelink)
    throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("userName is null");
        
        if (pagelink == null)
            throw new WebloggerException("Pagelink is null");
        
        Query query = strategy.getNamedQuery("WeblogTemplate.getByWebsite&Link");
        query.setParameter(1, website);
        query.setParameter(2, pagelink);
        try {
            return (WeblogTemplate)query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#getPageByAction(Weblog, java.lang.String)
     */
    public WeblogTemplate getPageByAction(Weblog website, String action)
            throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("website is null");
        
        if (action == null)
            throw new WebloggerException("Action name is null");
        
        
        Query query = strategy.getNamedQuery("WeblogTemplate.getByAction"); 
        query.setParameter(1, website);
        query.setParameter(2, action);
        try {
            return (WeblogTemplate)query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }        
    }
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#getPageByName(Weblog, java.lang.String)
     */
    public WeblogTemplate getPageByName(Weblog website, String pagename)
    throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("website is null");
        
        if (pagename == null)
            throw new WebloggerException("Page name is null");
        
        Query query = strategy.getNamedQuery("WeblogTemplate.getByWebsite&Name");
        query.setParameter(1, website);
        query.setParameter(2, pagename);
        try {
            return (WeblogTemplate)query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#getPages(Weblog)
     */
    public List getPages(Weblog website) throws WebloggerException {
        if (website == null)
            throw new WebloggerException("website is null");
        Query q = strategy.getNamedQuery(
                "WeblogTemplate.getByWebsiteOrderByName");
        q.setParameter(1, website);
        return q.getResultList();
    }
    
    public WeblogPermission getPermissions(String inviteId)
    throws WebloggerException {
        return (WeblogPermission)this.strategy.load(
                WeblogPermission.class, inviteId);
    }
    
    /**
     * Return permissions for specified user in website
     */
    public WeblogPermission getPermissions(
            Weblog website, User user) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogPermission.getByWebsiteAndUser");
        q.setParameter(1, website);
        q.setParameter(2, user);
        try {
            return (WeblogPermission)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Get pending permissions for user
     */
    public List getPendingPermissions(User user) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogPermission.getByUserAndPending");
        q.setParameter(1, user);
        q.setParameter(2, Boolean.TRUE);
        return q.getResultList();
    }
    
    /**
     * Get pending permissions for website
     */
    public List getPendingPermissions(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogPermission.getByWebsiteAndPending");
        q.setParameter(1, website);
        q.setParameter(2, Boolean.TRUE);
        return q.getResultList();
    }
    
    /**
     * Get all permissions of a website (pendings not including)
     */
    public List getAllPermissions(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogPermission.getByWebsiteAndPending");
        q.setParameter(1, website);
        q.setParameter(2, Boolean.FALSE);
        return q.getResultList();
    }
    
    /**
     * Get all permissions of a user.
     */
    public List getAllPermissions(User user) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogPermission.getByUserAndPending");
        q.setParameter(1, user);
        q.setParameter(2, Boolean.FALSE);
        return q.getResultList();        
    }
    
    public void release() {}
    
    public Map getUserNameLetterMap() throws WebloggerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map results = new TreeMap();
        Query query = strategy.getNamedQuery(
                "User.getCountByUserNameLike");
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            query.setParameter(1, currentChar + "%");
            List row = query.getResultList();
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }
    
    public List getUsersByLetter(char letter, int offset, int length)
    throws WebloggerException {
        Query query = strategy.getNamedQuery(
                "User.getByUserNameOrderByUserName");
        query.setParameter(1, letter + "%");
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }
    
    public Map getWeblogHandleLetterMap() throws WebloggerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map results = new TreeMap();
        Query query = strategy.getNamedQuery(
                "Weblog.getCountByHandleLike");
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            query.setParameter(1, currentChar + "%");
            List row = query.getResultList();
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }
    
    public List getWeblogsByLetter(char letter, int offset, int length)
    throws WebloggerException {
        Query query = strategy.getNamedQuery(
                "Weblog.getByLetterOrderByHandle");
        query.setParameter(1, letter + "%");
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }
    
    public List getMostCommentedWebsites(Date startDate, Date endDate,
            int offset, int length)
            throws WebloggerException {
        
        Query query = null;
        
        if (endDate == null) endDate = new Date();
        
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWebsiteByEndDate&StartDate");
            query.setParameter(1, end);
            query.setParameter(2, start);
        } else {
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWebsiteByEndDate");
            query.setParameter(1, end);
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        List queryResults = query.getResultList();
        List results = new ArrayList();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            results.add(new StatCount(
                    (String)row[1],                     // website id
                    (String)row[2],                     // website handle
                    (String)row[3],                     // website name
                    "statCount.weblogCommentCountType", // stat type
                    ((Long)row[0]).longValue())); // # comments
        }
        // Original query ordered by desc # comments.
        // JPA QL doesn't allow queries to be ordered by agregates; do it in memory
        Collections.sort(results, statCountCountReverseComparator);
        
        return results;
    }
    
    /**
     * Get count of weblogs, active and inactive
     */
    public long getWeblogCount() throws WebloggerException {
        long ret = 0;
        List results = strategy.getNamedQuery(
                "Weblog.getCountAllDistinct").getResultList();
        
        ret = ((Long)results.get(0)).longValue();
        
        return ret;
    }
    
    
    /**
     * Get count of users, enabled only
     */
    public long getUserCount() throws WebloggerException {
        long ret = 0;
        Query q = strategy.getNamedQuery("User.getCountEnabledDistinct");
        q.setParameter(1, Boolean.TRUE);
        List results = q.getResultList();
        ret =((Long)results.get(0)).longValue(); 
        
        return ret;
    }
    
	public User getUserByActivationCode(String activationCode) throws WebloggerException {
		if (activationCode == null)
			throw new WebloggerException("activationcode is null");
        Query q = strategy.getNamedQuery("User.getUserByActivationCode");
        q.setParameter(1, activationCode);
        try {
            return (User)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }		
	}    
}

