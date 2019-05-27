 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogBookmark;
import org.tightblog.domain.WeblogCategory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryTag;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WebloggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.repository.UserWeblogRoleRepository;
import org.tightblog.repository.WeblogCategoryRepository;
import org.tightblog.repository.WeblogEntryCommentRepository;
import org.tightblog.repository.WeblogEntryRepository;
import org.tightblog.repository.WeblogEntryTagRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WeblogTemplateRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

 /**
  * Weblog, category and tag management
  */
@Component
@EnableConfigurationProperties(DynamicProperties.class)
public class WeblogManager {

    private static Logger log = LoggerFactory.getLogger(WeblogManager.class);

    private WeblogEntryRepository weblogEntryRepository;
    private WeblogEntryCommentRepository weblogEntryCommentRepository;
    private WeblogCategoryRepository weblogCategoryRepository;
    private WeblogEntryTagRepository weblogEntryTagRepository;
    private WeblogTemplateRepository weblogTemplateRepository;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;
    private UserWeblogRoleRepository userWeblogRoleRepository;
    private WeblogRepository weblogRepository;
    private UserManager userManager;
    private DynamicProperties dp;

    @Autowired
    private MediaManager mediaManager;

    @Autowired
    private LuceneIndexer luceneIndexer;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("#{'${newblog.blogroll}'.split(',')}")
    private Set<String> newBlogBlogroll;

    @Value("#{'${newblog.categories}'.split(',')}")
    private Set<String> newBlogCategories;

    // Map of each weblog and its extra hit count that has had additional accesses since the
    // last scheduled updateHitCounters() call.
    private Map<String, Long> hitsTally = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    public WeblogManager(WeblogEntryRepository weblogEntryRepository,
                         WeblogCategoryRepository weblogCategoryRepository,
                         WeblogEntryTagRepository weblogEntryTagRepository,
                         WeblogEntryCommentRepository weblogEntryCommentRepository,
                         WeblogTemplateRepository weblogTemplateRepository,
                         UserWeblogRoleRepository userWeblogRoleRepository,
                         WebloggerPropertiesRepository webloggerPropertiesRepository,
                         WeblogRepository weblogRepository,
                         UserManager userManager,
                         DynamicProperties dp) {
        this.weblogEntryRepository = weblogEntryRepository;
        this.weblogEntryCommentRepository = weblogEntryCommentRepository;
        this.weblogCategoryRepository = weblogCategoryRepository;
        this.weblogEntryTagRepository = weblogEntryTagRepository;
        this.weblogTemplateRepository = weblogTemplateRepository;
        this.userWeblogRoleRepository = userWeblogRoleRepository;
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.weblogRepository = weblogRepository;
        this.userManager = userManager;
        this.dp = dp;
    }

    public void saveWeblog(Weblog weblog, boolean externallyViewableChange) {
        if (externallyViewableChange) {
            weblog.setLastModified(Instant.now());
        }
        weblogRepository.saveAndFlush(weblog);
        if (externallyViewableChange) {
            dp.updateLastSitewideChange();
            weblogRepository.evictWeblog(weblog.getHandle());
        }
    }

    public void removeWeblog(Weblog weblog) {
        // remove contents first, then remove weblog
        weblogTemplateRepository.deleteByWeblog(weblog);
        weblogTemplateRepository.evictWeblogTemplates(weblog);
        mediaManager.removeAllFiles(weblog);

        List<WeblogEntry> entryList = weblogEntryRepository.findByWeblog(weblog);
        entryList.forEach(e -> weblogEntryCommentRepository.deleteByWeblogEntry(e));
        weblogEntryRepository.deleteByWeblog(weblog);
        userWeblogRoleRepository.deleteByWeblog(weblog);

        // remove indexing
        luceneIndexer.updateIndex(weblog, true);

        // check if main blog, disconnect if it is
        WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
        Weblog test = props.getMainBlog();
        if (test != null && test.getId().equals(weblog.getId())) {
            props.setMainBlog(null);
            webloggerPropertiesRepository.saveAndFlush(props);
        }
        weblogRepository.delete(weblog);
        dp.updateLastSitewideChange();
        weblogRepository.evictWeblog(weblog.getHandle());
    }

