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
/*
 * RollerCache.java
 *
 * Created on September 18, 2005, 10:59 AM
 */

package org.roller.presentation.cache;

import java.util.Map;
import java.util.Set;


/**
 * Base interface representing a presentation cache in Roller.
 *
 * @author Allen Gilliland
 */
public interface Cache {
    
    /**
     * put an item in the cache.
     */
    public void put(String key, Object value);
    
    
    /**
     * get an item from the cache.
     */
    public Object get(String key);
    
    
    /**
     * remove an item from the cache.
     */
    public void remove(String key);
    
    
    /**
     * remove a set of items from the cache.
     */
    public void remove(Set keys);
    
    
    /**
     * clear the entire cache.
     */
    public void clear();
    
    
    /**
     * get a list of keys used in the cache.
     */
    public Set keySet();
    
    
    /**
     * get cache stats.
     */
    public Map stats();
    
}
