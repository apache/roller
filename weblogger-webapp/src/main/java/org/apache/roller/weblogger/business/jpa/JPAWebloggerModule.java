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

package org.apache.roller.weblogger.business.jpa;

import com.google.inject.Binder;
import com.google.inject.Module;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import org.apache.roller.planet.business.MultiPlanetURLStrategy;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.PlanetURLStrategy;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.business.fetcher.RomeFeedFetcher;
import org.apache.roller.planet.business.jpa.JPAPlanetImpl;
import org.apache.roller.planet.business.jpa.JPAPlanetManagerImpl;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.FileContentManager;
import org.apache.roller.weblogger.business.FileContentManagerImpl;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.MultiWeblogURLStrategy;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.PluginManagerImpl;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManager;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManagerImpl;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.themes.ThemeManagerImpl;


/**
 * Guice module for configuring JPA as Weblogger-backend.
 */
public class JPAWebloggerModule implements Module {

    public void configure(Binder binder) {

        binder.bind(Weblogger.class).to(JPAWebloggerImpl.class);
        
        binder.bind(JPAPersistenceStrategy.class);       
        
        binder.bind(AutoPingManager.class).to(     JPAAutoPingManagerImpl.class);   
        binder.bind(BookmarkManager.class).to(     JPABookmarkManagerImpl.class);  
        binder.bind(PingQueueManager.class).to(    JPAPingQueueManagerImpl.class);   
        binder.bind(PingTargetManager.class).to(   JPAPingTargetManagerImpl.class); 
        binder.bind(PropertiesManager.class).to(   JPAPropertiesManagerImpl.class);   
        binder.bind(RefererManager.class).to(      JPARefererManagerImpl.class);
        binder.bind(ThreadManager.class).to(       JPAThreadManagerImpl.class);  
        binder.bind(UserManager.class).to(         JPAUserManagerImpl.class);   
        binder.bind(WeblogManager.class).to(       JPAWeblogManagerImpl.class);   
        binder.bind(WeblogEntryManager.class).to(  JPAWeblogEntryManagerImpl.class);   
        binder.bind(OAuthManager.class).to(        JPAOAuthManagerImpl.class);

        binder.bind(OAuthValidator.class).to(      SimpleOAuthValidator.class);
                
        binder.bind(ReferrerQueueManager.class).to(ReferrerQueueManagerImpl.class);  
        binder.bind(MediaFileManager.class).to(    JPAMediaFileManagerImpl.class);
        binder.bind(FileContentManager.class).to(  FileContentManagerImpl.class);
        binder.bind(IndexManager.class).to(        IndexManagerImpl.class);
        binder.bind(PluginManager.class).to(       PluginManagerImpl.class);    
        binder.bind(ThemeManager.class).to(        ThemeManagerImpl.class);
        
        binder.bind(URLStrategy.class).to(         MultiWeblogURLStrategy.class);
        binder.bind(PlanetURLStrategy.class).to(   MultiPlanetURLStrategy.class);
		binder.bind(Planet.class).to(              JPAPlanetImpl.class);
        binder.bind(PlanetManager.class).to(       JPAPlanetManagerImpl.class);   
        binder.bind(FeedFetcher.class).to(         RomeFeedFetcher.class);
    }
    
}
