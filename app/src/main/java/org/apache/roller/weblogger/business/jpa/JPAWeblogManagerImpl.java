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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.HitCountQueue;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.util.cache.CacheManager;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JPAWeblogManagerImpl implements WeblogManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(JPAWeblogManagerImpl.class);
    
    private final UserManager userManager;
    private final WeblogEntryManager weblogEntryManager;
    private final MediaFileManager mediaFileManager;
    private final IndexManager indexManager;
    private final PingTargetManager pingTargetManager;
    private final JPAPersistenceStrategy strategy;
    private final CacheManager cacheManager;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map<String,String> weblogHandleToIdMap = new Hashtable<>();

    protected JPAWeblogManagerImpl(UserManager um, WeblogEntryManager wem, MediaFileManager mfm,
                                   IndexManager im, PingTargetManager ptm, JPAPersistenceStrategy strat,
                                   CacheManager cacheManager) {
        log.debug("Instantiating JPA Weblog Manager");
        this.userManager = um;
        this.weblogEntryManager = wem;
        this.mediaFileManager = mfm;
        this.indexManager = im;
        this.pingTargetManager = ptm;
        this.strategy = strat;
        this.cacheManager = cacheManager;
    }

    public void saveWeblog(Weblog weblog) throws WebloggerException {
        weblog.setLastModified(new java.util.Date());
        strategy.merge(weblog);
    }
    
    public void removeWeblog(Weblog weblog) throws WebloggerException {
        // remove contents first, then remove weblog
        this.removeWeblogContents(weblog);
        this.strategy.remove(weblog);
        
        // remove entry from cache mapping
        this.weblogHandleToIdMap.remove(weblog.getHandle());
    }
    
    /**
     * convenience method for removing contents of a weblog.
     */
    private void removeWeblogContents(Weblog weblog)
    throws  WebloggerException {
        
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
        
        // Remove the weblog's auto ping configurations
        List<AutoPing> autopings = pingTargetManager.getAutoPingsByWeblog(weblog);
        this.strategy.removeAll(autopings);

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
        Query removeCategories= strategy.getNamedUpdate("WeblogCategory.removeByWeblog");
        removeCategories.setParameter(1, weblog);
        removeCategories.executeUpdate();

        // remove permissions
        for (UserWeblogRole role : userManager.getWeblogRolesIncludingPending(weblog)) {
            userManager.revokeWeblogRole(role.getUser(), role.getWeblog());
        }

        // remove indexing
        indexManager.removeWeblogIndexOperation(weblog);

        // flush the changes before returning. This is required as there is a
        // circular dependency between WeblogCategory and Weblog
        this.strategy.flush();        
    }

    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#saveTemplate(WeblogTemplate)
     */
    public void saveTemplate(WeblogTemplate template) throws WebloggerException {
        this.strategy.store(template);
        
        // update weblog last modified date.  date updated by saveWeblog()
        saveWeblog(template.getWeblog());
    }

    public void saveTemplateRendition(WeblogTemplateRendition rendition) throws WebloggerException {
        this.strategy.store(rendition);

        // update weblog last modified date.  date updated by saveWeblog()
        saveWeblog(rendition.getWeblogTemplate().getWeblog());
    }
    
    public void removeTemplate(WeblogTemplate template) throws WebloggerException {
        this.strategy.remove(template);
        // update weblog last modified date.  date updated by saveWeblog()
        saveWeblog(template.getWeblog());
    }
    
    public void addWeblog(Weblog newWeblog) throws WebloggerException {
        this.strategy.store(newWeblog);
        this.strategy.flush();
        this.addWeblogContents(newWeblog);
    }
    
    private void addWeblogContents(Weblog newWeblog)
    throws WebloggerException {
        
        // grant weblog creator OWNER permission
        userManager.grantWeblogRole(
                newWeblog.getCreator(), newWeblog, WeblogRole.OWNER);
        
        String cats = WebloggerConfig.getProperty("newuser.categories");
        WeblogCategory firstCat = null;
        if (cats != null) {
            String[] splitcats = cats.split(",");
            for (String split : splitcats) {
                if (split.trim().length() == 0) {
                    continue;
                }
                WeblogCategory c = new WeblogCategory(
                        newWeblog,
                        split);
                if (firstCat == null) {
                    firstCat = c;
                }
                newWeblog.addCategory(c);
                this.strategy.store(c);
            }
        }

        this.strategy.store(newWeblog);

        // add default bookmarks
        String blogroll = WebloggerConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (String splitItem : splitroll) {
                String[] rollitems = splitItem.split("\\|");
                if (rollitems.length > 1) {
                    WeblogBookmark b = new WeblogBookmark(
                            newWeblog,
                            rollitems[0],
                            "",
                            rollitems[1].trim());
                    newWeblog.addBookmark(b);
                    this.strategy.store(b);
                }
            }
        }

        mediaFileManager.createDefaultMediaDirectory(newWeblog);

        // flush so that all data up to this point can be available in db
        this.strategy.flush();

        for (PingTarget pingTarget : pingTargetManager.getCommonPingTargets()) {
            if(pingTarget.isAutoEnabled()) {
                AutoPing autoPing = new AutoPing(pingTarget, newWeblog);
                pingTargetManager.saveAutoPing(autoPing);
            }
        }

    }
    
    public Weblog getWeblog(String id) throws WebloggerException {
        return this.strategy.load(Weblog.class, id);
    }
    
    public Weblog getWeblogByHandle(String handle) throws WebloggerException {
        return getWeblogByHandle(handle, Boolean.TRUE);
    }
    
    /**
     * Return weblog specified by handle.
     */
    public Weblog getWeblogByHandle(String handle, Boolean visible)
    throws WebloggerException {
        
        if (handle==null) {
            throw new WebloggerException("Handle cannot be null");
        }
        
        // check cache first
        // NOTE: if we ever allow changing handles then this needs updating
        if(this.weblogHandleToIdMap.containsKey(handle)) {
            
            Weblog weblog = this.getWeblog(this.weblogHandleToIdMap.get(handle));
            if (weblog != null) {
                // only return weblog if enabled status matches
                if(visible == null || visible.equals(weblog.getVisible())) {
                    log.debug("weblogHandleToId CACHE HIT - "+handle);
                    return weblog;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.weblogHandleToIdMap.remove(handle);
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
        if(weblog != null) {
            log.debug("weblogHandleToId CACHE MISS - "+handle);
            this.weblogHandleToIdMap.put(weblog.getHandle(), weblog.getId());
        }
        
        if(weblog != null &&
                (visible == null || visible.equals(weblog.getVisible()))) {
            return weblog;
        } else {
            return null;
        }
    }
    
    /**
     * Get weblogs of a user
     */
    public List<Weblog> getWeblogs(
            Boolean enabled, Boolean active,
            Date startDate, Date endDate, int offset, int length) throws WebloggerException {
        
        //if (endDate == null) endDate = new Date();
                      
        List<Object> params = new ArrayList<>();
        int size = 0;
        String queryString;
        StringBuilder whereClause = new StringBuilder();
        
        queryString = "SELECT w FROM Weblog w WHERE ";

        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, start);
            whereClause.append(" w.dateCreated > ?").append(size);
        }
        if (endDate != null) {
            Timestamp end = new Timestamp(endDate.getTime());
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, end);
            whereClause.append(" w.dateCreated < ?").append(size);
        }
        if (enabled != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, enabled);
            whereClause.append(" w.visible = ?").append(size);
        }
        if (active != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, active);
            whereClause.append(" w.active = ?").append(size);
        }      
                
        whereClause.append(" ORDER BY w.dateCreated DESC");
        
        TypedQuery<Weblog> query = strategy.getDynamicQuery(queryString + whereClause.toString(), Weblog.class);
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

    public List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException {
        List<Weblog> weblogs = new ArrayList<>();
        if (user == null) {
            return weblogs;
        }
        List<UserWeblogRole> roles = userManager.getWeblogRoles(user);
        for (UserWeblogRole role : roles) {
            Weblog weblog = role.getWeblog();
            if ((!enabledOnly || weblog.getVisible()) && BooleanUtils.isTrue(weblog.isActive())) {
                weblogs.add(weblog);
            }
        }
        return weblogs;
    }
    
    public List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException {
        List<User> users = new ArrayList<>();
        List<UserWeblogRole> roles = userManager.getWeblogRoles(weblog);
        for (UserWeblogRole role : roles) {
            User user = role.getUser();
            if (user == null) {
                log.error("ERROR user is null, userId:" + role.getUser().getId());
                continue;
            }
            if (!enabledOnly || user.getEnabled()) {
                users.add(user);
            }
        }
        return users;
    }

    public WeblogTemplate getTemplate(String id) throws WebloggerException {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) {
            return null;
        }
        
        return this.strategy.load(WeblogTemplate.class,id);
    }
    
    /**
     * Use JPA directly because Weblogger's Query API does too much allocation.
     */
    public WeblogTemplate getTemplateByLink(Weblog weblog, String templateLink)
    throws WebloggerException {
        
        if (weblog == null) {
            throw new WebloggerException("userName is null");
        }

        if (templateLink == null) {
            throw new WebloggerException("templateLink is null");
        }

        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery("WeblogTemplate.getByWeblog&Link",
                WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, templateLink);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#getTemplateByAction(Weblog, ComponentType)
     */
    public WeblogTemplate getTemplateByAction(Weblog weblog, ComponentType action)
            throws WebloggerException {
        
        if (weblog == null) {
            throw new WebloggerException("weblog is null");
        }

        if (action == null) {
            throw new WebloggerException("Action name is null");
        }
        
        TypedQuery<WeblogTemplate> query = strategy.getNamedQuery("WeblogTemplate.getByAction",
                WeblogTemplate.class);
        query.setParameter(1, weblog);
        query.setParameter(2, action);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }        
    }
    
    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#getTemplateByName(Weblog, java.lang.String)
     */
    public WeblogTemplate getTemplateByName(Weblog weblog, String templateName)
    throws WebloggerException {
        
        if (weblog == null) {
            throw new WebloggerException("weblog is null");
        }
        
        if (templateName == null) {
            throw new WebloggerException("Template name is null");
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

    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#getTemplates(Weblog)
     */
    public List<WeblogTemplate> getTemplates(Weblog weblog) throws WebloggerException {
        if (weblog == null) {
            throw new WebloggerException("weblog is null");
        }
        TypedQuery<WeblogTemplate> q = strategy.getNamedQuery(
                "WeblogTemplate.getByWeblogOrderByName", WeblogTemplate.class);
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    
    public Map<String, Long> getWeblogHandleLetterMap() throws WebloggerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<String, Long> results = new TreeMap<>();
        TypedQuery<Long> query = strategy.getNamedQuery(
                "Weblog.getCountByHandleLike", Long.class);
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            query.setParameter(1, currentChar + "%");
            List row = query.getResultList();
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }
    
    public List<Weblog> getWeblogsByLetter(char letter, int offset, int length)
    throws WebloggerException {
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
    
    public List<StatCount> getMostCommentedWeblogs(Date startDate, Date endDate,
            int offset, int length)
            throws WebloggerException {
        
        TypedQuery<Object[]> query;
        
        if (endDate == null) {
            endDate = new Date();
        }
        
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWeblogByEndDate&StartDate", Object[].class);
            query.setParameter(1, end);
            query.setParameter(2, start);
        } else {
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWeblogByEndDate", Object[].class);
            query.setParameter(1, end);
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        List<Object[]> queryResults = query.getResultList();
        List<StatCount> results = new ArrayList<>();
        for (Object[] row : queryResults) {
            StatCount sc = new StatCount(
                    (String)row[1],                     // weblog id
                    (String)row[2],                     // weblog handle
                    (String)row[3],                     // weblog name
                    "statCount.weblogCommentCountType", // stat type
                    ((Long)row[0]));        // # comments
            sc.setWeblogHandle((String)row[2]);
            results.add(sc);
        }

        // Original query ordered by desc # comments.
        // JPA QL doesn't allow queries to be ordered by aggregates; do it in memory
        Collections.sort(results, StatCount.CountComparator);

        return results;
    }
    
    /**
     * Get count of weblogs, active and inactive
     */
    public long getWeblogCount() throws WebloggerException {
        List<Long> results = strategy.getNamedQuery(
                "Weblog.getCountAllDistinct", Long.class).getResultList();
        return results.get(0);
    }

    /**
     * @inheritDoc
     */
    public int getHitCount(Weblog weblog)
            throws WebloggerException {
        Weblog copy = getWeblog(weblog.getId());
        return copy.getHitsToday();
    }

    /**
     * @inheritDoc
     */
    public List<Weblog> getHotWeblogs(int sinceDays, int offset, int length)
            throws WebloggerException {

        // figure out start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();

        TypedQuery<Weblog> query = strategy.getNamedQuery(
                "Weblog.getByWeblogEnabledTrueAndActiveTrue&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc",
                Weblog.class);
        query.setParameter(1, startDate);
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
    public void resetAllHitCounts() throws WebloggerException {
        log.info("daily hit counts getting reset...");
        Query q = strategy.getNamedUpdate("Weblog.updateDailyHitCountZero");
        q.executeUpdate();
        strategy.flush();
        log.info("finished resetting hit count");
    }

    /**
     * @inheritDoc
     */
    public void resetHitCount(Weblog weblog) throws WebloggerException {
        weblog.setHitsToday(0);
        strategy.store(weblog);
    }

    /**
     * @inheritDoc
     */
    public void incrementHitCount(Weblog weblog, int amount)
            throws WebloggerException {
        weblog.setHitsToday(getHitCount(weblog) + amount);
        strategy.store(weblog);
    }

    public void saveBookmark(WeblogBookmark bookmark) throws WebloggerException {
        bookmark.getWeblog().invalidateCache();
        this.strategy.store(bookmark);
    }

    public WeblogBookmark getBookmark(String id) throws WebloggerException {
        return strategy.load(WeblogBookmark.class, id);
    }

    public void removeBookmark(WeblogBookmark bookmark) throws WebloggerException {
        bookmark.getWeblog().getBookmarks().remove(bookmark);
        bookmark.getWeblog().invalidateCache();
        this.strategy.remove(bookmark);
    }

    @Override
    public void promoteScheduledEntries() throws WebloggerException {
        log.debug("promoting scheduled entries...");

        try {
            Date now = new Date();

            log.debug("looking up scheduled entries older than " + now);

            // get all published entries older than current time
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setEndDate(now);
            wesc.setStatus(WeblogEntry.PubStatus.SCHEDULED);
            List<WeblogEntry> scheduledEntries = weblogEntryManager.getWeblogEntries(wesc);
            log.info("promoting "+scheduledEntries.size() + " entries to PUBLISHED state");

            for (WeblogEntry entry : scheduledEntries) {
                entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                entry.setRefreshAggregates(true);
                weblogEntryManager.saveWeblogEntry(entry);
            }

            // commit the changes
            strategy.flush();

            // take a second pass to trigger reindexing and cache invalidations
            // this is because we need the updated entries flushed first
            for (WeblogEntry entry : scheduledEntries) {
                // trigger a cache invalidation
                cacheManager.invalidate(entry);
                // trigger search index on entry
                indexManager.addEntryReIndexOperation(entry);
            }

        } catch (WebloggerException e) {
            log.error("Error getting scheduled entries", e);
        } catch(Exception e) {
            log.error("Unexpected exception running task", e);
        }
        log.debug("finished promoting entries");
    }

    @Override
    public void updateHitCounters() throws WebloggerException {
        log.debug("updating blog hit counters...");

        HitCountQueue hitCounter = HitCountQueue.getInstance();

        // first get the current set of hits
        List<String> currentHits = hitCounter.getHits();

        // now reset the queued hits
        hitCounter.resetHits();

        // tally the counts, grouped by weblog handle
        Map<String, Long> hitsTally = new HashMap<>();
        long totalHitsProcessed = 0;
        for (String weblogHandle : currentHits) {
            Long count = hitsTally.get(weblogHandle);
            if(count == null) {
                count = 1L;
            } else {
                count = count + 1;
            }
            totalHitsProcessed += 1;
            hitsTally.put(weblogHandle, count);
        }

        // iterate over the tallied hits and store them in the db
        try {
            Weblog weblog;
            for (Map.Entry<String, Long> entry : hitsTally.entrySet()) {
                try {
                    weblog = getWeblogByHandle(entry.getKey());
                    incrementHitCount(weblog, entry.getValue().intValue());
                } catch (WebloggerException ex) {
                    log.error(ex);
                }
            }

            // flush the results to the db
            strategy.flush();

            log.debug("Added " + totalHitsProcessed + " hits to " + hitsTally.size() + " blogs");

        } catch (WebloggerException ex) {
            log.error("Error persisting updated hit counts", ex);
        }

    }

    /**
     * @inheritDoc
     */
    public void saveWeblogCategory(WeblogCategory cat) throws WebloggerException {
        boolean exists = getWeblogCategory(cat.getId()) != null;
        if (!exists && isDuplicateWeblogCategoryName(cat)) {
            throw new WebloggerException("Duplicate category name, cannot save category");
        }
        cat.getWeblog().invalidateCache();
        this.strategy.store(cat);
    }

    /**
     * @inheritDoc
     */
    public void removeWeblogCategory(WeblogCategory cat)
            throws WebloggerException {

        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(cat.getWeblog());
        wesc.setCatName(cat.getName());

        if (weblogEntryManager.getWeblogEntries(wesc).size() > 0) {
            throw new WebloggerException("Cannot remove category with entries");
        }

        cat.getWeblog().getWeblogCategories().remove(cat);
        cat.getWeblog().invalidateCache();
        this.strategy.remove(cat);
    }

    /**
     * @inheritDoc
     */
    public void moveWeblogCategoryContents(WeblogCategory srcCat,
                                           WeblogCategory destCat)
            throws WebloggerException {

        // get all entries in category and subcats
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(srcCat.getWeblog());
        wesc.setCatName(srcCat.getName());
        List<WeblogEntry> results = weblogEntryManager.getWeblogEntries(wesc);

        // Loop through entries in src cat, assign them to dest cat
        Weblog website = destCat.getWeblog();
        for (WeblogEntry entry : results) {
            entry.setCategory(destCat);
            entry.setWeblog(website);
            this.strategy.store(entry);
        }
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

    public boolean isDuplicateWeblogCategoryName(WeblogCategory cat)
            throws WebloggerException {
        return (getWeblogCategoryByName(
                cat.getWeblog(), cat.getName()) != null);
    }

    /**
     * @inheritDoc
     */
    public boolean isWeblogCategoryInUse(WeblogCategory cat) {
        TypedQuery<WeblogEntry> q = strategy.getNamedQuery("WeblogEntry.getByCategory", WeblogEntry.class);
        q.setParameter(1, cat);
        int entryCount = q.getResultList().size();
        return entryCount > 0;
    }
}
