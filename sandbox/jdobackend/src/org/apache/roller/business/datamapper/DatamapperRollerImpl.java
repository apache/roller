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

/*
 * DatamapperRollerImpl.java
 *
 * Created on May 27, 2006, 2:50 PM
 *
 */
    
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerImpl;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.PingQueueManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;

/**
 * A Datamapper specific implementation of the Roller business layer.
 */
public abstract class DatamapperRollerImpl extends RollerImpl {

    static final long serialVersionUID = 5256135928578074652L;

    protected static Log logger = 
            LogFactory.getLog(DatamapperRollerImpl.class);

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
            autoPingManager = createAutoPingManagerImpl(strategy);
        }
        return autoPingManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException {
        if ( bookmarkManager == null ) {
            bookmarkManager = createBookmarkManagerImpl(strategy);
        }
        return bookmarkManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() throws RollerException {
        if (pingQueueManager == null) {
            pingQueueManager = createPingQueueManagerImpl(strategy);
        }
        return pingQueueManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() throws RollerException {
        if (pingTargetManager == null) {
            pingTargetManager = createPingTargetManagerImpl(strategy);
        }
        return pingTargetManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getPlanetManager()
     */
    public PlanetManager getPlanetManager() throws RollerException {
        if ( planetManager == null ) {
            planetManager = createPlanetManagerImpl(strategy);
        }
        return planetManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() throws RollerException {
        if (propertiesManager == null) {
            propertiesManager = createPropertiesManagerImpl(strategy);
        }
        return propertiesManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException {
        if ( referrerManager == null ) {
            referrerManager = createReferrerManagerImpl(strategy);
        }
        return referrerManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException {
        if ( userManager == null ) {
            userManager = createUserManagerImpl(strategy);
        }
        return userManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException {
        if ( weblogManager == null ) {
            weblogManager = createWeblogManagerImpl(strategy);
        }
        return weblogManager;
    }

    protected AutoPingManager createAutoPingManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperAutoPingManagerImpl(strategy);
    }

    protected BookmarkManager createBookmarkManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperBookmarkManagerImpl(strategy);
    }

    protected PingTargetManager createPingTargetManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperPingTargetManagerImpl(strategy);
    }

    protected PingQueueManager createPingQueueManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperPingQueueManagerImpl(strategy);
    }

    protected PlanetManager createPlanetManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperPlanetManagerImpl(strategy);
    }

    protected PropertiesManager createPropertiesManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperPropertiesManagerImpl(strategy);
    }

    protected RefererManager createReferrerManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperReferrerManagerImpl(strategy);
    }

    protected UserManager createUserManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperUserManagerImpl(strategy);
    }

    protected WeblogManager createWeblogManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return new DatamapperWeblogManagerImpl(strategy);
    }

}
