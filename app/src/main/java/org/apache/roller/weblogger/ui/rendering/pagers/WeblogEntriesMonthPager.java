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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.business.URLStrategy;


/**
 *
 */
public class WeblogEntriesMonthPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesMonthPager.class);
    
    private SimpleDateFormat monthFormat = new SimpleDateFormat();
    
    private Date month;
    private Date nextMonth;
    private Date prevMonth;
    
    // collection for the pager
    private Map<Date, List<WeblogEntryWrapper>> entries = null;
    
    // are there more pages?
    private boolean more = false;
    
    
    public WeblogEntriesMonthPager(
            URLStrategy        strat,
            Weblog             weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catName,
            List               tags,
            int                page) {
        
        super(strat, weblog, locale, pageLink, entryAnchor, dateString, catName, tags, page);
        
        monthFormat = new SimpleDateFormat(
            messageUtils.getString("weblogEntriesPager.month.dateFormat"));
        
        getEntries();
        
        month = parseDate(dateString);
        
        Calendar cal = Calendar.getInstance();
        
        cal.setTime(month);
        cal.add(Calendar.MONTH, 1);
        nextMonth = cal.getTime();
        if (nextMonth.after(getToday())) {
            nextMonth = null;
        }
        
        cal.setTime(month);
        cal.add(Calendar.MONTH, -1);
        prevMonth = cal.getTime();
        Date endOfPrevMonth = DateUtil.getEndOfMonth(prevMonth,cal) ;
        Date weblogInitialDate = weblog.getDateCreated() != null ? weblog.getDateCreated() : new Date(0);
        if (endOfPrevMonth.before(weblogInitialDate)) {
            prevMonth = null;
        }
    }
    
    
    public Map<Date, List<WeblogEntryWrapper>> getEntries() {
        Date date = parseDate(dateString);
        Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        date = cal.getTime();
        Date startDate = DateUtil.getStartOfMonth(date, cal);
        Date endDate = DateUtil.getEndOfMonth(date, cal);
        
        if (entries == null) {
            entries = new TreeMap<Date, List<WeblogEntryWrapper>>(new ReverseComparator());
            try {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(weblog);
                wesc.setStartDate(startDate);
                wesc.setEndDate(endDate);
                wesc.setCatName(catName);
                wesc.setTags(tags);
                wesc.setStatus(WeblogEntry.PUBLISHED);
                wesc.setLocale(locale);
                wesc.setOffset(offset);
                wesc.setMaxResults(length+1);
                Map<Date, List<WeblogEntry>> mmap = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager().getWeblogEntryObjectMap(wesc);

                // need to wrap pojos
                int count = 0;
                for (Map.Entry<Date, List<WeblogEntry>> entry : mmap.entrySet()) {
                    // now we need to go through each entry in a day and wrap
                    List<WeblogEntryWrapper> wrapped = new ArrayList<WeblogEntryWrapper>();
                    List<WeblogEntry> unwrapped = entry.getValue();
                    for (int i=0; i < unwrapped.size(); i++) {
                        if (count++ < length) {
                            wrapped.add(i,WeblogEntryWrapper.wrap(unwrapped.get(i), urlStrategy));
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
        return createURL(0, 0, weblog, locale, pageLink, null, null, catName, tags);
    }
    
    
    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager.month.home");
    }
    
    
    public String getNextLink() {
        if (more) {
            return createURL(page, 1, weblog, locale, pageLink, null, dateString, catName, tags);
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
            return createURL(page, -1, weblog, locale, pageLink, null, dateString, catName, tags);
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
            String next = DateUtil.format6chars(nextMonth);
            return createURL(0, 0, weblog, locale, pageLink, null, next, catName, tags);
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
            String prev = DateUtil.format6chars(prevMonth);
            return createURL(0, 0, weblog, locale, pageLink, null, prev, catName, tags);
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
