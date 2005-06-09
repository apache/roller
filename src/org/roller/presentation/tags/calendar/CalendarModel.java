
package org.roller.presentation.tags.calendar;
import java.util.Calendar;
import java.util.Date;

/** 
 * Model interface for the CalendarTag. The CalendarTag will set a day, 
 * then use the computeUrl method to get the URLs it needs.
 */
public interface CalendarModel 
{    
    public Calendar getCalendar();
    
	public void setDay( String month ) throws Exception;
    
	public Date getDay();
    
    public Date getNextMonth();
    
    public String computePrevMonthUrl();    

    public String computeTodayMonthUrl();    

    public String computeNextMonthUrl();
    
    /** 
     * Create URL for use on edit-weblog page, preserves the request
     * parameters used by the tabbed-menu tag for navigation.
     * 
     * @param day   Day for URL
     * @param valid Always return a URL, never return null 
     * @return URL for day, or null if no weblog entry on that day
     */
    public String computeUrl( java.util.Date day, boolean valid );
    
    /** 
     * Get calendar cell content or null if none.
     * 
     * @param day Day for URL
     * @param valid Always return a URL, never return null 
     * @return Calendar cell content or null if none.
     */
    public String getContent( java.util.Date day );
}

