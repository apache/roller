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
 * Created on Jul 18, 2003
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.apache.roller.business.search.operations;

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
import org.apache.roller.business.IndexManagerImpl;
import org.apache.roller.business.search.FieldConstants;
import org.apache.roller.business.search.IndexUtil;
import org.apache.roller.model.IndexManager;


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
    private String websiteHandle;
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
                IndexUtil.getTerm(FieldConstants.WEBSITE_HANDLE, websiteHandle);

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
    public void setWebsiteHandle(String websiteHandle)
    {
        this.websiteHandle = websiteHandle;
    }

    /**
     * @param parameter
     */
    public void setCategory(String category)
    {
        this.category = category;
    }

}
