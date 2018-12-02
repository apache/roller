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
package org.tightblog.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.domain.AtomEnclosure;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogCategory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.repository.WeblogEntryCommentRepository;
import org.tightblog.repository.WeblogEntryRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;
import org.tightblog.util.Utilities;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Weblog entry and comment management.
 */
@Component
public class WeblogEntryManager {

    private static Logger log = LoggerFactory.getLogger(WeblogEntryManager.class);

    private WeblogRepository weblogRepository;
    private WeblogEntryRepository weblogEntryRepository;
    private WeblogEntryCommentRepository weblogEntryCommentRepository;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;
    private URLService urlService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public WeblogEntryManager(WeblogRepository weblogRepository, WeblogEntryRepository weblogEntryRepository,
                              WeblogEntryCommentRepository weblogEntryCommentRepository,
                              URLService urlService,
                              WebloggerPropertiesRepository webloggerPropertiesRepository) {
        this.weblogRepository = weblogRepository;
        this.weblogEntryRepository = weblogEntryRepository;
        this.weblogEntryCommentRepository = weblogEntryCommentRepository;
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.urlService = urlService;
    }

    /**
     * Save comment.
     *
     * @param refreshWeblog true if weblog should be marked for cache update, i.e., likely
     *                      rendering change to accommodate new or removed comment, vs. one
     *                      still requiring moderation.
     */
    public void saveComment(WeblogEntryComment comment, boolean refreshWeblog) {
        comment.setWeblog(comment.getWeblogEntry().getWeblog());
        weblogEntryCommentRepository.saveAndFlush(comment);
        if (refreshWeblog) {
            comment.getWeblog().invalidateCache();
            weblogRepository.saveAndFlush(comment.getWeblog());
        }
    }

    /**
     * Remove comment and invalidate its parent weblog's cache
     */
    public void removeComment(WeblogEntryComment comment) {
        weblogEntryCommentRepository.deleteById(comment.getId());
        comment.getWeblogEntry().getWeblog().invalidateCache();
        weblogRepository.saveAndFlush(comment.getWeblogEntry().getWeblog());
    }

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
        if (WeblogEntry.PubStatus.PUBLISHED.equals(entry.getStatus()) &&
                entry.getPubTime().isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
            entry.setStatus(WeblogEntry.PubStatus.SCHEDULED);
        }

        // Store value object (creates new or updates existing)
        Instant now = Instant.now();
        entry.setUpdateTime(now);
        entry.getWeblog().setLastModified(now);

