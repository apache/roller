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
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.util.DateUtil;


/**
 * Pager for weblog entries, handles latest, single-entry, month and day views.
 * Collection returned is a list of lists of entries, where each list of 
 * entries represents one day.
 */
public abstract class WeblogEntriesPager {
    
    
    /**
     * A map of entries representing this collection.
     *
     * The collection is grouped by days of entries.  Each value is a list of
     * entry objects keyed by the date they were published.
     */
    public abstract Map getEntries();
    
    
    /**
     * Link value for next collection view
     */
    public String getNextLink() {
        return null;
    }
    
    /**
     * Link name for next collection view
     */
    public String getNextLinkName() {
        return null;
    }
    
    /**
     * Link value for prev collection view
     */
    public String getPrevLink() {
        return null;
    }
    
    /**
     * Link name for prev collection view
     */
    public String getPrevLinkName() {
        return null;
    }
    
    /**
     * Does this pager represent a multi-page collection?
     */
    public boolean isMultiPage() {
        return false;
    }
    
    /**
     * Link value for next page in current collection view
     */
    public String getNextPageLink() {
        return null;
    }
    
    /**
     * Link name for next page in current collection view
     */
    public String getNextPageName() {
        return null;
    }
    
    /**
     * Link value for prev page in current collection view
     */
    public String getPrevPageLink() {
        return null;
    }
    
    /**
     * Link value for prev page in current collection view
     */
    public String getPrevPageName() {
        return null;
    }
    
}

