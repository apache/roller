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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.hibernate.HibernatePlanetImpl;

/**
 * Roller specific Planet implementation, uses RollerConfig to configure Hibernate.
 */
public class HibernateRollerPlanetImpl extends HibernatePlanetImpl {
    private static Log log = LogFactory.getLog(HibernateRollerPlanetImpl.class);

    // TODO_GUICE
    
    
    public HibernateRollerPlanetImpl(HibernatePersistenceStrategy strategy) throws PlanetException {
        super();
    }
    
    protected HibernatePersistenceStrategy getStrategy() throws PlanetException {
        return strategy;
    }    
    
    /**
     * Instantiates and returns an instance of HibernatePlanetImpl.
     */
    public static Planet instantiate() throws PlanetException {
        if (me == null) {
            log.debug("Instantiating HibernatePlanetImpl");
            me = new HibernatePlanetImpl();
        }
        
        return me;
    }   
}
