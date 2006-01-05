/*
 * Created on Dec 13, 2005
 */
package org.roller.business.jdo;

import java.sql.Connection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.utils.UpgradeDatabase;
import org.roller.model.AutoPingManager;
import org.roller.model.BookmarkManager;
import org.roller.model.PingQueueManager;
import org.roller.model.PingTargetManager;
import org.roller.model.PlanetManager;
import org.roller.model.PropertiesManager;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;

/**
 * Implements Roller, the entry point interface for the Roller business tier
 * APIs. JDO specific implementation.
 * 
 * @author Dave Johnson
 */
public class JDORollerImpl extends org.roller.business.RollerImpl {
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
     * @see org.roller.model.Roller#getPersistenceStrategy()
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