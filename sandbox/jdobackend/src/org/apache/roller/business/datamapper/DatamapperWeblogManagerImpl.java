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

package org.apache.roller.business.datamapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.TreeMap;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.HitCountData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.pojos.TagStat;
import org.apache.roller.pojos.TagStatComparator;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogEntryTagAggregateData;
import org.apache.roller.pojos.WeblogEntryTagData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.EntryAttributeData;
import org.apache.roller.util.DateUtil;

/*
 * DatamapperWeblogManagerImpl.java
 *
 * Created on May 31, 2006, 4:08 PM
 *
 */
public abstract class DatamapperWeblogManagerImpl implements WeblogManager {

    protected static Log log = LogFactory.getLog(
        DatamapperWeblogManagerImpl.class);
    
    protected DatamapperPersistenceStrategy strategy;

    // cached mapping of entryAnchors -> entryIds
    private Hashtable entryAnchorToIdMap = new Hashtable();

    /* inline creation of reverse comparator, anonymous inner class */
    private Comparator reverseComparator = new ReverseComparator();

    private Comparator tagStatComparator = new TagStatComparator();

    public DatamapperWeblogManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        log.debug("Instantiating Datamapper Weblog Manager");

        this.strategy = strategy;
    }

    /**
     * @inheritDoc
     */
    public void saveWeblogCategory(WeblogCategoryData cat)
            throws RollerException {
        if(cat.getId() == null && this.isDuplicateWeblogCategoryName(cat)) {
            throw new RollerException("Duplicate category name, cannot save category");
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());
        
        this.strategy.store(cat);
    }

    /**
     * @inheritDoc
     */
    public void removeWeblogCategory(WeblogCategoryData cat)
            throws RollerException {
        if(cat.retrieveWeblogEntries(true).size() > 0) {
            throw new RollerException("Cannot remove category with entries");
        }
        
        // remove cat
        this.strategy.remove(cat);
        //relationship management for the other side
        WeblogCategoryData parent = cat.getParent();
        if(parent != null) {
            parent.getWeblogCategories().remove(cat);
        }
        
        // update website default cats if needed
        if(cat.getWebsite().getBloggerCategory().equals(cat)) {
            WeblogCategoryData rootCat = this.getRootWeblogCategory(cat.getWebsite());
            cat.getWebsite().setBloggerCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        if(cat.getWebsite().getDefaultCategory().equals(cat)) {
            WeblogCategoryData rootCat = this.getRootWeblogCategory(cat.getWebsite());
            cat.getWebsite().setDefaultCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(
                cat.getWebsite());
    }    
    
    /**
     * @inheritDoc
     */
    public void moveWeblogCategory(WeblogCategoryData srcCat, WeblogCategoryData destCat)
            throws RollerException {
        
        // TODO: this check should be made before calling this method?
        if (destCat.descendentOf(srcCat)) {
            throw new RollerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        log.debug("Moving category "+srcCat.getPath() +
            " under "+destCat.getPath());
        
        srcCat.setParent(destCat);
        if("/".equals(destCat.getPath())) {
            srcCat.setPath("/"+srcCat.getName());
        } else {
            srcCat.setPath(destCat.getPath() + "/" + srcCat.getName());
        }
        saveWeblogCategory(srcCat);
        
        // the main work to be done for a category move is to update the 
        // path attribute of the category and all descendent categories
        updatePathTree(srcCat);
    }
    
    
    // updates the paths of all descendents of the given category
    private void updatePathTree(WeblogCategoryData cat) 
            throws RollerException {
        
        log.debug("Updating path tree for category "+cat.getPath());
        
        WeblogCategoryData childCat = null;
        Iterator childCats = cat.getWeblogCategories().iterator();
        while(childCats.hasNext()) {
            childCat = (WeblogCategoryData) childCats.next();
            
            log.debug("OLD child category path was "+childCat.getPath());
            
            // update path and save
            if("/".equals(cat.getPath())) {
                childCat.setPath("/" + childCat.getName());
            } else {
                childCat.setPath(cat.getPath() + "/" + childCat.getName());
            }
            saveWeblogCategory(childCat);
            
            log.debug("NEW child category path is "+ childCat.getPath());
            
            // then make recursive call to update this cats children
            updatePathTree(childCat);
        }
    }
    
    /**
     * @inheritDoc
     */
    public void moveWeblogCategoryContents(WeblogCategoryData srcCat, 
                WeblogCategoryData destCat)
            throws RollerException {
                
        // TODO: this check should be made before calling this method?
        if (destCat.descendentOf(srcCat)) {
            throw new RollerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        // get all entries in category and subcats
        List results = srcCat.retrieveWeblogEntries(true);
        
        // Loop through entries in src cat, assign them to dest cat
        Iterator iter = results.iterator();
        WebsiteData website = destCat.getWebsite();
        while (iter.hasNext()) {
            WeblogEntryData entry = (WeblogEntryData) iter.next();
            entry.setCategory(destCat);
            entry.setWebsite(website);
            this.strategy.store(entry);
        }
        
        // Make sure website's default and bloggerapi categories
        // are valid after the move
        
        if (srcCat.getWebsite().getDefaultCategory().getId()
                .equals(srcCat.getId())
        || srcCat.getWebsite().getDefaultCategory().descendentOf(srcCat)) {
            srcCat.getWebsite().setDefaultCategory(destCat);
            this.strategy.store(srcCat.getWebsite());
        }
        
        if (srcCat.getWebsite().getBloggerCategory().getId()
                .equals(srcCat.getId())
        || srcCat.getWebsite().getBloggerCategory().descendentOf(srcCat)) {
            srcCat.getWebsite().setBloggerCategory(destCat);
            this.strategy.store(srcCat.getWebsite());
        }
    }

    /**
     * @inheritDoc
     */
    public void saveComment(CommentData comment) throws RollerException {
        this.strategy.store(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
            .saveWebsite(comment.getWeblogEntry().getWebsite());
    }

    /**
     * @inheritDoc
     */
    public void removeComment(CommentData comment) throws RollerException {
        this.strategy.remove(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
            .saveWebsite(comment.getWeblogEntry().getWebsite());
    }

    /**
     * @inheritDoc
     */
    // TODO: perhaps the createAnchor() and queuePings() items should go outside this method?
    public void saveWeblogEntry(WeblogEntryData entry) throws RollerException {
        
        if (entry.getAnchor() == null || entry.getAnchor().trim().equals("")) {
            entry.setAnchor(this.createAnchor(entry));
        }
        
        for(Iterator it = entry.getAddedTags().iterator(); it.hasNext();) {
            String name = (String) it.next();
            updateTagCount(name, entry.getWebsite(), 1);
        }
        
        for(Iterator it = entry.getRemovedTags().iterator(); it.hasNext();) {
            String name = (String) it.next();
            updateTagCount(name, entry.getWebsite(), -1);
        }  
        
        this.strategy.store(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            RollerFactory.getRoller().getUserManager()
                .saveWebsite(entry.getWebsite());
        }
        
        if(entry.isPublished()) {
            // Queue applicable pings for this update.
            RollerFactory.getRoller().getAutopingManager()
                .queueApplicableAutoPings(entry);
        }
    }

    /**
     * @inheritDoc
     */
    public void removeWeblogEntry(WeblogEntryData entry)
            throws RollerException {
        List referers = (List) strategy.newQuery(RefererData.class, 
            "RefererData.getByWeblogEntry").execute(entry);
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            RefererData referer = (RefererData) iter.next();
            this.strategy.remove(referer);
        }
        
        // remove comments
        List comments = getComments(
                null,  // website
                entry,
                null,  // search String
                null,  // startDate
                null,  // endDate
                null,  // pending
                null,  // approved
                null,  // spam
                true,  // reverse chrono order (not that it matters)
                0,     // offset
                -1);   // no limit
        Iterator commentsIT = comments.iterator();
        while (commentsIT.hasNext()) {
            this.strategy.remove((CommentData) commentsIT.next());
        }
        
        // remove tags aggregates
        if(entry.getTags() != null) {
            for(Iterator it = entry.getTags().iterator(); it.hasNext(); ) {
                WeblogEntryTagData tag = (WeblogEntryTagData) it.next();
                updateTagCount(tag.getName(), entry.getWebsite(), -1);
            }
        }
        
        // remove entry
        this.strategy.remove(entry);
        
        // update weblog last modified date.  date updated by saveWebsite()
        if(entry.isPublished()) {
            RollerFactory.getRoller().getUserManager()
                .saveWebsite(entry.getWebsite());
        }
        
        // remove entry from cache mapping
        this.entryAnchorToIdMap.remove(entry.getWebsite().getHandle()+":"+entry.getAnchor());
    }

    public List getNextPrevEntries(WeblogEntryData current, String catName, 
                                   String locale, int maxEntries, boolean next)
            throws RollerException {
        DatamapperQuery query = null;
        List results = null;
        WeblogCategoryData category = null;
        
        if (catName != null && !catName.trim().equals("/")) {
            category = getWeblogCategoryByPath(current.getWebsite(), null, 
                catName);
        }
        
        if (category != null) {
            if (locale != null) {
                if (next) {
                    query = strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeGreater&Category&LocaleLikeOrderByPubTimeAsc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                            WeblogEntryData.PUBLISHED, 
                            current.getPubTime(), category, locale + '%'});
                } else {
                    query = strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeLess&Category&LocaleLikeOrderByPubTimeDesc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        category, locale + '%'});
                }
            } else {
                if (next) {
                    query = strategy.newQuery(WeblogEntryData.class,
                            "WeblogEntryData.getByWebsite&Status&PubTimeGreater&CategoryOrderByPubTimeAsc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        category});
                
                } else {
                    query = strategy.newQuery(WeblogEntryData.class,
                            "WeblogEntryData.getByWebsite&Status&PubTimeLess&CategoryOrderByPubTimeDesc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        category});                
                }
            }
        } else {
            if (locale != null) {
                if (next) {
                    query = strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeGreater&LocaleLikeOrderByPubTimeAsc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        locale + '%'});
                } else {
                    query = strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeLess&LocaleLikeOrderByPubTimeDesc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        locale + '%'});  
                }
            } else {
                if (next) {
                    query = strategy.newQuery(WeblogEntryData.class,
                            "WeblogEntryData.getByWebsite&Status&PubTimeGreaterOrderByPubTimeAsc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime()});
                
                } else {
                    query = strategy.newQuery(WeblogEntryData.class,
                            "WeblogEntryData.getByWebsite&Status&PubTimeLessOrderByPubTimeDesc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime()});                
                }
            }
        }
        return results; 
    }
    
    /**
     * @inheritDoc
     */
    public WeblogCategoryData getRootWeblogCategory(WebsiteData website)
            throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        
        return (WeblogCategoryData) strategy.newQuery(WeblogCategoryData.class, 
                "WeblogCategoryData.getByWebsite&ParentNull")
                        .setUnique().execute(website);
    }

    /**
     * @inheritDoc
     */
    public List getWeblogCategories(WebsiteData website, boolean includeRoot)
            throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        
        if (includeRoot) return getWeblogCategories(website);
        
        return (List) strategy.newQuery(WeblogCategoryData.class, 
                "WeblogCategoryData.getByWebsite&ParentNull").execute(website);
    }

    /**
     * @inheritDoc
     */
    public List getWeblogCategories(WebsiteData website)
            throws RollerException {
        if (website == null)
            throw new RollerException("website is null");

        return (List) strategy.newQuery(WeblogCategoryData.class, 
                "WeblogCategoryData.getByWebsite").execute(website);
    }

    /**
     * @inheritDoc
     */
    public List getWeblogEntriesPinnedToMain(Integer max)
            throws RollerException {
        DatamapperQuery query = strategy.newQuery(WeblogCategoryData.class, 
                "WeblogEntryData.getByPinnedToMainOrderByPubTimeDesc");
        if (max != null) {
            query.setRange(0, max.intValue());
        }
        return (List) query.execute(Boolean.TRUE);   
    }

    public void removeWeblogEntryAttribute(String name, WeblogEntryData entry)
            throws RollerException {
        for (Iterator it = entry.getEntryAttributes().iterator(); it.hasNext();) {
            EntryAttributeData entryAttribute = (EntryAttributeData) it.next();
            if (entryAttribute.getName().equals(name)) {
                //Call back the entity to adjust its internal state
                entry.onRemoveEntryAttribute(entryAttribute);
                //Remove it from database
                this.strategy.remove(entryAttribute);
                //Remove it from the collection
                it.remove();
            }
        }
    }

    public void removeWeblogEntryTag(String name, WeblogEntryData entry)
            throws RollerException {
        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTagData tag = (WeblogEntryTagData) it.next();
            if (tag.getName().equals(name)) {
                //Call back the entity to adjust its internal state
                entry.onRemoveTag(name);
                //Remove it from database
                this.strategy.remove(tag);
                //Remove it from the collection
                it.remove();
            }
        }
    }

    /**
     * @inheritDoc
     */
    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website,
            String anchor) throws RollerException {
        
        if (website == null)
            throw new RollerException("Website is null");

        if (anchor == null)
            throw new RollerException("Anchor is null");

        // mapping key is combo of weblog + anchor
        String mappingKey = website.getHandle()+":"+anchor;

        // check cache first
        // NOTE: if we ever allow changing anchors then this needs updating
        if(this.entryAnchorToIdMap.containsKey(mappingKey)) {

            WeblogEntryData entry = this.getWeblogEntry((String) this.entryAnchorToIdMap.get(mappingKey));
            if(entry != null) {
                log.debug("entryAnchorToIdMap CACHE HIT - "+mappingKey);
                return entry;
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.entryAnchorToIdMap.remove(mappingKey);
            }
        }

        // cache failed, do lookup
        WeblogEntryData entry = (WeblogEntryData)strategy.newQuery(WeblogEntryData.class,
            "WeblogEntryData.getByWebsite&AnchorOrderByPubTimeDesc")
            .setUnique().execute(new Object[]{website, anchor});

        // add mapping to cache
        if(entry != null) {
            log.debug("entryAnchorToIdMap CACHE MISS - "+mappingKey);
            this.entryAnchorToIdMap.put(mappingKey, entry.getId());
        }
        return entry;
    }

    /**
     * @inheritDoc
     */
    // TODO: this method should be removed and it's functionality moved to getWeblogEntries()
    public List getWeblogEntries(WeblogCategoryData cat, boolean subcats)
            throws RollerException {
        List results = null;
        
        if (!subcats) {
            results = (List) strategy.newQuery(WeblogEntryData.class, 
                "WeblogEntryData.getByCategory")
                .execute(cat);
        } else {
            results = (List) strategy.newQuery(WeblogEntryData.class, 
                "WeblogEntryData.getByCategory.pathLike&Website")
                .execute(new Object[] {cat.getPath() + '%', cat.getWebsite()});
        }
        
        return results;
    }

    /**
     * @inheritDoc
     */
    public String createAnchor(WeblogEntryData entry) throws RollerException {
        // Check for uniqueness of anchor
        String base = entry.createAnchorBase();
        String name = base;
        int count = 0;
            
        while (true) {
            if (count > 0) {
                name = base + count;
            }
                
            List results = (List) strategy.newQuery(WeblogEntryData.class, 
                "WeblogEntryData.getByWebsite&Anchor")
                .execute(new Object[] {entry.getWebsite(), name});
                
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
    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData cat)
            throws RollerException {

        // ensure that no sibling categories share the same name
        WeblogCategoryData parent = cat.getParent();
        if (null != parent) {
            return (getWeblogCategoryByPath(
                cat.getWebsite(), cat.getPath()) != null);
        }
        
        return false;
    }

    /**
     * @inheritDoc
     */
    public boolean isWeblogCategoryInUse(WeblogCategoryData cat)
            throws RollerException {
        int entryCount = ((List) strategy.newQuery(
            WeblogEntryData.class, "").execute(cat)).size();
            
        if (entryCount > 0) {
            return true;
        }
            
            Iterator cats = cat.getWeblogCategories().iterator();
            while (cats.hasNext()) {
                WeblogCategoryData childCat = (WeblogCategoryData)cats.next();
                if (childCat.isInUse()) {
                    return true;
                }
            }
            
            if (cat.getWebsite().getBloggerCategory().equals(cat)) {
                return true;
            }
            
            if (cat.getWebsite().getDefaultCategory().equals(cat)) {
                return true;
            }
            
        return false;
    }

    /**
     * @inheritDoc
     */
    public abstract List getComments(
            WebsiteData     website,
            WeblogEntryData entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            Boolean         pending,
            Boolean         approved,
            Boolean         spam,
            boolean         reverseChrono,
            int             offset,
            int             length
            ) throws RollerException;

    /**
     * @inheritDoc
     */
    public int removeMatchingComments(
            WebsiteData     website, 
            WeblogEntryData entry, 
            String  searchString, 
            Date    startDate, 
            Date    endDate, 
            Boolean pending, 
            Boolean approved, 
            Boolean spam) throws RollerException {
        List comments = getComments( 
            website, entry, searchString, startDate, endDate, 
            pending, approved, spam, true, 0, -1);
        int count = 0;
        for (Iterator it = comments.iterator(); it.hasNext();) {
            CommentData comment = (CommentData) it.next();
            removeComment(comment);
            count++;
        }
        return count;
    }
    
    /**
     * @inheritDoc
     */
    public WeblogCategoryData getWeblogCategory(String id)
            throws RollerException {
        return (WeblogCategoryData) this.strategy.load(
                WeblogCategoryData.class, id);
    }

    //--------------------------------------------- WeblogCategoryData Queries
    
    /**
     * @inheritDoc
     */
    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website,
            String categoryPath) throws RollerException {
        return getWeblogCategoryByPath(website, null, categoryPath);
    }

    /**
     * @inheritDoc
     */
    // TODO: ditch this method in favor of getWeblogCategoryByPath(weblog, path)
    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website,
            WeblogCategoryData category, String path) throws RollerException {
        
        if (path == null || path.trim().equals("/")) {
            return getRootWeblogCategory(website);
        } else {
            String catPath = path;
            
            // all cat paths must begin with a '/'
            if(!catPath.startsWith("/")) {
                catPath = "/"+catPath;
            }
            
            // now just do simple lookup by path
            return (WeblogCategoryData) strategy.newQuery(WeblogCategoryData.class, 
                    "WeblogCategoryData.getByPath&Website").setUnique().
                    execute(new Object[] {catPath, website});
        }
    }

    /**
     * @inheritDoc
     */
    public CommentData getComment(String id) throws RollerException {
        return (CommentData) this.strategy.load(CommentData.class, id);
    }

    /**
     * @inheritDoc
     */
    public WeblogEntryData getWeblogEntry(String id) throws RollerException {
        return (WeblogEntryData)strategy.load(WeblogEntryData.class, id);
    }

    /**
     * @inheritDoc
     */
    public Map getWeblogEntryObjectMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,            
            String  status,
            String  locale,
            int     offset,
            int     length) throws RollerException {
        return getWeblogEntryMap(
            website,
            startDate,
            endDate,
            catName,
            tags,    
            status,
            false,
            locale,
            offset,
            length);
    }

    /**
     * @inheritDoc
     */
    public Map getWeblogEntryStringMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,            
            String  status,
            String  locale,
            int     offset,
            int     length
            ) throws RollerException {
        return getWeblogEntryMap(
            website,
            startDate,
            endDate,
            catName,
            tags,    
            status,
            true,
            locale,
            offset,
            length);
    }

    private Map getWeblogEntryMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            List    tags,
            String  status,
            boolean stringsOnly,
            String  locale,
            int     offset,
            int     length) throws RollerException {
        
        TreeMap map = new TreeMap(reverseComparator);
        
        List entries = getWeblogEntries(
            website,                 
            null,
            startDate,
            endDate,
            catName,
            tags,
            status,
            null,
            null,
            locale,          
            offset,
            length);
        
        Calendar cal = Calendar.getInstance();
        if (website != null) {
            cal.setTimeZone(website.getTimeZoneInstance());
        }
        
        SimpleDateFormat formatter = DateUtil.get8charDateFormat();
        for (Iterator wbItr = entries.iterator(); wbItr.hasNext();) {
            WeblogEntryData entry = (WeblogEntryData) wbItr.next();
            Date sDate = DateUtil.getNoonOfDay(entry.getPubTime(), cal);
            if (stringsOnly) {
                if (map.get(sDate) == null)
                    map.put(sDate, formatter.format(sDate));
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
    public List getMostCommentedWeblogEntries(WebsiteData website,
            Date startDate, Date endDate, int offset,
            int length) throws RollerException {
        DatamapperQuery query = null;
        List queryResults = null;
        boolean setRange = offset != 0 || length != -1;

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        if (endDate == null) endDate = new Date();

        if (website != null) {
            if (startDate != null) {
                query = strategy.newQuery(CommentData.class, 
                    "CommentData.getMostCommentedWeblogEntryByWebsite&EndDate&StartDate");
                if (setRange) query.setRange(offset, offset + length);
                queryResults = (List) query.execute(
                    new Object[] {website, endDate, startDate});
            } else {
                query = strategy.newQuery(CommentData.class, 
                    "CommentData.getMostCommentedWeblogEntryByWebsite&EndDate");
                if (setRange) query.setRange(offset, offset + length);
                queryResults = (List) query.execute(
                    new Object[] {website, endDate});            
            }
        } else {
            if (startDate != null) {
                query = strategy.newQuery(CommentData.class, 
                    "CommentData.getMostCommentedWeblogEntryByEndDate&StartDate");
                if (setRange) query.setRange(offset, offset + length);
                queryResults = (List) query.execute(
                    new Object[] {endDate, startDate});
            } else {
                query = strategy.newQuery(CommentData.class, 
                    "CommentData.getMostCommentedWeblogEntryByEndDate");
                if (setRange) query.setRange(offset, offset + length);
                queryResults = (List) query.execute(endDate);            
            }
        }
        
        List results = new ArrayList();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            results.add(new StatCount(
                    (String)row[1],                             // entry id
                    (String)row[2],                             // entry anchor
                    (String)row[3],                             // entry title
                    "statCount.weblogEntryCommentCountType",    // stat desc
                    ((Long)row[0]).longValue())); // count
          }
        //TODO Uncomment following once integrated with code
        //Collections.sort(results, StatCount.getComparator());
        Collections.reverse(results);
        return results;
    }

    /**
     * @inheritDoc
     */
    public WeblogEntryData getNextEntry(WeblogEntryData current,
            String catName, String locale) throws RollerException {
        WeblogEntryData entry = null;
        List entryList = getNextPrevEntries(current, catName, locale, 1, true);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }

    /**
     * @inheritDoc
     */
    public WeblogEntryData getPreviousEntry(WeblogEntryData current,
            String catName, String locale) throws RollerException {
        WeblogEntryData entry = null;
        List entryList = getNextPrevEntries(current, catName, locale, 1, false);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
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
    public abstract void applyCommentDefaultsToEntries(WebsiteData website)
            throws RollerException;

    /**
     * @inheritDoc
     */
    public List getPopularTags(WebsiteData website, Date startDate, int limit)
            throws RollerException {
        DatamapperQuery query = null;
        List queryResults = null;
        
        if (website != null) {
            if (startDate != null) {
                query = strategy.newQuery(WeblogEntryTagAggregateData.class, 
                    "WeblogEntryTagAggregateData.getPopularTagsByWebsite&StartDate");
                if (limit > 0) query.setRange(0, limit);
                queryResults = (List) query.execute(
                    new Object[] {website, startDate});
            } else {
                query = strategy.newQuery(WeblogEntryTagAggregateData.class, 
                    "WeblogEntryTagAggregateData.getPopularTagsByWebsite");
                if (limit > 0) query.setRange(0, limit);
                queryResults = (List) query.execute(startDate);                
            }
        } else {
            if (startDate != null) {
                query = strategy.newQuery(WeblogEntryTagAggregateData.class, 
                    "WeblogEntryTagAggregateData.getPopularTagsByWebsiteNull&StartDate");
                if (limit > 0) query.setRange(0, limit);
                queryResults = (List) query.execute(startDate);
            } else {
                query = strategy.newQuery(WeblogEntryTagAggregateData.class, 
                    "WeblogEntryTagAggregateData.getPopularTagsByWebsiteNull");
                if (limit > 0) query.setRange(0, limit);
                queryResults = (List) query.execute();                
            }
        }

        // TODO queryString.append("order by sum(total) desc");  ???
        
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;

        List results = new ArrayList(limit);
            
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            TagStat t = new TagStat();
            t.setName((String) row[0]);
            t.setCount(((Integer) row[1]).intValue());                
                
            min = Math.min(min, t.getCount());
            max = Math.max(max, t.getCount());                
            results.add(t);
        }
            
        min = Math.log(1+min);
        max = Math.log(1+max);

        double range = Math.max(.01, max - min) * 1.0001;
            
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            TagStat t = (TagStat) iter.next();
            t.setIntensity((int) (1 + Math.floor(5 * (Math.log(1+t.getCount()) - min) / range)));
        }            

        // sort results by name, because query had to sort by total
        Collections.sort(results, tagStatComparator);
            
        return results;
    }

    /**
     * @inheritDoc
     */
    public List getTags(WebsiteData website, String sortBy, 
            String startsWith, int limit) throws RollerException {    
        DatamapperQuery query = null;
        List queryResults = null;
        boolean sortByName = sortBy == null || !sortBy.equals("count");
        
        if (website != null) {
            if (startsWith != null) {
                if (sortByName) {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class, 
                        "WeblogEntryTagAggregateData.getTagsByWebsite&NameStartsWithOrderByName");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(
                        new Object[] {website, startsWith + '%'});
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsite&NameStartsWith");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(
                        new Object[] {website, startsWith + '%'});
                }
            } else {
                if (sortByName) {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteOrderByName");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(website);
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsite");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(website);
                }
            }
        } else {
            if (startsWith != null) {
                if (sortByName) {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteNull&NameStartsWithOrderByName");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(startsWith + '%');
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteNull&NameStartsWith");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(startsWith + '%');
                }
            } else {
                if (sortByName) {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteNullOrderByName");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute();
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteNull");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute();
                }
            }
        }
        
        List results = new ArrayList();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            TagStat ce = new TagStat();
            ce.setName((String) row[0]);
            //TODO: DatamapperPort. The query retrieves SUM(w.total) which is always long
            //TagStat should be changed to receive count as long
            ce.setCount(((Long) row[1]).intValue());
            results.add(ce);
        }
        
        if (!sortByName) {
            Collections.sort(results, tagStatComparator);
        }
        
        return results;
    }

    /**
     * @inheritDoc
     */
    public abstract boolean getTagComboExists(List tags, WebsiteData weblog) throws RollerException;
    
    /**
     * @inheritDoc
     */
    public abstract void updateTagCount(String name, WebsiteData website, int amount)
            throws RollerException; 

    /**
     * @inheritDoc
     */
    public HitCountData getHitCount(String id) throws RollerException {
         
        // do lookup
       return (HitCountData) strategy.load(HitCountData.class, id);
    }
    
    /**
     * @inheritDoc
     */
    public HitCountData getHitCountByWeblog(WebsiteData weblog) 
        throws RollerException {
        
        return (HitCountData) strategy.newQuery(HitCountData.class, 
                "HitCountData.getByWeblog").setUnique().execute(weblog);
    }
    
    /**
     * @inheritDoc
     */
    public List getHotWeblogs(int sinceDays, int offset, int length) 
        throws RollerException {
        
        // figure out start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        
        DatamapperQuery query = strategy.newQuery(HitCountData.class, 
                "HitCountData.getByWeblogEnabledTrueAndActiveTrue&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc");

        if (length != -1) {
            query.setRange(offset, length);
        }
        
        return (List) query.execute(startDate);
    }
    
    
    /**
     * @inheritDoc
     */
    public void saveHitCount(HitCountData hitCount) throws RollerException {
        this.strategy.store(hitCount);
    }
    
    
    /**
     * @inheritDoc
     */
    public void removeHitCount(HitCountData hitCount) throws RollerException {
        this.strategy.remove(hitCount);
    }
    
    
    /**
     * @inheritDoc
     */
    public void incrementHitCount(WebsiteData weblog, int amount)
        throws RollerException {

        if(amount == 0) {
            throw new RollerException("Tag increment amount cannot be zero.");
        }
        
        if(weblog == null) {
            throw new RollerException("Website cannot be NULL.");
        }

        HitCountData hitCount = (HitCountData) strategy.newQuery(
                HitCountData.class, 
                "HitCountData.getByWeblog")
            .setUnique().execute(weblog);
        
        // create it if it doesn't exist
        if(hitCount == null && amount > 0) {
            hitCount = new HitCountData();
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
    public abstract void resetAllHitCounts() throws RollerException;
        
    /**
     * @inheritDoc
     */
    public void resetHitCount(WebsiteData weblog) throws RollerException {

        HitCountData hitCount = (HitCountData) strategy.newQuery(HitCountData.class, 
                "HitCountData.getByWeblog").setUnique().execute(weblog);

        hitCount.setDailyHits(0);
        strategy.store(hitCount);
    }

    /**
     * @inheritDoc
     */
    public long getCommentCount() throws RollerException {
        List results = (List) strategy.newQuery(CommentData.class, 
            "CommentData.getCountAllDistinct").execute();
        
        return ((Long)results.get(0)).longValue();
    }

    /**
     * @inheritDoc
     */
    public long getCommentCount(WebsiteData website) throws RollerException {
        List results = (List) strategy.newQuery(CommentData.class, 
            "CommentData.getCountDistinctByWebsite").execute(website);
        
        return ((Long)results.get(0)).longValue();
    }

    /**
     * @inheritDoc
     */
    public long getEntryCount() throws RollerException {
        List results = (List) strategy.newQuery(WeblogEntryData.class, 
            "WeblogEntryData.getCountDistinctByStatus")
            .execute("PUBLISHED");
        
        return ((Long)results.get(0)).longValue();
    }

    /**
     * @inheritDoc
     */
    public long getEntryCount(WebsiteData website) throws RollerException {
        List results = (List) strategy.newQuery(WeblogEntryData.class, 
            "WeblogEntryData.getCountDistinctByStatus&Website")
            .execute(new Object[] {"PUBLISHED", website});
        
        return ((Long)results.get(0)).longValue();
    }
    
}
