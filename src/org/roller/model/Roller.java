
package org.roller.model;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;

import java.sql.Connection;

/** The main entry point interface of the Roller business tier.
 * @author David M Johnson
 */
public interface Roller
{
	/** Get UserManager associated with this Roller instance.
     * @return UserManager associated with this Roller instance.
     * @throws RollerException If unable to create or return UserManager.
     */
	public UserManager getUserManager() throws RollerException;

	/** Get BookmarkManager associated with this Roller instance.
     * @return BookmarkManager associated with this Roller instance.
     * @throws RollerException If unable to create or return BookmarkManager.
     */
	public BookmarkManager getBookmarkManager() throws RollerException;

	/** Get WeblogManager associated with this Roller instance.
     * @return WeblogManager associated with this Roller instance.
     * @throws RollerException If unable to create or return WeblogManager.
     */
	public WeblogManager getWeblogManager() throws RollerException;
    
	/** Get RefererManager associated with this Roller instance.
	 * @return RefererManager associated with this Roller instance.
	 * @throws RollerException If unable to create or return RefererManager.
	 */	
	public RefererManager getRefererManager() throws RollerException;
    
    /** Get RefererManager associated with this Roller instance.
     * @return RefererManager associated with this Roller instance.
     * @throws RollerException If unable to create or return RefererManager.
     */ 
    public ConfigManager getConfigManager() throws RollerException;
    	
    /** Begin transaction for a thread.
     */ 
    public void begin() throws RollerException;
    
    /** Commit transaction for a thread.
     * @throws RollerException If database error is thrown.
     */ 
    public void commit() throws RollerException;
    
    /** Rollback uncommitted changes for a thread.
     */ 
    public void rollback();
    
    /** Rollback and release associated resources for a thread.
     */ 
    public void release();

    /** Get persistence strategy used by managers and POJOs. */
    public PersistenceStrategy getPersistenceStrategy();
    
    /** Repair websites if needed. */
    public void upgradeDatabase(Connection con) throws RollerException;
    
    /**
     * Release all resources necessary for this instance
     * of Roller.
     */
    public void shutdown();
    
}

