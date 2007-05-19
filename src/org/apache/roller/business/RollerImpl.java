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

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingQueueManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.referrers.ReferrerQueueManager;
import org.apache.roller.business.runnable.ThreadManager;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.business.themes.ThemeManager;

/**
 * Roller implementation designed for Dependency Injection
 */
public abstract class RollerImpl implements Roller {
    private static Log log = LogFactory.getLog(RollerImpl.class);

    private String version = null;
    private String buildTime = null;
    private String buildUser = null;
    
    private AutoPingManager      autopingManager;
    private BookmarkManager      bookmarkManager;
    private FileManager          fileManager;
    private IndexManager         indexManager;
    private PluginManager        pagePluginManager;
    private PingQueueManager     pingQueueManager;
    private PingTargetManager    pingTargetManager;
    private PropertiesManager    propertiesManager;
    private RefererManager       refererManager;
    private ReferrerQueueManager referrerQueueManager;
    private ThemeManager         themeManager;
    private ThreadManager        threadManager;
    private UserManager          userManager;
    private WeblogManager        weblogManager;
    
    
    public RollerImpl() {
                
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            log.error("version.properties not found", e);
        }
        
        version = props.getProperty("ro.version", "UNKNOWN");
        buildTime = props.getProperty("ro.buildTime", "UNKNOWN");
        buildUser = props.getProperty("ro.buildUser", "UNKNOWN");
    }
    
    public AutoPingManager getAutopingManager() {
        return autopingManager;
    }
    
    @Inject
    public void setAutopingManager(AutoPingManager autopingManager) {
        this.autopingManager = autopingManager;
    }
    
    public BookmarkManager getBookmarkManager() {
        return bookmarkManager;
    }
    
    @Inject
    public void setBookmarkManager(BookmarkManager bookmarkManager) {
        this.bookmarkManager = bookmarkManager;
    }
    
    public FileManager getFileManager() {
        return fileManager;
    }
    
    @Inject
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
    
    public IndexManager getIndexManager() {
        return indexManager;
    }
    
    @Inject
    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }
    
    public PluginManager getPagePluginManager() {
        return pagePluginManager;
    }
    
    @Inject
    public void setPagePluginManager(PluginManager pagePluginManager) {
        this.pagePluginManager = pagePluginManager;
    }
    
    public PingQueueManager getPingQueueManager() {
        return pingQueueManager;
    }
        
    @Inject
    public void setPingQueueManager(PingQueueManager pingQueueManager) {
        this.pingQueueManager = pingQueueManager;
    }
    
    public PingTargetManager getPingTargetManager() {
        return pingTargetManager;
    }

    @Inject
    public void setPingTargetManager(PingTargetManager pingTargetManager) {
        this.pingTargetManager = pingTargetManager;
    }

    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    @Inject
    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }
    
    public RefererManager getRefererManager() {
        return refererManager;
    }
    
    @Inject
    public void setRefererManager(RefererManager refererManager) {
        this.refererManager = refererManager;
    }
    
    public ReferrerQueueManager getReferrerQueueManager() {
        return referrerQueueManager;
    }
    
    @Inject
    public void setReferrerQueueManager(ReferrerQueueManager referrerQueueManager) {
        this.referrerQueueManager = referrerQueueManager;
    }
    
    public ThemeManager getThemeManager() {
        return themeManager;
    }
    
    @Inject
    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }
    
    public ThreadManager getThreadManager() {
        return threadManager;
    }
    
    @Inject
    public void setThreadManager(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }
    
    public UserManager getUserManager() {
        return userManager;
    }
    
    @Inject
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
        
    public WeblogManager getWeblogManager() {
        return weblogManager;
    }

    @Inject
    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }
    
    public void init() {
        this.indexManager.init();
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
