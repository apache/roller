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
package org.tightblog.business;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserStatus;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogBookmark;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.pojos.WeblogEntryTag;
import org.tightblog.pojos.WeblogEntryTagAggregate;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WebloggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.repository.WeblogCategoryRepository;
import org.tightblog.repository.WeblogEntryTagRepository;
import org.tightblog.repository.WeblogTemplateRepository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component("weblogManager")
public class WeblogManagerImpl implements WeblogManager {

    private static Logger log = LoggerFactory.getLogger(WeblogManagerImpl.class);

    @Autowired
    private WeblogCategoryRepository weblogCategoryRepository;

    @Autowired
    private WeblogEntryTagRepository weblogEntryTagRepository;

    @Autowired
    private WeblogTemplateRepository weblogTemplateRepository;

    @Autowired
    private UserManager userManager;

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    @Autowired
    private IndexManager indexManager;

    @Autowired
    private MediaFileManager mediaFileManager;

    @Autowired
    private JPAPersistenceStrategy strategy;

    @Value("#{'${newblog.blogroll}'.split(',')}")
    private Set<String> newBlogBlogroll;

    @Value("#{'${newblog.categories}'.split(',')}")
    private Set<String> newBlogCategories;


    // Map of each weblog and its extra hit count that has had additional accesses since the
    // last scheduled updateHitCounters() call.
    private Map<String, Long> hitsTally = Collections.synchronizedMap(new HashMap<>());

    // cached mapping of weblogHandles -> weblogIds
    private Map<String, String> weblogHandleToIdMap = new Hashtable<>();

    @Autowired
    public WeblogManagerImpl() {
    }

    @Override
    public void saveWeblog(Weblog weblog) {
        weblog.setLastModified(Instant.now());
        strategy.merge(weblog);

        // update last weblog change so any site weblog knows it needs to update
        WebloggerProperties props = strategy.getWebloggerProperties();
        props.setLastWeblogChange(Instant.now());
        strategy.store(props);
        strategy.flush();
    }

    @Override
    public void removeWeblog(Weblog weblog) {
        // remove contents first, then remove weblog
        removeWeblogContents(weblog);
        strategy.remove(weblog);

        // update last weblog change so any site weblog knows it needs to update
        WebloggerProperties props = strategy.getWebloggerProperties();
        props.setLastWeblogChange(Instant.now());
        strategy.store(props);

        strategy.flush();

        // remove entry from cache mapping
        weblogHandleToIdMap.remove(weblog.getHandle());
    }

    /**
     * convenience method for removing contents of a weblog.
     */
    private void removeWeblogContents(Weblog weblog) {
        weblogTemplateRepository.deleteByWeblog(weblog);
        mediaFileManager.removeAllFiles(weblog);

        // remove entries
        TypedQuery<WeblogEntry> refQuery = strategy.getNamedQuery("WeblogEntry.getByWeblog", WeblogEntry.class);
        refQuery.setParameter(1, weblog);
        List<WeblogEntry> entries = refQuery.getResultList();
        for (WeblogEntry entry : entries) {
            weblogEntryManager.removeWeblogEntry(entry);
        }
        this.strategy.flush();

        // remove permissions
        for (UserWeblogRole role : userManager.getWeblogRolesIncludingPending(weblog)) {
            userManager.revokeWeblogRole(role);
        }

        // remove indexing
        indexManager.updateIndex(weblog, true);

        // check if main blog, disconnect if it is
        WebloggerProperties props = strategy.getWebloggerProperties();
        Weblog test = props.getMainBlog();
        if (test != null && test.getId().equals(weblog.getId())) {
            props.setMainBlog(null);
            strategy.store(props);
        }
    }

    @Override
    public void addWeblog(Weblog newWeblog) {
        this.strategy.store(newWeblog);
        this.strategy.flush();
        this.addWeblogContents(newWeblog);
    }

