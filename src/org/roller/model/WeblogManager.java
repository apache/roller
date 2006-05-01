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

package org.roller.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.roller.RollerException;
import org.roller.pojos.Assoc;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogCategoryAssoc;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;


/**
 * Interface to weblog entry, category and comment management.
 */
public interface WeblogManager {
    
    public static final String CATEGORY_ATT = "category.att";
       
    public void saveWeblogEntry(WeblogEntryData entry) throws RollerException;
        
    public void removeWeblogEntry(WeblogEntryData entry) throws RollerException;
    
    
    /**
     * Get weblog entry by ID
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
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param sortby     Sort by either 'pubTime' or 'updateTime' (null for pubTime)
     * @param offset     Index of first entry to include.
     * @param length     Max number of entries to include.
     * @return List of WeblogEntryData objects in reverse chrono order.
     * @throws RollerException
     */
    public List getWeblogEntries(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            String  sortBy,
            int offset,
            int range)
            throws RollerException;
    
    
    /**
     * Get WeblogEntries up to limit as list in reverse chronological order.
     * The range offset and list arguments enable paging through query results.
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param sortby     Sort by either 'pubTime' or 'updateTime' (null for pubTime)
     * @param offset     Index of first entry to include.
     * @param maxEntries     Max number of entries to include.
     * @return List of WeblogEntryData objects in reverse chrono order.
     * @throws RollerException
     */
    public List getWeblogEntries(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            String  sortBy,
            Integer maxEntries)
            throws RollerException;
    
    
    /**
     * Get Weblog Entries grouped by day. This method returns a Map that
     * contains Lists, each List contains WeblogEntryData objects, and the
     * Lists are keyed by Date objects.
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param maxEntries Max entries or null for no limit.
     * @return Map of Lists, keyed by Date, and containing WeblogEntryData.
     * @throws RollerException
     */
    public Map getWeblogEntryObjectMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            Integer maxEntries)
            throws RollerException;
    
    
    /**
     * Get Weblog Entry date strings grouped by day. This method returns a Map
     * that contains Lists, each List contains YYYYMMDD date strings objects,
     * and the Lists are keyed by Date objects.
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param maxEntries Max entries or null for no limit.
     * @return Map of Lists, keyed by Date, and containing date strings.
     * @throws RollerException
     */
    public Map getWeblogEntryStringMap(
            WebsiteData website,
            Date    startDate,
            Date    endDate,
            String  catName,
            String  status,
            Integer maxEntries)
            throws RollerException;
    
    
    /**
     * Get weblog entries with given category or, optionally, any sub-category
     * of that category.
     * @param cat     Category.
     * @param subcats True if sub-categories are to be fetched.
     * @return        List of weblog entries in category.
     */
    public List getWeblogEntries(WeblogCategoryData cat, boolean subcats) 
            throws RollerException;
    
    
    /**
     * Get the WeblogEntry following, chronologically, the current entry.
     * Restrict by the Category, if named.
     *
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     * @return
     */
    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName) 
            throws RollerException;
    
    
    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     * Restrict by the Category, if named.
     *
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     * @return
     */
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, 
            String catName) throws RollerException;
    
    
    /**
     * Get entries next after current entry.
     * @param entry Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getNextEntries(WeblogEntryData entry, 
            String catName, int maxEntries) throws RollerException;
    
    
    /**
     * Get entries previous to current entry.
     * @param entry Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getPreviousEntries(WeblogEntryData entry, 
            String catName, int maxEntries) throws RollerException;
    
    
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
     * @throws RollerException
     */
    public Date getWeblogLastPublishTime(WebsiteData website, String catName )
            throws RollerException;
    
    
    public void saveWeblogCategory(WeblogCategoryData cat) throws RollerException;
    
    
    /**
     * Recategorize all entries with one category to another.
     * @param srcId
     * @param destId
     * @throws org.roller.RollerException
     */
    public void moveWeblogCategoryContents(String srcId, String destId) 
            throws RollerException;
    
    
    public void removeWeblogCategory(WeblogCategoryData cat) throws RollerException;
    
    
    /**
     * Get category by ID
     */
    public WeblogCategoryData getWeblogCategory(String id) throws RollerException;
    
    
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
    
    
    /** Get WebLogCategory objects for a website. */
    public List getWeblogCategories(WebsiteData website) throws RollerException;
    
    
    /** Get WebLogCategory objects for a website. */
    public List getWeblogCategories(WebsiteData website, boolean includeRoot)
            throws RollerException;
    
    
    /**
     * Get absolute path to category, appropriate for use by getWeblogCategoryByPath().
     * @param category WeblogCategoryData.
     * @return         Forward slash separated path string.
     */
    public String getPath(WeblogCategoryData category) throws RollerException;
    
    
    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData data) throws RollerException;
    
    public List getWeblogCategoryChildAssocs(WeblogCategoryData data) throws RollerException;
    
    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData data) throws RollerException;
    
    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData data) throws RollerException;
    
    
    public void saveComment(CommentData comment) throws RollerException;
    
    
    public void removeComment(CommentData comment) throws RollerException;
    
    
    /**
     * Get comment by ID
     */
    public CommentData getComment(String id) throws RollerException;
    
    
    /**
     * Generic comments query method
     * @param website    Website or null for all comments on site
     * @param entry      Entry or null to include all comments
     * @param startDate  Start date or null for no restriction
     * @param endDate    End date or null for no restriction
     * @param pending    Pending flag value or null for no restriction
     * @param pending    Approved flag value or null for no restriction
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
     * Get the URL of a website.
     * @param website    The website
     * @param contextUrl The context url, this is prepended and can be absolute
     *                   or relative depending on what is desired.
     * @return The url of the user's weblog
     */
    public String getUrl(WebsiteData website, String contextUrl) throws RollerException;
    
    /**
     * Apply comment default settings from website to all of website's entries.
     */
    public void applyCommentDefaultsToEntries(WebsiteData website) throws RollerException;
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
