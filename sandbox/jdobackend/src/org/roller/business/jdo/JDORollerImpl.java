/*
 * Created on Dec 13, 2005
 */
package org.roller.business.jdo;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.utils.UpgradeDatabase;
import org.roller.model.AutoPingManager;
import org.roller.model.BookmarkManager;
import org.roller.model.ConfigManager;
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
    private static Log              mLogger        = LogFactory
                                                           .getFactory()
                                                           .getInstance(
                                                                   JDORollerImpl.class);

    protected BookmarkManager       mBookmarkManager;
    protected ConfigManager         mConfigManager = null;
    protected PropertiesManager     mPropsManager  = null;
    protected PlanetManager         planetManager  = null;
    protected RefererManager        mRefererManager;
    protected UserManager           mUserManager;
    protected WeblogManager         mWeblogManager;
    protected PingQueueManager      mPingQueueManager;
    protected AutoPingManager       mAutoPingManager;
    protected PingTargetManager     mPingTargetManager;
    protected static JDORollerImpl  me;
    protected PersistenceStrategy   mStrategy      = null;
    protected static SessionFactory mSessionFactory;

    protected JDORollerImpl() throws RollerException {
        mStrategy = new JDOStrategy();
    }

    public static Roller instantiate() throws RollerException {
        if (me == null) {
            me = new JDORollerImpl();
        }

        return me;
    }

    public void begin() throws RollerException {
        mStrategy.begin(UserData.ANONYMOUS_USER);
    }

    public void begin(UserData user) throws RollerException {
        mStrategy.begin(user);
    }

    public UserData getUser() throws RollerException {
        return mStrategy.getUser();
    }

    public void setUser(UserData user) throws RollerException {
        mStrategy.setUser(user);
    }

    public void commit() throws RollerException {
        mStrategy.commit();
    }

    public void rollback() {
        try {
            mStrategy.rollback();
        }
        catch (Throwable e) {
            mLogger.error(e);
        }
    }

    /**
     * @see org.roller.model.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException {
        if (mUserManager == null) {
            mUserManager = new JDOUserManagerImpl(mStrategy);
        }
        return mUserManager;
    }

    /**
     * @see org.roller.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException {
        if (mBookmarkManager == null) {
            mBookmarkManager = new JDOBookmarkManagerImpl(mStrategy);
        }
        return mBookmarkManager;
    }

    /**
     * @see org.roller.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException {
        if (mWeblogManager == null) {
            mWeblogManager = new JDOWeblogManagerImpl(mStrategy);
        }
        return mWeblogManager;
    }

    /**
     * @see org.roller.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException {
        if (mRefererManager == null) {
            mRefererManager = new JDORefererManagerImpl();
        }
        return mRefererManager;
    }

    /**
     * @see org.roller.model.Roller#getConfigManager()
     */
    public ConfigManager getConfigManager() throws RollerException {
        if (mConfigManager == null) {
            mConfigManager = new JDOConfigManagerImpl(mStrategy);
        }
        return mConfigManager;
    }

    /**
     * @see org.roller.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() throws RollerException {
        if (mPropsManager == null) {
            mPropsManager = new JDOPropertiesManagerImpl(mStrategy);
        }
        return mPropsManager;
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() throws RollerException {
        if (mPingQueueManager == null) {
            mPingQueueManager = new JDOPingQueueManagerImpl(mStrategy);
        }
        return mPingQueueManager;
    }

    /**
     * @see org.roller.model.Roller#getPlanetManager()
     */
    public PlanetManager getPlanetManager() throws RollerException {
        if (planetManager == null) {
            planetManager = new JDOPlanetManagerImpl(mStrategy, this);
        }
        return planetManager;
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() throws RollerException {
        if (mAutoPingManager == null) {
            mAutoPingManager = new JDOAutoPingManagerImpl(mStrategy);
        }
        return mAutoPingManager;
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() throws RollerException {
        if (mPingTargetManager == null) {
            mPingTargetManager = new JDOPingTargetManagerImpl(mStrategy);
        }
        return mPingTargetManager;
    }

    /**
     * @see org.roller.model.Roller#getPersistenceStrategy()
     */
    public PersistenceStrategy getPersistenceStrategy() {
        return mStrategy;
    }

    /**
     * @see org.roller.model.Roller#upgradeDatabase(java.sql.Connection)
     */
    public void upgradeDatabase(Connection con) throws RollerException {
        UpgradeDatabase.upgradeDatabase(con);
    }

    public void release() {
        super.release();

        if (mBookmarkManager != null)
            mBookmarkManager.release();
        if (mConfigManager != null)
            mConfigManager.release();
        if (mRefererManager != null)
            mRefererManager.release();
        if (mUserManager != null)
            mUserManager.release();
        if (mWeblogManager != null)
            mWeblogManager.release();
        if (mPingTargetManager != null)
            mPingTargetManager.release();
        if (mPingQueueManager != null)
            mPingQueueManager.release();
        if (mAutoPingManager != null)
            mAutoPingManager.release();

        try {
            if (mStrategy != null)
                mStrategy.release();
        }
        catch (Throwable e) {
            mLogger.error("Exception with mSupport.release() [" + e + "]", e);
        }
    }

    public void shutdown() {
        super.shutdown();

        try {
            release();
        }
        catch (Exception e) {
            mLogger.error("Unable to close SessionFactory", e);
        }
    }
}