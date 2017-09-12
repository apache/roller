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

package org.tightblog.business;

import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;

import java.util.Map;

/**
 * An interface representing the TightBlog URL strategy.
 * <p>
 * Implementations of this interface provide methods which can be used to form
 * all of the public urls used by the system.
 */
public interface URLStrategy {

    /**
     * Url to login page.
     */
    String getLoginURL(boolean absolute);

    /**
     * Url to logout page.
     */
    String getLogoutURL(boolean absolute);

    /**
     * Url to register page.
     */
    String getRegisterURL(boolean absolute);

    /**
     * Get a url to a UI action in a given namespace, optionally specifying a weblog object
     * if that is needed by the action.
     */
    String getActionURL(String action, String namespace, Weblog weblog, Map<String, String> params);

    /**
     * Get a url to add a new weblog entry.
     */
    String getEntryAddURL(String weblogId, boolean absolute);

    /**
     * Get a url to edit a specific weblog entry.
     */
    String getEntryEditURL(String weblogId, String entryId, boolean absolute);

    /**
     * Get a url for the comments for a specific weblog entry.
     */
    String getCommentManagementURL(String weblogId, String entryId);

    /**
     * Get a url to weblog config page.
     */
    String getWeblogConfigURL(String weblogHandle, boolean absolute);

    /**
     * Get root url for a given weblog.
     */
    String getWeblogURL(Weblog weblog, boolean absolute);

    /**
     * Get url for a single weblog entry on a given weblog.
     */
    String getWeblogEntryURL(WeblogEntry entry, boolean absolute);

    /**
     * Get the url for previewing a draft of a given blog entry.
     */
    String getWeblogEntryDraftPreviewURL(WeblogEntry entry);

    /**
     * Get the POST url for a weblog entry comment.
     */
    String getWeblogEntryCommentURL(Weblog weblog, String entryAnchor);

    /**
     * Get url for a single weblog entry comments on a given weblog.
     * @param entry entry whose comments it is desired to view
     */
    String getWeblogCommentsURL(WeblogEntry entry);

    /**
     * Get url for a single weblog entry comment on a given weblog.
     */
    String getWeblogCommentURL(WeblogEntry entry, String timeStamp);

    /**
     * Get url for a single mediafile on a given weblog.
     */
    String getMediaFileURL(Weblog weblog, String fileAnchor);

    /**
     * Get url for a single mediafile thumbnail on a given weblog.
     */
    String getMediaFileThumbnailURL(Weblog weblog, String fileAnchor);

    /**
     * Get url for a collection of entries on a given weblog.
     */
    String getWeblogCollectionURL(Weblog weblog, String category, String dateString, String tag,
                                  int pageNum, boolean absolute);

    /**
     * Get url for a custom page on a given weblog.
     */
    String getWeblogPageURL(Weblog weblog, String pageLink, String category,
                            String dateString, String tag, int pageNum, boolean absolute);

    /**
     * Get url for a feed on a given weblog.
     */
    String getWeblogFeedURL(Weblog weblog, String type, String category, String tag);

    /**
     * Get url to search endpoint on a given weblog.
     */
    String getWeblogSearchURL(Weblog weblog, String query, String category, int pageNum, boolean absolute);

    /**
     * Generate the URL that commenters who selected "notify me" for future comments for an entry can
     * use to unsubscribe from further comments.
     */
    String getCommentNotificationUnsubscribeUrl(String commentId);

}
