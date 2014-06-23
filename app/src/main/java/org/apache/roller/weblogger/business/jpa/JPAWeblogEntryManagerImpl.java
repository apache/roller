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

import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogHitCount;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.TagStatComparator;
import org.apache.roller.weblogger.pojos.TagStatCountComparator;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntryAttribute;
import org.apache.roller.weblogger.pojos.StatCountCountComparator;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.business.WeblogEntryManager;


/**
 * JPAWeblogManagerImpl.java
 *
 * Created on May 31, 2006, 4:08 PM
 *
 */
@com.google.inject.Singleton
public class JPAWeblogEntryManagerImpl implements WeblogEntryManager {
    
    private static final Log LOG = LogFactory.getLog(JPAWeblogEntryManagerImpl.class);
    
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    
    // cached mapping of entryAnchors -> entryIds
    private Map<String, String> entryAnchorToIdMap = new HashMap<String, String>();
    
    /* inline creation of reverse comparator, anonymous inner class */
    private static final Comparator REVERSE_COMPARATOR = new ReverseComparator();
    
    private static final Comparator TAG_STAT_NAME_COMPARATOR = new TagStatComparator();
    
    private static final Comparator TAG_STAT_COUNT_REVERSE_COMPARATOR =
            Collections.reverseOrder(TagStatCountComparator.getInstance());
    
    private static final Comparator STAT_COUNT_COUNT_REVERSE_COMPARATOR =
            Collections.reverseOrder(StatCountCountComparator.getInstance());
    
    
    @com.google.inject.Inject
    protected JPAWeblogEntryManagerImpl(Weblogger roller, JPAPersistenceStrategy strategy) {
        LOG.debug("Instantiating JPA Weblog Manager");
        this.roller = roller;
        this.strategy = strategy;
    }
    
    /**
     * @inheritDoc
     */
    public void saveWeblogCategory(WeblogCategory cat) throws WebloggerException {
        boolean exists = getWeblogCategory(cat.getId()) != null;
        if (!exists && isDuplicateWeblogCategoryName(cat)) {
            throw new WebloggerException("Duplicate category name, cannot save category");
        }

        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(cat.getWeblog());
        this.strategy.store(cat);
    }
    
    /**
     * @inheritDoc
     */
    public void removeWeblogCategory(WeblogCategory cat)
    throws WebloggerException {
        if(cat.retrieveWeblogEntries(false).size() > 0) {
            throw new WebloggerException("Cannot remove category with entries");
        }

        cat.getWeblog().getWeblogCategories().remove(cat);

        // remove cat
        this.strategy.remove(cat);

        if(cat.equals(cat.getWeblog().getBloggerCategory())) {
            cat.getWeblog().setBloggerCategory(cat.getWeblog().getDefaultCategory());
            this.strategy.store(cat.getWeblog());
        }

        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(cat.getWeblog());
    }

    /**
     * @inheritDoc
     */
    public void moveWeblogCategoryContents(WeblogCategory srcCat,
            WeblogCategory destCat)
            throws WebloggerException {
        
        // get all entries in category and subcats
        List<WeblogEntry> results = srcCat.retrieveWeblogEntries(false);
        
        // Loop through entries in src cat, assign them to dest cat
        Weblog website = destCat.getWeblog();
        for (WeblogEntry entry : results) {
            entry.setCategory(destCat);
            entry.setWebsite(website);
            this.strategy.store(entry);
        }
        
        // Update Blogger API category if applicable
        WeblogCategory bloggerCategory = srcCat.getWeblog().getBloggerCategory();
        if (bloggerCategory != null && bloggerCategory.getId().equals(srcCat.getId())) {
            srcCat.getWeblog().setBloggerCategory(destCat);
            this.strategy.store(srcCat.getWeblog());
        }
    }
    
    /**
     * @inheritDoc
     */
    public void saveComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.store(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(comment.getWeblogEntry().getWebsite());
    }
    
