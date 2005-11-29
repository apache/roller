/*
 * RollerImpl.java
 *
 * Created on April 29, 2005, 5:33 PM
 */
package org.roller.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.FileManager;
import org.roller.model.IndexManager;
import org.roller.model.PagePluginManager;
import org.roller.model.Roller;
import org.roller.model.ThemeManager;
import org.roller.model.ThreadManager;

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
    }
    
    
    public void shutdown() {
        try {
            if (mIndexManager != null) mIndexManager.shutdown();
            if (mThreadManager != null) mThreadManager.shutdown();
        } catch(Exception e) {
            mLogger.warn(e);
        }
    }
}
