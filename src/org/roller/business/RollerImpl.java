/*
 * RollerImpl.java
 *
 * Created on April 29, 2005, 5:33 PM
 */
package org.roller.business;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.roller.RollerException;
import org.roller.business.referrers.ReferrerQueueManager;
import org.roller.business.referrers.ReferrerQueueManagerImpl;

import org.roller.business.utils.UpgradeDatabase;

import org.roller.model.AutoPingManager;
import org.roller.model.BookmarkManager;
import org.roller.model.ConfigManager;
import org.roller.model.FileManager;
import org.roller.model.IndexManager;
import org.roller.model.PagePluginManager;
import org.roller.model.PingQueueManager;
import org.roller.model.PingTargetManager;
import org.roller.model.PlanetManager;
import org.roller.model.PropertiesManager;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.ThemeManager;
import org.roller.model.ThreadManager;
import org.roller.pojos.UserData;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;

/**
 * The abstract version of the Roller implementation.
 * Here we put code that pertains to *all* implementations of the Roller
 * interface, regardless of their persistence strategy.
 *
 * @author Allen Gilliland
 */
public abstract class RollerImpl implements Roller {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RollerImpl.class);
    
    protected FileManager   mFileManager = null;
    protected IndexManager  mIndexManager = null;
    protected ThreadManager mThreadManager = null;
    protected ThemeManager  mThemeManager = null;
    protected PagePluginManager mPluginManager = null;
    protected BookmarkManager mBookmarkManager;   
    protected PropertiesManager mPropsManager = null;
    protected PlanetManager   mPlanetManager = null;
    protected RefererManager  mRefererManager;
    protected UserManager     mUserManager;
    protected WeblogManager   mWeblogManager;
    protected PingQueueManager mPingQueueManager;
    protected AutoPingManager mAutoPingManager;
    protected PingTargetManager mPingTargetManager;
    protected PersistenceStrategy mStrategy = null;
    
    /** Creates a new instance of RollerImpl */
    public RollerImpl() {
        // nothing to do here yet
    }
    
    
    /**
     * @see org.roller.model.Roller#getFileManager()
     */
    public FileManager getFileManager() throws RollerException {
        if (mFileManager == null) {
            mFileManager = new FileManagerImpl();
        }
        return mFileManager;
    }
    
    /**
     * @see org.roller.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() throws RollerException {
        if (mThreadManager == null) {
            mThreadManager = new ThreadManagerImpl();
        }
        return mThreadManager;
    }
    
    /**
     * @see org.roller.model.Roller#getIndexManager()
     */
    public IndexManager getIndexManager() throws RollerException {
        if (mIndexManager == null) {
            mIndexManager = new IndexManagerImpl();
        }
        return mIndexManager;
    }
    
    /**
     * @see org.roller.model.Roller#getThemeManager()
     */
    public ThemeManager getThemeManager() throws RollerException {
        if (mThemeManager == null) {
            mThemeManager = new ThemeManagerImpl();
        }
        return mThemeManager;
    }
    
    public ReferrerQueueManager getReferrerQueueManager() {
        return ReferrerQueueManagerImpl.getInstance();
    }
    
    /**
     * @see org.roller.model.Roller#getPluginManager()
     */
    public PagePluginManager getPagePluginManager() throws RollerException {
        if (mPluginManager == null) {
            mPluginManager = new PagePluginManagerImpl();
        }
        return mPluginManager;
    }
    
    
    public void release() {
        if (mFileManager != null) mFileManager.release();
        if (mThreadManager != null) mThreadManager.release();
        if (mPluginManager != null) mPluginManager.release();
        if (mBookmarkManager != null) mBookmarkManager.release();
        if (mRefererManager != null) mRefererManager.release();
        if (mUserManager != null) mUserManager.release();
        if (mWeblogManager != null) mWeblogManager.release();
        if (mPingTargetManager != null) mPingTargetManager.release();
        if (mPingQueueManager != null) mPingQueueManager.release();
        if (mAutoPingManager != null) mAutoPingManager.release();
        
        try
        {
            if (mStrategy != null) mStrategy.release(); 
        }
        catch (Throwable e)
        {
            mLogger.error(
            "Exception with mSupport.release() [" + e + "]", e);
        }
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
        } catch (Throwable e) {
            mLogger.error(e);
        }
    }

    /** */
    protected UserManager createUserManager() {
        throw new NullPointerException();
    }

    /** 
     * @see org.roller.model.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException {
        if (mUserManager == null) {
            mUserManager = createUserManager();
        }
        return mUserManager;
    }

    /** */
    protected BookmarkManager createBookmarkManager() {
        throw new NullPointerException();
    }

    /** 
     * @see org.roller.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException {
        if ( mBookmarkManager == null )  {
            mBookmarkManager = createBookmarkManager();
        }
        return mBookmarkManager;
    }

    /** */
    protected WeblogManager createWeblogManager() {
        throw new NullPointerException();
    }

    /** 
     * @see org.roller.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException {
        if ( mWeblogManager == null )  {
            mWeblogManager = createWeblogManager();
        }
        return mWeblogManager;
    }

    /** */
    protected RefererManager createRefererManager() {
        throw new NullPointerException();
    }

    /** 
     * @see org.roller.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException {
        if (mRefererManager == null) {
            mRefererManager = createRefererManager();
        }
        return mRefererManager;
    }

    /**
     * @see org.roller.model.Roller#getConfigManager()
     */
    public ConfigManager getConfigManager() throws RollerException 
    {
        throw new RollerException("getConfigManager is Deprecated.");
    }
    
    /** */
    protected PropertiesManager createPropertiesManager() {
        throw new NullPointerException();
    }

    /**
     * @see org.roller.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() throws RollerException {
        if (mPropsManager == null) {
            mPropsManager = createPropertiesManager();
        }
        return mPropsManager;
    }

    /** */
    protected PingQueueManager createPingQueueManager() {
        throw new NullPointerException();
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() throws RollerException {
        if (mPingQueueManager == null) {
            mPingQueueManager = createPingQueueManager();
        }
        return mPingQueueManager;
    }

    /** */
    protected PlanetManager createPlanetManager() {
        throw new NullPointerException();
    }

    /**
     * @see org.roller.model.Roller#getPlanetManager()
     */
    public PlanetManager getPlanetManager() throws RollerException {
        if ( mPlanetManager == null ) {
            mPlanetManager = createPlanetManager();
        }
        return mPlanetManager;
    }

    /** */
    protected  AutoPingManager createAutoPingManager() {
        throw new NullPointerException();
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() throws RollerException {
        if (mAutoPingManager == null) {
            mAutoPingManager = createAutoPingManager();
        }
        return mAutoPingManager;
    }

    /** */
    protected PingTargetManager createPingTargetManager() {
        throw new NullPointerException();
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() throws RollerException {
        if (mPingTargetManager == null) {
            mPingTargetManager = createPingTargetManager();
        }
        return mPingTargetManager;
    }

    /**
     * @see org.roller.model.Roller#upgradeDatabase(java.sql.Connection)
     */
    public void upgradeDatabase(Connection con) throws RollerException {
        UpgradeDatabase.upgradeDatabase(con);
    }

     public void shutdown() {
        try {
            if(getReferrerQueueManager() != null) getReferrerQueueManager().shutdown();
            if (mIndexManager != null) mIndexManager.shutdown();
            if (mThreadManager != null) mThreadManager.shutdown();
        } catch(Exception e) {
            mLogger.warn(e);
        }
    }
}
