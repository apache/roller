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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogHitCount;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.TagStatComparator;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntryAttribute;
import org.apache.roller.weblogger.util.DateUtil;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate implementation of the WeblogManager.
 */
public class HibernateWeblogManagerImpl implements WeblogManager {
    
    static final long serialVersionUID = -3730860865389981439L;
    
    private static Log log = LogFactory.getLog(HibernateWeblogManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    // cached mapping of entryAnchors -> entryIds
    private Hashtable entryAnchorToIdMap = new Hashtable();
    
    /* inline creation of reverse comparator, anonymous inner class */
    private Comparator reverseComparator = new ReverseComparator();
    
    private Comparator tagStatComparator = new TagStatComparator();
    
    public HibernateWeblogManagerImpl(HibernatePersistenceStrategy strat) {
        log.debug("Instantiating Hibernate Weblog Manager");
        
        this.strategy = strat;
    }
    
    
    public void saveWeblogCategory(WeblogCategory cat) throws WebloggerException {
        
        if(cat == null) {
            throw new IllegalArgumentException("Category is null");
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());
        
        this.strategy.store(cat);
    }
    
    
    public void removeWeblogCategory(WeblogCategory cat) throws WebloggerException {
        
        if(cat.retrieveWeblogEntries(true).size() > 0) {
            throw new WebloggerException("Cannot remove category with entries");
        }
        
        // remove cat
        this.strategy.remove(cat);
        
        // update website default cats if needed
        if(cat.getWebsite().getBloggerCategory().equals(cat)) {
            WeblogCategory rootCat = this.getRootWeblogCategory(cat.getWebsite());
            cat.getWebsite().setBloggerCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        if(cat.getWebsite().getDefaultCategory().equals(cat)) {
            WeblogCategory rootCat = this.getRootWeblogCategory(cat.getWebsite());
            cat.getWebsite().setDefaultCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());
    }
    
    
    public void moveWeblogCategory(WeblogCategory srcCat, WeblogCategory destCat)
            throws WebloggerException {
        
        // TODO: this check should be made before calling this method?
        if (destCat.descendentOf(srcCat)) {
            throw new WebloggerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        log.debug("Moving category "+srcCat.getPath()+" under "+destCat.getPath());
        
        srcCat.setParent(destCat);
        if("/".equals(destCat.getPath())) {
            srcCat.setPath("/"+srcCat.getName());
        } else {
            srcCat.setPath(destCat.getPath() + "/" + srcCat.getName());
        }
        saveWeblogCategory(srcCat);
        
        // the main work to be done for a category move is to update the 
        // path attribute of the category and all descendent categories
        WeblogCategory.updatePathTree(srcCat);
    }
    
    
    public void moveWeblogCategoryContents(WeblogCategory srcCat, WeblogCategory destCat)
            throws WebloggerException {
                
        // TODO: this check should be made before calling this method?
        if (destCat.descendentOf(srcCat)) {
            throw new WebloggerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        // get all entries in category and subcats
        List results = srcCat.retrieveWeblogEntries(true);
        
        // Loop through entries in src cat, assign them to dest cat
        Iterator iter = results.iterator();
        Weblog website = destCat.getWebsite();
        while (iter.hasNext()) {
            WeblogEntry entry = (WeblogEntry) iter.next();
            entry.setCategory(destCat);
            entry.setWebsite(website);
            this.strategy.store(entry);
        }
        
        // Make sure website's default and bloggerapi categories
        // are valid after the move
        
        if (srcCat.getWebsite().getDefaultCategory().getId().equals(srcCat.getId())
        || srcCat.getWebsite().getDefaultCategory().descendentOf(srcCat)) {
            srcCat.getWebsite().setDefaultCategory(destCat);
            this.strategy.store(srcCat.getWebsite());
        }
        
        if (srcCat.getWebsite().getBloggerCategory().getId().equals(srcCat.getId())
        || srcCat.getWebsite().getBloggerCategory().descendentOf(srcCat)) {
            srcCat.getWebsite().setBloggerCategory(destCat);
            this.strategy.store(srcCat.getWebsite());
        }
    }
    
    
    public void saveComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.store(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(comment.getWeblogEntry().getWebsite());
    }
    
    
    public void removeComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.remove(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(comment.getWeblogEntry().getWebsite());
    }
    
    
    // TODO: perhaps the createAnchor() and queuePings() items should go outside this method?
    public void saveWeblogEntry(WeblogEntry entry) throws WebloggerException {
        
        if (entry.getAnchor() == null || entry.getAnchor().trim().equals("")) {
            entry.setAnchor(this.createAnchor(entry));
        }
                
        for(Iterator it = entry.getAddedTags().iterator(); it.hasNext();) {
            String name = (String) it.next();
            updateTagCount(name, entry.getWebsite(), 1);
        }
        
        for(Iterator it = entry.getRemovedTags().iterator(); it.hasNext();) {
            String name = (String) it.next();
            updateTagCount(name, entry.getWebsite(), -1);
        } 
        
        // if the entry was published to future, set status as SCHEDULED
        // we only consider an entry future published if it is scheduled
        // more than 1 minute into the future
        if ("PUBLISHED".equals(entry.getStatus()) && 
                entry.getPubTime().after(new Date(System.currentTimeMillis() + 60000))) {
            entry.setStatus(WeblogEntry.SCHEDULED);
        }
        
        // Store value object (creates new or updates existing)
        entry.setUpdateTime(new Timestamp(new Date().getTime()));

        this.strategy.store(entry);        
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            RollerFactory.getRoller().getUserManager().saveWebsite(entry.getWebsite());
        }
        
        if(entry.isPublished()) {
            // Queue applicable pings for this update.
            RollerFactory.getRoller().getAutopingManager().queueApplicableAutoPings(entry);
        }
    }
    
    
    public void removeWeblogEntry(WeblogEntry entry) throws WebloggerException {
        
        Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
        
        // remove referers
        Criteria refererQuery = session.createCriteria(WeblogReferrer.class);
        refererQuery.add(Expression.eq("weblogEntry", entry));
        List referers = refererQuery.list();
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            WeblogReferrer referer = (WeblogReferrer) iter.next();
            this.strategy.remove(referer);
        }
        
        // remove comments
        List comments = getComments(
                null,  // website
                entry,
                null,  // search String
                null,  // startDate
                null,  // endDate
                null,  // status
                true,  // reverse chrono order (not that it matters)
                0,     // offset
                -1);   // no limit
        Iterator commentsIT = comments.iterator();
        while (commentsIT.hasNext()) {
            this.strategy.remove((WeblogEntryComment) commentsIT.next());
        }
        
        // remove tags aggregates
        if(entry.getTags() != null) {
            for(Iterator it = entry.getTags().iterator(); it.hasNext(); ) {
                WeblogEntryTag tag = (WeblogEntryTag) it.next();
                updateTagCount(tag.getName(), entry.getWebsite(), -1);
            }
        }
        
        // remove entry
        this.strategy.remove(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            RollerFactory.getRoller().getUserManager().saveWebsite(entry.getWebsite());
        }
        
        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWebsite().getHandle()+":"+entry.getAnchor());
    }
        
    public void removeWeblogEntryAttribute(String name,WeblogEntry entry)
             throws WebloggerException {
        for (Iterator it = entry.getEntryAttributes().iterator(); it.hasNext();) {
            WeblogEntryAttribute entryAttribute = (WeblogEntryAttribute) it.next();
            if (entryAttribute.getName().equals(name)) {
                //Call back the entity to adjust its internal state
                entry.onRemoveEntryAttribute(entryAttribute);
                //Remove it from the collection
                it.remove();
                //Remove it from database
                this.strategy.remove(entryAttribute);
            }
        }
    }

    public void removeWeblogEntryTag(String name,WeblogEntry entry)
            throws WebloggerException {
        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTag tag = (WeblogEntryTag) it.next();
            if (tag.getName().equals(name)) {
                //Call back the entity to adjust its internal state
                entry.onRemoveTag(name);
                //Remove it from the collection
                it.remove();
                //Remove it from database
                this.strategy.remove(tag);
            }
        }
    }

