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
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;


/**
 * @author David M Johnson
 */
public class DiskFeedInfoCacheTest extends TestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DiskFeedInfoCacheTest.class);
    }
    
    public void testCache() throws Exception {
        URL url = new URL("http://cnn.com");
        SyndFeedInfo info = new SyndFeedInfo();
        info.setUrl(url);
        
        String buildDir = System.getProperty("project.build.directory");
        assertNotNull("project.build.directory not null", buildDir);
        assertTrue("project.build.directory not zero length", buildDir.trim().length() > 0);
        if (!buildDir.startsWith("/")) buildDir = "..";
        File file = new File(buildDir);
        
        assertTrue("buildDir exists", file.exists());
        assertTrue("buildDir is directory", file.isDirectory());
        
        File cacheDir = new File(buildDir + File.separator + "planet-cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        
        DiskFeedInfoCache cache =
                new DiskFeedInfoCache(cacheDir.getAbsolutePath());
        cache.setFeedInfo(info.getUrl(), info);
        
        SyndFeedInfo info2 = cache.getFeedInfo(url);
        assertNotNull(info2);
        assertEquals(url, info2.getUrl());
    }
    
    public static Test suite() {
        return new TestSuite(DiskFeedInfoCacheTest.class);
        
    }
    
}
