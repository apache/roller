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
/* Created on Jul 18, 2003 */
package org.apache.roller.weblogger.business.search.operations;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.search.IndexUtil;

/**
 * An operation that searches the index.
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class SearchOperation extends ReadFromIndexOperation {

    // ~ Static fields/initializers
    // =============================================

    private static Log mLogger = LogFactory.getFactory().getInstance(
            SearchOperation.class);

    private static String[] SEARCH_FIELDS = new String[] {
            FieldConstants.CONTENT, FieldConstants.TITLE,
            FieldConstants.C_CONTENT, FieldConstants.CATEGORY };

    // private static BooleanClause.Occur[] SEARCH_FLAGS = new
    // BooleanClause.Occur[] {
    // BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
    // BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD };

    private static Sort SORTER = new Sort(new SortField(
            FieldConstants.PUBLISHED, SortField.Type.STRING, true));

    // ~ Instance fields
    // ========================================================

    private IndexSearcher searcher;
    private TopFieldDocs searchresults;

    private String term;
    private String websiteHandle;
    private String category;
    private String parseError;

    private int nMax = 500; // Limit documents.

    // ~ Constructors
    // ===========================================================

    /**
     * Create a new operation that searches the index.
     */
    public SearchOperation(IndexManager mgr) {
        // TODO: finish moving IndexManager to backend, so this cast is not
        // needed
        super((IndexManagerImpl) mgr);
    }

    // ~ Methods
    // ================================================================

    public void setTerm(String term) {
        this.term = term;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void doRun() {
        searchresults = null;

        searcher = null;

        try {
            IndexReader reader = manager.getSharedIndexReader();
            searcher = new IndexSearcher(reader);

            // Query query = MultiFieldQueryParser.parse(
            // FieldConstants.LUCENE_VERSION, term, SEARCH_FIELDS,
            // SEARCH_FLAGS, IndexManagerImpl.getAnalyzer());

            MultiFieldQueryParser multiParser = new MultiFieldQueryParser(
                    FieldConstants.LUCENE_VERSION, SEARCH_FIELDS,
                    IndexManagerImpl.getAnalyzer());

            // Make it an AND by default. Comment this out for an or (default)
            multiParser.setDefaultOperator(MultiFieldQueryParser.Operator.AND);

            // Create a query object out of our term
            Query query = multiParser.parse(term);

            Term tUsername = IndexUtil.getTerm(FieldConstants.WEBSITE_HANDLE,
                    websiteHandle);

            if (tUsername != null) {
                BooleanQuery bQuery = new BooleanQuery();
                bQuery.add(query, BooleanClause.Occur.MUST);
                bQuery.add(new TermQuery(tUsername), BooleanClause.Occur.MUST);
                query = bQuery;
            }

            Term tCategory = IndexUtil.getTerm(FieldConstants.CATEGORY,
                    category);

            if (tCategory != null) {
                BooleanQuery bQuery = new BooleanQuery();
                bQuery.add(query, BooleanClause.Occur.MUST);
                bQuery.add(new TermQuery(tCategory), BooleanClause.Occur.MUST);
                query = bQuery;
            }

            searchresults = searcher.search(query, null/* Filter */, nMax,
                    SORTER);

        } catch (IOException e) {
            mLogger.error("Error searching index", e);
            parseError = e.getMessage();

        } catch (Exception e) {
            // who cares?
            parseError = e.getMessage();
        }
        // don't need to close the reader, since we didn't do any writing!
    }

    /**
     * Gets the searcher.
     * 
     * @return the searcher
     */
    public IndexSearcher getSearcher() {
        return searcher;
    }

    /**
     * Sets the searcher.
     * 
     * @param searcher
     *            the new searcher
     */
    public void setSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    /**
     * Gets the results.
     * 
     * @return the results
     */
    public TopFieldDocs getResults() {
        return searchresults;
    }

    /**
     * Gets the results count.
     * 
     * @return the results count
     */
    public int getResultsCount() {
        if (searchresults == null)
            return -1;

        return searchresults.totalHits;
    }

    /**
     * Gets the parses the error.
     * 
     * @return the parses the error
     */
    public String getParseError() {
        return parseError;
    }

    /**
     * Sets the website handle.
     * 
     * @param websiteHandle
     *            the new website handle
     */
    public void setWebsiteHandle(String websiteHandle) {
        this.websiteHandle = websiteHandle;
    }

    /**
     * Sets the category.
     * 
     * @param category
     *            the new category
     */
    public void setCategory(String category) {
        this.category = category;
    }

}
