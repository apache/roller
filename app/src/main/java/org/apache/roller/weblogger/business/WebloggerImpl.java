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

import org.apache.roller.weblogger.business.plugins.PluginManager;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.config.PingConfig;

import javax.enterprise.inject.Alternative;


/**
 * The abstract version of the Weblogger implementation.
 * 
 * Here we put code that pertains to *all* implementations of the Weblogger
 * interface, regardless of their persistence strategy.
 */
@com.google.inject.Singleton
public abstract class WebloggerImpl implements Weblogger {
    
    private static Log log = LogFactory.getLog(WebloggerImpl.class);
    
    // managers
    private final AutoPingManager      autoPingManager;
    private final BookmarkManager      bookmarkManager;
    private final IndexManager         indexManager;
    private final MediaFileManager     mediaFileManager;
    private final FileContentManager   fileContentManager;
    private final PingQueueManager     pingQueueManager;
    private final PingTargetManager    pingTargetManager;
    private final PluginManager        pluginManager;
    private final PropertiesManager    propertiesManager;
    private final ThemeManager         themeManager;
    private final ThreadManager        threadManager;
    private final UserManager          userManager;
    private final WeblogManager        weblogManager;
    private final WeblogEntryManager   weblogEntryManager;
    private final OAuthManager         oauthManager;
    private final FeedFetcher          feedFetcher;
    private final PlanetManager        planetManager;
    
    // url strategy
    private final URLStrategy          urlStrategy;
    private final org.apache.roller.planet.business.PlanetURLStrategy planetUrlStrategy;
    
    // some simple attributes
    private final String version;
    private final String revision;
    private final String buildTime;
    private final String buildUser;
    
    
    protected WebloggerImpl(
        AutoPingManager      autoPingManager,
        BookmarkManager      bookmarkManager,
        IndexManager         indexManager,
        MediaFileManager     mediaFileManager,
        FileContentManager   fileContentManager,
        PingQueueManager     pingQueueManager,
        PingTargetManager    pingTargetManager,
        PluginManager        pluginManager,
        PropertiesManager    propertiesManager,
        ThemeManager         themeManager,
        ThreadManager        threadManager,
        UserManager          userManager,
        WeblogManager        weblogManager,
        WeblogEntryManager   weblogEntryManager,
        OAuthManager         oauthManager,
        FeedFetcher          feedFetcher,
        PlanetManager        planetManager,
        org.apache.roller.planet.business.PlanetURLStrategy planetUrlStrategy,
        URLStrategy          urlStrategy) throws WebloggerException { 
                
        this.autoPingManager     = autoPingManager;
        this.bookmarkManager     = bookmarkManager;
        this.indexManager        = indexManager;
        this.mediaFileManager    = mediaFileManager;
        this.fileContentManager  = fileContentManager;
        this.pingQueueManager    = pingQueueManager;
        this.pingTargetManager   = pingTargetManager;
        this.pluginManager       = pluginManager;
        this.propertiesManager   = propertiesManager;
        this.themeManager        = themeManager;
        this.threadManager       = threadManager;
        this.userManager         = userManager;
        this.weblogManager       = weblogManager;
        this.weblogEntryManager  = weblogEntryManager;
        this.oauthManager        = oauthManager;
        this.urlStrategy         = urlStrategy;
        this.feedFetcher         = feedFetcher;
        this.planetManager       = planetManager;
        this.planetUrlStrategy   = planetUrlStrategy;

        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/roller-version.properties"));
        } catch (IOException e) {
            log.error("roller-version.properties not found", e);
        }
        
