
package org.roller.business.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.business.IndexManagerImpl;

/**
 * @author aim4min
 */
public abstract class ReadFromIndexOperation extends IndexOperation
{

    /**
     * @param manager
     */
    public ReadFromIndexOperation(IndexManagerImpl mgr)
    {
        super(mgr);
    }

    private static Log mLogger = LogFactory.getFactory().getInstance(
            ReadFromIndexOperation.class);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public final void run()
    {
        try
        {
            manager.getReadWriteLock().readLock().acquire();
            doRun();
        }
        catch (InterruptedException e)
        {
            mLogger.info("Error acquiring read lock on index", e);
        }
        finally
        {
            manager.getReadWriteLock().readLock().release();
        }
    }

}