package org.roller.model;

import org.roller.RollerException;
import org.roller.business.search.operations.IndexOperation;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;

/**
 * Interface to Roller's Lucene-based search facility.
 * @author Dave Johnson
 */
public interface IndexManager
{
    /** Does index need to be rebuild */
    public abstract boolean isInconsistentAtStartup();
    
    /** Rebuild index, returns immediately and operates in background */
    public void rebuildUserIndex() throws RollerException;
    
    /** Remove user from index, returns immediately and operates in background */
    public void removeUserIndex(UserData user) throws RollerException;
    
    /** Remove entry from index, returns immediately and operates in background */
    public void removeEntryIndexOperation(WeblogEntryData entry) throws RollerException;
    
    /** Add entry to index, returns immediately and operates in background */
    public void addEntryIndexOperation(WeblogEntryData entry) throws RollerException;
    
    /** R-index entry, returns immediately and operates in background */
    public void addEntryReIndexOperation(WeblogEntryData entry) throws RollerException;
    
    /** Execute operation immediately */
    public abstract void executeIndexOperationNow(final IndexOperation op);
    
    /** Release to be called at end of request processing */
    public abstract void release();
    
    /** Shutdown to be called on application shutdown */
    public abstract void shutdown();
}