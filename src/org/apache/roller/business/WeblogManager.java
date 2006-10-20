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

package org.apache.roller.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.HitCountData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Interface to weblog entry, category and comment management.
 */
public interface WeblogManager {
    
    public static final String CATEGORY_ATT = "category.att";
       
    /**
     * Save weblog entry.
     */
    public void saveWeblogEntry(WeblogEntryData entry) throws RollerException;
       
    /**
     * Remove weblog entry.
     */
    public void removeWeblogEntry(WeblogEntryData entry) throws RollerException;    
    
    /**
     * Get weblog entry by id.
     */
    public WeblogEntryData getWeblogEntry(String id) throws RollerException;
    
    /** 
     * Get weblog entry by anchor. 
     */
    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website, String anchor) 
            throws RollerException;
        
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
     * @param length     Max comments to return (or -1 for no limit)
     * @return List of WeblogEntryData objects in reverse chrono order.
     * @throws RollerException
     */
    public List getWeblogEntries(
            WebsiteData website,
            UserData    user,
            Date        startDate,
            Date        endDate,
            String      catName,
            List        tags,
            String      status,
            String      sortBy,
            String      locale,             
            int         offset,
            int         range)
            throws RollerException;
       
    /**
     * Get Weblog Entries grouped by day. This method returns a Map that
     * contains Lists, each List contains WeblogEntryData objects, and the
     * Lists are keyed by Date objects.
     * @param website    Weblog or null to get for all weblogs.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param offset     Offset into results for paging
     * @param length     Max comments to return (or -1 for no limit)
     * @return Map of Lists, keyed by Date, and containing WeblogEntryData.
     * @throws RollerException
     */
    public Map getWeblogEntryObjectMap(
            WebsiteData website,
            Date        startDate,
            Date        endDate,
            String      catName,
            List        tags,            
            String      status,
            String      locale,
            int         offset,
            int         range)
            throws RollerException;
        
    /**
     * Get Weblog Entry date strings grouped by day. This method returns a Map
     * that contains Lists, each List contains YYYYMMDD date strings objects,
     * and the Lists are keyed by Date objects.
     * @param website    Weblog or null to get for all weblogs.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param offset     Offset into results for paging
     * @param length     Max comments to return (or -1 for no limit)
     * @return Map of Lists, keyed by Date, and containing date strings.
     * @throws RollerException
     */
    public Map getWeblogEntryStringMap(
            WebsiteData website,
            Date        startDate,
            Date        endDate,
            String      catName,
            List        tags,            
            String      status,
            String      locale,
            int         offset,
            int         range)
            throws RollerException;    
    
    /**
     * Get weblog entries with given category or, optionally, any sub-category
     * of that category.
     * @param cat     Category
     * @param subcats True if sub-categories are to be fetched
     * @return        List of weblog entries in category
     */
    public List getWeblogEntries(WeblogCategoryData cat, boolean subcats) 
            throws RollerException; 
    
    /** 
     * Get weblog enties ordered by descending number of comments.
     * @param website    Weblog or null to get for all weblogs.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param offset     Offset into results for paging
     * @param length     Max comments to return (or -1 for no limit)
     * @returns List of WeblogEntryData objects.
     */
    public List getMostCommentedWeblogEntries(
            WebsiteData website,             
            Date        startDate,
            Date        endDate,
            int         offset, 
            int         length)
            throws RollerException;
    
    /**
     * Get the WeblogEntry following, chronologically, the current entry.
     * Restrict by the Category, if named.
     * @param current The "current" WeblogEntryData
     * @param catName The value of the requested Category Name
     */
    public WeblogEntryData getNextEntry(WeblogEntryData current, 
            String catName, String locale) throws RollerException;    
    
    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     * Restrict by the Category, if named.
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     */
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, 
            String catName, String locale) throws RollerException;
        
    /**
     * Get entries next after current entry.
     * @param entry Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getNextEntries(WeblogEntryData entry, 
            String catName, String locale, int maxEntries) throws RollerException;
        
    /**
     * Get entries previous to current entry.
     * @param entry Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getPreviousEntries(WeblogEntryData entry, 
            String catName, String locale, int maxEntries) throws RollerException;    
    
    /**
     * Get specified number of most recent pinned and published Weblog Entries.
     * @param max Maximum number to return.
     * @return Collection of WeblogEntryData objects.
     */
    public List getWeblogEntriesPinnedToMain(Integer max) throws RollerException;    
    
    /** Get time of last update for a weblog specified by username */
    public Date getWeblogLastPublishTime(WebsiteData website) throws RollerException;   
    
    /**
     * Gets returns most recent pubTime, optionally restricted by category.
     * @param handle   Handle of website or null for all users
     * @param catName  Category name of posts or null for all categories
     * @return         Date Of last publish time
     */
    public Date getWeblogLastPublishTime(WebsiteData website, String catName )
            throws RollerException;
    
    /**
     * Save weblog category.
     */
    public void saveWeblogCategory(WeblogCategoryData cat) throws RollerException;
    
    /**
     * Remove weblog category.
     */
    public void removeWeblogCategory(WeblogCategoryData cat) throws RollerException;
        
    /**
     * Get category by id.
     */
    public WeblogCategoryData getWeblogCategory(String id) throws RollerException;
    
    /**
     * Recategorize all entries with one category to another.
     */
    public void moveWeblogCategoryContents(WeblogCategoryData srcCat, WeblogCategoryData destCat) 
            throws RollerException;
    
    /**
     * Get top level categories for a website.
     * @param website Website.
     */
    public WeblogCategoryData getRootWeblogCategory(WebsiteData website) throws RollerException;
    
    
    /**
     * Get category specified by website and categoryPath.
     * @param website      Website of WeblogCategory.
     * @param categoryPath Path of WeblogCategory, relative to category root.
     */
    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website, 
            String categoryPath) throws RollerException;
    
    /**
     * Get sub-category by path relative to specified category.
     * @param category  Root of path or null to start at top of category tree.
     * @param path      Path of category to be located.
     * @param website   Website of categories.
     * @return          Category specified by path or null if not found.
     */
    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData wd, 
            WeblogCategoryData category, String string) throws RollerException;    
    
    /** 
     * Get WebLogCategory objects for a website. 
     */
    public List getWeblogCategories(WebsiteData website) throws RollerException;    
    
    /** 
     * Get WebLogCategory objects for a website. 
     */
    public List getWeblogCategories(WebsiteData website, boolean includeRoot)
            throws RollerException;
        
    /**
     * Get absolute path to category, appropriate for use by getWeblogCategoryByPath().
     * @param category WeblogCategoryData.
     * @return         Forward slash separated path string.
     */
    public String getPath(WeblogCategoryData category) throws RollerException;
    
    /**
     * Get parent association for a category.
     */
    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData data) throws RollerException;
    
    /**
     * Get child associations for a category.
     */
    public List getWeblogCategoryChildAssocs(WeblogCategoryData data) throws RollerException;
    
    /** 
     * Get all descendent associations for a category.
     */
    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData data) throws RollerException;
    
    /**
     * Get all ancestor associates for a category.
     */
    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData data) throws RollerException;
               
    /**
     * Save comment.
     */
    public void saveComment(CommentData comment) throws RollerException;
    
    /**
     * Remove comment.
     */
    public void removeComment(CommentData comment) throws RollerException;
   
    /**
     * Get comment by id.
     */
    public CommentData getComment(String id) throws RollerException;
       
    /**
     * Generic comments query method.
     * @param website    Website or null for all comments on site
     * @param entry      Entry or null to include all comments
     * @param startDate  Start date or null for no restriction
     * @param endDate    End date or null for no restriction
     * @param pending    Pending flag value or null for no restriction
     * @param approved   Approved flag value or null for no restriction
     * @param reverseChrono True for results in reverse chrono order
     * @param spam       Spam flag value or null for no restriction
     * @param offset     Offset into results for paging
     * @param length     Max comments to return (or -1 for no limit)
     */
    public List getComments(
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
     * Deletes comments that match paramters.
     * @param website    Website or null for all comments on site
     * @param entry      Entry or null to include all comments
     * @param startDate  Start date or null for no restriction
     * @param endDate    End date or null for no restriction
     * @param approved   Pending flag value or null for no restriction
     * @param pending    Approved flag value or null for no restriction
     * @return Number of comments deleted
     */
    public int removeMatchingComments(
            WebsiteData     website,
            WeblogEntryData entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            Boolean         pending,
            Boolean         approved,
            Boolean         spam
            ) throws RollerException;
        
    /**
     * Create unique anchor for weblog entry.
     */
    public String createAnchor(WeblogEntryData data) throws RollerException;    
    
    /**
     * Check for duplicate category name.
     */
    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData data)
            throws RollerException;  
    
    /**
     * Check if weblog category is in use.
     */
    public boolean isWeblogCategoryInUse(WeblogCategoryData data)
            throws RollerException;    
    
    /**
     * Returns true if ancestor is truly an ancestor of child.
     */
    public boolean isDescendentOf(WeblogCategoryData child, 
            WeblogCategoryData ancestor) throws RollerException;
    
    /**
     * Apply comment default settings from website to all of website's entries.
     */
    public void applyCommentDefaultsToEntries(WebsiteData website) 
        throws RollerException;
    
    /**
     * Release all resources held by manager.
     */
    public void release();    
    
    /**
     * Get list of TagStat. There's no offset/length params just a limit.
     * @param website       Weblog or null to get for all weblogs.
     * @param startDate     Date or null of the most recent time a tag was used.
     * @param limit         Max TagStats to return (or -1 for no limit)
     * @return
     * @throws RollerException
     */
    public List getPopularTags(WebsiteData website, Date startDate, int limit)
            throws RollerException;
    
    /**
     * Get list of TagStat. There's no offset/length params just a limit.
     * @param website       Weblog or null to get for all weblogs.
     * @param sortBy        Sort by either 'name' or 'count' (null for name) 
     * @param startsWith    Prefix for tags to be returned (null or a string of length > 0)
     * @param limit         Max TagStats to return (or -1 for no limit)
     * @return
     * @throws RollerException
     */
    public List getTags(WebsiteData website, String sortBy, String startsWith, int limit)
            throws RollerException;    
    
    /**
     * This method maintains the tag aggregate table up-to-date with total counts. More
     * specifically every time this method is called it will act upon exactly two rows
     * in the database (tag,website,count), one with website matching the argument passed
     * and one where website is null. If the count ever reaches zero, the row must be deleted.
     * 
     * @param name      The tag name
     * @param website   The website to used when updating the stats.
     * @param amount    The amount to increment the tag count (it can be positive or negative).
     * @throws RollerException
     */
    public void updateTagCount(String name, WebsiteData website, int amount)
        throws RollerException;
    
    
    /**
     * Get a HitCountData by id.
     *
     * @param id The HitCountData id.
     * @return The HitCountData object, or null if it wasn't found.
     * @throws RollerException If there was a problem with the backend.
     */
    public HitCountData getHitCount(String id) throws RollerException;
    
    
    /**
     * Get a HitCountData by weblog.
     *
     * @param weblog The WebsiteData that you want the hit count for.
     * @return The HitCountData object, or null if it wasn't found.
     * @throws RollerException If there was a problem with the backend.
     */
    public HitCountData getHitCountByWeblog(WebsiteData weblog)
        throws RollerException;
    
    
    /**
     * Get HitCountData objects for the hotest weblogs.
     *
     * The results may be constrained to a certain number of days back from the
     * current time, as well as pagable via the offset and length params.
     *
     * The results are ordered by highest counts in descending order, and any
     * weblogs which are not active or enabled are not included.
     *
     * @param sinceDays Number of days in the past to consider.
     * @param offset What index in the results to begin from.
     * @param length The number of results to return.
     * @return The list of HitCountData objects ranked by hit count, descending.
     * @throws RollerException If there was a problem with the backend.
     */
    public List getHotWeblogs(int sinceDays, int offset, int length)
        throws RollerException;
    
    
    /**
     * Save a HitCountData object.
     *
     * @param hitCount The HitCountData object to save.
     * @throws RollerException If there was a problem with the backend.
     */
    public void saveHitCount(HitCountData hitCount) throws RollerException;
    
    
    /**
     * Remove a HitCountData object.
     *
     * @param hitCount The HitCountData object to remove.
     * @throws RollerException If there was a problem with the backend.
     */
    public void removeHitCount(HitCountData hitCount) throws RollerException;
    
    
    /**
     * Increment the hit count for a weblog by a certain amount.
     *
     * This is basically a convenience method for doing a lookup, modify, save
     * of a HitCountData object.
     *
     * @param weblog The WebsiteData object to increment the count for.
     * @param amount How much to increment by.
     * @throws RollerException If there was a problem with the backend.
     */
    public void incrementHitCount(WebsiteData weblog, int amount)
        throws RollerException;
    
    
    /**
     * Reset the hit counts for all weblogs.  This sets the counts back to 0.
     *
     * @throws RollerException If there was a problem with the backend.
     */
    public void resetAllHitCounts() throws RollerException;
    
    
    /**
     * Reset the hit counts for a single weblog.  This sets the count to 0.
     *
     * @param weblog The WebsiteData object to reset the count for.
     * @throws RollerException If there was a problem with the backend.
     */
    public void resetHitCount(WebsiteData weblog) throws RollerException;
    
}
