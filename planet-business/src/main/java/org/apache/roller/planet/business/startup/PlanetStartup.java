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

package org.apache.roller.planet.business.startup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.DatabaseProvider;


/**
 * Manages Roller Planet startup process.
 */
public final class PlanetStartup {
    
    private static final Log log = LogFactory.getLog(PlanetStartup.class);
    
    private static boolean prepared = false;
    
    private static DatabaseProvider dbProvider = null;
    private static StartupException dbProviderException = null;
    
    
    // non-instantiable
    private PlanetStartup() {}
    
    
    /**
     * Is the Roller Weblogger app properly prepared to be bootstrapped?
     */
    public static boolean isPrepared() {
        return prepared;
    }
    
    
    /**
     * Get a reference to the currently configured DatabaseProvider.
     *
     * @return DatabaseProvider The configured database provider.
     * @throws IllegalStateException If the app has not been properly prepared yet.
     */
    public static DatabaseProvider getDatabaseProvider() {
        if (dbProvider == null) {
            throw new IllegalStateException("Roller Planet has not been prepared yet");
        }
        return dbProvider;
    }
    
    
    /**
     * Get a reference to the exception thrown while instantiating the 
     * database provider, if any.
     *
     * @return StartupException Exception from db provider, or null if no exception thrown.
     */
    public static StartupException getDatabaseProviderException() {
        return dbProviderException;
    }

    
    /**
     * Run the Roller Weblogger preparation sequence.
     *
     * This sequence is what prepares the core services of the application such
     * as setting up the database and mail providers.
     */
    public static void prepare() throws StartupException {
        
        // setup database provider
        try {
            dbProvider = new DatabaseProvider();
        } catch(StartupException ex) { 
            dbProviderException = ex;
            throw ex;
        }   
        
        prepared = true;
    }
    
}
