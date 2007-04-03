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

package org.apache.roller.business.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

import org.apache.roller.pojos.StatCount;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.SimpleExpression;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.TagStat;
import org.apache.roller.pojos.WeblogEntryTagData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.PingQueueEntryData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.RoleData;
import org.hibernate.Query;


/**
 * Hibernate implementation of the UserManager.
 */
public class HibernateUserManagerImpl implements UserManager {
    
    static final long serialVersionUID = -5128460637997081121L;    
    private static Log log = LogFactory.getLog(HibernateUserManagerImpl.class);    
    private HibernatePersistenceStrategy strategy = null;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable();
    
    // cached mapping of userNames -> userIds
    private Map userNameToIdMap = new Hashtable();
        
    public HibernateUserManagerImpl(HibernatePersistenceStrategy strat) {
        log.debug("Instantiating Hibernate User Manager");
        
        this.strategy = strat;
    }
    
    /**
     * Update existing website.
     */
    public void saveWebsite(WebsiteData website) throws RollerException {
        
        website.setLastModified(new java.util.Date());
        strategy.store(website);
    }
    
    public void removeWebsite(WebsiteData weblog) throws RollerException {
        
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
    private void removeWebsiteContents(WebsiteData website)
    throws HibernateException, RollerException {
        
        Session session = this.strategy.getSession();
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        
        // remove tags
        Criteria tagQuery = session.createCriteria(WeblogEntryTagData.class)
            .add(Expression.eq("weblog.id", website.getId()));
        for(Iterator iter = tagQuery.list().iterator(); iter.hasNext();) {
            WeblogEntryTagData tagData = (WeblogEntryTagData) iter.next();
            this.strategy.remove(tagData);
        }
        
        // remove site tag aggregates
        List tags = wmgr.getTags(website, null, null, -1);
        for(Iterator iter = tags.iterator(); iter.hasNext();) {
            TagStat stat = (TagStat) iter.next();
            Query query = session.createQuery("update WeblogEntryTagAggregateData set total = total - ? where name = ? and weblog is null");
            query.setParameter(0, new Integer(stat.getCount()));
            query.setParameter(1, stat.getName());
            query.executeUpdate();
        }
        
        // delete all weblog tag aggregates
        session.createQuery("delete from WeblogEntryTagAggregateData where weblog = ?")
            .setParameter(0, website).executeUpdate();
        
        // delete all bad counts
        session.createQuery("delete from WeblogEntryTagAggregateData where total <= 0").executeUpdate();       
                
        // Remove the website's ping queue entries
        Criteria criteria = session.createCriteria(PingQueueEntryData.class);
        criteria.add(Expression.eq("website", website));
        List queueEntries = criteria.list();
        
        // Remove the website's auto ping configurations
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        List autopings = autoPingMgr.getAutoPingsByWebsite(website);
        Iterator it = autopings.iterator();
        while(it.hasNext()) {
            this.strategy.remove((AutoPingData) it.next());
        }
        
        // Remove the website's custom ping targets
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        List pingtargets = pingTargetMgr.getCustomPingTargets(website);
        it = pingtargets.iterator();
        while(it.hasNext()) {
            this.strategy.remove((PingTargetData) it.next());
        }
        
        // remove entries
        Criteria entryQuery = session.createCriteria(WeblogEntryData.class);
        entryQuery.add(Expression.eq("website", website));
        List entries = entryQuery.list();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            wmgr.removeWeblogEntry((WeblogEntryData) iter.next());
        }
        
        // remove associated referers
        Criteria refererQuery = session.createCriteria(RefererData.class);
        refererQuery.add(Expression.eq("website", website));
        List referers = refererQuery.list();
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            RefererData referer = (RefererData) iter.next();
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
        FolderData rootFolder = bmgr.getRootFolder(website);
        if (null != rootFolder) {
            this.strategy.remove(rootFolder);
        }
        
        // remove categories
        WeblogCategoryData rootCat = website.getDefaultCategory();
        if (null != rootCat) {
            this.strategy.remove(rootCat);
        }
        
        // remove permissions
        List permissions = this.getAllPermissions(website);
        for (Iterator iter = permissions.iterator(); iter.hasNext(); ) {
            this.removePermissions((PermissionsData) iter.next());
        }
    }
        
    public void saveUser(UserData data) throws RollerException {
        this.strategy.store(data);
    }
        
    public void removeUser(UserData user) throws RollerException {
        this.strategy.remove(user);
        
        // remove entry from cache mapping
        this.userNameToIdMap.remove(user.getUserName());
    }
        
    public void savePermissions(PermissionsData perms) throws RollerException {
        this.strategy.store(perms);
    }
        
