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

package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.jpa.JPAOAuthManagerImpl;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;


/**
 * 
 * The main entry point interface of the Weblogger business tier.
 */
public interface Weblogger {

    /**
     * 
     * Get UserManager associated with this Weblogger instance.
     */
    public UserManager getUserManager();
    
    
    /**
     * 
     * Get BookmarkManager associated with this Weblogger instance.
     */
    public BookmarkManager getBookmarkManager();
    
    
    /**
     *
     * Get OAuthManager associated with this Weblogger instance.
     */
    public OAuthManager getOAuthManager();


    /**
     * 
     * Get WeblogManager associated with this Weblogger instance.
     */
    public WeblogManager getWeblogManager();
    
    /**
     * 
     * Get WeblogManager associated with this Weblogger instance.
     */
    public WeblogEntryManager getWeblogEntryManager();
    
    
    /**
     * 
     * Get RefererManager associated with this Weblogger instance.
     */
    public RefererManager getRefererManager();
    
    
    /**
     * Get ReferrerQueueManager.
     */
    public ReferrerQueueManager getReferrerQueueManager();
    
    
    /**
     * Get the AutoPingManager associated with this Weblogger instance.
     */
    public AutoPingManager getAutopingManager();
    
    
    /**
     * Get the PingTargetManager associated with this Weblogger instance.
     */
    public PingTargetManager getPingTargetManager();
    
    
    /**
     * Get the PingQueueManager associated with this Weblogger instance.
     */
    public PingQueueManager getPingQueueManager();
    
    
    /**
     * 
     * Get PropertiesManager associated with this Weblogger instance.
     */
    public PropertiesManager getPropertiesManager();   
    
    
    /**
     * Get ThreadManager associated with this Weblogger instance.
     */
    public ThreadManager getThreadManager();
    
    
    /**
     * Get IndexManager associated with this Weblogger instance.
     */
    public IndexManager getIndexManager();
    
    
    /**
     * Get ThemeManager associated with this Weblogger instance.
     */
    public ThemeManager getThemeManager();
    
    
    /**
     * Get PluginManager associated with this Weblogger instance.
     */
    public PluginManager getPluginManager();
    
    /**
     * Get MediaFileManager associated with this Weblogger instance.
     */
    public MediaFileManager getMediaFileManager();
    
    /**
     * Get FileContentManager associated with this Weblogger instance.
     */
    public FileContentManager getFileContentManager();

    /**
     * Get the URLStrategy used to build all urls in the system.
     */
    public URLStrategy getUrlStrategy();
    
    /**
     * Flush object states.
     */
    public void flush() throws WebloggerException;
    
    
    /**
     * Release all resources associated with Weblogger session.
     */
    public void release();
    
    
    /**
     * Initialize any resources necessary for this instance of Weblogger.
     */
    public void initialize() throws InitializationException;
    
    
    /**
     * Release all resources necessary for this instance of Weblogger.
     */
    public void shutdown();
    
    
    /**
     * Weblogger version
     */
    public String getVersion();    
    
    /**
     * Weblogger source code management revision
     */
    public String getRevision();
    
    /**
     * Weblogger build time
     */
    public String getBuildTime();
        
    /**
     * Get username that built Weblogger
     */
    public String getBuildUser();
    
}
