
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogEntryTagData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.HitCountData;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.pojos.WeblogEntryTagAggregateData;
import org.apache.roller.pojos.TagStat;
import org.apache.roller.pojos.TagStatComparator;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.Utilities;

/*
 * DatamapperWeblogManagerImpl.java
 *
 * Created on May 31, 2006, 4:08 PM
 *
 */
public class DatamapperWeblogManagerImpl implements WeblogManager {

    private static Log log = LogFactory.getLog(
        DatamapperWeblogManagerImpl.class);
    
    private DatamapperPersistenceStrategy strategy;

    /* inline creation of reverse comparator, anonymous inner class */
    private Comparator reverseComparator = new ReverseComparator();

    private Comparator tagStatComparator = new TagStatComparator();

    public DatamapperWeblogManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        log.debug("Instantiating Datamapper Weblog Manager");

        this.strategy = strategy;
    }

    /**
     * Save weblog category.
     */
    public void saveWeblogCategory(WeblogCategoryData cat)
            throws RollerException {
        if(this.isDuplicateWeblogCategoryName(cat)) {
            throw new RollerException("Duplicate category name");
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(cat.getWebsite());
        
        this.strategy.store(cat);
    }

    public void moveWeblogCategory(WeblogCategoryData srcCat, 
        WeblogCategoryData destCat)
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
     * Remove weblog category.
     */
    public void removeWeblogCategory(WeblogCategoryData cat)
            throws RollerException {
        if(cat.retrieveWeblogEntries(true).size() > 0) {
            throw new RollerException("Cannot remove category with entries");
        }
        
        // remove cat
        this.strategy.remove(cat);
        
        // update website default cats if needed
        if(cat.getWebsite().getBloggerCategory().equals(cat)) {
            WeblogCategoryData rootCat = this.getRootWeblogCategory(
                cat.getWebsite());
            cat.getWebsite().setBloggerCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        if(cat.getWebsite().getDefaultCategory().equals(cat)) {
            WeblogCategoryData rootCat = this.getRootWeblogCategory(
                cat.getWebsite());
            cat.getWebsite().setDefaultCategory(rootCat);
            this.strategy.store(cat.getWebsite());
        }
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(
                cat.getWebsite());
    }

    /**
     * Recategorize all entries with one category to another.
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
     * Save comment.
     */
    public void saveComment(CommentData comment) throws RollerException {
        this.strategy.store(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
            .saveWebsite(comment.getWeblogEntry().getWebsite());
    }

    /**
     * Remove comment.
     */
    public void removeComment(CommentData comment) throws RollerException {
        this.strategy.remove(comment);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
            .saveWebsite(comment.getWeblogEntry().getWebsite());
    }

    /**
     * Save weblog entry.
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
     * Remove weblog entry.
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
        
        // TODO: remove entry from cache mapping
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
                            current.getPubTime(), category, locale});
                } else {
                    query = strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeLess&Category&LocaleLikeOrderByPubTimeDesc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        category, locale});  
                }
            } else {
                if (next) {
                    results = (List) strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeGreater&CategoryOrderByPubTimeAsc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        category});
                
                } else {
                    results = (List) strategy.newQuery(WeblogEntryData.class, 
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
                        locale});
                } else {
                    query = strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeLess&LocaleLikeOrderByPubTimeDesc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime(), 
                        locale});  
                }
            } else {
                if (next) {
                    results = (List) strategy.newQuery(WeblogEntryData.class, 
                            "WeblogEntryData.getByWebsite&Status&PubTimeGreater&OrderByPubTimeAsc");
                    if (maxEntries > -1) query.setRange(0, maxEntries);
                    results = (List) query.execute(
                        new Object[] {current.getWebsite(), 
                        WeblogEntryData.PUBLISHED, current.getPubTime()});
                
                } else {
                    results = (List) strategy.newQuery(WeblogEntryData.class, 
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
     * Get top level categories for a website.
     * @param website Website.
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
     * Get WebLogCategory objects for a website.
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
     * Get WebLogCategory objects for a website.
     */
    public List getWeblogCategories(WebsiteData website)
            throws RollerException {
        return (List) strategy.newQuery(WeblogCategoryData.class, 
                "WeblogCategoryData.getByWebsite").execute(website);
    }

    /**
     * Get WeblogEntries by offset/length as list in reverse chronological order.
     * The range offset and list arguments enable paging through query results.
     * @param website    Weblog or null to get for all weblogs.
     * @param user       User or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param sortBy     Sort by either 'pubTime' or 'updateTime' (null for pubTime)
     * @param offset     Offset into results for paging
     * @param range     Max comments to return (or -1 for no limit)
     * @return List of WeblogEntryData objects in reverse chrono order.
     * @throws RollerException
     */
    public List getWeblogEntries(WebsiteData website,  UserData user,
                                 Date startDate, Date endDate,
                                 String catName, List tags,
                                 String status, String sortBy, String locale,             
                                 int offset, int range) throws RollerException {
        return null; // TODO not implemented
    }

    /**
     * Get specified number of most recent pinned and published Weblog Entries.
     * @param max Maximum number to return.
     * @return Collection of WeblogEntryData objects.
     */
    public List getWeblogEntriesPinnedToMain(Integer max)
            throws RollerException {
        DatamapperQuery query = strategy.newQuery(WeblogCategoryData.class, 
                "WeblogEntryData.getByPinnedToMainOrderByPubTime");
        if (max != null) {
            query.setRange(0, max.intValue());
        }
        return (List) query.execute(Boolean.TRUE);   
    }

    /**
     * Get weblog entry by anchor.
     */
    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website,
            String anchor) throws RollerException {
        if (website == null)
            throw new RollerException("Website is null");
        
        if (anchor == null)
            throw new RollerException("Anchor is null");
        
        // TODO Impl entryAnchorToIdMap CACHE
        
        return (WeblogEntryData)strategy.newQuery(WeblogEntryData.class, 
            "getByAnchor")
            .execute(anchor);
    }

    /**
     * Gets returns most recent pubTime, optionally restricted by category.
     * @param website Handle of website or null for all users
     * @param catName Category name of posts or null for all categories
     * @return Date Of last publish time
     */
    public Date getWeblogLastPublishTime(WebsiteData website, String catName)
            throws RollerException {
        WeblogCategoryData cat = null;

        if (catName != null && website != null) {
            cat = getWeblogCategoryByPath(website, null, catName);
        }
    
        List list = null;
        if (website != null) {
            if (cat != null) {
                list = (List) strategy.newQuery(WeblogEntryData.class, 
                    "WeblogEntryData.getByStatus&PubTimeLessEqual&Category&WebsiteOrderByPubTimeDesc")
                    .setRange(0, 1)
                    .execute(
                        new Object[] {WeblogEntryData.PUBLISHED, new Date(), 
                            cat, website});
            } else {
                list = (List) strategy.newQuery(WeblogEntryData.class, 
                        "WeblogEntryData.getByStatus&PubTimeLessEqual&WebsiteOrderByPubTimeDesc")
                    .setRange(0, 1)
                    .execute(
                        new Object[] {WeblogEntryData.PUBLISHED, new Date(), 
                        website});
            }
        } else {
            // cat must also be null
            list = (List) strategy.newQuery(WeblogEntryData.class, 
                    "WeblogEntryData.getByStatus&PubTimeLessEqualOrderByPubTimeDesc")
                .setRange(0, 1)
                .execute(new Object[] {WeblogEntryData.PUBLISHED, new Date()});
        }
        if (list.size() > 0) {
            return ((WeblogEntryData)list.get(0)).getPubTime();
        } else {
            return null;
        }
    }

    /**
     * Get weblog entries with given category or, optionally, any sub-category
     * of that category.
     * @param cat Category
     * @param subcats True if sub-categories are to be fetched
     * @return List of weblog entries in category
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
                "WeblogEntryData.getByCategory.pathLike&amp;Website")
                .execute(new Object[] {cat, cat.getPath(), cat.getWebsite()});  
        }
        
        return results;
    }

    /**
     * Create unique anchor for weblog entry.
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
                "WeblogEntryData.getByWeblogEntry&Anchor")
                .execute(new Object[] {entry, name});
                
            if (results.size() < 1) {
                break;
            } else {
                count++;
            }
        }
        return name;
    }

    /**
     * Check for duplicate category name.
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
     * Check if weblog category is in use.
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
     * Generic comments query method.
     * @param website Website or null for all comments on site
     * @param entry Entry or null to include all comments
     * @param startDate Start date or null for no restriction
     * @param endDate End date or null for no restriction
     * @param pending Pending flag value or null for no restriction
     * @param pending Approved flag value or null for no restriction
     * @param reverseChrono True for results in reverse chrono order
     * @param spam Spam flag value or null for no restriction
     * @param offset Offset into results for paging
     * @param length Max comments to return (or -1 for no limit)
     */
    public List getComments(WebsiteData website, WeblogEntryData entry,
            String searchString, Date startDate,
            Date endDate, Boolean pending,
            Boolean approved, Boolean spam,
            boolean reverseChrono, int offset,
            int length) throws RollerException {
        return null;  // TODO not implemented
    }

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
        // TODO use Bulk Delete
    }
    
    /**
     * Get category by id.
     */
    public WeblogCategoryData getWeblogCategory(String id)
            throws RollerException {
        return (WeblogCategoryData) this.strategy.load(
                WeblogCategoryData.class, id);
    }

    /**
     * Get category specified by website and categoryPath.
     * @param website Website of WeblogCategory.
     * @param categoryPath Path of WeblogCategory, relative to category root.
     */
    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website,
            String categoryPath) throws RollerException {
        return getWeblogCategoryByPath(website, null, categoryPath);
    }

    /**
     * Get absolute path to category, appropriate for use by
     * getWeblogCategoryByPath().
     * @param category WeblogCategoryData.
     * @return Forward slash separated path string.
     */
    public String getPath(WeblogCategoryData category) throws RollerException {
        if (null == category.getParent()) {
            return "/";
        } else {
            String parentPath = getPath(category.getParent());
            parentPath = "/".equals(parentPath) ? "" : parentPath;
            return parentPath + "/" + category.getName();
        }
    }

    /**
     * Get sub-category by path relative to specified category.
     * @param category Root of path or null to start at top of category tree.
     * @param path Path of category to be located.
     * @param website Website of categories.
     * @return Category specified by path or null if not found.
     */
    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website,
            WeblogCategoryData category, String path) throws RollerException {
        final Iterator cats;
        final String[] pathArray = Utilities.stringToStringArray(path, "/");
        
        if (category == null && (null == path || "".equals(path.trim()))) {
            throw new RollerException("Bad arguments.");
        }
        
        if (path.trim().equals("/")) {
            return getRootWeblogCategory(website);
        } else if (category == null || path.trim().startsWith("/")) {
            cats = getRootWeblogCategory(website)
            .getWeblogCategories().iterator();
        } else {
            cats = category.getWeblogCategories().iterator();
        }
        
        while (cats.hasNext()) {
            WeblogCategoryData possibleMatch = (WeblogCategoryData)cats.next();
            if (possibleMatch.getName().equals(pathArray[0])) {
                if (pathArray.length == 1) {
                    return possibleMatch;
                } else {
                    String[] subpath = new String[pathArray.length - 1];
                    System.arraycopy(pathArray, 1, subpath, 0, subpath.length);
                    
                    String pathString= Utilities.stringArrayToString(subpath,"/");
                    return getWeblogCategoryByPath(website, possibleMatch, pathString);
                }
            }
        }
        
        // The category did not match and neither did any sub-categories
        return null;
    }

    /**
     * Get comment by id.
     */
    public CommentData getComment(String id) throws RollerException {
        return (CommentData) this.strategy.load(CommentData.class, id);
    }

    /**
     * Get weblog entry by id.
     */
    public WeblogEntryData getWeblogEntry(String id) throws RollerException {
        return (WeblogEntryData)strategy.load(WeblogEntryData.class, id);
    }

    /**
     * Get time of last update for a weblog specified by username
     */
    public Date getWeblogLastPublishTime(WebsiteData website)
            throws RollerException {
        return getWeblogLastPublishTime(website, null);
    }

    /**
     * Get Weblog Entries grouped by day. This method returns a Map that
     * contains Lists, each List contains WeblogEntryData objects, and the Lists
     * are keyed by Date objects.
     * @param website Weblog or null to get for all weblogs.
     * @param startDate Start date or null for no start date.
     * @param endDate End date or null for no end date.
     * @param catName Category path or null for all categories.
     * @param status Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param offset Offset into results for paging
     * @param range Max comments to return (or -1 for no limit)
     * @return Map of Lists, keyed by Date, and containing WeblogEntryData.
     * @throws org.apache.roller.RollerException
     */
    public Map getWeblogEntryObjectMap(WebsiteData website,
            Date startDate, Date endDate, String catName, List tags,
            String status, String locale, int offset,
            int range) 
                throws RollerException {
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
            range);
    }

    /**
     * Get Weblog Entry date strings grouped by day. This method returns a Map
     * that contains Lists, each List contains YYYYMMDD date strings objects,
     * and the Lists are keyed by Date objects.
     * @param website Weblog or null to get for all weblogs.
     * @param startDate Start date or null for no start date.
     * @param endDate End date or null for no end date.
     * @param catName Category path or null for all categories.
     * @param status Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param offset Offset into results for paging
     * @param range Max comments to return (or -1 for no limit)
     * @return Map of Lists, keyed by Date, and containing date strings.
     * @throws org.apache.roller.RollerException
     */
    public Map getWeblogEntryStringMap(WebsiteData website,
            Date startDate, Date endDate, String catName, List tags,
            String status, String locale, int offset,
            int range) throws RollerException {
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
            range);
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
     * Get weblog enties ordered by descending number of comments.
     * @param website Weblog or null to get for all weblogs.
     * @param startDate Start date or null for no start date.
     * @param endDate End date or null for no end date.
     * @param offset Offset into results for paging
     * @param length Max comments to return (or -1 for no limit)
     * @return List of WeblogEntryData objects.
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
                (String)row[1],
                (String)row[2],
                (String)row[3],
                "statCount.weblogEntryCommentCountType",
                new Long(((Integer)row[0]).intValue()).longValue()));
        }
        //TODO Uncomment following once integrated with code
        //Collections.sort(results, StatCount.getComparator());
        Collections.reverse(results);
        return results;
    }

    /**
     * Get entries next after current entry.
     * @param current Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getNextEntries(WeblogEntryData current, String catName,
            String locale, int maxEntries) throws RollerException {

        return getNextPrevEntries(current, catName, locale, maxEntries, true);
    }

    /**
     * Get entries previous to current entry.
     * @param current Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getPreviousEntries(WeblogEntryData current, String catName,
            String locale, int maxEntries) throws RollerException {

        return getNextPrevEntries(current, catName, locale, maxEntries, false);
    }

    /**
     * Get the WeblogEntry following, chronologically, the current entry.
     * Restrict by the Category, if named.
     * @param current The "current" WeblogEntryData
     * @param catName The value of the requested Category Name
     */
    public WeblogEntryData getNextEntry(WeblogEntryData current,
            String catName, String locale) throws RollerException {
        WeblogEntryData entry = null;
        List entryList = getNextEntries(current, catName, locale, 1);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }

    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     * Restrict by the Category, if named.
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     */
    public WeblogEntryData getPreviousEntry(WeblogEntryData current,
            String catName, String locale) throws RollerException {
        WeblogEntryData entry = null;
        List entryList = getPreviousEntries(current, catName, locale, 1);
        if (entryList != null && entryList.size() > 0) {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }

    /**
     * Release all resources held by manager.
     */
    public void release() {}

    /**
     * Apply comment default settings from website to all of website's entries.
     */
    public void applyCommentDefaultsToEntries(WebsiteData website)
            throws RollerException {
        if (log.isDebugEnabled()) {
            log.debug("applyCommentDefaults");
        }       
//        String updateString = "update WeblogEntryData set "
//            +"allowComments=:allowed, commentDays=:days, "
//            +"pubTime=pubTime, updateTime=updateTime " // ensure timestamps are NOT reset
//            +"where website=:site";
//        Query update = session.createQuery(updateString);
//        update.setParameter("allowed", website.getDefaultAllowComments());
//        update.setParameter("days", new Integer(website.getDefaultCommentDays()));
//        update.setParameter("site", website);
//        update.executeUpdate();            
        // TODO not implemented
    }

    /* (non-Javadoc)
     * @see org.apache.roller.business.WeblogManager#getPopularTags(org.apache.roller.pojos.WebsiteData, java.util.Date, int)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.roller.business.WeblogManager#getTags(org.apache.roller.pojos.WebsiteData,
     *      java.lang.String, java.lang.String, int)
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
                        new Object[] {website, startsWith});
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsite&NameStartsWith");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(
                        new Object[] {website, startsWith});
                }
            } else {
                if (sortByName) {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteOrderByName");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(startsWith);   
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsite");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(startsWith);   
                }
            }
        } else {
            if (startsWith != null) {
                if (sortByName) {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteNull&NameStartsWithOrderByName");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(startsWith);
                } else {
                    query = strategy.newQuery(WeblogEntryTagAggregateData.class,
                        "WeblogEntryTagAggregateData.getTagsByWebsiteNull&NameStartsWith");
                    if (limit > 0) query.setRange(0, limit);
                    queryResults = (List) query.execute(startsWith);
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
            ce.setCount(((Integer) row[1]).intValue());
            results.add(ce);
        }
        
        if (!sortByName) {
            Collections.sort(results, tagStatComparator);
        }
        
        return results;
    }

    public boolean getTagComboExists(List tags, WebsiteData weblog) {
        boolean comboExists = false;
        
//        StringBuffer queryString = new StringBuffer();
//        queryString.append("select distinct name ");
//        queryString.append("from WeblogEntryTagAggregateData ");
//        queryString.append("where name in ( :tags ) ");
//            
//        // are we checking a specific weblog, or site-wide?
//        if (weblog != null)
//            queryString.append("and weblog.id = '" + weblog.getId() + "' ");
//        else
//            queryString.append("and weblog is null ");
//            
//        Query query = session.createQuery(queryString.toString());
//        query.setParameterList("tags", tags);
//            
//        List results = query.list();
//        comboExists = (results != null && results.size() == tags.size());
        
        return comboExists; // TODO not implemented
    }
    
    public void updateTagCount(String name, WebsiteData website, int amount) 
            throws RollerException {
        if(amount == 0) {
            throw new RollerException("Tag increment amount cannot be zero.");
        }
        
        if(website == null) {
            throw new RollerException("Website cannot be NULL.");
        }

//        Junction conjunction = Expression.conjunction();
//        conjunction.add(Expression.eq("name", name));
//        conjunction.add(Expression.eq("weblog", website));
//
//        // The reason why add order lastUsed desc is to make sure we keep picking the most recent
//        // one in the case where we have multiple rows (clustered environment)
//        // eventually that second entry will have a very low total (most likely 1) and
//        // won't matter
//        
//        Criteria criteria = session.createCriteria(WeblogEntryTagAggregateData.class)
//            .add(conjunction).addOrder(Order.desc("lastUsed")).setMaxResults(1);
//        
//        WeblogEntryTagAggregateData weblogTagData = (WeblogEntryTagAggregateData) criteria.uniqueResult();
//
//        conjunction = Expression.conjunction();
//        conjunction.add(Restrictions.eq("name", name));
//        conjunction.add(Restrictions.isNull("weblog"));
//        
//        criteria = session.createCriteria(WeblogEntryTagAggregateData.class)
//            .add(conjunction).addOrder(Order.desc("lastUsed")).setMaxResults(1);
//    
//        WeblogEntryTagAggregateData siteTagData = (WeblogEntryTagAggregateData) criteria.uniqueResult();
//        
//        Timestamp lastUsed = new Timestamp((new Date()).getTime());
//        
//        // create it only if we are going to need it.
//        if(weblogTagData == null && amount > 0) {
//            weblogTagData = new WeblogEntryTagAggregateData(null, website, name, amount);
//            weblogTagData.setLastUsed(lastUsed);
//            session.save(weblogTagData);
//        } else if(weblogTagData != null) {
//            session.createQuery("update WeblogEntryTagAggregateData set total = total + ?, lastUsed = current_timestamp() where name = ? and weblog = ?")
//            .setInteger(0, amount)
//            .setString(1, weblogTagData.getName())
//            .setParameter(2, website)
//            .executeUpdate();
//        }
//        
//        // create it only if we are going to need it.        
//        if(siteTagData == null && amount > 0) {
//            siteTagData = new WeblogEntryTagAggregateData(null, null, name, amount);
//            siteTagData.setLastUsed(lastUsed);
//            session.save(siteTagData);
//        } else if(siteTagData != null) {
//            session.createQuery("update WeblogEntryTagAggregateData set total = total + ?, lastUsed = current_timestamp() where name = ? and weblog is null")
//            .setInteger(0, amount)
//            .setString(1, siteTagData.getName())
//            .executeUpdate();            
//        }       
        
        // delete all bad counts
        strategy.newRemoveQuery(WeblogEntryTagAggregateData.class, "WeblogEntryTagAggregateData.removeByTotalLessEqual").removeAll(new Integer(0));
        // TODO not implemented
    }


    public HitCountData getHitCount(String id) throws RollerException {

        if(id == null) {
            throw new RollerException("Id field cannot be NULL.");
        }

        return (HitCountData) strategy.load(HitCountData.class, id);
    }
    
    public HitCountData getHitCountByWeblog(WebsiteData weblog) 
        throws RollerException {
        
        return (HitCountData) strategy.newQuery(HitCountData.class, 
                "HitCountData.getByWeblog").setUnique().execute(weblog);
    }
    
    
    public List getHotWeblogs(int sinceDays, int offset, int length) 
        throws RollerException {
        
        // figure out start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        
        DatamapperQuery query = strategy.newQuery(HitCountData.class, 
                "HitCountData.getByWeblogEnabledTrueAndActiveTrue&DailyHitsGreaterZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc");

        if (length != -1) {
            query.setRange(offset, length);
        }
        
        return (List) query.execute(startDate);
    }
    
    
    public void saveHitCount(HitCountData hitCount) throws RollerException {
        this.strategy.store(hitCount);
    }
    
    
    public void removeHitCount(HitCountData hitCount) throws RollerException {
        this.strategy.remove(hitCount);
    }
    
    
    public void incrementHitCount(WebsiteData weblog, int amount)
        throws RollerException {

        if(amount == 0) {
            throw new RollerException("Tag increment amount cannot be zero.");
        }
        
        if(weblog == null) {
            throw new RollerException("Weblog cannot be NULL.");
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
    
    
    public void resetAllHitCounts() throws RollerException {
        
//            session.createQuery("update HitCountData set dailyHits = 0").executeUpdate();

        // TODO not implemented
    }
    
    
    public void resetHitCount(WebsiteData weblog) throws RollerException {

        HitCountData hitCount = (HitCountData) strategy.newQuery(HitCountData.class, 
                "HitCountData.getByWeblog").setUnique().execute(weblog);

        hitCount.setDailyHits(0);
        strategy.store(hitCount);
    }

    /**
     * Get site-wide comment count 
     */
    public long getCommentCount() throws RollerException {
        List results = (List) strategy.newQuery(CommentData.class, 
            "CommentData.getCountAllDistinct").execute();
        
        return ((Integer)results.get(0)).intValue();
    }


    /**
     * Get weblog comment count 
     */
    public long getCommentCount(WebsiteData website) throws RollerException {
        List results = (List) strategy.newQuery(CommentData.class, 
            "CommentData.getCountDistinctByWebsite").execute(website);
        
        return ((Integer)results.get(0)).intValue();
    }


    /**
     * Get site-wide entry count 
     */
    public long getEntryCount() throws RollerException {
        List results = (List) strategy.newQuery(WeblogEntryData.class, 
            "WeblogEntryData.getCountDistinctByStatus")
            .execute("PUBLISHED");
        
        return ((Integer)results.get(0)).intValue();
    }


    /**
     * Get weblog entry count 
     */
    public long getEntryCount(WebsiteData website) throws RollerException {
        List results = (List) strategy.newQuery(WeblogEntryData.class, 
            "WeblogEntryData.getCountDistinctByStatus&Website")
            .execute(new Object[] {"PUBLISHED", website});
        
        return ((Integer)results.get(0)).intValue();
    }
    
}