    /**
     * @inheritDoc
     */
    public void removeComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.remove(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(comment.getWeblogEntry().getWebsite());
    }
    
    /**
     * @inheritDoc
     */
    // TODO: perhaps the createAnchor() and queuePings() items should go outside this method?
    public void saveWeblogEntry(WeblogEntry entry) throws WebloggerException {

        if (entry.getCategory() == null) {
            // Entry is invalid without category, so use weblog client cat
            WeblogCategory cat = entry.getWebsite().getBloggerCategory();
            if (cat == null) {
                // Still no category, so use first one found
                cat = entry.getWebsite().getWeblogCategories().iterator().next();
            }
            entry.setCategory(cat);
        }

        // Entry is invalid without local. if missing use weblog default
        if (entry.getLocale() == null) {
            entry.setLocale(entry.getWebsite().getLocale());
        }
        
        if (entry.getAnchor() == null || entry.getAnchor().trim().equals("")) {
            entry.setAnchor(this.createAnchor(entry));
        }
        
        for (Object name : entry.getAddedTags()) {
            updateTagCount((String) name, entry.getWebsite(), 1);
        }

        for (Object name : entry.getRemovedTags()) {
            updateTagCount((String) name, entry.getWebsite(), -1);
        }

        // if the entry was published to future, set status as SCHEDULED
        // we only consider an entry future published if it is scheduled
        // more than 1 minute into the future
        if (PubStatus.PUBLISHED.equals(entry.getStatus()) &&
                entry.getPubTime().after(new Date(System.currentTimeMillis() + RollerConstants.MIN_IN_MS))) {
            entry.setStatus(PubStatus.SCHEDULED);
        }
        
        // Store value object (creates new or updates existing)
        entry.setUpdateTime(new Timestamp(new Date().getTime()));
        
        this.strategy.store(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            roller.getWeblogManager().saveWeblog(entry.getWebsite());
        }
        
        if(entry.isPublished()) {
            // Queue applicable pings for this update.
            roller.getAutopingManager().queueApplicableAutoPings(entry);
        }
    }
    
    /**
     * @inheritDoc
     */
    public void removeWeblogEntry(WeblogEntry entry) throws WebloggerException {
        Weblog weblog = entry.getWebsite();
        
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setEntry(entry);

        // remove comments
        List<WeblogEntryComment> comments = getComments(csc);
        for (WeblogEntryComment comment : comments) {
            this.strategy.remove(comment);
        }
        
        // remove tags aggregates
        if (entry.getTags() != null) {
            for (Iterator it = entry.getTags().iterator(); it.hasNext(); ) {
                WeblogEntryTag tag = (WeblogEntryTag) it.next();
                updateTagCount(tag.getName(), entry.getWebsite(), -1);
                it.remove();
                this.strategy.remove(tag);
            }
        }
        
        // remove attributes
        if (entry.getEntryAttributes() != null) {
            for (Iterator it = entry.getEntryAttributes().iterator(); it.hasNext(); ) {
                WeblogEntryAttribute att = (WeblogEntryAttribute) it.next();
                it.remove();
                this.strategy.remove(att);
            }
        }
        // TODO: can we eliminate this unnecessary flush with OpenJPA 1.0
        this.strategy.flush();
        
        // remove entry
        this.strategy.remove(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if (entry.isPublished()) {
            roller.getWeblogManager().saveWeblog(weblog);
        }
        
        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWebsite().getHandle()+":"+entry.getAnchor());
    }
    
