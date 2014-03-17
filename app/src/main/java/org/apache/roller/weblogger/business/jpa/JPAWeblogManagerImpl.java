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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.config.WebloggerConfig;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.StatCountCountComparator;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogThemeAssoc;
import org.apache.roller.weblogger.pojos.WeblogThemeTemplateCode;


/*
 * JPAWeblogManagerImpl.java
 * Created on May 31, 2006, 4:08 PM
 */
@com.google.inject.Singleton
public class JPAWeblogManagerImpl implements WeblogManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(JPAWeblogManagerImpl.class);
    
    private static final Comparator STAT_COUNT_COUNT_REVERSE_COMPARATOR =
            Collections.reverseOrder(StatCountCountComparator.getInstance());
    
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable();

    @com.google.inject.Inject
    protected JPAWeblogManagerImpl(Weblogger roller, JPAPersistenceStrategy strat) {
        log.debug("Instantiating JPA Weblog Manager");
        this.roller = roller;
        this.strategy = strat;
    }
    
    
    public void release() {}
    
    
    /**
     * Update existing website.
     */
    public void saveWeblog(Weblog website) throws WebloggerException {
        
        website.setLastModified(new java.util.Date());
        strategy.store(website);
    }
    
    public void removeWeblog(Weblog weblog) throws WebloggerException {
        
        // remove contents first, then remove website
        this.removeWebsiteContents(weblog);
        this.strategy.remove(weblog);
        
        // remove entry from cache mapping
        this.weblogHandleToIdMap.remove(weblog.getHandle());
    }
    
    /**
     * convenience method for removing contents of a weblog.
     * TODO BACKEND: use manager methods instead of queries here
     */
    private void removeWebsiteContents(Weblog website)
    throws  WebloggerException {
        
        UserManager        umgr = roller.getUserManager();
        WeblogEntryManager emgr = roller.getWeblogEntryManager();

        //remove theme Assocs
        Query themeAssocQuery = strategy.getNamedQuery("WeblogThemeAssoc.getThemeAssocsByWeblog");
        themeAssocQuery.setParameter(1,website);
        List<WeblogThemeAssoc> assocResults = themeAssocQuery.getResultList();
        for(WeblogThemeAssoc themeAssoc : assocResults) {
            this.strategy.remove(themeAssoc);
        }
        
        // remove tags
        Query tagQuery = strategy.getNamedQuery("WeblogEntryTag.getByWeblog");
        tagQuery.setParameter(1, website);
        List<WeblogEntryTag> results = tagQuery.getResultList();
        
        for(WeblogEntryTag tagData : results) {
            if (tagData.getWeblogEntry() != null) {
                tagData.getWeblogEntry().getTags().remove(tagData);
            }
            this.strategy.remove(tagData);
        }
        
        // remove site tag aggregates
        List tags = emgr.getTags(website, null, null, 0, -1);
        updateTagAggregates(tags);
        
        // delete all weblog tag aggregates
        Query removeAggs= strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.removeByWeblog");
        removeAggs.setParameter(1, website);
        removeAggs.executeUpdate();
        
        // delete all bad counts
        Query removeCounts = strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.removeByTotalLessEqual");
        removeCounts.setParameter(1, 0);
        removeCounts.executeUpdate();
        
        // Remove the website's ping queue entries
        Query q = strategy.getNamedQuery("PingQueueEntry.getByWebsite");
        q.setParameter(1, website);
        List queueEntries = q.getResultList();
        for (Object obj : queueEntries) {
            this.strategy.remove(obj);
        }
        
        // Remove the website's auto ping configurations
        AutoPingManager autoPingMgr = roller.getAutopingManager();
        List<AutoPing> autopings = autoPingMgr.getAutoPingsByWebsite(website);
        for (AutoPing autoPing : autopings) {
            this.strategy.remove(autoPing);
        }
        
        // remove associated referers
        Query refQuery2 = strategy.getNamedQuery("WeblogReferrer.getByWebsite");
        refQuery2.setParameter(1, website);
        List referers = refQuery2.getResultList();
        for (Object obj : referers) {
            WeblogReferrer referer = (WeblogReferrer) obj;
            this.strategy.remove(referer.getClass(), referer.getId());
        }
        // TODO: can we eliminate this unnecessary flush with OpenJPA 1.0
        this.strategy.flush(); 
       
        // remove associated pages
        Query pageQuery = strategy.getNamedQuery("WeblogTemplate.getByWebsite");
        pageQuery.setParameter(1, website);
        List pages = pageQuery.getResultList();
        for (Object obj : pages) {
            WeblogTemplate page = (WeblogTemplate) obj;

            //remove associated templateCode objects
            this.removeTemplateCodeObjs(page);

            this.strategy.remove(page);
        }
        
        // remove folders (including bookmarks)
        Query folderQuery = strategy.getNamedQuery("WeblogBookmarkFolder.getByWebsite");
        folderQuery.setParameter(1, website);
        List<WeblogBookmarkFolder> folders = pageQuery.getResultList();
        for (WeblogBookmarkFolder wbf : folders) {
            this.strategy.remove(wbf);
        }

        // remove mediafile metadata
        // remove uploaded files
        MediaFileManager mfmgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        mfmgr.removeAllFiles(website);
        //List<MediaFileDirectory> dirs = mmgr.getMediaFileDirectories(website);
        //for (MediaFileDirectory dir : dirs) {
            //this.strategy.remove(dir);
        //}
        this.strategy.flush();

        // remove entries
        Query refQuery = strategy.getNamedQuery("WeblogEntry.getByWebsite");
        refQuery.setParameter(1, website);
        List<WeblogEntry> entries = refQuery.getResultList();
        for (WeblogEntry entry : entries) {
            emgr.removeWeblogEntry(entry);
        }
        this.strategy.flush();
        
        // delete all weblog categories
        Query removeCategories= strategy.getNamedUpdate(
                "WeblogCategory.removeByWeblog");
        removeCategories.setParameter(1, website);
        removeCategories.executeUpdate();

        // remove permissions
        for (WeblogPermission perm : umgr.getWeblogPermissions(website)) {
            umgr.revokeWeblogPermission(perm.getWeblog(), perm.getUser(), WeblogPermission.ALL_ACTIONS);
        }
        
        // flush the changes before returning. This is required as there is a
        // circular dependency between WeblogCategory and Weblog
        this.strategy.flush();        
    }
    
    protected void updateTagAggregates(List<TagStat> tags) throws WebloggerException {
        for(TagStat stat : tags) {
            Query query = strategy.getNamedUpdate(
                "WeblogEntryTagAggregate.getByName&WebsiteNullOrderByLastUsedDesc");
            query.setParameter(1, stat.getName());
            try {
                WeblogEntryTagAggregate agg = (WeblogEntryTagAggregate)query.getSingleResult();
                agg.setTotal(agg.getTotal() - stat.getCount());
            } catch (NoResultException ignored) {} // no agg to be updated
        }
    }
    
    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#savePage(WeblogTemplate)
     */
    public void savePage(WeblogTemplate page) throws WebloggerException {
        this.strategy.store(page);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(page.getWebsite());
    }

    public void saveTemplateCode(WeblogThemeTemplateCode templateCode) throws WebloggerException {
        this.strategy.store(templateCode);
        // update of the template should happen by saving template page.
    }
    
    public void removePage(WeblogTemplate page) throws WebloggerException {
        //remove template code objects
       this.removeTemplateCodeObjs(page);

        this.strategy.remove(page);
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(page.getWebsite());
    }
    
    public void addWeblog(Weblog newWeblog) throws WebloggerException {

        this.strategy.store(newWeblog);
        this.strategy.flush();
        this.addWeblogContents(newWeblog);
    }
    
    private void addWeblogContents(Weblog newWeblog)
    throws WebloggerException {
        
        // grant weblog creator ADMIN permission
        List<String> actions = new ArrayList<String>();
        actions.add(WeblogPermission.ADMIN);
        roller.getUserManager().grantWeblogPermission(
                newWeblog, newWeblog.getCreator(), actions);
        
        String cats = WebloggerConfig.getProperty("newuser.categories");
        WeblogCategory firstCat = null;
        String[] splitcats = cats.split(",");
        for (int i=0; i<splitcats.length; i++) {
            if (splitcats[i].trim().length() == 0) {
                continue;
            }
            WeblogCategory c = new WeblogCategory(
                    newWeblog,       // newWeblog
                    splitcats[i],    // name
                    null,            // description
                    null );          // image
            if (firstCat == null) {
                firstCat = c;
            }
            this.strategy.store(c);
        }

        // Use first category as default for Blogger API
        if (firstCat != null) {
            newWeblog.setBloggerCategory(firstCat);
        }

        this.strategy.store(newWeblog);

        // add default bookmarks
        WeblogBookmarkFolder defaultFolder = new WeblogBookmarkFolder(
                "default", newWeblog);
        this.strategy.store(defaultFolder);
        
        String blogroll = WebloggerConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (int i=0; i<splitroll.length; i++) {
                String[] rollitems = splitroll[i].split("\\|");
                if (rollitems != null && rollitems.length > 1) {
                    WeblogBookmark b = new WeblogBookmark(
                            defaultFolder,       // parent
                            rollitems[0],        // name
                            "",                  // description
                            rollitems[1].trim(), // url
                            null,                // feedurl
                            null);               // image
                    this.strategy.store(b);
                }
            }
        }

        roller.getMediaFileManager().createRootMediaFileDirectory(newWeblog);

        // flush so that all data up to this point can be available in db
        this.strategy.flush();

        // add any auto enabled ping targets
        PingTargetManager pingTargetMgr = roller.getPingTargetManager();
        AutoPingManager autoPingMgr = roller.getAutopingManager();
        
        for (PingTarget pingTarget : pingTargetMgr.getCommonPingTargets()) {
            if(pingTarget.isAutoEnabled()) {
                AutoPing autoPing = new AutoPing(
                        null, pingTarget, newWeblog);
                autoPingMgr.saveAutoPing(autoPing);
            }
        }

    }
    
    public Weblog getWeblog(String id) throws WebloggerException {
        return (Weblog) this.strategy.load(Weblog.class, id);
    }
    
    public Weblog getWeblogByHandle(String handle) throws WebloggerException {
        return getWeblogByHandle(handle, Boolean.TRUE);
    }
    
    /**
     * Return website specified by handle.
     */
    public Weblog getWeblogByHandle(String handle, Boolean enabled)
    throws WebloggerException {
        
        if (handle==null ) {
            throw new WebloggerException("Handle cannot be null");
        }
        
        // check cache first
        // NOTE: if we ever allow changing handles then this needs updating
        if(this.weblogHandleToIdMap.containsKey(handle)) {
            
            Weblog weblog = this.getWeblog(
                    (String) this.weblogHandleToIdMap.get(handle));
            if(weblog != null) {
                // only return weblog if enabled status matches
                if(enabled == null || enabled.equals(weblog.getEnabled())) {
                    log.debug("weblogHandleToId CACHE HIT - "+handle);
                    return weblog;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.weblogHandleToIdMap.remove(handle);
            }
        }
        
        Query query = strategy.getNamedQuery("Weblog.getByHandle");
        query.setParameter(1, handle);
        Weblog website = null;
        try {
            website = (Weblog)query.getSingleResult();
        } catch (NoResultException e) {
            website = null;
        }
        
        // add mapping to cache
        if(website != null) {
            log.debug("weblogHandleToId CACHE MISS - "+handle);
            this.weblogHandleToIdMap.put(website.getHandle(), website.getId());
        }
        
        if(website != null &&
                (enabled == null || enabled.equals(website.getEnabled()))) {
            return website;
        } else {
            return null;
        }
    }
    
    /**
     * Get websites of a user
     */
    public List<Weblog> getWeblogs(
            Boolean enabled, Boolean active,
            Date startDate, Date endDate, int offset, int length) throws WebloggerException {
        
        //if (endDate == null) endDate = new Date();
                      
        List params = new ArrayList();
        int size = 0;
        StringBuilder queryString = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        
        queryString.append("SELECT w FROM Weblog w WHERE ");

        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, start);
            whereClause.append(" w.dateCreated > ?" + size);
        }
        if (endDate != null) {
            Timestamp end = new Timestamp(endDate.getTime());
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, end);
            whereClause.append(" w.dateCreated < ?" + size);
        }
        if (enabled != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, enabled);
            whereClause.append(" w.enabled = ?" + size);
        }
        if (active != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(size++, active);
            whereClause.append(" w.active = ?" + size);
        }      
                
        whereClause.append(" ORDER BY w.dateCreated DESC");
        
        Query query = strategy.getDynamicQuery(queryString.toString() + whereClause.toString());
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

    public WeblogThemeAssoc getThemeAssoc(Weblog weblog , String type) throws WebloggerException {

        if(weblog == null){
            throw new WebloggerException("Weblog is null");
        }
        if(type == null){
            throw new WebloggerException("Type is null");
        }
        Query query = strategy.getNamedQuery("WeblogThemeAssoc.getThemeAssocByType") ;
        query.setParameter(1,weblog);
        query.setParameter(2,type);

        return (WeblogThemeAssoc) query.getSingleResult();
    }

    public void saveThemeAssoc(WeblogThemeAssoc themeAssoc) throws WebloggerException {
          this.strategy.store(themeAssoc);

        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(themeAssoc.getWeblog());
    }
        
    public List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException {
        List<Weblog> weblogs = new ArrayList<Weblog>();
        if (user == null) {
            return weblogs;
        }
        List<WeblogPermission> perms = roller.getUserManager().getWeblogPermissions(user);
        for (WeblogPermission perm : perms) {
            Weblog weblog = perm.getWeblog();
            if (!enabledOnly || weblog.getEnabled()) {
                if (weblog.getActive() != null && weblog.getActive()) {
                    weblogs.add(weblog);
                }
            }
        }
        return weblogs;
    }
    
    public List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException {
        List<User> users = new ArrayList<User>();
        List<WeblogPermission> perms = roller.getUserManager().getWeblogPermissions(weblog);
        for (WeblogPermission perm : perms) {
            User user = perm.getUser();
            if (user == null) {
                log.error("ERROR user is null, userName:" + perm.getUserName());
                continue;
            }
            if (!enabledOnly || user.getEnabled()) {
                users.add(user);
            }
        }
        return users;
    }

    public WeblogTemplate getPage(String id) throws WebloggerException {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) {
            return null;
        }
        
        return (WeblogTemplate)this.strategy.load(WeblogTemplate.class,id);
    }
    
    /**
     * Use JPA directly because Weblogger's Query API does too much allocation.
     */
    public WeblogTemplate getPageByLink(Weblog website, String pagelink)
    throws WebloggerException {
        
        if (website == null) {
            throw new WebloggerException("userName is null");
        }

        if (pagelink == null) {
            throw new WebloggerException("Pagelink is null");
        }

        Query query = strategy.getNamedQuery("WeblogTemplate.getByWebsite&Link");
        query.setParameter(1, website);
        query.setParameter(2, pagelink);
        try {
            return (WeblogTemplate) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#getPageByAction(Weblog, String)
     */
    public WeblogTemplate getPageByAction(Weblog website, String action)
            throws WebloggerException {
        
        if (website == null) {
            throw new WebloggerException("website is null");
        }

        if (action == null) {
            throw new WebloggerException("Action name is null");
        }
        
        Query query = strategy.getNamedQuery("WeblogTemplate.getByAction"); 
        query.setParameter(1, website);
        query.setParameter(2, action);
        try {
            return (WeblogTemplate)query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }        
    }
    
    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#getPageByName(Weblog, java.lang.String)
     */
    public WeblogTemplate getPageByName(Weblog website, String pagename)
    throws WebloggerException {
        
        if (website == null) {
            throw new WebloggerException("website is null");
        }
        
        if (pagename == null) {
            throw new WebloggerException("Page name is null");
        }
        
        Query query = strategy.getNamedQuery("WeblogTemplate.getByWebsite&Name");
        query.setParameter(1, website);
        query.setParameter(2, pagename);
        try {
            return (WeblogTemplate)query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public WeblogThemeTemplateCode getTemplateCodeByType(String templateId, String type) throws WebloggerException{
        if(templateId == null) {
            throw new WebloggerException("Template Name is null");
        }

        if (type == null) {
            throw new WebloggerException("Type is null");
        }

        Query query = strategy.getNamedQuery("WeblogThemeTemplateCode.getTemplateCodeByType");
        query.setParameter(1, templateId);
        query.setParameter(2, type);
        try {
            return (WeblogThemeTemplateCode)query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * @see org.apache.roller.weblogger.business.WeblogManager#getPages(Weblog)
     */
    public List<WeblogTemplate> getPages(Weblog website) throws WebloggerException {
        if (website == null) {
            throw new WebloggerException("website is null");
        }
        Query q = strategy.getNamedQuery(
                "WeblogTemplate.getByWebsiteOrderByName");
        q.setParameter(1, website);
        return q.getResultList();
    }

    
    public Map getWeblogHandleLetterMap() throws WebloggerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map results = new TreeMap();
        Query query = strategy.getNamedQuery(
                "Weblog.getCountByHandleLike");
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
        Query query = strategy.getNamedQuery(
                "Weblog.getByLetterOrderByHandle");
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
        
        Query query = null;
        
        if (endDate == null) {
            endDate = new Date();
        }
        
        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWebsiteByEndDate&StartDate");
            query.setParameter(1, end);
            query.setParameter(2, start);
        } else {
            Timestamp end = new Timestamp(endDate.getTime());
            query = strategy.getNamedQuery(
                    "WeblogEntryComment.getMostCommentedWebsiteByEndDate");
            query.setParameter(1, end);
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        List queryResults = query.getResultList();
        List<StatCount> results = new ArrayList<StatCount>();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            StatCount sc = new StatCount(
                    (String)row[1],                     // website id
                    (String)row[2],                     // website handle
                    (String)row[3],                     // website name
                    "statCount.weblogCommentCountType", // stat type
                    ((Long)row[0]));        // # comments
            sc.setWeblogHandle((String)row[2]);
            results.add(sc);
        }
        // Original query ordered by desc # comments.
        // JPA QL doesn't allow queries to be ordered by agregates; do it in memory
        Collections.sort(results, STAT_COUNT_COUNT_REVERSE_COMPARATOR);
        
        return results;
    }
    
    /**
     * Get count of weblogs, active and inactive
     */
    public long getWeblogCount() throws WebloggerException {
        long ret = 0;
        List results = strategy.getNamedQuery(
                "Weblog.getCountAllDistinct").getResultList();
        
        ret = (Long)results.get(0);
        
        return ret;
    }

    private void removeTemplateCodeObjs(WeblogTemplate page) throws WebloggerException {
        Query codeQuery = strategy.getNamedQuery("WeblogThemeTemplateCode.getTemplateCodesByTemplateId");
        codeQuery.setParameter(1, page.getId());
        List codeList = codeQuery.getResultList();

        for (Object obj : codeList) {
            this.strategy.remove(obj);
        }
    }

}
