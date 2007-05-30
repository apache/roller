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
 */

package org.apache.roller.weblogger.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Utilities class for building urls.  This class is meant to centralize the
 * logic behind building urls so that logic isn't duplicated throughout the
 * code.
 */
public final class URLUtilities {
    
    // non-intantiable
    private URLUtilities() {}
    
    
    public static final String getXmlrpcURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-services/xmlrpc");
        
        return url.toString();
    }
    
    public static final String getAtomProtocolURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-services/app");
        
        return url.toString();
    }
    
    
    /**
     * Get root url for a given weblog.  Optionally for a certain locale.
     */
    public static final String getWeblogURL(Weblog weblog,
                                            String locale,
                                            boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/").append(weblog.getHandle()).append("/");
        
        if(locale != null) {
            url.append(locale).append("/");
        }
        
        return url.toString();
    }
    
    
    /**
     * Get url for a single weblog entry on a given weblog.
     */
    public static final String getWeblogEntryURL(Weblog weblog,
                                                 String locale,
                                                 String entryAnchor,
                                                 boolean absolute) {
        
        if(weblog == null || entryAnchor == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        url.append(getWeblogURL(weblog, locale, absolute));
        url.append("entry/").append(encode(entryAnchor));
        
        return url.toString();
    }
    
    
    /**
     * Get url for a single weblog entry comments on a given weblog.
     */
    public static final String getWeblogCommentsURL(Weblog weblog,
                                                    String locale,
                                                    String entryAnchor,
                                                    boolean absolute) {
        
        return getWeblogEntryURL(weblog, locale, entryAnchor, absolute)+"#comments";
    }
    
    
    /**
     * Get url for a single weblog entry comment on a given weblog.
     */
    public static final String getWeblogCommentURL(Weblog weblog,
                                                   String locale,
                                                   String entryAnchor,
                                                   String timeStamp,
                                                   boolean absolute) {
        
        return getWeblogEntryURL(weblog, locale, entryAnchor, absolute)+"#comment-"+timeStamp;
    }
    
    
    /**
     * Get url for a collection of entries on a given weblog.
     */
    public static final String getWeblogCollectionURL(Weblog weblog,
                                                      String locale,
                                                      String category,
                                                      String dateString,
                                                      List tags,
                                                      int pageNum,
                                                      boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer pathinfo = new StringBuffer();
        Map params = new HashMap();
        
        pathinfo.append(getWeblogURL(weblog, locale, absolute));
        
        String cat = null;
        if(category != null && "/".equals(category)) {
            cat = null;
        } else if(category != null && category.startsWith("/")) {
            cat = category.substring(1);
        }
        
        if(cat != null && dateString == null) {
            pathinfo.append("category/").append(encode(cat));
            
        } else if(dateString != null && cat == null) {
            pathinfo.append("date/").append(dateString);  
        
        } else if(tags != null && tags.size() > 0) {
            pathinfo.append("tags/").append(getEncodedTagsString(tags));
        } else {
            if(dateString != null) params.put("date", dateString);
            if(cat != null) params.put("cat", encode(cat));
        }

        if(pageNum > 0) {
            params.put("page", Integer.toString(pageNum));
        }
        
        return pathinfo.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url for a custom page on a given weblog.
     */
    public static final String getWeblogPageURL(Weblog weblog,
                                                String locale,
                                                String pageLink,
                                                String entryAnchor,
                                                String category,
                                                String dateString,
                                                List tags,
                                                int pageNum,
                                                boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer pathinfo = new StringBuffer();
        Map params = new HashMap();
        
        pathinfo.append(getWeblogURL(weblog, locale, absolute));
        
        if(pageLink != null) {
            pathinfo.append("page/").append(pageLink);
            
            // for custom pages we only allow query params
            if(dateString != null) {
                params.put("date", dateString);
            }
            if(category != null) {
                params.put("cat", encode(category));
            }
            if(tags != null && tags.size() > 0) {
                params.put("tags", getEncodedTagsString(tags));
            }
            if(pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        } else {
            // if there is no page link then this is just a typical collection url
            return getWeblogCollectionURL(weblog, locale, category, dateString, tags, pageNum, absolute);
        }
        
        return pathinfo.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url for a feed on a given weblog.
     */
    public static final String getWeblogFeedURL(Weblog weblog,
                                                String locale,
                                                String type,
                                                String format,
                                                String category,
                                                String term,
                                                List tags,
                                                boolean excerpts,
                                                boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        url.append(getWeblogURL(weblog, locale, absolute));
        url.append("feed/").append(type).append("/").append(format);
        
        Map params = new HashMap();
        if(category != null && category.trim().length() > 0) {
            params.put("cat", encode(category));
        }
        if(tags != null && tags.size() > 0) {
          params.put("tags", getEncodedTagsString(tags));
        }
        if(term != null && term.trim().length() > 0) {
            params.put("q", encode(term.trim()));
        }
        if(excerpts) {
            params.put("excerpts", "true");
        }
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url to search endpoint on a given weblog.
     */
    public static final String getWeblogSearchURL(Weblog weblog,
                                                  String locale,
                                                  String query,
                                                  String category,
                                                  int pageNum,
                                                  boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        url.append(getWeblogURL(weblog, locale, absolute));
        url.append("search");
        
        Map params = new HashMap();
        if(query != null) {
            params.put("q", encode(query));
            
            // other stuff only makes sense if there is a query
            if(category != null) {
                params.put("cat", encode(category));
            }
            if(pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        }
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url to a resource on a given weblog.
     */
    public static final String getWeblogResourceURL(Weblog weblog,
                                                    String filePath,
                                                    boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        url.append(getWeblogURL(weblog, null, absolute));
        url.append("resource/");
        
        if(filePath.startsWith("/")) {
            url.append(filePath.substring(1));
        } else {
            url.append(filePath);
        }
        
        return url.toString();
    }
    
    
    /**
     * Get url to rsd file on a given weblog.
     */
    public static final String getWeblogRsdURL(Weblog weblog,
                                               boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        return getWeblogURL(weblog, null, absolute)+"rsd";
    }
    
    
    /**
     * Get url to JSON tags service url, optionally for a given weblog.
     */
    public static final String getWeblogTagsJsonURL(Weblog weblog,
                                                    boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        // json tags service base
        url.append("/roller-services/json/tags/");
        
        // is this for a specific weblog or site-wide?
        if(weblog != null) {
            url.append(weblog.getHandle()).append("/");
        }
        
        return url.toString();
    }
    
    
    /**
     * Url to login page.
     */
    public static final String getLoginURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/login-redirect.rol");
        
        return url.toString();
    }
    
    
    /**
     * Url to logout page.
     */
    public static final String getLogoutURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/logout-redirect.rol");
        
        return url.toString();
    }
    
    
    /**
     * Get a url to add a new weblog entry.
     */
    public static final String getEntryAddURL(String weblogHandle,
                                              boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/entryAdd.rol");
        
        Map params = new HashMap();
        params.put("weblog", weblogHandle);
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get a url to edit a specific weblog entry.
     */
    public static final String getEntryEditURL(String weblogHandle,
                                               String entryId,
                                               boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/entryEdit.rol");
        
        Map params = new HashMap();
        params.put("weblog", weblogHandle);
        params.put("bean.id", entryId);
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get a url to weblog config page.
     */
    public static final String getWeblogConfigURL(String weblogHandle,
                                                  boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/admin/weblogConfig.rol");
        
        Map params = new HashMap();
        params.put("weblog", weblogHandle);
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get root url for a given *preview* weblog.  
     * Optionally for a certain locale.
     */
    public static final String getPreviewWeblogURL(String previewTheme,
                                                   Weblog weblog,
                                                   String locale,
                                                   boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/preview/").append(weblog.getHandle()).append("/");
        
        if(locale != null) {
            url.append(locale).append("/");
        }
        
        Map params = new HashMap();
        if(previewTheme != null) {
            params.put("theme", encode(previewTheme));
        }
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url for a given *preview* weblog entry.  
     * Optionally for a certain locale.
     */
    public static final String getPreviewWeblogEntryURL(String previewAnchor,
                                                        Weblog weblog,
                                                        String locale,
                                                        boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/preview/").append(weblog.getHandle()).append("/");
        
        if(locale != null) {
            url.append(locale).append("/");
        }
        
        Map params = new HashMap();
        if(previewAnchor != null) {
            params.put("previewEntry", encode(previewAnchor));
        }
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get a url to a *preview* resource on a given weblog.
     */
    public static final String getPreviewWeblogResourceURL(String previewTheme,
                                                           Weblog weblog,
                                                           String filePath,
                                                           boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(RollerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(RollerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/previewresource/").append(weblog.getHandle()).append("/");
        
        if(filePath.startsWith("/")) {
            url.append(filePath.substring(1));
        } else {
            url.append(filePath);
        }
        
        Map params = new HashMap();
        if(previewTheme != null && !WeblogTheme.CUSTOM.equals(previewTheme)) {
            params.put("theme", encode(previewTheme));
        }
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Compose a map of key=value params into a query string.
     */
    public static final String getQueryString(Map params) {
        
        if(params == null) {
            return null;
        }
        
        StringBuffer queryString = new StringBuffer();
        
        for(Iterator keys = params.keySet().iterator(); keys.hasNext();) {
            String key = (String) keys.next();
            String value = (String) params.get(key);
            
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            
            queryString.append(key);
            queryString.append("=");
            queryString.append(value);
        }
        
        return queryString.toString();
    }
    
    
    /**
     * URL encode a string using UTF-8.
     */
    public static final String encode(String str) {
        String encodedStr = str;
        try {
            encodedStr = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // ignored
        }
        return encodedStr;
    }
    
    
    /**
     * URL decode a string using UTF-8.
     */
    public static final String decode(String str) {
        String decodedStr = str;
        try {
            decodedStr = URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // ignored
        }
        return decodedStr;
    }
    
    
    public static final String getEncodedTagsString(List tags) {
        StringBuffer tagsString = new StringBuffer();
        if(tags != null && tags.size() > 0) {
            String tag = null;
            Iterator tagsIT = tags.iterator();
            
            // do first tag
            tag = (String) tagsIT.next();
            tagsString.append(encode(tag));
            
            // do rest of tags, joining them with a '+'
            while(tagsIT.hasNext()) {
                tag = (String) tagsIT.next();
                tagsString.append("+");
                tagsString.append(encode(tag));
            }
        }
        return tagsString.toString();
    }
}
