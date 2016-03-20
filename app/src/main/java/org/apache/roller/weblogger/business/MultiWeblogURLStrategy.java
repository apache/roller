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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;


/**
 * A Weblogger URLStrategy which builds urls for a multi-weblog environment.
 */
public class MultiWeblogURLStrategy implements URLStrategy {
    
    public MultiWeblogURLStrategy() {}
    
    /**
     * @inheritDoc
     */
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

    /**
     * Url to login page.
     */
    public String getLoginURL(boolean absolute) {
        return getRootURL(absolute) + "/tb-ui/login-redirect.rol";
    }

    /**
     * Url to logout page.
     */
    public String getLogoutURL(boolean absolute) {
        return getRootURL(absolute) + "/tb-ui/logout.rol";
    }

    /**
     * Url to register page.
     */
    public String getRegisterURL(boolean absolute) {
        return getRootURL(absolute) + "/tb-ui/register.rol";
    }

    /**
     * Get a url to a UI action in a given namespace, optionally specifying
     * a weblogHandle parameter if that is needed by the action.
     */
    public String getActionURL(String action, String namespace, String weblogHandle,
                               Map<String, String> parameters, boolean absolute) {
        String url = getRootURL(absolute) + namespace + "/" + action + ".rol";

        // add weblog handle parameter, if provided
        Map<String, String> params = new HashMap<>();
        if(weblogHandle != null) {
            params.put("weblog", weblogHandle);
        }

        if (parameters != null) {
            params.putAll(parameters);
        }
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url to add a new weblog entry.
     */
    public String getEntryAddURL(String weblogHandle, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/authoring/entryAdd.rol";
        Map<String, String> params = new HashMap<>();
        params.put("weblog", weblogHandle);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url to edit a specific weblog entry.
     */
    public String getEntryEditURL(String weblogHandle, String entryId, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/authoring/entryEdit.rol";
        Map<String, String> params = new HashMap<>();
        params.put("weblog", weblogHandle);
        params.put("bean.id", entryId);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get a url to weblog config page.
     */
    public String getWeblogConfigURL(String weblogHandle, boolean absolute) {
        String url = getRootURL(absolute) + "/tb-ui/authoring/weblogConfig.rol";
        Map<String, String> params = new HashMap<>();
        params.put("weblog", weblogHandle);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get URL of a planet's newsfeed.
     * @param planet URL for the desired planet
     */
    public String getPlanetURL(String planet) {
        String url = getRootURL(true) + "/planetrss";
        Map<String, String> params = new HashMap<>();
        params.put("planet", planet);
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get root url for a given weblog.
     */
    public String getWeblogURL(Weblog weblog, boolean absolute) {
        return getRootURL(absolute) + "/" + weblog.getHandle() + "/";
    }
    
    /**
     * Get url for a single weblog entry on a given weblog.
     */
    public String getWeblogEntryURL(Weblog weblog, String entryAnchor, boolean absolute) {
        return getWeblogURL(weblog, absolute) + "entry/" + Utilities.encode(entryAnchor);
    }
    
    /**
     * Get url for a single weblog media file on a given weblog.
     */
    public String getMediaFileURL(Weblog weblog, String fileAnchor, boolean absolute) {
        return getWeblogURL(weblog, absolute) + "mediaresource/" + Utilities.encode(fileAnchor);
    }

    /**
     * Get url for a single weblog media file on a given weblog.
     */
    public String getMediaFileThumbnailURL(Weblog weblog, String fileAnchor, boolean absolute) {
        return getMediaFileURL(weblog, fileAnchor, absolute) + "?t=true";
    }

    
    /**
     * Get url for a single weblog entry comments on a given weblog.
     */
    public String getWeblogCommentsURL(Weblog weblog, String entryAnchor, boolean absolute) {
        return getWeblogEntryURL(weblog, entryAnchor, absolute)+"#comments";
    }
    
    
    /**
     * Get url for a single weblog entry comment on a given weblog.
     */
    public String getWeblogCommentURL(Weblog weblog, String entryAnchor, String timeStamp, boolean absolute) {
        return getWeblogEntryURL(weblog, entryAnchor, absolute)+"#comment-"+timeStamp;
    }
    
    
    /**
     * Get url for a collection of entries on a given weblog.
     */
    public String getWeblogCollectionURL(Weblog weblog, String category, String dateString, List tags,
                                         int pageNum, boolean absolute) {
        StringBuilder pathinfo = new StringBuilder();
        pathinfo.append(getWeblogURL(weblog, absolute));

        Map<String, String> params = new HashMap<>();

        if(category != null && dateString == null) {
            pathinfo.append("category/").append(Utilities.encodePath(category));
        } else if(dateString != null && category == null) {
            pathinfo.append("date/").append(dateString);  
        } else if(tags != null && tags.size() > 0) {
            pathinfo.append("tags/").append(Utilities.getEncodedTagsString(tags));
        } else {
            if(dateString != null) {
                params.put("date", dateString);
            }
            if(category != null) {
                params.put("cat", Utilities.encode(category));
            }
        }

        if(pageNum > 0) {
            params.put("page", Integer.toString(pageNum));
        }
        
        return pathinfo.toString() + Utilities.getQueryString(params);
    }
    
    
    /**
     * Get url for a custom page on a given weblog.
     */
    public String getWeblogPageURL(Weblog weblog, String theme, String pageLink, String entryAnchor, String category,
                            String dateString, List tags, int pageNum, boolean absolute) {

        StringBuilder pathinfo = new StringBuilder();
        Map<String, String> params = new HashMap<>();
        
        pathinfo.append(getWeblogURL(weblog, absolute));
        
        if(pageLink != null) {
            pathinfo.append("page/").append(pageLink);
            
            // for custom pages we only allow query params
            if(dateString != null) {
                params.put("date", dateString);
            }
            if(category != null) {
                params.put("cat", Utilities.encode(category));
            }
            if(tags != null && tags.size() > 0) {
                params.put("tags", Utilities.getEncodedTagsString(tags));
            }
            if(pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        } else {
            // if there is no page link then this is just a typical collection url
            return getWeblogCollectionURL(weblog, category, dateString, tags, pageNum, absolute);
        }
        
        return pathinfo.toString() + Utilities.getQueryString(params);
    }

    /**
     * Get url for a feed on a given weblog.
     */
    public String getWeblogFeedURL(Weblog weblog, String type, String format, String category,
                            String term, List tags, boolean excerpts, boolean absolute) {

        String url = getWeblogURL(weblog, absolute) + "feed/" + type + "/" + format;
        
        Map<String, String> params = new HashMap<>();
        if (category != null && category.trim().length() > 0) {
            params.put("cat", Utilities.encode(category));
        }
        if (tags != null && tags.size() > 0) {
          params.put("tags", Utilities.getEncodedTagsString(tags));
        }
        if (term != null && term.trim().length() > 0) {
            params.put("q", Utilities.encode(term.trim()));
        }
        if (excerpts) {
            params.put("excerpts", "true");
        }
        
        return url + Utilities.getQueryString(params);
    }
    
    /**
     * Get url to search endpoint on a given weblog.
     */
    public String getWeblogSearchURL(Weblog weblog, String query, String category, int pageNum, boolean absolute) {
        String url = getWeblogURL(weblog, absolute) + "search";

        Map<String, String> params = new HashMap<>();
        if(query != null) {
            params.put("q", Utilities.encode(query));
            
            // other stuff only makes sense if there is a query
            if(category != null) {
                params.put("cat", Utilities.encode(category));
            }
            if(pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        }
        
        return url + Utilities.getQueryString(params);
    }
    
}