    @Override
    public String getAnalyticsTrackingCode(Weblog weblog) {
        WebloggerProperties props = strategy.getWebloggerProperties();

        if (props.isUsersOverrideAnalyticsCode() &&
                !StringUtils.isBlank(weblog.getAnalyticsCode())) {
            return weblog.getAnalyticsCode();
        } else {
            return StringUtils.defaultIfEmpty(props.getDefaultAnalyticsCode(), "");
        }
    }

    private void addWeblogContents(Weblog newWeblog) {

        if (getWeblogCount() == 1) {
            // first weblog, let's make it the frontpage one.
            WebloggerProperties props = strategy.getWebloggerProperties();
            props.setMainBlog(newWeblog);
            this.strategy.store(props);
        }

        // grant weblog creator OWNER permission
        userManager.grantWeblogRole(newWeblog.getCreator(), newWeblog, WeblogRole.OWNER, false);

        if (!ObjectUtils.isEmpty(newBlogCategories)) {
            for (String category : newBlogCategories) {
                WeblogCategory c = new WeblogCategory(newWeblog, category);
                newWeblog.addCategory(c);
                this.strategy.store(c);
            }
        }

        this.strategy.store(newWeblog);

        // add default bookmarks
        if (!ObjectUtils.isEmpty(newBlogBlogroll)) {
            for (String splitItem : newBlogBlogroll) {
                String[] rollitems = splitItem.split("\\|");
                if (rollitems.length > 1) {
                    WeblogBookmark b = new WeblogBookmark(
                            newWeblog,
                            rollitems[0],
                            rollitems[1].trim(), ""
                    );
                    newWeblog.addBookmark(b);
                    this.strategy.store(b);
                }
            }
        }

        // create initial media file directory named "default"
        mediaFileManager.createMediaDirectory(newWeblog, "default");

        // flush so that all data up to this point can be available in db
        this.strategy.flush();

    }

    @Override
    public Weblog getWeblog(String id) {
        return this.strategy.load(Weblog.class, id);
    }

    @Override
    public Weblog getWeblogByHandle(String handle) {
        return getWeblogByHandle(handle, Boolean.TRUE);
    }

    @Override
    public Weblog getWeblogByHandle(String handle, Boolean visible) {

        if (handle == null) {
            throw new IllegalArgumentException("Handle cannot be null");
        }

        // check cache first
        // NOTE: if we ever allow changing handles then this needs updating
        if (weblogHandleToIdMap.containsKey(handle)) {
            Weblog weblog = getWeblog(weblogHandleToIdMap.get(handle));

            if (weblog != null) {
                // only return weblog if enabled status matches
                if (visible == null || visible.equals(weblog.getVisible())) {
                    log.debug("weblogHandleToId CACHE HIT - {}", handle);
                    return weblog;
                }
            } else {
                // id no longer maps to an existing weblog, remove it from cache
                weblogHandleToIdMap.remove(handle);
            }
        }

        TypedQuery<Weblog> query = strategy.getNamedQuery("Weblog.getByHandle", Weblog.class);
        query.setParameter(1, handle);
        Weblog weblog;
        try {
            weblog = query.getSingleResult();
        } catch (NoResultException e) {
            weblog = null;
        }

        // add mapping to cache
        if (weblog != null) {
            log.trace("weblogHandleToId CACHE MISS - {}", handle);
            weblogHandleToIdMap.put(weblog.getHandle(), weblog.getId());
        }

        if (weblog != null && (visible == null || visible.equals(weblog.getVisible()))) {
            return weblog;
        } else {
            return null;
        }
    }

    @Override
    public List<Weblog> getWeblogs(Boolean visible, int offset, int length) {

        List<Object> params = new ArrayList<>();
        int size = 0;
        String queryString;
        StringBuilder whereClause = new StringBuilder();

        queryString = "SELECT w FROM Weblog w WHERE 1=1 ";

        if (visible != null) {
            whereClause.append(" AND ");
            params.add(size++, visible);
            whereClause.append(" w.visible = ?").append(size);
        }

        whereClause.append(" ORDER BY w.dateCreated DESC");

        TypedQuery<Weblog> query = strategy.getDynamicQuery(queryString + whereClause.toString(), Weblog.class);
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        return query.getResultList();
    }

