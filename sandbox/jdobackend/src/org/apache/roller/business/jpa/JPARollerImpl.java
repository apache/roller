
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
package org.apache.roller.business.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.datamapper.DatamapperRollerImpl;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.business.Roller;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;

/**
 * Implements Roller, the entry point interface for the Roller business tier
 * APIs. JDO specific implementation.
 */
public class JPARollerImpl extends DatamapperRollerImpl {
    
    /**
     * The logger associated with this class.
     */
    private static Log logger = LogFactory.getLog(JPARollerImpl.class);
    
    
    /**
     * Single constructor.
     * @throws org.apache.roller.RollerException on any error
     */
    protected JPARollerImpl() throws RollerException {
        // set strategy used by Datamapper
        strategy = new JPAPersistenceStrategy();
    }

    protected UserManager createDatamapperUserManager(DatamapperPersistenceStrategy strategy) {
        return new JPAUserManagerImpl((JPAPersistenceStrategy) strategy);
    }

    protected WeblogManager createDatamapperWeblogManager(
            DatamapperPersistenceStrategy strategy) {
        return null;
    }

    protected RefererManager createDatamapperRefererManagerImpl(DatamapperPersistenceStrategy strategy) {
        return new JPARefererManager((JPAPersistenceStrategy) strategy);
    }

    /**
     * Construct and return the singleton instance of the class.
     * @throws org.apache.roller.RollerException on any error
     * @return the singleton
     */
    public static Roller instantiate() throws RollerException {
        logger.debug("Instantiating JPARollerImpl");
        return new JPARollerImpl();
    }
}
