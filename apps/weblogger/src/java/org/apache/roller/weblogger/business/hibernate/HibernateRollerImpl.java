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

package org.apache.roller.weblogger.business.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.RollerImpl;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.Roller;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.hibernate.HibernatePersistenceStrategy;

/**
 * A Hibernate specific implementation of the Roller business layer.
 */
public class HibernateRollerImpl extends RollerImpl {
    
    static final long serialVersionUID = 5256135928578074652L;
    
    private static Log mLogger = LogFactory.getLog(HibernateRollerImpl.class);    
    
    // a persistence utility class
    private HibernatePersistenceStrategy strategy = null;
    
    // references to the managers we maintain
    private BookmarkManager   bookmarkManager = null;
    private PropertiesManager propertiesManager = null;
    private RefererManager    refererManager = null;
    private UserManager       userManager = null;
    private WeblogManager     weblogManager = null;
    private PingQueueManager  pingQueueManager = null;
    private AutoPingManager   autoPingManager = null;
    private PingTargetManager pingTargetManager = null;
    private ThreadManager     threadManager = null;
    
    
    protected HibernateRollerImpl() throws WebloggerException {
        try {
            String dialect =  
                RollerConfig.getProperty("hibernate.dialect");
            String connectionProvider = 
                RollerConfig.getProperty("hibernate.connectionProvider");
            strategy = new HibernatePersistenceStrategy(
                "/hibernate.cfg.xml", dialect, connectionProvider);
        } catch(Throwable t) {
            // if this happens then we are screwed
            mLogger.fatal("Error initializing Hibernate", t);
            throw new WebloggerException(t);
        }
    }
    
    
    /**
     * Instantiates and returns an instance of HibernateRollerImpl.
     */
    public static Roller instantiate() throws WebloggerException {
        mLogger.debug("Instantiating HibernateRollerImpl");
        return new HibernateRollerImpl();
    }
    
    
    public void flush() throws WebloggerException {
        this.strategy.flush();
    }
    
    
    public void release() {
        
        // release our own stuff first
        if (bookmarkManager != null) bookmarkManager.release();
        if (refererManager != null) refererManager.release();
        if (userManager != null) userManager.release();
        if (weblogManager != null) weblogManager.release();
        if (pingTargetManager != null) pingTargetManager.release();
        if (pingQueueManager != null) pingQueueManager.release();
        if (autoPingManager != null) autoPingManager.release();
        if( threadManager != null) threadManager.release();
        
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
     * @see org.apache.roller.weblogger.model.Roller#getUserManager()
     */
    public UserManager getUserManager() {
        if ( userManager == null ) {
            userManager = new HibernateUserManagerImpl(strategy);
        }
        return userManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() {
        if ( bookmarkManager == null ) {
            bookmarkManager = new HibernateBookmarkManagerImpl(strategy);
        }
        return bookmarkManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() {
        if ( weblogManager == null ) {
            weblogManager = new HibernateWeblogManagerImpl(strategy);
        }
        return weblogManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() {
        if ( refererManager == null ) {
            refererManager = new HibernateRefererManagerImpl(strategy);
        }
        return refererManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() {
        if (propertiesManager == null) {
            propertiesManager = new HibernatePropertiesManagerImpl(strategy);
        }
        return propertiesManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() {
        if (pingQueueManager == null) {
            pingQueueManager = new HibernatePingQueueManagerImpl(strategy);
        }
        return pingQueueManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() {
        if (autoPingManager == null) {
            autoPingManager = new HibernateAutoPingManagerImpl(strategy);
        }
        return autoPingManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() {
        if (pingTargetManager == null) {
            pingTargetManager = new HibernatePingTargetManagerImpl(strategy);
        }
        return pingTargetManager;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        if (threadManager == null) {
            threadManager = new HibernateThreadManagerImpl(strategy);
        }
        return threadManager;
    }
    
}
