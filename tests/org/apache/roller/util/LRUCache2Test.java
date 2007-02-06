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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test LRUCache2.
 */
public class LRUCache2Test extends TestCase {
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
    }
    
    public void testTimeout() {
        // Create cache with 100 item limit and 15 second timeout
        TestEnvironment env = new TestEnvironment();
        LRUCache2 cache = new LRUCache2(env, 100, 15000);
        
        env.time = 1000;
        cache.put("key1", "string1");
        cache.put("key2", "string2");
        cache.put("key3", "string3");
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
        
        env.time = 16000;
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
        assertNull(cache.get("key3"));
    }
    
    public void testLRU() {
        // Create cache with 3 item limit and 15 second timeout
        TestEnvironment env = new TestEnvironment();
        LRUCache2 cache = new LRUCache2(env, 3, 15000);
        
        env.time = 1000;
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
    
    public void testPurge() {
        // Create cache with 100 item limit and 15 second timeout
        TestEnvironment env = new TestEnvironment();
        LRUCache2 cache = new LRUCache2(env, 100, 15000);
        
        env.time = 1000;
        cache.put("key1", "string1");
        cache.put("key2", "string2");
        cache.put("key3", "string3");
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
        
        cache.purge(new String[] {"key1", "key2"});
        assertEquals(1, cache.size());
        
        cache.purge();
        assertEquals(0, cache.size());
    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }
    
    public static class TestEnvironment implements LRUCache2.Environment {
        public long time = 0;
        public long getCurrentTimeInMillis() {
            return time;
        }
    }
    
    public static Test suite() {
        return new TestSuite(LRUCache2Test.class);
    }
    
}
