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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.SearchResultMap;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.ui.rendering.pagers.SearchResultsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest;

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
    private Map<Date, Set<WeblogEntryWrapper>> results = new TreeMap<>(Collections.reverseOrder());

	// the pager used by the 3.0+ rendering system
	private SearchResultsPager pager = null;

	private int hits = 0;
	private int offset = 0;
	private int limit = 0;
	private Set<String> categories = new TreeSet<String>();
	private String errorMessage = "";

	@Override
	public void init(Map<String, Object> initData) throws WebloggerException {

		// we expect the init data to contain a searchRequest object
		searchRequest = (WeblogSearchRequest) initData.get("searchRequest");
		if (searchRequest == null) {
			throw new WebloggerException("expected searchRequest from init data");
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
			pager = new SearchResultsPager(urlStrategy, searchRequest, results, false);
			return;
		}

		// setup the search
		IndexManager indexMgr = WebloggerFactory.getWeblogger().getIndexManager();
		try {
			SearchResultMap searchResultMap = indexMgr.searchByDay(
				searchRequest.getQuery(),
				searchRequest.getWeblogHandle(),
				searchRequest.getWeblogCategoryName(),
				searchRequest.getLocale(),
				searchRequest.getPageNum(),
				urlStrategy
			);
			this.hits = searchResultMap.getResults().size();
			this.offset = searchResultMap.getOffset();
			this.limit = searchResultMap.getLimit();
			this.results = searchResultMap.getResults();
			this.categories = searchResultMap.getCategories();

		} catch (WebloggerException we) {
			errorMessage = we.getMessage();
		}

		// search completed, setup pager based on results
		pager = new SearchResultsPager(
			urlStrategy, searchRequest, results, (hits > (offset + limit)));
	}

	/**
	 * Is this page showing search results?
	 */
	@Override
	public boolean isSearchResults() {
		return true;
	}

	// override page model and return search results pager
	@Override
	public WeblogEntriesPager getWeblogEntriesPager() {
		return pager;
	}

	// override page model and return search results pager
	@Override
	public WeblogEntriesPager getWeblogEntriesPager(String category) {
		return pager;
	}

	public String getTerm() {
		String query = searchRequest.getQuery();
        return (query == null)
			? "" : StringEscapeUtils.escapeXml10(query);
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

	public Map<Date, Set<WeblogEntryWrapper>> getResults() {
		return results;
	}

	public Set<String> getCategories() {
		return categories;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getWeblogCategoryName() {
		return searchRequest.getWeblogCategoryName();
	}

	@Override
	public WeblogCategoryWrapper getWeblogCategory() {
		if (searchRequest.getWeblogCategory() != null) {
			return WeblogCategoryWrapper.wrap(searchRequest.getWeblogCategory(), urlStrategy);
		}
		return null;
	}

}
