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

package org.apache.roller.ui.rendering.model;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.DateUtil;


/**
 * Pager for weblog entries, handles latest, single-entry, month and day views.
 * Collection returned is a list of lists of entries, where each list of 
 * entries represents one day.
 */
public class WeblogEntriesPagerImpl implements WeblogEntriesPager {
    
    /**
     * Behavior of the pager is detemined by the mode, which is itself a pager.
     * The mode may be LatestMode, SingleEntryMode, DayMode or MonthMode.
     */
    protected WeblogEntriesPager mode = null;    
    protected Map                entries = null;    
    protected WebsiteData        weblog = null;
    protected WeblogTemplate     weblogPage = null;
    protected WeblogCategoryData cat = null; 
    protected String             dateString = null; 
    protected WeblogEntryData    entry = null;
    protected String             locale = null;
    protected boolean            more = false;
    protected int                offset = 0;
    protected int                page = 0;
    protected int                length = 0;
        
    protected static Log log =
            LogFactory.getFactory().getInstance(WeblogEntriesPagerImpl.class); 
    
    public WeblogEntriesPagerImpl(
            WebsiteData        weblog, 
            WeblogTemplate     weblogPage,
            WeblogEntryData    entry,
            String             dateString, 
            WeblogCategoryData cat,
            String             locale,
            int                page) { 
        
        this.weblog = weblog;
        this.weblogPage = weblogPage;
        this.entry = entry;
        this.dateString = dateString;
        this.cat = cat;
        this.locale = locale;
        this.page = page;
        
        length = weblog.getEntryDisplayCount();
        if(page > 0) {
            this.page = page;
        }
        this.offset = length * page;
                
        // determine which mode to use
        if (entry != null) {
            mode = new SingleEntryMode();
        } else if (dateString != null && dateString.length() == 8) {
            mode = new DayMode();
        } else if (dateString != null && dateString.length() == 6) {
            mode = new MonthMode();
        } else {
            mode = new LatestMode();
        }
    }
        
    public Map getEntries() {
        return mode.getEntries();
    }
    
    public String getHomeLink() {
        return mode.getHomeLink();
    }

    public String getHomeName() {
        return mode.getHomeName();
    }

    public String getNextLink() {
        return mode.getNextLink();
    }

    public String getNextName() {
        return mode.getNextName();
    }

    public String getPrevLink() {
        return mode.getPrevLink();
    }

    public String getPrevName() {
        return mode.getPrevName();
    }

    public String getNextCollectionLink() {
        return mode.getNextCollectionLink();
    }

    public String getNextCollectionName() {
        return mode.getNextCollectionName();
    }

    public String getPrevCollectionLink() {
        return mode.getPrevCollectionLink();
    }

