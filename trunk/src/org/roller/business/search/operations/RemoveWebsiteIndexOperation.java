/*
 * Created on Jul 16, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.business.search.operations;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.roller.business.IndexManagerImpl;
import org.roller.business.search.FieldConstants;
import org.roller.business.search.IndexUtil;
import org.roller.pojos.WebsiteData;


/**
 * @author aim4min
 *
 * An index operation that rebuilds a given users index (or all indexes.)
 */
public class RemoveWebsiteIndexOperation extends WriteToIndexOperation
{
    //~ Static fields/initializers =============================================

    private static Log mLogger =
        LogFactory.getFactory().getInstance(RemoveWebsiteIndexOperation.class);

    //~ Instance fields ========================================================

    private WebsiteData website;

    //~ Constructors ===========================================================

    /**
     * Create a new operation that will recreate an index.
     *
     * @param website The website to rebuild the index for, or null for all sites.
     */
    public RemoveWebsiteIndexOperation(IndexManagerImpl mgr, WebsiteData website)
    {
        super(mgr);
        this.website = website;
    }

    //~ Methods ================================================================

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void doRun()
    {
        Date start = new Date();
        IndexReader reader = beginDeleting();
        try
        {
            if (reader != null)
            {
                String handle = null;
                if (website != null)
                {
                    handle = website.getHandle();
                }
                Term tHandle =
                    IndexUtil.getTerm(FieldConstants.WEBSITE_HANDLE, handle);

                if (tHandle != null)
                {
                    reader.delete(tHandle);
                }
            }
        }
        catch (IOException e)
        {
            mLogger.info("Problems deleting doc from index", e);
        }
        finally
        {
            endDeleting();
        }

        Date end = new Date();
        double length = (end.getTime() - start.getTime()) / (double) 1000;

        if (website != null)
        {
            mLogger.info("Completed deleting indices for website '" +
                    website.getName() + "' in '" + length + "' seconds");
        }
    }
}
