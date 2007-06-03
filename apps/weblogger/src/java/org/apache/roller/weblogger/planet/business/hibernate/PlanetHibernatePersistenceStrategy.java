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

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.weblogger.config.RollerConfig;

/**
 * Base class for Hibernate persistence implementation.
 *
 * This class serves as a helper/util class for all of the Hibernate
 * manager implementations by providing a set of basic persistence methods
 * that can be easily reused.
 */
@com.google.inject.Singleton
public class PlanetHibernatePersistenceStrategy extends HibernatePersistenceStrategy {
    
    /**
     * Persistence strategy configures itself by using Roller properties:
     * 'hibernate.configResource' - the resource name of Roller's Hibernate XML configuration file, 
     * 'hibernate.dialect' - the classname of the Hibernate dialect to be used,
     * 'hibernate.connectionProvider - the classname of Roller's connnection provider impl.
     */
    protected PlanetHibernatePersistenceStrategy() throws WebloggerException {        
        String dialect =  
            RollerConfig.getProperty("hibernate.dialect");
        String connectionProvider = 
            RollerConfig.getProperty("hibernate.connectionProvider");        
        String configuration = "hibernate.cfg.xml";
        init(dialect, connectionProvider, configuration);
    }   
}