    public List getNextPrevEntries(WeblogEntry current, String catName,
            String locale, int maxEntries, boolean next)
            throws WebloggerException {

		if (current == null) {
			LOG.debug("current WeblogEntry cannot be null");
			return Collections.emptyList();
		}

        Query query;
        WeblogCategory category;
        
        List<Object> params = new ArrayList<Object>();
        int size = 0;
        String queryString = "SELECT e FROM WeblogEntry e WHERE ";
        StringBuilder whereClause = new StringBuilder();

        params.add(size++, current.getWebsite());
        whereClause.append("e.website = ?").append(size);
        
        params.add(size++, PubStatus.PUBLISHED);
        whereClause.append(" AND e.status = ?").append(size);
                
        if (next) {
            params.add(size++, current.getPubTime());
            whereClause.append(" AND e.pubTime > ?").append(size);
        } else {
            // pub time null if current article not yet published, in Draft view
            if (current.getPubTime() != null) {
                params.add(size++, current.getPubTime());
                whereClause.append(" AND e.pubTime < ?").append(size);
            }
        }
        
        if (catName != null) {
            category = getWeblogCategoryByName(current.getWebsite(), catName);
            if (category != null) {
                params.add(size++, category);
                whereClause.append(" AND e.category = ?").append(size);
            } else {
                throw new WebloggerException("Cannot find category: " + catName);
            } 
        }
        
        if(locale != null) {
            params.add(size++, locale + '%');
            whereClause.append(" AND e.locale like ?").append(size);
        }
        
        if (next) {
            whereClause.append(" ORDER BY e.pubTime ASC");
        } else {
            whereClause.append(" ORDER BY e.pubTime DESC");
        }
        query = strategy.getDynamicQuery(queryString + whereClause.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        query.setMaxResults(maxEntries);
        
        return query.getResultList();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public List<WeblogCategory> getWeblogCategories(Weblog website)
    throws WebloggerException {
        if (website == null) {
            throw new WebloggerException("website is null");
        }
        
        Query q = strategy.getNamedQuery(
                "WeblogCategory.getByWeblog");
        q.setParameter(1, website);
        return q.getResultList();
    }

    /**
     * @inheritDoc
     */
    public List<WeblogEntry> getWeblogEntries(WeblogEntrySearchCriteria wesc) throws WebloggerException {

        WeblogCategory cat = null;
        if (StringUtils.isNotEmpty(wesc.getCatName()) && wesc.getWeblog() != null) {
            cat = getWeblogCategoryByName(wesc.getWeblog(), wesc.getCatName());
        }

        List<Object> params = new ArrayList<Object>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
        
        if (wesc.getTags() == null || wesc.getTags().size()==0) {
            queryString.append("SELECT e FROM WeblogEntry e WHERE ");
        } else {
            queryString.append("SELECT e FROM WeblogEntry e JOIN e.tags t WHERE ");
            queryString.append("(");
            for (int i = 0; i < wesc.getTags().size(); i++) {
                if (i != 0) {
                    queryString.append(" OR ");
                }
                params.add(size++, wesc.getTags().get(i));
                queryString.append(" t.name = ?").append(size);                
            }
            queryString.append(") AND ");
        }
        
        if (wesc.getWeblog() != null) {
            params.add(size++, wesc.getWeblog().getId());
            queryString.append("e.website.id = ?").append(size);
        } else {
            params.add(size++, Boolean.TRUE);
            queryString.append("e.website.enabled = ?").append(size);
        }
        
        if (wesc.getUser() != null) {
            params.add(size++, wesc.getUser().getUserName());
            queryString.append(" AND e.creatorUserName = ?").append(size);
        }
        
        if (wesc.getStartDate() != null) {
            Timestamp start = new Timestamp(wesc.getStartDate().getTime());
            params.add(size++, start);
            queryString.append(" AND e.pubTime >= ?").append(size);
        }
        
        if (wesc.getEndDate() != null) {
            Timestamp end = new Timestamp(wesc.getEndDate().getTime());
            params.add(size++, end);
            queryString.append(" AND e.pubTime <= ?").append(size);
        }
        
        if (cat != null) {
            params.add(size++, cat.getId());
            queryString.append(" AND e.category.id = ?").append(size);
        }
                
        if (wesc.getStatus() != null) {
            params.add(size++, wesc.getStatus());
            queryString.append(" AND e.status = ?").append(size);
        }
        
        if (wesc.getLocale() != null) {
            params.add(size++, wesc.getLocale() + '%');
            queryString.append(" AND e.locale like ?").append(size);
        }
        
        if (StringUtils.isNotEmpty(wesc.getText())) {
            params.add(size++, '%' + wesc.getText() + '%');
            queryString.append(" AND ( e.text LIKE ?").append(size);
            queryString.append("    OR e.summary LIKE ?").append(size);
            queryString.append("    OR e.title LIKE ?").append(size);
            queryString.append(") ");
        }

        if (wesc.getSortBy() != null && wesc.getSortBy().equals(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME)) {
            queryString.append(" ORDER BY e.updateTime ");
        } else {
            queryString.append(" ORDER BY e.pubTime ");
        }
        
        if (wesc.getSortOrder() != null && wesc.getSortOrder().equals(WeblogEntrySearchCriteria.SortOrder.ASCENDING)) {
            queryString.append("ASC ");
        } else {
            queryString.append("DESC ");
        }
        
        
        Query query = strategy.getDynamicQuery(queryString.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        
        if (wesc.getOffset() != 0) {
            query.setFirstResult(wesc.getOffset());
        }
        if (wesc.getMaxResults() != -1) {
            query.setMaxResults(wesc.getMaxResults());
        }
        
        return query.getResultList();
    }
    
    /**
     * @inheritDoc
     */
    public List<WeblogEntry> getWeblogEntriesPinnedToMain(Integer max)
    throws WebloggerException {
        Query query = strategy.getNamedQuery(
                "WeblogEntry.getByPinnedToMain&statusOrderByPubTimeDesc");
        query.setParameter(1, Boolean.TRUE);
        query.setParameter(2, PubStatus.PUBLISHED);
        if (max != null) {
            query.setMaxResults(max);
        }
        return query.getResultList();
    }
    
    public void removeWeblogEntryAttribute(String name, WeblogEntry entry)
    throws WebloggerException {

        // seems silly, why is this not done in WeblogEntry?

        for (Iterator it = entry.getEntryAttributes().iterator(); it.hasNext();) {
            WeblogEntryAttribute entryAttribute = (WeblogEntryAttribute) it.next();
            if (entryAttribute.getName().equals(name)) {

                //Remove it from database
                this.strategy.remove(entryAttribute);

                //Remove it from the collection
                it.remove();
            }
        }
    }
    
    public void removeWeblogEntryTag(String name, WeblogEntry entry)
    throws WebloggerException {

        // seems silly, why is this not done in WeblogEntry?

        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTag tag = (WeblogEntryTag) it.next();
            if (tag.getName().equals(name)) {

                //Call back the entity to adjust its internal state
                entry.onRemoveTag(name);

                //Remove it from the collection
                it.remove();

                //Remove it from database
                this.strategy.remove(tag);
                this.strategy.flush();
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getWeblogEntryByAnchor(Weblog website,
            String anchor) throws WebloggerException {
        
        if (website == null) {
            throw new WebloggerException("Website is null");
        }
        
        if (anchor == null) {
            throw new WebloggerException("Anchor is null");
        }
        
        // mapping key is combo of weblog + anchor
        String mappingKey = website.getHandle()+":"+anchor;
        
        // check cache first
        // NOTE: if we ever allow changing anchors then this needs updating
        if(this.entryAnchorToIdMap.containsKey(mappingKey)) {
            
            WeblogEntry entry = this.getWeblogEntry((String) this.entryAnchorToIdMap.get(mappingKey));
            if(entry != null) {
                LOG.debug("entryAnchorToIdMap CACHE HIT - " + mappingKey);
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
        WeblogEntry entry;
        try {
            entry = (WeblogEntry)q.getSingleResult();
        } catch (NoResultException e) {
            entry = null;
        }
        
        // add mapping to cache
        if(entry != null) {
            LOG.debug("entryAnchorToIdMap CACHE MISS - " + mappingKey);
            this.entryAnchorToIdMap.put(mappingKey, entry.getId());
        }
        return entry;
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
        return (getWeblogCategoryByName(
                cat.getWeblog(), cat.getName()) != null);
    }
    
    /**
     * @inheritDoc
     */
    public boolean isWeblogCategoryInUse(WeblogCategory cat)
    throws WebloggerException {
        if (cat.getWeblog().getBloggerCategory().equals(cat)) {
            return true;
        }
        Query q = strategy.getNamedQuery("WeblogEntry.getByCategory");
        q.setParameter(1, cat);
        int entryCount = q.getResultList().size();
        return entryCount > 0;
    }
    
    /**
     * @inheritDoc
     */
    public List<WeblogEntryComment> getComments(CommentSearchCriteria csc) throws WebloggerException {
        
        List<Object> params = new ArrayList<Object>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT c FROM WeblogEntryComment c ");
        
        StringBuilder whereClause = new StringBuilder();
        if (csc.getEntry() != null) {
            params.add(size++, csc.getEntry());
            whereClause.append("c.weblogEntry = ?").append(size);
        } else if (csc.getWeblog() != null) {
            params.add(size++, csc.getWeblog());
            whereClause.append("c.weblogEntry.website = ?").append(size);
        }
        
        if (csc.getSearchText() != null) {
            params.add(size++, "%" + csc.getSearchText() + "%");
            appendConjuctionToWhereclause(whereClause, "(c.url LIKE ?")
                .append(size).append(" OR c.content LIKE ?").append(size).append(")");
        }
        
        if (csc.getStartDate() != null) {
            Timestamp start = new Timestamp(csc.getStartDate().getTime());
            params.add(size++, start);
            appendConjuctionToWhereclause(whereClause, "c.postTime >= ?").append(size);
        }
        
        if (csc.getEndDate() != null) {
            Timestamp end = new Timestamp(csc.getEndDate().getTime());
            params.add(size++, end);
            appendConjuctionToWhereclause(whereClause, "c.postTime <= ?").append(size);
        }
        
        if (csc.getStatus() != null) {
            params.add(size++, csc.getStatus());
            appendConjuctionToWhereclause(whereClause, "c.status = ?").append(size);
        }
        
        if(whereClause.length() != 0) {
            queryString.append(" WHERE ").append(whereClause);
        }
        if (csc.isReverseChrono()) {
            queryString.append(" ORDER BY c.postTime DESC");
        } else {
            queryString.append(" ORDER BY c.postTime ASC");
        }
        
        Query query = strategy.getDynamicQuery(queryString.toString());
        if (csc.getOffset() != 0) {
            query.setFirstResult(csc.getOffset());
        }
        if (csc.getMaxResults() != -1) {
            query.setMaxResults(csc.getMaxResults());
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
            Weblog     weblog,
            WeblogEntry entry,
            String  searchString,
            Date    startDate,
            Date    endDate,
            ApprovalStatus status) throws WebloggerException {
        
        // TODO dynamic bulk delete query: I'd MUCH rather use a bulk delete,
        // but MySQL says "General error, message from server: "You can't
        // specify target table 'roller_comment' for update in FROM clause"
        
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setWeblog(weblog);
        csc.setEntry(entry);
        csc.setSearchText(searchString);
        csc.setStartDate(startDate);
        csc.setEndDate(endDate);
        csc.setStatus(status);

        List<WeblogEntryComment> comments = getComments(csc);
        int count = 0;
        for (WeblogEntryComment comment : comments) {
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
    public WeblogCategory getWeblogCategoryByName(Weblog weblog,
            String categoryName) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogCategory.getByWeblog&Name");
        q.setParameter(1, weblog);
        q.setParameter(2, categoryName);
        try {
            return (WeblogCategory)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
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
    public Map<Date, List<WeblogEntry>> getWeblogEntryObjectMap(WeblogEntrySearchCriteria wesc) throws WebloggerException {
        return getWeblogEntryMap(wesc, false);
    }

    /**
     * @inheritDoc
     */
    public Map<Date, String> getWeblogEntryStringMap(WeblogEntrySearchCriteria wesc) throws WebloggerException {
        return getWeblogEntryMap(wesc, true);
    }
    
    private Map getWeblogEntryMap(WeblogEntrySearchCriteria wesc, boolean stringsOnly) throws WebloggerException {

        TreeMap map = new TreeMap(REVERSE_COMPARATOR);

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        Calendar cal = Calendar.getInstance();
        if (wesc.getWeblog() != null) {
            cal.setTimeZone(wesc.getWeblog().getTimeZoneInstance());
        }
        
        SimpleDateFormat formatter = DateUtil.get8charDateFormat();
        for (WeblogEntry entry : entries) {
            Date sDate = DateUtil.getNoonOfDay(entry.getPubTime(), cal);
            if (stringsOnly) {
                if (map.get(sDate) == null) {
                    map.put(sDate, formatter.format(sDate));
                }
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
    public List<StatCount> getMostCommentedWeblogEntries(Weblog website,
            Date startDate, Date endDate, int offset,
            int length) throws WebloggerException {
        Query query;
        List queryResults;

        Timestamp end = new Timestamp(endDate != null? endDate.getTime() : new Date().getTime());

        if (website != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByWebsite&EndDate&StartDate");
                query.setParameter(1, website);
                query.setParameter(2, end);
                query.setParameter(3, start);
            } else {
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByWebsite&EndDate");
                query.setParameter(1, website);
                query.setParameter(2, end);
            }
        } else {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByEndDate&StartDate");
                query.setParameter(1, end);
                query.setParameter(2, start);
            } else {
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
        List<StatCount> results = new ArrayList<StatCount>();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            StatCount sc = new StatCount(
                    (String)row[1],                             // weblog handle
                    (String)row[2],                             // entry anchor
                    (String)row[3],                             // entry title
                    "statCount.weblogEntryCommentCountType",    // stat desc
                    ((Long)row[0]));                            // count
            sc.setWeblogHandle((String)row[1]);
            results.add(sc);
        }
        // Original query ordered by desc count.
        // JPA QL doesn't allow queries to be ordered by agregates; do it in memory
        Collections.sort(results, STAT_COUNT_COUNT_REVERSE_COMPARATOR);
        
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("applyCommentDefaults");
        }
        
        // TODO: Non-standard JPA bulk update, using parameter values in set clause
        Query q = strategy.getNamedUpdate(
                "WeblogEntry.updateAllowComments&CommentDaysByWebsite");
        q.setParameter(1, website.getDefaultAllowComments());
        q.setParameter(2, website.getDefaultCommentDays());
        q.setParameter(3, website);
        q.executeUpdate();
    }
    
    /**
     * @inheritDoc
     */
    public List<TagStat> getPopularTags(Weblog website, Date startDate, int offset, int limit)
    throws WebloggerException {
        Query query;
        List queryResults;
        
        if (website != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryTagAggregate.getPopularTagsByWebsite&StartDate");
                query.setParameter(1, website);
                query.setParameter(2, start);
            } else {
                query = strategy.getNamedQuery(
                        "WeblogEntryTagAggregate.getPopularTagsByWebsite");
                query.setParameter(1, website);
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
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (limit != -1) {
            query.setMaxResults(limit);
        }
        queryResults = query.getResultList();
        
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        
        List<TagStat> results = new ArrayList<TagStat>(limit >= 0 ? limit : 25);
        
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
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
        
        for (TagStat t : results) {
            t.setIntensity((int) (1 + Math.floor(5 * (Math.log(1+t.getCount()) - min) / range)));
        }

        // sort results by name, because query had to sort by total
        Collections.sort(results, TAG_STAT_NAME_COMPARATOR);
        
        return results;
    }
    
    /**
     * @inheritDoc
     */
    public List<TagStat> getTags(Weblog website, String sortBy,
            String startsWith, int offset, int limit) throws WebloggerException {
        Query query;
        List queryResults;
        boolean sortByName = sortBy == null || !sortBy.equals("count");
                
        List<Object> params = new ArrayList<Object>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
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
        queryString.append(" GROUP BY w.name, w.total ORDER BY ").append(sortBy);

        query = strategy.getDynamicQuery(queryString.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (limit != -1) {
            query.setMaxResults(limit);
        }
        queryResults = query.getResultList();
        
        List<TagStat> results = new ArrayList<TagStat>();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            TagStat ce = new TagStat();
            ce.setName((String) row[0]);
            // The JPA query retrieves SUM(w.total) always as long
            ce.setCount(((Long) row[1]).intValue());
            results.add(ce);
        }

        if (sortByName) {
            Collections.sort(results, TAG_STAT_NAME_COMPARATOR);
        } else {
            Collections.sort(results, TAG_STAT_COUNT_REVERSE_COMPARATOR);
        }
        
        return results;
    }
    
    
    /**
     * @inheritDoc
     */
    public boolean getTagComboExists(List tags, Weblog weblog) throws WebloggerException{
        
        if (tags == null || tags.size() == 0) {
            return false;
        }
        
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT DISTINCT w.name ");
        queryString.append("FROM WeblogEntryTagAggregate w WHERE w.name IN (");
        // Append tags as parameter markers to avoid potential escaping issues
        // The IN clause would be of form (?1, ?2, ?3, ..)
        List<Object> params = new ArrayList<Object>(tags.size() + 1);
        final String paramSeparator = ", ";
        int i;
        for (i=0; i < tags.size(); i++) {
            queryString.append('?').append(i+1).append(paramSeparator);
            params.add(tags.get(i));
        }
        
        // Remove the trailing paramSeparator
        queryString.delete(queryString.length() - paramSeparator.length(),
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
        }
        
        // delete all bad counts
        Query removeq = strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.removeByTotalLessEqual");
        removeq.setParameter(1, 0);
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
    public List<WeblogHitCount> getHotWeblogs(int sinceDays, int offset, int length)
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
        WeblogHitCount hitCount;
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
        WeblogHitCount hitCount;
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
        q.setParameter(1, ApprovalStatus.APPROVED);
        List results = q.getResultList();
        return ((Long)results.get(0));
    }
    
    /**
     * @inheritDoc
     */
    public long getCommentCount(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntryComment.getCountDistinctByWebsite&Status");
        q.setParameter(1, website);
        q.setParameter(2, ApprovalStatus.APPROVED);
        List results = q.getResultList();
        return ((Long)results.get(0));
    }
    
    /**
     * @inheritDoc
     */
    public long getEntryCount() throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntry.getCountDistinctByStatus");
        q.setParameter(1, PubStatus.PUBLISHED);
        List results = q.getResultList();
        return ((Long)results.get(0));
    }
    
    /**
     * @inheritDoc
     */
    public long getEntryCount(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
                "WeblogEntry.getCountDistinctByStatus&Website");
        q.setParameter(1, PubStatus.PUBLISHED);
        q.setParameter(2, website);
        List results = q.getResultList();
        return ((Long)results.get(0));
    }
    
    /**
     * Appends given expression to given whereClause. If whereClause already
     * has other conditions, an " AND " is also appended before appending
     * the expression
     * @param whereClause The given where Clauuse
     * @param expression The given expression
     * @return the whereClause.
     */
    private static StringBuilder appendConjuctionToWhereclause(StringBuilder whereClause,
            String expression) {
        if(whereClause.length() != 0 && expression.length() != 0) {
            whereClause.append(" AND ");
        }
        return whereClause.append(expression);
    }
    
}
