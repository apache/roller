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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.wrapper.UserWrapper;


/**
 * Paging through a collection of users.
 */
public class UsersPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(UsersPager.class);
    
    private String letter = null;
    private String locale = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // collection for the pager
    private List users;
    
    // are there more items?
    private boolean more = false;
    
    
    public UsersPager(
            URLStrategy    strat,
            String         baseUrl,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);
        
        this.locale = locale;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public UsersPager(
            URLStrategy    strat,
            String         baseUrl,
            String         letter,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);
        
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
        
        if (users == null) {
            // calculate offset
            int offset = getPage() * length;
            
            List results = new ArrayList();
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                UserManager umgr = roller.getUserManager();
                List rawUsers = null;
                if (letter == null) {
                    rawUsers = umgr.getUsers(Boolean.TRUE, null, null, offset, length + 1);
                } else {
                    rawUsers = umgr.getUsersByLetter(letter.charAt(0), offset, length + 1);
                }
                
                // wrap the results
                int count = 0;
                for (Iterator it = rawUsers.iterator(); it.hasNext();) {
                    User user = (User) it.next();
                    if (count++ < length) {
                        results.add(UserWrapper.wrap(user));
                    } else {
                        more = true;
                    }
                }
                
            } catch (Exception e) {
                log.error("ERROR: fetching user list", e);
            }
            
            users = results;
        }
        
        return users;
    }
    
    
    public boolean hasMoreItems() {
        return more;
    }
    
}
