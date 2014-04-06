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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryCommentWrapper;


/**
 * Paging through a collection of comments.
 */
public class CommentsPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(CommentsPager.class);
    
    private Weblog weblog = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // the collection for the pager
    private List<WeblogEntryCommentWrapper> comments = null;
    
    // are there more items?
    private boolean more = false;
    
    // most recent update time of current set of entries
    private Date lastUpdated = null;        
    
    public CommentsPager(
            URLStrategy    strat,
            String         baseUrl,
            Weblog         weblog,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);
        
        this.weblog = weblog;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public List<WeblogEntryCommentWrapper> getItems() {
        
        if (comments == null) {
            // calculate offset
            int offset = getPage() * length;
            
            List<WeblogEntryCommentWrapper> results = new ArrayList<WeblogEntryCommentWrapper>();
            
            Date startDate = null;
            if(sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                WeblogEntryManager wmgr = roller.getWeblogEntryManager();

                CommentSearchCriteria csc = new CommentSearchCriteria();
                csc.setWeblog(weblog);
                csc.setStartDate(startDate);
                csc.setStatus(WeblogEntryComment.APPROVED);
                csc.setReverseChrono(true);
                csc.setOffset(offset);
                csc.setMaxResults(length + 1);

                List<WeblogEntryComment> comments = wmgr.getComments(csc);
                
                // wrap the results
                int count = 0;
                for (WeblogEntryComment comment : comments) {
                    if (count++ < length) {
                        results.add(WeblogEntryCommentWrapper.wrap(comment, urlStrategy));
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
    
    /** Get last updated time from items in pager */
    public Date getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by pubtime, so first might not be last updated
            List<WeblogEntryCommentWrapper> items = getItems();
            if (getItems() != null && getItems().size() > 0) {
                Timestamp newest = (getItems().get(0)).getPostTime();
                for (WeblogEntryCommentWrapper c : items) {
                    if (c.getPostTime().after(newest)) {
                        newest = c.getPostTime();
                    }
                }
                lastUpdated = new Date(newest.getTime());
            } else {
                // no update so we assume it's brand new
                lastUpdated = new Date();
            }
        }
        return lastUpdated;
    }
}

