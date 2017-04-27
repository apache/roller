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
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.AtomEnclosure;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.util.Utilities;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class JPAWeblogEntryManagerImpl implements WeblogEntryManager {

    private static Logger log = LoggerFactory.getLogger(JPAWeblogEntryManagerImpl.class);

    private final PingTargetManager pingTargetManager;
    private final JPAPersistenceStrategy strategy;

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    // cached mapping of entryAnchors -> entryIds
    private Map<String, String> entryAnchorToIdMap = Collections.synchronizedMap(new HashMap<>());

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
                entry.getPubTime().isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
            entry.setStatus(PubStatus.SCHEDULED);
        }

        // Store value object (creates new or updates existing)
        entry.setUpdateTime(Instant.now());

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

    private List<WeblogEntry> getNextPrevEntries(WeblogEntry current, String catName, boolean next) {

        if (current == null || current.getPubTime() == null) {
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
            params.add(size++, current.getPubTime());
            whereClause.append(" AND e.pubTime < ?").append(size);
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
        query.setMaxResults(1);

        return query.getResultList();
    }

    private QueryData createEntryQueryString(WeblogEntrySearchCriteria criteria, boolean countOnly) {
        QueryData qd = new QueryData();
        int size = 0;

        qd.queryString = "SELECT ".concat(countOnly ? "count(e)" : "e").concat(" FROM WeblogEntry e");

        if (criteria.getTags() == null || criteria.getTags().size() == 0) {
            qd.queryString += " WHERE 1=1 ";
        } else {
            // subquery to avoid this problem with Derby: http://stackoverflow.com/a/480536
            qd.queryString += " WHERE EXISTS ( Select 1 from WeblogEntryTag t " +
                    "where t.weblogEntry.id = e.id AND (";

            boolean isFirst = true;
            for (String tagName : criteria.getTags()) {
                if (!isFirst) {
                    qd.queryString += " OR ";
                }
                qd.params.add(size++, tagName);
                qd.queryString += " t.name = ?" + size;
                isFirst = false;
            }
            qd.queryString += ")) ";
        }

        if (criteria.getWeblog() != null) {
            qd.params.add(size++, criteria.getWeblog().getId());
            qd.queryString += "AND e.weblog.id = ?" + size;
        }

        qd.params.add(size++, Boolean.TRUE);
        qd.queryString += " AND e.weblog.visible = ?" + size;

        if (criteria.getUser() != null) {
            qd.params.add(size++, criteria.getUser().getUserName());
            qd.queryString += " AND e.creatorUserName = ?" + size;
        }

        if (criteria.getStartDate() != null) {
            qd.params.add(size++, criteria.getStartDate());
            qd.queryString += " AND e.pubTime >= ?" + size;
        }

        if (criteria.getEndDate() != null) {
            qd.params.add(size++, criteria.getEndDate());
            qd.queryString += " AND e.pubTime <= ?" + size;
        }

        if (!StringUtils.isEmpty(criteria.getCategoryName())) {
            qd.params.add(size++, criteria.getCategoryName());
            qd.queryString += " AND e.category.name = ?" + size;
        }

        if (criteria.getStatus() != null) {
            qd.params.add(size++, criteria.getStatus());
            qd.queryString += " AND e.status = ?" + size;
        }

        if (StringUtils.isNotEmpty(criteria.getText())) {
            qd.params.add(size++, '%' + criteria.getText() + '%');
            qd.queryString += " AND ( e.text LIKE ?" + size;
            qd.queryString += "    OR e.summary LIKE ?" + size;
            qd.queryString += "    OR e.title LIKE ?" + size;
            qd.queryString += ") ";
        }

        if (!countOnly) {
            if (criteria.getSortBy() != null && criteria.getSortBy().equals(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME)) {
                qd.queryString += " ORDER BY e.updateTime ";
            } else {
                qd.queryString += " ORDER BY e.pubTime ";
            }

            if (criteria.getSortOrder() != null && criteria.getSortOrder().equals(WeblogEntrySearchCriteria.SortOrder.ASCENDING)) {
                qd.queryString += "ASC ";
            } else {
                qd.queryString += "DESC ";
            }
        }

        return qd;
    }

    @Override
    public List<WeblogEntry> getWeblogEntries(WeblogEntrySearchCriteria criteria) {
        QueryData qd = createEntryQueryString(criteria, false);

        TypedQuery<WeblogEntry> query = strategy.getDynamicQuery(qd.queryString, WeblogEntry.class);
        for (int i = 0; i < qd.params.size(); i++) {
            query.setParameter(i + 1, qd.params.get(i));
        }

        if (criteria.getOffset() != 0) {
            query.setFirstResult(criteria.getOffset());
        }
        if (criteria.getMaxResults() != -1) {
            query.setMaxResults(criteria.getMaxResults());
        }

        return query.getResultList();
    }

    @Override
    public long getEntryCount(WeblogEntrySearchCriteria wesc) {
        QueryData cqd = createEntryQueryString(wesc, true);

        TypedQuery<Long> query = strategy.getDynamicQuery(cqd.queryString, Long.class);

        for (int i = 0; i < cqd.params.size(); i++) {
            query.setParameter(i + 1, cqd.params.get(i));
        }

        return query.getResultList().get(0);
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

            WeblogEntry entry = this.getWeblogEntry(this.entryAnchorToIdMap.get(mappingKey), false);
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
        String base;
        if (!StringUtils.isEmpty(entry.getTitle())) {
            base = Utilities.replaceNonAlphanumeric(entry.getTitle(), ' ').trim();
        } else {
            // try text
            base = Utilities.replaceNonAlphanumeric(entry.getText(), ' ').trim();
        }

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

        return base;
    }

    private QueryData createCommentQueryString(CommentSearchCriteria csc, boolean countOnly) {
        QueryData cqd = new QueryData();
        int size = 0;

        cqd.queryString = "SELECT ".concat(countOnly ? "count(c)" : "c").concat(" FROM WeblogEntryComment c");

        StringBuilder whereClause = new StringBuilder();
        if (csc.getEntry() != null) {
            cqd.params.add(size++, csc.getEntry());
            appendConjuctionToWhereclause(whereClause, "c.weblogEntry = ?").append(size);
        } else {
            if (csc.getWeblog() != null) {
                cqd.params.add(size++, csc.getWeblog());
                appendConjuctionToWhereclause(whereClause, "c.weblogEntry.weblog = ?").append(size);
            }
            if (csc.getCategoryName() != null) {
                cqd.params.add(size++, csc.getCategoryName());
                appendConjuctionToWhereclause(whereClause, "c.weblogEntry.category.name = ?").append(size);
            }
        }

        if (csc.getSearchText() != null) {
            cqd.params.add(size++, "%" + csc.getSearchText().toUpperCase() + "%");
            appendConjuctionToWhereclause(whereClause, "upper(c.content) LIKE ?").append(size);
        }

        if (csc.getStartDate() != null) {
            cqd.params.add(size++, csc.getStartDate());
            appendConjuctionToWhereclause(whereClause, "c.postTime >= ?").append(size);
        }

        if (csc.getEndDate() != null) {
            cqd.params.add(size++, csc.getEndDate());
            appendConjuctionToWhereclause(whereClause, "c.postTime <= ?").append(size);
        }

        if (csc.getStatus() != null) {
            cqd.params.add(size++, csc.getStatus());
            appendConjuctionToWhereclause(whereClause, "c.status = ?").append(size);
        }

        if (whereClause.length() != 0) {
            cqd.queryString += " WHERE " + whereClause.toString();
        }

        if (!countOnly) {
            if (csc.isReverseChrono()) {
                cqd.queryString += " ORDER BY c.postTime DESC";
            } else {
                cqd.queryString += " ORDER BY c.postTime ASC";
            }
        }

        return cqd;
    }

    private class QueryData {
        String queryString;
        List<Object> params = new ArrayList<>();
    }


    @Override
    public List<WeblogEntryComment> getComments(CommentSearchCriteria csc) {
        QueryData cqd = createCommentQueryString(csc, false);

        TypedQuery<WeblogEntryComment> query = strategy.getDynamicQuery(cqd.queryString, WeblogEntryComment.class);
        if (csc.getOffset() != 0) {
            query.setFirstResult(csc.getOffset());
        }
        if (csc.getMaxResults() != -1) {
            query.setMaxResults(csc.getMaxResults());
        }
        for (int i = 0; i < cqd.params.size(); i++) {
            query.setParameter(i + 1, cqd.params.get(i));
        }
        return query.getResultList();
    }

    @Override
    public long getCommentCount(CommentSearchCriteria csc) {
        QueryData cqd = createCommentQueryString(csc, true);

        TypedQuery<Long> query = strategy.getDynamicQuery(cqd.queryString, Long.class);

        for (int i = 0; i < cqd.params.size(); i++) {
            query.setParameter(i + 1, cqd.params.get(i));
        }

        return query.getResultList().get(0);
    }

    @Override
    public WeblogEntryComment getComment(String id) {
        return this.strategy.load(WeblogEntryComment.class, id);
    }

    @Override
    public WeblogEntry getWeblogEntry(String id, boolean bypassCache) {
        return strategy.load(WeblogEntry.class, id, bypassCache);
    }

    @Override
    public Map<LocalDate, List<WeblogEntry>> getWeblogEntryObjectMap(WeblogEntrySearchCriteria wesc) {
        TreeMap<LocalDate, List<WeblogEntry>> map = new TreeMap<>(Collections.reverseOrder());

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        for (WeblogEntry entry : entries) {
            LocalDate tmp = entry.getPubTime() == null ? LocalDate.now() :
                    entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate();
            List<WeblogEntry> dayEntries = map.computeIfAbsent(tmp, k -> new ArrayList<>());
            dayEntries.add(entry);
        }
        return map;
    }

    @Override
    public Map<LocalDate, String> getWeblogEntryStringMap(WeblogEntrySearchCriteria wesc) {
        TreeMap<LocalDate, String> map = new TreeMap<>(Collections.reverseOrder());

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Utilities.FORMAT_8CHARS);

        for (WeblogEntry entry : entries) {
            LocalDate maybeDate = entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate();
            map.putIfAbsent(maybeDate, formatter.format(maybeDate));
        }
        return map;
    }

    @Override
    public WeblogEntry getNextEntry(WeblogEntry current, String catName) {
        WeblogEntry entry = null;
        List<WeblogEntry> entryList = getNextPrevEntries(current, catName, true);
        if (entryList != null && entryList.size() > 0) {
            entry = entryList.get(0);
        }
        return entry;
    }

    @Override
    public WeblogEntry getPreviousEntry(WeblogEntry current, String catName) {
        WeblogEntry entry = null;
        List<WeblogEntry> entryList = getNextPrevEntries(current, catName, false);
        if (entryList != null && entryList.size() > 0) {
            entry = entryList.get(0);
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

    /**
     * Appends given expression to given whereClause. If whereClause already
     * has other conditions, an " AND " is also appended before appending
     * the expression
     *
     * @param whereClause The given where Clauuse
     * @param expression  The given expression
     * @return the whereClause.
     */
    private static StringBuilder appendConjuctionToWhereclause(StringBuilder whereClause, String expression) {
        if (whereClause.length() != 0 && expression.length() != 0) {
            whereClause.append(" AND ");
        }
        return whereClause.append(expression);
    }

    @Override
    public String processBlogText(Weblog.EditFormat format, String str) {
        String ret = str;

        if (Weblog.EditFormat.COMMONMARK.equals(format) && ret != null) {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(ret);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            ret = renderer.render(document);
        }

        if (ret != null) {
            WebloggerProperties props = strategy.getWebloggerProperties();
            Whitelist whitelist = props.getBlogHtmlPolicy().getWhitelist();

            if (whitelist != null) {
                ret = Jsoup.clean(ret, whitelist);
            }
        }

        return ret;
    }

    @Override
    public AtomEnclosure generateEnclosure(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        }

        AtomEnclosure resource;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            int response = con.getResponseCode();
            String message = con.getResponseMessage();

            if (response != 200) {
                // Bad Response
                log.debug("Mediacast error {}:{} from url {}", response, message, url);
                throw new IllegalArgumentException("entryEdit.mediaCastResponseError");
            } else {
                String contentType = con.getContentType();
                long length = con.getContentLength();

                if (contentType == null || length == -1) {
                    // Incomplete
                    log.debug("Response valid, but contentType or length is invalid");
                    throw new IllegalArgumentException("entryEdit.mediaCastLacksContentTypeOrLength");
                }

                resource = new AtomEnclosure(url, contentType, length);
                log.debug("Valid mediacast resource = {}", resource.toString());

            }
        } catch (MalformedURLException mfue) {
            // Bad URL
            log.debug("Malformed MediaCast url: {}", url);
            throw new IllegalArgumentException("entryEdit.mediaCastUrlMalformed", mfue);
        } catch (Exception e) {
            // Check Failed
            log.error("ERROR while checking MediaCast URL: {}: {}", url, e.getMessage());
            throw new IllegalArgumentException("entryEdit.mediaCastFailedFetchingInfo", e);
        }
        return resource;
    }

}