    public List getNextPrevEntries(WeblogEntry current, String catName, 
                                   String locale, int maxEntries, boolean next)
            throws WebloggerException {
        
        Junction conjunction = Expression.conjunction();
        conjunction.add(Expression.eq("website", current.getWebsite()));
        conjunction.add(Expression.eq("status",WeblogEntry.PUBLISHED));
        
        if (next) {
            conjunction.add(Expression.gt("pubTime", current.getPubTime()));
        } else {
            conjunction.add(Expression.lt("pubTime", current.getPubTime()));
        }
        
        if (catName != null && !catName.trim().equals("/")) {
            WeblogCategory category =
                    getWeblogCategoryByPath(current.getWebsite(), catName);
            if (category != null) {
                conjunction.add(Expression.eq("category", category));
            } else {
                throw new WebloggerException("Cannot find category: "+catName);
            }
        }
        
        if(locale != null) {
            conjunction.add(Expression.ilike("locale", locale, MatchMode.START));
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntry.class);
            criteria.addOrder(next ? Order.asc("pubTime") : Order.desc("pubTime"));
            criteria.add(conjunction);
            criteria.setMaxResults(maxEntries);
            List results = criteria.list();
            return results;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public WeblogCategory getRootWeblogCategory(Weblog website)
    throws WebloggerException {
        if (website == null)
            throw new WebloggerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategory.class);
            
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.isNull("parent"));
            criteria.setMaxResults(1);
            
