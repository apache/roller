
package org.roller.presentation.weblog.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.WeblogManager;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.tags.calendar.CalendarModel;
import org.roller.util.DateUtil;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Calendar model for calendar intended for use on view-weblog page.
 */
public class WeblogCalendarModel implements CalendarModel
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(WeblogCalendarModel.class);
    
    protected RollerRequest       mRollerReq = null;
	protected HttpServletRequest  mReq = null;
	protected HttpServletResponse mRes = null;
	protected Map                 mMonthMap;
    protected String              mSelfUrl;
    protected Date                mDay;
    protected String              mCatName = null;
    protected Calendar            mCalendar = null;

	public WeblogCalendarModel(
        RollerRequest rreq, HttpServletResponse res, String url, String cat)
	{
        mRollerReq = rreq;
		mReq = rreq.getRequest();
		mRes = res;
        mSelfUrl = url;
        
        setDay( mRollerReq.getDate(true) );
        
        // If category is specified in URL, then perpetuate it
        String catKey = RollerRequest.WEBLOGCATEGORYNAME_KEY;
        String catToUse = mReq.getParameter(catKey);
        if (mReq.getParameter(catKey) == null)
        {
            // If no cat in URL, then use method argument
            catToUse = cat;
        }
        if ( catToUse != null )
        {
            mCatName = "?"+catKey+"="+catToUse;
        }
        else
        {
            mCatName = "";
        }                
    }
 /*   
	public static WeblogCalendarModel getInstance(
		HttpServletRequest req, HttpServletResponse res, String url, String cat)
	{
		return new WeblogCalendarModel(RollerRequest.getRollerRequest(req), res, url, cat);
	}
*/	
	private void setDay(Date month)
	{
        mDay = month;
        
        // If category is specified in URL, then use it
        String catName = 
            mReq.getParameter(RollerRequest.WEBLOGCATEGORYNAME_KEY);

        mCalendar = Calendar.getInstance(
                                mRollerReq.getWebsite().getTimeZoneInstance(),
                                mRollerReq.getWebsite().getLocaleInstance()
        );   
        
        Calendar cal = (Calendar)mCalendar.clone();
        
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

        loadWeblogEntries(startDate, endDate, catName);
    }
	
	/**
     * @param startDate
     * @param endDate
     */
    protected void loadWeblogEntries(Date startDate, Date endDate, String catName)
    {
        try
        {
            WeblogManager mgr = mRollerReq.getRoller().getWeblogManager();
            mMonthMap = mgr.getWeblogEntryStringMap(
                            mRollerReq.getWebsite(), // userName
                            startDate,              // startDate
                            endDate,                // endDate
                            catName,                // catName
                            WeblogManager.PUB_ONLY, // status
                            new Integer(100) // TODO: WeblogEntry throttle 
            );
        }
        catch (RollerException e)
        {
            mLogger.error(e);
            mMonthMap = new HashMap();
        }
    }
    
    public void setDay(String month) throws Exception
	{
        SimpleDateFormat fmt = DateUtil.get8charDateFormat();
		ParsePosition pos = new ParsePosition(0);
        setDay( fmt.parse( month, pos ) );
	}
	
	public Date getDay()
	{
        return mDay;
	}
	
	public String getSelfUrl() throws Exception
	{
        return mRes.encodeURL(mSelfUrl);
	}

	public String getTargetUrl() throws Exception
	{
		return getSelfUrl();
	}

	public String getParameterName()
	{
		return RollerRequest.WEBLOGDAY_KEY;
	}

	public String getParameterValue( Date day )
	{
		return (String)mMonthMap.get( day );
	}

    /** 
     * Create URL for use on view-weblog page, ignores query-string.
     * @param day   Day for URL
     * @param valid Always return a URL, never return null 
     * @return URL for day, or null if no weblog entry on that day
     */
    public String computeUrl(java.util.Date day, boolean valid)
    {
        String url = null;
        try 
        {
            if ( day == null )
            {
                url = mRes.encodeURL(mSelfUrl + mCatName);
            }
            else
            {            
                // get the 8 char YYYYMMDD datestring for day, returns null 
                // if no weblog entry on that day
                String dateString = (String)mMonthMap.get( day );
                if ( dateString != null )
                {                
                    // append 8 char date string on end of selfurl
                    url = mRes.encodeURL(mSelfUrl+"/"+dateString+mCatName);
                }
                else if ( valid ) 
                {
                    // Make the date yyyyMMdd and append it to URL
                    dateString = DateUtil.format8chars( day );
                    url = mRes.encodeURL( mSelfUrl+"/"+dateString+mCatName);
                }
            }
        }
        catch (Exception e)
        {
           mRollerReq.getServletContext().log("ERROR: creating URL",e);
        }
        return url;
    } 
    
    /**
     * @see org.roller.presentation.tags.calendar.CalendarModel#getContent(Date, boolean)
     */
    public String getContent(Date day)
    {
        return null;
    }
    
    public Calendar getCalendar()
    {
        return (Calendar)mCalendar.clone();
    }
    
    public Date getNextMonth()
    {
        Calendar nextCal = getCalendar();
        nextCal.setTime( mDay );
        nextCal.add( Calendar.MONTH, 1 );
        return getFirstDayOfMonth(nextCal).getTime();
    }
    
    protected Calendar getFirstDayOfMonth(Calendar cal) 
    {
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        return cal;
    }
    
    protected Calendar getLastDayOfMonth(Calendar cal) 
    {
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return cal;
    }
    
    public String computeNextMonthUrl()
    {
        // Create yyyyMMdd dates for next month, prev month and today 
        Calendar nextCal = getCalendar();
        nextCal.setTime( mDay );
        nextCal.add( Calendar.MONTH, 1 );
        Date nextMonth = getLastDayOfMonth(nextCal).getTime();
        String nextMonthUrl = computeUrl(nextMonth, true);            
        return nextMonthUrl;
    }

    public String computePrevMonthUrl()
    {
        Calendar prevCal = getCalendar();
        prevCal.setTime( mDay );
        prevCal.add( Calendar.MONTH, -1 );
        getLastDayOfMonth( prevCal );
        String prevMonth = computeUrl(prevCal.getTime(),true);
        return prevMonth;
    }

    public String computeTodayMonthUrl()
    {
        return computeUrl(null,true);
    }
}

