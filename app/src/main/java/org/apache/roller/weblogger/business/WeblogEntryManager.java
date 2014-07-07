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
 */

package org.apache.roller.weblogger.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogHitCount;


/**
 * Interface to weblog entry, category and comment management.
 */
public interface WeblogEntryManager {

    /**
     * Save weblog entry.
     */
    void saveWeblogEntry(WeblogEntry entry) throws WebloggerException;
       
    /**
     * Remove weblog entry.
     */
    void removeWeblogEntry(WeblogEntry entry) throws WebloggerException;
    
    /**
     * Get weblog entry by id.
     */
    WeblogEntry getWeblogEntry(String id) throws WebloggerException;
    
    /** 
     * Get weblog entry by anchor. 
     */
    WeblogEntry getWeblogEntryByAnchor(Weblog website, String anchor)
            throws WebloggerException;
        
    /**
     * Get WeblogEntries by offset/length as list in reverse chronological order.
     * The range offset and list arguments enable paging through query results.
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return List of WeblogEntry objects in order specified by search criteria
     * @throws WebloggerException
     */
    List<WeblogEntry> getWeblogEntries(WeblogEntrySearchCriteria wesc)
            throws WebloggerException;

    /**
     * Get Weblog Entries grouped by day.
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return Map of Lists of WeblogEntries keyed by calendar day
     * @throws WebloggerException
     */
    Map<Date, List<WeblogEntry>> getWeblogEntryObjectMap(WeblogEntrySearchCriteria wesc)
            throws WebloggerException;

    /**
     * Get Weblog Entry date strings grouped by day. This method returns a Map
     * that contains one YYYYMMDD date string object for each calendar day having
     * one or more blog entries.
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return Map of date strings keyed by Date
     * @throws WebloggerException
     */
    Map<Date, String> getWeblogEntryStringMap(WeblogEntrySearchCriteria wesc)
            throws WebloggerException;
    
    /**
     * Get weblog entries ordered by descending number of comments.
     * @param website    Weblog or null to get for all weblogs.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param offset     Offset into results for paging
     * @param length     Max comments to return (or -1 for no limit)
     * @return List of StatCount objects.
     */
    List<StatCount> getMostCommentedWeblogEntries(
            Weblog website,             
            Date        startDate,
            Date        endDate,
            int         offset, 
            int         length)
            throws WebloggerException;
    
    /**
     * Get the WeblogEntry following, chronologically, the current entry.
     * Restrict by the Category, if named.
     * @param current The "current" WeblogEntryData
     * @param catName The value of the requested Category Name
     */
    WeblogEntry getNextEntry(WeblogEntry current,
            String catName, String locale) throws WebloggerException;    
    
    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     * Restrict by the Category, if named.
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     */
    WeblogEntry getPreviousEntry(WeblogEntry current,
            String catName, String locale) throws WebloggerException;
      
    
    /**
     * Get specified number of most recent pinned and published Weblog Entries.
     * @param max Maximum number to return.
     * @return Collection of WeblogEntry objects.
     */
    List<WeblogEntry> getWeblogEntriesPinnedToMain(Integer max) throws WebloggerException;

    /**
     * Remove attribute with given name from given WeblogEntryData
     * @param name Name of attribute to be removed
     */
    void removeWeblogEntryAttribute(String name,WeblogEntry entry)
            throws WebloggerException;

    /**
     * Remove tag with given name from given WeblogEntryData
     * @param name Name of tag to be removed
     */
    void removeWeblogEntryTag(String name,WeblogEntry entry)
            throws WebloggerException;

    /**
     * Save weblog category.
     */
    void saveWeblogCategory(WeblogCategory cat) throws WebloggerException;
    
    /**
     * Remove weblog category.
     */
    void removeWeblogCategory(WeblogCategory cat) throws WebloggerException;
        
    /**
     * Get category by id.
     */
    WeblogCategory getWeblogCategory(String id) throws WebloggerException;
    
    
    /**
     * Recategorize all entries with one category to another.
     */
    void moveWeblogCategoryContents(WeblogCategory srcCat, WeblogCategory destCat)
            throws WebloggerException;
    
    /**
     * Get category specified by website and name.
     * @param website      Website of WeblogCategory.
     * @param categoryName Name of WeblogCategory
     */
    WeblogCategory getWeblogCategoryByName(Weblog website,
            String categoryName) throws WebloggerException;

    /**
     * Get WebLogCategory objects for a website. 
     */
    List<WeblogCategory> getWeblogCategories(Weblog website)
            throws WebloggerException;

    /**
     * Save comment.
     */
    void saveComment(WeblogEntryComment comment) throws WebloggerException;
    
    /**
     * Remove comment.
     */
    void removeComment(WeblogEntryComment comment) throws WebloggerException;
   
    /**
     * Get comment by id.
     */
    WeblogEntryComment getComment(String id) throws WebloggerException;
       
    /**
     * Generic comments query method.
     * @param csc CommentSearchCriteria object with fields indicating search criteria
     * @return list of comments fitting search criteria
     */
    List<WeblogEntryComment> getComments(CommentSearchCriteria csc) throws WebloggerException;

    /**
     * Deletes comments that match paramters.
     * @param website    Website or null for all comments on site
     * @param entry      Entry or null to include all comments
     * @param startDate  Start date or null for no restriction
     * @param endDate    End date or null for no restriction
     * @param status     Status of comment
     * @return Number of comments deleted
     */
    int removeMatchingComments(
            
            Weblog          website,
            WeblogEntry     entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            ApprovalStatus  status
            
            ) throws WebloggerException;
        
