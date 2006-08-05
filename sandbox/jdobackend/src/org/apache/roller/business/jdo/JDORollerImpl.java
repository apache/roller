/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.roller.business.jdo;

import java.sql.Connection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.business.utils.UpgradeDatabase;
import org.apache.roller.business.datamapper.DatamapperRollerImpl;
import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.ConfigManager;
import org.apache.roller.model.PingQueueManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.business.datamapper.DatamapperPlanetManagerImpl;

/**
 * Implements Roller, the entry point interface for the Roller business tier
 * APIs. JDO specific implementation.
 * 
 * @author Dave Johnson
 */
public class JDORollerImpl extends DatamapperRollerImpl {
    /**
     * The logger associated with this class.
     */
    private static Log logger = LogFactory.getFactory()
            .getInstance(JDORollerImpl.class);

    /**
     * The singleton instance of the class.
     */
    protected static JDORollerImpl  me;

    /**
     * Single constructor.
     * @throws org.apache.roller.RollerException on any error
     */
    protected JDORollerImpl() throws RollerException {
        strategy = new JDOPersistenceStrategy();
    }

    /**
     * Construct and return the singleton instance of the class.
     * @throws org.apache.roller.RollerException on any error
     * @return the singleton
     */
    public static Roller instantiate() throws RollerException {
        if (me == null) {
            logger.debug("Instantiating DatamapperRollerImpl");
            me = new JDORollerImpl();
        }

        return me;
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

    /**
     * Create an instance of UserManager.
     */
    protected UserManager createUserManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of BookmarkManager.
     */
    protected BookmarkManager createBookmarkManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of
     */
    protected static DatamapperRollerImpl createDatamapperRollerImpl() {
        return null;
    }

    /**
     * Create an instance of PingTargetManager.
     */
    protected PingTargetManager createPingTargetManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of WeblogManager.
     */
    protected WeblogManager createWeblogManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of AutoPingManager.
     */
    protected AutoPingManager createAutoPingManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of RollerPlanetManager.
     */
    protected PlanetManager createRollerPlanetManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of PingQueueManager.
     */
    protected PingQueueManager createPingQueueManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of PropertiesManager.
     */
    protected PropertiesManager createPropertiesManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Create an instance of ReferrerManager.
     */
    protected RefererManager createReferrerManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        return null;
    }

    /**
     * Release resources associated with this Roller instance.
     */
    public void release() {
        super.release();
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        super.shutdown();
    }

    /**
     * Create an instance of PlanetManager.
     * @param strategy the PersistenceStrategy
     * @return the PlanetManager
     */
    protected PlanetManager createPlanetManagerImpl(DatamapperPersistenceStrategy strategy) {
    }
}