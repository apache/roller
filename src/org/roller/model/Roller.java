
package org.roller.model;

import java.io.Serializable;
import java.sql.Connection;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.referrers.ReferrerQueueManager;
import org.roller.pojos.UserData;


/** 
 * The main entry point interface of the Roller business tier.
 *
 * @author David M Johnson
 */
public interface Roller extends Serializable {
    
    /** 
     * Get UserManager associated with this Roller instance.
     *
     * @return UserManager associated with this Roller instance.
     * @throws RollerException If unable to create or return UserManager.
     */
    public UserManager getUserManager() throws RollerException;
    
    /** 
     * Get BookmarkManager associated with this Roller instance.
     *
     * @return BookmarkManager associated with this Roller instance.
     * @throws RollerException If unable to create or return BookmarkManager.
     */
    public BookmarkManager getBookmarkManager() throws RollerException;
    
    /** 
     * Get WeblogManager associated with this Roller instance.
     *
     * @return WeblogManager associated with this Roller instance.
     * @throws RollerException If unable to create or return WeblogManager.
     */
    public WeblogManager getWeblogManager() throws RollerException;
    
    /** 
     * Get RefererManager associated with this Roller instance.
     *
     * @return RefererManager associated with this Roller instance.
     * @throws RollerException If unable to create or return RefererManager.
     */
    public RefererManager getRefererManager() throws RollerException;
    
    /**
     * Get ReferrerQueueManager.
     */
    public ReferrerQueueManager getReferrerQueueManager();
    
    /** 
     * Get RefererManager associated with this Roller instance.
     *
     * @return RefererManager associated with this Roller instance.
     * @throws RollerException If unable to create or return RefererManager.
     */
    public ConfigManager getConfigManager() throws RollerException;
    
    /**
     * Get the AutoPingManager associated with this Roller instance.
     *
     * @return the AutoPingManager associated with this Roller instance.
     * @throws RollerException
     */
    public AutoPingManager getAutopingManager() throws RollerException;
    
    /**
     * Get the PingTargetManager associated with this Roller instance.
     *
     * @return the PingTargetManager associated with this Roller instance.
     * @throws RollerException
     */
    public PingTargetManager getPingTargetManager() throws RollerException;
    
    /**
     * Get the PingQueueManager associated with this Roller instance.
     *
     * @return the PingQueueManager associated with this Roller instance.
     * @throws RollerException
     */
    public PingQueueManager getPingQueueManager() throws RollerException;
    
    /** Get PropertiesManager associated with this Roller instance.
     * @return PropertiesManager associated with this Roller instance.
     * @throws RollerException If unable to create or return PropertiesManager.
     */
    public PropertiesManager getPropertiesManager() throws RollerException;
    
    /** 
     * Get FileManager associated with this Roller instance.
     *
     * @return FileManager associated with this Roller instance.
     * @throws RollerException If unable to create or return FileManager.
     */
    public FileManager getFileManager() throws RollerException;
    
    /**
     * Get ThreadManager associated with this Roller instance.
     */
    public ThreadManager getThreadManager() throws RollerException;
    
    /**
     * Get IndexManager associated with this Roller instance.
     */
    public IndexManager getIndexManager() throws RollerException;
    
    /**
     * Get PlanetManager associated with this Roller instance.
     */
    public PlanetManager getPlanetManager() throws RollerException;
    
    /**
     * Get ThemeManager associated with this Roller instance.
     */
    public ThemeManager getThemeManager() throws RollerException;
    
    /**
     * Get PagePluginManager associated with this Roller instance.
     */
    public PagePluginManager getPagePluginManager() throws RollerException;
    
    /** 
     * Begin transaction for a thread.
     */
    public void begin() throws RollerException;
    
    /**
     * Start Roller session on behalf of specified user.
     */
    public void begin(UserData user) throws RollerException;
    
    /**
     * Set user for Roller session.
     */
    public void setUser(UserData user) throws RollerException;
    
    /**
     * Get user associated with Roller session.
     */
    public UserData getUser() throws RollerException;
    
    /** 
     * Commit transaction for a thread.
     *
     * @throws RollerException If database error is thrown.
     */
    public void commit() throws RollerException;
    
    /** 
     * Rollback uncommitted changes for a thread.
     */
    public void rollback();
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
    /** 
     * Get persistence strategy used by managers and POJOs.
     */
    public PersistenceStrategy getPersistenceStrategy();
    
    /**
     * Upgrade database if needed.
     */
    public void upgradeDatabase(Connection con) throws RollerException;
    
    /**
     * Release all resources necessary for this instance of Roller.
     */
    public void shutdown();
}

