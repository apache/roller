
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
package org.apache.roller.business.datamapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;

/**
 * A Datamapper specific implementation of the Roller business layer.
 */
public abstract class DatamapperPlanetImpl implements Planet {

    protected static Log logger = LogFactory.getLog(DatamapperPlanetImpl.class);

    // our singleton instance
    private static DatamapperPlanetImpl me = null;

    // a persistence utility class
    protected DatamapperPersistenceStrategy strategy = null;

    // references to the managers we maintain
    private PlanetManager planetManager = null;

    
    protected DatamapperPlanetImpl() throws RollerException {
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
            planetManager = createDatamapperPlanetManager(strategy);
        }
        return planetManager;
    }

    protected PlanetManager createDatamapperPlanetManager(
            DatamapperPersistenceStrategy strategy) {
        return new DatamapperPlanetManagerImpl(strategy);
    }    
}
