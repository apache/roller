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

package org.apache.roller.planet.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.util.DatabaseProvider;
import org.apache.roller.util.DatabaseProvider.ConfigurationType;
import org.apache.roller.util.DatabaseProviderException;


/**
 * Encapsulates Roller database configuration via JDBC properties or JNDI.
 *
 * <p>Reads configuration properties from RollerConfig:</p>
 * <pre>
 * # Specify database configuration type of 'jndi' or 'jdbc'
 * database.configurationType=jndi
 * 
 * # For database configuration type 'jndi',this will be used
 * database.jndi.name=jdbc/rollerdb
 * 
 * # For database configuration type of 'jdbc', you MUST override these
 * database.jdbc.driverClass=
 * database.jdbc.connectionURL=
 * database.jdbc.username=
 * database.jdbc.password=
 * </pre>
 */
@com.google.inject.Singleton
public class PlanetDatabaseProvider extends DatabaseProvider {
    private static Log log = LogFactory.getLog(PlanetDatabaseProvider.class);
   
    /**
     * Contruct DatabseProvider using RollerConfig properties. 
     */ 
    private PlanetDatabaseProvider() throws DatabaseProviderException {
        String connectionTypeString = 
                PlanetConfig.getProperty("database.configurationType"); 
        if ("jdbc".equals(connectionTypeString)) {
            type = ConfigurationType.JDBC_PROPERTIES;
        }
        jndiName =          PlanetConfig.getProperty("database.jndi.name");
        jdbcDriverClass =   PlanetConfig.getProperty("database.jdbc.driverClass");
        jdbcConnectionURL = PlanetConfig.getProperty("database.jdbc.connectionURL");
        jdbcUsername =      PlanetConfig.getProperty("database.jdbc.username");
        jdbcPassword =      PlanetConfig.getProperty("database.jdbc.password");
        
        init(type, jndiName, jdbcDriverClass, jdbcConnectionURL, jdbcUsername, jdbcPassword);
    }
}
