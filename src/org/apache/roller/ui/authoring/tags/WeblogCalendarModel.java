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

package org.apache.roller.ui.authoring.tags;

import java.net.URLEncoder;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.tags.calendar.CalendarModel;
import org.apache.roller.util.DateUtil;

/** 
 * Calendar model for calendar intended for use on view-weblog page.
 */
public class WeblogCalendarModel implements CalendarModel
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(WeblogCalendarModel.class);
    
	protected HttpServletRequest  mReq = null;
	protected HttpServletResponse mRes = null;
	protected Map                 mMonthMap;
    protected String              mSelfUrl;
    protected Date                mDay;
    protected String              mCatName = null;
    protected Calendar            mCalendar = null;
    protected WebsiteData         mWebsite = null;
    

	public WeblogCalendarModel(HttpServletRequest req, HttpServletResponse res, 
                WebsiteData website, Date date, String url, String cat)
	{
		mReq = req;
		mRes = res;
                mWebsite = website;
                mSelfUrl = url;
        
        setDay( date );
        
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
            try 
            {
                mCatName = "?"+catKey+"="+URLEncoder.encode(catToUse, "UTF-8");
            }
            catch (Throwable shouldNeverHappen)
            {
                mLogger.error(shouldNeverHappen);
                mCatName = "?"+catKey+"="+catToUse;
            }
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
                                mWebsite.getTimeZoneInstance(),
                                mWebsite.getLocaleInstance()
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
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            mMonthMap = mgr.getWeblogEntryStringMap(
                            mWebsite,                  // website
                            startDate,                 // startDate
                            endDate,                   // endDate
                            catName,                   // catName
                            WeblogEntryData.PUBLISHED, // status
                            0, -1
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
        return (Date)mDay.clone();
	}
	
	public String getSelfUrl() throws Exception
	{
        return mSelfUrl;
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
                url = mSelfUrl + mCatName;
            }
            else
            {            
                // get the 8 char YYYYMMDD datestring for day, returns null 
                // if no weblog entry on that day
                String dateString = (String)mMonthMap.get( day );
                if ( dateString != null )
                {                
                    // append 8 char date string on end of selfurl
                    url = mSelfUrl+"/"+dateString+mCatName;
                }
                else if ( valid ) 
                {
                    // Make the date yyyyMMdd and append it to URL
                    dateString = DateUtil.format8chars( day );
                    url = mSelfUrl+"/"+dateString+mCatName;
                }
            }
        }
        catch (Exception e)
        {
           mLogger.error("ERROR: creating URL",e);
        }
        return url;
    } 
    
    /**
     * @see org.apache.roller.presentation.tags.calendar.CalendarModel#getContent(Date, boolean)
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
        String nextMonth = computeUrl(nextCal.getTime(), true);            
        
        // and strip off last two digits to get a month URL
        return nextMonth.substring(0, nextMonth.length() - 2);
    }

    public String computePrevMonthUrl()
    {
        // Create yyyyMMdd dates for prev month, prev month and today 
        Calendar prevCal = getCalendar();
        prevCal.setTime( mDay );
        prevCal.add( Calendar.MONTH, -1 );
        //getLastDayOfMonth( prevCal );
        String prevMonth = computeUrl(prevCal.getTime(),true);
        
        // and strip off last two digits to get a month URL
        return prevMonth.substring(0, prevMonth.length() - 2);
    }

    public String computeTodayMonthUrl()
    {
        return computeUrl(null,true);
    }
}

