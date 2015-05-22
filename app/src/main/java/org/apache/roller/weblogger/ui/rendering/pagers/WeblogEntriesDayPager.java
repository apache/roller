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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TimeZone;
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
public class WeblogEntriesDayPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesDayPager.class);
    
    private SimpleDateFormat dayFormat = new SimpleDateFormat();
    
    private Date day;
    private Date nextDay;
    private Date prevDay;
    
    // collection for the pager
    private Map<Date, List<WeblogEntryWrapper>> entries = null;
    
    // are there more pages?
    private boolean more = false;
    
    
    public WeblogEntriesDayPager(
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

        TimeZone tz = weblog.getTimeZoneInstance();

        dayFormat = new SimpleDateFormat(
            messageUtils.getString("weblogEntriesPager.day.dateFormat"));
        dayFormat.setTimeZone(tz);
        
        getEntries();
        
        day = parseDate(dateString);

        Calendar cal = Calendar.getInstance(tz);
        
        cal.setTime(day);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        nextDay = cal.getTime();
        if (nextDay.after(getToday())) {
            nextDay = null;
        }
        
        cal.setTime(day);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        prevDay = cal.getTime();
        Date weblogInitialDate = weblog.getDateCreated() != null ? weblog.getDateCreated() : new Date(0);
        if (DateUtil.getEndOfDay(prevDay,cal).before(weblogInitialDate)) {
            prevDay = null;
        }
    }
    
    
    public Map<Date, List<WeblogEntryWrapper>> getEntries() {
        Date date = parseDate(dateString);
        Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
        Date startDate;
        Date endDate = date;
        startDate = DateUtil.getStartOfDay(endDate, cal);
        endDate = DateUtil.getEndOfDay(endDate, cal);
        
        if (entries == null) {
            entries = new TreeMap<Date, List<WeblogEntryWrapper>>(Collections.reverseOrder());
            try {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(weblog);
                wesc.setStartDate(startDate);
                wesc.setEndDate(endDate);
                wesc.setCatName(catName);
                wesc.setTags(tags);
                wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                wesc.setLocale(locale);
                wesc.setOffset(offset);
                wesc.setMaxResults(length+1);
                Map<Date, List<WeblogEntry>> mmap =
                        WebloggerFactory.getWeblogger().getWeblogEntryManager().getWeblogEntryObjectMap(wesc);

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
        return messageUtils.getString("weblogEntriesPager.day.home");
    }
    
    
    public String getNextLink() {
        if (more) {
            return createURL(page, 1, weblog, locale, pageLink, null, dateString, catName, tags);
        }
        return null;
    }
    
    
    public String getNextName() {
        if (getNextLink() != null) {
            return messageUtils.getString("weblogEntriesPager.day.next", new Object[] {dayFormat.format(day)});
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (page > 0) {
            return createURL(page, -1, weblog, locale, pageLink, null, dateString, catName, tags);
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (getPrevLink() != null) {
            return messageUtils.getString("weblogEntriesPager.day.prev", new Object[] {dayFormat.format(day)});
        }
        return null;
    }
    
    
    public String getNextCollectionLink() {
        if (nextDay != null) {
            String next = DateUtil.format8chars(nextDay, weblog.getTimeZoneInstance());
            return createURL(0, 0, weblog, locale, pageLink, null, next, catName, tags);
        }
        return null;
    }
    
    
    public String getNextCollectionName() {
        if (nextDay != null) {
            return messageUtils.getString("weblogEntriesPager.day.nextCollection", new Object[] {dayFormat.format(nextDay)});
        }
        return null;
    }
    
    
    public String getPrevCollectionLink() {
        if (prevDay != null) {
            String prev = DateUtil.format8chars(prevDay, weblog.getTimeZoneInstance());
            return createURL(0, 0, weblog, locale, pageLink, null, prev, catName, tags);
        }
        return null;
    }
    
    
    public String getPrevCollectionName() {
        if (prevDay != null) {
            return messageUtils.getString("weblogEntriesPager.day.prevCollection", new Object[] {dayFormat.format(prevDay)});
        }
        return null;
    }
    
}
