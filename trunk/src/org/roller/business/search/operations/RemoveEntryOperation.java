/*
 * Created on Jul 16, 2003
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 *
 */
package org.roller.business.search.operations;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.roller.business.IndexManagerImpl;
import org.roller.business.search.FieldConstants;
import org.roller.pojos.WeblogEntryData;


/**
 * @author aim4min
 *
 * An operation that removes the weblog from the index.
 */
public class RemoveEntryOperation extends WriteToIndexOperation
{
    //~ Static fields/initializers =============================================

    private static Log mLogger =
        LogFactory.getFactory().getInstance(RemoveEntryOperation.class);

    //~ Instance fields ========================================================

    private WeblogEntryData data;

    //~ Constructors ===========================================================

    /**
     *
     */
    public RemoveEntryOperation(IndexManagerImpl mgr, WeblogEntryData data)
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
        IndexReader reader = beginDeleting();
        try
        {
            if (reader != null)
            {
                Term term = new Term(FieldConstants.ID, data.getId());
                reader.delete(term);
            }
        }
        catch (IOException e)
        {
            mLogger.error("Error deleting doc from index", e);
        }
        finally
        {
            endDeleting();
        }
    }

 
}
