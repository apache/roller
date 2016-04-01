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
package org.apache.roller.weblogger.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.Template.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogTemplate;

/**
 * Interface to weblog and weblog custom template management.
 */
public interface WeblogManager {
    
    /**
     * Add new website, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new website.
     * @param newWebsite New website to be created, must have creator.
     */
    void addWeblog(Weblog newWebsite) throws WebloggerException;
    
    /**
     * Store a single weblog.
     */
    void saveWeblog(Weblog data) throws WebloggerException;

    /**
     * Remove website object.
     */
    void removeWeblog(Weblog website) throws WebloggerException;
    
    /**
     * Get website object by name.
     */
    Weblog getWeblog(String id) throws WebloggerException;
    
    
    /**
     * Get website specified by handle (or null if enabled website not found).
     * @param handle  Handle of website
     */
    Weblog getWeblogByHandle(String handle) throws WebloggerException;
    
    
    /**
     * Get website specified by handle with option to return only enabled websites.
     * @param handle  Handle of website
     */
    Weblog getWeblogByHandle(String handle, Boolean enabled)
        throws WebloggerException;
    
    
    /**
     * Get websites optionally restricted by user, enabled and active status.
     * @param enabled   Get all with this enabled state (or null or all)
     * @param active    Get all with this active state (or null or all)
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate   Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length    Maximum number of results to return (for paging)
     * @return List of Weblog objects.
     */
    List<Weblog> getWeblogs(
            Boolean  enabled,
            Boolean  active,
            Date     startDate,
            Date     endDate,
            int      offset,
            int      length)
            throws WebloggerException;
    
    
    /**
     * Get websites of a user.
     * @param user        Get all weblogs for this user
     * @param enabledOnly Include only enabled weblogs?
     * @return List of Weblog objects.
     */
    List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException;
    
    
    /**
     * Get users of a weblog.
     * @param weblog Weblog to retrieve users for
     * @param enabledOnly Include only enabled users?
     * @return List of WebsiteData objects.
     */
    List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException;
    
    
    /**
     * Get websites ordered by descending number of comments.
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length       Maximum number of results to return (for paging)
     * @return List of StatCount objects.
     */
    List<StatCount> getMostCommentedWeblogs(
            Date startDate,
            Date endDate,
            int  offset,
            int  length)
            throws WebloggerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    Map<String, Long> getWeblogHandleLetterMap() throws WebloggerException;
    
    
    /** 
     * Get collection of weblogs whose handles begin with specified letter 
     */
    List<Weblog> getWeblogsByLetter(char letter, int offset, int length)
        throws WebloggerException;
    
    /**
     * Store a custom weblog template.
     */
    void saveTemplate(WeblogTemplate data) throws WebloggerException;
    
    
    /**
     * Remove a custom template.
     */
    void removeTemplate(WeblogTemplate template) throws WebloggerException;
    
    
    /**
     * Get a custom template by its id.
     */
    WeblogTemplate getTemplate(String id) throws WebloggerException;
    
    
    /**
     * Get a custom template by the action it supports.
     */
    WeblogTemplate getTemplateByAction(Weblog w, ComponentType a) throws WebloggerException;
    
    
    /**
     * Get a custom template by its name.
     */
    WeblogTemplate getTemplateByName(Weblog w, String p) throws WebloggerException;
    
    
    /**
     * Get a custom template by its relative path.
     */
    WeblogTemplate getTemplateByPath(Weblog w, String p)
        throws WebloggerException;

    /**
     * Save a custom template rendition
     */
    void saveTemplateRendition(WeblogTemplateRendition templateCode) throws WebloggerException;

    /**
     * Get all custom templates for a weblog
     */
    List<WeblogTemplate> getTemplates(Weblog w) throws WebloggerException;
   
    
    /**
     * Get count of active weblogs
     */    
    long getWeblogCount() throws WebloggerException;
    
    /**
     * Get a HitCountData by weblog.
     *
     * @param weblog The Weblog that you want the hit count for.
     * @return The number of hits today stored for the weblog.
     * @throws WebloggerException If weblog does not exist or other problem with the backend.
     */
    int getHitCount(Weblog weblog)
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
     * @return A List of Weblogs, ranked by descending hit count.
     * @throws WebloggerException If there was a problem with the backend.
     */
    List<Weblog> getHotWeblogs(int sinceDays, int offset, int length)
            throws WebloggerException;


    /**
     * Reset the hit counts for all weblogs.  This sets the counts back to 0.
     *
     * @throws WebloggerException If there was a problem with the backend.
     */
    void resetAllHitCounts() throws WebloggerException;

    /**
     * Check for any scheduled weblog entries whose publication time has been
     * reached and promote them.
     *
     * @throws WebloggerException If there was a problem with the backend.
     */
    void promoteScheduledEntries() throws WebloggerException;

    /**
     * Empty the {@link HitCountQueue}, updating individual blog counters
     * with its data.
     *
     * @throws WebloggerException If there was a problem with the backend.
     */
    void updateHitCounters() throws WebloggerException;

    /**
     * Reset the hit counts for a single weblog.  This sets the count to 03.
     *
     * @param weblog The WebsiteData object to reset the count for.
     * @throws WebloggerException If there was a problem with the backend.
     */
    void resetHitCount(Weblog weblog) throws WebloggerException;

    /**
     * Increment the hit count for a weblog by a certain amount.
     *
     * This is basically a convenience method for doing a lookup, modify, save
     * of hit data
     *
     * @param weblog The weblog object to increment the count for.
     * @param amount How much to increment by.
     * @throws WebloggerException If there was a problem with the backend.
     */
    void incrementHitCount(Weblog weblog, int amount)
            throws WebloggerException;


    /**
     * Save a Bookmark.
     *
     * @param bookmark The bookmark to be saved.
     * @throws WebloggerException If there is a problem.
     */
    void saveBookmark(WeblogBookmark bookmark) throws WebloggerException;


    /**
     * Remove a Bookmark.
     *
     * @param bookmark The bookmark to be removed.
     * @throws WebloggerException If there is a problem.
     */
    void removeBookmark(WeblogBookmark bookmark) throws WebloggerException;


    /**
     * Lookup a Bookmark by ID.
     *
     * @param id The id of the bookmark to lookup.
     * @return BookmarkData The bookmark, or null if not found.
     * @throws WebloggerException If there is a problem.
     */
    WeblogBookmark getBookmark(String id) throws WebloggerException;

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
     * Check for duplicate category name.
     */
    boolean isDuplicateWeblogCategoryName(WeblogCategory data)
            throws WebloggerException;

    /**
     * Check if weblog category is in use.
     */
    boolean isWeblogCategoryInUse(WeblogCategory data);

}
