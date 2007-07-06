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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
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
    private Map entries = null;
    
    // are there more pages?
    private boolean more = false;
    
    
    public WeblogEntriesDayPager(
            URLStrategy        strat,
            Weblog             weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath,
            List               tags,
            int                page) {
        
        super(strat, weblog, locale, pageLink, entryAnchor, dateString, catPath, tags, page);
        
        dayFormat = new SimpleDateFormat(
            messageUtils.getString("weblogEntriesPager.day.dateFormat"));
        
        getEntries();
        
        day = parseDate(dateString);
        
        Calendar cal = Calendar.getInstance();
        
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
    
    
    public Map getEntries() {
        Date date = parseDate(dateString);
        Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
        Date startDate = null;
        Date endDate = date;
        startDate = DateUtil.getStartOfDay(endDate, cal);
        endDate = DateUtil.getEndOfDay(endDate, cal);
        
        if (entries == null) {
            entries = new TreeMap(new ReverseComparator());
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                WeblogManager wmgr = roller.getWeblogManager();
                Map mmap = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogEntryObjectMap(
                        
                        weblog,
                        startDate,
                        endDate,
                        catPath,
                        tags,WeblogEntry.PUBLISHED, 
                        locale,
                        offset,  
                        length + 1);
                
                // need to wrap pojos
                int count = 0;
                Date key = null;
                Iterator days = mmap.keySet().iterator();
                while(days.hasNext()) {
                    key = (Date) days.next();
                    
                    // now we need to go through each entry in a day and wrap
                    List wrapped = new ArrayList();
                    List unwrapped = (List) mmap.get(key);
                    for(int i=0; i < unwrapped.size(); i++) {
                        if (count++ < length) {
                            wrapped.add(i,WeblogEntryWrapper.wrap((WeblogEntry)unwrapped.get(i), urlStrategy));
                        } else {
                            more = true;
                        }
                    }
                    
                    // done with that day, put it in the map
                    if(wrapped.size() > 0) {
                        entries.put(key, wrapped);
                    }
                }
                
                
            } catch (Exception e) {
                log.error("ERROR: getting entry month map", e);
            }
        }
        return entries;
    }
    
    
    public String getHomeLink() {
        return createURL(0, 0, weblog, locale, pageLink, null, null, catPath, tags);
    }
    
    
    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager.day.home");
    }
    
    
    public String getNextLink() {
        if (more) {
            return createURL(page, 1, weblog, locale, pageLink, null, dateString, catPath, tags);
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
            return createURL(page, -1, weblog, locale, pageLink, null, dateString, catPath, tags);
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
            String next = DateUtil.format8chars(nextDay);
            return createURL(0, 0, weblog, locale, pageLink, null, next, catPath, tags);
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
            String prev = DateUtil.format8chars(prevDay);
            return createURL(0, 0, weblog, locale, pageLink, null, prev, catPath, tags);
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
