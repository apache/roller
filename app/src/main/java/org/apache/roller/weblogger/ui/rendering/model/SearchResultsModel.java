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

package org.apache.roller.weblogger.ui.rendering.model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.operations.SearchOperation;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryWrapperComparator;
import org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.ui.rendering.pagers.SearchResultsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Extends normal page renderer model to represent search results.
 * 
 * Also adds some new methods which are specific only to search results.
 */
public class SearchResultsModel extends PageModel {

	public static final int RESULTS_PER_PAGE = 10;

	// the original search request
	WeblogSearchRequest searchRequest = null;
	private URLStrategy urlStrategy = null;

	// the actual search results mapped by Day -> Set of entries
	private TreeMap<Date, TreeSet<WeblogEntryWrapper>> results
            = new TreeMap<Date, TreeSet<WeblogEntryWrapper>>(new ReverseComparator());

	// the pager used by the 3.0+ rendering system
	private SearchResultsPager pager = null;

	private int hits = 0;
	private int offset = 0;
	private int limit = 0;
	private Set categories = new TreeSet();
	private boolean websiteSpecificSearch = true;
	private String errorMessage = null;

	public void init(Map initData) throws WebloggerException {

		// we expect the init data to contain a searchRequest object
		searchRequest = (WeblogSearchRequest) initData.get("searchRequest");
		if (searchRequest == null) {
			throw new WebloggerException(
					"expected searchRequest from init data");
		}

		// look for url strategy
		urlStrategy = (URLStrategy) initData.get("urlStrategy");
		if (urlStrategy == null) {
			urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();
		}

		// let parent initialize
		super.init(initData);

		// if there is no query, then we are done
		if (searchRequest.getQuery() == null) {
			pager = new SearchResultsPager(urlStrategy, searchRequest, results,
					false);
			return;
		}

		// setup the search
		IndexManager indexMgr = WebloggerFactory.getWeblogger()
				.getIndexManager();

		SearchOperation search = new SearchOperation(indexMgr);
		search.setTerm(searchRequest.getQuery());

		if (WebloggerRuntimeConfig.isSiteWideWeblog(searchRequest
				.getWeblogHandle())) {
			this.websiteSpecificSearch = false;
		} else {
			search.setWebsiteHandle(searchRequest.getWeblogHandle());
		}

		if (StringUtils.isNotEmpty(searchRequest.getWeblogCategoryName())) {
			search.setCategory(searchRequest.getWeblogCategoryName());
		}

		if (searchRequest.getLocale() != null) {
			search.setLocale(searchRequest.getLocale());
		}

		// execute search
		indexMgr.executeIndexOperationNow(search);

		if (search.getResultsCount() == -1) {
			// this means there has been a parsing (or IO) error
			this.errorMessage = I18nMessages.getMessages(
					searchRequest.getLocaleInstance()).getString(
					"error.searchProblem");
		} else {

			TopFieldDocs docs = search.getResults();
			ScoreDoc[] hits = docs.scoreDocs;
			this.hits = search.getResultsCount();

			// Convert the Hits into WeblogEntryData instances.
			convertHitsToEntries(hits, search);

		}

		// search completed, setup pager based on results
		pager = new SearchResultsPager(urlStrategy, searchRequest, results,
				(hits > (offset + limit)));
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

	/**
	 * Convert hits to entries.
	 * 
	 * @param hits
	 *            the hits
	 * @param search
	 *            the search
	 * @throws WebloggerException
	 *             the weblogger exception
	 */
	private void convertHitsToEntries(ScoreDoc[] hits, SearchOperation search)
			throws WebloggerException {

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

		try {
			TreeSet<String> categories = new TreeSet<String>();
			Weblogger roller = WebloggerFactory.getWeblogger();
			WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();

			WeblogEntry entry;
			Document doc;
			String handle;
			Timestamp now = new Timestamp(new Date().getTime());
			for (int i = offset; i < offset + limit; i++) {
				doc = search.getSearcher().doc(hits[i].doc);
				handle = doc.getField(FieldConstants.WEBSITE_HANDLE)
						.stringValue();

                entry = weblogMgr.getWeblogEntry(doc.getField(
                        FieldConstants.ID).stringValue());

                if (!(websiteSpecificSearch && handle.equals(searchRequest.getWeblogHandle()))
                        && doc.getField(FieldConstants.CATEGORY) != null) {
                    categories.add(doc.getField(FieldConstants.CATEGORY).stringValue());
                }

				// maybe null if search result returned inactive user
				// or entry's user is not the requested user.
				// but don't return future posts
				if (entry != null && entry.getPubTime().before(now)) {
					addEntryToResults(WeblogEntryWrapper.wrap(entry,
							urlStrategy));
				}
			}

			if (categories.size() > 0) {
				this.categories = categories;
			}
		} catch (IOException e) {
			throw new WebloggerException(e);
		}
	}

	private void addEntryToResults(WeblogEntryWrapper entry) {

		// convert entry's each date to midnight (00m 00h 00s)
		Date midnight = DateUtil.getStartOfDay(entry.getPubTime());

		// ensure we do not get duplicates from Lucene by
		// using a Set Collection. Entries sorted by pubTime.
		TreeSet<WeblogEntryWrapper> set = this.results.get(midnight);
		if (set == null) {
			// date is not mapped yet, so we need a new Set
			set = new TreeSet<WeblogEntryWrapper>(new WeblogEntryWrapperComparator());
			this.results.put(midnight, set);
		}
		set.add(entry);
	}

	public String getTerm() {
		String query = searchRequest.getQuery();
		return (query == null) 
			? "" : StringEscapeUtils.escapeXml(Utilities.escapeHTML(query));
	}

	public String getRawTerm() {
		return (searchRequest.getQuery() == null) ? "" : searchRequest
				.getQuery();
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

	public String getWeblogCategoryName() {
		return searchRequest.getWeblogCategoryName();
	}

	public WeblogCategoryWrapper getWeblogCategory() {
		if (searchRequest.getWeblogCategory() != null) {
			return WeblogCategoryWrapper.wrap(
					searchRequest.getWeblogCategory(), urlStrategy);
		}
		return null;
	}

}
