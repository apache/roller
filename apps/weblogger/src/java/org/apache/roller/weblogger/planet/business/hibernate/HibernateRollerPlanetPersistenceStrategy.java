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

package org.apache.roller.weblogger.planet.business.hibernate;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.weblogger.config.RollerConfig;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;


/**
 * Hibernate strategy for Planet, uses RollerConfig to get Hibernate configuration.
 */
@com.google.inject.Singleton
public class HibernateRollerPlanetPersistenceStrategy extends HibernatePersistenceStrategy {
    
    
    /**
     * Persistence strategy configures itself by using 'planet-hibernate.cfg.xml' 
     * and Roller properties: 'hibernate.dialect' - the classname of the Hibernate 
     * dialect to be used, 'hibernate.connectionProvider - the classname of 
     * Roller's connnection provider impl.
     */
    protected HibernateRollerPlanetPersistenceStrategy() throws PlanetException {
        
        String dialect =  
            RollerConfig.getProperty("hibernate.dialect");
        String connectionProvider = 
            RollerConfig.getProperty("hibernate.connectionProvider");        
        
        Configuration config = new Configuration();
        config.configure("/META-INF/planet-hibernate.cfg.xml");

        // Add dialect specified by Roller config and our connection provider
        Properties props = new Properties();
        props.put(Environment.DIALECT, dialect);
        props.put(Environment.CONNECTION_PROVIDER, connectionProvider);
        config.mergeProperties(props);
        
        sessionFactory = config.buildSessionFactory(); 
    }
    
}
