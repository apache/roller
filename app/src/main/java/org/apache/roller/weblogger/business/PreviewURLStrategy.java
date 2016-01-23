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

package org.apache.roller.weblogger.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;


/**
 * A URLStrategy used by the preview rendering system.
 */
public class PreviewURLStrategy extends MultiWeblogURLStrategy {
    
    private final String previewTheme;
    private static final String PREVIEW_URL_SEGMENT = "/roller-ui/authoring/preview/";

    public PreviewURLStrategy() {
        previewTheme = null;
    }

    public PreviewURLStrategy(String theme) {
        previewTheme = theme;
    }
    
    /**
     * Get root url for a given *preview* weblog.  
     */
    @Override
    public String getWeblogURL(Weblog weblog, boolean absolute) {
        String url = getRootURL(absolute) + PREVIEW_URL_SEGMENT + weblog.getHandle() + "/";
        Map<String, String> params = new HashMap<>();
        if (previewTheme != null) {
            params.put("theme", Utilities.encode(previewTheme));
        }
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get url for a given *preview* weblog entry.
     */
    @Override
    public String getWeblogEntryURL(Weblog weblog, String previewAnchor, boolean absolute) {
        String url = getRootURL(absolute) + PREVIEW_URL_SEGMENT + weblog.getHandle() + "/";
        
        Map<String, String> params = new HashMap<>();
        if (previewTheme != null) {
            params.put("theme", Utilities.encode(previewTheme));
        }
        if (previewAnchor != null) {
            params.put("previewEntry", Utilities.encode(previewAnchor));
        }
        
        return url + Utilities.getQueryString(params);
    }

    /**
     * Get url for a collection of entries on a given weblog.
     */
    public String getWeblogCollectionURL(Weblog weblog, String category, String dateString, List tags,
                                  int pageNum, boolean absolute) {
        
        String pathinfo = getRootURL(absolute) + PREVIEW_URL_SEGMENT + weblog.getHandle() + "/";
        Map<String, String> params = new HashMap<>();
        if(category != null && dateString == null) {
            pathinfo += "category/" + Utilities.encodePath(category);
        } else if(dateString != null && category == null) {
            pathinfo += "date/" + dateString;
        } else if(tags != null && tags.size() > 0) {
            pathinfo += "tags/" + Utilities.getEncodedTagsString(tags);
        } else {
            if (dateString != null) {
                params.put("date", dateString);
            }
            if (category != null) {
                params.put("cat", Utilities.encode(category));
            }
        }

        if(pageNum > 0) {
            params.put("page", Integer.toString(pageNum));
        }
        
        if(previewTheme != null) {
            params.put("theme", Utilities.encode(previewTheme));
        }

        return pathinfo + Utilities.getQueryString(params);
    }

    /**
     * Get url for a custom page on a given weblog.
     */
    @Override
    public String getWeblogPageURL(Weblog weblog, String theme, String pageLink, String entryAnchor, String category,
                            String dateString, List tags, int pageNum, boolean absolute) {

        String pathinfo = getRootURL(absolute) + PREVIEW_URL_SEGMENT + weblog.getHandle() + "/";
        Map<String, String> params = new HashMap<>();

        if (previewTheme != null) {
            params.put("theme", Utilities.encode(previewTheme));
        } else if (theme != null) {
            params.put("theme", Utilities.encode(theme));
        }
        
        if(pageLink != null) {
            pathinfo += "page/" + pageLink;
            
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
        
        return pathinfo + Utilities.getQueryString(params);
    }

}
