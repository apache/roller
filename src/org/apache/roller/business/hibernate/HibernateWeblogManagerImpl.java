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
/*
 * Created on Jun 16, 2004
 */
package org.apache.roller.business.hibernate;

import java.text.SimpleDateFormat;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.WeblogCategoryAssoc;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.StringUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.Utilities;


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
    
    private SimpleDateFormat formatter = DateUtil.get8charDateFormat();
    
    
    public HibernateWeblogManagerImpl(HibernatePersistenceStrategy strat) {
        log.debug("Instantiating Hibernate Weblog Manager");
        
        this.strategy = strat;
    }
    
    
    public void saveWeblogCategory(WeblogCategoryData cat) throws RollerException {
        
        if(this.isDuplicateWeblogCategoryName(cat)) {
            throw new RollerException("Duplicate category name");
        }
        
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
    }
    
    
    public void moveWeblogCategoryContents(String srcId, String destId)
            throws RollerException {
        
        WeblogCategoryData srcCd =
                (WeblogCategoryData) this.strategy.load(
                srcId, WeblogCategoryData.class);
        
        WeblogCategoryData destCd =
                (WeblogCategoryData) this.strategy.load(
                destId, WeblogCategoryData.class);
        
        // TODO: this check should be made before calling this method?
        if (destCd.descendentOf(srcCd)) {
            throw new RollerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        // get all entries in category and subcats
        List results = getWeblogEntries(srcCd, true);
        
        // Loop through entries in src cat, assign them to dest cat
        Iterator iter = results.iterator();
        WebsiteData website = destCd.getWebsite();
        while (iter.hasNext()) {
            WeblogEntryData entry = (WeblogEntryData) iter.next();
            entry.setCategory(destCd);
            entry.setWebsite(website);
            this.strategy.store(entry);
        }
        
        // Make sure website's default and bloggerapi categories
        // are valid after the move
        
        if (srcCd.getWebsite().getDefaultCategory().getId().equals(srcId)
        || srcCd.getWebsite().getDefaultCategory().descendentOf(srcCd)) {
            srcCd.getWebsite().setDefaultCategory(destCd);
            this.strategy.store(srcCd.getWebsite());
        }
        
        if (srcCd.getWebsite().getBloggerCategory().getId().equals(srcId)
        || srcCd.getWebsite().getBloggerCategory().descendentOf(srcCd)) {
            srcCd.getWebsite().setBloggerCategory(destCd);
            this.strategy.store(srcCd.getWebsite());
        }
    }
    
    
    public void saveComment(CommentData comment) throws RollerException {
        this.strategy.store(comment);
    }
    
    
    public void removeComment(CommentData comment) throws RollerException {
        this.strategy.remove(comment);
    }
    
    
    // TODO: perhaps the createAnchor() and queuePings() items should go outside this method?
    public void saveWeblogEntry(WeblogEntryData entry) throws RollerException {
        
        if (entry.getAnchor() == null || entry.getAnchor().trim().equals("")) {
            entry.setAnchor(this.createAnchor(entry));
        }
        
        this.strategy.store(entry);
        
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
        
        // remove entry
        this.strategy.remove(entry);
        
        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWebsite().getHandle()+":"+entry.getAnchor());
    }
    
    
    private void removeWeblogEntryContents(WeblogEntryData entry) throws RollerException {
        
        if(entry == null) {
            throw new RollerException("cannot remove null entry");
        }
        
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
    }
    
    
    public List getNextPrevEntries(
            WeblogEntryData current, String catName, int maxEntries, boolean next)
            throws RollerException {
        if (catName != null && catName.trim().equals("/")) {
            catName = null;
        }
        Junction conjunction = Expression.conjunction();
        conjunction.add(Expression.eq("website", current.getWebsite()));
        conjunction.add(Expression.eq("status", WeblogEntryData.PUBLISHED));
        
        if (next) {
            conjunction.add(Expression.gt("pubTime", current.getPubTime()));
        } else {
            conjunction.add(Expression.lt("pubTime", current.getPubTime()));
        }
        
        if (catName != null) {
            WeblogCategoryData category =
                    getWeblogCategoryByPath(current.getWebsite(), null, catName);
            if (category != null) {
                conjunction.add(Expression.eq("category", category));
            } else {
                throw new RollerException("Cannot find category: "+catName);
            }
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
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            String  sortby,
            Integer maxEntries) throws RollerException {
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
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            
            if (website != null) {
                criteria.add(Expression.eq("website", website));
            } else {
                criteria.createAlias("website","w");
                criteria.add(Expression.eq("w.enabled", Boolean.TRUE));
            }
            
            if (startDate != null) {
                criteria.add(
                        Expression.ge("pubTime", startDate));
            }
            
            if (endDate != null) {
                criteria.add(
                        Expression.le("pubTime", endDate));
            }
            
            if (cat != null && website != null) {
                criteria.add(Expression.eq("category", cat));
            }
            
            if (status != null) {
                criteria.add(Expression.eq("status", status));
            }
            
            if (sortby != null && sortby.equals("updateTime")) {
                criteria.addOrder(Order.desc("updateTime"));
            } else {
                criteria.addOrder(Order.desc("pubTime"));
            }
            
            if (maxEntries != null) {
                criteria.setMaxResults(maxEntries.intValue());
            }
            return criteria.list();
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
        Roller mRoller = RollerFactory.getRoller();
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
                criteria.add(Expression.eq("c.name", cat.getName()));
                criteria.add(Expression.eq("ancestorCategory", parent));
                criteria.add(Expression.eq("relation", Assoc.PARENT));
                sameNames = criteria.list();
            } catch (HibernateException e) {
                throw new RollerException(e);
            }
            if (sameNames.size() > 1) {
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
    
    
    public List getWeblogEntries(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            String  sortby,
            int     offset,
            int     range) throws RollerException {
        List filtered = new ArrayList();
        List entries = getWeblogEntries(
                website,
                startDate,
                endDate,
                catName,
                status,
                sortby,
                new Integer(offset + range));
        if (entries.size() < offset) {
            return entries;
        }
        for (int i=offset; i<entries.size(); i++) {
            filtered.add(entries.get(i));
        }
        return filtered;
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
            String  status,
            Integer maxEntries) throws RollerException {
        return getWeblogEntryMap(
                website,
                startDate,
                endDate,
                catName,
                status,
                maxEntries,
                false);
    }
    
    public Map getWeblogEntryStringMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            Integer maxEntries) throws RollerException {
        return getWeblogEntryMap(
                website,
                startDate,
                endDate,
                catName,
                status,
                maxEntries,
                true);
    }
    
    private Map getWeblogEntryMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            Integer maxEntries,
            boolean stringsOnly) throws RollerException {
        TreeMap map = new TreeMap(reverseComparator);
        
        List entries = getWeblogEntries(
                website,
                startDate,
                endDate,
                catName,
                status,
                null,
                maxEntries);
        
        Calendar cal = Calendar.getInstance();
        if (website != null) {
            cal.setTimeZone(website.getTimeZoneInstance());
        }
        
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
    
    public List getNextEntries(
            WeblogEntryData current, String catName, int maxEntries)
            throws RollerException {
        return getNextPrevEntries(current, catName, maxEntries, true);
    }
    
    public List getPreviousEntries(
            WeblogEntryData current, String catName, int maxEntries)
            throws RollerException {
        return getNextPrevEntries(current, catName, maxEntries, false);
    }
    
    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName)
    throws RollerException {
        WeblogEntryData entry = null;
        List entryList = getNextEntries(current, catName, 1);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }
    
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, String catName)
    throws RollerException {
        WeblogEntryData entry = null;
        List entryList = getPreviousEntries(current, catName, 1);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }
    
    /**
     * Get absolute URL to this website.
     * @return Absolute URL to this website.
     */
    public String getUrl(WebsiteData site, String contextUrl) {
        String url =
                Utilities.escapeHTML(contextUrl + "/page/" + site.getHandle());
        return url;
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
}
