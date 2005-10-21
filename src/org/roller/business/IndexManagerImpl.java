/*
 * Created on Jul 18, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.business;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.roller.RollerException;
import org.roller.business.search.operations.AddEntryOperation;
import org.roller.business.search.operations.IndexOperation;
import org.roller.business.search.operations.ReIndexEntryOperation;
import org.roller.business.search.operations.RebuildWebsiteIndexOperation;
import org.roller.business.search.operations.RemoveEntryOperation;
import org.roller.business.search.operations.RemoveWebsiteIndexOperation;
import org.roller.business.search.operations.WriteToIndexOperation;
import org.roller.model.IndexManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.roller.config.RollerConfig;
import org.roller.model.RollerFactory;
import org.roller.util.StringUtils;

/**
 * Lucene implementation of IndexManager. This is the central entry point into the Lucene
 * searching API.
 * @author aim4min
 * @author mraible (formatting and making indexDir configurable)
 */
public class IndexManagerImpl implements IndexManager
{
    //~ Static fields/initializers
    // =============================================

    private IndexReader reader;

    static Log mLogger = LogFactory.getFactory().getInstance(
            IndexManagerImpl.class);

    //~ Instance fields
    // ========================================================

    private boolean searchEnabled = true;

    File indexConsistencyMarker;

    private boolean useRAMIndex = false;

    private RAMDirectory fRAMindex;

    private String indexDir = null;

    private boolean inconsistentAtStartup = false;

    private ReadWriteLock rwl = new WriterPreferenceReadWriteLock();

    //~ Constructors
    // ===========================================================

    /**
     * Creates a new lucene index manager. This should only be created once.
     * Creating the index manager more than once will definately result in
     * errors. The prefered way of getting an index is through the
     * RollerContext.
     * 
     * @param indexDir -
     *            the path to the index directory
     */
    public IndexManagerImpl()
    {
        // check config to see if the internal search is enabled
        String enabled = RollerConfig.getProperty("search.enabled");
        if("false".equalsIgnoreCase(enabled))
            this.searchEnabled = false;
        
        // we also need to know what our index directory is
        // Note: system property expansion is now handled by RollerConfig
        String indexDir = RollerConfig.getProperty("search.index.dir");

        this.indexDir = indexDir.replace('/', File.separatorChar);
        
        // a little debugging
        mLogger.info("search enabled: " + this.searchEnabled);
        mLogger.info("index dir: " + this.indexDir);

        String test = indexDir + File.separator + ".index-inconsistent";
        indexConsistencyMarker = new File(test);

        // only setup the index if search is enabled
        if (this.searchEnabled) 
        {
            
            // 1. If inconsistency marker exists.
            //     Delete index
            // 2. if we're using RAM index
            //     load ram index wrapper around index
            //
            if (indexConsistencyMarker.exists()) 
            {
                getFSDirectory(true);
                inconsistentAtStartup = true;
            } 
            else 
            {
                try 
                {
                    File makeIndexDir = new File(indexDir);
                    if (!makeIndexDir.exists()) 
                    {
                        makeIndexDir.mkdirs();
                        inconsistentAtStartup = true;
                    }
                    indexConsistencyMarker.createNewFile();
                } 
                catch (IOException e) 
                {
                    mLogger.error(e);
                }
            }
            
            if (indexExists()) 
            {
                if (useRAMIndex) 
                {
                    Directory filesystem = getFSDirectory(false);
                    
                    try 
                    {
                        fRAMindex = new RAMDirectory(filesystem);
                    } 
                    catch (IOException e)                     
                    {
                        mLogger.error("Error creating in-memory index", e);
                    }
                }
            } 
            else 
            {
                if (useRAMIndex) 
                {
                    fRAMindex = new RAMDirectory();
                    createIndex(fRAMindex);
                } 
                else 
                {
                    createIndex(getFSDirectory(true));
                }
            }
            
            if (isInconsistentAtStartup())
            {
                mLogger.info(
                    "Index was inconsistent. Rebuilding index in the background...");
                try 
                {                    
                    rebuildWebsiteIndex();
                }
                catch (RollerException e) 
                {
                    mLogger.error("ERROR: scheduling re-index operation");
                }
            }       
        }
    }

    //~ Methods
    // ================================================================
    
    public void rebuildWebsiteIndex() throws RollerException
    {
        scheduleIndexOperation( 
                new RebuildWebsiteIndexOperation(this, null));
    }
    
    public void rebuildWebsiteIndex(WebsiteData website) throws RollerException
    {
        scheduleIndexOperation( 
                new RebuildWebsiteIndexOperation(this, website));
    }
    
    public void removeWebsiteIndex(WebsiteData website) throws RollerException
    {
        scheduleIndexOperation(
                new RemoveWebsiteIndexOperation(this, website));
    }
    
