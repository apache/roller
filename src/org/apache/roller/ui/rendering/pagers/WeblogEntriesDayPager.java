/*
 * WeblogEntriesDayPager.java
 *
 * Created on August 9, 2006, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.rendering.pagers;

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
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.MessageUtilities;


/**
 *
 */
public class WeblogEntriesDayPager extends AbstractWeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesDayPager.class);
    
    private SimpleDateFormat dayFormat = new SimpleDateFormat(
            MessageUtilities.getString("weblogEntriesPager.day.dateFormat"));
    
    private Date day;
    private Date nextDay;
    private Date prevDay;
    
    // collection for the pager
    private Map entries = null;
    
    // are there more pages?
    private boolean more = false;
    
    
    public WeblogEntriesDayPager(
            WebsiteData        weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath,
            int                page) {
        
        super(weblog, locale, pageLink, entryAnchor, dateString, catPath, page);
        
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
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                Map mmap = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                        weblog,
                        startDate,
                        endDate,
                        catPath,
                        WeblogEntryData.PUBLISHED, 
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
                            wrapped.add(i, 
                            WeblogEntryDataWrapper.wrap((WeblogEntryData)unwrapped.get(i)));
                        } else {
                            more = true;
                        }
                    }
                    
                    // done with that day, put it in the map
                    entries.put(key, wrapped);
                }
                
                
            } catch (Exception e) {
                log.error("ERROR: getting entry month map", e);
            }
        }
        return entries;
    }
    
    
    public String getHomeLink() {
        return createURL(0, 0, weblog, locale, pageLink, null, null, catPath);
    }
    
    
    public String getHomeName() {
        return MessageUtilities.getString("weblogEntriesPager.day.home");
    }
    
    
    public String getNextLink() {
        if (more) {
            return createURL(page, 1, weblog, locale, pageLink, null, dateString, catPath);
        }
        return null;
    }
    
    
    public String getNextName() {
        if (getNextLink() != null) {
            return MessageUtilities.getString("weblogEntriesPager.day.next", new Object[] {dayFormat.format(day)});
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (page > 0) {
            return createURL(page, -1, weblog, locale, pageLink, null, dateString, catPath);
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (getPrevLink() != null) {
            return MessageUtilities.getString("weblogEntriesPager.day.prev", new Object[] {dayFormat.format(day)});
        }
        return null;
    }
    
    
    public String getNextCollectionLink() {
        if (nextDay != null) {
            String next = DateUtil.format8chars(nextDay);
            return createURL(0, 0, weblog, locale, pageLink, null, next, catPath);
        }
        return null;
    }
    
    
    public String getNextCollectionName() {
        if (nextDay != null) {
            return MessageUtilities.getString("weblogEntriesPager.day.nextCollection", new Object[] {dayFormat.format(nextDay)});
        }
        return null;
    }
    
    
    public String getPrevCollectionLink() {
        if (prevDay != null) {
            String prev = DateUtil.format8chars(prevDay);
            return createURL(0, 0, weblog, locale, pageLink, null, prev, catPath);
        }
        return null;
    }
    
    
    public String getPrevCollectionName() {
        if (prevDay != null) {
            return MessageUtilities.getString("weblogEntriesPager.day.prevCollection", new Object[] {dayFormat.format(prevDay)});
        }
        return null;
    }
    
}