    public void removePermissions(PermissionsData perms) throws RollerException {
        this.strategy.remove(perms);
    }
        
    /**
     * @see org.apache.roller.model.UserManager#storePage(org.apache.roller.pojos.WeblogTemplate)
     */
    public void savePage(WeblogTemplate page) throws RollerException {
        this.strategy.store(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(page.getWebsite());
    }
        
    public void removePage(WeblogTemplate page) throws RollerException {
        this.strategy.remove(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(page.getWebsite());
    }
        
    public void addUser(UserData newUser) throws RollerException {
        
        if(newUser == null)
            throw new RollerException("cannot add null user");
        
        // TODO BACKEND: we must do this in a better fashion, like getUserCnt()?
        boolean adminUser = false;
        List existingUsers = this.getUsers(null, Boolean.TRUE, null, null, 0, 1);
        if(existingUsers.size() == 0) {
            // Make first user an admin
            adminUser = true;
        }
        
        if(getUserByUserName(newUser.getUserName()) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new RollerException("error.add.user.userNameInUse");
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
        
    public void addWebsite(WebsiteData newWeblog) throws RollerException {
        
        this.strategy.store(newWeblog);
        this.addWeblogContents(newWeblog);
    }
        
    private void addWeblogContents(WebsiteData newWeblog) throws RollerException {
        
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        
        // grant weblog creator ADMIN permissions
        PermissionsData perms = new PermissionsData();
        perms.setUser(newWeblog.getCreator());
        perms.setWebsite(newWeblog);
        perms.setPending(false);
        perms.setPermissionMask(PermissionsData.ADMIN);
        this.strategy.store(perms);
        
        // add default category
        WeblogCategoryData rootCat = new WeblogCategoryData(
                newWeblog, // newWeblog
                null,      // parent
                "root",    // name
                "root",    // description
                null );    // image
        this.strategy.store(rootCat);
        
        String cats = RollerConfig.getProperty("newuser.categories");
        WeblogCategoryData firstCat = rootCat;
        if (cats != null && cats.trim().length() > 0) {
            String[] splitcats = cats.split(",");
            for (int i=0; i<splitcats.length; i++) {
                WeblogCategoryData c = new WeblogCategoryData(
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
        FolderData root = new FolderData(
                null, "root", "root", newWeblog);
        this.strategy.store(root);
        
        Integer zero = new Integer(0);
        String blogroll = RollerConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (int i=0; i<splitroll.length; i++) {
                String[] rollitems = splitroll[i].split("\\|");
                if (rollitems != null && rollitems.length > 1) {
                    BookmarkData b = new BookmarkData(
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
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        
        Iterator pingTargets = pingTargetMgr.getCommonPingTargets().iterator();
        PingTargetData pingTarget = null;
        while(pingTargets.hasNext()) {
            pingTarget = (PingTargetData) pingTargets.next();
            
            if(pingTarget.isAutoEnabled()) {
                AutoPingData autoPing = new AutoPingData(null, pingTarget, newWeblog);
                autoPingMgr.saveAutoPing(autoPing);
            }
        }
    }
        
    /**
     * Creates and stores a pending PermissionsData for user and website specified.
     * TODO BACKEND: do we really need this?  can't we just use storePermissions()?
     */
    public PermissionsData inviteUser(WebsiteData website,
            UserData user, short mask) throws RollerException {
        
        if (website == null) throw new RollerException("Website cannot be null");
        if (user == null) throw new RollerException("User cannot be null");
        
        PermissionsData perms = new PermissionsData();
        perms.setWebsite(website);
        perms.setUser(user);
        perms.setPermissionMask(mask);
        this.strategy.store(perms);
        
        return perms;
    }
        
    /**
     * Remove user permissions from a website.
     *
     * TODO: replace this with a domain model method like weblog.retireUser(user)
     */
    public void retireUser(WebsiteData website, UserData user) throws RollerException {
        
        if (website == null) throw new RollerException("Website cannot be null");
        if (user == null) throw new RollerException("User cannot be null");
        
        Iterator perms = website.getPermissions().iterator();
        PermissionsData target = null;
        while (perms.hasNext()) {
            PermissionsData pd = (PermissionsData)perms.next();
            if (pd.getUser().getId().equals(user.getId())) {
                target = pd;
                break;
            }
        }
        if (target == null) throw new RollerException("User not member of website");
        
        website.removePermission(target);
        this.strategy.remove(target);
    }
        
    public WebsiteData getWebsite(String id) throws RollerException {
        return (WebsiteData) this.strategy.load(id,WebsiteData.class);
    }
        
    public WebsiteData getWebsiteByHandle(String handle) throws RollerException {
        return getWebsiteByHandle(handle, Boolean.TRUE);
    }
        
    /**
     * Return website specified by handle.
     */
    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled)
    throws RollerException {
        
        if (handle==null )
            throw new RollerException("Handle cannot be null");
        
        // check cache first
        // NOTE: if we ever allow changing handles then this needs updating
        if(this.weblogHandleToIdMap.containsKey(handle)) {
            
            WebsiteData weblog = this.getWebsite((String) this.weblogHandleToIdMap.get(handle));
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
            Criteria criteria = session.createCriteria(WebsiteData.class);
            criteria.add(new IgnoreCaseEqExpression("handle", handle));
            
            WebsiteData website = (WebsiteData) criteria.uniqueResult();
            
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
            throw new RollerException(e);
        }
    }
        
    /**
     * Get websites of a user
     */
    public List getWebsites(UserData user, Boolean enabled, Boolean active, 
                            Date startDate, Date endDate, int offset, int length)  
            throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WebsiteData.class);
            
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
            throw new RollerException(e);
        }
    }
        
    public UserData getUser(String id) throws RollerException {
        return (UserData)this.strategy.load(id,UserData.class);
    }
        
    public UserData getUserByUserName(String userName) throws RollerException {
        return getUserByUserName(userName, Boolean.TRUE);
    }
    
    public UserData getUserByUserName(String userName, Boolean enabled)
    throws RollerException {
        
        if (userName==null )
            throw new RollerException("userName cannot be null");
        
        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {
            
            UserData user = this.getUser((String) this.userNameToIdMap.get(userName));
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
            Criteria criteria = session.createCriteria(UserData.class);
            
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
            
            UserData user = (UserData) criteria.uniqueResult();
            
            // add mapping to cache
            if(user != null) {
                log.debug("userNameToIdMap CACHE MISS - "+userName);
                this.userNameToIdMap.put(user.getUserName(), user.getId());
            }
            
            return user;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public List getUsers(WebsiteData weblog, Boolean enabled, Date startDate, 
                         Date endDate, int offset, int length) 
            throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(UserData.class);
            
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
            throw new RollerException(e);
        }
    }
    
        
    public List getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws RollerException {
        
        List results = new ArrayList();
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(UserData.class);
            
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
            throw new RollerException(e);
        }
        return results;
    }
    
    public WeblogTemplate getPage(String id) throws RollerException {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) return null;
        
        return (WeblogTemplate)this.strategy.load(id,WeblogTemplate.class);
    }
    
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    public WeblogTemplate getPageByLink(WebsiteData website, String pagelink)
            throws RollerException {
        
        if (website == null)
            throw new RollerException("userName is null");
        
        if (pagelink == null)
            throw new RollerException("Pagelink is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website",website));
            criteria.add(Expression.eq("link",pagelink));
            
            return (WeblogTemplate) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    /**
     * @see org.apache.roller.model.UserManager#getPageByAction(WebsiteData, java.lang.String)
     */
    public WeblogTemplate getPageByAction(WebsiteData website, String action)
            throws RollerException {
        
        if (website == null)
            throw new RollerException("website is null");
        
        if (action == null)
            throw new RollerException("Action name is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("action", action));
            
            return (WeblogTemplate) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    /**
     * @see org.apache.roller.model.UserManager#getPageByName(WebsiteData, java.lang.String)
     */
    public WeblogTemplate getPageByName(WebsiteData website, String pagename)
            throws RollerException {
        
        if (website == null)
            throw new RollerException("website is null");
        
        if (pagename == null)
            throw new RollerException("Page name is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("name", pagename));
            
            return (WeblogTemplate) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * @see org.apache.roller.model.UserManager#getPages(WebsiteData)
     */
    public List getPages(WebsiteData website) throws RollerException {
        
        if (website == null)
            throw new RollerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogTemplate.class);
            criteria.add(Expression.eq("website",website));
            criteria.addOrder(Order.asc("name"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public PermissionsData getPermissions(String inviteId) throws RollerException {
        return (PermissionsData)this.strategy.load(inviteId, PermissionsData.class);
    }
    
    /**
     * Return permissions for specified user in website
     */
    public PermissionsData getPermissions(
            WebsiteData website, UserData user) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(PermissionsData.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("user", user));
            
            List list = criteria.list();
            return list.size()!=0 ? (PermissionsData)list.get(0) : null;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * Get pending permissions for user
     */
    public List getPendingPermissions(UserData user) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(PermissionsData.class);
            criteria.add(Expression.eq("user", user));
            criteria.add(Expression.eq("pending", Boolean.TRUE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * Get pending permissions for website
     */
    public List getPendingPermissions(WebsiteData website) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(PermissionsData.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("pending", Boolean.TRUE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * Get all permissions of a website (pendings not including)
     */
    public List getAllPermissions(WebsiteData website) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(PermissionsData.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("pending", Boolean.FALSE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * Get all permissions of a user.
     */
    public List getAllPermissions(UserData user) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(PermissionsData.class);
            criteria.add(Expression.eq("user", user));
            criteria.add(Expression.eq("pending", Boolean.FALSE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public void release() {}
    
    public Map getUserNameLetterMap() throws RollerException {
        // TODO: ATLAS getUserNameLetterMap DONE TESTED
        String msg = "Getting username letter map";
        try {      
            String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Map results = new TreeMap();
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();
            for (int i=0; i<26; i++) {
                Query query = session.createQuery(
                    "select count(user) from UserData user where upper(user.userName) like '"+lc.charAt(i)+"%'");
                List row = query.list();
                Integer count = (Integer)row.get(0);
                results.put(new String(new char[]{lc.charAt(i)}), count);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new RollerException(msg, pe);
        }
    }
    
    public List getUsersByLetter(char letter, int offset, int length) 
        throws RollerException { 
        // TODO: ATLAS getUsersByLetter DONE
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(UserData.class);
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
            throw new RollerException(e);
        }
    }
    
    public Map getWeblogHandleLetterMap() throws RollerException {
        // TODO: ATLAS getWeblogHandleLetterMap DONE
        String msg = "Getting weblog letter map";
        try {      
            String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Map results = new TreeMap();
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();
            for (int i=0; i<26; i++) {
                Query query = session.createQuery(
                    "select count(website) from WebsiteData website where upper(website.handle) like '"+lc.charAt(i)+"%'");
                List row = query.list();
                Integer count = (Integer)row.get(0);
                results.put(new String(new char[]{lc.charAt(i)}), count);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new RollerException(msg, pe);
        }
    }
    
    public List getWeblogsByLetter(char letter, int offset, int length) 
        throws RollerException {
        // TODO: ATLAS getWeblogsByLetter DONE
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WebsiteData.class);
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
            throw new RollerException(e);
        }
    }
        
    public List getMostCommentedWebsites(Date startDate, Date endDate, int offset, int length) 
        throws RollerException {
        // TODO: ATLAS getMostCommentedWebsites DONE TESTED
        String msg = "Getting most commented websites";
        if (endDate == null) endDate = new Date();
        try {      
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();            
            StringBuffer sb = new StringBuffer();
            sb.append("select count(distinct c), c.weblogEntry.website.id, c.weblogEntry.website.handle, c.weblogEntry.website.name ");
            sb.append("from CommentData c where c.weblogEntry.pubTime < :endDate ");
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
                    new Long(((Integer)row[0]).intValue()).longValue()); // # comments
                statCount.setWeblogHandle((String)row[2]);
                results.add(statCount);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new RollerException(msg, pe);
        }
    }
    
    
    /**
     * Get count of weblogs, active and inactive
     */    
    public long getWeblogCount() throws RollerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            String query = "select count(distinct w) from WebsiteData w";
            List result = session.createQuery(query).list();
            ret = ((Integer)result.get(0)).intValue();
        } catch (Exception e) {
            throw new RollerException(e);
        }
        return ret;
    }

    public void revokeRole(String roleName, UserData user) throws RollerException {
        RoleData removeme = null;
        Collection roles = user.getRoles();
        Iterator iter = roles.iterator();
        while (iter.hasNext()) {
            RoleData role = (RoleData) iter.next();
            if (role.getRole().equals(roleName)) {
                iter.remove();
                this.strategy.remove(role);
            }
        }
    }


    
    /**
     * Get count of users, enabled only
     */    
    public long getUserCount() throws RollerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            String query = "select count(distinct u) from UserData u where u.enabled=true";
            List result = session.createQuery(query).list();
            ret = ((Integer)result.get(0)).intValue();
        } catch (Exception e) {
            throw new RollerException(e);
        }
        return ret;
    }
    
     
    /** Doesn't seem to be any other way to get ignore case w/o QBE */
    class IgnoreCaseEqExpression extends SimpleExpression {
        public IgnoreCaseEqExpression(String property, Object value) {
            super(property, value, "=", true);
        }
    }


	public UserData getUserByActivationCode(String activationCode) throws RollerException {

		if (activationCode == null)
			throw new RollerException("activationcode is null");

		try {
			Session session = ((HibernatePersistenceStrategy) this.strategy)
					.getSession();
			Criteria criteria = session.createCriteria(UserData.class);

			criteria.add(Expression.eq("activationCode", activationCode));

			UserData user = (UserData) criteria.uniqueResult();

			return user;
		} catch (HibernateException e) {
			throw new RollerException(e);
		}		
	}
}






