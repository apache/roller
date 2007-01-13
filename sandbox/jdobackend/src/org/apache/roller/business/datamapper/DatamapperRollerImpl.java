
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
package org.apache.roller.business.datamapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.ConfigManager;
import org.apache.roller.business.PropertiesManager;
import org.apache.roller.business.RollerImpl;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.business.runnable.ThreadManager;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingQueueManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.referrers.RefererManager;

/**
 * A Datamapper specific implementation of the Roller business layer.
 */
public abstract class DatamapperRollerImpl extends RollerImpl {

    static final long serialVersionUID = 5256135928578074652L;

    protected static Log logger = LogFactory.getLog(DatamapperRollerImpl.class);

    // our singleton instance
    private static DatamapperRollerImpl me = null;

    // a persistence utility class
    protected DatamapperPersistenceStrategy strategy = null;

    // references to the managers we maintain
    private BookmarkManager bookmarkManager = null;
    private PropertiesManager propertiesManager = null;
    private RefererManager referrerManager = null;
    private UserManager userManager = null;
    private WeblogManager weblogManager = null;
    private PingQueueManager pingQueueManager = null;
    private AutoPingManager autoPingManager = null;
    private PingTargetManager pingTargetManager = null;
    private ThreadManager threadManager = null;

    
    protected DatamapperRollerImpl() throws RollerException {
    }

    
    public void flush() throws RollerException {
        this.strategy.flush();
    }

    
    public void release() {

        // release our own stuff first
        if (bookmarkManager != null) bookmarkManager.release();
        if (referrerManager != null) referrerManager.release();
        if (userManager != null) userManager.release();
        if (weblogManager != null) weblogManager.release();
        if (pingTargetManager != null) pingTargetManager.release();
        if (pingQueueManager != null) pingQueueManager.release();
        if (autoPingManager != null) autoPingManager.release();

        // tell Datamapper to close down
        this.strategy.release();

        // then let parent do its thing
        super.release();
    }

    
    public void shutdown() {

        // do our own shutdown first
        this.release();

        // then let parent do its thing
        super.shutdown();
    }

    /**
     * @see org.apache.roller.business.Roller#getAutopingManager()
     */
    public AutoPingManager getAutopingManager() {
        if (autoPingManager == null) {
            autoPingManager = createDatamapperAutoPingManager(strategy);
        }
        return autoPingManager;
    }

    protected AutoPingManager createDatamapperAutoPingManager(
            DatamapperPersistenceStrategy strategy) {
        return new DatamapperAutoPingManagerImpl(strategy);
    }
    
    /**
     * @see org.apache.roller.business.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() {
        if ( bookmarkManager == null ) {
            bookmarkManager = createDatamapperBookmarkManager(strategy);
        }
        return bookmarkManager;
    }

    protected BookmarkManager createDatamapperBookmarkManager(
            DatamapperPersistenceStrategy strategy) {
        return new DatamapperBookmarkManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() {
        if (pingQueueManager == null) {
            pingQueueManager = createDatamapperPingQueueManager(strategy);
        }
        return pingQueueManager;
    }

    protected PingQueueManager createDatamapperPingQueueManager(
            DatamapperPersistenceStrategy strategy) {
        return new DatamapperPingQueueManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() {
        if (pingTargetManager == null) {
            pingTargetManager = createDatamapperPingTargetManager(strategy);
        }
        return pingTargetManager;
    }

    protected PingTargetManager createDatamapperPingTargetManager(
            DatamapperPersistenceStrategy strategy) {
        return new DatamapperPingTargetManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() {
        if (propertiesManager == null) {
            propertiesManager = createDatamapperPropertiesManager(strategy);
        }
        return propertiesManager;
    }

    protected PropertiesManager createDatamapperPropertiesManager(
            DatamapperPersistenceStrategy strategy) {
        return new DatamapperPropertiesManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() {
        if ( referrerManager == null ) {
            referrerManager = createDatamapperRefererManagerImpl(strategy);
        }
        return referrerManager;
    }

    protected abstract RefererManager createDatamapperRefererManagerImpl(
            DatamapperPersistenceStrategy strategy);

    /**
     * @see org.apache.roller.business.Roller#getUserManager()
     */
    public UserManager getUserManager() {
        if ( userManager == null ) {
            userManager = createDatamapperUserManager(strategy);
        }
        return userManager;
    }

    protected abstract UserManager createDatamapperUserManager(
            DatamapperPersistenceStrategy strategy);

    /**
     * @see org.apache.roller.business.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() {
        if ( weblogManager == null ) {
            weblogManager = createDatamapperWeblogManager(strategy);
        }
        return weblogManager;
    }

    protected abstract WeblogManager createDatamapperWeblogManager(
            DatamapperPersistenceStrategy strategy);

    /**
     * @see org.apache.roller.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        if (threadManager == null) {
            threadManager = createDatamapperThreadManager(strategy);
        }
        return threadManager;
    }

    protected abstract ThreadManager createDatamapperThreadManager(
            DatamapperPersistenceStrategy strategy);

    /**
     * This method is deprecated.
     * @return null
     * @see org.apache.roller.business.Roller#getConfigManager()
     * @deprecated see JIRA issue ROL-1151
     */
    public ConfigManager getConfigManager() {
        throw new RuntimeException("Deprecated method getConfigManager.");
    }
    
}
