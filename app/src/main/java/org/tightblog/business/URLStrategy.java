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
    String getLoginURL();

    /**
     * Url to logout page.
     */
    String getLogoutURL();

    /**
     * Url to register page.
     */
    String getRegisterURL();

    /**
     * Url to home page of blog server (normally default blog).
     */
    String getHomeURL();

    /**
     * Get a url to a UI action in a given namespace, optionally specifying a weblog object
     * if that is needed by the action.
     */
    String getActionURL(String action, String namespace, Weblog weblog, Map<String, String> params);

    /**
     * Get a url to add a new weblog entry.
     */
    String getNewEntryURL(String weblogId);

    /**
     * Get a url to edit a specific weblog entry.
     */
    String getEntryEditURL(WeblogEntry entry);

    /**
     * Get a url for the comments for a specific weblog entry.
     */
    String getCommentManagementURL(String weblogId, String entryId);

    /**
     * Get the url for what the comment authentication form submits to.
     */
    String getCommentAuthenticatorURL();

    /**
     * Get a url to weblog config page.
     */
    String getWeblogConfigURL(String weblogHandle);

    /**
     * Get root url for a given weblog.
     */
    String getWeblogURL(Weblog weblog);

    /**
     * Get url for a single weblog entry on a given weblog.
     */
    String getWeblogEntryURL(WeblogEntry entry);

    /**
     * Get the url for previewing a draft of a given blog entry.
     */
    String getWeblogEntryDraftPreviewURL(WeblogEntry entry);

    /**
     * Get the POST url for a weblog entry comment.
     * @param entry weblog entry where comment is being placed
     * @param isPreview true if commenter wishes to preview (not yet submit) comment
     */
    String getWeblogEntryPostCommentURL(WeblogEntry entry, boolean isPreview);

    /**
     * Get url for the comments section of a single weblog entry
     * @param entry entry whose comments it is desired to view
     */
    String getWeblogEntryCommentsURL(WeblogEntry entry);

    /**
     * Get url for a single weblog entry comment
     * @param entry entry whose comments it is desired to view
     * @param timeStamp timestamp of comment (used to identify comment desired)
     */
    String getCommentURL(WeblogEntry entry, String timeStamp);

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
    String getWeblogCollectionURL(Weblog weblog, String category, String dateString, String tag, int pageNum);

    /**
     * Get a url for a static resource (image, stylesheet) in a shared (bundled) theme
     */
    String getThemeResourceURL(String theme, String filePath);

    /**
     * TightBlog, in its template section, allows for creation of custom external
     * pages, HTML-generating templates containing arbitrary content that can be
     * viewed via a specific link outside of the normal pages for a blog, e.g.,
     * "http://www.foo.com/myblog/page/MyPage.html."
     * @param weblog weblog containing the custom external page.
     * @param pageLink the page link (in the example above MyPage.html) defined by
     *                 the blogger when creating the template that generates this
     *                 page. For the example above, it would be MyPage.html.
     * @param dateString Desired date (YYYYMM or YYYYMMDD format) parameter for the page,
     *                   useful e.g. a calendar on a blog archive page where the YYYYMM
     *                   indicates the calendar month to display
     * @return URL, absolute or relative, for the given pageLink.
     */
    String getCustomPageURL(Weblog weblog, String pageLink, String dateString);

    /**
     * Get url for a feed on a given weblog.
     */
    String getAtomFeedURL(Weblog weblog);

    /**
     * Get url for a feed on a given weblog category.
     */
    String getAtomFeedURLForCategory(Weblog weblog, String category);

    /**
     * Get url for a feed on a given tag for a weblog.
     */
    String getAtomFeedURLForTag(Weblog weblog, String tag);

    /**
     * Get url to search endpoint on a given weblog.
     */
    String getWeblogSearchURL(Weblog weblog, String query, String category, int pageNum);

    /**
     * Generate the URL that commenters who selected "notify me" for future comments for an entry can
     * use to unsubscribe from further comments.
     */
    String getCommentNotificationUnsubscribeURL(String commentId);
}
