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
/*
 * Created on Feb 23, 2003
 */
package org.roller.business.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.RollerImpl;
import org.roller.model.BookmarkManager;
import org.roller.model.ConfigManager;
import org.roller.model.AutoPingManager;
import org.roller.model.PingQueueManager;
import org.roller.model.PingTargetManager;
import org.roller.model.PlanetManager;
import org.roller.model.PropertiesManager;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;


/**
 * A Hibernate specific implementation of the Roller business layer.
 */
public class HibernateRollerImpl extends RollerImpl {
    
    static final long serialVersionUID = 5256135928578074652L;
    
    private static Log mLogger = LogFactory.getLog(HibernateRollerImpl.class);
    
    // our singleton instance
    private static HibernateRollerImpl me = null;
    
    // a persistence utility class
    private HibernatePersistenceStrategy strategy = null;
    
    // references to the managers we maintain
    private BookmarkManager bookmarkManager = null;
    private ConfigManager configManager = null;
    private PropertiesManager propertiesManager = null;
    private PlanetManager planetManager = null;
    private RefererManager refererManager = null;
    private UserManager userManager = null;
    private WeblogManager weblogManager = null;
    private PingQueueManager pingQueueManager = null;
    private AutoPingManager autoPingManager = null;
    private PingTargetManager pingTargetManager = null;
    
    
    protected HibernateRollerImpl() throws RollerException {
        try {
            strategy = new HibernatePersistenceStrategy();
        } catch(Throwable t) {
            // if this happens then we are screwed
            throw new RollerException(t);
        }
    }
    
    
    /**
     * Instantiates and returns an instance of HibernateRollerImpl.
     */
    public static Roller instantiate() throws RollerException {
        if (me == null) {
            mLogger.debug("Instantiating HibernateRollerImpl");
            me = new HibernateRollerImpl();
        }
        
        return me;
    }
    
    
    public void flush() throws RollerException {
        this.strategy.flush();
    }
    
    
    public void release() {
        
        // release our own stuff first
        if (bookmarkManager != null) bookmarkManager.release();
        if (configManager != null) configManager.release();
        if (refererManager != null) refererManager.release();
        if (userManager != null) userManager.release();
        if (weblogManager != null) weblogManager.release();
        if (pingTargetManager != null) pingTargetManager.release();
        if (pingQueueManager != null) pingQueueManager.release();
        if (autoPingManager != null) autoPingManager.release();
        
        // tell Hibernate to close down
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
     * @see org.roller.model.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException {
        if ( userManager == null ) {
            userManager = new HibernateUserManagerImpl(strategy);
        }
        return userManager;
    }
    
    /**
     * @see org.roller.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException {
        if ( bookmarkManager == null ) {
            bookmarkManager = new HibernateBookmarkManagerImpl(strategy);
        }
        return bookmarkManager;
    }
    
    /**
     * @see org.roller.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException {
        if ( weblogManager == null ) {
            weblogManager = new HibernateWeblogManagerImpl(strategy);
        }
        return weblogManager;
    }
    
    /**
     * @see org.roller.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException {
        if ( refererManager == null ) {
            refererManager = new HibernateRefererManagerImpl(strategy);
        }
        return refererManager;
    }
    
    /**
     * @see org.roller.model.Roller#getConfigManager()
     */
    public ConfigManager getConfigManager() throws RollerException {
        if (configManager == null) {
            configManager = new HibernateConfigManagerImpl(strategy);
        }
        return configManager;
    }
    
    /**
     * @see org.roller.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() throws RollerException {
        if (propertiesManager == null) {
            propertiesManager = new HibernatePropertiesManagerImpl(strategy);
        }
        return propertiesManager;
    }
    
    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() throws RollerException {
        if (pingQueueManager == null) {
            pingQueueManager = new HibernatePingQueueManagerImpl(strategy);
        }
        return pingQueueManager;
    }
    
    /**
     * @see org.roller.model.Roller#getPlanetManager()
     */
    public PlanetManager getPlanetManager() throws RollerException {
        if ( planetManager == null ) {
            planetManager = new HibernateRollerPlanetManagerImpl(strategy);
        }
        return planetManager;
    }
    
    
    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() throws RollerException {
        if (autoPingManager == null) {
            autoPingManager = new HibernateAutoPingManagerImpl(strategy);
        }
        return autoPingManager;
    }
    
    
    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() throws RollerException {
        if (pingTargetManager == null) {
            pingTargetManager = new HibernatePingTargetManagerImpl(strategy);
        }
        return pingTargetManager;
    }
    
}
