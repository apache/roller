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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.business.URLStrategy;
import org.apache.roller.planet.business.datamapper.DatamapperPlanetImpl;
import org.apache.roller.planet.business.datamapper.DatamapperPlanetManagerImpl;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;


/**
 * Implements Planet, the entry point interface for the Roller-Planet business 
 * tier APIs using the Java Persistence API (JPA).
 */
public class JPAPlanetImpl extends DatamapperPlanetImpl {   
    
    private static Log log = LogFactory.getLog(JPAPlanetImpl.class);
    
    // our singleton instance
    private static JPAPlanetImpl me = null;
        
    // references to the managers we maintain
    protected PlanetManager planetManager = null;
    
    // url strategy
    protected URLStrategy urlStrategy = null;
    
        
    protected JPAPlanetImpl() throws RollerException {
        // set strategy used by Datamapper
        strategy = new JPAPersistenceStrategy("PlanetPU");
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
}
