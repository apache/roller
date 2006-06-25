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
package org.apache.roller.ui.rendering.model;

import java.util.Map;

/**
 * Interface to a paged sub-collection, returned as map of lists of values, 
 * with one map per day. 
 */
public interface RenderDayPager {
        
    /**
     * Current list of values, as map of lists of items, one map per day.
     */
    public Map getCurrentValues();
    
    /**
     * Get URL of next page of collection.
     */
    public String getNextLink();
    
    /**
     * Get name of next page of collection.
     */
    public String getNextLinkName();
    
    /**
     * Get URL of previous page of collection.
     */
    public String getPrevLink();
    
    /**
     * Get name of previous page of collection.
     */
    public String getPrevLinkName();
    
    /**
     * Get URL of next collection after this one.
     */
    public String getNextCollectionLink();
    
    /**
     * Get name of next collection after this one.
     */
    public String getNextCollectionName();
    
    /**
     * Get URL of previous collection before this one.
     */
    public String getPrevCollectionLink();
    
    /**
     * Get name of previous collection before this one.
     */
    public String getPrevCollectionName();    
}
