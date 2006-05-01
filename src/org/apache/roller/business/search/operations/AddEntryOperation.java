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
 * Created on Jul 16, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.apache.roller.business.search.operations;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.roller.business.IndexManagerImpl;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogEntryData;


/**
 * @author aim4min
 *
 * An operation that adds a new log entry into the index.
 */
public class AddEntryOperation extends WriteToIndexOperation
{
    //~ Static fields/initializers =============================================

    private static Log mLogger =
        LogFactory.getFactory().getInstance(AddEntryOperation.class);

    //~ Instance fields ========================================================

    private WeblogEntryData data;

    //~ Constructors ===========================================================

    /**
     * Adds a web log entry into the index.
     */
    public AddEntryOperation(IndexManagerImpl mgr, WeblogEntryData data)
    {
        super(mgr);
        this.data = data;
    }

    //~ Methods ================================================================

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void doRun()
    {    	
        IndexWriter writer = beginWriting();
        Roller roller = RollerFactory.getRoller();
        try
        {
            if (writer != null)
            {
                writer.addDocument(getDocument(data));
            }
        }
        catch (IOException e)
        {
            mLogger.error("Problems adding doc to index", e);
        }
        finally
        {
            if (roller != null) roller.release();
            endWriting();
        }    	
    }
    
}
