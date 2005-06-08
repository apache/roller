/*
 * Created on Jul 16, 2003
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 *
 */
package org.roller.presentation.weblog.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.weblog.search.FieldConstants;

import java.io.IOException;


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
    public RemoveEntryOperation(WeblogEntryData data)
    {
        super();
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
