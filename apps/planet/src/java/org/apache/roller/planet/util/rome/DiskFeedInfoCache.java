/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.planet.util.rome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import org.apache.roller.planet.util.Utilities;

/**
 * @author David M. Johnson
 */
public class DiskFeedInfoCache implements FeedFetcherCache
{
    private static Log logger = 
        LogFactory.getFactory().getInstance(DiskFeedInfoCache.class);
    
    protected String cachePath = null;
    public DiskFeedInfoCache(String cachePath)
    {
        this.cachePath = cachePath;
    }
    public SyndFeedInfo getFeedInfo(URL url)
    {
        SyndFeedInfo info = null;
        String fileName = cachePath + File.separator + "feed_" 
                + Utilities.replaceNonAlphanumeric(url.toString(),'_').trim();        
        FileInputStream fis;
        try
        {
            fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            info = (SyndFeedInfo)ois.readObject();
            fis.close();
        }
        catch (FileNotFoundException fnfe)
        {
            logger.debug("Cache miss for " + url.toString());
        }
        catch (ClassNotFoundException cnfe)
        {
            // Error writing to cahce is fatal
            throw new RuntimeException("Attempting to read from cache", cnfe);
        }
        catch (IOException fnfe)
        {
            // Error writing to cahce is fatal
            throw new RuntimeException("Attempting to read from cache", fnfe);
        }
        if (info == null) logger.debug("Cache MISS!");
        return info;
    }

    public void setFeedInfo(URL url, SyndFeedInfo feedInfo)
    {
        String fileName = cachePath + File.separator + "feed_" 
                + Utilities.replaceNonAlphanumeric(url.toString(),'_').trim();  
        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(feedInfo);
            fos.flush();
            fos.close();
        }
        catch (Exception e)
        {
            // Error writing to cahce is fatal
            throw new RuntimeException("Attempting to write to cache", e);
        }
    }
}
