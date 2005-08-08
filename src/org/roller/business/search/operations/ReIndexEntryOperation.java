/*
 * Created on Jul 16, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.business.search.operations;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.roller.business.IndexManagerImpl;
import org.roller.business.search.FieldConstants;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.WeblogEntryData;

/**
 * @author aim4min
 *
 * An operation that adds a new log entry into the index.
 */
public class ReIndexEntryOperation extends WriteToIndexOperation
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
    public ReIndexEntryOperation(IndexManagerImpl mgr, WeblogEntryData data)
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
