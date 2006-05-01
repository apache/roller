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
/*
 * Created on Dec 13, 2005
 */
package org.apache.roller.business.jdo;

import java.sql.Connection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.PersistenceStrategy;
import org.apache.roller.business.utils.UpgradeDatabase;
import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.PingQueueManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.UserData;

/**
 * Implements Roller, the entry point interface for the Roller business tier
 * APIs. JDO specific implementation.
 * 
 * @author Dave Johnson
 */
public class JDORollerImpl extends org.apache.roller.business.RollerImpl {
    private static Log mLogger = LogFactory.getFactory()
            .getInstance(JDORollerImpl.class);

    protected static JDORollerImpl  me;
    protected PersistenceStrategy   mStrategy      = null;

    protected JDORollerImpl() throws RollerException {
        PersistenceManagerFactory pmf = 
                JDOHelper.getPersistenceManagerFactory("JDOPMF.properties");
        mStrategy = new JDOStrategy(pmf);
    }

    public static Roller instantiate() throws RollerException {
        if (me == null) {
            me = new JDORollerImpl();
        }

        return me;
    }


    /** */
    protected UserManager createUserManager() {
        return new JDOUserManagerImpl(mStrategy);
    }

    /** */
    protected BookmarkManager createBookmarkManager() {
        return new JDOBookmarkManagerImpl(mStrategy);
    }

    /** */
    protected WeblogManager createWeblogManager() {
        return new JDOWeblogManagerImpl(mStrategy);
    }

    /** */
    protected RefererManager createRefererManager() {
        return new JDORefererManagerImpl();
    }

    /** */
    protected PropertiesManager createPropertiesManager() {
        return new JDOPropertiesManagerImpl(mStrategy);
    }

    /** */
    protected PingQueueManager createPingQueueManager() {
        return new JDOPingQueueManagerImpl(mStrategy);
    }

    /** */
    protected PlanetManager createPlanetManager() {
        return new JDOPlanetManagerImpl(mStrategy, this);
    }

    /** */
    protected  AutoPingManager createAutoPingManager() {
        return new JDOAutoPingManagerImpl(mStrategy);
    }

    /** */
    protected PingTargetManager createPingTargetManager() {
        return new JDOPingTargetManagerImpl(mStrategy);
    }

    /**
     * @see org.apache.roller.model.Roller#getPersistenceStrategy()
     */
    public PersistenceStrategy getPersistenceStrategy() {
        return mStrategy;
    }

    public void release() {
        super.release();
        // nothing else to do for now
    }

    public void shutdown() {
        super.shutdown();

        try {
            release();
        }
        catch (Exception e) {
            mLogger.error("Unable to close PersistenceManagerFactory", e);
        }
    }
}