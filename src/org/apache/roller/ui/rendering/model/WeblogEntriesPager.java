package org.apache.roller.ui.rendering.model;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.DateUtil;


/**
 * Pager for weblog entries, handles latest, single-entry, month and day views.
 * Collection returned is a list of lists of entries, where each list of 
 * entries represents one day.
 */
public class WeblogEntriesPager implements RenderDayPager {
    
    /**
     * Behavior of the pager is detemined by the mode, which is itself a pager.
     * The mode may be LatestMode, SingleEntryMode, DayMode or MonthMode.
     */
    protected RenderDayPager mode = null;
    
    protected Map            entries = null;
    protected WebsiteData    weblog = null;
    protected int            offset = 0;
    protected int            length = 0;
    protected String         chosenCatPath = null; 
    protected String         dateString = null; 
    protected String         entryAnchor = null;
    protected boolean        more = false;
        
    protected static Log log =
            LogFactory.getFactory().getInstance(WeblogEntriesPager.class); 
    
    public WeblogEntriesPager(
            HttpServletRequest request, 
            WebsiteData weblog, 
            String entryAnchor,
            String requestedCat,
            String callerCat,
            String dateString) { 
        
        this.weblog = weblog;
        this.dateString = dateString;
        this.entryAnchor = entryAnchor;
        
        length = weblog.getEntryDisplayCount();
        
        if (callerCat != null && "nil".equals(callerCat)) callerCat = null;
        
        if (request.getParameter("offset") != null) {
            try {
                offset = Integer.parseInt(request.getParameter("offset"));
            } catch (Exception ignored) {}
        }
        
        String chosenCatPath = callerCat != null ? callerCat : requestedCat;
        if (chosenCatPath == null) {
            // no category specifed so use default
            chosenCatPath = weblog.getDefaultCategory().getPath();
            chosenCatPath = chosenCatPath.equals("/") ? null : chosenCatPath;
        }
        
        // determine which mode we're working in
        if (entryAnchor != null) {
            mode = new SingleEntryMode();
        } else if (dateString != null && dateString.length() == 8) {
            mode = new DayMode();
        } else if (dateString != null && dateString.length() == 6) {
            mode = new MonthMode();
        } else {
            mode = new LatestMode();
        }
    }
        
    public Map getCurrentValues() {
        return mode.getCurrentValues();
    }
    
    public String getNextLink() {
        return mode.getNextLink();
    }

    public String getNextLinkName() {
        return mode.getNextLink();
    }

    public String getPrevLink() {
        return mode.getPrevLink();
    }

