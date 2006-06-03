/*
 * Created on Jun 15, 2004
 */
package org.roller.util;

import java.util.Map;

// David Flanaghan: http://www.davidflanagan.com/blog/000014.html
public class LRUCache extends java.util.LinkedHashMap 
{
    protected int maxsize;
    public LRUCache(int maxsize) 
    {
        super(maxsize*4/3 + 1, 0.75f, true);
        this.maxsize = maxsize;
    }
    protected boolean removeEldestEntry(Map.Entry eldest) { 
        return size() > this.maxsize; 
    }
}
    
