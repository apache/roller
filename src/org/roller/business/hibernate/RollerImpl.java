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
import org.roller.business.BookmarkManagerImpl;
import org.roller.business.PersistenceStrategy;
import org.roller.business.RefererManagerImpl;
import org.roller.business.UserManagerImpl;
import org.roller.business.WeblogManagerImpl;
import org.roller.business.utils.UpgradeDatabase;
import org.roller.model.BookmarkManager;
import org.roller.model.ConfigManager;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;

import java.io.Serializable;
import java.sql.Connection;

/**
 * @author David M Johnson
 */
public class RollerImpl implements Roller, Serializable
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerImpl.class);
    
    private BookmarkManagerImpl mBookmarkMgr;   
    private UserManagerImpl mUserMgr;
    private RefererManagerImpl mRefererMgr;
    private WeblogManagerImpl mWeblogMgr;
    private ConfigManager mConfigManager = null;
    
    private static RollerImpl me;
	private PersistenceStrategy mStrategy = null;
	private static SessionFactory mSessionFactory;
	
    protected RollerImpl() throws RollerException
    {
		mStrategy = new HibernateStrategy(mSessionFactory);    
    }

    /**
     * Instantiates and returns an instance of RollerImpl.
     * @see org.roller.pojos.Roller#instantiate(ServletContext)
     */
    public static Roller instantiate(String dummy) 
        throws RollerException
    {
        if (me == null) {
            if (mLogger.isDebugEnabled()) 
            {
                mLogger.debug("initializing sessionFactory for Hibernate");
            }
            
    		try
    		{
            Configuration config = new Configuration();
            config.addClass(org.roller.business.HitCountData.class);     
            config.addClass(org.roller.pojos.BookmarkData.class);
            config.addClass(org.roller.pojos.CommentData.class);
            config.addClass(org.roller.pojos.FolderAssoc.class);
            config.addClass(org.roller.pojos.FolderData.class);
            config.addClass(org.roller.pojos.PageData.class);
            config.addClass(org.roller.pojos.RefererData.class);
            config.addClass(org.roller.pojos.RoleData.class);
            config.addClass(org.roller.pojos.RollerConfig.class);
            config.addClass(org.roller.pojos.UserData.class);
            config.addClass(org.roller.pojos.UserCookie.class);
            config.addClass(org.roller.pojos.WeblogCategoryData.class);
            config.addClass(org.roller.pojos.WeblogCategoryAssoc.class);
            config.addClass(org.roller.pojos.WeblogEntryData.class);
            config.addClass(org.roller.pojos.WebsiteData.class);        
            mSessionFactory = config.buildSessionFactory();
    		}
    		catch (HibernateException e)
    		{
    			mLogger.error("Error Setting up SessionFactory",e);
    			throw new RollerException(e);
    		}
            me = new RollerImpl();    
        } 
        return me;
    }

	public void release()
	{
		if (mUserMgr != null)
			mUserMgr.release();
		if (mWeblogMgr != null)
			mWeblogMgr.release();
		if (mRefererMgr != null)
			mRefererMgr.release();
		try
		{
			if (mStrategy != null)
				mStrategy.release(); 
		}
		catch (Exception e)
		{
			mLogger.error(
			"Exception with mSupport.release() [" + e + "]", e);
		}
	}

    public void begin() throws RollerException
    {
    	    mStrategy.begin();
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
     * @see org.roller.pojos.Roller#getUserManager()
     */
    public UserManager getUserManager() throws RollerException
    {
        if ( mUserMgr == null ) 
        {
            mUserMgr = new HibernateUserManagerImpl(mStrategy, this);
        }
        return mUserMgr;
    }

    /** 
     * @see org.roller.pojos.Roller#getBookmarkManager()
     */
    public BookmarkManager getBookmarkManager() throws RollerException
    {
        if ( mBookmarkMgr == null ) 
        {
            mBookmarkMgr = new HibernateBookmarkManagerImpl(mStrategy, this);
        }
        return mBookmarkMgr;
    }

    /** 
     * @see org.roller.pojos.Roller#getWeblogManager()
     */
    public WeblogManager getWeblogManager() throws RollerException
    {
        if ( mWeblogMgr == null ) 
        {
            mWeblogMgr = new HibernateWeblogManagerImpl(mStrategy, this);
        }
        return mWeblogMgr;
    }

    /** 
     * @see org.roller.pojos.Roller#getRefererManager()
     */
    public RefererManager getRefererManager() throws RollerException
    {
        if ( mRefererMgr == null ) 
        {
            mRefererMgr = new HibernateRefererManagerImpl(this, mStrategy);
        }
        return mRefererMgr;
    }

    /**
     * @see org.roller.model.Roller#getConfigManager()
     */
    public ConfigManager getConfigManager() throws RollerException {
        if (mConfigManager == null)
        {
            mConfigManager = new HibernateConfigManagerImpl(mStrategy, this);
        }
        return mConfigManager;
    }

    /** 
     * @see org.roller.model.Roller#getPersistenceStrategy()
     */
    public PersistenceStrategy getPersistenceStrategy()
    {
        return mStrategy;
    }
    
    /** 
     * @see org.roller.model.Roller#repairIfNeeded()
     */
    public void upgradeDatabase(Connection con) throws RollerException
    {
        UpgradeDatabase.upgradeDatabase(con); 
    }

    public void shutdown()
    {
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
