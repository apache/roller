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
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TimeZone;

public class JPAWeblogEntryManagerImpl implements WeblogEntryManager {

    private static Logger log = LoggerFactory.getLogger(JPAWeblogEntryManagerImpl.class);

    private final PingTargetManager pingTargetManager;
    private final JPAPersistenceStrategy strategy;

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private List<WeblogEntryCommentPlugin> commentPlugins = new ArrayList<>();

    public void setCommentPlugins(List<WeblogEntryCommentPlugin> commentPlugins) {
        this.commentPlugins = commentPlugins;
    }

    private List<WeblogEntryPlugin> weblogEntryPlugins = new ArrayList<>();

    public void setWeblogEntryPlugins(List<WeblogEntryPlugin> weblogEntryPlugins) {
        this.weblogEntryPlugins = weblogEntryPlugins;
    }

    // cached mapping of entryAnchors -> entryIds
    private Map<String, String> entryAnchorToIdMap = new HashMap<>();

    protected JPAWeblogEntryManagerImpl(PingTargetManager mgr, JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Weblog Manager");
        this.pingTargetManager = mgr;
        this.strategy = strategy;
    }

    @Override
    public void saveComment(WeblogEntryComment comment, boolean refreshWeblog) {
        if (refreshWeblog) {
            comment.getWeblogEntry().getWeblog().invalidateCache();
        }
        this.strategy.store(comment);
    }

    @Override
    public void removeComment(WeblogEntryComment comment) {
        comment.getWeblogEntry().getWeblog().invalidateCache();
        this.strategy.remove(comment);
    }

    @Override
    public void saveWeblogEntry(WeblogEntry entry) {

        if (entry.getCategory() == null) {
            // Entry is invalid without category, so use first one found if not provided
            WeblogCategory cat = entry.getWeblog().getWeblogCategories().iterator().next();
            entry.setCategory(cat);
        }

        if (entry.getAnchor() == null || entry.getAnchor().trim().equals("")) {
            entry.setAnchor(this.createAnchor(entry));
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
        entry.getWeblog().invalidateCache();

        if (entry.isPublished()) {
            strategy.store(entry.getWeblog());
        }

        if (entry.isPublished()) {
            // Queue applicable pings for this update.
            pingTargetManager.addToPingSet(entry.getWeblog());
        }

    }

    @Override
    public void removeWeblogEntry(WeblogEntry entry) {
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setEntry(entry);

        // remove comments
        List<WeblogEntryComment> comments = getComments(csc);
        for (WeblogEntryComment comment : comments) {
            this.strategy.remove(comment);
        }

        // remove entry
        this.strategy.remove(entry);
        entry.getWeblog().invalidateCache();

        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWeblog().getHandle() + ":" + entry.getAnchor());
    }

