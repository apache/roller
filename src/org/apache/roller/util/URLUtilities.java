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

package org.apache.roller.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.WebsiteData;


/**
 * Utilities class for building urls.  This class is meant to centralize the
 * logic behind building urls so that logic isn't duplicated throughout the
 * code.
 */
public final class URLUtilities {
    
    // non-intantiable
    private URLUtilities() {}
    
    
    /**
     * Get root url for a given weblog.  Optionally for a certain locale.
     */
    public static final String getWeblogURL(WebsiteData weblog,
                                            String locale,
                                            boolean absolute) {
        
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
    public static final String getWeblogEntryURL(WebsiteData weblog,
                                                 String locale,
                                                 String entryAnchor,
                                                 boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        url.append(getWeblogURL(weblog, locale, absolute));
        url.append("entry/").append(encode(entryAnchor));
        
        return url.toString();
    }
    
    
    /**
     * Get url for a single weblog entry comments on a given weblog.
     */
    public static final String getWeblogCommentsURL(WebsiteData weblog,
                                                    String locale,
                                                    String entryAnchor,
                                                    boolean absolute) {
        
        return getWeblogEntryURL(weblog, locale, entryAnchor, absolute)+"#comments";
    }
    
    
    /**
     * Get url for a single weblog entry comment on a given weblog.
     */
    public static final String getWeblogCommentURL(WebsiteData weblog,
                                                   String locale,
                                                   String entryAnchor,
                                                   String timeStamp,
                                                   boolean absolute) {
        
        return getWeblogEntryURL(weblog, locale, entryAnchor, absolute)+"#comment-"+timeStamp;
    }
    
    
    /**
     * Get url for a collection of entries on a given weblog.
     */
    public static final String getWeblogCollectionURL(WebsiteData weblog,
                                                      String locale,
                                                      String category,
                                                      String dateString,
                                                      int pageNum,
                                                      boolean absolute) {
        
        StringBuffer pathinfo = new StringBuffer();
        Map params = new HashMap();
        
        pathinfo.append(getWeblogURL(weblog, locale, absolute));
        
        if(category != null && dateString == null) {
            String cat = category;
            if(category.startsWith("/")) {
                cat = category.substring(1);
            }
            
            pathinfo.append("category/").append(encode(cat));   
            
        } else if(dateString != null && category == null) {
            pathinfo.append("date/").append(dateString);  
            
        } else {
            if(dateString != null) params.put("date", dateString);
            if(category != null) params.put("cat", encode(category));
        }

        if(pageNum > 0) {
            params.put("page", Integer.toString(pageNum));
        }
        
        return pathinfo.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url for a custom page on a given weblog.
     */
    public static final String getWeblogPageURL(WebsiteData weblog,
                                                String locale,
                                                String pageLink,
                                                String entryAnchor,
                                                String category,
                                                String dateString,
                                                int pageNum,
                                                boolean absolute) {
        
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
            if(pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        } else {
            // if there is no page link then this is just a typical collection url
            return getWeblogCollectionURL(weblog, locale, category, dateString, pageNum, absolute);
        }
        
        return pathinfo.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url for a feed on a given weblog.
     */
    public static final String getWeblogFeedURL(WebsiteData weblog,
                                                String locale,
                                                String type,
                                                String format,
                                                String category,
                                                boolean excerpts,
                                                boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        url.append(getWeblogURL(weblog, locale, absolute));
        url.append("feed/").append(type).append("/").append(format);
        
        Map params = new HashMap();
        if(category != null) {
            params.put("cat", encode(category));
        }
        if(excerpts) {
            params.put("excerpts", Boolean.TRUE);
        }
        
        return url.toString() + getQueryString(params);
    }
    
    
    /**
     * Get url to search endpoint on a given weblog.
     */
    public static final String getWeblogSearchURL(WebsiteData weblog,
                                                  String locale,
                                                  String query,
                                                  String category,
                                                  int pageNum,
                                                  boolean absolute) {
        
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
    public static final String getWeblogResourceURL(WebsiteData weblog,
                                                    String filePath,
                                                    boolean absolute) {
        
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
    public static final String getWeblogRsdURL(WebsiteData weblog,
                                               boolean absolute) {
        return getWeblogURL(weblog, null, absolute)+"rsd";
    }
    
    
    /**
     * Compose a map of key=value params into a query string.
     */
    public static final String getQueryString(Map params) {
        
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
    
}
