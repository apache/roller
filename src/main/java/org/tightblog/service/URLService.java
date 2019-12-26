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
package org.tightblog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.util.Utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Service used to generate URLs used by the system based on
 * available system settings.
 */
@Component
@EnableConfigurationProperties(DynamicProperties.class)
public class URLService {

    private static final String PREVIEW_URL_SEGMENT = "/tb-ui/authoring/preview/";

    @Autowired
    private DynamicProperties dp;

    public URLService() {
    }

    /**
     * Url to login page.
     */
    public String getLoginURL() {
        return dp.getAbsoluteUrl() + "/tb-ui/app/login-redirect";
    }

    /**
     * Url to logout page.
     */
    public String getLogoutURL() {
        return dp.getAbsoluteUrl() + "/tb-ui/app/logout";
    }

    /**
     * Url to register page.
     */
    public String getRegisterURL() {
        return dp.getAbsoluteUrl() + "/tb-ui/app/register";
    }

    /**
     * Url to home page of blog server (normally default blog).
     */
    public String getHomeURL() {
        return dp.getAbsoluteUrl();
    }

    /**
     * Generate the URL that commenters who selected "notify me" for future comments for an entry can
     * use to unsubscribe from further comments.
     */
    public String getCommentNotificationUnsubscribeURL(String commentId) {
        return dp.getAbsoluteUrl() + "/tb-ui/app/unsubscribe?commentId=" + commentId;
    }

