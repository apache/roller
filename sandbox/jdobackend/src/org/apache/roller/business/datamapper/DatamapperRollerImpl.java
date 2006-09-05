/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.business.datamapper;
    
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerImpl;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.ConfigManager;
import org.apache.roller.model.PingQueueManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;


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
    private PlanetManager planetManager = null;
    private RefererManager referrerManager = null;
    private UserManager userManager = null;
    private WeblogManager weblogManager = null;
    private PingQueueManager pingQueueManager = null;
    private AutoPingManager autoPingManager = null;
    private PingTargetManager pingTargetManager = null;

    
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
     * @see org.apache.roller.model.Roller#getAutoPingManager()
     */
    public AutoPingManager getAutopingManager() throws RollerException {
        if (autoPingManager == null) {
            autoPingManager = new DatamapperAutoPingManagerImpl(strategy);
        }
        return autoPingManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException {
        if ( bookmarkManager == null ) {
            bookmarkManager = new DatamapperBookmarkManagerImpl(strategy);
        }
        return bookmarkManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() throws RollerException {
        if (pingQueueManager == null) {
            pingQueueManager = new DatamapperPingQueueManagerImpl(strategy);
        }
        return pingQueueManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() throws RollerException {
        if (pingTargetManager == null) {
            pingTargetManager = new DatamapperPingTargetManagerImpl(strategy);
        }
        return pingTargetManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getPlanetManager()
     */
    public PlanetManager getPlanetManager() throws RollerException {
        if ( planetManager == null ) {
            planetManager = new DatamapperPlanetManagerImpl(strategy);
        }
        return planetManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() throws RollerException {
        if (propertiesManager == null) {
            propertiesManager = new DatamapperPropertiesManagerImpl(strategy);
        }
        return propertiesManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException {
        if ( referrerManager == null ) {
            referrerManager = new DatamapperReferrerManagerImpl(strategy);
        }
        return referrerManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException {
        if ( userManager == null ) {
            userManager = new DatamapperUserManagerImpl(strategy);
        }
        return userManager;
    }

    
    /**
     * @see org.apache.roller.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException {
        if ( weblogManager == null ) {
            weblogManager = new DatamapperWeblogManagerImpl(strategy);
        }
        return weblogManager;
    }
    
    
    /**
     * This method is deprecated.
     * @return null
     * @see org.apache.roller.model.Roller#getConfigManager()
     * @deprecated see JIRA issue ROL-1151
     * @throws org.apache.roller.RollerException on any error
     */
    public ConfigManager getConfigManager() throws RollerException {
        throw new RollerException("Deprecated method getConfigManager.");
    }
    
}