        version = props.getProperty("ro.version", "UNKNOWN");
        revision = props.getProperty("ro.revision", "UNKNOWN");
        buildTime = props.getProperty("ro.buildTime", "UNKNOWN");
        buildUser = props.getProperty("ro.buildUser", "UNKNOWN");
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        return threadManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.model.Weblogger#getIndexManager()
     */
    public IndexManager getIndexManager() {
        return indexManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getThemeManager()
     */
    public ThemeManager getThemeManager() {
        return themeManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getUserManager()
     */
    public UserManager getUserManager() {
        return userManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() {
        return bookmarkManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getMediaFileManager()
     */
    public MediaFileManager getMediaFileManager() {
        return mediaFileManager;
    }
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getFileContentManager()
     */
    public FileContentManager getFileContentManager() {
        return fileContentManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getWeblogEntryManager()
     */
    public WeblogEntryManager getWeblogEntryManager() {
        return weblogEntryManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getWeblogManager()
     */
    public WeblogManager getWeblogManager() {
        return weblogManager;
    }
    

    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() {
        return pingQueueManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() {
        return autoPingManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.modelWebloggerr#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() {
        return pingTargetManager;
    }
    
    
    /**
     *
     *
     * @see org.apache.roller.weblogger.modelWebloggerr#getPluginManager()
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }


    /**
     *
     *
     * @see org.apache.roller.weblogger.modelWebloggerr#getOauthManager()
     */
    public OAuthManager getOAuthManager() {
        return oauthManager;
    }


    /**
     * @inheritDoc
     */
    public URLStrategy getUrlStrategy() {
        return urlStrategy;
    }
	
    public FeedFetcher getFeedFetcher() {
        return feedFetcher;
    }

    public PlanetManager getPlanetManager() {
        return planetManager;
    }

	public org.apache.roller.planet.business.PlanetURLStrategy getPlanetURLStrategy() {
		return planetUrlStrategy;
	}

    /**
     * @inheritDoc
     */
    public void release() {
        try {
            autoPingManager.release();
            bookmarkManager.release();
            mediaFileManager.release();
            fileContentManager.release();
            pingTargetManager.release();
            pingQueueManager.release();
            pluginManager.release();
            threadManager.release();
            userManager.release();
            weblogManager.release();
        } catch(Exception e) {
            log.error("Error calling Roller.release()", e);
        }
    }
    
    
    /**
     * @inheritDoc
     */
    public void initialize() throws InitializationException {
        
        log.info("Initializing Roller Weblogger business tier");
        
        // TODO: this should probably be done in a more uniform fashion, possibly
        // using annotations?  biggest issue is controlling ordering
        getPropertiesManager().initialize();
        getThemeManager().initialize();
        getThreadManager().initialize();
        getIndexManager().initialize();
        getMediaFileManager().initialize();
        
        try {
            // Initialize ping systems
            // TODO: this should probably be moving inside ping manager initialize() methods?
            
            // Initialize common targets from the configuration
            PingConfig.initializeCommonTargets();
            
            // Initialize ping variants
            PingConfig.initializePingVariants();
            
            // Remove all autoping configurations if ping usage has been disabled.
            if (PingConfig.getDisablePingUsage()) {
                log.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
                WebloggerFactory.getWeblogger().getAutopingManager().removeAllAutoPings();
            }
        } catch (Exception e) {
            throw new InitializationException("Error initializing ping systems", e);
        }
        
        // we always need to do a flush after initialization because it's
        // possible that some changes need to be persisted
        try {
            flush();
        } catch(WebloggerException ex) {
            throw new InitializationException("Error flushing after initialization", ex);
        } 
        
        log.info("Roller Weblogger business tier successfully initialized");
    }
    
    
    /**
     * @inheritDoc
     */
    public void shutdown() {
        try {
            HitCountQueue.getInstance().shutdown();
            if (indexManager != null) {
                indexManager.shutdown();
            }
            if (threadManager != null) {
                threadManager.shutdown();
            }
        } catch(Exception e) {
            log.error("Error calling Roller.shutdown()", e);
        }
    }
    
    
    /**
     * Weblogger version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Get source code repository revision # used to create build
     */
    public String getRevision() {
        return revision;
    }
        
    /**
     * Weblogger build time
     */
    public String getBuildTime() {
        return buildTime;
    }
    
    
    /**
     * Get username that built Weblogger
     */
    public String getBuildUser() {
        return buildUser;
    }
    
}
