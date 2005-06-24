/*
 * Created on Jun 15, 2004
 */
package org.roller.presentation.pagecache.rollercache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.RollerConfig;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.LanguageUtil;
import org.roller.presentation.pagecache.FilterHandler;
import org.roller.util.LRUCache;

/**
 * Page cache implementation that uses a simple LRUCache. Can be configured 
 * using filter configuration parameters:
 * <ul>
 * <li><b>size</b>: number of pages to keep in cache. Once cache reaches
 * this size, each new cache entry will push out the LRU cache entry.</li>
 * 
 * <li><b>timeoutInterval</b>: interval to timeout pages in seconds
 * (default is 1800 seconds). Sites with a large number of users, and thus a 
 * lot of cache churn which makes this check unnecessary, may wish to  set this
 *  to -1 to disable timeout checking.</li>
 * 
 * <li><b>timeoutRatio</b>: portion of old pages to expire on timeout 
 * interval where 1.0 is 100% (default is 1.0). This only applies if the 
 * timeoutInterval is set to something other than -1.</li>
 * </ul> 
 * @author David M Johnson
 */
public class LRUCacheHandler implements FilterHandler
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(LRUCacheHandler.class);

    private Map mPageCache = null;
    
    private String mName = null;
    
    /** Timeout interval: how often to run timeout task (default 30 mintes) */
    private long mTimeoutInterval = 30 * 60 * 1000; // milliseconds
    
    /** Timeout ratio: % of cache to expire on each timeout (default 1.0) */
    private float mTimeoutRatio = 1.0F;
    
    // Statistics
    private int misses = 0;
    private int hits = 0;
    
    private final static String FILE_SEPARATOR = "/";
    private final static char FILE_SEPARATOR_CHAR = FILE_SEPARATOR.charAt(0);
    private final static short AVERAGE_KEY_LENGTH = 30;
    private static final String m_strBase64Chars = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    

    public LRUCacheHandler(FilterConfig config)
    {      
        mName = config.getFilterName();
        mLogger.info("Initializing for: " + mName);
        
        String cacheSize = RollerConfig.getProperty("cache.filter.page.size");
        String cacheTimeout = RollerConfig.getProperty("cache.filter.page.timeout");
        
        int size = 200;
        try
        {
            size = Integer.parseInt(cacheSize);
        }
        catch (Exception e)
        {
            mLogger.warn(config.getFilterName() 
                + "Can't read cache size parameter, using default...");
        }
        mLogger.info(mName + " size=" + size);
        mPageCache = Collections.synchronizedMap(new LRUCache(size));

        long intervalSeconds = mTimeoutInterval / 1000L;
        try
        {
            mTimeoutInterval = 1000L * Long.parseLong(cacheTimeout);
            
            if (mTimeoutInterval == -1)
            {
                mLogger.info(config.getFilterName() 
                   + "timeoutInterval of -1: timeouts are disabled");
            }
            else if (mTimeoutInterval < (30 * 1000))
            {
                mTimeoutInterval = 30 * 1000;
                mLogger.warn(config.getFilterName() 
                   + "timeoutInterval cannot be less than 30 seconds");
            }
        }
        catch (Exception e)
        {
            mLogger.warn(config.getFilterName() 
                + "Can't read timeoutInterval parameter, disabling timeout.");
            mTimeoutInterval = -1;
        }
        mLogger.info(mName + " timeoutInterval=" + intervalSeconds);
        
        try
        {
            mTimeoutRatio = Float.parseFloat(
                config.getInitParameter("timeoutRatio"));
        }
        catch (Exception e)
        {
            mLogger.warn(config.getFilterName() 
                + "Can't read timeoutRatio parameter, using default...");
        }
        mLogger.info(mName + " timeoutRatio=" + mTimeoutRatio);
        
        if (mTimeoutInterval != -1 && mTimeoutRatio != 0.0)
        {
            Timer timer = new Timer();            
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    timeoutCache();
                }
            }, mTimeoutInterval, mTimeoutInterval);        
        }
    }
    
    /**
     * @see org.roller.presentation.pagecache.FilterHandler#destroy()
     */
    public void destroy()
    {
    }

    /**
     * @see org.roller.presentation.pagecache.FilterHandler#doFilter(
     *      javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res,
                    FilterChain chain) throws ServletException, IOException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // get locale
        Locale locale = LanguageUtil.getViewLocale(request);
        
        // generate language-sensitive cache-key
        String generatedKey = null;
        if (locale != null)
        {
            generatedKey = generateEntryKey(null, 
                      request, 1, locale.getLanguage());
        }
        else
        {
            generatedKey = generateEntryKey(null, 
                      request, 1, null);
        }
        
        // Add authenticated user name, if there is one, to cache key
        java.security.Principal prince = request.getUserPrincipal();        
        StringBuffer keyb = new StringBuffer();
        keyb.append(generatedKey);
        if (prince != null)
        {
            keyb.append("_");
            keyb.append(prince);
        }
        String key = keyb.toString();
                
        ResponseContent respContent = (ResponseContent)getFromCache(key);
        if (respContent == null) 
        {
            try
            {
                CacheHttpServletResponseWrapper cacheResponse = 
                    new CacheHttpServletResponseWrapper(response);
                chain.doFilter(request, cacheResponse);
                
                // Store as the cache content the result of the response
                // if no exception was noted by content generator.
                if (request.getAttribute("DisplayException") == null)
                {
                    ResponseContent rc = cacheResponse.getContent();
                    putToCache(key, rc);
                }
                else
                {
                    StringBuffer sb = new StringBuffer();
                    sb.append("Display exception, cache, key=");
                    sb.append(key);
                    mLogger.error(sb.toString());
                }
            }
            catch (java.net.SocketException se)
            {
                // ignore socket exceptions
            }
            catch (Exception e)
            {
                // something unexpected and bad happened
                StringBuffer sb = new StringBuffer();
                sb.append("Error rendering page, key=");
                sb.append(key);
                mLogger.error(sb.toString());
            }           
        }
        else
        {
            try
            {
                respContent.writeTo(response);
            }
            catch (java.net.SocketException se)
            {
                // ignore socket exceptions
            }
            catch (Exception e)
            {
                if (mLogger.isDebugEnabled())
                {
                    StringBuffer sb = new StringBuffer();
                    sb.append("Probably a client abort exception, key=");
                    sb.append(key);
                    mLogger.error(sb.toString());
                }
            }
            
        }
    }

    /**
     * Purge entire cache.
     */
    public synchronized void flushCache(HttpServletRequest req)
    {
        mPageCache.clear();
    }

    /**
     * Purge user's entries from cache.
     */
    public synchronized void removeFromCache(
            HttpServletRequest req, WebsiteData website)
    {
        // TODO: can we make this a little more precise, perhaps via regex?
        String rssString = "/rss/" + website.getHandle(); // user's pages
        String pageString = "/page/" + website.getHandle(); // user's RSS feeds
        String mainRssString = "/rss_"; // main RSS feed
        List purgeList = new ArrayList();
        
        Iterator keys = mPageCache.keySet().iterator();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            
            if (key.indexOf(rssString)!=-1 
                    || key.indexOf(pageString)!=-1 || key.indexOf(mainRssString)!=-1) 
            {
                purgeList.add(key);
            }
        }
        
        Iterator purgeIter = purgeList.iterator();
        while (purgeIter.hasNext())
        {
            String key = (String) purgeIter.next();
            mPageCache.remove(key);
        }
        
        if (mLogger.isDebugEnabled())
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Purged, count=");
            sb.append(purgeList.size());
            sb.append(", website=");
            sb.append(website.getHandle());
            mLogger.debug(sb.toString());
        }        
    }
    
    public synchronized void timeoutCache() 
    {
        if (mTimeoutRatio == 1.0)
        {
            mLogger.debug("Timing out whole cache: " + mName);
            mPageCache.clear();   
        }
        else 
        {
            int numToTimeout = (int)(mTimeoutRatio * mPageCache.size());
            mLogger.debug(
                "Timing out " + numToTimeout + " of " + mPageCache.size()
                + " entries from cache: " + mName);
            ArrayList allKeys = new ArrayList(mPageCache.keySet());
            for (int i=numToTimeout; i>0; i--) 
            {
                mPageCache.remove(allKeys.get(i));
            }
        }
    }
    
    /** 
     * Get from cache. Synchronized because "In access-ordered linked hash 
     * maps, merely querying the map with get is a structural modification" 
     */
    public synchronized Object getFromCache(String key) 
    {
        Object entry = mPageCache.get(key);
        
        if (entry != null && mLogger.isDebugEnabled())
        {
            hits++;
        }
        return entry;
    }

    public synchronized void putToCache(String key, Object entry) 
    {
        mPageCache.put(key, entry);
        if (mLogger.isDebugEnabled())
        {
            misses++;
            
            StringBuffer sb = new StringBuffer();
            sb.append("Missed, cache size=");
            sb.append(mPageCache.size());
            sb.append(", hits=");
            sb.append(hits);
            sb.append(", misses=");
            sb.append(misses);
            sb.append(", key=");
            sb.append(key);
            mLogger.debug(sb.toString());
        }
    }

    public String generateEntryKey(String key, 
                       HttpServletRequest request, int scope, String language)
    {
        StringBuffer cBuffer = new StringBuffer(AVERAGE_KEY_LENGTH);
        // Append the language if available
        if (language != null)
        {
            cBuffer.append(FILE_SEPARATOR).append(language);
        }
        
        //cBuffer.append(FILE_SEPARATOR).append(request.getServerName());
        
        if (key != null)
        {
            cBuffer.append(FILE_SEPARATOR).append(key);
        }
        else
        {
            String generatedKey = request.getRequestURI();
            if (generatedKey.charAt(0) != FILE_SEPARATOR_CHAR)
            {
                cBuffer.append(FILE_SEPARATOR_CHAR);
            }
            cBuffer.append(generatedKey);
            cBuffer.append("_").append(request.getMethod()).append("_");
            generatedKey = getSortedQueryString(request);
            if (generatedKey != null)
            {
                try
                {
                    java.security.MessageDigest digest = 
                        java.security.MessageDigest.getInstance("MD5");
                    byte[] b = digest.digest(generatedKey.getBytes());
                    cBuffer.append("_");
                    // Base64 encoding allows for unwanted slash characters.
                    cBuffer.append(toBase64(b).replace('/', '_'));
                }
                catch (Exception e)
                {
                    // Ignore query string
                }
            }
        }
        return cBuffer.toString();
    }

    protected String getSortedQueryString(HttpServletRequest request)
    {
        Map paramMap = request.getParameterMap();
        if (paramMap.isEmpty())
        {
            return null;
        }
        Set paramSet = new TreeMap(paramMap).entrySet();
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (Iterator it = paramSet.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            String[] values = (String[]) entry.getValue();
            for (int i = 0; i < values.length; i++)
            {
                String key = (String) entry.getKey();
                if ((key.length() != 10) || !"jsessionid".equals(key))
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        buf.append('&');
                    }
                    buf.append(key).append('=').append(values[i]);
                }
            }
        }
        // We get a 0 length buffer if the only parameter was a jsessionid
        if (buf.length() == 0)
        {
            return null;
        }
        else
        {
            return buf.toString();
        }
    }

    /**
     * Convert a byte array into a Base64 string (as used in mime formats)
     */
    private static String toBase64(byte[] aValue)
    {
        int byte1;
        int byte2;
        int byte3;
        int iByteLen = aValue.length;
        StringBuffer tt = new StringBuffer();
        for (int i = 0; i < iByteLen; i += 3)
        {
            boolean bByte2 = (i + 1) < iByteLen;
            boolean bByte3 = (i + 2) < iByteLen;
            byte1 = aValue[i] & 0xFF;
            byte2 = (bByte2) ? (aValue[i + 1] & 0xFF) : 0;
            byte3 = (bByte3) ? (aValue[i + 2] & 0xFF) : 0;
            tt.append(m_strBase64Chars.charAt(byte1 / 4));
            tt.append(m_strBase64Chars.charAt((byte2 / 16)
                            + ((byte1 & 0x3) * 16)));
            tt.append(((bByte2) ? m_strBase64Chars.charAt((byte3 / 64)
                            + ((byte2 & 0xF) * 4)) : '='));
            tt.append(((bByte3) ? m_strBase64Chars.charAt(byte3 & 0x3F) : '='));
        }
        return tt.toString();
    }
}
