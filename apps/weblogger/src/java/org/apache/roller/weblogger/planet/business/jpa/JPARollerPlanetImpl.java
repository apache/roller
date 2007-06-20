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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.planet.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.jpa.JPAPlanetImpl;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;


/**
 * Roller specific Planet implementation, uses RollerConfig to configure JPA.
 */
public class JPARollerPlanetImpl extends JPAPlanetImpl {
    private static Log log = LogFactory.getLog(JPARollerPlanetImpl.class);
    
    public JPARollerPlanetImpl() throws PlanetException {
        super();
    }
    
    protected JPAPersistenceStrategy getStrategy() throws PlanetException {
        
        // Add OpenJPA, Toplink and Hibernate properties to Roller config.
        Properties props = new Properties();
        Enumeration keys = PlanetConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("openjpa.") || key.startsWith("toplink.")) {
                String value = PlanetConfig.getProperty(key);
                log.info(key + ": " + value);
                props.setProperty(key, value);
            }
        }
        
        DatabaseProvider dbProvider = WebloggerStartup.getDatabaseProvider();
        
        if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
            return new JPAPersistenceStrategy(
                "PlanetPU", "java:comp/env/" + dbProvider.getJndiName(), props); 
        } else {
            return new JPAPersistenceStrategy(
                "PlanetPU",  
                RollerConfig.getProperty("database.jdbc.driverClass"),
                RollerConfig.getProperty("database.jdbc.connectionURL"),
                RollerConfig.getProperty("database.jdbc.username"),
                RollerConfig.getProperty("database.jdbc.password"), props);
        }
    }
    
    /**
     * Instantiates and returns an instance of JPAPlanetImpl.
     */
    public static Planet instantiate() throws PlanetException {
        if (me == null) {
            log.debug("Instantiating JPAPlanetImpl");
            me = new JPARollerPlanetImpl();
        }
        
        return me;
    }
    
}