    @Override
    public List<User> getWeblogUsers(Weblog weblog) {
        List<User> users = new ArrayList<>();
        List<UserWeblogRole> roles = userManager.getWeblogRoles(weblog);
        for (UserWeblogRole role : roles) {
            User user = role.getUser();
            if (UserStatus.ENABLED.equals(user.getStatus())) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public Map<Character, Integer> getWeblogHandleLetterMap() {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<Character, Integer> results = new TreeMap<>();
        TypedQuery<Long> query = strategy.getNamedQuery(
                "Weblog.getCountByHandleLike", Long.class);
        for (int i = 0; i < 26; i++) {
            char currentChar = lc.charAt(i);
            query.setParameter(1, currentChar + "%");
            List row = query.getResultList();
            Long count = (Long) row.get(0);
            results.put(currentChar, count.intValue());
        }
        return results;
    }

    @Override
    public List<Weblog> getWeblogsByLetter(char letter, int offset, int length) {
        TypedQuery<Weblog> query = strategy.getNamedQuery(
                "Weblog.getByLetterOrderByHandle", Weblog.class);

        char upperCase = Character.toUpperCase(letter);

        query.setParameter(1, upperCase + "%");
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    @Override
    public long getWeblogCount() {
        List<Long> results = strategy.getNamedQuery(
                "Weblog.getCountAllDistinct", Long.class).getResultList();
        return results.get(0);
    }

    @Override
    public List<Weblog> getHotWeblogs(int offset, int length) {

        TypedQuery<Weblog> query = strategy.getNamedQuery(
                "Weblog.getByWeblog&DailyHitsGreaterThenZeroOrderByDailyHitsDesc", Weblog.class);

        if (offset != 0) {
            query.setFirstResult(offset);
        }

        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    @Override
    public void promoteScheduledEntries() {
        log.debug("promoting scheduled entries...");

        try {
            Instant now = Instant.now();
            log.debug("looking up scheduled entries older than {}", now);

            // get all published entries older than current time
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setEndDate(now);
            wesc.setStatus(WeblogEntry.PubStatus.SCHEDULED);
            List<WeblogEntry> scheduledEntries = weblogEntryManager.getWeblogEntries(wesc);
            log.debug("promoting {} entries to PUBLISHED state", scheduledEntries.size());

            for (WeblogEntry entry : scheduledEntries) {
                entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                weblogEntryManager.saveWeblogEntry(entry);
            }

            if (scheduledEntries.size() > 0) {
                // update last weblog change so any site weblog knows it needs to update
                WebloggerProperties props = strategy.getWebloggerProperties();
                props.setLastWeblogChange(Instant.now());
                strategy.store(props);
            }

            // commit the changes
            strategy.flush();

            // take a second pass to trigger reindexing
            // this is because we need the updated entries flushed first
            for (WeblogEntry entry : scheduledEntries) {
                // trigger search index on entry
                indexManager.updateIndex(entry, false);
            }

        } catch (Exception e) {
            log.error("Unexpected exception running task", e);
        }
        log.debug("finished promoting entries");
    }

    @Override
    public void incrementHitCount(Weblog weblog) {
        if (weblog != null) {
            Long count = hitsTally.getOrDefault(weblog.getId(), 0L);
            hitsTally.put(weblog.getId(), count + 1);
        }
    }

    @Override
    public void resetAllHitCounts() {
        hitsTally.clear();
        Query q = strategy.getNamedUpdate("Weblog.updateDailyHitCountZero");
        q.executeUpdate();
        strategy.flush();
        log.info("daily hit counts reset");
    }

    @Override
    public void updateHitCounters() {
        if (hitsTally.size() > 0) {
            // Make a reference to the current queue
            Map<String, Long> hitsTallyCopy = hitsTally;

            // reset queue for next execution
            hitsTally = Collections.synchronizedMap(new HashMap<>());

            // iterate over the tallied hits and store them in the db
            long totalHitsProcessed = 0;
            Weblog weblog;
            for (Map.Entry<String, Long> entry : hitsTallyCopy.entrySet()) {
                weblog = getWeblog(entry.getKey());
                strategy.refresh(weblog);

                if (weblog != null) {
                    weblog.setHitsToday(weblog.getHitsToday() + entry.getValue().intValue());
                    strategy.store(weblog);
                    totalHitsProcessed += entry.getValue();
                }
            }

            // flush the results to the db
            strategy.flush();

            log.info("Updated blog hits, {} total extra hits from {} blogs", totalHitsProcessed, hitsTallyCopy.size());
        }
    }

    @Override
    public void moveWeblogCategoryContents(WeblogCategory srcCat, WeblogCategory destCat) {

        // get all entries in category and subcats
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(srcCat.getWeblog());
        wesc.setCategoryName(srcCat.getName());
        List<WeblogEntry> results = weblogEntryManager.getWeblogEntries(wesc);

        // Loop through entries in src cat, assign them to dest cat
        Weblog website = destCat.getWeblog();
        for (WeblogEntry entry : results) {
            entry.setCategory(destCat);
            entry.setWeblog(website);
            this.strategy.store(entry);
        }
    }

    @Override
    public List<WeblogCategory> getWeblogCategories(Weblog weblog) {
        if (weblog == null) {
            throw new IllegalArgumentException("weblog is null");
        }

        List<WeblogCategory> categories = weblogCategoryRepository.findByWeblogOrderByPosition(weblog);

        // obtain usage stats
        String queryString = "SELECT new org.tightblog.business.WeblogManagerImpl.CategoryStats(we.category, " +
                "min(we.pubTime), max(we.pubTime), count(we)) " +
                "FROM WeblogEntry we WHERE we.weblog.id = ?1 GROUP BY we.category";
        TypedQuery<CategoryStats> query = strategy.getDynamicQuery(queryString, CategoryStats.class);
        query.setParameter(1, weblog.getId());

        List<CategoryStats> stats = query.getResultList();

        for (CategoryStats stat : stats) {
            WeblogCategory category = categories.stream().filter(
                    r -> r.getId().equals(stat.category.getId())).findFirst().orElse(null);
            if (category != null) {
                category.setNumEntries(stat.numEntries);
                category.setFirstEntry(stat.firstEntry);
                category.setLastEntry(stat.lastEntry);
            }
        }
        return categories;
    }

    public static class CategoryStats {
        public CategoryStats(WeblogCategory category, Instant firstEntry, Instant lastEntry, Long numEntries) {
            this.category = category;
            this.numEntries = numEntries.intValue();
            this.firstEntry = firstEntry.atZone(category.getWeblog().getZoneId()).toLocalDate();
            this.lastEntry = lastEntry.atZone(category.getWeblog().getZoneId()).toLocalDate();
        }

        WeblogCategory category;
        private int numEntries;
        private LocalDate firstEntry;
        private LocalDate lastEntry;
    }

    @Override
    public List<WeblogEntryTagAggregate> getTags(Weblog weblog, String sortBy, String startsWith, int offset, int limit) {
        boolean sortByName = !"count".equals(sortBy);

        List<Object> params = new ArrayList<>();
        int size = 0;

        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT wtag.name, COUNT(wtag), MIN(we.pubTime), MAX(we.pubTime) " +
                "FROM WeblogEntryTag wtag, WeblogEntry we WHERE wtag.weblogEntry.id = we.id");

        if (weblog != null) {
            params.add(size++, weblog.getId());
            queryString.append(" AND wtag.weblog.id = ?").append(size);
        }

        if (startsWith != null && startsWith.length() > 0) {
            params.add(size++, startsWith + '%');
            queryString.append(" AND wtag.name LIKE ?").append(size);
        }

        if (sortByName) {
            sortBy = "wtag.name";
        } else {
            sortBy = "COUNT(wtag) DESC";
        }

        queryString.append(" GROUP BY wtag.name ORDER BY ").append(sortBy);

        TypedQuery<WeblogEntryTagAggregate> query =
                strategy.getDynamicQuery(queryString.toString(), WeblogEntryTagAggregate.class);

        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (limit != -1) {
            query.setMaxResults(limit);
        }
        List queryResults = query.getResultList();

        List<WeblogEntryTagAggregate> results = new ArrayList<>();
        if (queryResults != null) {
            for (Object obj : queryResults) {
                Object[] row = (Object[]) obj;
                WeblogEntryTagAggregate ce = new WeblogEntryTagAggregate();
                ce.setName((String) row[0]);
                // The JPA query retrieves SUM(w.total) always as long
                ce.setTotal(((Long) row[1]).intValue());
                if (weblog != null) {
                    ce.setFirstEntry(((Instant) row[2]).atZone(weblog.getZoneId()).toLocalDate());
                    ce.setLastEntry(((Instant) row[3]).atZone(weblog.getZoneId()).toLocalDate());
                }
                results.add(ce);
            }
        }

        if (sortByName) {
            results.sort(WeblogEntryTagAggregate.NAME_COMPARATOR);
        } else {
            results.sort(WeblogEntryTagAggregate.COUNT_COMPARATOR);
        }

        return results;
    }

    @Override
    public List<WeblogEntryTagAggregate> getPopularTags(Weblog weblog, int offset, int limit) {

        List<WeblogEntryTagAggregate> tagAggs = getTags(weblog, "name", null, offset, (limit >= 0) ? limit : 25);

        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;

        for (WeblogEntryTagAggregate tagAgg : tagAggs) {
            min = Math.min(min, tagAgg.getTotal());
            max = Math.max(max, tagAgg.getTotal());
        }

        min = Math.log(1 + min);
        max = Math.log(1 + max);

        double range = Math.max(.01, max - min) * 1.0001;
        for (WeblogEntryTagAggregate tagAgg : tagAggs) {
            tagAgg.setIntensity((int) (1 + Math.floor(5 * (Math.log(1 + tagAgg.getTotal()) - min) / range)));
        }

        return tagAggs;
    }

    @Override
    public void removeTag(Weblog weblog, String tagName) {
        weblogEntryTagRepository.deleteByWeblogAndName(weblog, tagName);
        weblog.invalidateCache();

        // clear JPA cache of weblog entries for a weblog, to ensure no old tag data
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(weblog);
        List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);
        for (WeblogEntry entry : entries) {
            strategy.evict(WeblogEntry.class, entry.getId());
        }
        strategy.flush();
    }

    @Override
    public Map<String, Integer> addTag(Weblog weblog, String currentTagName, String newTagName) {
        Map<String, Integer> resultsMap = new HashMap<>();
        int updatedEntries = 0;
        int unchangedEntries = 0;

        List<WeblogEntryTag> currentResults = weblogEntryTagRepository.findByWeblogAndName(weblog, currentTagName);
        List<String> alreadyEntryIdList = weblogEntryTagRepository.getEntryIdByWeblogAndName(weblog, newTagName);

        for (WeblogEntryTag currentTag : currentResults) {
            if (alreadyEntryIdList.contains(currentTag.getWeblogEntry().getId())) {
                unchangedEntries++;
            } else {
                WeblogEntryTag newTag = new WeblogEntryTag(weblog, currentTag.getWeblogEntry(), newTagName);
                strategy.store(newTag);
                // clear JPA cache, to ensure no old tag data
                strategy.evict(WeblogEntry.class, currentTag.getWeblogEntry().getId());
                updatedEntries++;
            }
        }

        if (updatedEntries > 0) {
            weblog.invalidateCache();
        }

        strategy.flush();
        resultsMap.put("updated", updatedEntries);
        resultsMap.put("unchanged", unchangedEntries);
        return resultsMap;
    }
}
