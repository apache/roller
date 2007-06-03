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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.sql.Timestamp;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogHitCount;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.TagStatComparator;
import org.apache.roller.weblogger.pojos.TagStatCountComparator;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntryAttribute;
import org.apache.roller.weblogger.pojos.StatCountCountComparator;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.util.DateUtil;

/*
 * JPAWeblogManagerImpl.java
 *
 * Created on May 31, 2006, 4:08 PM
 *
 */
public class JPAWeblogManagerImpl implements WeblogManager {
    
    protected static Log log = LogFactory.getLog(
            JPAWeblogManagerImpl.class);
    
    protected JPAPersistenceStrategy strategy;
    
    // cached mapping of entryAnchors -> entryIds
    private Hashtable entryAnchorToIdMap = new Hashtable();
    
    /* inline creation of reverse comparator, anonymous inner class */
    private static final Comparator reverseComparator = new ReverseComparator();
    
    private static final Comparator tagStatNameComparator = new TagStatComparator();
    
    private static final Comparator tagStatCountReverseComparator =
            Collections.reverseOrder(TagStatCountComparator.getInstance());
    
    private static final Comparator statCountCountReverseComparator =
            Collections.reverseOrder(StatCountCountComparator.getInstance());
    
    public JPAWeblogManagerImpl
            (JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Weblog Manager");
        
        this.strategy = strategy;
    }
    
    /**
     * @inheritDoc
     */
    public void saveWeblogCategory(WeblogCategory cat) throws WebloggerException {
        boolean exists = getWeblogCategory(cat.getId()) != null;
        if (!exists) {
            if (isDuplicateWeblogCategoryName(cat)) {
                throw new WebloggerException("Duplicate category name, cannot save category");
            }
            // Newly added object. If it has a parent,
            // maintain relationship from both sides
            WeblogCategory parent = cat.getParent();
            if(parent != null) {
                parent.getWeblogCategories().add(cat);
            }
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());        
        this.strategy.store(cat);
    }
    
    /**
     * @inheritDoc
     */
    public void removeWeblogCategory(WeblogCategory cat)
    throws WebloggerException {
        if(cat.retrieveWeblogEntries(true).size() > 0) {
            throw new WebloggerException("Cannot remove category with entries");
        }
        
        // remove cat
        this.strategy.remove(cat);
        //relationship management for the other side
        WeblogCategory parent = cat.getParent();
        if(parent != null) {
            parent.getWeblogCategories().remove(cat);
        }
        
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
        RollerFactory.getRoller().getUserManager().saveWebsite(
                cat.getWebsite());
    }
    
    /**
     * @inheritDoc
     */
    public void moveWeblogCategory(WeblogCategory srcCat, WeblogCategory destCat)
    throws WebloggerException {
        
        // TODO: this check should be made before calling this method?
        if (destCat.descendentOf(srcCat)) {
            throw new WebloggerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        log.debug("Moving category "+srcCat.getPath() +
                " under "+destCat.getPath());
        
        
        WeblogCategory oldParent = srcCat.getParent();
        if(oldParent != null) {
            oldParent.getWeblogCategories().remove(srcCat);
        }
        srcCat.setParent(destCat);
        destCat.getWeblogCategories().add(srcCat);
        
        if("/".equals(destCat.getPath())) {
            srcCat.setPath("/"+srcCat.getName());
        } else {
            srcCat.setPath(destCat.getPath() + "/" + srcCat.getName());
        }
        saveWeblogCategory(srcCat);
        
        // the main work to be done for a category move is to update the
        // path attribute of the category and all descendent categories
        updatePathTree(srcCat);
    }
    
    
    // updates the paths of all descendents of the given category
    private void updatePathTree(WeblogCategory cat)
    throws WebloggerException {
        
        log.debug("Updating path tree for category "+cat.getPath());
        
        WeblogCategory childCat = null;
        Iterator childCats = cat.getWeblogCategories().iterator();
        while(childCats.hasNext()) {
            childCat = (WeblogCategory) childCats.next();
            
            log.debug("OLD child category path was "+childCat.getPath());
            
            // update path and save
            if("/".equals(cat.getPath())) {
                childCat.setPath("/" + childCat.getName());
            } else {
                childCat.setPath(cat.getPath() + "/" + childCat.getName());
            }
            saveWeblogCategory(childCat);
            
            log.debug("NEW child category path is "+ childCat.getPath());
            
            // then make recursive call to update this cats children
            updatePathTree(childCat);
        }
    }
    
    /**
     * @inheritDoc
     */
    public void moveWeblogCategoryContents(WeblogCategory srcCat,
            WeblogCategory destCat)
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
        
        if (srcCat.getWebsite().getDefaultCategory().getId()
        .equals(srcCat.getId())
        || srcCat.getWebsite().getDefaultCategory().descendentOf(srcCat)) {
            srcCat.getWebsite().setDefaultCategory(destCat);
            this.strategy.store(srcCat.getWebsite());
        }
        
        if (srcCat.getWebsite().getBloggerCategory().getId()
        .equals(srcCat.getId())
        || srcCat.getWebsite().getBloggerCategory().descendentOf(srcCat)) {
            srcCat.getWebsite().setBloggerCategory(destCat);
            this.strategy.store(srcCat.getWebsite());
        }
    }
    
