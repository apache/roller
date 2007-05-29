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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;


/**
 * Paging through a collection of weblogs.
 */
public class WeblogsPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(WeblogsPager.class);
    
    private String letter = null;
    private String locale = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // collection for the pager
    private List weblogs;
    
    // are there more items?
    private boolean more = false;
    
    
    public WeblogsPager(
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
    
    
    public WeblogsPager(
            String         baseUrl,
            String         letter,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(baseUrl, page);
        
        this.letter = letter;
        this.locale = locale;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public String getNextLink() {
        // need to add letter param if it exists
        if(letter != null) {
            int page = getPage() + 1;
            if(hasMoreItems()) {
                Map params = new HashMap();
                params.put("page", ""+page);
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
        if(letter != null) {
            int page = getPage() - 1;
            if (page >= 0) {
                Map params = new HashMap();
                params.put("page", ""+page);
                params.put("letter", letter);
                return createURL(getUrl(), params);
            }
            return null;
        } else {
            return super.getPrevLink();
        }
    }
    
    
    public List getItems() {
        
        if (weblogs == null) {
            // calculate offset
            int offset = getPage() * length;
            
            List results = new ArrayList();
            Date startDate = null;
            if (sinceDays != -1) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            try {
                Roller roller = RollerFactory.getRoller();
                UserManager umgr = roller.getUserManager();
                List rawWeblogs = null;
                if (letter == null) {
                    rawWeblogs = umgr.getWebsites(null, Boolean.TRUE, Boolean.TRUE, startDate, null, offset, length + 1);
                } else {
                    rawWeblogs = umgr.getWeblogsByLetter(letter.charAt(0), offset, length + 1);
                }
                // Collections returned by backend are not writeable, so create copy
                List weblogs = new ArrayList();
                weblogs.addAll(rawWeblogs);
                
                // check if there are more results for paging
                if(weblogs.size() > length) {
                    more = true;
                    weblogs.remove(weblogs.size() - 1);
                }
                
                // wrap the results
                for (Iterator it = weblogs.iterator(); it.hasNext();) {
                    Weblog website = (Weblog) it.next();
                    results.add(WebsiteDataWrapper.wrap(website));
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
