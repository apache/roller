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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TimeZone;

/**
 * JPAWeblogManagerImpl.java
 *
 * Created on May 31, 2006, 4:08 PM
 *
 */
public class JPAWeblogEntryManagerImpl implements WeblogEntryManager {
    
    private static final Log LOG = LogFactory.getLog(JPAWeblogEntryManagerImpl.class);

    private final PingTargetManager pingTargetManager;
    private final JPAPersistenceStrategy strategy;
    
    // cached mapping of entryAnchors -> entryIds
    private Map<String, String> entryAnchorToIdMap = new HashMap<>();

    protected JPAWeblogEntryManagerImpl(PingTargetManager mgr, JPAPersistenceStrategy strategy) {
        LOG.debug("Instantiating JPA Weblog Manager");
        this.pingTargetManager = mgr;
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

        updateWeblogLastModifiedDate(cat.getWeblog());
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
            cat.getWeblog().setBloggerCategory(null);
            this.strategy.store(cat.getWeblog());
        }

        updateWeblogLastModifiedDate(cat.getWeblog());
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
            entry.setWeblog(website);
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
        updateWeblogLastModifiedDate(comment.getWeblogEntry().getWeblog());
    }
    
    /**
     * @inheritDoc
     */
    public void removeComment(WeblogEntryComment comment) throws WebloggerException {
        this.strategy.remove(comment);
        updateWeblogLastModifiedDate(comment.getWeblogEntry().getWeblog());
    }
    
    /**
     * @inheritDoc
     */
    // TODO: perhaps the createAnchor() and queuePings() items should go outside this method?
    public void saveWeblogEntry(WeblogEntry entry) throws WebloggerException {

        if (entry.getCategory() == null) {
            // Entry is invalid without category, so use weblog client cat
            WeblogCategory cat = entry.getWeblog().getBloggerCategory();
            if (cat == null) {
                // Still no category, so use first one found
                cat = entry.getWeblog().getWeblogCategories().iterator().next();
            }
            entry.setCategory(cat);
        }

        if (entry.getAnchor() == null || entry.getAnchor().trim().equals("")) {
            entry.setAnchor(this.createAnchor(entry));
        }
        
        for (WeblogEntryTag tag : entry.getRemovedTags()) {
            removeWeblogEntryTag(tag);
        }

        // if the entry was published to future, set status as SCHEDULED
        // we only consider an entry future published if it is scheduled
        // more than 1 minute into the future
        if (PubStatus.PUBLISHED.equals(entry.getStatus()) &&
                entry.getPubTime().after(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE))) {
            entry.setStatus(PubStatus.SCHEDULED);
        }
        
        // Store value object (creates new or updates existing)
        entry.setUpdateTime(new Timestamp(new Date().getTime()));
        
        this.strategy.store(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            updateWeblogLastModifiedDate(entry.getWeblog());
        }
        
        if(entry.isPublished()) {
            // Queue applicable pings for this update.
            pingTargetManager.queueApplicableAutoPings(entry.getWeblog());
        }
    }
    
    private void updateWeblogLastModifiedDate(Weblog weblog) throws WebloggerException {
        weblog.setLastModified(new java.util.Date());
        strategy.store(weblog);
    }

    /**
     * @inheritDoc
     */
    public void removeWeblogEntry(WeblogEntry entry) throws WebloggerException {
        Weblog weblog = entry.getWeblog();
        
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setEntry(entry);

        // remove comments
        List<WeblogEntryComment> comments = getComments(csc);
        for (WeblogEntryComment comment : comments) {
            this.strategy.remove(comment);
        }
        
        // remove tag & tag aggregates
        if (entry.getTags() != null) {
            for (WeblogEntryTag tag : entry.getTags()) {
                removeWeblogEntryTag(tag);
            }
        }
        
        // remove entry
        this.strategy.remove(entry);
        
        if (entry.isPublished()) {
            updateWeblogLastModifiedDate(weblog);
        }
        
        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWeblog().getHandle() + ":" + entry.getAnchor());
    }
    
    public List getNextPrevEntries(WeblogEntry current, String catName,
            int maxEntries, boolean next)
            throws WebloggerException {

		if (current == null) {
			LOG.debug("current WeblogEntry cannot be null");
			return Collections.emptyList();
		}

        TypedQuery<WeblogEntry> query;
        WeblogCategory category;
        
        List<Object> params = new ArrayList<>();
        int size = 0;
        String queryString = "SELECT e FROM WeblogEntry e WHERE ";
        StringBuilder whereClause = new StringBuilder();

        params.add(size++, current.getWeblog());
        whereClause.append("e.weblog = ?").append(size);
        
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
            category = getWeblogCategoryByName(current.getWeblog(), catName);
            if (category != null) {
                params.add(size++, category);
                whereClause.append(" AND e.category = ?").append(size);
            } else {
                throw new WebloggerException("Cannot find category: " + catName);
            } 
        }
        
        if (next) {
            whereClause.append(" ORDER BY e.pubTime ASC");
        } else {
            whereClause.append(" ORDER BY e.pubTime DESC");
        }
        query = strategy.getDynamicQuery(queryString + whereClause.toString(), WeblogEntry.class);
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
    public List<WeblogCategory> getWeblogCategories(Weblog weblog)
    throws WebloggerException {
        if (weblog == null) {
            throw new WebloggerException("weblog is null");
        }
        
        TypedQuery<WeblogCategory> q = strategy.getNamedQuery(
                "WeblogCategory.getByWeblog", WeblogCategory.class);
        q.setParameter(1, weblog);
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

        List<Object> params = new ArrayList<>();
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
            queryString.append("e.weblog.id = ?").append(size);
        } else {
            params.add(size++, Boolean.TRUE);
            queryString.append("e.weblog.visible = ?").append(size);
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
        
        
        TypedQuery<WeblogEntry> query = strategy.getDynamicQuery(queryString.toString(), WeblogEntry.class);
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
        TypedQuery<WeblogEntry> query = strategy.getNamedQuery(
                "WeblogEntry.getByPinnedToMain&statusOrderByPubTimeDesc", WeblogEntry.class);
        query.setParameter(1, Boolean.TRUE);
        query.setParameter(2, PubStatus.PUBLISHED);
        if (max != null) {
            query.setMaxResults(max);
        }
        return query.getResultList();
    }
    
    private void removeWeblogEntryTag(WeblogEntryTag tag) throws WebloggerException {
        this.strategy.remove(tag);
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
        String mappingKey = website.getHandle() + ":" + anchor;
        
        // check cache first
        // NOTE: if we ever allow changing anchors then this needs updating
        if(this.entryAnchorToIdMap.containsKey(mappingKey)) {
            
            WeblogEntry entry = this.getWeblogEntry(this.entryAnchorToIdMap.get(mappingKey));
            if(entry != null) {
                LOG.debug("entryAnchorToIdMap CACHE HIT - " + mappingKey);
                return entry;
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.entryAnchorToIdMap.remove(mappingKey);
            }
        }
        
        // cache failed, do lookup
        TypedQuery<WeblogEntry> q = strategy.getNamedQuery(
                "WeblogEntry.getByWeblog&AnchorOrderByPubTimeDesc", WeblogEntry.class);
        q.setParameter(1, website);
        q.setParameter(2, anchor);
        WeblogEntry entry;
        try {
            entry = q.getSingleResult();
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
            
            TypedQuery<WeblogEntry> q = strategy.getNamedQuery(
                    "WeblogEntry.getByWeblog&Anchor", WeblogEntry.class);
            q.setParameter(1, entry.getWeblog());
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
        TypedQuery<WeblogEntry> q = strategy.getNamedQuery("WeblogEntry.getByCategory", WeblogEntry.class);
        q.setParameter(1, cat);
        int entryCount = q.getResultList().size();
        return entryCount > 0;
    }
    
    /**
     * @inheritDoc
     */
    public List<WeblogEntryComment> getComments(CommentSearchCriteria csc) throws WebloggerException {
        
        List<Object> params = new ArrayList<>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT c FROM WeblogEntryComment c ");
        
        StringBuilder whereClause = new StringBuilder();
        if (csc.getEntry() != null) {
            params.add(size++, csc.getEntry());
            whereClause.append("c.weblogEntry = ?").append(size);
        } else if (csc.getWeblog() != null) {
            params.add(size++, csc.getWeblog());
            whereClause.append("c.weblogEntry.weblog = ?").append(size);
        }
        
        if (csc.getSearchText() != null) {
            params.add(size++, "%" + csc.getSearchText().toUpperCase() + "%");
            appendConjuctionToWhereclause(whereClause, "upper(c.content) LIKE ?").append(size);
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
        
        TypedQuery<WeblogEntryComment> query = strategy.getDynamicQuery(queryString.toString(), WeblogEntryComment.class);
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
        // specify target table 'weblog_entry_comment' for update in FROM clause"
        
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
    public WeblogCategory getWeblogCategory(String id) throws WebloggerException {
        return this.strategy.load(WeblogCategory.class, id);
    }
    
    //--------------------------------------------- WeblogCategory Queries
    
    /**
     * @inheritDoc
     */
    public WeblogCategory getWeblogCategoryByName(Weblog weblog,
            String categoryName) throws WebloggerException {
        TypedQuery<WeblogCategory> q = strategy.getNamedQuery(
                "WeblogCategory.getByWeblog&Name", WeblogCategory.class);
        q.setParameter(1, weblog);
        q.setParameter(2, categoryName);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    public WeblogEntryComment getComment(String id) throws WebloggerException {
        return this.strategy.load(WeblogEntryComment.class, id);
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getWeblogEntry(String id) throws WebloggerException {
        return strategy.load(WeblogEntry.class, id);
    }
    
    /**
     * @inheritDoc
     */
    public Map<Date, List<WeblogEntry>> getWeblogEntryObjectMap(WeblogEntrySearchCriteria wesc) throws WebloggerException {
        TreeMap<Date, List<WeblogEntry>> map = new TreeMap<>(Collections.reverseOrder());

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        Calendar cal = Calendar.getInstance();
        if (wesc.getWeblog() != null) {
            cal.setTimeZone(wesc.getWeblog().getTimeZoneInstance());
        }

        for (WeblogEntry entry : entries) {
            cal.setTime(entry.getPubTime() == null ? new Date() : entry.getPubTime());
            Date sDate = DateUtils.truncate(cal, Calendar.DATE).getTime();
            List<WeblogEntry> dayEntries = map.get(sDate);
            if (dayEntries == null) {
                dayEntries = new ArrayList<>();
                map.put(sDate, dayEntries);
            }
            dayEntries.add(entry);
        }
        return map;
    }

    /**
     * @inheritDoc
     */
    public Map<Date, String> getWeblogEntryStringMap(WeblogEntrySearchCriteria wesc) throws WebloggerException {
        TreeMap<Date, String> map = new TreeMap<>(Collections.reverseOrder());

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        Calendar cal = Calendar.getInstance();
        FastDateFormat formatter;
        if (wesc.getWeblog() != null) {
            TimeZone tz = wesc.getWeblog().getTimeZoneInstance();
            formatter = FastDateFormat.getInstance(WebloggerUtils.FORMAT_8CHARS, tz);
        } else {
            formatter = FastDateFormat.getInstance(WebloggerUtils.FORMAT_8CHARS);
        }

        for (WeblogEntry entry : entries) {
            cal.setTime(entry.getPubTime());
            Date sDate = DateUtils.truncate(cal, Calendar.DATE).getTime();
            if (map.get(sDate) == null) {
                map.put(sDate, formatter.format(sDate));
            }
        }
        return map;
    }

    /**
     * @inheritDoc
     */
    public List<StatCount> getMostCommentedWeblogEntries(Weblog weblog,
            Date startDate, Date endDate, int offset,
            int length) throws WebloggerException {
        TypedQuery<WeblogEntryComment> query;
        List queryResults;

        Timestamp end = new Timestamp(endDate != null? endDate.getTime() : new Date().getTime());

        if (weblog != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByWeblog&EndDate&StartDate",
                        WeblogEntryComment.class);
                query.setParameter(1, weblog);
                query.setParameter(2, end);
                query.setParameter(3, start);
            } else {
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByWeblog&EndDate", WeblogEntryComment.class);
                query.setParameter(1, weblog);
                query.setParameter(2, end);
            }
        } else {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByEndDate&StartDate", WeblogEntryComment.class);
                query.setParameter(1, end);
                query.setParameter(2, start);
            } else {
                query = strategy.getNamedQuery(
                        "WeblogEntryComment.getMostCommentedWeblogEntryByEndDate", WeblogEntryComment.class);
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
        List<StatCount> results = new ArrayList<>();
        if (queryResults != null) {
            for (Object obj : queryResults) {
                Object[] row = (Object[]) obj;
                StatCount sc = new StatCount(
                        (String)row[1],                             // weblog handle
                        (String)row[2],                             // entry anchor
                        (String)row[3],                             // entry title
                        "statCount.weblogEntryCommentCountType",    // stat desc
                        ((Long)row[0]));                            // count
                sc.setWeblogHandle((String)row[1]);
                results.add(sc);
            }
        }
        // Original query ordered by desc count.
        // JPA QL doesn't allow queries to be ordered by agregates; do it in memory
        Collections.sort(results, StatCount.CountComparator);
        
        return results;
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getNextEntry(WeblogEntry current,
            String catName) throws WebloggerException {
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, 1, true);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }
    
    /**
     * @inheritDoc
     */
    public WeblogEntry getPreviousEntry(WeblogEntry current,
            String catName) throws WebloggerException {
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, 1, false);
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
    public void applyCommentDefaultsToEntries(Weblog weblog)
    throws WebloggerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("applyCommentDefaults");
        }
        
        // TODO: Non-standard JPA bulk update, using parameter values in set clause
        Query q = strategy.getNamedUpdate(
                "WeblogEntry.updateAllowComments&CommentDaysByWeblog");
        q.setParameter(1, weblog.getDefaultAllowComments());
        q.setParameter(2, weblog.getDefaultCommentDays());
        q.setParameter(3, weblog);
        q.executeUpdate();
    }
    
    /**
     * @inheritDoc
     */
    public List<TagStat> getPopularTags(Weblog website, int offset, int limit)
    throws WebloggerException {
        TypedQuery<TagStat> query;
        List queryResults;
        
        if (website != null) {
            query = strategy.getNamedQuery(
                    "WeblogEntryTagAggregate.getPopularTagsByWeblog", TagStat.class);
            query.setParameter(1, website);
        } else {
            query = strategy.getNamedQuery(
                    "WeblogEntryTagAggregate.getPopularTagsByWeblogNull", TagStat.class);
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
        
        List<TagStat> results = new ArrayList<>(limit >= 0 ? limit : 25);
        
        if (queryResults != null) {
            for (Object obj : queryResults) {
                Object[] row = (Object[]) obj;
                TagStat t = new TagStat();
                t.setName((String) row[0]);
                t.setCount(((Number) row[1]).intValue());

                min = Math.min(min, t.getCount());
                max = Math.max(max, t.getCount());
                results.add(t);
            }
        }

        min = Math.log(1+min);
        max = Math.log(1+max);
        
        double range = Math.max(.01, max - min) * 1.0001;
        
        for (TagStat t : results) {
            t.setIntensity((int) (1 + Math.floor(5 * (Math.log(1+t.getCount()) - min) / range)));
        }

        // sort results by name, because query had to sort by total
        Collections.sort(results, TagStat.Comparator);
        
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
                
        List<Object> params = new ArrayList<>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE 1 = 1");
                
        if (website != null) {
            params.add(size++, website.getId());
            queryString.append(" AND w.weblog.id = ?").append(size);
        }
                       
        if (startsWith != null && startsWith.length() > 0) {
            params.add(size++, startsWith + '%');
            queryString.append(" AND w.name LIKE ?").append(size);
        }
                    
        if (sortBy != null && sortBy.equals("count")) {
            sortBy = "w.total DESC";
        } else {
            sortBy = "w.name";
        }
        queryString.append(" GROUP BY w.name ORDER BY ").append(sortBy);

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
        
        List<TagStat> results = new ArrayList<>();
        if (queryResults != null) {
            for (Object obj : queryResults) {
                Object[] row = (Object[]) obj;
                TagStat ce = new TagStat();
                ce.setName((String) row[0]);
                // The JPA query retrieves SUM(w.total) always as long
                ce.setCount(((Long) row[1]).intValue());
                results.add(ce);
            }
        }

        if (sortByName) {
            Collections.sort(results, TagStat.Comparator);
        } else {
            Collections.sort(results, TagStat.CountComparator);
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
        List<Object> params = new ArrayList<>(tags.size() + 1);
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
        
        if (weblog != null) {
            queryString.append(" AND w.weblog = ?").append(i+1);
            params.add(weblog);
        }
        
        TypedQuery<String> q = strategy.getDynamicQuery(queryString.toString(), String.class);
        for (int j=0; j<params.size(); j++) {
            q.setParameter(j+1, params.get(j));
        }
        List<String> results = q.getResultList();
        
        //TODO: DatamapperPort: Since we are only interested in knowing whether
        //results.size() == tags.size(). This query can be optimized to just fetch COUNT
        //instead of objects as done currently
        return (results != null && results.size() == tags.size());
    }


    /**
     * @inheritDoc
     */
    public long getCommentCount() throws WebloggerException {
        TypedQuery<Long> q = strategy.getNamedQuery(
                "WeblogEntryComment.getCountAllDistinctByStatus", Long.class);
        q.setParameter(1, ApprovalStatus.APPROVED);
        return q.getResultList().get(0);
    }
    
    /**
     * @inheritDoc
     */
    public long getCommentCount(Weblog weblog) throws WebloggerException {
        TypedQuery<Long> q = strategy.getNamedQuery(
                "WeblogEntryComment.getCountDistinctByWeblog&Status", Long.class);
        q.setParameter(1, weblog);
        q.setParameter(2, ApprovalStatus.APPROVED);
        return q.getResultList().get(0);
    }
    
    /**
     * @inheritDoc
     */
    public long getEntryCount() throws WebloggerException {
        TypedQuery<Long> q = strategy.getNamedQuery(
                "WeblogEntry.getCountDistinctByStatus", Long.class);
        q.setParameter(1, PubStatus.PUBLISHED);
        return q.getResultList().get(0);
    }
    
    /**
     * @inheritDoc
     */
    public long getEntryCount(Weblog website) throws WebloggerException {
        TypedQuery<Long> q = strategy.getNamedQuery(
                "WeblogEntry.getCountDistinctByStatus&Weblog", Long.class);
        q.setParameter(1, PubStatus.PUBLISHED);
        q.setParameter(2, website);
        return q.getResultList().get(0);
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
        if (whereClause.length() != 0 && expression.length() != 0) {
            whereClause.append(" AND ");
        }
        return whereClause.append(expression);
    }
    
}