        weblogEntryRepository.save(entry);
        weblogRepository.saveAndFlush(entry.getWeblog());
    }

    public void removeWeblogEntry(WeblogEntry entry) {
        weblogEntryCommentRepository.deleteByWeblogEntry(entry);
        weblogEntryRepository.delete(entry);
        entry.getWeblog().invalidateCache();
        weblogRepository.saveAndFlush(entry.getWeblog());
    }

    /**
     * Find nearest published blog entry before or after a given target date.  Useful for date-based
     * pagination where it is desired to determine the next or previous time period that
     * contains a blog entry.
     *
     * @param weblog weblog whose entries to search
     * @param categoryName Category name that entry must belong to or null if entry may belong to any category
     * @param targetDate Earliest (if succeeding = true) or latest (succeeding = false) publish time of blog entry
     * @param succeeding If true, find the first blog entry whose publish time comes after the targetDate, if false, the
     *                   entry closest to but before the targetDate.
     * @return WeblogEntry meeting the above criteria or null if no entry matches
     */
    public WeblogEntry findNearestWeblogEntry(Weblog weblog, String categoryName, LocalDateTime targetDate,
                                              boolean succeeding) {
        WeblogEntry nearestEntry = null;

        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(weblog);
        wesc.setCategoryName(categoryName);
        wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        wesc.setMaxResults(1);
        if (succeeding) {
            wesc.setStartDate(targetDate.atZone(ZoneId.systemDefault()).toInstant());
            wesc.setSortOrder(WeblogEntrySearchCriteria.SortOrder.ASCENDING);
        } else {
            wesc.setEndDate(targetDate.atZone(ZoneId.systemDefault()).toInstant());
            wesc.setSortOrder(WeblogEntrySearchCriteria.SortOrder.DESCENDING);
        }
        List entries = getWeblogEntries(wesc);
        if (entries.size() > 0) {
            nearestEntry = (WeblogEntry) entries.get(0);
        }
        return nearestEntry;
    }

    /**
     * Get the WeblogEntry following, chronologically, the current entry.
     *
     * @param current The "current" WeblogEntry
     */
    public WeblogEntry getNextPublishedEntry(WeblogEntry current) {
        WeblogEntry entry = null;
        List<WeblogEntry> entryList = getNextPrevEntries(current, true);
        if (entryList != null && entryList.size() > 0) {
            entry = entryList.get(0);
        }
        return entry;
    }

    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     *
     * @param current The "current" WeblogEntry
     */
    public WeblogEntry getPreviousPublishedEntry(WeblogEntry current) {
        WeblogEntry entry = null;
        List<WeblogEntry> entryList = getNextPrevEntries(current, false);
        if (entryList != null && entryList.size() > 0) {
            entry = entryList.get(0);
        }
        return entry;
    }

    private List<WeblogEntry> getNextPrevEntries(WeblogEntry current, boolean next) {

        if (current == null || current.getPubTime() == null) {
            return Collections.emptyList();
        }

        TypedQuery<WeblogEntry> query;

        List<Object> params = new ArrayList<>();
        int size = 0;
        String queryString = "SELECT e FROM WeblogEntry e WHERE ";
        StringBuilder whereClause = new StringBuilder();

        params.add(size++, current.getWeblog());
        whereClause.append("e.weblog = ?").append(size);

        params.add(size++, current.getId());
        whereClause.append(" AND e.id <> ?").append(size);

        params.add(size++, WeblogEntry.PubStatus.PUBLISHED);
        whereClause.append(" AND e.status = ?").append(size);

        params.add(size++, current.getPubTime());
        if (next) {
            whereClause.append(" AND e.pubTime >= ?").append(size);
            whereClause.append(" ORDER BY e.pubTime ASC, e.id ASC");
        } else {
            whereClause.append(" AND e.pubTime <= ?").append(size);
            whereClause.append(" ORDER BY e.pubTime DESC, e.id DESC");
        }

        query = entityManager.createQuery(queryString + whereClause.toString(), WeblogEntry.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        query.setMaxResults(1);

        return query.getResultList();
    }

    private QueryData createEntryQueryString(WeblogEntrySearchCriteria criteria) {
        QueryData qd = new QueryData();
        int size = 0;

        qd.queryString = "SELECT e FROM WeblogEntry e";

        if (criteria.getTag() == null) {
            qd.queryString += " WHERE 1=1 ";
        } else {
            // subquery to avoid this problem with Derby: http://stackoverflow.com/a/480536
            qd.queryString += " WHERE EXISTS ( Select 1 from WeblogEntryTag t " +
                    "where t.weblogEntry.id = e.id AND (";

            qd.params.add(size++, criteria.getTag());
            qd.queryString += " t.name = ?" + size;
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

        qd.queryString += " ORDER BY ";
        qd.queryString += WeblogEntrySearchCriteria.SortBy.UPDATE_TIME.equals(criteria.getSortBy()) ?
                " e.updateTime " : " e.pubTime ";
        String sortOrder = WeblogEntrySearchCriteria.SortOrder.ASCENDING.equals(criteria.getSortOrder()) ? " ASC " : " DESC ";
        qd.queryString += sortOrder + ", e.id " + sortOrder;

        return qd;
    }

    /**
     * Get WeblogEntries by offset/length as list in reverse chronological order.
     * The range offset and list arguments enable paging through query results.
     *
     * @param criteria WeblogEntrySearchCriteria object listing desired search parameters
     * @return List of WeblogEntry objects in order specified by search criteria
     */
    public List<WeblogEntry> getWeblogEntries(WeblogEntrySearchCriteria criteria) {
        QueryData qd = createEntryQueryString(criteria);

        TypedQuery<WeblogEntry> query = entityManager.createQuery(qd.queryString, WeblogEntry.class);
        for (int i = 0; i < qd.params.size(); i++) {
            query.setParameter(i + 1, qd.params.get(i));
        }

        if (criteria.getOffset() != 0) {
            query.setFirstResult(criteria.getOffset());
        }
        if (criteria.getMaxResults() != -1) {
            query.setMaxResults(criteria.getMaxResults());
        }

        List<WeblogEntry> results = query.getResultList();

        if (criteria.isCalculatePermalinks()) {
            results = results.stream()
                    .peek(we -> we.setPermalink(urlService.getWeblogEntryURL(we)))
                    .collect(Collectors.toList());
        }

        return results;
    }

    public WeblogEntry getWeblogEntryByAnchor(Weblog weblog, String anchor) {
        WeblogEntry entry = weblogEntryRepository.findByWeblogAndAnchor(weblog, anchor);
        if (entry != null) {
            entry.setCommentRepository(weblogEntryCommentRepository);
        }
        return entry;
    }

    /**
     * Create unique anchor for weblog entry.
     */
    public String createAnchor(WeblogEntry entry) {
        // Check for uniqueness of anchor
        String base = createAnchorBase(entry);
        String name = base;
        int count = 0;

        while (true) {
            if (count++ > 0) {
                name = base + count;
            }
            WeblogEntry entryTest = weblogEntryRepository.findByWeblogAndAnchor(entry.getWeblog(), name);
            if (entryTest == null) {
                break;
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

    private QueryData createCommentQueryString(CommentSearchCriteria csc) {
        QueryData cqd = new QueryData();
        int size = 0;

        cqd.queryString = "SELECT c FROM WeblogEntryComment c";

        StringBuilder whereClause = new StringBuilder();
        if (csc.getEntry() != null) {
            cqd.params.add(size++, csc.getEntry());
            appendConjuctionToWhereClause(whereClause, "c.weblogEntry = ?").append(size);
        } else {
            if (csc.getWeblog() != null) {
                cqd.params.add(size++, csc.getWeblog());
                appendConjuctionToWhereClause(whereClause, "c.weblog = ?").append(size);
            }
            if (csc.getCategoryName() != null) {
                cqd.params.add(size++, csc.getCategoryName());
                appendConjuctionToWhereClause(whereClause, "c.weblogEntry.category.name = ?").append(size);
            }
        }

        if (csc.getSearchText() != null) {
            cqd.params.add(size++, "%" + csc.getSearchText().toUpperCase() + "%");
            appendConjuctionToWhereClause(whereClause, "upper(c.content) LIKE ?").append(size);
        }

        if (csc.getStartDate() != null) {
            cqd.params.add(size++, csc.getStartDate());
            appendConjuctionToWhereClause(whereClause, "c.postTime >= ?").append(size);
        }

        if (csc.getEndDate() != null) {
            cqd.params.add(size++, csc.getEndDate());
            appendConjuctionToWhereClause(whereClause, "c.postTime <= ?").append(size);
        }

        if (csc.getStatus() != null) {
            cqd.params.add(size++, csc.getStatus());
            appendConjuctionToWhereClause(whereClause, "c.status = ?").append(size);
        }

        if (whereClause.length() != 0) {
            cqd.queryString += " WHERE " + whereClause.toString();
        }

        if (csc.isReverseChrono()) {
            cqd.queryString += " ORDER BY c.postTime DESC";
        } else {
            cqd.queryString += " ORDER BY c.postTime ASC";
        }

        return cqd;
    }

    private class QueryData {
        String queryString;
        List<Object> params = new ArrayList<>();
    }

    /**
     * Get Weblog Entries grouped by calendar day.
     *
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return Map of Lists of WeblogEntries keyed by calendar day
     */
    public Map<LocalDate, List<WeblogEntry>> getDateToWeblogEntryMap(WeblogEntrySearchCriteria wesc) {
        Map<LocalDate, List<WeblogEntry>> map = new TreeMap<>(Collections.reverseOrder());

        List<WeblogEntry> entries = getWeblogEntries(wesc);

        for (WeblogEntry entry : entries) {
            entry.setCommentRepository(weblogEntryCommentRepository);
            LocalDate tmp = entry.getPubTime() == null ? LocalDate.now() :
                    entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate();
            List<WeblogEntry> dayEntries = map.computeIfAbsent(tmp, k -> new ArrayList<>());
            dayEntries.add(entry);
        }
        return map;
    }

    /**
     * Generic comments query method.
     *
     * @param csc CommentSearchCriteria object with fields indicating search criteria
     * @return list of comments fitting search criteria
     */
    public List<WeblogEntryComment> getComments(CommentSearchCriteria csc) {
        QueryData cqd = createCommentQueryString(csc);

        TypedQuery<WeblogEntryComment> query = entityManager.createQuery(cqd.queryString, WeblogEntryComment.class);
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

    /**
     * Determine whether further comments for a particular blog entry are allowed.
     * @return true if additional comments may be made, false otherwise.
     */
    public boolean canSubmitNewComments(WeblogEntry entry) {
        if (!entry.isPublished()) {
            return false;
        }
        if (WebloggerProperties.CommentPolicy.NONE.equals(
                webloggerPropertiesRepository.findOrNull().getCommentPolicy())) {
            return false;
        }
        if (WebloggerProperties.CommentPolicy.NONE.equals(entry.getWeblog().getAllowComments())) {
            return false;
        }
        if (entry.getCommentDays() == 0) {
            return false;
        }
        if (entry.getCommentDays() < 0) {
            return true;
        }
        boolean ret = false;

        Instant inPubTime = entry.getPubTime();
        if (inPubTime != null) {
            Instant lastCommentDay = inPubTime.plus(entry.getCommentDays(), ChronoUnit.DAYS);
            if (Instant.now().isBefore(lastCommentDay)) {
                ret = true;
            }
        }
        return ret;
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
    private static StringBuilder appendConjuctionToWhereClause(StringBuilder whereClause, String expression) {
        if (whereClause.length() != 0 && expression.length() != 0) {
            whereClause.append(" AND ");
        }
        return whereClause.append(expression);
    }

    /**
     * Process the blog text based on whether Commonmark and/or JSoup tag
     * filtering is activated.  This method must *NOT* alter the contents of
     * the original entry object, to allow the blogger to return to his original
     * text for additional editing as desired.
     *
     * @param format Weblog.EditFormat indicating input format of string
     * @param str   String to which to apply processing.
     * @return the transformed text
     */
    public String processBlogText(Weblog.EditFormat format, String str) {
        String ret = str;

        if (Weblog.EditFormat.COMMONMARK.equals(format) && ret != null) {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(ret);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            ret = renderer.render(document);
        }

        if (ret != null) {
            WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
            Whitelist whitelist = props.getBlogHtmlPolicy().getWhitelist();

            if (whitelist != null) {
                ret = Jsoup.clean(ret, whitelist);
            }
        }

        return ret;
    }

    /**
     * Create an Atom enclosure element for the resource (usually podcast or other
     * multimedia) at the specified URL.
     *
     * @param url web URL where the resource is located.
     * @return AtomEnclosure element for the resource
     */
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

    /**
     * Turn off further notifications to a blog commenter who requested "notify me"
     * for future comments for a particular weblog entry.
     * @param commentId weblog entry id where commenter commented
     * @return Pair &lt;String, Boolean> = String is blog entry title or null if not found,
     *         Boolean is true if subscribed user found (& hence unsubscribed), false if user
     *         not found or blog entry not found.
     */
    @Transactional
    public Pair<String, Boolean> stopNotificationsForCommenter(String commentId) {
        boolean found = false;
        String blogEntryTitle = null;

        WeblogEntryComment commentWithUnsubscribingUser = weblogEntryCommentRepository.findByIdOrNull(commentId);

        if (commentWithUnsubscribingUser != null) {
            // get entry
            WeblogEntry entry = commentWithUnsubscribingUser.getWeblogEntry();
            blogEntryTitle = entry.getTitle();

            // turn off notify on all comments for this entry by user
            List<WeblogEntryComment> comments = weblogEntryCommentRepository.findByWeblogEntry(entry);

            for (WeblogEntryComment comment : comments) {
                if (comment.getNotify() && comment.getEmail().equalsIgnoreCase(commentWithUnsubscribingUser.getEmail())) {
                    comment.setNotify(false);
                    weblogEntryCommentRepository.save(comment);
                    found = true;
                }
            }
        }
        return Pair.of(blogEntryTitle, found);
    }
}
