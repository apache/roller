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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.Template.ComponentType;
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
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.WebloggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component("weblogManager")
public class WeblogManagerImpl implements WeblogManager {

    private static Logger log = LoggerFactory.getLogger(WeblogManagerImpl.class);

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

    // Map of each weblog and its extra hit count that has had additional accesses since the
    // last scheduled updateHitCounters() call.
    private Map<String, Long> hitsTally = Collections.synchronizedMap(new HashMap<>());

    // cached mapping of weblogHandles -> weblogIds
    private Map<String, String> weblogHandleToIdMap = new Hashtable<>();

    protected WeblogManagerImpl() {
    }

    @Override
    public void saveWeblog(Weblog weblog) {
        weblog.setLastModified(Instant.now());
        strategy.merge(weblog);

        // update last weblog change so any site weblog knows it needs to update
        WebloggerProperties props = strategy.getWebloggerProperties();
        props.setLastWeblogChange(Instant.now());
        strategy.store(props);
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

        // remove tags
        TypedQuery<WeblogEntryTag> tagQuery = strategy.getNamedQuery("WeblogEntryTag.getByWeblog",
                WeblogEntryTag.class);
        tagQuery.setParameter(1, weblog);
        List<WeblogEntryTag> results = tagQuery.getResultList();

        for (WeblogEntryTag tagData : results) {
            if (tagData.getWeblogEntry() != null) {
                tagData.getWeblogEntry().getTags().remove(tagData);
            }
            this.strategy.remove(tagData);
        }

        // remove associated templates
        TypedQuery<WeblogTemplate> templateQuery = strategy.getNamedQuery("WeblogTemplate.getByWeblog",
                WeblogTemplate.class);
        templateQuery.setParameter(1, weblog);
        List<WeblogTemplate> templates = templateQuery.getResultList();

        for (WeblogTemplate template : templates) {
            this.strategy.remove(template);
        }

        // remove bookmarks
        TypedQuery<WeblogBookmark> bookmarkQuery = strategy.getNamedQuery("Bookmark.getByWeblog",
                WeblogBookmark.class);
        bookmarkQuery.setParameter(1, weblog);
        List<WeblogBookmark> bookmarks = bookmarkQuery.getResultList();
        for (WeblogBookmark bookmark : bookmarks) {
            this.strategy.remove(bookmark);
        }

        // remove mediafile metadata
        // remove uploaded files
        mediaFileManager.removeAllFiles(weblog);
        this.strategy.flush();

        // remove entries
        TypedQuery<WeblogEntry> refQuery = strategy.getNamedQuery("WeblogEntry.getByWeblog", WeblogEntry.class);
        refQuery.setParameter(1, weblog);
        List<WeblogEntry> entries = refQuery.getResultList();
        for (WeblogEntry entry : entries) {
            weblogEntryManager.removeWeblogEntry(entry);
        }
        this.strategy.flush();

        // delete all weblog categories
        Query removeCategories = strategy.getNamedUpdate("WeblogCategory.removeByWeblog");
        removeCategories.setParameter(1, weblog);
        removeCategories.executeUpdate();

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

        // flush the changes before returning. This is required as there is a
        // circular dependency between WeblogCategory and Weblog
        this.strategy.flush();
    }

    @Override
    public void saveTemplate(WeblogTemplate template) {
        this.strategy.store(template);

        // update weblog last modified date.  date updated by saveWeblog()
        saveWeblog(template.getWeblog());
    }

    @Override
    public void removeTemplate(WeblogTemplate template) {
        this.strategy.remove(template);
        saveWeblog(template.getWeblog());
    }

