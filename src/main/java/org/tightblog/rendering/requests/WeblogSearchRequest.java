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
package org.tightblog.rendering.requests;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
import org.tightblog.rendering.model.SearchResultsModel;
import org.tightblog.service.indexer.FieldConstants;
import org.tightblog.service.indexer.SearchTask;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WeblogSearchRequest extends WeblogPageRequest {

    private static Logger log = LoggerFactory.getLogger(WeblogSearchRequest.class);

    private static final int RESULTS_PER_PAGE = 10;

    private String searchPhrase;
    private int resultCount;
    private int offset;
    private int limit;
    private SearchResultsModel searchModel;

    public WeblogSearchRequest(String weblogHandle, Principal principal, SearchResultsModel searchModel) {
        super(weblogHandle, principal, searchModel);
        this.searchModel = searchModel;
    }

    public String getSearchPhrase() {
        return searchPhrase;
    }

    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getResultCount() {
        if (pager == null) {
            // populates resultCount
            getWeblogEntriesPager();
        }
        return resultCount;
    }

    @Override
    public boolean isSearchResults() {
        return true;
    }

    @Override
    // TODO: override needed?
    public boolean isNoIndex() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("WeblogSearchRequest: parent=%s category=%s searchPhrase=%s",
                super.toString(), category, searchPhrase);
    }

    // override page model and return search results pager
    @Override
    public WeblogEntryListGenerator.WeblogEntryListData getWeblogEntriesPager() {
        if (pager == null) {
            Map<LocalDate, List<WeblogEntry>> entriesByDateMap = Collections.emptyMap();

            if (searchPhrase != null) {

                // setup the search
                SearchTask searchTask = new SearchTask(searchModel.getLuceneIndexer());
                searchTask.setTerm(searchPhrase);

                if (!searchModel.getThemeManager().getSharedTheme(weblog.getTheme()).isSiteWide()) {
                    searchTask.setWeblogHandle(getWeblogHandle());
                }

                if (StringUtils.isNotEmpty(category)) {
                    searchTask.setCategory(category);
                }

                // execute search
                searchModel.getLuceneIndexer().executeIndexOperationNow(searchTask);

                // -1 indicates a parsing/IO error
                if (searchTask.getResultsCount() >= 0) {
                    TopFieldDocs docs = searchTask.getResults();
                    ScoreDoc[] hitsArr = docs.scoreDocs;
                    this.resultCount = searchTask.getResultsCount();

                    // Convert hits into WeblogEntry instances.  Results are mapped by Day -> Set of entries
                    // to eliminate any duplicates and then converted into Day -> List map used by pagers
                    entriesByDateMap = convertHitsToEntries(hitsArr, searchTask).entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue()),
                                    (v1, v2) -> {
                                        throw new RuntimeException(String.format("Duplicate key for values %s and %s",
                                                v1, v2));
                                    },
                                    () -> new TreeMap<LocalDate, List<WeblogEntry>>(Comparator.reverseOrder()))
                            );
                }
            }
            pager = searchModel.getWeblogEntryListGenerator().getSearchPager(weblog, searchPhrase,
                    category, getPageNum(), entriesByDateMap, resultCount > (offset + limit));
        }
        return pager;
    }

    /**
     * Create weblog entries for each result found.
     */
    public Map<LocalDate, TreeSet<WeblogEntry>> convertHitsToEntries(ScoreDoc[] hits, SearchTask searchTask) {
        Map<LocalDate, TreeSet<WeblogEntry>> results = new HashMap<>();

        // determine offset and limit
        this.offset = getPageNum() * RESULTS_PER_PAGE;
        if (this.offset >= hits.length) {
            this.offset = 0;
        }

        this.limit = RESULTS_PER_PAGE;
        if (this.offset + this.limit > hits.length) {
            this.limit = hits.length - this.offset;
        }

        WeblogEntry entry;
        Document doc;
        for (int i = offset; i < offset + limit; i++) {
            try {
                doc = searchTask.getSearcher().doc(hits[i].doc);
            } catch (IOException e) {
                log.warn("IOException processing {}", hits[i].doc, e);
                continue;
            }
            entry = searchModel.getWeblogEntryDao().
                    findByIdOrNull(doc.getField(FieldConstants.ID).stringValue());

            if (entry != null && WeblogEntry.PubStatus.PUBLISHED.equals(entry.getStatus())) {
                LocalDate pubDate = entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate();

                // ensure we do not get duplicates from Lucene by using a set collection.
                results.putIfAbsent(pubDate, new TreeSet<>(Comparator.comparing(WeblogEntry::getPubTime).reversed()
                        .thenComparing(WeblogEntry::getTitle)));
                results.get(pubDate).add(entry);
            }
        }

        return results;
    }
}
