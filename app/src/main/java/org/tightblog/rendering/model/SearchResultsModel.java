/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.rendering.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.business.search.FieldConstants;
import org.tightblog.business.search.IndexManager;
import org.tightblog.business.search.tasks.SearchTask;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.rendering.pagers.WeblogEntriesPager;
import org.tightblog.rendering.pagers.WeblogEntriesSearchPager;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.I18nMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Extends normal page renderer model to represent search results.
 * <p>
 * Also adds some methods which are specific only to search results.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SearchResultsModel extends PageModel {

    private static Logger log = LoggerFactory.getLogger(SearchResultsModel.class);

    private static final int RESULTS_PER_PAGE = 10;

    // the original search request
    private WeblogPageRequest searchRequest;

    // the actual search results mapped by Day -> Set of entries
    private Map<LocalDate, TreeSet<WeblogEntry>> results = new TreeMap<>(Collections.reverseOrder());

    private WeblogEntriesSearchPager pager;

    private int resultCount;
    private int offset;
    private int limit;
    private Set categories = new TreeSet();
    private boolean websiteSpecificSearch = true;
    private String errorMessage;

    @Autowired
    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * Init page model, requires a WeblogSearchRequest object.
     */
    @Override
    public void init(Map<String, Object> initData) {
        searchRequest = (WeblogPageRequest) initData.get("parsedRequest");
        if (searchRequest == null) {
            throw new IllegalStateException("expected WeblogPageRequest from init data");
        }

        // let parent initialize
        super.init(initData);

        // if there is no query, then we are done
        if (searchRequest.getQuery() == null) {
            pager = new WeblogEntriesSearchPager(urlStrategy, searchRequest, Collections.emptyMap(), false);
            return;
        }

        // setup the search
        SearchTask search = new SearchTask(indexManager);
        search.setTerm(searchRequest.getQuery());

        if (themeManager.getSharedTheme(searchRequest.getWeblog().getTheme()).isSiteWide()) {
            this.websiteSpecificSearch = false;
        } else {
            search.setWebsiteHandle(searchRequest.getWeblogHandle());
        }

        if (StringUtils.isNotEmpty(searchRequest.getWeblogCategoryName())) {
            search.setCategory(searchRequest.getWeblogCategoryName());
        }

        // execute search
        indexManager.executeIndexOperationNow(search);

        if (search.getResultsCount() == -1) {
            // this means there has been a parsing (or IO) error
            this.errorMessage = I18nMessages.getMessages(
                    searchRequest.getWeblog().getLocaleInstance()).getString("error.searchProblem");
        } else {

            TopFieldDocs docs = search.getResults();
            ScoreDoc[] hitsArr = docs.scoreDocs;
            this.resultCount = search.getResultsCount();

            // Convert the Hits into WeblogEntry instances.
            convertHitsToEntries(hitsArr, search);
        }

        // search completed, setup pager based on results
        // convert Map from <Date, Set> to <Date, List>
        Map<LocalDate, List<WeblogEntry>> listMap = results.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));

        pager = new WeblogEntriesSearchPager(urlStrategy, searchRequest, listMap,
                resultCount > (offset + limit));
    }

    @Override
    public boolean isAddNoIndexDirective() {
        return true;
    }

    // override page model and return search results pager
    public WeblogEntriesPager getWeblogEntriesPager() {
        return pager;
    }

    /**
     * Create weblog entries for each result found.
     */
    private void convertHitsToEntries(ScoreDoc[] hits, SearchTask search) {

        // determine offset
        this.offset = searchRequest.getPageNum() * RESULTS_PER_PAGE;
        if (this.offset >= hits.length) {
            this.offset = 0;
        }

        // determine limit
        this.limit = RESULTS_PER_PAGE;
        if (this.offset + this.limit > hits.length) {
            this.limit = hits.length - this.offset;
        }

        Set<String> categorySet = new TreeSet<>();

        WeblogEntry entry;
        Document doc;
        String handle;
        Instant now = Instant.now();
        for (int i = offset; i < offset + limit; i++) {
            try {
                doc = search.getSearcher().doc(hits[i].doc);
            } catch (IOException e) {
                log.warn("IOException processing {}", hits[i].doc, e);
                continue;
            }
            handle = doc.getField(FieldConstants.WEBSITE_HANDLE).stringValue();

            entry = weblogEntryManager.getWeblogEntry(doc.getField(FieldConstants.ID).stringValue(), false);

            if (!(websiteSpecificSearch && handle.equals(searchRequest.getWeblogHandle())) &&
                    doc.getField(FieldConstants.CATEGORY) != null) {
                categorySet.add(doc.getField(FieldConstants.CATEGORY).stringValue());
            }

            // maybe null if search result returned inactive user
            // or entry's user is not the requested user.
            // but don't return future posts
            if (entry != null && entry.getPubTime().isBefore(now)) {
                addEntryToResults(entry);
            }
        }

        if (categorySet.size() > 0) {
            this.categories = categorySet;
        }
    }

    private void addEntryToResults(WeblogEntry entry) {
        LocalDate pubDate = entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate();

        // ensure we do not get duplicates from Lucene by using a set collection.
        this.results.putIfAbsent(pubDate, new TreeSet<>(Comparator.comparing(WeblogEntry::getPubTime)
                .thenComparing(WeblogEntry::getTitle)));
        this.results.get(pubDate).add(entry);
    }

    @Override
    public boolean isSearchResults() {
        return true;
    }

    // TODO: handle no search terms
    public String getSearchTerm() {
        String query = searchRequest.getQuery();
        return (query == null) ? "" : StringEscapeUtils.escapeXml10(query);
    }

    public int getResultCount() {
        return resultCount;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public Map getResults() {
        return results;
    }

    public Set getCategories() {
        return categories;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getCategoryName() {
        return searchRequest.getWeblogCategoryName();
    }
}
