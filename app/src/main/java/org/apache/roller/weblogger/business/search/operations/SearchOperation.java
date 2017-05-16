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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.search.operations;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An operation that searches the index.
 */
public class SearchOperation extends ReadFromIndexOperation {

    // ~ Static fields/initializers
    // =============================================

    private static Logger log = LoggerFactory.getLogger(SearchOperation.class);

    // Fields that a user may search on (even if more fields are indexed)
    private static String[] searchFields = new String[]{
            FieldConstants.CONTENT, FieldConstants.TITLE,
            FieldConstants.COMMENT_CONTENT};

    private static Sort sorter = new Sort(new SortField(
            FieldConstants.PUBLISHED, SortField.Type.STRING, true));

    // ~ Instance fields
    // ========================================================

    private IndexSearcher searcher;
    private TopFieldDocs searchresults;

    private String term;
    private String websiteHandle;
    private String category;

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

    public void doRun() {
        final int docLimit = 500;
        searchresults = null;
        searcher = null;

        try {
            IndexReader reader = manager.getSharedIndexReader();
            searcher = new IndexSearcher(reader);

            MultiFieldQueryParser multiParser = new MultiFieldQueryParser(
                    searchFields,
                    IndexManagerImpl.getAnalyzer());

            // Make it an AND by default. Comment this out for an or (default)
            multiParser.setDefaultOperator(MultiFieldQueryParser.Operator.AND);

            // Create a query object out of our term
            Query query = multiParser.parse(term);

            Term tUsername = IndexOperation.getTerm(FieldConstants.WEBSITE_HANDLE,
                    websiteHandle);

            if (tUsername != null) {
                query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(new TermQuery(tUsername), BooleanClause.Occur.MUST)
                        .build();
            }

            if (category != null) {
                Term tCategory = new Term(FieldConstants.CATEGORY, category.toLowerCase());
                query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(new TermQuery(tCategory), BooleanClause.Occur.MUST)
                        .build();
            }

            searchresults = searcher.search(query, docLimit, sorter);

        } catch (IOException e) {
            log.error("Error searching index", e);
        } catch (ParseException e) {
            // who cares?
            log.error("Parser error searching index", e);
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
        if (searchresults == null) {
            return -1;
        }
        return searchresults.totalHits;
    }

    /**
     * Sets the website handle.
     *
     * @param websiteHandle the new website handle
     */
    public void setWebsiteHandle(String websiteHandle) {
        this.websiteHandle = websiteHandle;
    }

    /**
     * Sets the category.
     *
     * @param category the new category
     */
    public void setCategory(String category) {
        this.category = category;
    }

}
