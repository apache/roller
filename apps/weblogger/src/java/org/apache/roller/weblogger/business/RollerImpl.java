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
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManager;
import org.apache.roller.weblogger.business.referrers.ReferrerQueueManagerImpl;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.config.PingConfig;

/**
 * The abstract version of the Roller implementation.
 *
 * Here we put code that pertains to *all* implementations of the Roller
 * interface, regardless of their persistence strategy.
 */
@com.google.inject.Singleton
public abstract class RollerImpl implements Roller {
    private static Log mLogger = LogFactory.getLog(RollerImpl.class);
    
    private AutoPingManager      autoPingManager = null;
    private BookmarkManager      bookmarkManager = null;
    private FileManager          fileManager = null;
    private IndexManager         indexManager = null;
    private PingQueueManager     pingQueueManager = null;
    private PingTargetManager    pingTargetManager = null;
    private PluginManager        pluginManager = null;
    private PropertiesManager    propertiesManager = null;
    private RefererManager       refererManager = null;
    private ReferrerQueueManager refererQueueManager = null;
    private ThemeManager         themeManager = null;
    private ThreadManager        threadManager = null;
    private UserManager          userManager = null;
    private WeblogManager        weblogManager = null;
            
    private String version = null;
    private String buildTime = null;
    private String buildUser = null;
    
    protected RollerImpl(
        AutoPingManager      autoPingManager,
        BookmarkManager      bookmarkManager,
        FileManager          fileManager,
        IndexManager         indexManager,
        PingQueueManager     pingQueueManager,
        PingTargetManager    pingTargetManager,
        PluginManager        pluginManager,
        PropertiesManager    propertiesManager,
        RefererManager       refererManager,
        ReferrerQueueManager refererQueueManager, 
        ThemeManager         themeManager,
        ThreadManager        threadManager,
        UserManager          userManager,
        WeblogManager        weblogManager) throws WebloggerException { 
                
        this.autoPingManager     = autoPingManager;
        this.bookmarkManager     = bookmarkManager;
        this.fileManager         = fileManager;
        this.indexManager        = indexManager;
        this.pingQueueManager    = pingQueueManager;
        this.pingTargetManager   = pingTargetManager;
        this.pluginManager       = pluginManager;
        this.propertiesManager   = propertiesManager;
        this.refererManager      = refererManager;
        this.refererQueueManager = refererQueueManager;
        this.themeManager        = themeManager;
        this.threadManager       = threadManager;
        this.userManager         = userManager;
        this.weblogManager       = weblogManager;
            
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
        return fileManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        return threadManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getIndexManager()
     */
    public IndexManager getIndexManager() {
        return indexManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getThemeManager()
     */
    public ThemeManager getThemeManager() {
        return themeManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.business.referrers.ReferrerQueueManager
     */
    public ReferrerQueueManager getReferrerQueueManager() {
        return refererQueueManager;
    }
    
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getUserManager()
     */
    public UserManager getUserManager() {
        return userManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() {
        return bookmarkManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() {
        return weblogManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() {
        return refererManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() {
        return pingQueueManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() {
        return autoPingManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() {
        return pingTargetManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPluginManager()
     */
    public PluginManager getPagePluginManager() {
        return pluginManager;
    }
    
    
    public void release() {
        try {
            autoPingManager.release();
            bookmarkManager.release();
            fileManager.release();
            pingTargetManager.release();
            pingQueueManager.release();
            pluginManager.release();
            refererManager.release();
            threadManager.release();
            userManager.release();
            weblogManager.release();
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