    /**
     * Get a url to a UI action in a given namespace, optionally specifying a weblog object
     * if that is needed by the action.
     */
    public String getActionURL(String namespace, String action, Weblog weblog,
                               Map<String, String> parameters) {
        String url = dp.getAbsoluteUrl() + namespace + "/" + action;

        // add weblog handle parameter, if provided
        Map<String, String> params = new HashMap<>();
        if (weblog != null) {
            params.put("weblogId", weblog.getId());
        }

        if (parameters != null) {
            params.putAll(parameters);
        }
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url to add a new weblog entry.
     */
    public String getNewEntryURL(String weblogId) {
        String url = dp.getAbsoluteUrl() + "/tb-ui/app/authoring/entryAdd";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogId);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url to edit a specific weblog entry.
     */
    public String getEntryEditURL(WeblogEntry entry) {
        String url = dp.getAbsoluteUrl() + "/tb-ui/app/authoring/entryEdit";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", entry.getWeblog().getId());
        params.put("entryId", entry.getId());
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url for the comments for a specific weblog entry.
     */
    public String getCommentManagementURL(String weblogId, String entryId) {
        String url = dp.getAbsoluteUrl() + "/tb-ui/app/authoring/comments";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogId);
        params.put("entryId", entryId);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url to weblog config page.
     */
    public String getWeblogConfigURL(String weblogHandle) {
        String url = dp.getAbsoluteUrl() + "/tb-ui/app/authoring/weblogConfig";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogHandle);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get root url for a given weblog.
     */
    public String getWeblogURL(Weblog weblog) {
        return getWeblogRootURL(weblog);
    }

    /**
     * Get the url for previewing a draft of a given blog entry.
     */
    public String getWeblogEntryDraftPreviewURL(WeblogEntry entry) {
        String url = dp.getAbsoluteUrl() + PREVIEW_URL_SEGMENT + entry.getWeblog().getHandle() + "/";
        url += "entry/" + Utilities.encode(entry.getAnchor());
        return url;
    }

    /**
     * Get the POST url for a weblog entry comment.
     * @param entry weblog entry where comment is being placed
     */
    public String getWeblogEntryPostCommentURL(WeblogEntry entry) {
        String url = "";
        if (entry != null) {
            url = getWeblogURL(entry.getWeblog()) + "entrycomment/"
                    + Utilities.encode(entry.getAnchor());
        }
        return url;
    }

    /**
     * Get url for a single mediafile on a given weblog.
     */
    public String getMediaFileURL(Weblog weblog, String fileAnchor) {
        return getWeblogURL(weblog) + "mediafile/" + Utilities.encode(fileAnchor);
    }

    /**
     * Get url for a single mediafile thumbnail on a given weblog.
     */
    public String getMediaFileThumbnailURL(Weblog weblog, String fileAnchor) {
        return getMediaFileURL(weblog, fileAnchor) + "?tn=true";
    }

    /**
     * Get url for the comments section of a single weblog entry
     * @param entry entry whose comments it is desired to view
     */
    public String getWeblogEntryCommentsURL(WeblogEntry entry) {
        return getWeblogEntryURL(entry) + "#comments";
    }

    /**
     * Get url for a single weblog entry comment
     * @param entry entry whose comments it is desired to view
     * @param timeStamp timestamp of comment (used to identify comment desired)
     */
    public String getCommentURL(WeblogEntry entry, String timeStamp) {
        return getWeblogEntryURL(entry) + "#comment-" + timeStamp;
    }

    /**
     * Get url for a feed on a given weblog.
     */
    public String getAtomFeedURL(Weblog weblog) {
        return getWeblogURL(weblog) + "feed";
    }

    /**
     * Get url for a feed on a given weblog category.
     */
    public String getAtomFeedURLForCategory(Weblog weblog, String category) {
        String url = getAtomFeedURL(weblog);
        if (category != null && category.trim().length() > 0) {
            url += "?category=" + Utilities.encode(category);
        }
        return url;
    }

    /**
     * Get url for a feed on a given tag for a weblog.
     */
    public String getAtomFeedURLForTag(Weblog weblog, String tag) {
        String url = getAtomFeedURL(weblog);
        if (tag != null && tag.trim().length() > 0) {
            url += "?tag=" + Utilities.encode(tag);
        }
        return url;
    }

    /* Weblog URL before any params added */
    private String getWeblogRootURL(Weblog weblog) {
        return dp.getAbsoluteUrl() + "/" + weblog.getHandle() + "/";
    }

    /**
     * Get url for a single weblog entry on a given weblog.
     */
    public String getWeblogEntryURL(WeblogEntry entry) {
        String url = getWeblogRootURL(entry.getWeblog()) + "entry/" + Utilities.encode(entry.getAnchor());
        Map<String, String> params = new HashMap<>();
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get url for a collection of entries on a given weblog.
     */
    public String getWeblogCollectionURL(Weblog weblog, String category, String dateString, String tag, int pageNum) {

        String pathinfo = getWeblogRootURL(weblog);
        Map<String, String> params = new HashMap<>();

        if (category != null && dateString == null) {
            pathinfo += "category/" + Utilities.encode(category);
            if (tag != null) {
                params.put("tag", Utilities.encode(tag));
            }
        } else if (dateString != null && category == null) {
            pathinfo += "date/" + dateString;
        } else if (tag != null) {
            pathinfo += "tag/" + Utilities.encode(tag);
        } else {
            if (dateString != null) {
                params.put("date", dateString);
            }
            if (category != null) {
                params.put("cat", Utilities.encode(category));
            }
        }

        if (pageNum > 0) {
            params.put("page", Integer.toString(pageNum));
        }

        return pathinfo + Utilities.getQueryString(params);
    }

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
    public String getCustomPageURL(Weblog weblog, String pageLink, String dateString) {
        String url = getWeblogRootURL(weblog);
        Map<String, String> params = new HashMap<>();

        if (pageLink != null && pageLink.length() > 0) {
            url += "page/" + pageLink;

            if (dateString != null) {
                params.put("date", dateString);
            }
        }

        return url + Utilities.getQueryString(params);
    }

    /**
     * Get url to search endpoint on a given weblog.
     */
    public String getWeblogSearchURL(Weblog weblog, String query, String category, int pageNum) {
        String url = getWeblogURL(weblog) + "search";

        Map<String, String> params = new HashMap<>();
        if (query != null) {
            params.put("q", Utilities.encode(query));

            // other stuff only makes sense if there is a query
            if (category != null) {
                params.put("cat", Utilities.encode(category));
            }
            if (pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        }

        return url + Utilities.getQueryString(params);
    }

    /**
     * Get the url for what the comment authentication form submits to.
     */
    public String getCommentAuthenticatorURL() {
        return dp.getAbsoluteUrl() + "/tb-ui/rendering/comment/authform";
    }

    /**
     * Get a url for a static resource (image, stylesheet) in a shared (bundled) theme
     */
    public String getThemeResourceURL(String theme, String filePath) {
        return dp.getAbsoluteUrl() + "/blogthemes/" + theme + "/" + filePath;
    }

}
