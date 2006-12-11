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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.hibernate.HibernatePersistenceStrategy;


/**
 * A Hibernate specific implementation of the Roller business layer.
 */
public class HibernateRollerPlanetImpl extends HibernatePlanetImpl {   
    
    private static Log log = LogFactory.getLog(HibernateRollerPlanetImpl.class);
    
    // our singleton instance
    private static HibernateRollerPlanetImpl me = null;
    
        
    /**
     * Create HibernatePlanetImpl using Hibernate XML config file or config
     * file plus JDBC overrides from planet-custom.properties.
     */
    protected HibernateRollerPlanetImpl() throws RollerException {
        super();
    }
    
    
    /**
     * Instantiates and returns an instance of HibernatePlanetImpl.
     */
    public static Planet instantiate() throws RollerException {
        if (me == null) {
            log.debug("Instantiating HibernateRollerPlanetImpl");
            me = new HibernateRollerPlanetImpl();
        }
        
        return me;
    }
    
    
    public PlanetManager getPlanetManager() {
        if ( planetManager == null ) {
            planetManager = new HibernateRollerPlanetManagerImpl(strategy);  
        }
        return planetManager;
    }
    
}
