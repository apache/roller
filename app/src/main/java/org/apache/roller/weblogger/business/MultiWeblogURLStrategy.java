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

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * A Weblogger URLStrategy which builds urls for a multi-weblog environment.
 */
public class MultiWeblogURLStrategy implements URLStrategy {

    public MultiWeblogURLStrategy() {
    }

    @Override
    public URLStrategy getPreviewURLStrategy(String previewTheme) {
        return new PreviewURLStrategy(previewTheme);
    }

    protected String getRootURL(boolean absolute) {
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
    public String getActionURL(String action, String namespace, Weblog weblog,
                               Map<String, String> parameters, boolean absolute) {
        String url = getRootURL(absolute) + namespace + "/" + action;

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
    public String getCommentManagementURL(String weblogId, String entryId, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/app/authoring/comments";
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
        return getRootURL(absolute) + "/" + weblog.getHandle() + "/";
    }

    @Override
    public String getWeblogEntryURL(Weblog weblog, String entryAnchor, boolean absolute) {
        return getWeblogURL(weblog, absolute) + "entry/" + Utilities.encode(entryAnchor);
    }

    @Override
    public String getWeblogEntryCommentURL(Weblog weblog, String entryAnchor, boolean absolute) {
        return getWeblogURL(weblog, absolute) + "entrycomment/" + Utilities.encode(entryAnchor);
    }

    @Override
    public String getMediaFileURL(Weblog weblog, String fileAnchor, boolean absolute) {
        return getWeblogURL(weblog, absolute) + "mediaresource/" + Utilities.encode(fileAnchor);
    }

    @Override
    public String getMediaFileThumbnailURL(Weblog weblog, String fileAnchor, boolean absolute) {
        return getMediaFileURL(weblog, fileAnchor, absolute) + "?t=true";
    }

    @Override
    public String getWeblogCommentsURL(Weblog weblog, String entryAnchor, boolean absolute) {
        return getWeblogEntryURL(weblog, entryAnchor, absolute) + "#comments";
    }

    @Override
    public String getWeblogCommentURL(Weblog weblog, String entryAnchor, String timeStamp, boolean absolute) {
        return getWeblogEntryURL(weblog, entryAnchor, absolute) + "#comment-" + timeStamp;
    }

    @Override
    public String getWeblogCollectionURL(Weblog weblog, String category, String dateString, String tag,
                                         int pageNum, boolean absolute) {
        StringBuilder pathinfo = new StringBuilder();
        pathinfo.append(getWeblogURL(weblog, absolute));

        Map<String, String> params = new HashMap<>();

        if (category != null && dateString == null) {
            pathinfo.append("category/").append(Utilities.encodePath(category));
        } else if (dateString != null && category == null) {
            pathinfo.append("date/").append(dateString);
        } else if (tag != null) {
            pathinfo.append("tags/").append(Utilities.encodePath(tag));
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

        return pathinfo.toString() + Utilities.getQueryString(params);
    }

    @Override
    public String getWeblogPageURL(Weblog weblog, String theme, String pageLink, String entryAnchor, String category,
                                   String dateString, String tag, int pageNum, boolean absolute) {

        StringBuilder pathinfo = new StringBuilder();
        Map<String, String> params = new HashMap<>();

        pathinfo.append(getWeblogURL(weblog, absolute));

        if (pageLink != null) {
            pathinfo.append("page/").append(pageLink);

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

        return pathinfo.toString() + Utilities.getQueryString(params);
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