    /**
     * Create unique anchor for weblog entry.
     */
    String createAnchor(WeblogEntry data) throws WebloggerException;
    
    /**
     * Check for duplicate category name.
     */
    boolean isDuplicateWeblogCategoryName(WeblogCategory data)
            throws WebloggerException;  
    
    /**
     * Check if weblog category is in use.
     */
    boolean isWeblogCategoryInUse(WeblogCategory data)
            throws WebloggerException;    
    
    
    /**
     * Apply comment default settings from website to all of website's entries.
     */
    void applyCommentDefaultsToEntries(Weblog website)
        throws WebloggerException;
    
    /**
     * Release all resources held by manager.
     */
    void release();
    
    /**
     * Get list of TagStat. There's no offset/length params just a limit.
     * @param website       Weblog or null to get for all weblogs.
     * @param startDate     Date or null of the most recent time a tag was used.
     * @param limit         Max TagStats to return (or -1 for no limit)
     * @return List of most popular tags.
     * @throws WebloggerException
     */
    List<TagStat> getPopularTags(Weblog website, Date startDate, int offset, int limit)
            throws WebloggerException;
    
    /**
     * Get list of TagStat. There's no offset/length params just a limit.
     * @param website       Weblog or null to get for all weblogs.
     * @param sortBy        Sort by either 'name' or 'count' (null for name) 
     * @param startsWith    Prefix for tags to be returned (null or a string of length > 0)
     * @param limit         Max TagStats to return (or -1 for no limit)
     * @return List of tags matching the criteria.
     * @throws WebloggerException
     */
    List<TagStat> getTags(Weblog website, String sortBy, String startsWith, int offset, int limit)
            throws WebloggerException;    
    
    /**
     * Does the specified tag combination exist?  Optionally confined to a specific weblog.
     *
     * This tests if the intersection of the tags listed will yield any results
     * and returns a true/false value if so.  This means that if the tags list
     * is "foo", "bar" and only the tag "foo" has been used then this method
     * should return false.
     *
     * @param tags The List of tags to check for.
     * @param weblog The weblog to confine the check to.
     * @return True if tags exist, false otherwise.
     * @throws WebloggerException If there is any problem doing the operation.
     */
    boolean getTagComboExists(List tags, Weblog weblog)
        throws WebloggerException;
    
    /**
     * This method maintains the tag aggregate table up-to-date with total counts. More
     * specifically every time this method is called it will act upon exactly two rows
     * in the database (tag,website,count), one with website matching the argument passed
     * and one where website is null. If the count ever reaches zero, the row must be deleted.
     * 
     * @param name      The tag name
     * @param website   The website to used when updating the stats.
     * @param amount    The amount to increment the tag count (it can be positive or negative).
     * @throws WebloggerException
     */
    void updateTagCount(String name, Weblog website, int amount)
        throws WebloggerException;
    
    
    /**
     * Get a HitCountData by id.
     *
     * @param id The HitCountData id.
     * @return The HitCountData object, or null if it wasn't found.
     * @throws WebloggerException If there was a problem with the backend.
     */
    WeblogHitCount getHitCount(String id) throws WebloggerException;
    
    
    /**
     * Get a HitCountData by weblog.
     *
     * @param weblog The WebsiteData that you want the hit count for.
     * @return The HitCountData object, or null if it wasn't found.
     * @throws WebloggerException If there was a problem with the backend.
     */
    WeblogHitCount getHitCountByWeblog(Weblog weblog)
        throws WebloggerException;
    
    
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
     * @throws WebloggerException If there was a problem with the backend.
     */
    List<WeblogHitCount> getHotWeblogs(int sinceDays, int offset, int length)
        throws WebloggerException;
    
    
    /**
     * Save a HitCountData object.
     *
     * @param hitCount The HitCountData object to save.
     * @throws WebloggerException If there was a problem with the backend.
     */
    void saveHitCount(WeblogHitCount hitCount) throws WebloggerException;
    
    
    /**
     * Remove a HitCountData object.
     *
     * @param hitCount The HitCountData object to remove.
     * @throws WebloggerException If there was a problem with the backend.
     */
    void removeHitCount(WeblogHitCount hitCount) throws WebloggerException;
    
    
    /**
     * Increment the hit count for a weblog by a certain amount.
     *
     * This is basically a convenience method for doing a lookup, modify, save
     * of a HitCountData object.
     *
     * @param weblog The WebsiteData object to increment the count for.
     * @param amount How much to increment by.
     * @throws WebloggerException If there was a problem with the backend.
     */
    void incrementHitCount(Weblog weblog, int amount)
        throws WebloggerException;
    
    
    /**
     * Reset the hit counts for all weblogs.  This sets the counts back to 0.
     *
     * @throws WebloggerException If there was a problem with the backend.
     */
    void resetAllHitCounts() throws WebloggerException;
    
    
    /**
     * Reset the hit counts for a single weblog.  This sets the count to 0.
     *
     * @param weblog The WebsiteData object to reset the count for.
     * @throws WebloggerException If there was a problem with the backend.
     */
    void resetHitCount(Weblog weblog) throws WebloggerException;

    
    /**
     * Get site-wide comment count 
     */
    long getCommentCount() throws WebloggerException;

    
    /**
     * Get weblog comment count 
     */    
    long getCommentCount(Weblog websiteData) throws WebloggerException;

    
    /**
     * Get site-wide entry count 
     */    
    long getEntryCount() throws WebloggerException;

    
    /**
     * Get weblog entry count 
     */    
    long getEntryCount(Weblog websiteData) throws WebloggerException;
    
}

