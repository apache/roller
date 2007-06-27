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

package org.apache.roller.weblogger.business.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

import org.apache.roller.weblogger.pojos.StatCount;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.SimpleExpression;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.PingQueueEntry;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.UserRole;
import org.hibernate.Query;


/**
 * Hibernate implementation of the UserManager.
 */    
@com.google.inject.Singleton
public class HibernateUserManagerImpl implements UserManager {
    
    static final long serialVersionUID = -5128460637997081121L;
    
    private static Log log = LogFactory.getLog(HibernateUserManagerImpl.class);
    
    private final Weblogger roller;
    private final HibernatePersistenceStrategy strategy;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable();
    
    // cached mapping of userNames -> userIds
    private Map userNameToIdMap = new Hashtable();
   
    
    @com.google.inject.Inject
    protected HibernateUserManagerImpl(Weblogger roller, HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate User Manager");
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
    throws HibernateException, WebloggerException {
        
        Session session = this.strategy.getSession();
        
        BookmarkManager bmgr = roller.getBookmarkManager();
        WeblogManager wmgr = roller.getWeblogManager();
        
        // remove tags
        Criteria tagQuery = session.createCriteria(WeblogEntryTag.class)
            .add(Expression.eq("weblog.id", website.getId()));
        for(Iterator iter = tagQuery.list().iterator(); iter.hasNext();) {
            WeblogEntryTag tagData = (WeblogEntryTag) iter.next();
            this.strategy.remove(tagData);
        }
        
        // remove site tag aggregates
        List tags = wmgr.getTags(website, null, null, -1);
        for(Iterator iter = tags.iterator(); iter.hasNext();) {
            TagStat stat = (TagStat) iter.next();
            Query query = session.createQuery("update WeblogEntryTagAggregate set total = total - ? where name = ? and weblog is null");
            query.setParameter(0, new Integer(stat.getCount()));
            query.setParameter(1, stat.getName());
            query.executeUpdate();
        }
        
        // delete all weblog tag aggregates
        session.createQuery("delete from WeblogEntryTagAggregate where weblog = ?")
            .setParameter(0, website).executeUpdate();
        
        // delete all bad counts
        session.createQuery("delete from WeblogEntryTagAggregate where total <= 0").executeUpdate();       
                
        // Remove the website's ping queue entries
        Criteria criteria = session.createCriteria(PingQueueEntry.class);
        criteria.add(Expression.eq("website", website));
        List queueEntries = criteria.list();
        
        // Remove the website's auto ping configurations
        AutoPingManager autoPingMgr = roller.getAutopingManager();
        List autopings = autoPingMgr.getAutoPingsByWebsite(website);
        Iterator it = autopings.iterator();
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
        
        // remove entries
        Criteria entryQuery = session.createCriteria(WeblogEntry.class);
        entryQuery.add(Expression.eq("website", website));
        List entries = entryQuery.list();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            wmgr.removeWeblogEntry((WeblogEntry) iter.next());
        }
        
        // remove associated referers
        Criteria refererQuery = session.createCriteria(WeblogReferrer.class);
        refererQuery.add(Expression.eq("website", website));
        List referers = refererQuery.list();
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            WeblogReferrer referer = (WeblogReferrer) iter.next();
            this.strategy.remove(referer);
        }
        
        
        // remove associated pages
        Criteria pageQuery = session.createCriteria(WeblogTemplate.class);
        pageQuery.add(Expression.eq("website", website));
        List pages = pageQuery.list();
        for (Iterator iter = pages.iterator(); iter.hasNext();) {
            this.removePage((WeblogTemplate) iter.next());
        }
        
        // remove folders (including bookmarks)
        WeblogBookmarkFolder rootFolder = bmgr.getRootFolder(website);
        if (null != rootFolder) {
            this.strategy.remove(rootFolder);
        }
        
        // remove categories
        WeblogCategory rootCat = website.getDefaultCategory();
        if (null != rootCat) {
            this.strategy.remove(rootCat);
        }
        
