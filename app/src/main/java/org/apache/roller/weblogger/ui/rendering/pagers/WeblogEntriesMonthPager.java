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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.business.URLStrategy;

public class WeblogEntriesMonthPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesMonthPager.class);
    
    private SimpleDateFormat monthFormat = new SimpleDateFormat();
    
    private Date month;
    private Date nextMonth;
    private Date prevMonth;
    
    // collection for the pager
    private Map<Date, List<WeblogEntry>> entries = null;
    
    // are there more pages?
    private boolean more = false;

    public WeblogEntriesMonthPager(
            WeblogEntryManager weblogEntryManager,
            URLStrategy        strat,
            Weblog             weblog,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catName,
            List<String>       tags,
            int                page) {
        
        super(weblogEntryManager, strat, weblog, pageLink, entryAnchor, dateString, catName, tags, page);

        TimeZone tz = weblog.getTimeZoneInstance();

        monthFormat = new SimpleDateFormat(
            messageUtils.getString("weblogEntriesPager.month.dateFormat"));
        monthFormat.setTimeZone(tz);
        
        getEntries();
        
        month = parseDate(dateString);
        
        Calendar cal = Calendar.getInstance(tz);
        
        // don't allow for paging into months in the future
        cal.setTime(month);
        cal.add(Calendar.MONTH, 1);
        nextMonth = cal.getTime();
        if (nextMonth.after(getToday())) {
            nextMonth = null;
        }

        // don't allow for paging into months before the blog's create date
        cal.setTime(month);
        cal.add(Calendar.MONTH, -1);
        prevMonth = cal.getTime();
        Date endOfPrevMonth = new Date(DateUtils.ceiling(cal, Calendar.MONTH).getTimeInMillis() - 1);
        Date weblogInitialDate = weblog.getDateCreated() != null ? weblog.getDateCreated() : new Date(0);
        if (endOfPrevMonth.before(weblogInitialDate)) {
            prevMonth = null;
        }
    }
    
    
    public Map<Date, List<WeblogEntry>> getEntries() {
        Date date = parseDate(dateString);
        Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
        cal.setTime(date);
        date = cal.getTime();
        Date startDate = DateUtils.truncate(date, Calendar.MONTH);
        Date endDate = new Date(DateUtils.ceiling(date, Calendar.MONTH).getTime() - 1);
        
        if (entries == null) {
            entries = new TreeMap<>(Collections.reverseOrder());
            try {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(weblog);
                wesc.setStartDate(startDate);
                wesc.setEndDate(endDate);
                wesc.setCatName(catName);
                wesc.setTags(tags);
                wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                wesc.setOffset(offset);
                wesc.setMaxResults(length+1);
                Map<Date, List<WeblogEntry>> mmap = weblogEntryManager.getWeblogEntryObjectMap(wesc);

                // need to wrap pojos
                int count = 0;
                for (Map.Entry<Date, List<WeblogEntry>> entry : mmap.entrySet()) {
                    // now we need to go through each entry in a day and wrap
                    List<WeblogEntry> wrapped = new ArrayList<>();
                    List<WeblogEntry> unwrapped = entry.getValue();
                    for (int i=0; i < unwrapped.size(); i++) {
                        if (count++ < length) {
                            wrapped.add(i, unwrapped.get(i).templateCopy());
                        } else {
                            more = true;
                        }
                    }
                    
                    // done with that day, put it in the map
                    if (wrapped.size() > 0) {
                        entries.put(entry.getKey(), wrapped);
                    }
                }
            } catch (Exception e) {
                log.error("ERROR: getting entry month map", e);
            }
        }
        return entries;
    }
    
    
    public String getHomeLink() {
        return createURL(0, 0, weblog, pageLink, null, null, catName, tags);
    }
    
    
    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager.month.home");
    }
    
    
    public String getNextLink() {
        if (more) {
            return createURL(page, 1, weblog, pageLink, null, dateString, catName, tags);
        }
        return null;
    }
    
    
    public String getNextName() {
        if (getNextLink() != null) {
            return messageUtils.getString("weblogEntriesPager.month.next", new Object[] {monthFormat.format(month)});
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (offset > 0) {
            return createURL(page, -1, weblog, pageLink, null, dateString, catName, tags);
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (getPrevLink() != null) {
            return messageUtils.getString("weblogEntriesPager.month.prev", new Object[] {monthFormat.format(month)});
        }
        return null;
    }
    
    
    public String getNextCollectionLink() {
        if (nextMonth != null) {
            String next = FastDateFormat.getInstance(WebloggerCommon.FORMAT_6CHARS, weblog.getTimeZoneInstance()).format(nextMonth);
            return createURL(0, 0, weblog, pageLink, null, next, catName, tags);
        }
        return null;
    }
    
    
    public String getNextCollectionName() {
        if (nextMonth != null) {
            return messageUtils.getString("weblogEntriesPager.month.nextCollection", new Object[] {monthFormat.format(nextMonth)});
        }
        return null;
    }
    
    
    public String getPrevCollectionLink() {
        if (prevMonth != null) {
            String prev = FastDateFormat.getInstance(WebloggerCommon.FORMAT_6CHARS, weblog.getTimeZoneInstance()).format(prevMonth);
            return createURL(0, 0, weblog, pageLink, null, prev, catName, tags);
        }
        return null;
    }
    
    
    public String getPrevCollectionName() {
        if (prevMonth != null) {
            return messageUtils.getString("weblogEntriesPager.month.prevCollection", new Object[] {monthFormat.format(prevMonth)});
        }
        return null;
    }
    
}
