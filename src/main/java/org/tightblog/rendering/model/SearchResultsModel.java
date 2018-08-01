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
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private int resultCount;
    private int offset;
    private int limit;

    @Autowired
    private IndexManager indexManager;

    void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Override
    public boolean isSearchResults() {
        return true;
    }

    @Override
    public boolean isAddNoIndexDirective() {
        return true;
    }

    public String getSearchTerm() {
        return StringEscapeUtils.escapeXml10(pageRequest.getQuery());
    }

    public int getResultCount() {
        if (pager == null) {
            // populates resultCount
            getWeblogEntriesPager();
        }
        return resultCount;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    // override page model and return search results pager
    public WeblogEntryListData getWeblogEntriesPager() {
        if (pager == null) {
            Map<LocalDate, List<WeblogEntry>> listMap = Collections.emptyMap();

            if (pageRequest.getQuery() != null) {

                // setup the search
                SearchTask searchTask = new SearchTask(indexManager);
                searchTask.setTerm(pageRequest.getQuery());

                if (!themeManager.getSharedTheme(pageRequest.getWeblog().getTheme()).isSiteWide()) {
                    searchTask.setWeblogHandle(pageRequest.getWeblogHandle());
                }

                if (StringUtils.isNotEmpty(pageRequest.getCategory())) {
                    searchTask.setCategory(pageRequest.getCategory());
                }

                // execute search
                indexManager.executeIndexOperationNow(searchTask);

                // -1 indicates a parsing/IO error
                if (searchTask.getResultsCount() >= 0) {
                    TopFieldDocs docs = searchTask.getResults();
                    ScoreDoc[] hitsArr = docs.scoreDocs;
                    this.resultCount = searchTask.getResultsCount();

                    // Convert hits into WeblogEntry instances.  Results are mapped by Day -> Set of entries
                    // to eliminate any duplicates and then converted into Day -> List map used by pagers
                    listMap = convertHitsToEntries(hitsArr, searchTask).entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue()),
                                (v1, v2) -> {
                                    throw new RuntimeException(String.format("Duplicate key for values %s and %s",
                                            v1, v2));
                                },
                                () -> new TreeMap<LocalDate, List<WeblogEntry>>(Comparator.reverseOrder()))
                            );
                }
            }
            pager = weblogEntryListGenerator.getSearchPager(pageRequest, listMap,
                    resultCount > (offset + limit));
        }
        return pager;
    }

    /**
     * Create weblog entries for each result found.
     */
    Map<LocalDate, TreeSet<WeblogEntry>> convertHitsToEntries(ScoreDoc[] hits, SearchTask searchTask) {
        Map<LocalDate, TreeSet<WeblogEntry>> results = new HashMap<>();

        // determine offset and limit
        this.offset = pageRequest.getPageNum() * RESULTS_PER_PAGE;
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
            entry = weblogEntryManager.getWeblogEntry(doc.getField(FieldConstants.ID).stringValue(), false);

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
