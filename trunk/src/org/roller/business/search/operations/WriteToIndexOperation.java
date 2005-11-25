/*
 * Created on Aug 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.roller.business.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.business.IndexManagerImpl;

/**
 * @author aim4min
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class WriteToIndexOperation extends IndexOperation {

	/**
     * @param manager
     */
    public WriteToIndexOperation(IndexManagerImpl mgr)
    {
        super(mgr);
    }

    private static Log mLogger =
		LogFactory.getFactory().getInstance(WriteToIndexOperation.class);
		  
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() 
    {
		try 
        {
            manager.getReadWriteLock().writeLock().acquire();
            mLogger.info("Starting search index operation");
            doRun();
            mLogger.info("Search index operation complete");
		} 
        catch (InterruptedException e) 
        {
			mLogger.error("Error acquiring write lock on index", e);
		} 
        finally 
        {
			manager.getReadWriteLock().writeLock().release();
		}
        manager.resetSharedReader();
	}
}
