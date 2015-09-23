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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.business.plugins.PluginManager;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;

/**
 * The abstract version of the Weblogger implementation.
 * 
 * Here we put code that pertains to *all* implementations of the Weblogger
 * interface, regardless of their persistence strategy.
 */
public abstract class WebloggerImpl implements Weblogger {
    
    private static Log log = LogFactory.getLog(WebloggerImpl.class);
    
    // managers
    private final IndexManager         indexManager;
    private final MediaFileManager     mediaFileManager;
    private final FileContentManager   fileContentManager;
    private final PingTargetManager    pingTargetManager;
    private final PluginManager        pluginManager;
    private final PropertiesManager    propertiesManager;
    private final ThemeManager         themeManager;
    private final ThreadManager        threadManager;
    private final UserManager          userManager;
    private final WeblogManager        weblogManager;
    private final WeblogEntryManager   weblogEntryManager;
    private final FeedProcessor feedFetcher;
    private final PlanetManager        planetManager;
    
    // url strategy
    private final URLStrategy          urlStrategy;

    // some simple attributes
    private final String version;
    private final String revision;
    private final String buildTime;
    private final String buildUser;
    
    
    protected WebloggerImpl(
        IndexManager         indexManager,
        MediaFileManager     mediaFileManager,
        FileContentManager   fileContentManager,
        PingTargetManager    pingTargetManager,
        PluginManager        pluginManager,
        PropertiesManager    propertiesManager,
        ThemeManager         themeManager,
        ThreadManager        threadManager,
        UserManager          userManager,
        WeblogManager        weblogManager,
        WeblogEntryManager   weblogEntryManager,
        FeedProcessor feedFetcher,
        PlanetManager        planetManager,
        URLStrategy          urlStrategy) throws WebloggerException {
                
        this.indexManager        = indexManager;
        this.mediaFileManager    = mediaFileManager;
        this.fileContentManager  = fileContentManager;
        this.pingTargetManager   = pingTargetManager;
        this.pluginManager       = pluginManager;
        this.propertiesManager   = propertiesManager;
        this.themeManager        = themeManager;
        this.threadManager       = threadManager;
        this.userManager         = userManager;
        this.weblogManager       = weblogManager;
        this.weblogEntryManager  = weblogEntryManager;
        this.urlStrategy         = urlStrategy;
        this.feedFetcher         = feedFetcher;
        this.planetManager       = planetManager;

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
     * @see org.apache.roller.weblogger.business.Weblogger#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        return threadManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getIndexManager()
     */
    public IndexManager getIndexManager() {
        return indexManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getThemeManager()
     */
    public ThemeManager getThemeManager() {
        return themeManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getUserManager()
     */
    public UserManager getUserManager() {
        return userManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getMediaFileManager()
     */
    public MediaFileManager getMediaFileManager() {
        return mediaFileManager;
    }
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getFileContentManager()
     */
    public FileContentManager getFileContentManager() {
        return fileContentManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getWeblogEntryManager()
     */
    public WeblogEntryManager getWeblogEntryManager() {
        return weblogEntryManager;
    }
    
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getWeblogManager()
     */
    public WeblogManager getWeblogManager() {
        return weblogManager;
    }
    

    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    /**
     * 
     * 
     * @see org.apache.roller.weblogger.business.Weblogger#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() {
        return pingTargetManager;
    }
    
    
    /**
     *
     *
     * @see org.apache.roller.weblogger.business.Weblogger#getPluginManager()
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }


    /**
     * @inheritDoc
     */
    public URLStrategy getUrlStrategy() {
        return urlStrategy;
    }
	
    public FeedProcessor getFeedFetcher() {
        return feedFetcher;
    }

    public PlanetManager getPlanetManager() {
        return planetManager;
    }


    /**
     * @inheritDoc
     */
    public void release() {
        try {
            mediaFileManager.release();
            fileContentManager.release();
            pingTargetManager.release();
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
    public void initialize() throws WebloggerException {
        
        log.info("Initializing Roller Weblogger business tier");
        
        getPropertiesManager().initialize();
        getThemeManager().initialize();
        getThreadManager().initialize();
        getIndexManager().initialize();
        getMediaFileManager().initialize();
        getPingTargetManager().initialize();

        // we always need to do a flush after initialization because it's
        // possible that some changes need to be persisted
        try {
            flush();
        } catch(WebloggerException ex) {
            throw new WebloggerException("Error flushing after initialization", ex);
        } 
        
        log.info("Roller Weblogger business tier successfully initialized");
    }
    
    
    /**
     * @inheritDoc
     */
    public void shutdown() {
        try {
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