    public String getPrevLinkName() {
        return mode.getPrevLinkName();
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
    class LatestMode implements RenderDayPager {
        
        public Map getCurrentValues() {
            return getCurrentValuesImpl();
        }

        public String getNextLink() {
            String ret = null;
            if (more) {
                int nextOffset = offset + length;
                ret = weblog.getURL() + "&offset=" + nextOffset;
            }
            return ret;
        }

        public String getNextLinkName() {
            String ret = null;
            if (getNextLink() != null) {
                ret = "Next"; // TODO: I18N
            }
            return ret;
        }

        public String getPrevLink() {
            String ret = null;
            if (offset > 0) {
                int prevOffset = offset + length;
                prevOffset = (prevOffset < 0) ? 0 : prevOffset;
                ret = weblog.getURL() + "&offset=" + prevOffset;
            }
            return ret;
        }

        public String getPrevLinkName() {
            String ret = null;
            if (getNextLink() != null) {
                ret = "Prev"; // TODO: I18N
            }
            return ret;
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
    class SingleEntryMode implements RenderDayPager {
        String nextLink = null;
        WeblogEntryData entry = null;
        WeblogEntryData nextEntry = null;
        WeblogEntryData prevEntry = null;
        
        public SingleEntryMode() {
            SingleEntryMode.this.getCurrentValues();
        }
        
        /**
         * Wrap entry up in map of lists.
         */
        public Map getCurrentValues() {
            if (entries == null) try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                entry = wmgr.getWeblogEntryByAnchor(weblog, entryAnchor);
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

        public String getNextLink() {
            String ret = null;
            if (getNextEntry() != null) {
                ret = getNextEntry().getPermalink();
            }
            return ret;
        }

        public String getNextLinkName() {
            String ret = null;
            if (getNextEntry() != null) {
                ret = getNextEntry().getTitle();
            }
            return ret;
        }

        public String getPrevLink() {
            String ret = null;
            if (getPrevEntry() != null) {
                ret = getPrevEntry().getPermalink();
            }
            return ret;
        }

        public String getPrevLinkName() {
            String ret = null;
            if (getPrevEntry() != null) {
                ret = getPrevEntry().getTitle();
            }
            return ret;
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
                nextEntry = wmgr.getNextEntry(entry, chosenCatPath);
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
                prevEntry = wmgr.getPreviousEntry(entry, chosenCatPath); 
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
    class DayMode implements RenderDayPager {
        private Date day;
        private Date nextDay;
        private Date prevDay;
        
        public DayMode() {
            getCurrentValuesImpl();
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
        
        public Map getCurrentValues() {
            return getCurrentValuesImpl();
        }

        public String getNextLink() {
            String ret = null;
            if (more) {
                int nextOffset = offset + length;
                ret = weblog.getURL() + "/date/" + dateString + "&offset=" + nextOffset;
            }
            return ret;
        }

        public String getNextLinkName() {
            String ret = null;
            if (getNextLink() != null) {
                ret = "Next"; // TODO: I18N
            }
            return ret;
        }

        public String getPrevLink() {
            String ret = null;
            if (offset > 0) {
                int prevOffset = offset + length;
                prevOffset = (prevOffset < 0) ? 0 : prevOffset;
                ret = weblog.getURL() + "/date/" + dateString + "&offset=" + prevOffset;
            }
            return ret;
        }

        public String getPrevLinkName() {
            String ret = null;
            if (getNextLink() != null) {
                ret = "Prev"; // TODO: I18N
            }
            return ret;
        }

        public String getNextCollectionLink() {
            String ret = null;
            if (nextDay != null) {
                String next = DateUtil.format8chars(nextDay);
                ret = weblog.getURL() + "/date/" + next;
            }
            return ret;
        }

        public String getNextCollectionName() {
            String ret = null;
            if (nextDay != null) {
                ret = DateUtil.format8chars(nextDay);
            }
            return ret;
        }

        public String getPrevCollectionLink() {
            String ret = null;
            if (prevDay != null) {
                String prev = DateUtil.format8chars(prevDay);
                ret = weblog.getURL() + "/date/" + prev;
            }
            return ret;
        }

        public String getPrevCollectionName() {
            String ret = null;
            if (prevDay != null) {
                ret = DateUtil.format8chars(prevDay);
            }
            return ret;
        }
    }
    
    //-------------------------------------------------------------------------

    /**
     * We're paging through entries within one month.
     * Next/prev methods return links to offsets within month's entries.
     * Next/prev collection methods return links to next and previous months.
     */
    class MonthMode implements RenderDayPager {
        private Date month;
        private Date nextMonth;
        private Date prevMonth;
        
        public MonthMode() {
            getCurrentValuesImpl();
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
        
        public Map getCurrentValues() {
            return getCurrentValuesImpl();
        }

        public String getNextLink() {
            String ret = null;
            if (more) {
                int nextOffset = offset + length;
                ret = weblog.getURL() + "/date/" + dateString + "&offset=" + nextOffset;
            }
            return ret;
        }

        public String getNextLinkName() {
            String ret = null;
            if (getNextLink() != null) {
                ret = "Next"; // TODO: I18N
            }
            return ret;
        }

        public String getPrevLink() {
            String ret = null;
            if (offset > 0) {
                int prevOffset = offset + length;
                prevOffset = (prevOffset < 0) ? 0 : prevOffset;
                ret = weblog.getURL() + "/date/" + dateString + "&offset=" + prevOffset;
            }
            return ret;
        }

        public String getPrevLinkName() {
            String ret = null;
            if (getNextLink() != null) {
                ret = "Prev"; // TODO: I18N
            }
            return ret;
        }

        public String getNextCollectionLink() {
            String ret = null;
            if (nextMonth != null) {
                String next = DateUtil.format6chars(nextMonth); 
                ret = weblog.getURL() + "/date/" + next;
            }
            return ret;
        }

        public String getNextCollectionName() {
            String ret = null;
            if (nextMonth != null) {
                ret = DateUtil.format6chars(nextMonth);
            }
            return ret;
        }

        public String getPrevCollectionLink() {
            String ret = null;
            if (prevMonth != null) {
                String prev = DateUtil.format6chars(prevMonth);
                ret = weblog.getURL() + "/date/" + prev;
            }
            return ret;
        }

        public String getPrevCollectionName() {
            String ret = null;
            if (prevMonth != null) {
                ret = DateUtil.format6chars(prevMonth);
            }
            return ret;
        }
        
    }  
                
    //------------------------------------------------------------------------
    
    /**
     * Get current values specified by request, a map of lists of entry  
     * wrappers, keyed by date objects, where each list holds entries 
     * for one day.
     */
    private Map getCurrentValuesImpl() {
        if (entries == null) {
            entries = new TreeMap();
            try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                Date date = parseDate(dateString);

                Calendar cal = Calendar.getInstance(weblog.getTimeZoneInstance());
                Date startDate = null;
                Date endDate = date;
                if (endDate == null) endDate = new Date();
                if (mode instanceof DayMode) {
                    // URL specified a specific day so get all entries for it
                    startDate = DateUtil.getStartOfDay(endDate, cal);
                    endDate = DateUtil.getEndOfDay(endDate, cal);
                } else if (mode instanceof MonthMode) {
                    // URL specified a specific month so get all entries for it
                    startDate = DateUtil.getStartOfMonth(endDate, cal);
                    endDate = DateUtil.getEndOfMonth(endDate, cal);
                }
                Map mmap = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                        weblog,
                        startDate,
                        endDate,
                        chosenCatPath,
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
    

}

