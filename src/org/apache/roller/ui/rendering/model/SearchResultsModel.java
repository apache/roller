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

package org.apache.roller.ui.rendering.model;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.roller.RollerException;
import org.apache.roller.business.search.FieldConstants;
import org.apache.roller.business.search.operations.SearchOperation;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.IndexManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogEntryWrapperComparator;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.ui.rendering.pagers.SearchResultsPager;
import org.apache.roller.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.util.DateUtil;


/**
 * Extends normal page renderer model to represent search results.
 *
 * Also adds some new methods which are specific only to search results.
 */
public class SearchResultsModel extends PageModel {
    
    private static final ResourceBundle bundle = 
            ResourceBundle.getBundle("ApplicationResources");
    
    public static final int RESULTS_PER_PAGE = 10;
    
    
    // the original search request
    WeblogSearchRequest searchRequest = null;
    
    // the actual search results mapped by Day -> Set of entries
    private TreeMap results = new TreeMap(new ReverseComparator());
    
    // the pager used by the 3.0+ rendering system
    private SearchResultsPager pager = null;
    
    private int hits = 0;
    private int offset = 0;
    private int limit = 0;
    private Set categories = new TreeSet();
    private boolean websiteSpecificSearch = true;
    private String errorMessage = null;
    
    
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a searchRequest object
        searchRequest = (WeblogSearchRequest) initData.get("searchRequest");
        if(searchRequest == null) {
            throw new RollerException("expected searchRequest from init data");
        }
        
        // let parent initialize
        super.init(initData);
        
        // setup the search
        IndexManager indexMgr = RollerFactory.getRoller().getIndexManager();
        
        SearchOperation search = new SearchOperation(indexMgr);
        search.setTerm(searchRequest.getQuery());
        
        if(RollerRuntimeConfig.isSiteWideWeblog(searchRequest.getWeblogHandle())) {
            this.websiteSpecificSearch = false;
        } else {
            search.setWebsiteHandle(searchRequest.getWeblogHandle());
        }
        
        if(StringUtils.isNotEmpty(searchRequest.getWeblogCategoryName())) {
            search.setCategory(searchRequest.getWeblogCategoryName());
        }
        
        // execute search
        indexMgr.executeIndexOperationNow(search);
        
        if (search.getResultsCount() == -1) {
            // this means there has been a parsing (or IO) error
            this.errorMessage = bundle.getString("error.searchProblem");
        } else {
            Hits hits = search.getResults();
            this.hits = search.getResultsCount();
            
            // Convert the Hits into WeblogEntryData instances.
            convertHitsToEntries(hits);
        }
        
        // search completed, setup pager based on results
        pager = new SearchResultsPager(searchRequest, results, (hits > (offset+limit)));
    }
    
    /**
     * Is this page showing search results?
     */
    public boolean isSearchResults() {
        return true;
    }
    
    // override page model and return search results pager
    public WeblogEntriesPager getWeblogEntriesPager() {
        return pager;
    }
    
    // override page model and return search results pager
    public WeblogEntriesPager getWeblogEntriesPager(String category) {
        return pager;
    }
    
    private void convertHitsToEntries(Hits hits) throws RollerException {
        
        // determine offset
        this.offset = searchRequest.getPageNum() * RESULTS_PER_PAGE;
        if(this.offset >= hits.length()) {
            this.offset = 0;
        }
        
        // determine limit
        this.limit = RESULTS_PER_PAGE;
        if(this.offset + this.limit > hits.length()) {
            this.limit = hits.length() - this.offset;
        }
        
        try {
            TreeSet categories = new TreeSet();
            Roller roller = RollerFactory.getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();
            
            WeblogEntryData entry = null;
            Document doc = null;
            String handle = null;
            for(int i = offset; i < offset+limit; i++) {
                
                entry = null; // reset for each iteration
                
                doc = hits.doc(i);
                handle = doc.getField(FieldConstants.WEBSITE_HANDLE).stringValue();
                
                if(websiteSpecificSearch &&
                        handle.equals(searchRequest.getWeblogHandle())) {
                    
                    entry = weblogMgr.getWeblogEntry(
                            doc.getField(FieldConstants.ID).stringValue());
                } else {
                    
                    entry = weblogMgr.getWeblogEntry(
                            doc.getField(FieldConstants.ID).stringValue());
                    
                    if (doc.getField(FieldConstants.CATEGORY) != null) {
                        categories.add(
                                doc.getField(FieldConstants.CATEGORY).stringValue());
                    }
                }
                
                // maybe null if search result returned inactive user
                // or entry's user is not the requested user.
                if (entry != null) {
                    addEntryToResults(WeblogEntryDataWrapper.wrap(entry));
                }
            }
            
            if(categories.size() > 0) {
                this.categories = categories;
            }
        } catch(IOException e) {
            throw new RollerException(e);
        }
    }
    
    
    private void addEntryToResults(WeblogEntryDataWrapper entry) {
        
        // convert entry's each date to midnight (00m 00h 00s)
        Date midnight = DateUtil.getStartOfDay(entry.getPubTime());
        
        // ensure we do not get duplicates from Lucene by
        // using a Set Collection.  Entries sorted by pubTime.
        TreeSet set = (TreeSet) this.results.get(midnight);
        if (set == null) {
            // date is not mapped yet, so we need a new Set
            set = new TreeSet( new WeblogEntryWrapperComparator());
            this.results.put(midnight, set);
        }
        set.add(entry);
    }
    
    
    public String getTerm() {
        return searchRequest.getQuery();
    }

    public int getHits() {
        return hits;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public TreeMap getResults() {
        return results;
    }

    public Set getCategories() {
        return categories;
    }

    public boolean isWebsiteSpecificSearch() {
        return websiteSpecificSearch;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
