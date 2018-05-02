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
package org.tightblog.business;

import org.tightblog.pojos.Template.ComponentType;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogBookmark;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntryTagAggregate;
import org.tightblog.pojos.WeblogTemplate;

import java.util.List;
import java.util.Map;

/**
 * Interface to weblog and weblog custom template management.
 */
public interface WeblogManager {

    /**
     * Add new weblog, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new weblog.
     *
     * @param newWeblog New weblog to be created, must have creator field populated.
     */
    void addWeblog(Weblog newWeblog);

    /**
     * Store a single weblog.
     */
    void saveWeblog(Weblog data);

    /**
     * Remove weblog object.
     */
    void removeWeblog(Weblog weblog);

    /**
     * Get weblog object by name.
     */
    Weblog getWeblog(String id);

    /**
     * Get weblog specified by handle (or null if enabled weblog not found).
     *
     * @param handle Handle of weblog
     * @return Weblog instance or null if not found
     */
    Weblog getWeblogByHandle(String handle);

    /**
     * Get weblog specified by handle with option to return only enabled weblogs.
     *
     * @param handle Handle of weblog
     * @return Weblog instance or null if not found
     */
    Weblog getWeblogByHandle(String handle, Boolean enabled);

    /**
     * Get weblogs optionally restricted by user, enabled and active status.
     *
     * @param visible Get all with this visible state (or null or all)
     * @param offset  Offset into results (for paging)
     * @param length  Maximum number of results to return (for paging)
     * @return List of Weblog objects.
     */
    List<Weblog> getWeblogs(Boolean visible, int offset, int length);

    /**
     * Get users of a weblog.
     *
     * @param weblog      Weblog to retrieve users for
     * @return List of User objects.
     */
    List<User> getWeblogUsers(Weblog weblog);

    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    Map<Character, Integer> getWeblogHandleLetterMap();

    /**
     * Get collection of weblogs whose handles begin with specified letter
     */
    List<Weblog> getWeblogsByLetter(char letter, int offset, int length);

    /**
     * Get the analytics tracking code to be used for the provided Weblog
     * @param weblog weblog to determine tracking code for
     * @return analytics tracking code, empty string if none.
     */
    String getAnalyticsTrackingCode(Weblog weblog);

    /**
     * Store a custom weblog template.
     */
    void saveTemplate(WeblogTemplate template);

    /**
     * Remove a custom template.
     */
    void removeTemplate(WeblogTemplate template);

    /**
     * Get a custom template by its id.
     */
    WeblogTemplate getTemplate(String id);

    /**
     * Get a custom template by the action it supports.
     */
    WeblogTemplate getTemplateByAction(Weblog w, ComponentType a);

    /**
     * Get a custom template by its name.
     */
    WeblogTemplate getTemplateByName(Weblog w, String p);

    /**
     * Get a custom template by its relative path.
     */
    WeblogTemplate getTemplateByPath(Weblog w, String p);

    /**
     * Get all custom templates for a weblog
     */
    List<WeblogTemplate> getTemplates(Weblog w);

    /**
     * Get count of active weblogs, returning long type as that is what the
     * JPA COUNT aggregate returns (http://stackoverflow.com/a/3574441/1207540)
     */
    long getWeblogCount();

    /**
     * Return the day's most popular weblog objects as determined by blog hit count.
     * <p>
     * The results are pagable via the offset and length params.
     * <p>
     * The results are ordered by highest counts in descending order, and any
     * weblogs which are not visible are not included.
     *
     * @param offset    What index in the results to begin from, positive number or 0 for beginning
     * @param length    The number of results to return.
     * @return A List of Weblogs, ranked by descending hit count.
     */
    List<Weblog> getHotWeblogs(int offset, int length);

    /**
     * Reset the hit counts for all weblogs.  This sets the counts back to 0.
     */
    void resetAllHitCounts();

    /**
     * Check for any scheduled weblog entries whose publication time has been
     * reached and promote them.
     */
    void promoteScheduledEntries();

