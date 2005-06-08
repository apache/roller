/*
 * Created on Aug 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.roller.presentation.weblog.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author aim4min
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class ReadFromIndexOperation extends IndexOperation {


	private static Log mLogger =
		LogFactory.getFactory().getInstance(ReadFromIndexOperation.class);
		  
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		try {
 			manager.getReadWriteLock().readLock().acquire();
 			doRun();
		} catch (InterruptedException e) {
			mLogger.info("Error acquiring read lock on index", e);
		} finally {
			manager.getReadWriteLock().readLock().release();
		}
	}

}
