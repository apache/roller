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

package org.apache.roller.planet.business;

import java.util.Map;


/**
 * An interface representing the Roller Planet url strategy.
 *
 * Implementations of this interface provide methods which can be used to form
 * all of the public urls used by Roller Planet.
 */
public interface URLStrategy {
    
    /**
     * Get root url for a given planet.
     *
     * @param planetHandle The 'handle' of the planet being referenced.
     * @returns The url to the planet.
     */
    public String getPlanetURL(String planetHandle);
    
    
    /**
     * Get url for a specific aggregation group of a planet.
     *
     * @param planetHandle The 'handle' of the planet.
     * @param groupHandle The 'hadle' of the planet group.
     * @param pageNum The page number.
     * @returns The url to the planet group.
     */
    public String getPlanetGroupURL(String planetHandle, String groupHandle, int pageNum);
    
    
    /**
     * Get url to a feed for a specific group of a planet, in the given format.
     *
     * @param planetHandle The 'handle' of the planet being referenced.
     * @param groupHandle The 'hadle' of the planet group being referenced.
     * @param format The feed format being requested.
     * @returns The url to the feed.
     */
    public String getPlanetGroupFeedURL(String planetHandle, String groupHandle, String format);
    
    
    /**
     * Get url to a opml file for a specific group of a planet.
     *
     * @param planetHandle The 'handle' of the planet being referenced.
     * @param groupHandle The 'hadle' of the planet group being referenced.
     * @returns The url to the feed.
     */
    public String getPlanetGroupOpmlURL(String planetHandle, String groupHandle);
    
    
    /**
     * Compose a map of key=value params into a query string.
     */
    public String getQueryString(Map params);
    
    
    /**
     * URL encode a string.
     */
    public String encode(String str);
    
    
    /**
     * URL decode a string.
     */
    public String decode(String str);
    
}
