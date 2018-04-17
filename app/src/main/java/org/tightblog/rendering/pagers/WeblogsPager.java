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

package org.tightblog.rendering.pagers;

import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Weblog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paging through a collection of weblogs.
 */
public class WeblogsPager implements Pager {

    private static Logger log = LoggerFactory.getLogger(WeblogsPager.class);

    private String letter;
    private int length;
    private String url;
    private int page;

    // collection for the pager
    private List<Weblog> weblogs;

    // are there more items?
    private boolean more;

    private WeblogManager weblogManager;

    public WeblogsPager(
            WeblogManager weblogManager,
            String baseUrl,
            String letter,
            int page,
            int length) {

        this.url = baseUrl;
        if (page > 0) {
            this.page = page;
        }

        this.weblogManager = weblogManager;
        this.letter = letter;
        this.length = length;

        // initialize the collection
        getItems();
    }

    @Override
    public String getHomeLink() {
        return url;
    }

    @Override
    public String getHomeLabel() {
        return "Home";
    }

    @Override
    public String getNextLink() {
        // need to add letter param if it exists
        if (letter != null) {
            int nextPage = getPage() + 1;
            if (hasMoreItems()) {
                Map<String, String> params = new HashMap<>();
                params.put("page", "" + nextPage);
                params.put("letter", letter);
                return createURL(getUrl(), params);
            }
            return null;
        } else {
            if (hasMoreItems()) {
                int nextPage = page + 1;
                Map<String, String> params = new HashMap<>();
                params.put("page", "" + nextPage);
                return createURL(url, params);
            }
            return null;
        }
    }

    private String createURL(String urlToCreate, Map<String, String> params) {
        return urlToCreate + Utilities.getQueryString(params);
    }

    @Override
    public String getPrevLabel() {
        if (page > 0) {
            return "Previous";
        }
        return null;
    }

    @Override
    public String getNextLabel() {
        if (hasMoreItems()) {
            return "Next";
        }
        return null;
    }

    @Override
    public String getPrevLink() {
        // need to add letter param if it exists
        if (letter != null) {
            int prevPage = getPage() - 1;
            if (prevPage >= 0) {
                Map<String, String> params = new HashMap<>();
                params.put("page", "" + prevPage);
                params.put("letter", letter);
                return createURL(getUrl(), params);
            }
            return null;
        } else {
            if (page > 0) {
                int prevPage = page - 1;
                Map<String, String> params = new HashMap<>();
                params.put("page", "" + prevPage);
                return createURL(url, params);
            }
            return null;
        }
    }

    public List<Weblog> getItems() {

        if (weblogs == null) {
            // calculate offset
            int offset = getPage() * length;

            List<Weblog> results = new ArrayList<>();

            try {
                List<Weblog> rawWeblogs;
                if (letter == null) {
                    rawWeblogs = weblogManager.getWeblogs(Boolean.TRUE, offset, length + 1);
                } else {
                    rawWeblogs = weblogManager.getWeblogsByLetter(letter.charAt(0), offset, length + 1);
                }

                // wrap the results
                int count = 0;
                for (Weblog weblog : rawWeblogs) {
                    if (count++ < length) {
                        results.add(weblog);
                    } else {
                        more = true;
                    }
                }

            } catch (Exception e) {
                log.error("ERROR: fetching weblog list", e);
            }

            weblogs = results;
        }

        return weblogs;
    }

    private boolean hasMoreItems() {
        return more;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