    public String getPrevCollectionName() {
        return mode.getPrevCollectionName();
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * We're paging through the latest entries in the blog.
     * In this mode there's no prev/next collection.
     */
    class LatestMode implements WeblogEntriesPager {
        
        public LatestMode() {
            LatestMode.this.getEntries();
        }
        
        public Map getEntries() {
            return getEntriesImpl(null, new Date());
        }

        public String getHomeLink() {
            return createURL(page, 0, weblog, weblogPage, null, cat, null, locale); 
        }
        
        public String getHomeName() {
            return "Main"; // TODO: I18N
        }
        
        public String getNextLink() {
            if (more) {
                return createURL(page, 1, weblog, weblogPage, entry, cat, dateString, locale);              
            }
            return null;
        }

        public String getNextName() {
            if (getNextLink() != null) {
                return "Next"; // TODO: I18N
            }
            return null;
        }

        public String getPrevLink() {
            if (page > 0) {
                return createURL(page, -1, weblog, weblogPage, entry, cat, dateString, locale); 
            }
            return null;
        }

        public String getPrevName() {
            if (getNextLink() != null) {
                return "Prev"; // TODO: I18N
            }
            return null;
        }

        public String getNextCollectionLink() {
            return null;
        }

        public String getNextCollectionName() {
            return null;
        }

        public String getPrevCollectionLink() {
            return null;
        }

        public String getPrevCollectionName() {
            return null;
        }
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * We're showing one weblog entry.
     * Next/prev return permalinks of next and previous weblog entries.
     * In this mode there's no prev/next collection.
     */
    class SingleEntryMode implements WeblogEntriesPager {
        String nextLink = null;
        WeblogEntryData entry = null;
        WeblogEntryData nextEntry = null;
        WeblogEntryData prevEntry = null;
        
        public SingleEntryMode() {
            SingleEntryMode.this.getEntries();
        }
        
        /**
         * Wrap entry up in map of lists.
         */
        public Map getEntries() {
            if (entries == null) try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                entry = wmgr.getWeblogEntryByAnchor(weblog, entry.getAnchor());
                if (entry == null || !entry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                    entry = null;
                } else {
                    entries = new TreeMap();
                    entries.put(new Date(entry.getPubTime().getTime()), 
                        Collections.singletonList(WeblogEntryDataWrapper.wrap(entry)));
                } 
            } catch (Exception e) {
                log.error("ERROR: fetching entry");
            }
            return entries;
        }

        public String getHomeLink() {
            return createURL(page, 1, weblog, weblogPage, null, cat, null, locale); 
        }
        
        public String getHomeName() {
            return "Main"; // TODO: I18N
        }
                
        public String getNextLink() {
            if (getNextEntry() != null) {
                return createURL(page, 0, weblog, weblogPage, getNextEntry(), cat, dateString, locale);
            }
            return null;
        }

        public String getNextName() {
            if (getNextEntry() != null) {
                return getNextEntry().getTitle();
            }
            return null;
        }

        public String getPrevLink() {
            if (getPrevEntry() != null) {
                return createURL(page, 0, weblog, weblogPage, getPrevEntry(), cat, dateString, locale); 
            }
            return null;
        }

        public String getPrevName() {
            if (getPrevEntry() != null) {
                return getPrevEntry().getTitle();
            }
            return null;
        }

        public String getNextCollectionLink() {
            return null;
        }

        public String getNextCollectionName() {
            return null;
        }

        public String getPrevCollectionLink() {
            return null;
        }

        public String getPrevCollectionName() {
            return null;
        }   
        
        private WeblogEntryData getNextEntry() {
            if (nextEntry == null) try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                nextEntry = wmgr.getNextEntry(entry, cat != null ? cat.getPath() : null);
                // make sure that entry is published and not to future
                if (nextEntry != null && nextEntry.getPubTime().after(new Date()) 
                    && nextEntry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                    nextEntry = null;
                }
            } catch (RollerException e) {
                log.error("ERROR: getting next entry", e);
            }
            return nextEntry;
        }
        
        private WeblogEntryData getPrevEntry() {
            if (prevEntry == null) try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                prevEntry = wmgr.getPreviousEntry(entry, cat != null ? cat.getPath() : null); 
                // make sure that entry is published and not to future
                if (prevEntry != null && prevEntry.getPubTime().after(new Date()) 
                    && prevEntry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                    prevEntry = null;
                }
            } catch (RollerException e) {
                log.error("ERROR: getting prev entry", e);
            }
            return prevEntry;
        }
    }
    
    //-------------------------------------------------------------------------

    /**
     * We're paging through entries in one day.
     * Next/prev methods return links to offsets within day's entries.
     * Next/prev collection methods return links to next and previous days.
     */
    class DayMode implements WeblogEntriesPager {
        private Date day;
        private Date nextDay;
        private Date prevDay;
        
        public DayMode() {
            DayMode.this.getEntries();
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
        }
        
        public Map getEntries() {
            Date date = parseDate(dateString);
            Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
            Date startDate = null;
            Date endDate = date;
            startDate = DateUtil.getStartOfDay(endDate, cal);
            endDate = DateUtil.getEndOfDay(endDate, cal);
            return getEntriesImpl(startDate, endDate);
        }

        public String getHomeLink() {
             return createURL(page, 0, weblog, weblogPage, null, cat, null, locale);
        }
        
        public String getHomeName() {
            return "Main"; // TODO: I18N
        }
        
        public String getNextLink() {
            if (more) {
                return createURL(page, 1, weblog, weblogPage, null, cat, dateString, locale);
            }
            return null;
        }

        public String getNextName() {
            if (getNextLink() != null) {
                return "Next"; // TODO: I18N
            }
            return null;
        }

        public String getPrevLink() {
            if (page > 0) {
                return createURL(page, -1, weblog, weblogPage, null, cat, dateString, locale);
            }
            return null;
        }

        public String getPrevName() {
            if (getNextLink() != null) {
                return "Prev"; // TODO: I18N
            }
            return null;
        }

        public String getNextCollectionLink() {
            if (nextDay != null) {
                String next = DateUtil.format8chars(nextDay);
                return createURL(page, 0, weblog, weblogPage, null, cat, next, locale);
            }
            return null;
        }

        public String getNextCollectionName() {
            if (nextDay != null) {
                return DateUtil.format8chars(nextDay);
            }
            return null;
        }

        public String getPrevCollectionLink() {
            if (prevDay != null) {
                String prev = DateUtil.format8chars(prevDay);
                return createURL(page, 0, weblog, weblogPage, null, cat, prev, locale);
            }
            return null;
        }

        public String getPrevCollectionName() {
            if (prevDay != null) {
                return DateUtil.format8chars(prevDay);
            }
            return null;
        }
    }
    
    //-------------------------------------------------------------------------

    /**
     * We're paging through entries within one month.
     * Next/prev methods return links to offsets within month's entries.
     * Next/prev collection methods return links to next and previous months.
     */
    class MonthMode implements WeblogEntriesPager {
        private Date month;
        private Date nextMonth;
        private Date prevMonth;
        
        public MonthMode() {
            MonthMode.this.getEntries();
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
        }
        
        public Map getEntries() {
            Date date = parseDate(dateString);
            Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            date = cal.getTime();
            Date startDate = DateUtil.getStartOfMonth(date, cal);;
            Date endDate = DateUtil.getEndOfMonth(date, cal);;
            return getEntriesImpl(startDate, endDate);
        }

        public String getHomeLink() {
            return createURL(page, 0, weblog, weblogPage, null, cat, null, locale);
        }
        
        public String getHomeName() {
            return "Main"; // TODO: I18N
        }
        
        public String getNextLink() {
            if (more) {
                 return createURL(page, 1, weblog, weblogPage, null, cat, dateString, locale);
            }
            return null;
        }

        public String getNextName() {
            if (getNextLink() != null) {
                return "Next"; // TODO: I18N
            }
            return null;
        }

        public String getPrevLink() {
            if (offset > 0) {
                int prevOffset = offset + length;
                prevOffset = (prevOffset < 0) ? 0 : prevOffset;
                return createURL(page, -1, weblog, weblogPage, null, cat, dateString, locale); 
            }
            return null;
        }

        public String getPrevName() {
            if (getNextLink() != null) {
                return "Prev"; // TODO: I18N
            }
            return null;
        }

        public String getNextCollectionLink() {
            if (nextMonth != null) {
                String next = DateUtil.format6chars(nextMonth); 
                return createURL(page, 0, weblog, weblogPage, null, cat, next, locale);
            }
            return null;
        }

        public String getNextCollectionName() {
            if (nextMonth != null) {
                return DateUtil.format6chars(nextMonth);
            }
            return null;
        }

        public String getPrevCollectionLink() {
            if (prevMonth != null) {
                String prev = DateUtil.format6chars(prevMonth);
                return createURL(page, 0, weblog, weblogPage, null, cat, prev, locale); 
            } 
            return null;
        }

        public String getPrevCollectionName() {
            if (prevMonth != null) {
                return DateUtil.format6chars(prevMonth);
            }
            return null;
        }        
    }  
                
    //------------------------------------------------------------------------
    
    /**
     * Get current values specified by request, a map of lists of entry  
     * wrappers, keyed by date objects, where each list holds entries 
     * for one day.
     */
    private Map getEntriesImpl(Date startDate, Date endDate) {
        if (entries == null) {
            entries = new TreeMap(new ReverseComparator());
            try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                Map mmap = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                        weblog,
                        startDate,
                        endDate,
                        cat != null ? cat.getPath() : null,
                        WeblogEntryData.PUBLISHED, 
                        locale,
                        offset,  
                        length + 1);
                              
                // need to wrap pojos
                int count = 0;
                java.util.Date key = null;
                Iterator days = mmap.keySet().iterator();
                while(days.hasNext()) {
                    key = (java.util.Date)days.next();

                    // now we need to go through each entry in a day and wrap
                    List wrapped = new ArrayList();
                    List unwrapped= (List) mmap.get(key);
                    for(int i=0; i < unwrapped.size(); i++) {
                        wrapped.add(i, 
                            WeblogEntryDataWrapper.wrap((WeblogEntryData)unwrapped.get(i)));
                        if (++count < length) {
                            entries.put(key, wrapped);
                        } else {
                            more = true;
                        }
                    }                            
                }
            } catch (Exception e) {
                log.error("ERROR: getting entry month map", e);
            }
        }
        return entries;
    }
    
    /** 
     * Parse data as either 6-char or 8-char format.
     */
    private Date parseDate(String dateString) {
        Date ret = null;
        SimpleDateFormat char8DateFormat = DateUtil.get8charDateFormat();
        SimpleDateFormat char6DateFormat = DateUtil.get6charDateFormat();
        if (   dateString!=null
                && dateString.length()==8
                && StringUtils.isNumeric(dateString) ) {
            ParsePosition pos = new ParsePosition(0);
            ret = char8DateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
        }
        if (   dateString!=null
                && dateString.length()==6
                && StringUtils.isNumeric(dateString) ) {
            ParsePosition pos = new ParsePosition(0);
            ret = char6DateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
        }
        return ret;
    }
    
    /**
     * Return today based on current blog's timezone/locale.
     */
    private Date getToday() {
        Calendar todayCal = Calendar.getInstance();
        todayCal = Calendar.getInstance(
            weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
        todayCal.setTime(new Date());
        return todayCal.getTime();
    }
    
    /**
     * Create URL that encodes pager state using most appropriate forms of URL.
     * @param pageAdd To be added to page number, or 0 for no page number
     */
    protected static String createURL(
            int                page, 
            int                pageAdd, 
            WebsiteData        website, 
            WeblogTemplate     weblogPage, 
            WeblogEntryData    entry, 
            WeblogCategoryData cat, 
            String             dateString, 
            String             locale) {
        
        Map params = new HashMap();
        if (pageAdd != 0) params.put("page", Integer.toString(page + pageAdd));

        StringBuffer pathinfo = new StringBuffer();
        pathinfo.append(website.getURL());
        if (locale != null) {
            pathinfo.append("/");
            pathinfo.append(locale);
        }
        if (weblogPage != null) {
            pathinfo.append("/page/");
            pathinfo.append(weblogPage.getLink());
            if (entry != null) params.put("entry", entry.getAnchor());
            if (dateString != null) params.put("date", dateString);
            if (cat != null) params.put("cat", cat.getPath());
        } 
        else if (entry != null) {
            pathinfo.append("/entry/");
            pathinfo.append(entry.getAnchor());
            if (dateString != null) params.put("date", dateString);
            if (cat != null) params.put("cat", cat.getPath());
        } 
        else if (cat != null && dateString == null) {
            pathinfo.append("/category");
            pathinfo.append(cat.getPath());                
        } 
        else if (dateString != null && cat == null) {
            pathinfo.append("/date/");
            pathinfo.append(dateString);                
        } 
        else {
            if (dateString != null) params.put("date", dateString);
            if (cat != null) params.put("cat", cat.getPath());
        }

        StringBuffer queryString = new StringBuffer();
        for (Iterator keys = params.keySet().iterator(); keys.hasNext();) {
            String key = (String) keys.next();
            String value = (String)params.get(key);
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            queryString.append(key);
            queryString.append("=");
            queryString.append(value);
        }
        return pathinfo.toString() + queryString.toString();
    }
} 
