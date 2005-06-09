/*
 * Created on Feb 23, 2003
 */
package org.roller.business.hibernate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.utils.UpgradeDatabase;
import org.roller.model.BookmarkManager;
import org.roller.model.ConfigManager;
import org.roller.model.AutoPingManager;
import org.roller.model.PingQueueManager;
import org.roller.model.PingTargetManager;
import org.roller.model.PlanetManager;
import org.roller.model.PropertiesManager;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;

import java.sql.Connection;

/**
 * Implements Roller, the entry point interface for the Roller business tier APIs.
 * Hibernate specific implementation.
 *
 * @author David M Johnson
 */
public class HibernateRollerImpl extends org.roller.business.RollerImpl
{
    static final long serialVersionUID = 5256135928578074652L;

    private static Log mLogger = 
        LogFactory.getFactory().getInstance(HibernateRollerImpl.class);
    
    protected BookmarkManager mBookmarkManager;   
    protected ConfigManager   mConfigManager = null;
    protected PropertiesManager mPropsManager = null;
    protected PlanetManager   planetManager = null;
    protected RefererManager  mRefererManager;
    protected UserManager     mUserManager;
    protected WeblogManager   mWeblogManager;
    protected PingQueueManager mPingQueueManager;
    protected AutoPingManager mAutoPingManager;
    protected PingTargetManager mPingTargetManager;
    protected static HibernateRollerImpl me;
    protected PersistenceStrategy mStrategy = null;
    protected static SessionFactory mSessionFactory;


    protected HibernateRollerImpl() throws RollerException
    {
        mLogger.debug("Initializing sessionFactory for Hibernate");
        
        try 
        {
            Configuration config = new Configuration();
            config.configure("/hibernate.cfg.xml");
            mSessionFactory = config.buildSessionFactory();
        } 
        catch (HibernateException e) 
        {
            mLogger.error("Error Setting up SessionFactory",e);
            throw new RollerException(e);
        }
        
        mStrategy = new HibernateStrategy(mSessionFactory);    
    }

    
    /**
     * Instantiates and returns an instance of HibernateRollerImpl.
     * @see org.roller.model.RollerFactory
     */
    public static Roller instantiate() throws RollerException
    {
        if (me == null) 
        {
            mLogger.debug("Instantiating HibernateRollerImpl");
            me = new HibernateRollerImpl();
        }
        
        return me;
    }
    

    public void begin() throws RollerException
    {
        mStrategy.begin(UserData.ANONYMOUS_USER);
    }
    
    public void begin(UserData user) throws RollerException
    {
        mStrategy.begin(user);
    }
    
    public UserData getUser() throws RollerException
    {
        return mStrategy.getUser();
    }
    
    public void setUser(UserData user) throws RollerException
    {
        mStrategy.setUser(user);
    }
    
    public void commit() throws RollerException
    {
        mStrategy.commit();
    }
    
	public void rollback()
	{
		try
		{
			mStrategy.rollback();
		}
		catch (Exception e)
		{
			mLogger.error(e);
		}
	}

    /** 
     * @see org.roller.model.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException
    {
        if ( mUserManager == null ) 
        {
            mUserManager = new HibernateUserManagerImpl(mStrategy);
        }
        return mUserManager;
    }

    /** 
     * @see org.roller.model.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException
    {
        if ( mBookmarkManager == null ) 
        {
            mBookmarkManager = new HibernateBookmarkManagerImpl(mStrategy);
        }
        return mBookmarkManager;
    }

    /** 
     * @see org.roller.model.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException
    {
        if ( mWeblogManager == null ) 
        {
            mWeblogManager = new HibernateWeblogManagerImpl(mStrategy);
        }
        return mWeblogManager;
    }

    /** 
     * @see org.roller.model.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException
    {
        if ( mRefererManager == null ) 
        {
            mRefererManager = new HibernateRefererManagerImpl(mStrategy);
        }
        return mRefererManager;
    }

    /**
     * @see org.roller.model.Roller#getConfigManager()
     */
    public ConfigManager getConfigManager() throws RollerException 
    {
        if (mConfigManager == null)
        {
            mConfigManager = new HibernateConfigManagerImpl(mStrategy);
        }
        return mConfigManager;
    }
    
    /**
     * @see org.roller.model.Roller#getPropertiesManager()
     */
    public PropertiesManager getPropertiesManager() throws RollerException 
    {
        if (mPropsManager == null)
        {
            mPropsManager = new HibernatePropertiesManagerImpl(mStrategy);
        }
        return mPropsManager;
    }

    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingQueueManager getPingQueueManager() throws RollerException
    {
        if (mPingQueueManager == null)
        {
            mPingQueueManager = new HibernatePingQueueManagerImpl(mStrategy);
        }
        return mPingQueueManager;
    }

    /**
     * @see org.roller.model.Roller#getPlanetManager()
     */
    public PlanetManager getPlanetManager() throws RollerException
    {
        if ( planetManager == null ) 
        {
            planetManager = new HibernatePlanetManagerImpl(mStrategy,this);
        }
        return planetManager;
    }


    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public AutoPingManager getAutopingManager() throws RollerException
    {
        if (mAutoPingManager == null)
        {
            mAutoPingManager = new HibernateAutoPingManagerImpl(mStrategy);
        }
        return mAutoPingManager;
    }


    /**
     * @see org.roller.model.Roller#getPingTargetManager()
     */
    public PingTargetManager getPingTargetManager() throws RollerException
    {
        if (mPingTargetManager == null)
        {
            mPingTargetManager = new HibernatePingTargetManagerImpl(mStrategy);
        }
        return mPingTargetManager;
    }


    /**
     * @see org.roller.model.Roller#getPersistenceStrategy()
     */
    public PersistenceStrategy getPersistenceStrategy()
    {
        return mStrategy;
    }
    
    /** 
     * @see org.roller.model.Roller#upgradeDatabase(java.sql.Connection)
     */
    public void upgradeDatabase(Connection con) throws RollerException
    {
        UpgradeDatabase.upgradeDatabase(con); 
    }

    public void release()
    {
        super.release();
        
        if (mBookmarkManager != null) mBookmarkManager.release();
        if (mConfigManager != null) mConfigManager.release();
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
        catch (Exception e)
        {
            mLogger.error(
            "Exception with mSupport.release() [" + e + "]", e);
        }
    }

    public void shutdown()
    {
        super.shutdown();
        
        try
        {
            release();
            mSessionFactory.close();
        }
        catch (HibernateException e)
        {
            mLogger.error("Unable to close SessionFactory", e);
        }
    }
}
