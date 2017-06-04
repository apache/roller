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
package org.tightblog.rendering.pagers;

import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntryComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Paging through a collection of comments.
 */
public class CommentsPager extends AbstractPager {

    private static Logger log = LoggerFactory.getLogger(CommentsPager.class);

    private Weblog weblog = null;
    private int sinceDays = -1;
    private int length = 0;
    private String categoryName = null;

    // the collection for the pager
    private List<WeblogEntryComment> comments = null;

    // are there more items?
    private boolean more = false;

    // most recent update time of current set of entries
    private Instant lastUpdated = null;

    private WeblogEntryManager weblogEntryManager;

    public CommentsPager(
            WeblogEntryManager weblogEntryManager,
            String baseUrl,
            Weblog weblog,
            String categoryName,
            int sinceDays,
            int page,
            int length) {

        super(baseUrl, page);

        this.weblogEntryManager = weblogEntryManager;
        this.weblog = weblog;
        this.categoryName = categoryName;
        this.sinceDays = sinceDays;
        this.length = length;

        // initialize the collection
        getItems();
    }

    public List<WeblogEntryComment> getItems() {

        if (comments == null) {
            // calculate offset
            int offset = getPage() * length;

            List<WeblogEntryComment> results = new ArrayList<>();

            LocalDateTime startDate = null;
            if (sinceDays > 0) {
                startDate = LocalDateTime.now().minusDays(sinceDays);
            }

            try {
                CommentSearchCriteria csc = new CommentSearchCriteria();
                csc.setWeblog(weblog);
                csc.setCategoryName(categoryName);
                if (startDate != null) {
                    csc.setStartDate(startDate.atZone(ZoneId.systemDefault()).toInstant());
                }
                csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
                csc.setOffset(offset);
                csc.setMaxResults(length + 1);

                List<WeblogEntryComment> commentsList = weblogEntryManager.getComments(csc);

                // wrap the results
                int count = 0;
                for (WeblogEntryComment comment : commentsList) {
                    if (count++ < length) {
                        results.add(comment);
                    } else {
                        more = true;
                    }
                }

            } catch (Exception e) {
                log.error("ERROR: fetching comment list", e);
            }

            comments = results;
        }

        return comments;
    }

    public boolean hasMoreItems() {
        return more;
    }

    /**
     * Get last updated time from items in pager
     */
    @SuppressWarnings("unused")
    public Instant getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by post time, so use that
            List<WeblogEntryComment> items = getItems();
            if (items != null && items.size() > 0) {
                lastUpdated = items.get(0).getPostTime();
            } else {
                // no update so we assume it's brand new
                lastUpdated = Instant.now();
            }
        }
        return lastUpdated;
    }
}