    @Override
    public void addWeblog(Weblog newWeblog) {
        this.strategy.store(newWeblog);
        this.strategy.flush();
        this.addWeblogContents(newWeblog);
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

        String cats = WebloggerStaticConfig.getProperty("newuser.categories");
        WeblogCategory firstCat = null;
        if (cats != null) {
            String[] splitcats = cats.split(",");
            for (String split : splitcats) {
                if (split.trim().length() == 0) {
                    continue;
                }
                WeblogCategory c = new WeblogCategory(newWeblog, split);
                if (firstCat == null) {
                    firstCat = c;
                }
                newWeblog.addCategory(c);
                this.strategy.store(c);
            }
        }

        this.strategy.store(newWeblog);

        // add default bookmarks
        String blogroll = WebloggerStaticConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (String splitItem : splitroll) {
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
            log.debug("weblogHandleToId CACHE MISS - {}", handle);
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
            if (user == null) {
                log.error("ERROR user is null, userId: {}", role.getUser().getId());
                continue;
            }
            if (UserStatus.ENABLED.equals(user.getStatus())) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public WeblogTemplate getTemplate(String id) {
        if (id == null) {
            return null;
        }
        return this.strategy.load(WeblogTemplate.class, id);
    }

    /**
     * Using JPA directly because Weblogger's Query API does too much memory allocation.
     */
    @Override
    public WeblogTemplate getTemplateByPath(Weblog weblog, String path) {

        if (weblog == null) {
            throw new IllegalArgumentException("userName is null");
        }

        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }

        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery("WeblogTemplate.getByWeblog&RelativePath",
                WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, path);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public WeblogTemplate getTemplateByAction(Weblog weblog, ComponentType action) {

        if (weblog == null) {
            throw new IllegalArgumentException("weblog is null");
        }

        if (action == null) {
            throw new IllegalArgumentException("Action name is null");
        }

        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery("WeblogTemplate.getByRole",
                WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, action);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public WeblogTemplate getTemplateByName(Weblog weblog, String templateName) {

        if (weblog == null) {
            throw new IllegalArgumentException("weblog is null");
        }

        if (templateName == null) {
            throw new IllegalArgumentException("Template name is null");
        }

        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery("WeblogTemplate.getByWeblog&Name",
                WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, templateName);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<WeblogTemplate> getTemplates(Weblog weblog) {
        if (weblog == null) {
            throw new IllegalArgumentException("weblog is null");
        }
        TypedQuery<WeblogTemplate> q = strategy.getNamedQuery(
                "WeblogTemplate.getByWeblogOrderByName", WeblogTemplate.class);
        q.setParameter(1, weblog);
        return q.getResultList();
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
        query.setParameter(1, letter + "%");
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
    public List<Weblog> getHotWeblogs(int sinceDays, int offset, int length) {

        TypedQuery<Weblog> query = strategy.getNamedQuery(
                "Weblog.getByWeblog&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc", Weblog.class);
        query.setParameter(1, Instant.now().minus(sinceDays, ChronoUnit.DAYS));
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    @Override
    public void saveBookmark(WeblogBookmark bookmark) {
        bookmark.getWeblog().invalidateCache();
        this.strategy.store(bookmark);
    }

    @Override
    public WeblogBookmark getBookmark(String id) {
        return strategy.load(WeblogBookmark.class, id);
    }

    @Override
    public void removeBookmark(WeblogBookmark bookmark) {
        Weblog weblog = bookmark.getWeblog();
        weblog.getBookmarks().remove(bookmark);
        weblog.invalidateCache();
        this.strategy.remove(bookmark);
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
    public void saveWeblogCategory(WeblogCategory cat) {
        WeblogCategory test = getWeblogCategoryByName(cat.getWeblog(), cat.getName());

        if (test != null && !test.getId().equals(cat.getId())) {
            throw new IllegalArgumentException("Duplicate category name, cannot save category");
        }
        cat.getWeblog().invalidateCache();
        this.strategy.store(cat);
    }

    @Override
    public void removeWeblogCategory(WeblogCategory cat) {

        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(cat.getWeblog());
        wesc.setCategoryName(cat.getName());

        if (weblogEntryManager.getWeblogEntries(wesc).size() > 0) {
            throw new IllegalStateException("Cannot remove category with entries");
        }

        cat.getWeblog().getWeblogCategories().remove(cat);
        cat.getWeblog().invalidateCache();
        this.strategy.remove(cat);
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

        TypedQuery<WeblogCategory> q = strategy.getNamedQuery(
                "WeblogCategory.getByWeblog", WeblogCategory.class);
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    @Override
    public WeblogCategory getWeblogCategory(String id) {
        return this.strategy.load(WeblogCategory.class, id);
    }

    //--------------------------------------------- WeblogCategory Queries

    @Override
    public WeblogCategory getWeblogCategoryByName(Weblog weblog, String categoryName) {
        TypedQuery<WeblogCategory> q = strategy.getNamedQuery(
                "WeblogCategory.getByWeblog&Name", WeblogCategory.class);
        q.setParameter(1, weblog);
        q.setParameter(2, categoryName);
        q.setHint("javax.persistence.cache.storeMode", "REFRESH");
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
    public List<WeblogEntryTagAggregate> getTags(Weblog weblog, String sortBy, String startsWith, int offset, int limit) {
        boolean sortByName = !"count".equals(sortBy);

        List<Object> params = new ArrayList<>();
        int size = 0;

        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE 1 = 1");

        if (weblog != null) {
            params.add(size++, weblog.getId());
            queryString.append(" AND w.weblog.id = ?").append(size);
        }

        if (startsWith != null && startsWith.length() > 0) {
            params.add(size++, startsWith + '%');
            queryString.append(" AND w.name LIKE ?").append(size);
        }

        if (sortByName) {
            sortBy = "w.name";
        } else {
            sortBy = "SUM(w.total) DESC";
        }
        queryString.append(" GROUP BY w.name ORDER BY ").append(sortBy);

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
                results.add(ce);
            }
        }

        if (sortByName) {
            results.sort(WeblogEntryTagAggregate.nameComparator);
        } else {
            results.sort(WeblogEntryTagAggregate.countComparator);
        }

        return results;
    }

    @Override
    public boolean getTagExists(Weblog weblog, String tag) {
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
        for (int j = 0; j < params.size(); j++) {
            q.setParameter(j + 1, params.get(j));
        }
        List<String> results = q.getResultList();

        // OK if at least one article matches the tag
        return (results != null && results.size() > 0);
    }

    @Override
    public void removeTag(Weblog weblog, String tagName) {
        Query removeTag = strategy.getNamedUpdate("WeblogEntryTag.removeByWeblogAndTagName");
        removeTag.setParameter(1, weblog);
        removeTag.setParameter(2, tagName);
        removeTag.executeUpdate();
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

        TypedQuery<WeblogEntryTag> currentTagQuery = strategy.getNamedQuery("WeblogEntryTag.getByWeblogAndName",
                WeblogEntryTag.class);
        currentTagQuery.setParameter(1, weblog);
        currentTagQuery.setParameter(2, currentTagName);
        List<WeblogEntryTag> currentResults = currentTagQuery.getResultList();

        TypedQuery<String> alreadyHasNewTagQuery = strategy.getNamedQuery("WeblogEntryTag.getEntryIdByWeblogAndName",
                String.class);
        alreadyHasNewTagQuery.setParameter(1, weblog);
        alreadyHasNewTagQuery.setParameter(2, newTagName);
        List<String> alreadyEntryIdList = alreadyHasNewTagQuery.getResultList();

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
