/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
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
