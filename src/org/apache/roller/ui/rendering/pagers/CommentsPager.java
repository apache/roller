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

package org.apache.roller.ui.rendering.pagers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;


/**
 * Paging through a collection of comments.
 */
public class CommentsPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(CommentsPager.class);
    
    private String locale = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // the collection for the pager
    private List comments = null;
    
    // are there more items?
    private boolean more = false;
    
    
    public CommentsPager(
            String         baseUrl,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(baseUrl, page);
        
        this.locale = locale;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public List getItems() {
        
        if (comments == null) {
            // calculate offset
            int offset = getPage() * length;
            
            List results = new ArrayList();
            
            Date startDate = null;
            if(sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                List entries = wmgr.getComments(
                        null, null, null, startDate, null,
                        CommentData.APPROVED, true, offset, length + 1);
                
                // check if there are more results for paging
                if(entries.size() > length) {
                    more = true;
                    entries.remove(entries.size() - 1);
                }
                
                // wrap the results
                for (Iterator it = entries.iterator(); it.hasNext();) {
                    CommentData comment = (CommentData) it.next();
                    results.add(CommentDataWrapper.wrap(comment));
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
    
}
