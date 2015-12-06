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
package org.apache.roller.weblogger.util;

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.ExpiringLRUCacheImpl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class ExpiringLRUCacheImplTest extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testLRU() {
        // Create cache with 3 item limit and 15 second timeout
        Cache cache = new ExpiringLRUCacheImpl("test", 3, 15000);
        
        cache.put("key1", "string1");
        cache.put("key2", "string2");
        cache.put("key3", "string3");
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
        
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        
        // accessing key1 and key2 will make key3 LRU
        cache.get("key1");
        cache.get("key2");
        
        // adding a forth key will push out the LRU entry
        cache.put("key4", "string4");
        assertNull(cache.get("key3"));
    }

    @Test
    public void testRemove() {
        // Create cache with 100 item limit and 15 second timeout
        Cache cache = new ExpiringLRUCacheImpl("test", 100, 15000);

        cache.put("key1", "string1");
        cache.put("key2", "string2");
        cache.put("key3", "string3");
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
        
        cache.remove("key1");
        cache.remove("key2");
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));

        cache.clear();
        assertNull(cache.get("key3"));
    }
    
}