    public void addEntryIndexOperation(WeblogEntryData entry) throws RollerException
    {
        AddEntryOperation addEntry = new AddEntryOperation(this, entry);
        scheduleIndexOperation(addEntry);
    }
    
    public void addEntryReIndexOperation(WeblogEntryData entry) throws RollerException
    {
        ReIndexEntryOperation reindex = new ReIndexEntryOperation(this, entry);
        scheduleIndexOperation(reindex);
    }
 
    public void removeEntryIndexOperation(WeblogEntryData entry) throws RollerException
    {
        RemoveEntryOperation removeOp = new RemoveEntryOperation(this, entry);
        executeIndexOperationNow(removeOp); 
    }
    
    public ReadWriteLock getReadWriteLock()
    {
        return rwl;
    }

    public boolean isInconsistentAtStartup()
    {
        return inconsistentAtStartup;
    }

    /**
     * This is the analyzer that will be used to tokenize comment text.
     * 
     * @return Analyzer to be used in manipulating the database.
     */
    public static final Analyzer getAnalyzer()
    {
        return new StandardAnalyzer();
    }

    private void scheduleIndexOperation(final IndexOperation op)
    {
        try
        {
            // only if search is enabled
            if(this.searchEnabled) {
                mLogger.debug("Starting scheduled index operation: "+op.getClass().getName());
                RollerFactory.getRoller().getThreadManager().executeInBackground(op);
            }
        }
        catch (RollerException re)
        {
            mLogger.error("Error getting thread manager", re);
        }
        catch (InterruptedException e)
        {
            mLogger.error("Error executing operation", e);
        }
    }

    /**
     * @param search
     */
    public void executeIndexOperationNow(final IndexOperation op)
    {
        try
        {
            // only if search is enabled
            if(this.searchEnabled) {
                mLogger.debug("Executing index operation now: "+op.getClass().getName());
                RollerFactory.getRoller().getThreadManager().executeInForeground(op);
            }
        }
        catch (RollerException re)
        {
            mLogger.error("Error getting thread manager", re);
        }
        catch (InterruptedException e)
        {
            mLogger.error("Error executing operation", e);
        }
    }

    public synchronized void resetSharedReader() 
    {
        reader = null;
    }
    public synchronized IndexReader getSharedIndexReader()
    {
        if (reader == null)
        {
            try
            {
                reader = IndexReader.open(getIndexDirectory());
            }
            catch (IOException e)
            {
            }
        }
        return reader;
    }

    /**
     * Get the directory that is used by the lucene index. This method will
     * return null if there is no index at the directory location. If we are
     * using a RAM index, the directory will be a ram directory.
     * 
     * @return Directory The directory containing the index, or null if error.
     */
    public Directory getIndexDirectory()
    {
        if (useRAMIndex)
        {
            return fRAMindex;
        }
        else
        {
            return getFSDirectory(false);
        }
    }

    private boolean indexExists()
    {
        return IndexReader.indexExists(indexDir);
    }

    Directory getFSDirectory(boolean delete)
    {
        Directory directory = null;

        try
        {
            directory = FSDirectory.getDirectory(indexDir, delete);
        }
        catch (IOException e)
        {
            mLogger.error("Problem accessing index directory", e);
        }

        return directory;
    }

    private void createIndex(Directory dir)
    {
        IndexWriter writer = null;

        try
        {
            writer = new IndexWriter(dir, IndexManagerImpl.getAnalyzer(), true);
        }
        catch (IOException e)
        {
            mLogger.error("Error creating index", e);
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
            catch (IOException e)
            {
            }
        }
    }

    private IndexOperation getSaveIndexOperation()
    {
        return new WriteToIndexOperation(this) {
            public void doRun()
            {
                Directory dir = getIndexDirectory();
                Directory fsdir = getFSDirectory(true);

                IndexWriter writer = null;

                try
                {
                    writer = new IndexWriter(fsdir, IndexManagerImpl
                            .getAnalyzer(), true);

                    writer.addIndexes(new Directory[] { dir });
                    indexConsistencyMarker.delete();
                }
                catch (IOException e)
                {
                    mLogger.error("Problem saving index to disk", e);

                    // Delete the directory, since there was a problem saving
                    // the RAM contents
                    getFSDirectory(true);
                }
                finally
                {
                    try
                    {
                        if (writer != null)
                            writer.close();
                    }
                    catch (IOException e1)
                    {
                        mLogger.warn("Unable to close IndexWriter.");
                    }
                }

            }
        };
    }

    public void release() 
    {
        // no-op
    }
    
    public void shutdown()
    {
        if (useRAMIndex)
        {
            scheduleIndexOperation(getSaveIndexOperation());
        }
        else
        {
            indexConsistencyMarker.delete();
        }

        try
        {
            if (reader != null)
                reader.close();
        }
        catch (IOException e)
        {
            // won't happen, since it was
        }
    }
}