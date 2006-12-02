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

package org.apache.roller.ui.core.tags.calendar;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.URLUtilities;


/**
 * Calendar model for calendar intended for use on view-weblog page.
 */
public class WeblogCalendarModel implements CalendarModel {
    
    private static Log log = LogFactory.getLog(WeblogCalendarModel.class);
    
    protected Map                 monthMap;
    protected Date                day;
    protected String              cat = null;
    protected String              pageLink = null;
    protected String              locale = null;
    protected Calendar            calendar = null;
    protected WebsiteData         weblog = null;
    
    protected WeblogPageRequest pageRequest = null;
    
    
    public WeblogCalendarModel(WeblogPageRequest pRequest, String catArgument) {
        
        this.pageRequest = pRequest;
        try {
            this.weblog = pageRequest.getWeblog();
            
            pageLink = pageRequest.getWeblogPageName();
            
            day = DateUtil.parseWeblogURLDateString(pageRequest.getWeblogDate(),
                    weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
            initDay(day);
            
            locale = pageRequest.getLocale();
            
            if(weblog == null) {
                throw new RollerException("unable to lookup weblog: "+
                        pageRequest.getWeblogHandle());
            }
            // Category method argument overrides category from URL
            if (catArgument != null) {
                cat = catArgument;
            } else if (pageRequest.getWeblogCategoryName() != null) {
                cat = pageRequest.getWeblogCategoryName();
            }
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
        
        // Compute first second of month
        cal.setTime(month);
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        Date startDate = cal.getTime();
        
        // Compute last second of month
        cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        Date endDate = cal.getTime();
        
        // Fix for ROL-840 Don't include future entries
        Date now = new Date();
        if (endDate.after(now)) endDate = now;
        
        loadWeblogEntries(startDate, endDate, cat);
    }
    
    protected void loadWeblogEntries(Date startDate, Date endDate, String catName) {
        try {
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            monthMap = mgr.getWeblogEntryStringMap(
                    weblog,                  // website
                    startDate,                 // startDate
                    endDate,                   // endDate
                    catName,                   // cat
                    null,                      // tags
                    WeblogEntryData.PUBLISHED, // status
                    locale,
                    0, -1);
        } catch (RollerException e) {
            log.error(e);
            monthMap = new HashMap();
        }
    }
    
    public void setDay(String month) throws Exception {
        SimpleDateFormat fmt = DateUtil.get8charDateFormat();
        ParsePosition pos = new ParsePosition(0);
        initDay( fmt.parse( month, pos ) );
    }
    
    public Date getDay() {
        return (Date)day.clone();
    }
    
    public String getParameterValue(Date day) {
        return (String)monthMap.get( day );
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
        String dateString = (String)monthMap.get(day);
        if (dateString == null && !alwaysURL) return null;
        else if (dateString == null && !monthURL) {
            dateString = DateUtil.format8chars(day);
        } else if (dateString == null && monthURL) {
            dateString = DateUtil.format6chars(day);
        }
        try {
            if (pageLink == null) { // create date URL
                url = URLUtilities.getWeblogCollectionURL(weblog, locale, cat, dateString, null, -1, false);
            } else { // create page URL
                url = URLUtilities.getWeblogPageURL(weblog, locale, pageLink, null, cat, dateString, null, -1, false);
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
        Calendar nextCal = getCalendar();
        nextCal.setTime( day );
        nextCal.add( Calendar.MONTH, 1 );
        return getFirstDayOfMonth(nextCal).getTime();
    }

    public Date getPrevMonth() {
        Calendar prevCal = getCalendar();
        prevCal.setTime( day );
        prevCal.add( Calendar.MONTH, -1 );
        return getFirstDayOfMonth(prevCal).getTime();
    }
    
    protected Calendar getFirstDayOfMonth(Calendar cal) {
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        return cal;
    }
    
    protected Calendar getLastDayOfMonth(Calendar cal) {
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return cal;
    }
    
    public String computeNextMonthUrl() {
        // Create yyyyMMdd dates for next month, prev month and today
        Calendar nextCal = getCalendar();
        nextCal.setTime( day );
        nextCal.add( Calendar.MONTH, 1 );
        String nextMonth = computeUrl(nextCal.getTime(), true, true);
        
        // and strip off last two digits to get a month URL
        return nextMonth;
    }

    public Date getInitialMonth() {
        Calendar cal = getCalendar();
        // if there is no dateCreated value, default to beginning of epoch.
        cal.setTime(weblog.getDateCreated() != null ? weblog.getDateCreated() : new Date(0));
        return getFirstDayOfMonth(cal).getTime();
    }

    public String computePrevMonthUrl() {
        // Create yyyyMMdd dates for prev month, prev month and today
        Calendar prevCal = getCalendar();
        prevCal.setTime(day);
        prevCal.add(Calendar.MONTH, -1);
        //getLastDayOfMonth( prevCal );
        String prevMonth = computeUrl(prevCal.getTime(), true, true);
        
        // and strip off last two digits to get a month URL
        return prevMonth;
    }
    
    public String computeTodayMonthUrl() {
        return URLUtilities.getWeblogCollectionURL(weblog, locale, cat, null, null, -1, false);
    }
    
}
