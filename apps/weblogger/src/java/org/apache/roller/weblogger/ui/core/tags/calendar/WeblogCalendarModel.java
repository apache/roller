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

package org.apache.roller.weblogger.ui.core.tags.calendar;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.util.DateUtil;
import org.apache.roller.weblogger.util.URLUtilities;


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
    protected Weblog       weblog = null;
    protected Date              prevMonth = null; // prev month or null if none
    protected Date              nextMonth = null; // next month or null if none    
    protected WeblogPageRequest pageRequest = null;
    
    
    public WeblogCalendarModel(WeblogPageRequest pRequest, String catArgument) {
        
        this.pageRequest = pRequest;
        try {
            this.weblog = pageRequest.getWeblog();            
            if(weblog == null) {
                throw new RollerException("unable to lookup weblog: "+
                        pageRequest.getWeblogHandle());
            }
            pageLink = pageRequest.getWeblogPageName();            
            day = DateUtil.parseWeblogURLDateString(pageRequest.getWeblogDate(),
                  weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
            locale = pageRequest.getLocale();
            
            initDay(day);  
            
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
        Date startDate = DateUtil.getStartOfMonth(month);
        Date endDate = DateUtil.getEndOfMonth(month);
        
        // Determine previous non-empty month
        // Get entries before startDate, using category restriction limit 1
        // Use entry's date as previous month
        try {
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            List prevEntries = mgr.getWeblogEntries(
                    
                    weblog,                    // website
                    null,                      // user
                    null,                      // startDate
                    startDate,                 // endDate 
                    cat,                       // cat
                    null,WeblogEntry.PUBLISHED, // status
                    null,                      // text
                    null,                      // sortby (null means pubTime)
                    WeblogManager.DESCENDING,  // sortorder, null means DESCENDING
                    locale,                    // locale
                    0, 1);                     // offset, range
            if (prevEntries.size() > 0) {
                WeblogEntry prevEntry = (WeblogEntry)prevEntries.get(0);
                prevMonth = DateUtil.getStartOfMonth(new Date(prevEntry.getPubTime().getTime()));
            }
        } catch (RollerException e) {
            log.error("ERROR determining previous non-empty month");
        }
        
        // Determine next non-empty month
        // Get entries after endDate, using category restriction limit 1
        // Use entry's date as next month
        try {
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            List nextEntries = mgr.getWeblogEntries(
                    
                    weblog,                    // website
                    null,                      // user
                    endDate,                   // startDate
                    null,                      // endDate 
                    cat,                       // cat
                    null,WeblogEntry.PUBLISHED, // status
                    null,                      // text
                    null,                      // sortby (null means pubTime)
                    WeblogManager.ASCENDING,   // sortorder
                    locale,                    // locale
                    0, 1);                     // offset, range
            if (nextEntries.size() > 0) {
                WeblogEntry nextEntry = (WeblogEntry)nextEntries.get(0);
                nextMonth = DateUtil.getStartOfMonth(new Date(nextEntry.getPubTime().getTime()));
            }
        } catch (RollerException e) {
            log.error("ERROR determining next non-empty month");
        }  
        
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
                    null,WeblogEntry.PUBLISHED, // status
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
        return URLUtilities.getWeblogCollectionURL(weblog, locale, cat, null, null, -1, false);
    }
    
}