    /**
     * Add new weblog, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new weblog.
     *
     * @param newWeblog New weblog to be created, must have creator field populated.
     */
    public void addWeblog(Weblog newWeblog) {
        weblogRepository.save(newWeblog);

        if (weblogRepository.count() == 1) {
            // first weblog, let's make it the frontpage one.
            WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
            props.setMainBlog(newWeblog);
            webloggerPropertiesRepository.save(props);
        }

        // grant weblog creator OWNER permission
        userManager.grantWeblogRole(newWeblog.getCreator(), newWeblog, WeblogRole.OWNER);

        // add default categories and bookmarks
        if (!ObjectUtils.isEmpty(newBlogCategories)) {
            for (String category : newBlogCategories) {
                WeblogCategory c = new WeblogCategory(newWeblog, category);
                newWeblog.addCategory(c);
            }
        }

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
                }
            }
        }
        // create initial media file directory named "default"
        mediaManager.createMediaDirectory(newWeblog, "default");
        saveWeblog(newWeblog, true);
    }

    /**
     * Get the analytics tracking code to be used for the provided Weblog
     * @param weblog weblog to determine tracking code for
     * @return analytics tracking code, empty string if none.
     */
    public String getAnalyticsTrackingCode(Weblog weblog) {
        WebloggerProperties props = webloggerPropertiesRepository.findOrNull();

        if (props.isUsersOverrideAnalyticsCode() &&
                !StringUtils.isBlank(weblog.getAnalyticsCode())) {
            return weblog.getAnalyticsCode();
        } else {
            return StringUtils.defaultIfEmpty(props.getDefaultAnalyticsCode(), "");
        }
    }

    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map<Character, Integer> getWeblogHandleLetterMap() {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<Character, Integer> results = new TreeMap<>();
        for (int i = 0; i < 26; i++) {
            char currentChar = lc.charAt(i);
            int count = weblogRepository.getCountByHandle(currentChar);
            results.put(currentChar, count);
        }
        return results;
    }

    /**
     * Increment the hit (external view) count by one for a weblog.  This
     * information is not written to the database immediately but stored
     * in a queue.
     *
     * @param weblog The weblog object to increment the count for.
     */
    public void incrementHitCount(Weblog weblog) {
        if (weblog != null) {
            Long count = hitsTally.getOrDefault(weblog.getId(), 0L);
            hitsTally.put(weblog.getId(), count + 1);
        }
    }

    /**
     * Job to write out the hit count queue to the database, updating
     * individual blog's hit counters
     */
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
                weblog = weblogRepository.findById(entry.getKey()).orElse(null);
                if (weblog != null) {
                    weblog.setHitsToday(weblog.getHitsToday() + entry.getValue().intValue());
                    saveWeblog(weblog, true);
                    totalHitsProcessed += entry.getValue();
                    log.info("Updated blog hits, {} total extra hits from {} blogs", totalHitsProcessed, hitsTallyCopy.size());
                }
            }
        }
    }

    /**
     * Get WeblogCategory objects for a weblog.
     * @param weblog weblog whose categories are desired
     */
    public List<WeblogCategory> getWeblogCategories(Weblog weblog) {
        if (weblog == null) {
            throw new IllegalArgumentException("weblog is null");
        }

        List<WeblogCategory> categories = weblogCategoryRepository.findByWeblogOrderByPosition(weblog);

        // obtain usage stats
        String queryString = "SELECT new org.tightblog.service.WeblogManager.CategoryStats(we.category, " +
                "min(we.pubTime), max(we.pubTime), count(we)) " +
                "FROM WeblogEntry we WHERE we.weblog.id = ?1 GROUP BY we.category";
        TypedQuery<CategoryStats> query = entityManager.createQuery(queryString, CategoryStats.class);
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

    /**
     * Get list of WeblogEntryTagAggregate objects for the tags comprising a weblog.
     *
     * @param weblog    Weblog or null to get for all weblogs.
     * @param sortBy     Sort by either 'name' or 'count' (null for name)
     * @param startsWith Prefix for tags to be returned (null or a string of length > 0)
     * @param offset     0-based index into returns
     * @param limit      Max objects to return (or -1 for no limit)
     * @return List of tags matching the criteria.
     */
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
                entityManager.createQuery(queryString.toString(), WeblogEntryTagAggregate.class);

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

    /**
     * Get list of WeblogEntryTagAggregate objects identifying the most used tags for a weblog.
     * There are no offset/length params just a limit.
     *
     * @param weblog Weblog or null to get for all weblogs.
     * @param offset 0-based index into results
     * @param limit  Max objects to return (or -1 for no limit)
     * @return List of most popular tags.
     */
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

    /**
     * Remove all tags with a given name from a weblog's entries
     *
     * @param weblog The weblog to remove the tag from
     * @param tagName Tag name to remove.
     */
    public void removeTag(Weblog weblog, String tagName) {
        List<WeblogEntryTag> currentResults = weblogEntryTagRepository.findByWeblogAndName(weblog, tagName);

        boolean updated = false;
        for (WeblogEntryTag tag : currentResults) {
            tag.getWeblogEntry().getTags().remove(tag);
            weblogEntryRepository.save(tag.getWeblogEntry());
            updated = true;
        }

        if (updated) {
            saveWeblog(weblog, true);
        }
    }

    /**
     * Add a tag to all entries having a current tag.
     *
     * @param weblog The weblog whose entries tag will be added to
     * @param currentTagName The entries, having this tag, that will receive the new tag.
     * @param newTagName New tag to add to entries having currentTag, if they don't have this tag already.
     * @return Map with keys of "updated" and "unchanged" indicating number of entries updated, where
     *         unchanged refers to entries having currentTag but already having newTag.
     */
    public Map<String, Integer> addTag(Weblog weblog, String currentTagName, String newTagName) {
        Map<String, Integer> resultsMap = new HashMap<>();
        int updatedEntries = 0;
        int unchangedEntries = 0;

        List<WeblogEntryTag> currentResults = weblogEntryTagRepository.findByWeblogAndName(weblog, currentTagName);
        List<String> alreadyEntryIdList = weblogEntryTagRepository.getEntryIdsByWeblogAndName(weblog, newTagName);

        for (WeblogEntryTag currentTag : currentResults) {
            if (alreadyEntryIdList.contains(currentTag.getWeblogEntry().getId())) {
                unchangedEntries++;
            } else {
                WeblogEntryTag newTag = new WeblogEntryTag(weblog, currentTag.getWeblogEntry(), newTagName);
                currentTag.getWeblogEntry().getTags().add(newTag);
                weblogEntryRepository.save(currentTag.getWeblogEntry());
                updatedEntries++;
            }
        }

        if (updatedEntries > 0) {
            saveWeblog(weblog, true);
        }

        resultsMap.put("updated", updatedEntries);
        resultsMap.put("unchanged", unchangedEntries);
        return resultsMap;
    }

    public void evictWeblogTemplateCaches(Weblog weblog, String templateName, Template.Role role) {
        weblogTemplateRepository.evictWeblogTemplates(weblog);
        weblogTemplateRepository.evictWeblogTemplateByName(weblog, templateName);
        weblogTemplateRepository.evictWeblogTemplateByRole(weblog, role);
    }
}