    /**
     * Increment the hit (external view) count by one for a weblog.  This
     * information is not written to the database immediately but stored
     * in a queue.
     *
     * @param weblog The weblog object to increment the count for.
     */
    void incrementHitCount(Weblog weblog);

    /**
     * Job to write out the hit count queue to the database, updating
     * individual blog's hit counters
     */
    void updateHitCounters();

    /**
     * Save a Bookmark.
     *
     * @param bookmark The bookmark to be saved.
     */
    void saveBookmark(WeblogBookmark bookmark);

    /**
     * Remove a Bookmark.
     *
     * @param bookmark The bookmark to be removed.
     */
    void removeBookmark(WeblogBookmark bookmark);

    /**
     * Lookup a Bookmark by ID.
     *
     * @param id The id of the bookmark to lookup.
     * @return BookmarkData The bookmark, or null if not found.
     */
    WeblogBookmark getBookmark(String id);

    /**
     * Save weblog category.
     */
    void saveWeblogCategory(WeblogCategory cat);

    /**
     * Remove weblog category.
     */
    void removeWeblogCategory(WeblogCategory cat);

    /**
     * Get category by id.
     */
    WeblogCategory getWeblogCategory(String id);

    /**
     * Recategorize all entries with one category to another.
     */
    void moveWeblogCategoryContents(WeblogCategory srcCat, WeblogCategory destCat);

    /**
     * Get category specified by weblog and name.
     *
     * @param weblog       Weblog of WeblogCategory.
     * @param categoryName Name of WeblogCategory
     * @return WeblogCategory, or null if not found.
     */
    WeblogCategory getWeblogCategoryByName(Weblog weblog, String categoryName);

    /**
     * Get WeblogCategory objects for a weblog.
     */
    List<WeblogCategory> getWeblogCategories(Weblog weblog);

    /**
     * Get list of WeblogEntryTagAggregate objects identifying the most used tags for a weblog.
     * There are no offset/length params just a limit.
     *
     * @param weblog Weblog or null to get for all weblogs.
     * @param offset 0-based index into results
     * @param limit  Max objects to return (or -1 for no limit)
     * @return List of most popular tags.
     */
    List<WeblogEntryTagAggregate> getPopularTags(Weblog weblog, int offset, int limit);

    /**
     * Get list of WeblogEntryTagAggregate objects for the tags comprising a weblog.
     *
     * @param website    Weblog or null to get for all weblogs.
     * @param sortBy     Sort by either 'name' or 'count' (null for name)
     * @param startsWith Prefix for tags to be returned (null or a string of length > 0)
     * @param offset     0-based index into returns
     * @param limit      Max objects to return (or -1 for no limit)
     * @return List of tags matching the criteria.
     */
    List<WeblogEntryTagAggregate> getTags(Weblog website, String sortBy, String startsWith, int offset, int limit);

    /**
     * Does the specified tag exist?  Optionally confined to a specific weblog.
     * <p/>
     * This checks if the Weblog (or all weblogs) has at least one blog entry with
     * the given tag.
     *
     * @param weblog The weblog to confine the check to.
     * @param tag    The tag to search for.
     * @return True if the tag exists, false otherwise.
     */
    boolean getTagExists(Weblog weblog, String tag);

    /**
     * Remove all tags with a given name from a weblog's entries
     *
     * @param weblog The weblog to remove the tag from
     * @param tagName Tag name to remove.
     */
    void removeTag(Weblog weblog, String tagName);

    /**
     * Add a tag to all entries having a current tag.
     *
     * @param weblog The weblog whose entries tag will be added to
     * @param currentTag The entries, having this tag, that will receive the new tag.
     * @param newTag New tag to add to entries having currentTag, if they don't have this tag already.
     * @return Map with keys of "updated" and "unchanged" indicating number of entries updated, where
     *         unchanged refers to entries having currentTag but already having newTag.
     */
    Map<String, Integer> addTag(Weblog weblog, String currentTag, String newTag);
}
