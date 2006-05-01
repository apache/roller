/*
 * Created on Jun 15, 2004
 */
package org.apache.roller.util;

import org.apache.roller.presentation.bookmarks.BookmarksActionTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author dmj
 */
public class LRUCache2Test extends TestCase
{
    /** 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        // TODO Auto-generated method stub
        super.setUp();
    }
    
    public void testTimeout() 
    {
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
    
    public void testLRU() 
    {
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
    
    public void testPurge() 
    {
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
    protected void tearDown() throws Exception
    {
        // TODO Auto-generated method stub
        super.tearDown();
    }
    
    public static class TestEnvironment implements LRUCache2.Environment 
	{
    	public long time = 0;
		public long getCurrentTimeInMillis() 
		{
			return time;
		}
	}

    public static Test suite() 
    {
        return new TestSuite(LRUCache2Test.class);
    }
}
