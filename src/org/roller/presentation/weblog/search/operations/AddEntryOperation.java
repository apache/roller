/*
 * Created on Jul 16, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.presentation.weblog.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.roller.pojos.WeblogEntryData;

import java.io.IOException;


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
    public AddEntryOperation(WeblogEntryData data)
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
        IndexWriter writer = beginWriting();

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
            endWriting();
        }    	
    }
    
}
