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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Some utility methods used when dealing with urls.
 */
public final class URLUtilities {
    
    // non-instantiable
    private URLUtilities() {}
    
    
    /**
     * Compose a map of key=value params into a query string.
     */
    public static String getQueryString(Map<String, String> params) {
        
        if(params == null) {
            return null;
        }
        
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {

            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }

            queryString.append(entry.getKey());
            queryString.append("=");
            queryString.append(entry.getValue());
        }

        return queryString.toString();
    }
    
    
    /**
     * URL encode a string using UTF-8.
     */
    public static String encode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
    
    
    /**
     * URL decode a string using UTF-8.
     */
    public static String decode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }
    
    
    public static String getEncodedTagsString(List<String> tags) {
        StringBuilder tagsString = new StringBuilder();
        if(tags != null && !tags.isEmpty()) {
            String tag;
            Iterator<String> tagsIT = tags.iterator();
            
            // do first tag
            tag = tagsIT.next();
            tagsString.append(encode(tag));
            
            // do rest of tags, joining them with a '+'
            while(tagsIT.hasNext()) {
                tag = tagsIT.next();
                tagsString.append("+");
                tagsString.append(encode(tag));
            }
        }
        return tagsString.toString();
    }
    
        
    /**
     * URL encode a path string using UTF-8. The path separator '/' will not be encoded
     */
    public static String encodePath(String path) {
        int i = path.indexOf('/');
        StringBuilder sb = new StringBuilder();
        while (i != -1) {
            sb.append(encode(path.substring(0, i))).append('/');
            path = path.substring(i + 1);
            i = path.indexOf('/');
        }
        sb.append(encode(path));
        return sb.toString();
    }
}


