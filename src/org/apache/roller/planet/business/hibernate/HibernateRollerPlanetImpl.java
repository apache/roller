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
package org.apache.roller.planet.business.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.planet.business.Planet;

/**
 * Roller specific Planet implementation, uses RollerConfig to configure Hibernate.
 */
public class HibernateRollerPlanetImpl extends HibernatePlanetImpl {
    private static Log log = LogFactory.getLog(HibernateRollerPlanetImpl.class);
    
    public HibernateRollerPlanetImpl() throws RollerException {
        super();
    }
    
    protected HibernatePersistenceStrategy getStrategy() throws RollerException {
        try {
            String dialect =  
                RollerConfig.getProperty("hibernate.dialect");
            String connectionProvider = 
                RollerConfig.getProperty("hibernate.connectionProvider");
            return new HibernatePersistenceStrategy(
                "/planet-hibernate.cfg.xml", dialect, connectionProvider);

        } catch(Throwable t) {
            // if this happens then we are screwed
            log.fatal("Error initializing Hibernate", t);
            throw new RollerException(t);
        }        
    }
    
    
    /**
     * Instantiates and returns an instance of HibernatePlanetImpl.
     */
    public static Planet instantiate() throws RollerException {
        if (me == null) {
            log.debug("Instantiating HibernatePlanetImpl");
            me = new HibernatePlanetImpl();
        }
        
        return me;
    }   
}