    private List getNextPrevEntries(WeblogEntry current, String catName, int maxEntries, boolean next) {

        if (current == null) {
            log.debug("current WeblogEntry cannot be null");
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
            category = weblogManager.getWeblogCategoryByName(current.getWeblog(), catName);
            if (category != null) {
                params.add(size++, category);
                whereClause.append(" AND e.category = ?").append(size);
            }
        }

        if (next) {
            whereClause.append(" ORDER BY e.pubTime ASC");
        } else {
            whereClause.append(" ORDER BY e.pubTime DESC");
        }
        query = strategy.getDynamicQuery(queryString + whereClause.toString(), WeblogEntry.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        query.setMaxResults(maxEntries);

        return query.getResultList();
    }

    @Override
    public List<WeblogEntry> getWeblogEntries(WeblogEntrySearchCriteria wesc) {

        List<Object> params = new ArrayList<>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();

        if (wesc.getTags() == null || wesc.getTags().size() == 0) {
            queryString.append("SELECT e FROM WeblogEntry e WHERE 1=1 ");
        } else {
            // subquery to avoid this problem with Derby: http://stackoverflow.com/a/480536
            queryString.append("SELECT e FROM WeblogEntry e WHERE EXISTS ( Select 1 from WeblogEntryTag t " +
                    "where t.weblogEntry.id = e.id AND ");
            queryString.append("(");
            boolean isFirst = true;
            for (String tagName : wesc.getTags()) {
                if (!isFirst) {
                    queryString.append(" OR ");
                }
                params.add(size++, tagName);
                queryString.append(" t.name = ?").append(size);
                isFirst = false;
            }
            queryString.append(")) ");
        }

        if (wesc.getWeblog() != null) {
            params.add(size++, wesc.getWeblog().getId());
            queryString.append("AND e.weblog.id = ?").append(size);
        }

        params.add(size++, Boolean.TRUE);
        queryString.append(" AND e.weblog.visible = ?").append(size);

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

        if (wesc.getCatName() != null) {
            params.add(size++, wesc.getCatName());
            queryString.append(" AND e.category.name = ?").append(size);
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
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        if (wesc.getOffset() != 0) {
            query.setFirstResult(wesc.getOffset());
        }
        if (wesc.getMaxResults() != -1) {
            query.setMaxResults(wesc.getMaxResults());
        }

        return query.getResultList();
    }

    @Override
    public WeblogEntry getWeblogEntryByAnchor(Weblog website, String anchor) {

        if (website == null) {
            throw new IllegalArgumentException("Website is null");
        }

        if (anchor == null) {
            throw new IllegalArgumentException("Anchor is null");
        }

        // mapping key is combo of weblog + anchor
        String mappingKey = website.getHandle() + ":" + anchor;

        // check cache first
        // NOTE: if we ever allow changing anchors then this needs updating
        if (this.entryAnchorToIdMap.containsKey(mappingKey)) {

            WeblogEntry entry = this.getWeblogEntry(this.entryAnchorToIdMap.get(mappingKey));
            if (entry != null) {
                log.debug("entryAnchorToIdMap CACHE HIT - {}", mappingKey);
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
        if (entry != null) {
            log.debug("entryAnchorToIdMap CACHE MISS - {}", mappingKey);
            this.entryAnchorToIdMap.put(mappingKey, entry.getId());
        }
        return entry;
    }

    @Override
    public String createAnchor(WeblogEntry entry) {
        // Check for uniqueness of anchor
        String base = createAnchorBase(entry);
        String name = base;
        int count = 0;

        while (true) {
            if (count > 0) {
                name = base + count;
            }

            TypedQuery<WeblogEntry> q = strategy.getNamedQuery("WeblogEntry.getByWeblog&Anchor", WeblogEntry.class);
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
     * Create anchor for weblog entry, based on title or text
     */
    private String createAnchorBase(WeblogEntry entry) {

        // Use title (minus non-alphanumeric characters)
        String base = null;
        if (!StringUtils.isEmpty(entry.getTitle())) {
            base = Utilities.replaceNonAlphanumeric(entry.getTitle(), ' ').trim();
        }
        // If we still have no base, then try text (minus non-alphanumerics)
        if (StringUtils.isEmpty(base) && !StringUtils.isEmpty(entry.getText())) {
            base = Utilities.replaceNonAlphanumeric(entry.getText(), ' ').trim();
        }

        if (!StringUtils.isEmpty(base)) {

            // Use only the first 4 words
            StringTokenizer toker = new StringTokenizer(base);
            String tmp = null;
            int count = 0;
            while (toker.hasMoreTokens() && count < 5) {
                String s = toker.nextToken();
                s = s.toLowerCase();
                tmp = (tmp == null) ? s : tmp + "-" + s;
                count++;
            }
            base = tmp;
        }
        // No title or text, so instead we will use the items date
        // in YYYYMMDD format as the base anchor
        else {
            base = FastDateFormat.getInstance(WebloggerCommon.FORMAT_8CHARS,
                    entry.getWeblog().getTimeZoneInstance()).format(entry.getPubTime());
        }

        return base;
    }

    @Override
    public List<WeblogEntryComment> getComments(CommentSearchCriteria csc) {

        List<Object> params = new ArrayList<>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT c FROM WeblogEntryComment c");

        StringBuilder whereClause = new StringBuilder();
        if (csc.getEntry() != null) {
            params.add(size++, csc.getEntry());
            appendConjuctionToWhereclause(whereClause, "c.weblogEntry = ?").append(size);
        } else {
            if (csc.getWeblog() != null) {
                params.add(size++, csc.getWeblog());
                appendConjuctionToWhereclause(whereClause, "c.weblogEntry.weblog = ?").append(size);
            }
            if (csc.getCategoryName() != null) {
                params.add(size++, csc.getCategoryName());
                appendConjuctionToWhereclause(whereClause, "c.weblogEntry.category.name = ?").append(size);
            }
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

    @Override
    public WeblogEntryComment getComment(String id) {
        return this.strategy.load(WeblogEntryComment.class, id);
    }
    
    @Override
    public WeblogEntry getWeblogEntry(String id) {
        return strategy.load(WeblogEntry.class, id);
    }

    @Override
    public Map<Date, List<WeblogEntry>> getWeblogEntryObjectMap(WeblogEntrySearchCriteria wesc) {
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

    @Override
    public Map<Date, String> getWeblogEntryStringMap(WeblogEntrySearchCriteria wesc) {
        TreeMap<Date, String> map = new TreeMap<>(Collections.reverseOrder());

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        Calendar cal = Calendar.getInstance();
        FastDateFormat formatter;
        if (wesc.getWeblog() != null) {
            TimeZone tz = wesc.getWeblog().getTimeZoneInstance();
            formatter = FastDateFormat.getInstance(WebloggerCommon.FORMAT_8CHARS, tz);
        } else {
            formatter = FastDateFormat.getInstance(WebloggerCommon.FORMAT_8CHARS);
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

    @Override
    public WeblogEntry getNextEntry(WeblogEntry current, String catName) {
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, 1, true);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }

    @Override
    public WeblogEntry getPreviousEntry(WeblogEntry current, String catName) {
        WeblogEntry entry = null;
        List entryList = getNextPrevEntries(current, catName, 1, false);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntry)entryList.get(0);
        }
        return entry;
    }

    @Override
    public void applyCommentDefaultsToEntries(Weblog weblog) {
        Query q = strategy.getNamedUpdate("WeblogEntry.updateCommentDaysByWeblog");
        q.setParameter(1, weblog.getDefaultCommentDays());
        q.setParameter(2, weblog);
        q.executeUpdate();
    }

    @Override
    public List<TagStat> getPopularTags(Weblog weblog, int offset, int limit) {
        TypedQuery<TagStat> query;
        List queryResults;
        
        query = strategy.getNamedQuery("WeblogEntryTagAggregate.getPopularTagsByWeblog", TagStat.class);
        query.setParameter(1, weblog);

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

    @Override
    public List<TagStat> getTags(Weblog website, String sortBy, String startsWith, int offset, int limit) {
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
    
    @Override
    public boolean getTagExists(String tag, Weblog weblog) {
        if (tag == null) {
            return false;
        }

        List<Object> params = new ArrayList<>(2);
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT DISTINCT w.name ");
        queryString.append("FROM WeblogEntryTagAggregate w WHERE w.name = ?1");
        params.add(tag);

        if (weblog != null) {
            queryString.append(" AND w.weblog = ?2");
            params.add(weblog);
        }
        
        TypedQuery<String> q = strategy.getDynamicQuery(queryString.toString(), String.class);
        for (int j=0; j<params.size(); j++) {
            q.setParameter(j+1, params.get(j));
        }
        List<String> results = q.getResultList();
        
        // OK if at least one article matches the tag
        return (results != null && results.size() > 0);
    }

    @Override
    public long getCommentCount() {
        TypedQuery<Long> q = strategy.getNamedQuery("WeblogEntryComment.getCountAllDistinctByStatus", Long.class);
        q.setParameter(1, ApprovalStatus.APPROVED);
        return q.getResultList().get(0);
    }

    @Override
    public long getCommentCount(Weblog weblog) {
        TypedQuery<Long> q = strategy.getNamedQuery("WeblogEntryComment.getCountDistinctByWeblog&Status", Long.class);
        q.setParameter(1, weblog);
        q.setParameter(2, ApprovalStatus.APPROVED);
        return q.getResultList().get(0);
    }

    @Override
    public long getEntryCount() {
        TypedQuery<Long> q = strategy.getNamedQuery("WeblogEntry.getCountDistinctByStatus", Long.class);
        q.setParameter(1, PubStatus.PUBLISHED);
        return q.getResultList().get(0);
    }

    @Override
    public long getEntryCount(Weblog website) {
        TypedQuery<Long> q = strategy.getNamedQuery("WeblogEntry.getCountDistinctByStatus&Weblog", Long.class);
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
    private static StringBuilder appendConjuctionToWhereclause(StringBuilder whereClause, String expression) {
        if (whereClause.length() != 0 && expression.length() != 0) {
            whereClause.append(" AND ");
        }
        return whereClause.append(expression);
    }

    @Override
    public String applyWeblogEntryPlugins(WeblogEntry entry, String str) {
        String ret = str;
        WeblogEntry copy = new WeblogEntry(entry);
        List<String> entryPlugins = copy.getPluginsList();
        if (entryPlugins != null) {
            for (WeblogEntryPlugin inPlugin : weblogEntryPlugins) {
                if (entryPlugins.contains(inPlugin.getName())) {
                    try {
                        ret = inPlugin.render(entry, ret);
                    } catch (Exception e) {
                        log.error("ERROR from plugin: {}", inPlugin.getName());
                    }
                }
            }
        }
        return HTMLSanitizer.conditionallySanitize(ret);
    }

    @Override
    public String applyCommentPlugins(WeblogEntryComment comment, String text) {
        if(comment == null || text == null) {
            throw new IllegalArgumentException("comment cannot be null");
        }
        String content = text;
        if (commentPlugins.size() > 0) {
            for (WeblogEntryCommentPlugin plugin : commentPlugins) {
                if(comment.getPlugins() != null && comment.getPlugins().contains(plugin.getId())) {
                    log.debug("Invoking comment plugin {}", plugin.getId());
                    content = plugin.render(comment, content);
                }
            }
        }
        return content;
    }

}
