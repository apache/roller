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

import com.rometools.fetcher.impl.DiskFeedInfoCache;
import com.rometools.fetcher.impl.SyndFeedInfo;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David M Johnson
 */
public class DiskFeedInfoCacheTest  {
    
    @Test
    public void testCache() throws Exception {
        URL url = new URL("http://cnn.com");
        SyndFeedInfo info = new SyndFeedInfo();
        info.setUrl(url);
        
        String testPlanetCache = WebloggerConfig.getProperty("cache.dir");
        assertNotNull("testPlanetCache not null", testPlanetCache);
        assertTrue( testPlanetCache.trim().length() > 0, "testPlanetCache not zero length");
        
        File cacheDir = new File(testPlanetCache);
        if (!cacheDir.exists()) cacheDir.mkdirs();
        
        DiskFeedInfoCache cache =
                new DiskFeedInfoCache(WebloggerConfig.getProperty("cache.dir"));
        cache.setFeedInfo(info.getUrl(), info);
        
        SyndFeedInfo info2 = cache.getFeedInfo(url);
        assertNotNull(info2);
        assertEquals(url, info2.getUrl());
    }
    
}
