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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * JPA strategy for Planet, uses PlanetConfig to get JPA configuration.
 */
@com.google.inject.Singleton
public class JPARollerPlanetPersistenceStrategy extends JPAPersistenceStrategy {
    
    private static Log logger = 
        LogFactory.getFactory().getInstance(JPARollerPlanetPersistenceStrategy.class); 
    
    
    /**
     * Uses database configuration information from WebloggerConfig (i.e. roller-custom.properties)
     * 
     * @throws org.apache.roller.PlanetException on any error
     */
    protected JPARollerPlanetPersistenceStrategy() throws PlanetException { 

        DatabaseProvider dbProvider = WebloggerStartup.getDatabaseProvider();
                        
        String jpaConfigurationType = PlanetConfig.getProperty("jpa.configurationType");
        if ("jndi".equals(jpaConfigurationType)) {
        	// If JNDI configuration type specified in Planet Config then use it
            // Lookup EMF via JNDI: added for Geronimo
            String emfJndiName = "java:comp/env/" + PlanetConfig.getProperty("jpa.emf.jndi.name");
            try {
                emf = (EntityManagerFactory) new InitialContext().lookup(emfJndiName);
            } catch (NamingException e) {
                throw new PlanetException("Could not look up EntityManagerFactory in jndi at " + emfJndiName, e);
            }
            
        } else {
        	
            // Add all JPA, OpenJPA, HibernateJPA, etc. properties found
            Properties emfProps = new Properties();
            Enumeration keys = WebloggerConfig.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (       key.startsWith("javax.persistence.") 
                        || key.startsWith("openjpa.") 
                        || key.startsWith("hibernate.")) {
                    String value = WebloggerConfig.getProperty(key);
                    logger.info(key + ": " + value);
                    emfProps.setProperty(key, value);
                }
            }
	        
            if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
                emfProps.setProperty("javax.persistence.nonJtaDataSource", dbProvider.getJndiName());

            } else {
                emfProps.setProperty("javax.persistence.jdbc.driver", dbProvider.getJdbcDriverClass());
                emfProps.setProperty("javax.persistence.jdbc.url", dbProvider.getJdbcConnectionURL());
                emfProps.setProperty("javax.persistence.jdbc.user", dbProvider.getJdbcUsername());
                emfProps.setProperty("javax.persistence.jdbc.password", dbProvider.getJdbcPassword());
            }
            
            try {
                emf = Persistence.createEntityManagerFactory("PlanetPU", emfProps);
            } catch (PersistenceException pe) {
                logger.error("ERROR: creating entity manager", pe);
                throw new PlanetException(pe);
            }
        }        
    }  
}
