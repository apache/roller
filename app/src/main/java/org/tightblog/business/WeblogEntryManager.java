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

import org.tightblog.pojos.AtomEnclosure;
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogEntrySearchCriteria;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface to weblog entry, category and comment management.
 */
public interface WeblogEntryManager {

    /**
     * Save weblog entry.
     */
    void saveWeblogEntry(WeblogEntry entry);

    /**
     * Remove weblog entry.
     */
    void removeWeblogEntry(WeblogEntry entry);

    /**
     * Get weblog entry by id.
     */
    WeblogEntry getWeblogEntry(String id, boolean bypassCache);

    /**
     * Get entry count based on specified WeblogEntrySearchCriteria
     */
    long getEntryCount(WeblogEntrySearchCriteria wesc);

    /**
     * Get weblog entry by anchor.
     */
    WeblogEntry getWeblogEntryByAnchor(Weblog website, String anchor);

    /**
     * Get WeblogEntries by offset/length as list in reverse chronological order.
     * The range offset and list arguments enable paging through query results.
     *
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return List of WeblogEntry objects in order specified by search criteria
     */
    List<WeblogEntry> getWeblogEntries(WeblogEntrySearchCriteria wesc);

    /**
     * Get Weblog Entries grouped by day.
     *
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return Map of Lists of WeblogEntries keyed by calendar day
     */
    Map<LocalDate, List<WeblogEntry>> getWeblogEntryObjectMap(WeblogEntrySearchCriteria wesc);

    /**
     * Get Weblog Entry date strings grouped by day. This method returns a Map
     * that contains one YYYYMMDD date string object for each calendar day having
     * one or more blog entries.
     *
     * @param wesc WeblogEntrySearchCriteria object listing desired search parameters
     * @return Map of date strings keyed by Date
     */
    Map<LocalDate, String> getWeblogEntryStringMap(WeblogEntrySearchCriteria wesc);

    /**
     * Get the WeblogEntry following, chronologically, the current entry.
     * Restrict by the Category, if named.
     *
     * @param current The "current" WeblogEntryData
     * @param catName The value of the requested Category Name
     */
    WeblogEntry getNextEntry(WeblogEntry current, String catName);

    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     * Restrict by the Category, if named.
     *
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     */
    WeblogEntry getPreviousEntry(WeblogEntry current, String catName);

    /**
     * Save comment.
     *
     * @param refreshWeblog true if weblog should be marked for cache update, i.e., likely
     *                      rendering change to accommodate new or removed comment, vs. one
     *                      still requiring moderation.
     */
    void saveComment(WeblogEntryComment comment, boolean refreshWeblog);

    /**
     * Remove comment.
     */
    void removeComment(WeblogEntryComment comment);

    /**
     * Get comment by id.
     */
    WeblogEntryComment getComment(String id);

    /**
     * Generic comments query method.
     *
     * @param csc CommentSearchCriteria object with fields indicating search criteria
     * @return list of comments fitting search criteria
     */
    List<WeblogEntryComment> getComments(CommentSearchCriteria csc);

    /**
     * Create unique anchor for weblog entry.
     */
    String createAnchor(WeblogEntry data);

    /**
     * Apply comment default settings from website to all of website's entries.
     */
    void applyCommentDefaultsToEntries(Weblog website);

    /**
     * Get comment count based on specified CommentSearchCriteria
     */
    long getCommentCount(CommentSearchCriteria csc);

    /**
     * Process the blog text based on whether Commonmark and/or JSoup tag
     * filtering is activated.  This method must *NOT* alter the contents of
     * the original entry object, to allow the blogger to return to his original
     * text for additional editing as desired.
     *
     * @param format Weblog.EditFormat indicating input format of string
     * @param str   String to which to apply processing.
     * @return the transformed text
     */
    String processBlogText(Weblog.EditFormat format, String str);

    /**
     * Create an Atom enclosure element for the resource (usually podcast or other
     * multimedia) at the specified URL.
     *
     * @param url web URL where the resource is located.
     * @return AtomEnclosure element for the resource
     */
    AtomEnclosure generateEnclosure(String url);

}