    /**
     * @inheritDoc
     */
    public void saveComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.store(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
        .saveWebsite(comment.getWeblogEntry().getWebsite());
    }
    
    /**
     * @inheritDoc
     */
    public void removeComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.remove(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
        .saveWebsite(comment.getWeblogEntry().getWebsite());
    }
    
    /**
     * @inheritDoc
     */
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
            RollerFactory.getRoller().getUserManager()
            .saveWebsite(entry.getWebsite());
        }
        
        if(entry.isPublished()) {
            // Queue applicable pings for this update.
            RollerFactory.getRoller().getAutopingManager()
            .queueApplicableAutoPings(entry);
        }
    }
    
    /**
     * @inheritDoc
     */
    public void removeWeblogEntry(WeblogEntry entry)
    throws WebloggerException {
        Query q = strategy.getNamedQuery("WeblogReferrer.getByWeblogEntry");
        q.setParameter(1, entry);
        List referers = q.getResultList();
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
                it.remove();
                this.strategy.remove(tag);
            }
        }
        
        // remove entry
        this.strategy.remove(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            RollerFactory.getRoller().getUserManager()
            .saveWebsite(entry.getWebsite());
        }
        
        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWebsite().getHandle()+":"+entry.getAnchor());
    }
    
    public List getNextPrevEntries(WeblogEntry current, String catName,
            String locale, int maxEntries, boolean next)
            throws WebloggerException {
        Query query = null;
        List results = null;
        WeblogCategory category = null;
        
        if (catName != null && !catName.trim().equals("/")) {
            category = getWeblogCategoryByPath(current.getWebsite(), null,
                    catName);
        }
                
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        StringBuffer whereClause = new StringBuffer();
        queryString.append("SELECT e FROM WeblogEntry e WHERE ");
                     
        params.add(size++, current.getWebsite());
        whereClause.append("e.website = ?" + size); 
        
        params.add(size++, WeblogEntry.PUBLISHED);
        whereClause.append(" AND e.status = ?" + size);
                
        if (next) {
            params.add(size++, current.getPubTime());
            whereClause.append(" AND e.pubTime > ?" + size);
        } else {
            params.add(size++, current.getPubTime());
            whereClause.append(" AND e.pubTime < ?" + size);
        }
        
        if (catName != null && !catName.trim().equals("/")) {
            category = getWeblogCategoryByPath(current.getWebsite(), catName);
            if (category != null) {
                params.add(size++, category);
                whereClause.append(" AND e.category = ?" + size);
            } else {
                throw new WebloggerException("Cannot find category: " + catName);
            } 
        }
        
        if(locale != null) {
            params.add(size++, locale + '%');
            whereClause.append(" AND e.locale like ?" + size);
        }
        
        if (next) {
            whereClause.append(" ORDER BY e.pubTime ASC");
        } else {
            whereClause.append(" ORDER BY e.pubTime DESC");
        }
        query = strategy.getDynamicQuery(queryString.toString() + whereClause.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        query.setMaxResults(maxEntries);
        
        return query.getResultList();
    }
    
    /**
     * @inheritDoc
     */
    public WeblogCategory getRootWeblogCategory(Weblog website)
    throws WebloggerException {
        if (website == null)
            throw new WebloggerException("website is null");
        
        Query q = strategy.getNamedQuery(
                "WeblogCategory.getByWebsite&ParentNull");
        q.setParameter(1, website);
        try {
            return (WeblogCategory)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @inheritDoc
     */
    public List getWeblogCategories(Weblog website, boolean includeRoot)
    throws WebloggerException {
        if (website == null)
            throw new WebloggerException("website is null");
        
        if (includeRoot) return getWeblogCategories(website);
        
        Query q = strategy.getNamedQuery(
                "WeblogCategory.getByWebsite&ParentNotNull");
        q.setParameter(1, website);
        return q.getResultList();
    }
    
    /**
     * @inheritDoc
     */
    public List getWeblogCategories(Weblog website)
    throws WebloggerException {
        if (website == null)
            throw new WebloggerException("website is null");
        
        Query q = strategy.getNamedQuery(
                "WeblogCategory.getByWebsite");
        q.setParameter(1, website);
        return q.getResultList();
    }
    
    /**
     * @inheritDoc
     */
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
        
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        
        //queryString.append("SELECT e FROM WeblogEntry e WHERE ");
        if (tags == null || tags.size()==0) {
            queryString.append("SELECT e FROM WeblogEntry e WHERE ");
        } else {
            queryString.append("SELECT e FROM WeblogEntry e JOIN e.tags t WHERE ");
            queryString.append("(");
            for(int i = 0; i < tags.size(); i++) {
                if (i != 0) queryString.append(" OR ");
                params.add(size++, tags.get(i));
                queryString.append(" t.name = ?").append(size);                
            }
            queryString.append(") AND ");
        }
        
        if (website != null) {
            params.add(size++, website.getId());
            queryString.append("e.website.id = ?").append(size);
        } else {
            params.add(size++, Boolean.TRUE);
            queryString.append("e.website.enabled = ?").append(size);
        }
        
        /*if (tags != null && tags.size() > 0) {
            // A JOIN with WeblogEntryTag in parent quert will cause a DISTINCT in SELECT clause
            // WeblogEntry has a clob field and many databases do not link DISTINCT for CLOB fields
            // Hence as a workaround using corelated EXISTS query.
            queryString.append(" AND EXISTS (SELECT t FROM WeblogEntryTag t WHERE "
                    + " t.weblogEntry = e AND t.name IN (");
            final String PARAM_SEPERATOR = ", ";
            for(int i = 0; i < tags.size(); i++) {
                params.add(size++, tags.get(i));
                queryString.append("?").append(size).append(PARAM_SEPERATOR);
            }
            // Remove the trailing PARAM_SEPERATOR
            queryString.delete(queryString.length() - PARAM_SEPERATOR.length(),
                    queryString.length());

            // Close the brace FOR IN clause and EXIST clause
            queryString.append(" ) )");
        }*/

        if (user != null) {
            params.add(size++, user.getId());
            queryString.append(" AND e.creator.id = ?").append(size);
        }
        
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            params.add(size++, start);
            queryString.append(" AND e.pubTime >= ?").append(size);
        }
        
        if (endDate != null) {
            Timestamp end = new Timestamp(endDate.getTime());
            params.add(size++, end);
            queryString.append(" AND e.pubTime <= ?").append(size);
        }
        
        if (cat != null && website != null) {
            params.add(size++, cat.getId());
            queryString.append(" AND e.category.id = ?").append(size);
        }
                
        if (status != null) {
            params.add(size++, status);
            queryString.append(" AND e.status = ?").append(size);
        }
        
        if (locale != null) {
            params.add(size++, locale + '%');
            queryString.append(" AND e.locale like ?").append(size);
        }
        
        if (text != null) {
            params.add(size++, '%' + text + '%');
            queryString.append(" AND ( text LIKE ?").append(size);
            queryString.append("    OR summary LIKE ? ").append(size);
            queryString.append("    OR title LIKE ?").append(size);
            queryString.append(") ");
        }
        
        if (sortby != null && sortby.equals("updateTime")) {
            queryString.append(" ORDER BY e.updateTime ");
        } else {
            queryString.append(" ORDER BY e.pubTime ");
        }
        
        if (sortOrder != null && sortOrder.equals(ASCENDING)) {
            queryString.append("ASC ");
        } else {
            queryString.append("DESC ");
        }
        
        
        Query query = strategy.getDynamicQuery(queryString.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
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
     * @inheritDoc
     */
    public List getWeblogEntriesPinnedToMain(Integer max)
    throws WebloggerException {
        Query query = strategy.getNamedQuery(
                "WeblogEntry.getByPinnedToMain&statusOrderByPubTimeDesc");
        query.setParameter(1, Boolean.TRUE);
        query.setParameter(2, WeblogEntry.PUBLISHED);
        if (max != null) {
            query.setMaxResults(max.intValue());
        }
        return query.getResultList();
    }
    
    public void removeWeblogEntryAttribute(String name, WeblogEntry entry)
    throws WebloggerException {
        for (Iterator it = entry.getEntryAttributes().iterator(); it.hasNext();) {
            WeblogEntryAttribute entryAttribute = (WeblogEntryAttribute) it.next();
            if (entryAttribute.getName().equals(name)) {
                //Call back the entity to adjust its internal state
                entry.onRemoveEntryAttribute(entryAttribute);
                //Remove it from database
                this.strategy.remove(entryAttribute);
                //Remove it from the collection
                it.remove();
            }
        }
    }
    
    public void removeWeblogEntryTag(String name, WeblogEntry entry)
    throws WebloggerException {
        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTag tag = (WeblogEntryTag) it.next();
            if (tag.getName().equals(name)) {
                //Call back the entity to adjust its internal state
                entry.onRemoveTag(name);
                //Remove it from database
                this.strategy.remove(tag);
                //Remove it from the collection
                it.remove();
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getWeblogEntryByAnchor(Weblog website,
            String anchor) throws WebloggerException {
        
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
        Query q = strategy.getNamedQuery(
                "WeblogEntry.getByWebsite&AnchorOrderByPubTimeDesc");
        q.setParameter(1, website);
        q.setParameter(2, anchor);
        WeblogEntry entry = null;
        try {
            entry = (WeblogEntry)q.getSingleResult();
        } catch (NoResultException e) {
            entry = null;
        }
        
        // add mapping to cache
        if(entry != null) {
            log.debug("entryAnchorToIdMap CACHE MISS - "+mappingKey);
            this.entryAnchorToIdMap.put(mappingKey, entry.getId());
        }
        return entry;
    }
    
    /**
     * @inheritDoc
     */
    // TODO: this method should be removed and it's functionality moved to getWeblogEntries()
    public List getWeblogEntries(WeblogCategory cat, boolean subcats)
    throws WebloggerException {
        List results = null;
        
        if (!subcats) {
            Query q = strategy.getNamedQuery(
                    "WeblogEntry.getByStatus&Category");
            q.setParameter(1, WeblogEntry.PUBLISHED);
            q.setParameter(2, cat);
            results = q.getResultList();
        } else {
            Query q = strategy.getNamedQuery(
                    "WeblogEntry.getByStatus&Category.pathLike&Website");
            q.setParameter(1, WeblogEntry.PUBLISHED);
            q.setParameter(2, cat.getPath() + '%');
            q.setParameter(3, cat.getWebsite());
            results = q.getResultList();
        }
        
        return results;
    }
    
    /**
     * @inheritDoc
     */
    public String createAnchor(WeblogEntry entry) throws WebloggerException {
        // Check for uniqueness of anchor
        String base = entry.createAnchorBase();
        String name = base;
        int count = 0;
        
        while (true) {
            if (count > 0) {
                name = base + count;
            }
            
            Query q = strategy.getNamedQuery(
                    "WeblogEntry.getByWebsite&Anchor");
            q.setParameter(1, entry.getWebsite());
            q.setParameter(2, name);
            List results = q.getResultList();
            
            if (results.size() < 1) {
                break;
            } else {
                count++;
            }
        }
        return name;
    }
    
    /**
     * @inheritDoc
     */
    public boolean isDuplicateWeblogCategoryName(WeblogCategory cat)
    throws WebloggerException {
        
        // ensure that no sibling categories share the same name
        WeblogCategory parent = cat.getParent();
        if (null != parent) {
            return (getWeblogCategoryByPath(
                    cat.getWebsite(), cat.getPath()) != null);
        }
        
        return false;
    }
    
    /**
     * @inheritDoc
     */
    public boolean isWeblogCategoryInUse(WeblogCategory cat)
    throws WebloggerException {
        
        Query q = strategy.getNamedQuery("WeblogEntry.getByCategory");
        q.setParameter(1, cat);
        int entryCount = q.getResultList().size();
        
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
    }
    
    /**
     * @inheritDoc
     */
    public List getComments(
            Weblog     website,
            WeblogEntry entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            String          status,
            boolean         reverseChrono,
            int             offset,
            int             length) throws WebloggerException {
        
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT c FROM WeblogEntryComment c ");
        
        StringBuffer whereClause = new StringBuffer();
        if (entry != null) {
            params.add(size++, entry);
            whereClause.append("c.weblogEntry = ?").append(size);
        } else if (website != null) {
            params.add(size++, website);
            whereClause.append("c.weblogEntry.website = ?").append(size);
        }
        
        if (searchString != null) {
            params.add(size++, "%" + searchString + "%");
            appendConjuctionToWhereclause(whereClause, "(c.url LIKE ?")
            .append(size).append(" OR c.content LIKE ?").append(size).append(")");
        }
        
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            params.add(size++, start);
            appendConjuctionToWhereclause(whereClause, "c.postTime >= ?");
        }
        
        if (endDate != null) {
            Timestamp end = new Timestamp(endDate.getTime());
            params.add(size++, end);
            appendConjuctionToWhereclause(whereClause, "c.postTime <= ?");
        }
        
        if (status != null) {
            String comparisionOperator;
            if("ALL_IGNORE_SPAM".equals(status)) {
                // we want all comments, except spam
                // so that means where status != SPAM
                status = WeblogEntryComment.SPAM;
                comparisionOperator = " <> ";
            } else {
                comparisionOperator = " = ";
            }
            params.add(size++, status);
            appendConjuctionToWhereclause(whereClause, "c.status ")
            .append(comparisionOperator).append('?').append(size);
        }
        
        if(whereClause.length() != 0) {
            queryString.append(" WHERE ").append(whereClause);
        }
        if (reverseChrono) {
            queryString.append(" ORDER BY c.postTime DESC");
        } else {
            queryString.append(" ORDER BY c.postTime ASC");
        }
        
        Query query = strategy.getDynamicQuery(queryString.toString());
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
    
    
    /**
     * @inheritDoc
     */
    public int removeMatchingComments(
            Weblog     website,
            WeblogEntry entry,
            String  searchString,
            Date    startDate,
            Date    endDate,
            String status) throws WebloggerException {
        
        // TODO dynamic bulk delete query: I'd MUCH rather use a bulk delete,
        // but MySQL says "General error, message from server: "You can't
        // specify target table 'roller_comment' for update in FROM clause"
        
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
    }
    
    
    /**
     * @inheritDoc
     */
    public WeblogCategory getWeblogCategory(String id)
    throws WebloggerException {
        return (WeblogCategory) this.strategy.load(
                WeblogCategory.class, id);
    }
    
    //--------------------------------------------- WeblogCategory Queries
    
    /**
     * @inheritDoc
     */
    public WeblogCategory getWeblogCategoryByPath(Weblog website,
            String categoryPath) throws WebloggerException {
        return getWeblogCategoryByPath(website, null, categoryPath);
    }
    
    /**
     * @inheritDoc
     */
    // TODO: ditch this method in favor of getWeblogCategoryByPath(weblog, path)
    public WeblogCategory getWeblogCategoryByPath(Weblog website,
            WeblogCategory category, String path) throws WebloggerException {
        
        if (path == null || path.trim().equals("/")) {
            return getRootWeblogCategory(website);
        } else {
            String catPath = path;
            
            // all cat paths must begin with a '/'
            if(!catPath.startsWith("/")) {
                catPath = "/"+catPath;
            }
            
            // now just do simple lookup by path
            Query q = strategy.getNamedQuery(
                    "WeblogCategory.getByPath&Website");
            q.setParameter(1, catPath);
            q.setParameter(2, website);
            try {
                return (WeblogCategory)q.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntryComment getComment(String id) throws WebloggerException {
        return (WeblogEntryComment) this.strategy.load(WeblogEntryComment.class, id);
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getWeblogEntry(String id) throws WebloggerException {
        return (WeblogEntry)strategy.load(WeblogEntry.class, id);
    }
    
    /**
     * @inheritDoc
     */
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
    
    /**
     * @inheritDoc
     */
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
                length);
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
            int     length) throws WebloggerException {
        
        TreeMap map = new TreeMap(reverseComparator);
        
        List entries = getWeblogEntries( 
                website,
                null, // user
                startDate,
                endDate,
                catName,
                tags,
                status,
                null, // text
                null, // sortBy
                null, // sortOrder
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
    
    /**
     * @inheritDoc
     */
    public List getMostCommentedWeblogEntries(Weblog website,
            Date startDate, Date endDate, int offset,
            int length) throws WebloggerException {
        Query query = null;
        List queryResults = null;        
        if (endDate == null) endDate = new Date();
        
        if (website != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByWebsite&EndDate&StartDate");
                query.setParameter(1, website);
                query.setParameter(2, end);
                query.setParameter(3, start);
            } else {
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByWebsite&EndDate");
                query.setParameter(1, website);
                query.setParameter(2, end);
            }
        } else {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByEndDate&StartDate");
                query.setParameter(1, end);
                query.setParameter(2, start);
            } else {
                Timestamp end = new Timestamp(endDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByEndDate");
                query.setParameter(1, end);
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        queryResults = query.getResultList();
        List results = new ArrayList();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            results.add(new StatCount(
                    (String)row[1],                             // entry id
                    (String)row[2],                             // entry anchor
                    (String)row[3],                             // entry title
                    "statCount.weblogEntryCommentCountType",    // stat desc
                    ((Long)row[0]).longValue())); // count
        }
        // Original query ordered by desc count.
        // JPA QL doesn't allow queries to be ordered by agregates; do it in memory
        Collections.sort(results, statCountCountReverseComparator);
        
        return results;
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getNextEntry(WeblogEntry current,
            String catName, String locale) throws WebloggerException {
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, locale, 1, true);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getPreviousEntry(WeblogEntry current,
            String catName, String locale) throws WebloggerException {
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, locale, 1, false);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }
    
    /**
     * @inheritDoc
     */
    public void release() {}
    
    /**
     * @inheritDoc
     */
    public void applyCommentDefaultsToEntries(Weblog website)
    throws WebloggerException {
        if (log.isDebugEnabled()) {
            log.debug("applyCommentDefaults");
        }
        
        // TODO: Non-standard JPA bulk update, using parameter values in set clause
        Query q = strategy.getNamedUpdate(
                "WeblogEntry.updateAllowComments&CommentDaysByWebsite");
        q.setParameter(1, website.getDefaultAllowComments());
        q.setParameter(2, new Integer(website.getDefaultCommentDays()));
        q.setParameter(3, website);
        q.executeUpdate();
    }
    
    /**
     * @inheritDoc
     */
    public List getPopularTags(Weblog website, Date startDate, int limit)
    throws WebloggerException {
        Query query = null;
        List queryResults = null;
        
        if (website != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryTagAggregate.getPopularTagsByWebsite&StartDate");
                query.setParameter(1, website);
                query.setParameter(2, start);
            } else {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryTagAggregate.getPopularTagsByWebsite");
                query.setParameter(1, start);
            }
        } else {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryTagAggregate.getPopularTagsByWebsiteNull&StartDate");
                query.setParameter(1, start);
            } else {
                query = strategy.getNamedQuery(
                        "WeblogEntryTagAggregate.getPopularTagsByWebsiteNull");
            }
        }
        if (limit != -1) {
            query.setMaxResults(limit);
        }
        queryResults = query.getResultList();
        
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        
        List results = new ArrayList(limit);
        
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
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
        
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            TagStat t = (TagStat) iter.next();
            t.setIntensity((int) (1 + Math.floor(5 * (Math.log(1+t.getCount()) - min) / range)));
        }
        
        // sort results by name, because query had to sort by total
        Collections.sort(results, tagStatNameComparator);
        
        return results;
    }
    
    /**
     * @inheritDoc
     */
    public List getTags(Weblog website, String sortBy,
            String startsWith, int limit) throws WebloggerException {
        Query query = null;
        List queryResults = null;
        boolean sortByName = sortBy == null || !sortBy.equals("count");
                
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();            
        queryString.append("SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE ");
                
        if (website != null) {
            params.add(size++, website.getId());
            queryString.append(" w.weblog.id = ?").append(size);
        } else {
            queryString.append(" w.weblog IS NULL"); 
        }
                       
        if (startsWith != null && startsWith.length() > 0) {
            params.add(size++, startsWith + '%');
            queryString.append(" AND w.name LIKE ?" + size);
        }
                    
        if (sortBy != null && sortBy.equals("count")) {
            sortBy = "w.total DESC";
        } else {
            sortBy = "w.name";
        }
        queryString.append(" GROUP BY w.name, w.total ORDER BY " + sortBy);

        query = strategy.getDynamicQuery(queryString.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        if (limit != -1) {
            query.setMaxResults(limit);
        }
        queryResults = query.getResultList();
        
        List results = new ArrayList();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            TagStat ce = new TagStat();
            ce.setName((String) row[0]);
            // The JPA query retrieves SUM(w.total) always as long
            ce.setCount(((Long) row[1]).intValue());
            results.add(ce);
        }
        
        if (sortByName) {
            Collections.sort(results, tagStatNameComparator);
        } else {
            Collections.sort(results, tagStatCountReverseComparator);
        }
        
        return results;
    }
    
    
    /**
     * @inheritDoc
     */
    public boolean getTagComboExists(List tags, Weblog weblog) throws WebloggerException{
        
        if(tags == null || tags.size() == 0) {
            return false;
        }
        
        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT DISTINCT w.name ");
        queryString.append("FROM WeblogEntryTagAggregate w WHERE w.name IN (");
        //?1) AND w.weblog = ?2");
        //Append tags as parameter markers to avoid potential escaping issues
        //The IN clause would be of form (?1, ?2, ?3, ..)
        ArrayList params = new ArrayList(tags.size() + 1);
        final String PARAM_SEPERATOR = ", ";
        int i;
        for (i=0; i < tags.size(); i++) {
            queryString.append('?').append(i+1).append(PARAM_SEPERATOR);
            params.add(tags.get(i));
        }
        
        // Remove the trailing PARAM_SEPERATOR
        queryString.delete(queryString.length() - PARAM_SEPERATOR.length(),
                queryString.length());
        // Close the brace of IN clause
        queryString.append(')');
        
        if(weblog != null) {
            queryString.append(" AND w.weblog = ?").append(i+1);
            params.add(weblog);
        } else {
            queryString.append(" AND w.weblog IS NULL");
        }
        
        Query q = strategy.getDynamicQuery(queryString.toString());
        for (int j=0; j<params.size(); j++) {
            q.setParameter(j+1, params.get(j));
        }
        List results = q.getResultList();
        
        //TODO: DatamapperPort: Since we are only interested in knowing whether
        //results.size() == tags.size(). This query can be optimized to just fetch COUNT
        //instead of objects as done currently
        return (results != null && results.size() == tags.size());
    }
    
    /**
     * @inheritDoc
     */
    public void updateTagCount(String name, Weblog website, int amount)
    throws WebloggerException {
        if(amount == 0) {
            throw new WebloggerException("Tag increment amount cannot be zero.");
        }
        
        if(website == null) {
            throw new WebloggerException("Website cannot be NULL.");
        }
        
        // The reason why add order lastUsed desc is to make sure we keep picking the most recent
        // one in the case where we have multiple rows (clustered environment)
        // eventually that second entry will have a very low total (most likely 1) and
        // won't matter
        Query weblogQuery = strategy.getNamedQuery(
                "WeblogEntryTagAggregate.getByName&WebsiteOrderByLastUsedDesc");
        weblogQuery.setParameter(1, name);
        weblogQuery.setParameter(2, website);
        WeblogEntryTagAggregate weblogTagData;
        try {
            weblogTagData = (WeblogEntryTagAggregate)weblogQuery.getSingleResult();
        } catch (NoResultException e) {
            weblogTagData = null;
        }
        
        Query siteQuery = strategy.getNamedQuery(
                "WeblogEntryTagAggregate.getByName&WebsiteNullOrderByLastUsedDesc");
        siteQuery.setParameter(1, name);
        WeblogEntryTagAggregate siteTagData;
        try {
            siteTagData = (WeblogEntryTagAggregate)siteQuery.getSingleResult();
        } catch (NoResultException e) {
            siteTagData = null;
        }
        Timestamp lastUsed = new Timestamp((new Date()).getTime());
        
        // create it only if we are going to need it.
        if (weblogTagData == null && amount > 0) {
            weblogTagData = new WeblogEntryTagAggregate(null, website, name, amount);
            weblogTagData.setLastUsed(lastUsed);
            strategy.store(weblogTagData);
            
        } else if (weblogTagData != null) {
            weblogTagData.setTotal(weblogTagData.getTotal() + amount);
            weblogTagData.setLastUsed(lastUsed);
            strategy.store(weblogTagData);
            // Why use update query when only one object needs update?
//            Query update = strategy.getNamedUpdate(
//                    "WeblogEntryTagAggregate.updateAddToTotalByName&Weblog");
//            update.setParameter(1, new Long(amount));
//            update.setParameter(2, lastUsed);
//            update.setParameter(3, weblogTagData.getName());
//            update.setParameter(4, website);
//            update.executeUpdate();
        }
        
        // create it only if we are going to need it.
        if (siteTagData == null && amount > 0) {
            siteTagData = new WeblogEntryTagAggregate(null, null, name, amount);
            siteTagData.setLastUsed(lastUsed);
            strategy.store(siteTagData);
            
        } else if(siteTagData != null) {
            siteTagData.setTotal(siteTagData.getTotal() + amount);
            siteTagData.setLastUsed(lastUsed);
            strategy.store(siteTagData);
            // Why use update query when only one object needs update?
//            Query update = strategy.getNamedUpdate(
//                    "WeblogEntryTagAggregate.updateAddToTotalByName&WeblogNull");
//            update.setParameter(1, new Long(amount));
//            update.setParameter(2, siteTagData.getName());
//            update.executeUpdate();
        }
        
        // delete all bad counts
        Query removeq = strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.removeByTotalLessEqual");
        removeq.setParameter(1, new Integer(0));
        removeq.executeUpdate();
    }
    
    /**
     * @inheritDoc
     */
    public WeblogHitCount getHitCount(String id) throws WebloggerException {
        
        // do lookup
        return (WeblogHitCount) strategy.load(WeblogHitCount.class, id);
    }
    
    /**
     * @inheritDoc
     */
    public WeblogHitCount getHitCountByWeblog(Weblog weblog)
    throws WebloggerException {
        Query q = strategy.getNamedQuery("WeblogHitCount.getByWeblog");
        q.setParameter(1, weblog);
        try {
            return (WeblogHitCount)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @inheritDoc
     */
    public List getHotWeblogs(int sinceDays, int offset, int length)
    throws WebloggerException {
        
        // figure out start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        
        Query query = strategy.getNamedQuery(
                "WeblogHitCount.getByWeblogEnabledTrueAndActiveTrue&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc");
        query.setParameter(1, startDate);
        
        // Was commented out due to https://glassfish.dev.java.net/issues/show_bug.cgi?id=2084
        // TODO: determine if this is still an issue. Is it a problem with other JPA implementations?
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }        
        return query.getResultList();
    }
    
    
    /**
     * @inheritDoc
     */
    public void saveHitCount(WeblogHitCount hitCount) throws WebloggerException {
        this.strategy.store(hitCount);
    }
    
    
    /**
     * @inheritDoc
     */
    public void removeHitCount(WeblogHitCount hitCount) throws WebloggerException {
        this.strategy.remove(hitCount);
    }
    
    
    /**
     * @inheritDoc
     */
    public void incrementHitCount(Weblog weblog, int amount)
    throws WebloggerException {
        
        if(amount == 0) {
            throw new WebloggerException("Tag increment amount cannot be zero.");
        }
        
        if(weblog == null) {
            throw new WebloggerException("Website cannot be NULL.");
        }
        
        Query q = strategy.getNamedQuery("WeblogHitCount.getByWeblog");
        q.setParameter(1, weblog);
        WeblogHitCount hitCount = null;
        try {
            hitCount = (WeblogHitCount)q.getSingleResult();
        } catch (NoResultException e) {
            hitCount = null;
        }
        
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
    
    /**
     * @inheritDoc
     */
    public void resetAllHitCounts() throws WebloggerException {       
        Query q = strategy.getNamedUpdate("WeblogHitCount.updateDailyHitCountZero");
        q.executeUpdate();
    }
    
    /**
     * @inheritDoc
     */
    public void resetHitCount(Weblog weblog) throws WebloggerException {
        Query q = strategy.getNamedQuery("WeblogHitCount.getByWeblog");
        q.setParameter(1, weblog);
        WeblogHitCount hitCount = null;
        try {
            hitCount = (WeblogHitCount)q.getSingleResult();
            hitCount.setDailyHits(0);
            strategy.store(hitCount);
        } catch (NoResultException e) {
            // ignore: no hit count for weblog
        }       

    }
    
    /**
     * @inheritDoc
     */
    public long getCommentCount() throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntryComment.getCountAllDistinctByStatus");
        q.setParameter(1, WeblogEntryComment.APPROVED);
        List results = q.getResultList();
        return ((Long)results.get(0)).longValue();
    }
    
    /**
     * @inheritDoc
     */
    public long getCommentCount(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntryComment.getCountDistinctByWebsite&Status");
        q.setParameter(1, website);
        q.setParameter(2, WeblogEntryComment.APPROVED);
        List results = q.getResultList();
        return ((Long)results.get(0)).longValue();
    }
    
    /**
     * @inheritDoc
     */
    public long getEntryCount() throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntry.getCountDistinctByStatus");
        q.setParameter(1, "PUBLISHED");
        List results = q.getResultList();
        return ((Long)results.get(0)).longValue();
    }
    
    /**
     * @inheritDoc
     */
    public long getEntryCount(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntry.getCountDistinctByStatus&Website");
        q.setParameter(1, "PUBLISHED");
        q.setParameter(2, website);
        List results = q.getResultList();
        return ((Long)results.get(0)).longValue();
    }
    
    /**
     * Appends given expression to given whereClause. If whereClause already
     * has other conditions, an " AND " is also appended before appending
     * the expression
     * @param whereClause The given where Clauuse
     * @param expression The given expression
     * @return the whereClause.
     */
    private static StringBuffer appendConjuctionToWhereclause(StringBuffer whereClause,
            String expression) {
        if(whereClause.length() != 0 && expression.length() != 0) {
            whereClause.append(" AND ");
        }
        return whereClause.append(expression);
    }
    
}
