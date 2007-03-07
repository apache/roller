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

package org.apache.roller.planet.business.jpa;

import java.io.InputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.URLStrategy;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.planet.business.PropertiesManager;


/**
 * Implements Planet, the entry point interface for the Roller-Planet business 
 * tier APIs using the Java Persistence API (JPA).
 */
public class JPAPlanetImpl implements Planet {   
    
    private static Log log = LogFactory.getLog(JPAPlanetImpl.class);
    
    // a persistence utility class
    protected JPAPersistenceStrategy strategy = null;
    
    // our singleton instance
    private static JPAPlanetImpl me = null;
        
    // references to the managers we maintain
    private PlanetManager planetManager = null;
    private PropertiesManager propertiesManager = null;
    
    // url strategy
    protected URLStrategy urlStrategy = null;
    
        
    protected JPAPlanetImpl() throws RollerException {
        
        // set strategy used by Datamapper
        // You can configure JPA completely via the JPAEMF.properties file
        Properties emfProps = loadPropertiesFromResourceName(
           "JPAEMF.properties", getContextClassLoader());
        
        // Or add OpenJPA, Toplink and Hibernate properties to Roller config.
        // These override properties set so far.
        Enumeration keys = PlanetConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("openjpa.") || key.startsWith("toplink.")) {
                String value = PlanetConfig.getProperty(key);
                log.info(key + ": " + value);
                emfProps.setProperty(key, value);
            }
        }

        // Or, for plain-old JDBC, use Roller's jdbc properties and
        // we'll convert them to OpenJPA, Toplink and Hibernate properties.
        // These override properties set so far.
        if (StringUtils.isNotEmpty(PlanetConfig.getProperty("jdbc.driverClass"))) {
            
            String driver =   PlanetConfig.getProperty("jdbc.driverClass");
            String url =      PlanetConfig.getProperty("jdbc.connectionURL");
            String username = PlanetConfig.getProperty("jdbc.username");
            String password = PlanetConfig.getProperty("jdbc.password");
            log.info("driverClass:    " + driver);
            log.info("connectionURL:  " + url);
            log.info("username:       " + username);         
            
            emfProps.setProperty("openjpa.ConnectionDriverName", driver);
            emfProps.setProperty("openjpa.ConnectionURL", url);
            emfProps.setProperty("openjpa.ConnectionUserName", username);
            emfProps.setProperty("openjpa.ConnectionPassword", password); 
           
            emfProps.setProperty("toplink.jdbc.driver", driver);
            emfProps.setProperty("toplink.jdbc.url", url);
            emfProps.setProperty("toplink.jdbc.user", username);
            emfProps.setProperty("toplink.jdbc.password", password);
            
            emfProps.setProperty("hibernate.connection.driver_class", driver);
            emfProps.setProperty("hibernate.connection.url", url);
            emfProps.setProperty("hibernate.connection.username", username);
            emfProps.setProperty("hibernate.connection.password", password);      
        } 
        
        strategy = new JPAPersistenceStrategy("PlanetPU", emfProps);
    }
    
    
    /**
     * Instantiates and returns an instance of JPAPlanetImpl.
     */
    public static Planet instantiate() throws RollerException {
        if (me == null) {
            log.debug("Instantiating JPAPlanetImpl");
            me = new JPAPlanetImpl();
        }
        
        return me;
    }    

    public URLStrategy getURLStrategy() {
        return this.urlStrategy;
    }
    
    public void setURLStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
        log.info("Using URLStrategy: " + urlStrategy.getClass().getName());
    }
    
        public void flush() throws RollerException {
        this.strategy.flush();
    }

    
    public void release() {

        // release our own stuff first
        //if (planetManager != null) planetManager.release();

        // tell Datamapper to close down
        this.strategy.release();
    }

    
    public void shutdown() {

        // do our own shutdown first
        this.release();
    }
    
    /**
     * @see org.apache.roller.business.Roller#getBookmarkManager()
     */
    public PlanetManager getPlanetManager() {
        if ( planetManager == null ) {
            planetManager = createPlanetManager(strategy);
        }
        return planetManager;
    }

    protected PlanetManager createPlanetManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPlanetManagerImpl(strategy);
    }    
    
    /**
     * @see org.apache.roller.business.Roller#getBookmarkManager()
     */
    public PropertiesManager getPropertiesManager() {
        if ( propertiesManager == null ) {
            propertiesManager = createPropertiesManager(strategy);
        }
        return propertiesManager;
    }

    protected PropertiesManager createPropertiesManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPropertiesManagerImpl(strategy);
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
