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

import org.apache.roller.weblogger.business.plugins.PluginManagerImpl;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManager;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManagerImpl;
import org.apache.roller.weblogger.business.runnable.ThreadManagerImpl;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.themes.ThemeManagerImpl;
import org.apache.roller.weblogger.config.PingConfig;


/**
 * The abstract version of the Roller implementation.
 *
 * Here we put code that pertains to *all* implementations of the Roller
 * interface, regardless of their persistence strategy.
 */
public abstract class RollerImpl implements Roller {
    
    private static Log mLogger = LogFactory.getLog(RollerImpl.class);
    
    private FileManager   fileManager = null;
    private IndexManager  indexManager = null;
    private ThreadManager threadManager = null;
    private ThemeManager  themeManager = null;
    private PluginManager pluginManager = null;
            
    private String version = null;
    private String buildTime = null;
    private String buildUser = null;
    
    
    public RollerImpl() {
                
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            mLogger.error("version.properties not found", e);
        }
        
        version = props.getProperty("ro.version", "UNKNOWN");
        buildTime = props.getProperty("ro.buildTime", "UNKNOWN");
        buildUser = props.getProperty("ro.buildUser", "UNKNOWN");
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getFileManager()
     */
    public FileManager getFileManager() {
        if (fileManager == null) {
            fileManager = new FileManagerImpl();
        }
        return fileManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        if (threadManager == null) {
            threadManager = new ThreadManagerImpl();
        }
        return threadManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getIndexManager()
     */
    public IndexManager getIndexManager() {
        if (indexManager == null) {
            indexManager = new IndexManagerImpl();
        }
        return indexManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getThemeManager()
     */
    public ThemeManager getThemeManager() {
        if (themeManager == null) {
            themeManager = new ThemeManagerImpl();
        }
        return themeManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.business.referrers.ReferrerQueueManager
     */
    public ReferrerQueueManager getReferrerQueueManager() {
        return ReferrerQueueManagerImpl.getInstance();
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPluginManager()
     */
    public PluginManager getPagePluginManager() {
        if (pluginManager == null) {
            pluginManager = new PluginManagerImpl();
        }
        return pluginManager;
    }
    
    
    public void release() {
        try {
            if (fileManager != null) fileManager.release();
            if (threadManager != null) threadManager.release();
            if (pluginManager != null) pluginManager.release();
        } catch(Throwable e) {
            mLogger.error("Error calling Roller.release()", e);
        }
    }
    
    
    /**
     * @inheritDoc
     */
    public void initialize() throws InitializationException {
        
        mLogger.info("Initializing Roller Weblogger business tier");
        
        // TODO: this should probably be done in a more uniform fashion, possibly
        // using annotations?  biggest issue is controlling ordering
        getPropertiesManager().initialize();
        getThemeManager().initialize();
        getThreadManager().initialize();
        getIndexManager().initialize();
        
        try {
            // Initialize ping systems
            // TODO: this should probably be moving inside ping manager initialize() methods?
            
            // Initialize common targets from the configuration
            PingConfig.initializeCommonTargets();
            
            // Initialize ping variants
            PingConfig.initializePingVariants();
            
            // Remove custom ping targets if they have been disallowed
            if (PingConfig.getDisallowCustomTargets()) {
                mLogger.info("Custom ping targets have been disallowed.  Removing any existing custom targets.");
                RollerFactory.getRoller().getPingTargetManager().removeAllCustomPingTargets();
            }
            
            // Remove all autoping configurations if ping usage has been disabled.
            if (PingConfig.getDisablePingUsage()) {
                mLogger.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
                RollerFactory.getRoller().getAutopingManager().removeAllAutoPings();
            }
        } catch (Throwable t) {
            throw new InitializationException("Error initializing ping systems", t);
        }
        
        // we always need to do a flush after initialization because it's
        // possible that some changes need to be persisted
        try {
            flush();
        } catch(WebloggerException ex) {
            throw new InitializationException("Error flushing after initialization", ex);
        }
        
        mLogger.info("Roller Weblogger business tier successfully initialized");
    }
    
    
    public void shutdown() {
        try {
            HitCountQueue.getInstance().shutdown();
            if (getReferrerQueueManager() != null) getReferrerQueueManager().shutdown();
            if (indexManager != null) indexManager.shutdown();
            if (threadManager != null) threadManager.shutdown();
        } catch(Throwable e) {
            mLogger.error("Error calling Roller.shutdown()", e);
        }
    }
    
    /** Roller version */
    public String getVersion() {
        return version;
    }
    
    
    /** Roller build time */
    public String getBuildTime() {
        return buildTime;
    }
    
    
    /** Get username that built Roller */
    public String getBuildUser() {
        return buildUser;
    }
    
}
