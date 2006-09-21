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
import org.apache.roller.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.planet.business.hibernate.HibernatePlanetManagerImpl;
import org.apache.roller.planet.model.PlanetManager;
import org.apache.roller.planet.model.Planet;


/**
 * A Hibernate specific implementation of the Roller business layer.
 */
public class HibernatePlanetImpl implements Planet {   
    
    private static Log mLogger = LogFactory.getLog(HibernatePlanetImpl.class);
    
    // our singleton instance
    private static HibernatePlanetImpl me = null;
    
    // a persistence utility class
    private HibernatePersistenceStrategy strategy = null;
    
    // references to the managers we maintain
    private PlanetManager planetManager = null;
    
    
    protected HibernatePlanetImpl() throws RollerException {
        try {
            strategy = new HibernatePersistenceStrategy();
        } catch(Throwable t) {
            // if this happens then we are screwed
            mLogger.fatal("Error initializing Hibernate", t);
            throw new RollerException(t);
        }
    }
    
    
    /**
     * Instantiates and returns an instance of HibernatePlanetImpl.
     */
    public static Planet instantiate() throws RollerException {
        if (me == null) {
            mLogger.debug("Instantiating HibernatePlanetImpl");
            me = new HibernatePlanetImpl();
        }
        
        return me;
    }
    
    
    public PlanetManager getPlanetManager() throws RollerException {
        if ( planetManager == null ) {
            planetManager = new HibernatePlanetManagerImpl(strategy);  
        }
        return planetManager;
    }
    
    
    public void flush() throws RollerException {
        this.strategy.flush();
    }
    
    
    public void release() {
                
        // tell Hibernate to close down
        this.strategy.release();
    }
    
    
    public void shutdown() {
        
        // do our own shutdown first
        this.release();
    }
    
}
