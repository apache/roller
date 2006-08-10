/*
 * AbstractWeblogEntriesPager.java
 *
 * Created on August 9, 2006, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.rendering.pagers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.MessageUtilities;
import org.apache.roller.util.URLUtilities;


/**
 *
 */
public abstract class AbstractWeblogEntriesPager implements WeblogEntriesPager {
    
    private static Log log = LogFactory.getLog(AbstractWeblogEntriesPager.class);
    
    WebsiteData weblog = null;
    String locale = null;
    String pageLink = null;
    String entryAnchor = null;
    String dateString = null;
    String catPath = null;
    int offset = 0;
    int page = 0;
    int length = 0;
    
    
    public AbstractWeblogEntriesPager(
            WebsiteData        weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath,
            int                page) {
        
        this.weblog = weblog;
        this.locale = locale;
        this.pageLink = pageLink;
        this.entryAnchor = entryAnchor;
        this.dateString = dateString;
        this.catPath = catPath;
        
        // make sure offset, length, and page are valid
        length = weblog.getEntryDisplayCount();
        if(page > 0) {
            this.page = page;
        }
        this.offset = length * page;
    }
    
    
    public boolean hasMoreEntries() {
        return false;
    }
    
    
    public String getHomeLink() {
        return createURL(0, 0, weblog, locale, pageLink, entryAnchor, dateString, catPath);
    }
    
    
    public String getHomeName() {
        return MessageUtilities.getString("weblogEntriesPager.latest.home");
    }
    
    
    public String getNextLink() {
        if (hasMoreEntries()) {
            return createURL(page, 1, weblog, locale, pageLink, entryAnchor, dateString, catPath);
        }
        return null;
    }
    
    
    public String getNextName() {
        if (hasMoreEntries()) {
            return MessageUtilities.getString("weblogEntriesPager.latest.next");
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (page > 0) {
            return createURL(page, -1, weblog, locale, pageLink, entryAnchor, dateString, catPath);
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (page > 0) {
            return MessageUtilities.getString("weblogEntriesPager.latest.prev");
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
    
    
    /**
     * Parse data as either 6-char or 8-char format.
     */
    protected Date parseDate(String dateString) {
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
    protected Date getToday() {
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
    protected String createURL(
            int                page,
            int                pageAdd,
            WebsiteData        website,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath) {
        
        int pageNum = page + pageAdd;
        
        if (pageLink != null) {
            return URLUtilities.getWeblogPageURL(website, locale, pageLink, entryAnchor, catPath, dateString, pageNum, false);
        } else if (entryAnchor != null) {
            return URLUtilities.getWeblogEntryURL(website, locale, entryAnchor, true);
        }
        
        return URLUtilities.getWeblogCollectionURL(website, locale, catPath, dateString, pageNum, false);
    }
    
}
