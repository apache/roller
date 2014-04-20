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
 */

package org.apache.roller.weblogger.planet.business;

import org.apache.roller.planet.business.MultiPlanetURLStrategy;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;


/**
 * Override Planet's URL strategy for use within Roller.
 */
public class PlanetURLStrategy extends MultiPlanetURLStrategy {   
    
    
    /**
     * Get URL configured for Planet.
     * @param planet There's only one planet in Roller, so this is ignored.
     */
    public String getPlanetURL(String planet) {
        StringBuilder url = new StringBuilder();
        url.append(WebloggerRuntimeConfig.getProperty("site.absoluteurl"));
        return url.toString();
    }
    
    
    /**
     * Get URL configured for Planet.
     * @param planet There's only one planet in Roller, so this is ignored.
     * @param group   Handle of planet group (or null for default group).
     * @param pageNum Page number of results to return.
     */
    public String getPlanetGroupURL(String planet, String group, int pageNum) {

        StringBuilder url = new StringBuilder();
        String sep = "?";
        
        url.append(getPlanetURL(planet));
        if (group != null) {
            url.append(sep);
            url.append("group=").append(group);
            sep = "&";
        }
        
        if (pageNum > 0) {
            url.append(sep);
            url.append("page=");
            url.append(pageNum);
        }
        
        return url.toString();
    }
    
    
    /**
     * Get URL of planet group's newsfeed.
     * @param planet There's only one planet in Roller, so this is ignored.
     * @param group Handle of planet group (or null for default group).
     * @param format  Feed format to be returned (ignored, currently only RSS is supported).
     */
    public String getPlanetGroupFeedURL(String planet, String group, String format) {
        
        StringBuilder url = new StringBuilder();
        String sep = "?";
        
        url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        url.append("planetrss");

        if (group != null) {
            url.append(sep);
            url.append("group=").append(group);
        }
        
        return url.toString();
    }
    
    
    /**
     * Currently, Roller doesn't support OPML so this returns null.
     * @param planet There's only one planet in Roller, so this is ignored.
     * @param group Handle of planet group.
     */
    public String getPlanetGroupOpmlURL(String planet, String group) {
        return null;
    }
}
