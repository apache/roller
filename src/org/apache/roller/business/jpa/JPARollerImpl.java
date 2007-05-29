
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
package org.apache.roller.business.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.DatabaseProvider;
import org.apache.roller.business.PropertiesManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerImpl;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.business.runnable.ThreadManager;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingQueueManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.config.RollerConfig;


/**
 * A JPA specific implementation of the Roller business layer.
 */
public class JPARollerImpl extends RollerImpl {

    static final long serialVersionUID = 5256135928578074652L;

    protected static Log logger = LogFactory.getLog(JPARollerImpl.class);

    // our singleton instance
    private static JPARollerImpl me = null;

    // a persistence utility class
    protected JPAPersistenceStrategy strategy = null;

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
    
    /**
     * Single constructor.
     * @throws org.apache.roller.RollerException on any error
     */
    protected JPARollerImpl() throws RollerException {
        
        // Add OpenJPA, Toplink and Hibernate properties to Roller config.
        Properties props = new Properties();
        Enumeration keys = RollerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("openjpa.") || key.startsWith("toplink.")) {
                String value = RollerConfig.getProperty(key);
                logger.info(key + ": " + value);
                props.setProperty(key, value);
            }
        }
        
        DatabaseProvider dbProvider = DatabaseProvider.getDatabaseProvider();
        if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
            strategy = new JPAPersistenceStrategy(
                "RollerPU", "java:comp/env/" + dbProvider.getJndiName(), props); 
        } else {
            strategy = new JPAPersistenceStrategy(
                "RollerPU",  
                dbProvider.getJdbcDriverClass(),
                dbProvider.getJdbcConnectionURL(),
                dbProvider.getJdbcUsername(),
                dbProvider.getJdbcPassword(), 
                props);
        }
    }
    
    protected UserManager createJPAUserManager(JPAPersistenceStrategy strategy) {
        return new JPAUserManagerImpl((JPAPersistenceStrategy) strategy);
    }

    protected WeblogManager createJPAWeblogManager(
            JPAPersistenceStrategy strategy) {
        return new JPAWeblogManagerImpl((JPAPersistenceStrategy) strategy);
    }

    protected ThreadManager createJPAThreadManager(
            JPAPersistenceStrategy strategy) {
        return new JPAThreadManagerImpl((JPAPersistenceStrategy) strategy);
    }

    protected RefererManager createJPARefererManagerImpl(JPAPersistenceStrategy strategy) {
        return new JPARefererManagerImpl((JPAPersistenceStrategy) strategy);
    }

    /**
     * Construct and return the singleton instance of the class.
     * @throws org.apache.roller.RollerException on any error
     * @return the singleton
     */
    public static Roller instantiate() throws RollerException {
        logger.debug("Instantiating JPARollerImpl");
        Roller roller = new JPARollerImpl();

        // Now that Roller has been instantiated, initialize individual managers
        roller.getPropertiesManager();
        roller.getIndexManager();
        roller.getThemeManager();          
        return roller;
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

        // tell JPA to close down
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
            autoPingManager = createJPAAutoPingManager(strategy);
        }
        return autoPingManager;
    }

    protected AutoPingManager createJPAAutoPingManager(
            JPAPersistenceStrategy strategy) {
        return new JPAAutoPingManagerImpl(strategy);
    }
    
    /**
     * @see org.apache.roller.business.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() {
        if ( bookmarkManager == null ) {
            bookmarkManager = createJPABookmarkManager(strategy);
        }
        return bookmarkManager;
    }

    protected BookmarkManager createJPABookmarkManager(
            JPAPersistenceStrategy strategy) {
        return new JPABookmarkManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() {
        if (pingQueueManager == null) {
            pingQueueManager = createJPAPingQueueManager(strategy);
        }
        return pingQueueManager;
    }

    protected PingQueueManager createJPAPingQueueManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPingQueueManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() {
        if (pingTargetManager == null) {
            pingTargetManager = createJPAPingTargetManager(strategy);
        }
        return pingTargetManager;
    }

    protected PingTargetManager createJPAPingTargetManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPingTargetManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() {
        if (propertiesManager == null) {
            propertiesManager = createJPAPropertiesManager(strategy);
        }
        return propertiesManager;
    }

    protected PropertiesManager createJPAPropertiesManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPropertiesManagerImpl(strategy);
    }

    /**
     * @see org.apache.roller.business.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() {
        if ( referrerManager == null ) {
            referrerManager = createJPARefererManagerImpl(strategy);
        }
        return referrerManager;
    }

    /**
     * @see org.apache.roller.business.Roller#getUserManager()
     */
    public UserManager getUserManager() {
        if ( userManager == null ) {
            userManager = createJPAUserManager(strategy);
        }
        return userManager;
    }

    /**
     * @see org.apache.roller.business.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() {
        if ( weblogManager == null ) {
            weblogManager = createJPAWeblogManager(strategy);
        }
        return weblogManager;
    }

    /**
     * @see org.apache.roller.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() {
        if (threadManager == null) {
            threadManager = createJPAThreadManager(strategy);
        }
        return threadManager;
    }
        
    /**
     * Loads properties from given resourceName using given class loader
     * @param resourceName The name of the resource containing properties
     * @param cl Classloeder to be used to locate the resouce
     * @return A properties object
     * @throws RollerException
     */
    private static Properties loadPropertiesFromResourceName(
            String resourceName, ClassLoader cl) throws RollerException {
        Properties props = new Properties();
        InputStream in = null;
        in = cl.getResourceAsStream(resourceName);
        if (in == null) {
            //TODO: Check how i18n is done in roller
            throw new RollerException(
                    "Could not locate properties to load " + resourceName);
        }
        try {
            props.load(in);
        } catch (IOException ioe) {
            throw new RollerException(
                    "Could not load properties from " + resourceName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        
        return props;
    }
    
    /**
     * Get the context class loader associated with the current thread. This is
     * done in a doPrivileged block because it is a secure method.
     * @return the current thread's context class loader.
     */
    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
}
