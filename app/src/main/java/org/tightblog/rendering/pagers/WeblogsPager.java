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

import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Weblog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paging through a collection of weblogs.
 */
public class WeblogsPager extends AbstractPager {

    private static Logger log = LoggerFactory.getLogger(WeblogsPager.class);

    private String letter = null;
    private int length = 0;

    // collection for the pager
    private List<Weblog> weblogs;

    // are there more items?
    private boolean more = false;

    private WeblogManager weblogManager;

    public WeblogsPager(
            WeblogManager weblogManager,
            URLStrategy strat,
            String baseUrl,
            String letter,
            int page,
            int length) {

        super(strat, baseUrl, page);

        this.weblogManager = weblogManager;
        this.letter = letter;
        this.length = length;

        // initialize the collection
        getItems();
    }

    public String getNextLink() {
        // need to add letter param if it exists
        if (letter != null) {
            int page = getPage() + 1;
            if (hasMoreItems()) {
                Map<String, String> params = new HashMap<>();
                params.put("page", "" + page);
                params.put("letter", letter);
                return createURL(getUrl(), params);
            }
            return null;
        } else {
            return super.getNextLink();
        }
    }

    public String getPrevLink() {
        // need to add letter param if it exists
        if (letter != null) {
            int page = getPage() - 1;
            if (page >= 0) {
                Map<String, String> params = new HashMap<>();
                params.put("page", "" + page);
                params.put("letter", letter);
                return createURL(getUrl(), params);
            }
            return null;
        } else {
            return super.getPrevLink();
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

    public boolean hasMoreItems() {
        return more;
    }

}
