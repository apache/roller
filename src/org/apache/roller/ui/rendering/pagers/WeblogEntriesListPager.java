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
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;

/**
 * Simple pager for list of weblog entries.
 */
public class WeblogEntriesListPager extends AbstractPager {
    private WebsiteData queryWeblog;
    private UserData    queryUser;
    private String      queryCat;
    private List        entries;    
    protected static Log log =
            LogFactory.getFactory().getInstance(WeblogEntriesListPager.class);
    
    public WeblogEntriesListPager(            
            WebsiteData    weblog,
            WebsiteData    queryWeblog,
            UserData       queryUser,
            String         queryCat,
            Template       weblogPage,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        super(weblog, weblogPage, locale, sinceDays, page, length);
        this.queryWeblog = queryWeblog;
        this.queryUser = queryUser;
        this.queryCat = queryCat;
        getItems();
    }
    
    public List getItems() {
        if (entries == null) {
            List results = new ArrayList();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);
            Date startDate = cal.getTime();
            try {            
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                UserManager umgr = roller.getUserManager();
                List rawEntries = wmgr.getWeblogEntries( 
                    queryWeblog, 
                    queryUser, 
                    startDate, 
                    new Date(), 
                    queryCat, 
                    WeblogEntryData.PUBLISHED, 
                    "pubTime", 
                    locale, 
                    offset, 
                    length + 1);
                int count = 0;
                for (Iterator it = rawEntries.iterator(); it.hasNext();) {
                    WeblogEntryData entry = (WeblogEntryData) it.next();
                    if (count++ < length) {
                        results.add(WeblogEntryDataWrapper.wrap(entry));
                    } else {
                        more = true;
                    }                      
                }
            } catch (Exception e) {
                log.error("ERROR: fetching weblog entries list", e);
            }
            entries = results;
        }
        return entries;
    }   
}
