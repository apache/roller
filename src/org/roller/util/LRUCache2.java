package org.roller.util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * LRU cache with per-entry timeout logic.
 * 
 * @author Dave Johnson
 */
public class LRUCache2
{
    private long timeout;
    private Map cache = null;
    private Environment environment = null;

    /**
     * Create cache.
     * 
     * @param maxsize
     *            Maximum number of entries in cache.
     * @param timeout
     *            Entry timeout in milli-seconds.
     */
    public LRUCache2(int maxsize, long timeout)
    {
        this.environment = new DefaultEnvironment();
        this.timeout = timeout;
        this.cache = new LRULinkedHashMap(maxsize);
    }

    /**
     * Create cache that uses custom environment.
     * 
     * @param maxsize
     *            Maximum number of entries in cache.
     * @param timeout
     *            Entry timeout in milli-seconds.
     */
    public LRUCache2(Environment environment, int maxsize, long timeout)
    {
        this.environment = environment;
        this.timeout = timeout;
        this.cache = new LRULinkedHashMap(maxsize);
    }

    public synchronized void put(Object key, Object value)
    {
        CacheEntry entry = new CacheEntry(value, environment
                        .getCurrentTimeInMillis());
        cache.put(key, entry);
    }

    public Object get(Object key)
    {
        Object value = null;
        CacheEntry entry = null;
        synchronized(this)
        {
            entry = (CacheEntry) cache.get(key);
        }
        if (entry != null)
        {
            if (environment.getCurrentTimeInMillis() - entry.getTimeCached() < timeout)
            {
                value = entry.getValue();
            }
            else
            {
                cache.remove(entry);
            }
        }
        return value;
    }

    public synchronized void purge()
    {
        cache.clear();
    }

    public synchronized void purge(String[] patterns)
    {
        List purgeList = new ArrayList();
        Iterator keys = cache.keySet().iterator();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            for (int i = 0; i < patterns.length; i++)
            {
                if (key.indexOf(patterns[i]) != -1)
                {
                    purgeList.add(key);
                    break;
                }
            }
        }
        Iterator purgeIter = purgeList.iterator();
        while (purgeIter.hasNext())
        {
            String key = (String) purgeIter.next();
            cache.remove(key);
        }
    }

    public int size()
    {
        return cache.size();
    }
    public interface Environment
    {
        public long getCurrentTimeInMillis();
    }
    public static class DefaultEnvironment implements Environment
    {
        public long getCurrentTimeInMillis()
        {
            return System.currentTimeMillis();
        }
    }
    private static class CacheEntry
    {
        private Object value;
        private long timeCached = -1;

        public CacheEntry(Object value, long timeCached)
        {
            this.timeCached = timeCached;
            this.value = value;
        }

        public long getTimeCached()
        {
            return timeCached;
        }

        public Object getValue()
        {
            return value;
        }
    }
    
    // David Flanaghan: http://www.davidflanagan.com/blog/000014.html
    private static class LRULinkedHashMap extends LinkedHashMap
    {
        protected int maxsize;

        public LRULinkedHashMap(int maxsize)
        {
            super(maxsize * 4 / 3 + 1, 0.75f, true);
            this.maxsize = maxsize;
        }

        protected boolean removeEldestEntry(Map.Entry eldest)
        {
            return this.size() > this.maxsize;
        }
    }
}
