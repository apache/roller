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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingQueueEntry;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogUserPermission;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.SimpleExpression;


/**
 * Hibernate implementation of the WeblogManager.
 */
@com.google.inject.Singleton
public class HibernateWeblogManagerImpl implements WeblogManager {
    
     private static Log log = LogFactory.getLog(HibernateWeblogManagerImpl.class);
    
    private final Weblogger roller;
    private final HibernatePersistenceStrategy strategy;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable(); 
    
    
        
    @com.google.inject.Inject
    protected HibernateWeblogManagerImpl(Weblogger roller, HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate Weblog Manager");
        this.roller = roller;       
        this.strategy = strat;
    }
    
    
    public void release() {}  
    
    
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
        WeblogEntryManager wmgr = roller.getWeblogEntryManager();
        
        // remove tags
        Criteria tagQuery = session.createCriteria(WeblogEntryTag.class)
            .add(Expression.eq("weblog.id", website.getId()));
        for(Iterator iter = tagQuery.list().iterator(); iter.hasNext();) {
            WeblogEntryTag tagData = (WeblogEntryTag) iter.next();
            this.strategy.remove(tagData);
        }
        
        // remove site tag aggregates
        List tags = wmgr.getTags(website, null, null, 0, -1);
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
        List permissions = roller.getUserManager().getAllPermissions(website);
        for (Iterator iter = permissions.iterator(); iter.hasNext(); ) {
            roller.getUserManager().removePermissions((WeblogUserPermission) iter.next());
        }
        
        // remove uploaded files
        FileManager fmgr = WebloggerFactory.getWeblogger().getFileManager();
        fmgr.deleteAllFiles(website);
    }
        
    /**
     * @see org.apache.roller.weblogger.model.UserManager#storePage(org.apache.roller.weblogger.pojos.WeblogTemplate)
     */
    public void savePage(WeblogTemplate page) throws WebloggerException {
        this.strategy.store(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWebsite(page.getWebsite());
    }
        
    public void removePage(WeblogTemplate page) throws WebloggerException {
        this.strategy.remove(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWebsite(page.getWebsite());
    }
        
    public void addWebsite(Weblog newWeblog) throws WebloggerException {
        
        this.strategy.store(newWeblog);
        this.addWeblogContents(newWeblog);
    }
        
    private void addWeblogContents(Weblog newWeblog) throws WebloggerException {
        
        UserManager umgr = roller.getUserManager();
        WeblogEntryManager wmgr = roller.getWeblogEntryManager();
        
        // grant weblog creator ADMIN permissions
        WeblogUserPermission perms = new WeblogUserPermission();
        perms.setUser(newWeblog.getCreator());
        perms.setWebsite(newWeblog);
        perms.setPending(false);
        perms.setPermissionMask(WeblogUserPermission.ADMIN);
        this.strategy.store(perms);
        
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


         
    /** Doesn't seem to be any other way to get ignore case w/o QBE */
    class IgnoreCaseEqExpression extends SimpleExpression {
        public IgnoreCaseEqExpression(String property, Object value) {
            super(property, value, "=", true);
        }
    }
}
