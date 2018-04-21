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
package org.tightblog.business.search.tasks;

import org.apache.lucene.analysis.Analyzer;
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
import org.tightblog.business.search.FieldConstants;
import org.tightblog.business.search.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Task for searching the index to retrieve blog entries.
 */
public class SearchTask extends AbstractTask {

    private static final Logger LOG = LoggerFactory.getLogger(SearchTask.class);

    // Fields that a user may search on (even if more fields are indexed)
    private static final String[] SEARCH_FIELDS = new String[]{
            FieldConstants.CONTENT, FieldConstants.TITLE,
            FieldConstants.COMMENT_CONTENT};

    private static final Sort SORTER = new Sort(new SortField(
            FieldConstants.PUBLISHED, SortField.Type.STRING, true));

    private IndexSearcher searcher;
    private TopFieldDocs searchResults;

    private String term;
    private String weblogHandle;
    private String category;

    public SearchTask(IndexManager mgr) {
        super(mgr);
    }

    @Override
    public void run() {
        try {
            manager.getReadWriteLock().readLock().lock();
            doRun();
        } catch (Exception e) {
            LOG.info("Error acquiring read lock on index", e);
        } finally {
            manager.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public void doRun() {
        final int docLimit = 500;
        searchResults = null;
        searcher = null;

        try (Analyzer analyzer = manager.getAnalyzer()) {
            if (analyzer != null) {
                IndexReader reader = manager.getDirectoryReader();
                if (searcher == null) {
                    searcher = new IndexSearcher(reader);
                }

                MultiFieldQueryParser multiParser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer);

                // Make it an AND by default. Comment this out for an or (default)
                multiParser.setDefaultOperator(MultiFieldQueryParser.Operator.AND);

                // Create a query object out of our term
                Query query = multiParser.parse(term);

                Term tUsername = getTerm(FieldConstants.WEBSITE_HANDLE, weblogHandle);

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

                searchResults = searcher.search(query, docLimit, SORTER);
            }
        } catch (IOException | ParseException e) {
            LOG.error("Error searching index", e);
        }
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    // for testing
    public void setSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    public TopFieldDocs getResults() {
        return searchResults;
    }

    // for testing
    public void setResults(TopFieldDocs results) {
        this.searchResults = results;
    }

    public int getResultsCount() {
        if (searchResults == null) {
            return -1;
        }
        return searchResults.totalHits;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
