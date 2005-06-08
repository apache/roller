/*
 * Created on Jul 16, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.presentation.weblog.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.weblog.search.FieldConstants;
import org.roller.presentation.weblog.search.IndexUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @author aim4min
 *
 * An index operation that rebuilds a given users index (or all indexes.)
 */
public class RebuildUserIndexOperation extends WriteToIndexOperation
{
    //~ Static fields/initializers =============================================

    private static Log mLogger =
        LogFactory.getFactory().getInstance(RebuildUserIndexOperation.class);

    //~ Instance fields ========================================================

    private WebsiteData website;

    //~ Constructors ===========================================================

    /**
     * Create a new operation that will recreate an index.
     *
     * @param website The website to rebuild the index for, or null for all users.
     */
    public RebuildUserIndexOperation(WebsiteData website)
    {
        super();
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
                String userName = null;
                if (website != null && website.getUser() != null)
                {
                    userName = website.getUser().getUserName();
                }
                Term tUsername =
                    IndexUtil.getTerm(FieldConstants.USERNAME, userName);

                if (tUsername != null)
                {
                    reader.delete(tUsername);
                }
                else
                {
                    Term all =
                        IndexUtil.getTerm(FieldConstants.CONSTANT,
                                          FieldConstants.CONSTANT_V);
                    reader.delete(all);
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

        IndexWriter writer = beginWriting();

        Roller roller = RollerFactory.getRoller();
        try
        {
            roller.begin();
            if (writer != null)
            {
                WeblogManager weblogManager = roller.getWeblogManager();

                //List entries =
                    //weblogManager.getWeblogEntriesInDateRange(
                    // getUsername(), null, null, null, true);

                List entries = weblogManager .getWeblogEntries(
                    website,                 // userName
                    null,                   // startDate
                    null,                   // endDate
                    null,                   // catName
                    WeblogManager.PUB_ONLY, // status
                    new Integer(100));     // maxEntries TODO: fix this!

                for (Iterator wbItr = entries.iterator(); wbItr.hasNext();) {
                    WeblogEntryData entry = (WeblogEntryData) wbItr.next();
                    writer.addDocument(getDocument(entry));
                }
                // release the database connection
                roller.release();
            }
        }
        catch (IOException e)
        {
            mLogger.info("Error adding doc to index", e);
        }
        catch (RollerException e)
        {
            mLogger.info("Data Access error", e);
        }
        finally
        {
            endWriting();
            if (roller != null) roller.release();
        }

        Date end = new Date();
        double length = (end.getTime() - start.getTime()) / (double) 1000;

        if (website == null)
        {
            mLogger.info("Completed rebuilding index for all users in '" +
                         length + "' secs");
        }
        else
        {
            mLogger.info("Completed rebuilding index for '" +
                            website.getUser().getUserName() + "' in '" + length + "' seconds");
        }
    }
}
