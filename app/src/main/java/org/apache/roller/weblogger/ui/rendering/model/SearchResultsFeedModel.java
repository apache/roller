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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.SearchResultList;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.SearchResultsFeedPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Extends normal page renderer model to represent search results for Atom
 * feeds.
 * 
 * Also adds some new methods which are specific only to search results.
 */
public class SearchResultsFeedModel implements Model {

	private WeblogFeedRequest feedRequest = null;
	private URLStrategy urlStrategy = null;
	private Weblog weblog = null;

	// the pager used by the 3.0+ rendering system
	private SearchResultsFeedPager pager = null;

	private List<WeblogEntryWrapper> results = new ArrayList<>();

	private Set<String> categories = Collections.emptySet();

	private int hits = 0;
	private int offset = 0;
	private int limit = 0;

	private String errorMessage = "";

	@Override
	public String getModelName() {
		return "model";
	}

    @Override
	public void init(Map<String, Object> initData) throws WebloggerException {

		// we expect the init data to contain a weblogRequest object
		WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
		if (weblogRequest == null) {
			throw new WebloggerException("expected weblogRequest from init data");
		}

		if (weblogRequest instanceof WeblogFeedRequest) {
			this.feedRequest = (WeblogFeedRequest) weblogRequest;
		} else {
			throw new WebloggerException(
					"weblogRequest is not a WeblogFeedRequest."
					+ "  FeedModel only supports feed requests.");
		}

		// look for url strategy
		urlStrategy = (URLStrategy) initData.get("urlStrategy");
		if (urlStrategy == null) {
			urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();
		}

		// extract weblog object
		weblog = feedRequest.getWeblog();

		String pagerUrl = urlStrategy.getWeblogFeedURL(weblog,
				feedRequest.getLocale(), feedRequest.getType(),
				// cat and term below null but added to URL in pager
                feedRequest.getFormat(), null, null,
				null, false, true);

		// if there is no query, then we are done
		if (feedRequest.getTerm() == null) {
			pager = new SearchResultsFeedPager(urlStrategy, pagerUrl,
					feedRequest.getPage(), feedRequest, results, false);
			return;
		}

		int entryCount = WebloggerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");

		// setup the search
		IndexManager indexMgr = WebloggerFactory.getWeblogger().getIndexManager();
		try {
			SearchResultList searchResult = indexMgr.search(
				feedRequest.getTerm(),
				feedRequest.getWeblogHandle(),
				feedRequest.getWeblogCategoryName(),
				feedRequest.getLocale(),
				feedRequest.getPage(),
				entryCount,
				urlStrategy
			);
			this.hits = searchResult.getResults().size();
			this.offset = searchResult.getOffset();
			this.limit = searchResult.getLimit();
			this.results = searchResult.getResults();
			this.categories = searchResult.getCategories();

		} catch (WebloggerException we) {
			errorMessage = we.getMessage();
		}

		// search completed, setup pager based on results
		pager = new SearchResultsFeedPager(urlStrategy, pagerUrl,
				feedRequest.getPage(), feedRequest, results,
				(hits > (offset + limit)));
	}

	public Pager<WeblogEntryWrapper> getSearchResultsPager() {
		return pager;
	}


	/**
	 * Get weblog being displayed.
	 */
	public WeblogWrapper getWeblog() {
		return WeblogWrapper.wrap(weblog, urlStrategy);
	}

	public String getTerm() {
		String query =feedRequest.getTerm() ;
		return (query == null) 
			? "" : StringEscapeUtils.escapeXml11(Utilities.escapeHTML(query));
	}

	public int getHits() {
		return hits;
	}

	public int getOffset() {
		return offset;
	}

	public int getPage() {
		return feedRequest.getPage();
	}

	public int getLimit() {
		return limit;
	}

	public List<WeblogEntryWrapper> getResults() {
		return results;
	}

	public Set<String> getCategories() {
		return categories;
	}

	public String getCategoryName() {
		return feedRequest.getWeblogCategoryName();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public WeblogCategoryWrapper getWeblogCategory() {
		if (feedRequest.getWeblogCategory() != null) {
			return WeblogCategoryWrapper.wrap(feedRequest.getWeblogCategory(),
				urlStrategy);
		}
		return null;
	}
}
