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

package org.apache.roller.weblogger.ui.core.tags.calendar;

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;


/**
 * Calendar model for calendar intended for use on view-weblog page.
 */
public class WeblogCalendarModel implements CalendarModel {
    
    private static Log log = LogFactory.getLog(WeblogCalendarModel.class);
    
    protected Map               monthMap;
    protected Date              day;
    protected String            cat = null;
    protected String            pageLink = null;
    protected String            locale = null;
    protected Calendar          calendar = null;
    protected Weblog            weblog = null;
    protected Date              prevMonth = null;
    protected Date              nextMonth = null;
    protected WeblogPageRequest pageRequest = null;
    
    
    public WeblogCalendarModel(WeblogPageRequest pRequest, String catArgument) {
        
        this.pageRequest = pRequest;
        try {
            this.weblog = pageRequest.getWeblog();            
            if(weblog == null) {
                throw new WebloggerException("unable to lookup weblog: "+
                        pageRequest.getWeblogHandle());
            }
            pageLink = pageRequest.getWeblogPageName();            
            day = parseWeblogURLDateString(pageRequest.getWeblogDate(),
                  weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
            locale = pageRequest.getLocale();
            
            // Category method argument overrides category from URL
            if (catArgument != null) {
                cat = catArgument;
            } else if (pageRequest.getWeblogCategoryName() != null) {
                cat = pageRequest.getWeblogCategoryName();
            }
            
            initDay(day);

        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("ERROR: initializing calendar", e);
        }
        
    }
    
    
    protected void initDay(Date month) {
        calendar = Calendar.getInstance(
                weblog.getTimeZoneInstance(),
                weblog.getLocaleInstance());
        
        Calendar cal = (Calendar)calendar.clone();
        cal.setTime(month);
        Date startDate = DateUtils.truncate(cal, Calendar.MONTH).getTime();
        Date endDate = new Date(DateUtils.ceiling(cal, Calendar.MONTH).getTimeInMillis() - 1);

        // Determine previous non-empty month
        // Get entries before startDate, using category restriction limit 1
        // Use entry's date as previous month
        try {
            WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            // since we need an entry.pubTime < startDate, but the method uses endDate
            wesc.setEndDate(new Date(startDate.getTime()-1));
            wesc.setCatName(cat);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setSortOrder(WeblogEntrySearchCriteria.SortOrder.DESCENDING);
            wesc.setLocale(locale);
            wesc.setMaxResults(1);
            List prevEntries = mgr.getWeblogEntries(wesc);

            if (prevEntries.size() > 0) {
                WeblogEntry prevEntry = (WeblogEntry)prevEntries.get(0);
                Calendar calPrev = getCalendar();
                calPrev.setTime(new Date(prevEntry.getPubTime().getTime()));
                prevMonth = DateUtils.truncate(calPrev, Calendar.MONTH).getTime();
            }
        } catch (WebloggerException e) {
            log.error("ERROR determining previous non-empty month");
        }
        
        // Determine next non-empty month
        // Get entries after endDate, using category restriction limit 1
        // Use entry's date as next month
        try {
            WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            // since we need an entry.pubTime > endDate, but the method uses startDate
            wesc.setStartDate(new Date(endDate.getTime()+1));
            wesc.setCatName(cat);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setSortOrder(WeblogEntrySearchCriteria.SortOrder.ASCENDING);
            wesc.setLocale(locale);
            wesc.setMaxResults(1);
            List nextEntries = mgr.getWeblogEntries(wesc);
            if (nextEntries.size() > 0) {
                WeblogEntry nextEntry = (WeblogEntry)nextEntries.get(0);
                Calendar calNext = getCalendar();
                calNext.setTime(new Date(nextEntry.getPubTime().getTime()));
                nextMonth = DateUtils.truncate(calNext, Calendar.MONTH).getTime();
            }
        } catch (WebloggerException e) {
            log.error("ERROR determining next non-empty month");
        }  
        
        // Don't include future entries
        Date now = new Date();
        if (endDate.after(now)) {
        	endDate = now;
        	nextMonth = null;
        }
        
        loadWeblogEntries(startDate, endDate, cat);
    }
    
    protected void loadWeblogEntries(Date startDate, Date endDate, String catName) {
        try {
            WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setStartDate(startDate);
            wesc.setEndDate(endDate);
            wesc.setCatName(catName);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setLocale(locale);
            monthMap = mgr.getWeblogEntryStringMap(wesc);
        } catch (WebloggerException e) {
            log.error(e);
            monthMap = new HashMap<>();
        }
    }

    public void setDay(String month) throws Exception {
        FastDateFormat fmt = FastDateFormat.getInstance(RollerConstants.FORMAT_8CHARS, getCalendar().getTimeZone());
        ParsePosition pos = new ParsePosition(0);
        initDay( fmt.parse( month, pos ) );
    }
    
    public Date getDay() {
        return (Date)day.clone();
    }
    
    /**
     * Parse data as either 6-char or 8-char format.
     */
    public static Date parseWeblogURLDateString(String dateString, TimeZone tz, Locale locale) {
        
        Date ret = new Date();
        Calendar cal = Calendar.getInstance(tz,locale);
        
        if (dateString != null
                && dateString.length()==8
                && StringUtils.isNumeric(dateString) ) {
            FastDateFormat char8DateFormat = FastDateFormat.getInstance(RollerConstants.FORMAT_8CHARS, cal.getTimeZone());
            ParsePosition pos = new ParsePosition(0);
            ret = char8DateFormat.parse(dateString, pos);

            // make sure the requested date is not in the future
            // Date is always ms offset from epoch in UTC, by no means of timezone.
            Date today = new Date();
            if(ret.after(today)) {
                ret = today;
            }
            
        } else if(dateString != null
                && dateString.length()==6
                && StringUtils.isNumeric(dateString)) {
            FastDateFormat char6DateFormat = FastDateFormat.getInstance(RollerConstants.FORMAT_6CHARS, cal.getTimeZone());
            ParsePosition pos = new ParsePosition(0);
            ret = char6DateFormat.parse(dateString, pos);
            
            // make sure the requested date is not in the future
            Date today = new Date();
            if(ret.after(today)) {
                ret = today;
            }
        }
        
        return ret;
    }
    
    /**
     * Create URL for use on view-weblog page
     * @param day       Day for URL or null if no entries on that day
     * @param alwaysURL Always return a URL, never return null
     * @return          URL for day, or null if no weblog entry on that day
     */
    public String computeUrl(Date day, boolean monthURL, boolean alwaysURL) {
        String url = null;
        // get the 8 char YYYYMMDD datestring for day
        String dateString = (String) monthMap.get(day);
        if (dateString == null && !alwaysURL) {
            return null;
        }
        else if (dateString == null && !monthURL) {
            dateString = FastDateFormat.getInstance(RollerConstants.FORMAT_8CHARS, getCalendar().getTimeZone()).format(day);
        } else if (dateString == null) {
            dateString = FastDateFormat.getInstance(RollerConstants.FORMAT_6CHARS, getCalendar().getTimeZone()).format(day);
        }
        try {
            if (pageLink == null) {
                // create date URL
                url = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogCollectionURL(weblog, locale, cat, dateString, null, -1, false);
            } else {
                // create page URL
                url = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogPageURL(weblog, locale, pageLink, null, cat, dateString, null, -1, false);
            }
        } catch (Exception e) {
            log.error("ERROR: creating URL",e);
        }
        return url;
    }
    
    public String getContent(Date day) {
        return null;
    }
    
    public Calendar getCalendar() {
        return (Calendar)calendar.clone();
    }
    
    public Date getNextMonth() {
        return nextMonth;
    }

    public Date getPrevMonth() {
        return prevMonth;
    }
       
    public String computeNextMonthUrl() {
        return computeUrl(nextMonth, true, true);
    }

    public String computePrevMonthUrl() {
        return computeUrl(prevMonth, true, true);
    }
    
    public String computeTodayMonthUrl() {
    	String url;
        if (pageLink == null) {
            // create default URL
            url = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogCollectionURL(weblog, locale, cat, null, null, -1, false);
        } else {
            // create page URL
            url = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogPageURL(weblog, locale, pageLink, null, cat, null, null, -1, false);
        }
    	return url;
    }
    
}
