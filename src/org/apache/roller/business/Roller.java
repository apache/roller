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

package org.apache.roller.business;

import java.io.Serializable;
import java.sql.Connection;
import org.apache.roller.RollerException;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingQueueManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.referrers.ReferrerQueueManager;
import org.apache.roller.business.runnable.ThreadManager;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.business.themes.ThemeManager;


/** 
 * The main entry point interface of the Roller business tier.
 */
public interface Roller {
    
    
    /** 
     * Get UserManager associated with this Roller instance.
     */
    public UserManager getUserManager();
    
    
    /** 
     * Get BookmarkManager associated with this Roller instance.
     */
    public BookmarkManager getBookmarkManager();
    
    
    /** 
     * Get WeblogManager associated with this Roller instance.
     */
    public WeblogManager getWeblogManager();
    
    
    /** 
     * Get RefererManager associated with this Roller instance.
     */
    public RefererManager getRefererManager();
    
    
    /**
     * Get ReferrerQueueManager.
     */
    public ReferrerQueueManager getReferrerQueueManager();
    
    
    /** 
     * Get RefererManager associated with this Roller instance.
     */
    public ConfigManager getConfigManager();
    
    
    /**
     * Get the AutoPingManager associated with this Roller instance.
     */
    public AutoPingManager getAutopingManager();
    
    
    /**
     * Get the PingTargetManager associated with this Roller instance.
     */
    public PingTargetManager getPingTargetManager();
    
    
    /**
     * Get the PingQueueManager associated with this Roller instance.
     */
    public PingQueueManager getPingQueueManager();
    
    
    /** 
     * Get PropertiesManager associated with this Roller instance.
     */
    public PropertiesManager getPropertiesManager();
    
    
    /** 
     * Get FileManager associated with this Roller instance.
     */
    public FileManager getFileManager();
    
    
    /**
     * Get ThreadManager associated with this Roller instance.
     */
    public ThreadManager getThreadManager();
    
    
    /**
     * Get IndexManager associated with this Roller instance.
     */
    public IndexManager getIndexManager();
    
    
    /**
     * Get ThemeManager associated with this Roller instance.
     */
    public ThemeManager getThemeManager();
    
    
    /**
     * Get PluginManager associated with this Roller instance.
     */
    public PluginManager getPagePluginManager();
    
    
    /**
     * Flush object states.
     */
    public void flush() throws RollerException;
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
    
    /**
     * Release all resources necessary for this instance of Roller.
     */
    public void shutdown();
    
    /** Roller version */
    public String getVersion();    
    
    /** Roller build time */
    public String getBuildTime();
        
    /** Get username that built Roller */
    public String getBuildUser();
}

