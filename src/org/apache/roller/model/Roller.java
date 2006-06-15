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

package org.apache.roller.model;

import java.io.Serializable;
import java.sql.Connection;
import org.apache.roller.RollerException;
import org.apache.roller.business.referrers.ReferrerQueueManager;


/** 
 * The main entry point interface of the Roller business tier.
 */
public interface Roller {
    
    
    /** 
     * Get UserManager associated with this Roller instance.
     */
    public UserManager getUserManager() throws RollerException;
    
    
    /** 
     * Get BookmarkManager associated with this Roller instance.
     */
    public BookmarkManager getBookmarkManager() throws RollerException;
    
    
    /** 
     * Get WeblogManager associated with this Roller instance.
     */
    public WeblogManager getWeblogManager() throws RollerException;
    
    
    /** 
     * Get RefererManager associated with this Roller instance.
     */
    public RefererManager getRefererManager() throws RollerException;
    
    
    /**
     * Get ReferrerQueueManager.
     */
    public ReferrerQueueManager getReferrerQueueManager();
    
    
    /** 
     * Get RefererManager associated with this Roller instance.
     */
    public ConfigManager getConfigManager() throws RollerException;
    
    
    /**
     * Get the AutoPingManager associated with this Roller instance.
     */
    public AutoPingManager getAutopingManager() throws RollerException;
    
    
    /**
     * Get the PingTargetManager associated with this Roller instance.
     */
    public PingTargetManager getPingTargetManager() throws RollerException;
    
    
    /**
     * Get the PingQueueManager associated with this Roller instance.
     */
    public PingQueueManager getPingQueueManager() throws RollerException;
    
    
    /** 
     * Get PropertiesManager associated with this Roller instance.
     */
    public PropertiesManager getPropertiesManager() throws RollerException;
    
    
    /** 
     * Get FileManager associated with this Roller instance.
     */
    public FileManager getFileManager() throws RollerException;
    
    
    /**
     * Get ThreadManager associated with this Roller instance.
     */
    public ThreadManager getThreadManager() throws RollerException;
    
    
    /**
     * Get IndexManager associated with this Roller instance.
     */
    public IndexManager getIndexManager() throws RollerException;
    
    
    /**
     * Get PlanetManager associated with this Roller instance.
     */
    public PlanetManager getPlanetManager() throws RollerException;
    
    
    /**
     * Get ThemeManager associated with this Roller instance.
     */
    public ThemeManager getThemeManager() throws RollerException;
    
    
    /**
     * Get PluginManager associated with this Roller instance.
     */
    public PluginManager getPagePluginManager() throws RollerException;
    
    
    /**
     * Upgrade database if needed.
     */
    public void upgradeDatabase(Connection con) throws RollerException;
    
    
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
    
}

