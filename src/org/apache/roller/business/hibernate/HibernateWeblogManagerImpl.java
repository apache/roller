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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.pojos.TagStat;
import org.apache.roller.pojos.TagStatComparator;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryAssoc;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogEntryTagData;
import org.apache.roller.pojos.WeblogEntryTagAggregateData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.Utilities;
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
    
    
    public void saveWeblogCategory(WeblogCategoryData cat) throws RollerException {
        
        if(this.isDuplicateWeblogCategoryName(cat)) {
            throw new RollerException("Duplicate category name, cannot save category");
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());
        
        this.strategy.store(cat);
    }
    
    
    public void removeWeblogCategory(WeblogCategoryData cat) throws RollerException {
        
        if(cat.retrieveWeblogEntries(true).size() > 0) {
            throw new RollerException("Cannot remove category with entries");
        }
        
        // remove cat
        this.strategy.remove(cat);
        
        // update website default cats if needed
        if(cat.getWebsite().getBloggerCategory().equals(cat)) {
            WeblogCategoryData rootCat = this.getRootWeblogCategory(cat.getWebsite());
            cat.getWebsite().setBloggerCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        if(cat.getWebsite().getDefaultCategory().equals(cat)) {
            WeblogCategoryData rootCat = this.getRootWeblogCategory(cat.getWebsite());
            cat.getWebsite().setDefaultCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());
    }
    
    
    public void moveWeblogCategoryContents(WeblogCategoryData srcCat, WeblogCategoryData destCat)
            throws RollerException {
                
        // TODO: this check should be made before calling this method?
        if (destCat.descendentOf(srcCat)) {
            throw new RollerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        // get all entries in category and subcats
        List results = srcCat.retrieveWeblogEntries(true);
        
        // Loop through entries in src cat, assign them to dest cat
        Iterator iter = results.iterator();
        WebsiteData website = destCat.getWebsite();
        while (iter.hasNext()) {
            WeblogEntryData entry = (WeblogEntryData) iter.next();
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
    
    
    public void saveComment(CommentData comment) throws RollerException {
        this.strategy.store(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(comment.getWeblogEntry().getWebsite());
    }
    
    
    public void removeComment(CommentData comment) throws RollerException {
        this.strategy.remove(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(comment.getWeblogEntry().getWebsite());
    }
    
    
    // TODO: perhaps the createAnchor() and queuePings() items should go outside this method?
    public void saveWeblogEntry(WeblogEntryData entry) throws RollerException {
        
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
    
    
    public void removeWeblogEntry(WeblogEntryData entry) throws RollerException {
        
        Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
        
        // remove referers
        Criteria refererQuery = session.createCriteria(RefererData.class);
        refererQuery.add(Expression.eq("weblogEntry", entry));
        List referers = refererQuery.list();
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            RefererData referer = (RefererData) iter.next();
            this.strategy.remove(referer);
        }
        
        // remove comments
        List comments = getComments(
                null,  // website
                entry,
                null,  // search String
                null,  // startDate
                null,  // endDate
                null,  // pending
                null,  // approved
                null,  // spam
                true,  // reverse chrono order (not that it matters)
                0,     // offset
                -1);   // no limit
        Iterator commentsIT = comments.iterator();
        while (commentsIT.hasNext()) {
            this.strategy.remove((CommentData) commentsIT.next());
        }
        
        // remove tags aggregates
        if(entry.getTags() != null) {
            for(Iterator it = entry.getTags().iterator(); it.hasNext(); ) {
                WeblogEntryTagData tag = (WeblogEntryTagData) it.next();
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
        
    public List getNextPrevEntries(WeblogEntryData current, String catName, 
                                   String locale, int maxEntries, boolean next)
            throws RollerException {
        
        Junction conjunction = Expression.conjunction();
        conjunction.add(Expression.eq("website", current.getWebsite()));
        conjunction.add(Expression.eq("status", WeblogEntryData.PUBLISHED));
        
        if (next) {
            conjunction.add(Expression.gt("pubTime", current.getPubTime()));
        } else {
            conjunction.add(Expression.lt("pubTime", current.getPubTime()));
        }
        
        if (catName != null && !catName.trim().equals("/")) {
            WeblogCategoryData category =
                    getWeblogCategoryByPath(current.getWebsite(), null, catName);
            if (category != null) {
                conjunction.add(Expression.eq("category", category));
            } else {
                throw new RollerException("Cannot find category: "+catName);
            }
        }
        
        if(locale != null) {
            conjunction.add(Expression.ilike("locale", locale, MatchMode.START));
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.addOrder(next ? Order.asc("pubTime") : Order.desc("pubTime"));
            criteria.add(conjunction);
            criteria.setMaxResults(maxEntries);
            List results = criteria.list();
            return results;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public WeblogCategoryData getRootWeblogCategory(WebsiteData website)
    throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.createAlias("category","c");
            
            criteria.add(Expression.eq("c.website", website));
            criteria.add(Expression.isNull("ancestorCategory"));
            criteria.add(Expression.eq("relation", WeblogCategoryAssoc.PARENT));
            
            criteria.setMaxResults(1);
            
            List list = criteria.list();
            return ((WeblogCategoryAssoc)list.get(0)).getCategory();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogCategories(WebsiteData website, boolean includeRoot)
    throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        
        if (includeRoot) return getWeblogCategories(website);
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.createAlias("category", "c");
            criteria.add(Expression.eq("c.website", website));
            criteria.add(Expression.isNotNull("ancestorCategory"));
            criteria.add(Expression.eq("relation", "PARENT"));
            Iterator assocs = criteria.list().iterator();
            List cats = new ArrayList();
            while (assocs.hasNext()) {
                WeblogCategoryAssoc assoc = (WeblogCategoryAssoc) assocs.next();
                cats.add(assoc.getCategory());
            }
            return cats;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogCategories(WebsiteData website) throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryData.class);
            criteria.add(Expression.eq("website", website));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogEntries(
            WebsiteData website,
            UserData    user,
            Date        startDate,
            Date        endDate,
            String      catName,
            List        tags,
            String      status,
            String      sortby,
            String      locale,
            int         offset,
            int         length) throws RollerException {
        
        WeblogCategoryData cat = null;
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
            queryString.append("from WeblogEntryData e where ");

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
            
            if (locale != null) {
                queryString.append("and locale like ? ");
                params.add(locale + '%');
            }
            
            if (sortby != null && sortby.equals("updateTime")) {
                queryString.append("order by updateTime desc ");
            } else {
                queryString.append("order by pubTime desc ");
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
            throw new RollerException(e);
        }
    }
    
    public List getWeblogEntriesPinnedToMain(Integer max) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.add(Expression.eq("pinnedToMain", Boolean.TRUE));
            criteria.addOrder(Order.desc("pubTime"));
            if (max != null) {
                criteria.setMaxResults(max.intValue());
            }
            return criteria.list();
        } catch (HibernateException e) {
            log.error(e);
            throw new RollerException(e);
        }
    }
    
    
    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website, String anchor) 
            throws RollerException {
        
        if (website == null)
            throw new RollerException("Website is null");
        
        if (anchor == null)
            throw new RollerException("Anchor is null");
        
        // mapping key is combo of weblog + anchor
        String mappingKey = website.getHandle()+":"+anchor;
        
        // check cache first
        // NOTE: if we ever allow changing anchors then this needs updating
        if(this.entryAnchorToIdMap.containsKey(mappingKey)) {
            
            WeblogEntryData entry = this.getWeblogEntry((String) this.entryAnchorToIdMap.get(mappingKey));
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
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.add(Expression.conjunction()
            .add(Expression.eq("website",website))
            .add(Expression.eq("anchor",anchor)));
            criteria.addOrder(Order.desc("pubTime"));
            criteria.setMaxResults(1);
            
            List list = criteria.list();
            
            WeblogEntryData entry = null;
            if(list.size() != 0) {
                entry = (WeblogEntryData) criteria.uniqueResult();
            }
            
            // add mapping to cache
            if(entry != null) {
                log.debug("entryAnchorToIdMap CACHE MISS - "+mappingKey);
                this.entryAnchorToIdMap.put(mappingKey, entry.getId());
            }
            
            return entry;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public Date getWeblogLastPublishTime(WebsiteData website, String catName)
    throws RollerException {
        WeblogCategoryData cat = null;

        if (catName != null && website != null) {
            cat = getWeblogCategoryByPath(website, null, catName);
            if (cat == null) catName = null;
        }
        if (catName != null && catName.trim().equals("/")) {
            catName = null;
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.add(Expression.eq("status", WeblogEntryData.PUBLISHED));
            criteria.add(Expression.le("pubTime", new Date()));
            
            if (website != null) {
                criteria.add(Expression.eq("website", website));
            }
            
            if ( cat != null ) {
                criteria.add(Expression.eq("category", cat));
            }
            
            criteria.addOrder(Order.desc("pubTime"));
            criteria.setMaxResults(1);
            List list = criteria.list();
            if (list.size() > 0) {
                return ((WeblogEntryData)list.get(0)).getPubTime();
            } else {
                return null;
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    public List getWeblogEntries(WeblogCategoryData cat, boolean subcats)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            List entries = new LinkedList();
            
            if (subcats) {
                // Get entries in subcategories
                Criteria assocsQuery =
                        session.createCriteria(WeblogCategoryAssoc.class);
                assocsQuery.add(Expression.eq("ancestorCategory", cat));
                Iterator assocs = assocsQuery.list().iterator();
                while (assocs.hasNext()) {
                    WeblogCategoryAssoc assoc = (WeblogCategoryAssoc)assocs.next();
                    Criteria entriesQuery =
                            session.createCriteria(WeblogEntryData.class);
                    entriesQuery.add(
                            Expression.eq("category", assoc.getCategory()));
                    Iterator entryIter = entriesQuery.list().iterator();
                    while (entryIter.hasNext()) {
                        WeblogEntryData entry = (WeblogEntryData)entryIter.next();
                        entries.add(entry);
                    }
                }
            }
            
            // Get entries in category
            Criteria entriesQuery =
                    session.createCriteria(WeblogEntryData.class);
            entriesQuery.add(Expression.eq("category", cat));
            Iterator entryIter = entriesQuery.list().iterator();
            while (entryIter.hasNext()) {
                WeblogEntryData entry = (WeblogEntryData)entryIter.next();
                entries.add(entry);
            }
            return entries;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    public String createAnchor(WeblogEntryData entry) throws RollerException {
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
                Criteria criteria = session.createCriteria(WeblogEntryData.class);
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
            throw new RollerException(e);
        }
    }
    
    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData cat)
    throws RollerException {
        // ensure that no sibling categories share the same name
        WeblogCategoryData parent =
                null == cat.getId() ? (WeblogCategoryData)cat.getNewParent() : cat.getParent();
        
        if (null != parent) // don't worry about root
        {
            List sameNames;
            try {
                Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
                Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
                criteria.createAlias("category", "c");
                criteria.add(Expression.ne("c.id", cat.getId()));
                criteria.add(Expression.eq("c.name", cat.getName()));
                criteria.add(Expression.eq("ancestorCategory", parent));
                criteria.add(Expression.eq("relation", Assoc.PARENT));
                sameNames = criteria.list();
            } catch (HibernateException e) {
                throw new RollerException(e);
            }
            if (sameNames.size() > 0) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isWeblogCategoryInUse(WeblogCategoryData cat)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.add(Expression.eq("category", cat));
            criteria.setMaxResults(1);
            int entryCount = criteria.list().size();
            
            if (entryCount > 0) {
                return true;
            }
            
            Iterator cats = cat.getWeblogCategories().iterator();
            while (cats.hasNext()) {
                WeblogCategoryData childCat = (WeblogCategoryData)cats.next();
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
            throw new RollerException(e);
        }
    }
    
    public boolean isDescendentOf(
            WeblogCategoryData child, WeblogCategoryData ancestor)
            throws RollerException {
        boolean ret = false;
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("category", child));
            criteria.add(Expression.eq("ancestorCategory", ancestor));
            ret = criteria.list().size() > 0;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return ret;
    }
    
    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData cat)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("category", cat));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            List parents = criteria.list();
            if (parents.size() > 1) {
                throw new RollerException("ERROR: more than one parent");
            } else if (parents.size() == 1) {
                return (Assoc) parents.get(0);
            } else {
                return null;
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogCategoryChildAssocs(WeblogCategoryData cat)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("ancestorCategory", cat));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData cat)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("ancestorCategory", cat));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData cat)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("category", cat));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
                
    public List getComments(
            WebsiteData     website,
            WeblogEntryData entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            Boolean         pending,
            Boolean         approved,
            Boolean         spam,
            boolean         reverseChrono,
            int             offset,
            int             length
            ) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(CommentData.class);
            
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
            
            if (pending != null) {
                criteria.add(Expression.eq("pending", pending));
            }
            
            if (approved != null) {
                criteria.add(Expression.eq("approved", approved));
            }
            
            if (spam != null) {
                criteria.add(Expression.eq("spam", spam));
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
            throw new RollerException(e);
        }
    }
    
    public int removeMatchingComments(
            WebsiteData     website, 
            WeblogEntryData entry, 
            String  searchString, 
            Date    startDate, 
            Date    endDate, 
            Boolean pending, 
            Boolean approved, 
            Boolean spam) throws RollerException {

        try {
            List comments = getComments( 
                website, entry, searchString, startDate, endDate, 
                pending, approved, spam, true, 0, -1);
            int count = 0;
            for (Iterator it = comments.iterator(); it.hasNext();) {
                CommentData comment = (CommentData) it.next();
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
                "delete CommentData cmt where cmt.id in "
              + "(select c.id from CommentData as c where ");
                
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
            throw new RollerException(e);
        }
    }
    
    
    public WeblogCategoryData getWeblogCategory(String id)
    throws RollerException {
        return (WeblogCategoryData) this.strategy.load(
                id,
                WeblogCategoryData.class);
    }
    
    //--------------------------------------------- WeblogCategoryData Queries
    
    public WeblogCategoryData getWeblogCategoryByPath(
            WebsiteData website, String categoryPath) throws RollerException {
        return getWeblogCategoryByPath(website, null, categoryPath);
    }
    
    public String getPath(WeblogCategoryData category) throws RollerException {
        if (null == category.getParent()) {
            return "/";
        } else {
            String parentPath = getPath(category.getParent());
            parentPath = "/".equals(parentPath) ? "" : parentPath;
            return parentPath + "/" + category.getName();
        }
    }
    
    public WeblogCategoryData getWeblogCategoryByPath(
            WebsiteData website, WeblogCategoryData category, String path)
            throws RollerException {
        final Iterator cats;
        final String[] pathArray = Utilities.stringToStringArray(path, "/");
        
        if (category == null && (null == path || "".equals(path.trim()))) {
            throw new RollerException("Bad arguments.");
        }
        
        if (path.trim().equals("/")) {
            return getRootWeblogCategory(website);
        } else if (category == null || path.trim().startsWith("/")) {
            cats = getRootWeblogCategory(website).getWeblogCategories().iterator();
        } else {
            cats = category.getWeblogCategories().iterator();
        }
        
        while (cats.hasNext()) {
            WeblogCategoryData possibleMatch = (WeblogCategoryData)cats.next();
            if (possibleMatch.getName().equals(pathArray[0])) {
                if (pathArray.length == 1) {
                    return possibleMatch;
                } else {
                    String[] subpath = new String[pathArray.length - 1];
                    System.arraycopy(pathArray, 1, subpath, 0, subpath.length);
                    
                    String pathString= Utilities.stringArrayToString(subpath,"/");
                    return getWeblogCategoryByPath(website, possibleMatch, pathString);
                }
            }
        }
        
        // The category did not match and neither did any sub-categories
        return null;
    }
        
    public CommentData getComment(String id) throws RollerException {
        return (CommentData) this.strategy.load(id, CommentData.class);
    }
        
    public WeblogEntryData getWeblogEntry(String id)
    throws RollerException {
        return (WeblogEntryData) this.strategy.load(
                id, WeblogEntryData.class);
    }
            
    /**
     * Gets the Date of the latest Entry publish time, before the end of today,
     * for all WeblogEntries
     */
    public Date getWeblogLastPublishTime(WebsiteData website)
    throws RollerException {
        return getWeblogLastPublishTime(website, null);
    }
    
    public Map getWeblogEntryObjectMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,            
            String  status,
            String  locale,
            int     offset,
            int     length) throws RollerException {
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
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,            
            String  status,
            String  locale,
            int     offset,
            int     length
            ) throws RollerException {
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
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,
            String  status,
            boolean stringsOnly,
            String  locale,
            int     offset,
            int     length
            ) throws RollerException {
        
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
            locale,             
            offset,
            length);
        
        Calendar cal = Calendar.getInstance();
        if (website != null) {
            cal.setTimeZone(website.getTimeZoneInstance());
        }
        
        SimpleDateFormat formatter = DateUtil.get8charDateFormat();
        for (Iterator wbItr = entries.iterator(); wbItr.hasNext();) {
            WeblogEntryData entry = (WeblogEntryData) wbItr.next();
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
            WebsiteData website, Date startDate, Date endDate, int offset, int length) 
            throws RollerException {
        // TODO: ATLAS getMostCommentedWeblogEntries DONE
        String msg = "Getting most commented weblog entres";
        if (endDate == null) endDate = new Date();
        try {      
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();            
            Query query = null;
            if (website != null) {
                StringBuffer sb = new StringBuffer();
                sb.append("select count(distinct c), c.weblogEntry.id, c.weblogEntry.anchor, c.weblogEntry.title from CommentData c ");
                sb.append("where c.weblogEntry.website=:website and c.weblogEntry.pubTime < :endDate ");
                if (startDate != null) {
                    sb.append("and c.weblogEntry.pubTime > :startDate ");
                }                   
                sb.append("group by c.weblogEntry.id, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("order by col_0_0_ desc");
                query = session.createQuery(sb.toString());
                query.setParameter("website", website);
                query.setParameter("endDate", endDate);
                if (startDate != null) {
                    query.setParameter("startDate", startDate);
                }   
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("select count(distinct c), c.weblogEntry.id, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("from CommentData c group by c.weblogEntry.id, c.weblogEntry.anchor, c.weblogEntry.title ");
                sb.append("where c.weblogEntry.pubTime < :endDate ");
                if (startDate != null) {
                    sb.append("and c.weblogEntry.pubTime > :startDate ");
                } 
                sb.append("order by col_0_0_ desc");
                query = session.createQuery(sb.toString());
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
                results.add(new StatCount(
                    (String)row[1], 
                    (String)row[2], 
                    (String)row[3], 
                    "statCount.weblogEntryCommentCountType", 
                    new Long(((Integer)row[0]).intValue()).longValue()));
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new RollerException(msg, pe);
        }
    }
    
    
    public List getNextEntries(WeblogEntryData current, String catName, 
                               String locale, int maxEntries)
            throws RollerException {
        
        return getNextPrevEntries(current, catName, locale, maxEntries, true);
    }
    
    
    public List getPreviousEntries(WeblogEntryData current, String catName, 
                                   String locale, int maxEntries)
            throws RollerException {
        
        return getNextPrevEntries(current, catName, locale, maxEntries, false);
    }
    
    
    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName,
                                        String locale)
            throws RollerException {
        
        WeblogEntryData entry = null;
        List entryList = getNextEntries(current, catName, locale, 1);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }
    
    
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, String catName,
                                            String locale)
            throws RollerException {
        
        WeblogEntryData entry = null;
        List entryList = getPreviousEntries(current, catName, locale, 1);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }
    
    
    public void release() {}

    /**
     * Apply comment defaults (defaultAllowComments and defaultCommentDays) to
     * all existing entries in a website using a single HQL query.
     * @param website Website where comment defaults are from/to be applied.
     */
    public void applyCommentDefaultsToEntries(WebsiteData website) throws RollerException {
        if (log.isDebugEnabled()) {
            log.debug("applyCommentDefaults");
        }       
        try {
            Session session = strategy.getSession();
            String updateString = "update WeblogEntryData set "
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
     * @see org.apache.roller.model.WeblogManager#getPopularTags(org.apache.roller.pojos.WebsiteData, java.util.Date, int)
     */
    public List getPopularTags(WebsiteData website, Date startDate, int limit)
            throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy)
                    .getSession();

            ArrayList params = new ArrayList();
            StringBuffer queryString = new StringBuffer();
            queryString.append("select name, sum(total) ");
            queryString.append("from WeblogEntryTagAggregateData where ");
            if (website != null) {
                queryString.append("website.id = ? ");
                params.add(website.getId());
            } else {
                queryString.append("website = NULL ");
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
                t.setCount(((Integer) row[1]).intValue());                
                
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
            throw new RollerException(e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.roller.model.WeblogManager#getTags(org.apache.roller.pojos.WebsiteData,
     *      java.lang.String, java.lang.String, int)
     */
    public List getTags(WebsiteData website, String sortBy, String startsWith, int limit) throws RollerException {    
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
            queryString.append("from WeblogEntryTagAggregateData where ");
            if (website != null)
                queryString.append("website.id = '" + website.getId() + "' ");
            else
                queryString.append("website = NULL ");
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
                ce.setCount(((Integer) row[1]).intValue());
                results.add(ce);
            }

            return results;

        } catch (HibernateException e) {
            throw new RollerException(e);
        }

    }
    
    public void updateTagCount(String name, WebsiteData website, int amount) throws RollerException {
        
        Session session = ((HibernatePersistenceStrategy) strategy)
        .getSession();
        
        if(amount == 0) {
            throw new RollerException("Tag increment amount cannot be zero.");
        }
        
        if(website == null) {
            throw new RollerException("Website cannot be NULL.");
        }
                        
        Junction conjunction = Expression.conjunction();
        conjunction.add(Expression.eq("name", name));
        conjunction.add(Expression.eq("website", website));

        // The reason why add order lastUsed desc is to make sure we keep picking the most recent
        // one in the case where we have multiple rows (clustered environment)
        // eventually that second entry will have a very low total (most likely 1) and
        // won't matter
        
        Criteria criteria = session.createCriteria(WeblogEntryTagAggregateData.class)
            .add(conjunction).addOrder(Order.desc("lastUsed")).setMaxResults(1);
        
        WeblogEntryTagAggregateData weblogTagData = (WeblogEntryTagAggregateData) criteria.uniqueResult();

        conjunction = Expression.conjunction();
        conjunction.add(Restrictions.eq("name", name));
        conjunction.add(Restrictions.isNull("website"));
        
        criteria = session.createCriteria(WeblogEntryTagAggregateData.class)
            .add(conjunction).addOrder(Order.desc("lastUsed")).setMaxResults(1);
    
        WeblogEntryTagAggregateData siteTagData = (WeblogEntryTagAggregateData) criteria.uniqueResult();
        
        Timestamp lastUsed = new Timestamp((new Date()).getTime());
        
        // create it only if we are going to need it.
        if(weblogTagData == null && amount > 0) {
            weblogTagData = new WeblogEntryTagAggregateData(null, website, name, amount);
            weblogTagData.setLastUsed(lastUsed);
            session.save(weblogTagData);
        } else if(weblogTagData != null) {
            session.createQuery("update WeblogEntryTagAggregateData set total = total + ?, lastUsed = current_timestamp() where name = ? and website = ?")
            .setInteger(0, amount)
            .setString(1, weblogTagData.getName())
            .setParameter(2, website)
            .executeUpdate();
        }
        
        // create it only if we are going to need it.        
        if(siteTagData == null && amount > 0) {
            siteTagData = new WeblogEntryTagAggregateData(null, null, name, amount);
            siteTagData.setLastUsed(lastUsed);
            session.save(siteTagData);
        } else if(siteTagData != null) {
            session.createQuery("update WeblogEntryTagAggregateData set total = total + ?, lastUsed = current_timestamp() where name = ? and website is null")
            .setInteger(0, amount)
            .setString(1, siteTagData.getName())
            .executeUpdate();            
        }       
        
        // delete all bad counts
        session.createQuery("delete from WeblogEntryTagAggregateData where total <= 0").executeUpdate();
    }    
}
