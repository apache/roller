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

package org.apache.roller.weblogger.planet.business.jpa;

import java.util.Enumeration;
import java.util.Properties;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.RollerConfig;

/**
 * JPA strategy for Planet, uses RollerConfig to get JPA configuration.
 */
@com.google.inject.Singleton
public class JPARollerPlanetPersistenceStrategy extends JPAPersistenceStrategy {
    private static Log logger = 
        LogFactory.getFactory().getInstance(JPARollerPlanetPersistenceStrategy.class); 
    
    /**
     * Construct by finding using DatabaseProvider and RollerConfig.
     * @throws org.apache.roller.PlanetException on any error
     */
    protected JPARollerPlanetPersistenceStrategy() throws PlanetException { 

        DatabaseProvider dbProvider = WebloggerStartup.getDatabaseProvider();
        
        // Pull in any properties defined in JMAEMF.properties config file
        Properties emfProps = loadPropertiesFromResourceName(
           "JPAEMF.properties", getContextClassLoader());
                
        // Add all OpenJPA and Toplinks properties found in RollerConfig
        Enumeration keys = RollerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("openjpa.") || key.startsWith("toplink.")) {
                String value = RollerConfig.getProperty(key);
                logger.info(key + ": " + value);
                emfProps.setProperty(key, value);
            }
        }
        
        if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
            // We're doing JNDI, so set OpenJPA JNDI name property
            String jndiName = "java:comp/env/" + dbProvider.getJndiName();
            emfProps.setProperty("openjpa.ConnectionFactoryName", jndiName);
            
        } else {
            // So set JDBD properties for OpenJPA
            emfProps.setProperty("openjpa.ConnectionDriverName",     dbProvider.getJdbcDriverClass());
            emfProps.setProperty("openjpa.ConnectionURL",            dbProvider.getJdbcConnectionURL());
            emfProps.setProperty("openjpa.ConnectionUserName",       dbProvider.getJdbcUsername());
            emfProps.setProperty("openjpa.ConnectionPassword",       dbProvider.getJdbcPassword()); 

            // And Toplink JPA
            emfProps.setProperty("toplink.jdbc.driver",              dbProvider.getJdbcDriverClass());
            emfProps.setProperty("toplink.jdbc.url",                 dbProvider.getJdbcConnectionURL());
            emfProps.setProperty("toplink.jdbc.user",                dbProvider.getJdbcUsername());
            emfProps.setProperty("toplink.jdbc.password",            dbProvider.getJdbcPassword());

            // And Hibernate JPA
            emfProps.setProperty("hibernate.connection.driver_class",dbProvider.getJdbcDriverClass());
            emfProps.setProperty("hibernate.connection.url",         dbProvider.getJdbcConnectionURL());
            emfProps.setProperty("hibernate.connection.username",    dbProvider.getJdbcUsername());
            emfProps.setProperty("hibernate.connection.password",    dbProvider.getJdbcPassword()); 
        }
        
        try {
            this.emf = Persistence.createEntityManagerFactory("PlanetPU", emfProps);
        } catch (PersistenceException pe) {
            logger.error("ERROR: creating entity manager", pe);
            throw new PlanetException(pe);
        }
    }  
}