            return (WeblogCategory) criteria.uniqueResult();
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getWeblogCategories(Weblog website, boolean includeRoot)
            throws WebloggerException {
        
        if (website == null) {
            throw new WebloggerException("website is null");
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategory.class);
            criteria.add(Expression.eq("website", website));
            
            if(!includeRoot) {
                criteria.add(Expression.isNotNull("parent"));
            }
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getWeblogEntries(
            Weblog website,
            User    user,
            Date        startDate,
            Date        endDate,
            String      catName,
            List        tags,
            String      status,
            String      text,
            String      sortby,
            String      sortOrder,
            String      locale,
            int         offset,
            int         length) throws WebloggerException {
        
        WeblogCategory cat = null;
        if (StringUtils.isNotEmpty(catName) && website != null) {
            cat = getWeblogCategoryByPath(website, catName);
            if (cat == null) catName = null;
        }
        if (catName != null && catName.trim().equals("/")) {
            catName = null;
        }
                
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            
            ArrayList params = new ArrayList();
            StringBuffer queryString = new StringBuffer();
            queryString.append("from WeblogEntry e where ");

            if (website != null) {
                queryString.append("website.id = ? ");                
                params.add(website.getId());
            } else {
                queryString.append("website.enabled = ? ");                
                params.add(Boolean.TRUE);                
            }
            
            if (user != null) {
                queryString.append("and creator.id = ? ");
                params.add(user.getId());
            }

            if (startDate != null) {
                queryString.append("and pubTime >= ? ");
                params.add(startDate);
            }
            
            if (endDate != null) {
                queryString.append("and pubTime <= ? ");
                params.add(endDate);                
            }
            
            if (cat != null && website != null) {
                queryString.append("and category.id = ? ");
                params.add(cat.getId());                
            }
            
            if (tags != null && tags.size() > 0) {
              for(int i = 0; i < tags.size(); i++) {
                queryString.append(" and tags.name = ?");
                params.add(tags.get(i));
              }
            }
            
            if (status != null) {
                queryString.append("and status = ? ");
                params.add(status);
            }
            
            if (text != null) {
                queryString.append("and (text like ? or summary like ? or title like ?) ");
                String tmp = "%" + text + "%";
                params.add(tmp);
                params.add(tmp);
                params.add(tmp);
            }
            
            if (locale != null) {
                queryString.append("and locale like ? ");
                params.add(locale + '%');
            }
            

            if (sortby != null && sortby.equals("updateTime")) {
                queryString.append("order by updateTime ");
            } else {
                queryString.append("order by pubTime ");
            }
            
            if (sortOrder != null && sortOrder.equals(ASCENDING)) {
                queryString.append("asc ");
            } else {
                queryString.append("desc ");
            }

                
            Query query = session.createQuery(queryString.toString());
            
            // set params
            for(int i = 0; i < params.size(); i++) {
              query.setParameter(i, params.get(i));
            }
                        
            if (offset != 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            return query.list();
            
        } catch (HibernateException e) {
            log.error(e);
            throw new WebloggerException(e);
        }
    }
    
    public List getWeblogEntriesPinnedToMain(Integer max) throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntry.class);
            criteria.add(Expression.eq("pinnedToMain", Boolean.TRUE));
            criteria.add(Expression.eq("status",WeblogEntry.PUBLISHED));
            criteria.addOrder(Order.desc("pubTime"));
            if (max != null) {
                criteria.setMaxResults(max.intValue());
            }
            return criteria.list();
        } catch (HibernateException e) {
            log.error(e);
            throw new WebloggerException(e);
        }
    }
    
    
    public WeblogEntry getWeblogEntryByAnchor(Weblog website, String anchor) 
            throws WebloggerException {
        
        if (website == null)
            throw new WebloggerException("Website is null");
        
        if (anchor == null)
            throw new WebloggerException("Anchor is null");
        
        // mapping key is combo of weblog + anchor
        String mappingKey = website.getHandle()+":"+anchor;
        
        // check cache first
        // NOTE: if we ever allow changing anchors then this needs updating
        if(this.entryAnchorToIdMap.containsKey(mappingKey)) {
            
            WeblogEntry entry = this.getWeblogEntry((String) this.entryAnchorToIdMap.get(mappingKey));
            if(entry != null) {
                log.debug("entryAnchorToIdMap CACHE HIT - "+mappingKey);
                return entry;
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.entryAnchorToIdMap.remove(mappingKey);
            }
        }
        
        // cache failed, do lookup
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntry.class);
            criteria.add(Expression.conjunction()
            .add(Expression.eq("website",website))
            .add(Expression.eq("anchor",anchor)));
            criteria.addOrder(Order.desc("pubTime"));
            criteria.setMaxResults(1);
            
            List list = criteria.list();
            
            WeblogEntry entry = null;
            if(list.size() != 0) {
                entry = (WeblogEntry) criteria.uniqueResult();
            }
            
            // add mapping to cache
            if(entry != null) {
                log.debug("entryAnchorToIdMap CACHE MISS - "+mappingKey);
                this.entryAnchorToIdMap.put(mappingKey, entry.getId());
            }
            
            return entry;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    // TODO: this method should be removed and it's functionality moved to getWeblogEntries()
    public List getWeblogEntries(WeblogCategory cat, boolean subcats)
        throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntry.class);
            criteria.add(Expression.eq("status",WeblogEntry.PUBLISHED));
            
            if(!subcats) {
                // if no subcats then this is an equals query
                criteria.add(Expression.eq("category", cat));
            } else {
                // if we are doing subcats then do a case sensitive
                // query using category path
                criteria.createAlias("category", "cat");
                criteria.add(Expression.like("cat.path", cat.getPath()+"%"));
                criteria.add(Expression.eq("website", cat.getWebsite()));
            }
            
            return criteria.list();
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
        
    }
        
    public String createAnchor(WeblogEntry entry) throws WebloggerException {
        try {
            // Check for uniqueness of anchor
            String base = entry.createAnchorBase();
            String name = base;
            int count = 0;
            
            while (true) {
                if (count > 0) {
                    name = base + count;
                }
                
                Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
                Criteria criteria = session.createCriteria(WeblogEntry.class);
                criteria.add(Expression.eq("website", entry.getWebsite()));
                criteria.add(Expression.eq("anchor", name));
                
                List results = criteria.list();
                
                if (results.size() < 1) {
                    break;
                } else {
                    count++;
                }
            }
            return name;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    // TODO: provide unit test case
    public boolean isDuplicateWeblogCategoryName(WeblogCategory cat)
            throws WebloggerException {
        
        // ensure that no sibling categories share the same name
        WeblogCategory parent = cat.getParent();
        if (null != parent) {
            return (getWeblogCategoryByPath(cat.getWebsite(), cat.getPath()) != null);
        }
        
        return false;
    }
    
    
    public boolean isWeblogCategoryInUse(WeblogCategory cat)
    throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntry.class);
            criteria.add(Expression.eq("category", cat));
            criteria.setMaxResults(1);
            int entryCount = criteria.list().size();
            
            if (entryCount > 0) {
                return true;
            }
            
            Iterator cats = cat.getWeblogCategories().iterator();
            while (cats.hasNext()) {
                WeblogCategory childCat = (WeblogCategory)cats.next();
                if (childCat.isInUse()) {
                    return true;
                }
            }
            
            if (cat.getWebsite().getBloggerCategory().equals(cat)) {
                return true;
            }
            
            if (cat.getWebsite().getDefaultCategory().equals(cat)) {
                return true;
            }
            
            return false;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
                
    public List getComments(
            
            Weblog     website,WeblogEntry entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            String          status,
            boolean         reverseChrono,
            int             offset,
            int             length
            
            ) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryComment.class);
            
            if (entry != null) {
                criteria.add(Expression.eq("weblogEntry", entry));
            } else if (website != null) {
                criteria.createAlias("weblogEntry","e");
                criteria.add(Expression.eq("e.website", website));
            }
            
            if (searchString != null) {
                criteria.add(Expression.disjunction()
                   .add(Expression.like("url", searchString, MatchMode.ANYWHERE))
                   .add(Expression.like("content", searchString, MatchMode.ANYWHERE)));
            }
            
            if (startDate != null) {
                criteria.add(Expression.ge("postTime", startDate));
            }
            
            if (endDate != null) {
                criteria.add(Expression.le("postTime", endDate));
            }
            
            if (status != null) {
                if("ALL_IGNORE_SPAM".equals(status)) {
                    // we want all comments, expect spam
                    // so that means where status != SPAM
                    criteria.add(Expression.ne("status", "SPAM"));
                } else {
                    criteria.add(Expression.eq("status", status));
                }
            }
            
            if (length != -1) {
                criteria.setMaxResults(offset + length);
            }
            
            if (reverseChrono) {
                criteria.addOrder(Order.desc("postTime"));
            } else {
                criteria.addOrder(Order.asc("postTime"));
            }
            
            List comments = criteria.list();
            if (offset==0 || comments.size() < offset) {
                return comments;
            }
            List range = new ArrayList();
            for (int i=offset; i<comments.size(); i++) {
                range.add(comments.get(i));
            }
            return range;
            
        } catch (HibernateException e) {
            log.error(e);
            throw new WebloggerException(e);
        }
    }
    
    public int removeMatchingComments(
            
            Weblog     website,WeblogEntry entry, 
            String  searchString, 
            Date    startDate, 
            Date    endDate, 
            String status) throws WebloggerException {

        try {
            List comments = getComments( 
                website, entry, searchString, startDate, endDate, 
                status, true, 0, -1);
            int count = 0;
            for (Iterator it = comments.iterator(); it.hasNext();) {
                WeblogEntryComment comment = (WeblogEntryComment) it.next();
                removeComment(comment);
                count++;
            }
            return count;
        
        /* I'd MUCH rather use a bulk delete, but MySQL says "General error,  
           message from server: "You can't specify target table 'roller_comment' 
           for update in FROM clause"

            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
         
            // Can't use Criteria API to do bulk delete, so we build string     
            StringBuffer queryString = new StringBuffer();
            ArrayList params = new ArrayList();
         
            // Can't use join in a bulk delete query, but can use a sub-query      
            queryString.append(
                "delete WeblogEntryComment cmt where cmt.id in "
              + "(select c.id from WeblogEntryComment as c where ");
                
            if (entry != null) {
                queryString.append("c.weblogEntry.anchor = ? and c.weblogEntry.website.handle = ? ");
                params.add(entry.getAnchor());
                params.add(entry.getWebsite().getHandle());
            } else if (website != null) {
                queryString.append("c.weblogEntry.website.handle = ? ");
                params.add(website.getHandle());
            } 
            
            if (searchString != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("(c.url like ? or c.content like ?) ");
                searchString = '%' + searchString + '%';
                params.add(searchString);
                params.add(searchString);
            }
            
            if (startDate != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.postTime > ? ");
                params.add(startDate);
            }
            
            if (endDate != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.postTime < ? ");
                params.add(endDate);
            }
            
            if (pending != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.pending = ? ");
                params.add(pending);
            }
            
            if (approved != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.approved = ? ");
                params.add(approved);
            }
            
            if (spam != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.spam = ? ");
                params.add(spam);
            }
            queryString.append(")");
            
            Query query = session.createQuery(queryString.toString());
            for(int i = 0; i < params.size(); i++) {
              query.setParameter(i, params.get(i));
            }
            return query.executeUpdate(); 
         */
                        
        } catch (HibernateException e) {
            log.error(e);
            throw new WebloggerException(e);
        }
    }
    
    
    public WeblogCategory getWeblogCategory(String id)
    throws WebloggerException {
        return (WeblogCategory) this.strategy.load(
                id,
                WeblogCategory.class);
    }
    
    //--------------------------------------------- WeblogCategory Queries
    
    public WeblogCategory getWeblogCategoryByPath(Weblog website, 
                                                      String categoryPath) 
            throws WebloggerException {
        
        if (categoryPath == null || categoryPath.trim().equals("/")) {
            return getRootWeblogCategory(website);
        } else {
            String catPath = categoryPath;
            
            // all cat paths must begin with a '/'
            if(!catPath.startsWith("/")) {
                catPath = "/"+catPath;
            }
            
            // now just do simple lookup by path
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();            
            Criteria criteria = session.createCriteria(WeblogCategory.class);
            criteria.add(Expression.eq("path", catPath));
            criteria.add(Expression.eq("website", website));
            
            return (WeblogCategory) criteria.uniqueResult();
        }
    }
    
        
    public WeblogEntryComment getComment(String id) throws WebloggerException {
        return (WeblogEntryComment) this.strategy.load(id,WeblogEntryComment.class);
    }
        
    public WeblogEntry getWeblogEntry(String id)
    throws WebloggerException {
        return (WeblogEntry) this.strategy.load(
                
                id,WeblogEntry.class);
    }
    
    
    public Map getWeblogEntryObjectMap(
            Weblog website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,            
            String  status,
            String  locale,
            int     offset,
            int     length) throws WebloggerException {
        return getWeblogEntryMap(
            website,
            startDate,
            endDate,
            catName,
            tags,
            status,
            false,
            locale,
            offset,
            length);
    }
    
    public Map getWeblogEntryStringMap(
            Weblog website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,            
            String  status,
            String  locale,
            int     offset,
            int     length
            ) throws WebloggerException {
        return getWeblogEntryMap(
            website,
            startDate,
            endDate,
            catName,
            tags,
            status,
            true,
            locale,
            offset,
            length
            );
    }
    
    private Map getWeblogEntryMap(
            Weblog website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,
            String  status,
            boolean stringsOnly,
            String  locale,
            int     offset,
            int     length
            ) throws WebloggerException {
        
        TreeMap map = new TreeMap(reverseComparator);
        
        List entries = getWeblogEntries(
            website,                 
            null,
            startDate,
            endDate,
            catName,
            tags,
            status,
            null,
            null,
            null,
            locale,             
            offset,
            length);
        
        Calendar cal = Calendar.getInstance();
        if (website != null) {
            cal.setTimeZone(website.getTimeZoneInstance());
        }
        
        SimpleDateFormat formatter = DateUtil.get8charDateFormat();
        for (Iterator wbItr = entries.iterator(); wbItr.hasNext();) {
            WeblogEntry entry = (WeblogEntry) wbItr.next();
            Date sDate = DateUtil.getNoonOfDay(entry.getPubTime(), cal);
            if (stringsOnly) {
                if (map.get(sDate) == null)
                    map.put(sDate, formatter.format(sDate));
            } else {
                List dayEntries = (List) map.get(sDate);
                if (dayEntries == null) {
                    dayEntries = new ArrayList();
                    map.put(sDate, dayEntries);
                }
                dayEntries.add(entry);
            }
        }
        return map;
    }
    
    public List getMostCommentedWeblogEntries( 
            Weblog website, Date startDate, Date endDate, int offset, int length) 
            throws WebloggerException {
        // TODO: ATLAS getMostCommentedWeblogEntries DONE
        String msg = "Getting most commented weblog entries";
        if (endDate == null) endDate = new Date();
        try {      
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();            
            Query query = null;
            if (website != null) {
                StringBuffer sb = new StringBuffer();
                sb.append("select count(distinct c), c.weblogEntry.website.handle, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("from WeblogEntryComment c ");
                sb.append("where c.weblogEntry.website=:website and c.weblogEntry.pubTime < :endDate ");
                if (startDate != null) {
                    sb.append("and c.weblogEntry.pubTime > :startDate ");
                }                   
                sb.append("group by c.weblogEntry.website.handle, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("order by col_0_0_ desc");
                query = session.createQuery(sb.toString());
                query.setParameter("website", website);
                query.setParameter("endDate", endDate);
                if (startDate != null) {
                    query.setParameter("startDate", startDate);
                }   
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("select count(distinct c),   c.weblogEntry.website.handle, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("from WeblogEntryComment c ");
                sb.append("where c.weblogEntry.pubTime < :endDate ");
                if (startDate != null) {
                    sb.append("and c.weblogEntry.pubTime > :startDate ");
                } 
                sb.append("group by c.weblogEntry.website.handle, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("order by col_0_0_ desc ");
                query = session.createQuery(sb.toString());
                query.setParameter("endDate", endDate);
                if (startDate != null) {
                    query.setParameter("startDate", startDate);
                } 
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
                    (String)row[1],                             // website handle
                    (String)row[2],                             // entry anchor
                    (String)row[3],                             // entry title
                    "statCount.weblogEntryCommentCountType",    // stat desc
                    new Long(((Number)row[0]).longValue())); // count
                
                results.add(statCount);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new WebloggerException(msg, pe);
        }
    }
    
    
    public WeblogEntry getNextEntry(WeblogEntry current, String catName,
                                        String locale)
            throws WebloggerException {
        
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, locale, 1, true);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }
    
    
    public WeblogEntry getPreviousEntry(WeblogEntry current, String catName,
                                            String locale)
            throws WebloggerException {
        
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, locale, 1, false);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }
    
    
    public void release() {}

    /**
     * Apply comment defaults (defaultAllowComments and defaultCommentDays) to
     * all existing entries in a website using a single HQL query.
     * @param website Website where comment defaults are from/to be applied.
     */
    public void applyCommentDefaultsToEntries(Weblog website) throws WebloggerException {
        if (log.isDebugEnabled()) {
            log.debug("applyCommentDefaults");
        }       
        try {
            Session session = strategy.getSession();
            String updateString = "update WeblogEntry set "
                +"allowComments=:allowed, commentDays=:days, "
                +"pubTime=pubTime, updateTime=updateTime " // ensure timestamps are NOT reset
                +"where website=:site";
            Query update = session.createQuery(updateString);
            update.setParameter("allowed", website.getDefaultAllowComments());
            update.setParameter("days", new Integer(website.getDefaultCommentDays()));
            update.setParameter("site", website);
            update.executeUpdate();            
        } catch (Exception e) {
            log.error("EXCEPTION applying comment defaults",e);
        }
    }   
    
    /* (non-Javadoc)
     * @see org.apache.roller.weblogger.model.WeblogManager#getPopularTags(org.apache.roller.weblogger.pojos.WebsiteData, java.util.Date, int)
     */
    public List getPopularTags(Weblog website, Date startDate, int limit)
            throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy)
                    .getSession();

            ArrayList params = new ArrayList();
            StringBuffer queryString = new StringBuffer();
            queryString.append("select name, sum(total) ");
            queryString.append("from WeblogEntryTagAggregate where ");
            if (website != null) {
                queryString.append("weblog.id = ? ");
                params.add(website.getId());
            } else {
                queryString.append("weblog = NULL ");
            }
            if (startDate != null) {
                queryString.append("and lastUsed >= ? ");
                params.add(startDate);
            }

            queryString.append("group by name, total order by total desc");

            Query query = session.createQuery(queryString.toString());
            if (limit > 0)
                query.setMaxResults(limit);

            // set params
            for (int i = 0; i < params.size(); i++) {
                query.setParameter(i, params.get(i));
            }

            double min = Integer.MAX_VALUE;
            double max = Integer.MIN_VALUE;

            List results = new ArrayList(limit);
            
            for (Iterator iter = query.list().iterator(); iter.hasNext();) {
                Object[] row = (Object[]) iter.next();
                TagStat t = new TagStat();
                t.setName((String) row[0]);
                t.setCount(((Number) row[1]).intValue());                
                
                min = Math.min(min, t.getCount());
                max = Math.max(max, t.getCount());                
                results.add(t);
            }
            
            min = Math.log(1+min);
            max = Math.log(1+max);

            double range = Math.max(.01, max - min) * 1.0001;
            
            for (Iterator iter = results.iterator(); iter.hasNext(); )
            {
                TagStat t = (TagStat) iter.next();
                t.setIntensity((int) (1 + Math.floor(5 * (Math.log(1+t.getCount()) - min) / range)));
            }            

            // sort results by name, because query had to sort by total
            Collections.sort(results, tagStatComparator);
            
            return results;

        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.roller.weblogger.model.WeblogManager#getTags(org.apache.roller.weblogger.pojos.WebsiteData,
     *      java.lang.String, java.lang.String, int)
     */
    public List getTags(Weblog website, String sortBy, String startsWith, int limit) throws WebloggerException {    
        try {
            List results = new ArrayList();

            Session session = ((HibernatePersistenceStrategy) strategy)
                    .getSession();

            if (sortBy != null && sortBy.equals("count")) {
                sortBy = "total desc";
            } else {
                sortBy = "name";
            }

            StringBuffer queryString = new StringBuffer();
            queryString.append("select name, sum(total) ");
            queryString.append("from WeblogEntryTagAggregate where ");
            if (website != null)
                queryString.append("weblog.id = '" + website.getId() + "' ");
            else
                queryString.append("weblog = NULL ");
            if (startsWith != null && startsWith.length() > 0)
                queryString.append("and name like '" + startsWith + "%' ");

            queryString.append("group by name, total order by " + sortBy);

            Query query = session.createQuery(queryString.toString());
            if (limit > 0)
                query.setMaxResults(limit);

            for (Iterator iter = query.list().iterator(); iter.hasNext();) {
                Object[] row = (Object[]) iter.next();
                TagStat ce = new TagStat();
                ce.setName((String) row[0]);
                ce.setCount(((Number) row[1]).intValue());
                results.add(ce);
            }

            return results;

        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }

    }
    
    
    /**
     * Remember, this checks that the exact tag intersection specified by the
     * list of tags exists either site-wide or for a given weblog.  So if the
     * list of tags is "foo", "bar" and we only find "foo" then that's a
     * miss and we return false.
     */
    public boolean getTagComboExists(List tags, Weblog weblog) 
            throws WebloggerException {
        
        boolean comboExists = false;
        
        if(tags == null) {
            return false;
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy) strategy)
                        .getSession();
            
            StringBuffer queryString = new StringBuffer();
            queryString.append("select distinct name ");
            queryString.append("from WeblogEntryTagAggregate ");
            queryString.append("where name in ( :tags ) ");
            
            // are we checking a specific weblog, or site-wide?
            if (weblog != null)
                queryString.append("and weblog.id = '" + weblog.getId() + "' ");
            else
                queryString.append("and weblog is null ");
            
            Query query = session.createQuery(queryString.toString());
            query.setParameterList("tags", tags);
            
            List results = query.list();
            comboExists = (results != null && results.size() == tags.size());
            
        } catch (HibernateException ex) {
            throw new WebloggerException(ex);
        }
            
        return comboExists;
    }
    
    
    public void updateTagCount(String name, Weblog website, int amount) throws WebloggerException {
        
        Session session = ((HibernatePersistenceStrategy) strategy)
        .getSession();
        
        if(amount == 0) {
            throw new WebloggerException("Tag increment amount cannot be zero.");
        }
        
        if(website == null) {
            throw new WebloggerException("Website cannot be NULL.");
        }
                        
        Junction conjunction = Expression.conjunction();
        conjunction.add(Expression.eq("name", name));
        conjunction.add(Expression.eq("weblog", website));

        // The reason why add order lastUsed desc is to make sure we keep picking the most recent
        // one in the case where we have multiple rows (clustered environment)
        // eventually that second entry will have a very low total (most likely 1) and
        // won't matter
        
        Criteria criteria = session.createCriteria(WeblogEntryTagAggregate.class)
            .add(conjunction).addOrder(Order.desc("lastUsed")).setMaxResults(1);
        
        WeblogEntryTagAggregate weblogTagData = (WeblogEntryTagAggregate) criteria.uniqueResult();

        conjunction = Expression.conjunction();
        conjunction.add(Restrictions.eq("name", name));
        conjunction.add(Restrictions.isNull("weblog"));
        
        criteria = session.createCriteria(WeblogEntryTagAggregate.class)
            .add(conjunction).addOrder(Order.desc("lastUsed")).setMaxResults(1);
    
        WeblogEntryTagAggregate siteTagData = (WeblogEntryTagAggregate) criteria.uniqueResult();
        
        Timestamp lastUsed = new Timestamp((new Date()).getTime());
        
        // create it only if we are going to need it.
        if(weblogTagData == null && amount > 0) {
            weblogTagData = new WeblogEntryTagAggregate(null, website, name, amount);
            weblogTagData.setLastUsed(lastUsed);
            session.save(weblogTagData);
        } else if(weblogTagData != null) {
            session.createQuery("update WeblogEntryTagAggregate set total = total + ?, lastUsed = current_timestamp() where name = ? and weblog = ?")
            .setInteger(0, amount)
            .setString(1, weblogTagData.getName())
            .setParameter(2, website)
            .executeUpdate();
        }
        
        // create it only if we are going to need it.        
        if(siteTagData == null && amount > 0) {
            siteTagData = new WeblogEntryTagAggregate(null, null, name, amount);
            siteTagData.setLastUsed(lastUsed);
            session.save(siteTagData);
        } else if(siteTagData != null) {
            session.createQuery("update WeblogEntryTagAggregate set total = total + ?, lastUsed = current_timestamp() where name = ? and weblog is null")
            .setInteger(0, amount)
            .setString(1, siteTagData.getName())
            .executeUpdate();            
        }       
        
        // delete all bad counts
        session.createQuery("delete from WeblogEntryTagAggregate where total <= 0").executeUpdate();
    }
    
    
    public WeblogHitCount getHitCount(String id) throws WebloggerException {
        
        // do lookup
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogHitCount.class);
            
            criteria.add(Expression.eq("id", id));
            WeblogHitCount hitCount = (WeblogHitCount) criteria.uniqueResult();
            
            return hitCount;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public WeblogHitCount getHitCountByWeblog(Weblog weblog) 
        throws WebloggerException {
        
        // do lookup
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogHitCount.class);
            
            criteria.add(Expression.eq("weblog", weblog));
            WeblogHitCount hitCount = (WeblogHitCount) criteria.uniqueResult();
            
            return hitCount;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getHotWeblogs(int sinceDays, int offset, int length) 
        throws WebloggerException {
        
        // figure out start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        
        try {      
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();            
            Query query = session.createQuery(
                    "from WeblogHitCount hcd " +
                    "where hcd.weblog.enabled=true " +
                    "and hcd.weblog.active=true " +
                    "and hcd.weblog.lastModified > :startDate " +
                    "and hcd.dailyHits > 0 " +
                    "order by hcd.dailyHits desc");
            query.setParameter("startDate", startDate);
            
            if (offset != 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            
            return query.list();
            
        } catch (Throwable pe) {
            throw new WebloggerException(pe);
        }
    }
    
    
    public void saveHitCount(WeblogHitCount hitCount) throws WebloggerException {
        this.strategy.store(hitCount);
    }
    
    
    public void removeHitCount(WeblogHitCount hitCount) throws WebloggerException {
        this.strategy.remove(hitCount);
    }
    
    
    public void incrementHitCount(Weblog weblog, int amount)
        throws WebloggerException {
        
        Session session = ((HibernatePersistenceStrategy) strategy).getSession();
        
        if(amount == 0) {
            throw new WebloggerException("Tag increment amount cannot be zero.");
        }
        
        if(weblog == null) {
            throw new WebloggerException("Website cannot be NULL.");
        }
        
        Criteria criteria = session.createCriteria(WeblogHitCount.class);
        criteria.add(Expression.eq("weblog", weblog));
        criteria.setMaxResults(1);
        
        WeblogHitCount hitCount = (WeblogHitCount) criteria.uniqueResult();
        
        // create it if it doesn't exist
        if(hitCount == null && amount > 0) {
            hitCount = new WeblogHitCount();
            hitCount.setWeblog(weblog);
            hitCount.setDailyHits(amount);
            strategy.store(hitCount);
        } else if(hitCount != null) {
            hitCount.setDailyHits(hitCount.getDailyHits() + amount);
            strategy.store(hitCount);
        }
    }
    
    
    public void resetAllHitCounts() throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            session.createQuery("update WeblogHitCount set dailyHits = 0").executeUpdate();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public void resetHitCount(Weblog weblog) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            String query = "update WeblogHitCount set dailyHits = 0 where weblog = ?";
            session.createQuery(query).setParameter(0, weblog).executeUpdate();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get site-wide comment count 
     */
    public long getCommentCount() throws WebloggerException {
        return getCommentCount(null);
    }

    
    /**
     * Get weblog comment count 
     */    
    public long getCommentCount(Weblog website) throws WebloggerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Query query = null;
            if(website == null) {
                query = session.createQuery("select count(distinct c) "+
                        "from WeblogEntryComment c where c.status=?");
                query.setParameter(0,WeblogEntryComment.APPROVED);
            } else {
                query = session.createQuery("select count(distinct c) "+
                        "from WeblogEntryComment c where c.status=? and c.weblogEntry.website=?");
                query.setParameter(0,WeblogEntryComment.APPROVED);
                query.setParameter(1, website);
            }
            List result = query.list();
            ret = ((Number)result.get(0)).intValue();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        return ret;
    }

    
    /**
     * Get site-wide entry count 
     */    
    public long getEntryCount() throws WebloggerException {
        return getEntryCount(null);
    }

    
    /**
     * Get weblog entry count 
     */    
    public long getEntryCount(Weblog website) throws WebloggerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Query query = null;
            if(website == null) {
                query = session.createQuery("select count(distinct e) "+
                        "from WeblogEntry e where e.status=?");
                query.setParameter(0,WeblogEntry.PUBLISHED);
            } else {
                query = session.createQuery("select count(distinct e) "+
                        "from WeblogEntry e where e.status=? and e.website=?");
                query.setParameter(0,WeblogEntry.PUBLISHED);
                query.setParameter(1, website);
            }
            List result = query.list();
            ret = ((Number)result.get(0)).intValue();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        return ret;
    }
    
}
