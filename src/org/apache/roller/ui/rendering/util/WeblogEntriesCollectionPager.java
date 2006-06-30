/*
 * WeblogEntriesCollectionPager.java
 *
 * Created on June 29, 2006, 3:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.rendering.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.DateUtil;


/**
 * We're paging through the latest entries in the blog.
 * In this mode there's no prev/next collection.
 */
public class WeblogEntriesCollectionPager extends WeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesCollectionPager.class);
    
    private Map entries = null;
    
    private WebsiteData weblog = null;
    private String dateString = null;
    private String catPath = null;
    private int page = 0;
    
    // these will help define the boundaries of the entries query
    private Date startDate = null;
    private Date endDate = null;
    private int offset = 0;
    private int length = 0;
        
    // next/prev link variables
    private String nextLink = null;
    private String nextLinkName = null;
    private String prevLink = null;
    private String prevLinkName = null;
    
    
    public WeblogEntriesCollectionPager(WebsiteData weblog, String dateString,
                              String catPath, int page) {
        
        this.weblog = weblog;
        this.dateString = dateString;
        this.catPath = catPath;
        if(page > 0) {
            this.page = page;
        }
        
        // calculate offset and length
        this.length = weblog.getEntryDisplayCount();
        this.offset = length * page;
        
        // if we have a date string then do date calculations
        Date thisDate = null;
        Date nextDate = null;
        Date prevDate = null;
        String nextDateString = null;
        String prevDateString = null;
        if(dateString != null) {
            Calendar cal = Calendar.getInstance();
            
            // parse date string and figure out date
            thisDate = parseDate(dateString, weblog);
            
            // single day
            if(dateString.length() == 8) {
                // calculate next day
                cal.setTime(thisDate);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                nextDate = cal.getTime();
                if(nextDate.after(getToday(weblog))) {
                    nextDate = null;
                }
                
                // calculate next day date string
                nextDateString = DateUtil.format8chars(nextDate);
                
                // calculate previous day
                cal.setTime(thisDate);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                prevDate = cal.getTime();
                
                // calculate prev day date string
                prevDateString = DateUtil.format8chars(prevDate);
                
                // calculate query start/end dates
                cal = Calendar.getInstance(weblog.getTimeZoneInstance());
                startDate = DateUtil.getStartOfDay(thisDate, cal);
                endDate = DateUtil.getEndOfDay(thisDate, cal);
                
            // single month
            } else if(dateString.length() == 6) {
                // calculate next month
                cal.setTime(thisDate);
                cal.add(Calendar.MONTH, 1);
                nextDate = cal.getTime();
                if(nextDate.after(getToday(weblog))) {
                    nextDate = null;
                }
                
                // calculate next month date string
                nextDateString = DateUtil.format6chars(nextDate);
                
                // calculate previous month
                cal.setTime(thisDate);
                cal.add(Calendar.MONTH, -1);
                prevDate = cal.getTime();
                
                // calculate prev month date string
                prevDateString = DateUtil.format6chars(prevDate);
                
                // calculate query start/end dates
                cal = Calendar.getInstance(weblog.getTimeZoneInstance());
                startDate = DateUtil.getStartOfMonth(thisDate, cal);
                endDate = DateUtil.getEndOfMonth(thisDate, cal);
            }
        }
        
        // finally, calculate next/prev links and link names
        if(dateString != null & catPath != null) {
            nextLink = weblog.getURL()+"?date="+nextDateString+"&cat="+catPath+"&page="+page++;
            nextLinkName = nextDateString;
            prevLink = weblog.getURL()+"?date="+prevDateString+"&cat="+catPath;
            prevLinkName = prevDateString;
            
            if(page > 1) {
                prevLink += "&page="+page--;
            }
            
        } else if(dateString != null) {
            nextLink = weblog.getURL()+"/date/"+nextDateString;
            nextLinkName = nextDateString;
            prevLink = weblog.getURL()+"/date/"+prevDateString;
            prevLinkName = prevDateString;
            
            nextLink += "?page="+page++;
            if(page > 1) {
                prevLink += "?page="+page--;
            }
        } else {
            // there is no next/prev paging for homepage or just category
        }
    }
    
    
    public Map getEntries() {
        
        if(this.entries == null) {
            try {
                this.entries = getEntriesMap(weblog, startDate, endDate,
                        catPath, offset, length);
            } catch (RollerException ex) {
                log.error("Error getting entries", ex);
            }
        }
        
        return this.entries;
    }
    
    
    public String getNextLink() {
        return nextLink;
    }
    
    
    public String getNextLinkName() {
        return nextLinkName;
    }
    
    
    public String getPrevLink() {
        return prevLink;
    }
    
    
    public String getPrevLinkName() {
        return prevLinkName;
    }
    
    
    // TODO 3.0: calculate # of entries and figure this out
//    public boolean isMultiPage() {
//        return more;
//    }
//    
//    
//    public String getNextPageLink() {
//        String ret = null;
//        if (more) {
//            ret = getCurrentPageLink() + "?page=" + this.page++;
//        }
//        return ret;
//    }
    
    
    public String getNextPageLinkName() {
        String ret = null;
        if (getNextLink() != null) {
            ret = "Next"; // TODO: I18N
        }
        return ret;
    }
    
    
    public String getPrevPageLink() {
        String ret = null;
        if(this.page > 0) {
            ret = getCurrentPageLink() + "?page=" + this.page--;
        }
        return ret;
    }
    
    
    public String getPrevPageLinkName() {
        String ret = null;
        if (getNextLink() != null) {
            ret = "Prev"; // TODO: I18N
        }
        return ret;
    }
    
    
    private String getCurrentPageLink() {
        return weblog.getURL();
    }
    
    
    /**
     * Convenience method for querying for a map of entries and getting the
     * results wrapped using our pojo wrappers.
     */
    Map getEntriesMap(WebsiteData weblog, Date start, Date end,
                      String catPath, int offset, int length)
            throws RollerException {
        
        Map entries = new TreeMap();
        
        Map mmap = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                weblog,
                start,
                end,
                catPath,
                WeblogEntryData.PUBLISHED,
                offset,
                length);
        
        // need to wrap pojos
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
            }
            entries.put(key, wrapped);
        }
        
        return entries;
    }
    
    
    /** 
     * Parse data as either 6-char or 8-char format.
     */
    Date parseDate(String dateString, WebsiteData weblog) {
        
        Date ret = null;
        SimpleDateFormat char8DateFormat = DateUtil.get8charDateFormat();
        SimpleDateFormat char6DateFormat = DateUtil.get6charDateFormat();
        
        if (dateString != null
                && dateString.length()==8
                && StringUtils.isNumeric(dateString) ) {
            ParsePosition pos = new ParsePosition(0);
            ret = char8DateFormat.parse(dateString, pos);
            
            // make sure the requested date is not in the future
            Date today = getToday(weblog);
            if(ret.after(today)) {
                ret = today;
            }
            
        } else if(dateString != null
                && dateString.length()==6
                && StringUtils.isNumeric(dateString)) {
            ParsePosition pos = new ParsePosition(0);
            ret = char6DateFormat.parse(dateString, pos);
            
            // make sure the requested date is not in the future
            Date today = getToday(weblog);
            if(ret.after(today)) {
                ret = today;
            }
        }
        
        return ret;
    }
    
    
    /**
     * Return today based on current blog's timezone/locale.
     */
    Date getToday(WebsiteData weblog) {
        Calendar todayCal = Calendar.getInstance();
        todayCal = Calendar.getInstance(
            weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
        todayCal.setTime(new Date());
        return todayCal.getTime();
    }
    
}