        // remove permissions
        List permissions = this.getAllPermissions(website);
        for (Iterator iter = permissions.iterator(); iter.hasNext(); ) {
            this.removePermissions((WeblogPermission) iter.next());
        }
    }
        
    public void saveUser(User data) throws WebloggerException {
        this.strategy.store(data);
    }
        
    public void removeUser(User user) throws WebloggerException {
        this.strategy.remove(user);
        
        // remove entry from cache mapping
        this.userNameToIdMap.remove(user.getUserName());
    }
        
    public void savePermissions(WeblogPermission perms) throws WebloggerException {
        this.strategy.store(perms);
    }
        
    public void removePermissions(WeblogPermission perms) throws WebloggerException {
        
        // make sure associations are broken
        perms.getWebsite().getPermissions().remove(perms);
        perms.getUser().getPermissions().remove(perms);
        
        this.strategy.remove(perms);
    }
        
    /**
     * @see org.apache.roller.weblogger.model.UserManager#storePage(org.apache.roller.weblogger.pojos.WeblogTemplate)
     */
    public void savePage(WeblogTemplate page) throws WebloggerException {
        this.strategy.store(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getUserManager().saveWebsite(page.getWebsite());
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
        }
        
        if(getUserByUserName(newUser.getUserName()) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new WebloggerException("error.add.user.userNameInUse");
        }
        
        newUser.grantRole("editor");
        if(adminUser) {
            newUser.grantRole("admin");
            
            //if user was disabled (because of activation user account with e-mail property), 
            //enable it for admin user
            newUser.setEnabled(Boolean.TRUE);
            newUser.setActivationCode(null);
        }
        
        this.strategy.store(newUser);
    }
        
    public void addWebsite(Weblog newWeblog) throws WebloggerException {
        
        this.strategy.store(newWeblog);
        this.addWeblogContents(newWeblog);
    }
        
    private void addWeblogContents(Weblog newWeblog) throws WebloggerException {
        
        UserManager umgr = roller.getUserManager();
        WeblogManager wmgr = roller.getWeblogManager();
        
        // grant weblog creator ADMIN permissions
        WeblogPermission perms = new WeblogPermission();
        perms.setUser(newWeblog.getCreator());
        perms.setWebsite(newWeblog);
        perms.setPending(false);
        perms.setPermissionMask(WeblogPermission.ADMIN);
        this.strategy.store(perms);
        
        // add default category
        WeblogCategory rootCat = new WeblogCategory(
                newWeblog, // newWeblog
                null,      // parent
                "root",    // name
                "root",    // description
                null );    // image
        this.strategy.store(rootCat);
        
        String cats = RollerConfig.getProperty("newuser.categories");
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
        String blogroll = RollerConfig.getProperty("newuser.blogroll");
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
        PingTargetManager pingTargetMgr = roller.getPingTargetManager();
        AutoPingManager autoPingMgr = roller.getAutopingManager();
        
        Iterator pingTargets = pingTargetMgr.getCommonPingTargets().iterator();
        PingTarget pingTarget = null;
        while(pingTargets.hasNext()) {
            pingTarget = (PingTarget) pingTargets.next();
            
            if(pingTarget.isAutoEnabled()) {
                AutoPing autoPing = new AutoPing(null, pingTarget, newWeblog);
                autoPingMgr.saveAutoPing(autoPing);
            }
        }
    }
        
    /**
     * Creates and stores a pending PermissionsData for user and website specified.
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
        this.strategy.store(perms);
        
        // manage associations
        website.getPermissions().add(perms);
        user.getPermissions().add(perms);
        
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
        
    public Weblog getWebsite(String id) throws WebloggerException {
        return (Weblog) this.strategy.load(id,Weblog.class);
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
            
            Weblog weblog = this.getWebsite((String) this.weblogHandleToIdMap.get(handle));
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
        
        // cache failed, do lookup
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(Weblog.class);
            criteria.add(new IgnoreCaseEqExpression("handle", handle));
            
            Weblog website = (Weblog) criteria.uniqueResult();
            
            // add mapping to cache
            if(website != null) {
                log.debug("weblogHandleToId CACHE MISS - "+handle);
                this.weblogHandleToIdMap.put(website.getHandle(), website.getId());
            }
            
            // enforce check against enabled status
            if(website != null && 
                    (enabled == null || enabled.equals(website.getEnabled()))) {
                return website;
            } else {
                return null;
            }
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * Get websites of a user
     */
    public List getWebsites(User user, Boolean enabled, Boolean active, 
                            Date startDate, Date endDate, int offset, int length)  
            throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(Weblog.class);
            
            if (user != null) {
                criteria.createAlias("permissions","permissions");
                criteria.add(Expression.eq("permissions.user", user));
                criteria.add(Expression.eq("permissions.pending", Boolean.FALSE));
            }
            if (startDate != null) {
                criteria.add(Expression.gt("dateCreated", startDate));
            }
            if (endDate != null) {
                criteria.add(Expression.lt("dateCreated", endDate));
            }
            if (enabled != null) {
                criteria.add(Expression.eq("enabled", enabled));
            }
            if (active != null) {
                criteria.add(Expression.eq("active", active));
            }
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            criteria.addOrder(Order.desc("dateCreated"));
            
            return criteria.list();
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    public User getUser(String id) throws WebloggerException {
        return (User)this.strategy.load(id,User.class);
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
            
            User user = this.getUser((String) this.userNameToIdMap.get(userName));
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
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            
            if (enabled != null) {
                criteria.add(
                        Expression.conjunction()
                        .add(Expression.eq("userName", userName))
                        .add(Expression.eq("enabled", enabled)));
            } else {
                criteria.add(
                        Expression.conjunction()
                        .add(Expression.eq("userName", userName)));
            }
            
            User user = (User) criteria.uniqueResult();
            
            // add mapping to cache
            if(user != null) {
                log.debug("userNameToIdMap CACHE MISS - "+userName);
                this.userNameToIdMap.put(user.getUserName(), user.getId());
            }
            
            return user;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getUsers(Weblog weblog, Boolean enabled, Date startDate, 
                         Date endDate, int offset, int length) 
            throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            
            if (weblog != null) {
                criteria.createAlias("permissions", "permissions");
                criteria.add(Expression.eq("permissions.website", weblog));
            }
            
            if (enabled != null) {
                criteria.add(Expression.eq("enabled", enabled));
            }
            
            if (startDate != null) {
                // if we are doing date range then we must have an end date
                if(endDate == null) {
                    endDate = new Date();
                }
                
                criteria.add(Expression.lt("dateCreated", endDate));
                criteria.add(Expression.gt("dateCreated", startDate));
            }
            
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            
            criteria.addOrder(Order.desc("dateCreated"));
            
            return criteria.list();
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
        
    public List getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws WebloggerException {
        
        List results = new ArrayList();
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            
            if (enabled != null) {
                criteria.add(Expression.eq("enabled", enabled));
            }
            if (startsWith != null) {
                criteria.add(Expression.disjunction()
                .add(Expression.like("userName", startsWith, MatchMode.START))
                .add(Expression.like("emailAddress", startsWith, MatchMode.START)));
            }
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            results = criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
        return results;
    }
    
    public WeblogTemplate getPage(String id) throws WebloggerException {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) return null;
        
        return (WeblogTemplate)this.strategy.load(id,WeblogTemplate.class);
    }
    
    /**
     * Use Hibernate directly because Weblogger's Query API does too much allocation.
     */
    public WeblogTemplate getPageByLink(Weblog website, String pagelink)
            throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("userName is null");
        
        if (pagelink == null)
            throw new WebloggerException("Pagelink is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website",website));
            criteria.add(Expression.eq("link",pagelink));
            
            return (WeblogTemplate) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#getPageByAction(WebsiteData, java.lang.String)
     */
    public WeblogTemplate getPageByAction(Weblog website, String action)
            throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("website is null");
        
        if (action == null)
            throw new WebloggerException("Action name is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("action", action));
            
            return (WeblogTemplate) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#getPageByName(WebsiteData, java.lang.String)
     */
    public WeblogTemplate getPageByName(Weblog website, String pagename)
            throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("website is null");
        
        if (pagename == null)
            throw new WebloggerException("Page name is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("name", pagename));
            
            return (WeblogTemplate) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * @see org.apache.roller.weblogger.model.UserManager#getPages(WebsiteData)
     */
    public List getPages(Weblog website) throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website",website));
            criteria.addOrder(Order.asc("name"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public WeblogPermission getPermissions(String inviteId) throws WebloggerException {
        return (WeblogPermission)this.strategy.load(inviteId,WeblogPermission.class);
    }
    
    /**
     * Return permissions for specified user in website
     */
    public WeblogPermission getPermissions(
            Weblog website, User user) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogPermission.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("user", user));
            
            List list = criteria.list();
            return list.size()!=0 ? (WeblogPermission)list.get(0) : null;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get pending permissions for user
     */
    public List getPendingPermissions(User user) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogPermission.class);
            criteria.add(Expression.eq("user", user));
            criteria.add(Expression.eq("pending", Boolean.TRUE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get pending permissions for website
     */
    public List getPendingPermissions(Weblog website) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogPermission.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("pending", Boolean.TRUE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get all permissions of a website (pendings not including)
     */
    public List getAllPermissions(Weblog website) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogPermission.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("pending", Boolean.FALSE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get all permissions of a user.
     */
    public List getAllPermissions(User user) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogPermission.class);
            criteria.add(Expression.eq("user", user));
            criteria.add(Expression.eq("pending", Boolean.FALSE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public void release() {}
    
    public Map getUserNameLetterMap() throws WebloggerException {
        // TODO: ATLAS getUserNameLetterMap DONE TESTED
        String msg = "Getting username letter map";
        try {      
            String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Map results = new TreeMap();
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();
            for (int i=0; i<26; i++) {
                Query query = session.createQuery(
                    "select count(user) from User user where upper(user.userName) like '"+lc.charAt(i)+"%'");
                List row = query.list();
                Number count = (Number) row.get(0);
                results.put(new String(new char[]{lc.charAt(i)}), count);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new WebloggerException(msg, pe);
        }
    }
    
    public List getUsersByLetter(char letter, int offset, int length) 
        throws WebloggerException { 
        // TODO: ATLAS getUsersByLetter DONE
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Expression.ilike("userName", new String(new char[]{letter}) + "%", MatchMode.START));
            criteria.addOrder(Order.asc("userName"));
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public Map getWeblogHandleLetterMap() throws WebloggerException {
        // TODO: ATLAS getWeblogHandleLetterMap DONE
        String msg = "Getting weblog letter map";
        try {      
            String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Map results = new TreeMap();
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();
            for (int i=0; i<26; i++) {
                Query query = session.createQuery(
                    "select count(website) from Weblog website where upper(website.handle) like '"+lc.charAt(i)+"%'");
                List row = query.list();
                Number count = (Number)row.get(0);
                results.put(new String(new char[]{lc.charAt(i)}), count);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new WebloggerException(msg, pe);
        }
    }
    
    public List getWeblogsByLetter(char letter, int offset, int length) 
        throws WebloggerException {
        // TODO: ATLAS getWeblogsByLetter DONE
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(Weblog.class);
            criteria.add(Expression.ilike("handle", new String(new char[]{letter}) + "%", MatchMode.START));
            criteria.addOrder(Order.asc("handle"));
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    public List getMostCommentedWebsites(Date startDate, Date endDate, int offset, int length) 
        throws WebloggerException {
        // TODO: ATLAS getMostCommentedWebsites DONE TESTED
        String msg = "Getting most commented websites";
        if (endDate == null) endDate = new Date();
        try {      
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();            
            StringBuffer sb = new StringBuffer();
            sb.append("select count(distinct c), c.weblogEntry.website.id, c.weblogEntry.website.handle, c.weblogEntry.website.name ");
            sb.append("from WeblogEntryComment c where c.weblogEntry.pubTime < :endDate ");
            if (startDate != null) {
                sb.append("and c.weblogEntry.pubTime > :startDate ");
            }  
            sb.append("group by c.weblogEntry.website.id, c.weblogEntry.website.handle, c.weblogEntry.website.name order by col_0_0_ desc");
            Query query = session.createQuery(sb.toString());
            query.setParameter("endDate", endDate);
            if (startDate != null) {
                query.setParameter("startDate", startDate);
            }   
            if (offset != 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            List results = new ArrayList();
            for (Iterator iter = query.list().iterator(); iter.hasNext();) {
                Object[] row = (Object[]) iter.next();
                StatCount statCount = new StatCount(
                    (String)row[1],                     // website id
                    (String)row[2],                     // website handle
                    (String)row[3],                     // website name
                    "statCount.weblogCommentCountType", // stat type 
                    new Long(((Number)row[0]).longValue())); // # comments
                statCount.setWeblogHandle((String)row[2]);
                results.add(statCount);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new WebloggerException(msg, pe);
        }
    }
    
    
    /**
     * Get count of weblogs, active and inactive
     */    
    public long getWeblogCount() throws WebloggerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            String query = "select count(distinct w) from Weblog w";
            List result = session.createQuery(query).list();
            ret = ((Number)result.get(0)).intValue();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        return ret;
    }

    public void revokeRole(String roleName, User user) throws WebloggerException {
        UserRole removeme = null;
        Collection roles = user.getRoles();
        Iterator iter = roles.iterator();
        while (iter.hasNext()) {
            UserRole role = (UserRole) iter.next();
            if (role.getRole().equals(roleName)) {
                iter.remove();
                this.strategy.remove(role);
            }
        }
    }


    
    /**
     * Get count of users, enabled only
     */    
    public long getUserCount() throws WebloggerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            String query = "select count(distinct u) from User u where u.enabled=true";
            List result = session.createQuery(query).list();
            ret = ((Number)result.get(0)).intValue();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        return ret;
    }
    
     
    /** Doesn't seem to be any other way to get ignore case w/o QBE */
    class IgnoreCaseEqExpression extends SimpleExpression {
        public IgnoreCaseEqExpression(String property, Object value) {
            super(property, value, "=", true);
        }
    }


	public User getUserByActivationCode(String activationCode) throws WebloggerException {

		if (activationCode == null)
			throw new WebloggerException("activationcode is null");

		try {
			Session session = ((HibernatePersistenceStrategy) this.strategy)
					.getSession();
			Criteria criteria = session.createCriteria(User.class);

			criteria.add(Expression.eq("activationCode", activationCode));

			User user = (User) criteria.uniqueResult();

			return user;
		} catch (HibernateException e) {
			throw new WebloggerException(e);
		}		
	}
}






