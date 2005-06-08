/*
 * Created on Mar 4, 2004
 */
package org.roller.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.persistence.MockPersistenceStrategy;

import java.sql.Connection;

/**
 * @author lance.lavandowska
 */
public class MockRoller implements Roller
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MockRoller.class);    
    private MockPersistenceStrategy mStrategy = null;
    private UserManager mUserManager = null;
    private BookmarkManager mBookmarkManager = null;
    private WeblogManager mWeblogManager = null;
    private RefererManager mRefererManager = null;
    private ConfigManager mConfigManager = null;

    public static Roller instantiate(String dummy) throws RollerException    {        return new MockRoller();    }      public MockRoller()    {        mStrategy = new MockPersistenceStrategy();    }        public UserManager getUserManager() throws RollerException    {        if (mUserManager == null)        {            mUserManager = new MockUserManager(mStrategy, this);        }        return mUserManager;    }    //-----------------------------------------------------------------------    public BookmarkManager getBookmarkManager() throws RollerException    {        if (mBookmarkManager == null)        {            mBookmarkManager = new MockBookmarkManager(mStrategy, this);        }        return mBookmarkManager;    }    //-----------------------------------------------------------------------    public WeblogManager getWeblogManager() throws RollerException    {        if (mWeblogManager == null)        {            mWeblogManager = new MockWeblogManager(mStrategy, this);        }        return mWeblogManager;    }    //-----------------------------------------------------------------------    public RefererManager getRefererManager() throws RollerException    {        if (mRefererManager == null)        {            mRefererManager = new MockRefererManager(mStrategy, getUserManager());        }        return mRefererManager;    }    //-----------------------------------------------------------------------    /**     * @see org.roller.model.Roller#getConfigManager()     */    public ConfigManager getConfigManager() throws RollerException {        if (mConfigManager == null)        {            mConfigManager = new MockConfigManager(mStrategy, this);        }        return mConfigManager;    }    /*      * @see org.roller.model.Roller#begin()     */    public void begin()    {        // no-op    }    /*      * @see org.roller.model.Roller#commit()     */    public void commit() throws RollerException    {        // no-op    }    /*      * @see org.roller.model.Roller#rollback()     */    public void rollback()    {        // no-op    }    /*      * @see org.roller.model.Roller#release()     */    public void release()    {        // no-op    }
    
    /*      * @see org.roller.model.Roller#getPersistenceStrategy()     */    public PersistenceStrategy getPersistenceStrategy()    {        return mStrategy;    }
    /*      * @see org.roller.model.Roller#shutdown()     */    public void shutdown()    {        // TODO Auto-generated method stub    }

    /** 
     * @see org.roller.model.Roller#upgradeDatabase(java.sql.Connection)
     */
    public void upgradeDatabase(Connection con) throws RollerException
    {
        // no-op
    }}
