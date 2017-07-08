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
import org.tightblog.util.Utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard implementation of the URLStrategy interface.
 */
public class URLStrategyImpl implements URLStrategy {

    private final String previewTheme;

    private static final String PREVIEW_URL_SEGMENT = "/tb-ui/authoring/preview/";

    public URLStrategyImpl() {
        previewTheme = null;
    }

    public URLStrategyImpl(String previewTheme) {
        this.previewTheme = previewTheme;
    }

    private String getRootURL(boolean absolute) {
        if (absolute) {
            return WebloggerStaticConfig.getAbsoluteContextURL();
        } else {
            return WebloggerStaticConfig.getRelativeContextURL();
        }
    }

    @Override
    public String getLoginURL(boolean absolute) {
        return getRootURL(absolute) + "/tb-ui/app/login-redirect";
    }

    @Override
    public String getLogoutURL(boolean absolute) {
        return getRootURL(absolute) + "/tb-ui/app/logout";
    }

    @Override
    public String getRegisterURL(boolean absolute) {
        return getRootURL(absolute) + "/tb-ui/app/register";
    }

    @Override
    public String getCommentNotificationUnsubscribeUrl(String commentId) {
        return getRootURL(true) + "/tb-ui/app/unsubscribe?commentId=" + commentId;
    }

    @Override
    public String getActionURL(String action, String namespace, Weblog weblog,
                               Map<String, String> parameters) {
        String url = getRootURL(true) + namespace + "/" + action;

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

    @Override
    public String getEntryAddURL(String weblogId, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/app/authoring/entryAdd";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogId);
        return url + Utilities.getQueryString(params);
    }

    @Override
    public String getEntryEditURL(String weblogId, String entryId, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/app/authoring/entryEdit";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogId);
        params.put("entryId", entryId);
        return url + Utilities.getQueryString(params);
    }

    @Override
    public String getCommentManagementURL(String weblogId, String entryId) {
        String url = getRootURL(true) + "/tb-ui/app/authoring/comments";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogId);
        params.put("entryId", entryId);
        return url + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogConfigURL(String weblogHandle, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/app/authoring/weblogConfig";
        Map<String, String> params = new HashMap<>();
        params.put("weblogId", weblogHandle);
        return url + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogURL(Weblog weblog, boolean absolute) {
        String url = getRootWeblogURL(weblog, absolute);
        Map<String, String> params = new HashMap<>();
        addPreviewParams(params);
        return url + Utilities.getQueryString(params);
    }

    /* Weblog URL before any params added */
    private String getRootWeblogURL(Weblog weblog, boolean absolute) {
        if (previewTheme == null) {
            return getRootURL(absolute) + "/" + weblog.getHandle() + "/";
        } else {
            return getRootURL(absolute) + PREVIEW_URL_SEGMENT + weblog.getHandle() + "/";
        }
    }

    private void addPreviewParams(Map<String, String> params) {
        if (previewTheme != null) {
            params.put("theme", Utilities.encode(previewTheme));
        }
    }

    @Override
    public String getWeblogEntryURL(Weblog weblog, String entryAnchor, boolean absolute) {
        String url = getRootWeblogURL(weblog, absolute) + "entry/" + Utilities.encode(entryAnchor);
        Map<String, String> params = new HashMap<>();
        addPreviewParams(params);
        return url + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogEntryPreviewURL(WeblogEntry entry) {
        String url = getRootURL(true) + PREVIEW_URL_SEGMENT + entry.getWeblog().getHandle() + "/";
        url += "entry/" + Utilities.encode(entry.getAnchor());
        return url;
    }

    @Override
    public String getWeblogEntryCommentURL(Weblog weblog, String entryAnchor) {
        return getWeblogURL(weblog, true) + "entrycomment/" + Utilities.encode(entryAnchor);
    }

    @Override
    public String getMediaFileURL(Weblog weblog, String fileAnchor) {
        return getWeblogURL(weblog, true) + "mediaresource/" + Utilities.encode(fileAnchor);
    }

    @Override
    public String getMediaFileThumbnailURL(Weblog weblog, String fileAnchor) {
        return getMediaFileURL(weblog, fileAnchor) + "?t=true";
    }

    @Override
    public String getWeblogCommentsURL(Weblog weblog, String entryAnchor) {
        return getWeblogEntryURL(weblog, entryAnchor, true) + "#comments";
    }

    @Override
    public String getWeblogCommentURL(Weblog weblog, String entryAnchor, String timeStamp) {
        return getWeblogEntryURL(weblog, entryAnchor, true) + "#comment-" + timeStamp;
    }

    @Override
    public String getWeblogCollectionURL(Weblog weblog, String category, String dateString, String tag,
                                         int pageNum, boolean absolute) {

        String pathinfo = getRootWeblogURL(weblog, absolute);
        Map<String, String> params = new HashMap<>();

        if (category != null && dateString == null) {
            pathinfo += "category/" + Utilities.encodePath(category);
        } else if (dateString != null && category == null) {
            pathinfo += "date/" + dateString;
        } else if (tag != null) {
            pathinfo += "tags/" + Utilities.encodePath(tag);
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

        addPreviewParams(params);

        return pathinfo + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogPageURL(Weblog weblog, String theme, String pageLink, String entryAnchor, String category,
                                   String dateString, String tag, int pageNum, boolean absolute) {

        String pathinfo = getRootWeblogURL(weblog, absolute);
        Map<String, String> params = new HashMap<>();

        if (theme != null) {
            params.put("theme", Utilities.encode(theme));
        } else {
            addPreviewParams(params);
        }

        if (pageLink != null) {
            pathinfo += "page/" + pageLink;

            // for custom pages we only allow query params
            if (dateString != null) {
                params.put("date", dateString);
            }
            if (category != null) {
                params.put("cat", Utilities.encode(category));
            }
            if (tag != null) {
                params.put("tag", Utilities.encode(tag));
            }
            if (pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        } else {
            // if there is no page link then this is just a typical collection url
            return getWeblogCollectionURL(weblog, category, dateString, tag, pageNum, absolute);
        }

        return pathinfo + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogFeedURL(Weblog weblog, String type, String category, String tag) {

        String url = getWeblogURL(weblog, true) + "feed/" + type;

        Map<String, String> params = new HashMap<>();
        if (category != null && category.trim().length() > 0) {
            params.put("cat", Utilities.encode(category));
        }
        if (tag != null && tag.trim().length() > 0) {
            params.put("tag", Utilities.encode(tag));
        }

        return url + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogSearchURL(Weblog weblog, String query, String category, int pageNum, boolean absolute) {
        String url = getWeblogURL(weblog, absolute) + "search";

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

}
