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

package org.apache.roller.planet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.config.PlanetRuntimeConfig;


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
    public static final String getPlanetURL(String planet) {
        
        if(planet == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        url.append(PlanetRuntimeConfig.getProperty("site.absoluteurl"));
        url.append("/").append(planet).append("/");
        
        return url.toString();
    }
    
    
    /**
     * Get url for a single weblog entry on a given weblog.
     */
    public static final String getPlanetGroupURL(String planet,
                                                 String group) {
        
        if(planet == null || group == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        url.append(getPlanetURL(planet));
        url.append("group/").append(group).append("/");
        
        return url.toString();
    }
    
    
    /**
     * Get url for a feed on a given weblog.
     */
    public static final String getPlanetGroupFeedURL(String planet,
                                                     String group,
                                                     String format) {
        
        if(planet == null || group == null) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        
        url.append(getPlanetGroupURL(planet, group));
        url.append("feed/").append(format);
        
        return url.toString();
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
