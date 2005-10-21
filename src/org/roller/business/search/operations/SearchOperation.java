/*
 * Created on Jul 18, 2003
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.business.search.operations;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.roller.business.IndexManagerImpl;
import org.roller.business.search.FieldConstants;
import org.roller.business.search.IndexUtil;
import org.roller.model.IndexManager;


/**
 * @author aim4min
 *
 * An operation that searches the index.
 */
public class SearchOperation extends ReadFromIndexOperation
{
    //~ Static fields/initializers =============================================

    private static Log mLogger =
        LogFactory.getFactory().getInstance(SearchOperation.class);
        
    private static String[] SEARCH_FIELDS = new String[]{
        FieldConstants.CONTENT, FieldConstants.TITLE, 
        FieldConstants.C_CONTENT, FieldConstants.CATEGORY
    };
    
    private static Sort SORTER = new Sort( new SortField(
        FieldConstants.PUBLISHED, SortField.STRING, true) );

    //~ Instance fields ========================================================

    private String term;
    private String username;
    private String category;
    private Hits searchresults;
    private String parseError;

    //~ Constructors ===========================================================

    /**
     * Create a new operation that searches the index.
     */
    public SearchOperation(IndexManager mgr)
    {
        // TODO: finish moving  IndexManager to backend, so this cast is not needed
        super((IndexManagerImpl)mgr); 
    }

    //~ Methods ================================================================

    public void setTerm(String term)
    {
        this.term = term;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void doRun()
    {
        searchresults = null;
      		
        IndexSearcher searcher = null;

        try
        {
            IndexReader reader = manager.getSharedIndexReader();
            searcher = new IndexSearcher(reader);

            Query query =
                MultiFieldQueryParser.parse(
                    term, SEARCH_FIELDS, new StandardAnalyzer());

            Term tUsername =
                IndexUtil.getTerm(FieldConstants.USERNAME, username);

            if (tUsername != null)
            {
                BooleanQuery bQuery = new BooleanQuery();
                bQuery.add(query, true, false);
                bQuery.add(new TermQuery(tUsername), true, false);
                query = bQuery;
            }
            
            Term tCategory =
                IndexUtil.getTerm(FieldConstants.CATEGORY, category);

            if (tCategory != null)
            {
                BooleanQuery bQuery = new BooleanQuery();
                bQuery.add(query, true, false);
                bQuery.add(new TermQuery(tCategory), true, false);
                query = bQuery;
            }
            searchresults = searcher.search(query, null/*Filter*/, SORTER);
        }
        catch (IOException e)
        {
            mLogger.error("Error searching index", e);
            parseError = e.getMessage();
        }
        catch (ParseException e)
        {
            // who cares?
            parseError = e.getMessage();
        }
        // don't need to close the reader, since we didn't do any writing!
    }

    public Hits getResults()
    {
        return searchresults;
    }
    
    public int getResultsCount()
    {
        if (searchresults == null) return -1;
        
        return searchresults.length();
    }
    
    public String getParseError()
    {
        return parseError;
    }

    /**
     * @param string
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @param parameter
     */
    public void setCategory(String category)
    {
        this.category = category;
    }

}
