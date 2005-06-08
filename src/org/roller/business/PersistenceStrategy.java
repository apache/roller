/*
 * Created on Aug 13, 2003
 */
package org.roller.business;

import org.roller.RollerException;
import org.roller.pojos.*;

import java.util.List;

/**
 * Persistence strategy masks underlying persistence mechanism.
 * @author Lance Lavandowska
 * @author Dave Johnson
 */
public interface PersistenceStrategy
{
    /** 
     * Save a persistent object to storage. This method is only needed when
     * a new object is to be added to storage. 
     */
    public PersistentObject store(PersistentObject data)
        throws RollerException;
       
    /** 
     * Load an persistent object from storage. Object returned is a 
     * persistent instance, meaning that changes to it will be automatically
     * saved to storage the next time that commit() is called.
     */
    public PersistentObject load(
        String id, Class cls) throws RollerException;
        
    /** 
     * Remove an object from storage.
     */
    public void remove(PersistentObject po)
        throws RollerException;
        
    /** 
     * Remove an object from storage.
     */
    public void remove(String id, Class cls)
        throws RollerException;
        
    /** 
     * Release existing resources and start new session and transaction.
     */
    public void begin() throws RollerException;

    /** 
     * Commit all changes made to persistent objects since last call to release.
     */
	public void commit() throws RollerException;

    /**
     * Rollback all changes since last call to release.
     */
	public void rollback() throws RollerException;
    
    /** 
     * Release associated resources (database connection, session, etc.).
     */
    public void release() throws RollerException;
}
