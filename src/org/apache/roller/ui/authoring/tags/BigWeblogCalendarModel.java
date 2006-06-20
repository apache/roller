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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.util.DateUtil;

/**
 * Model for big calendar that displays titles for each day.
 * @author David M Johnson
 */
public class BigWeblogCalendarModel extends WeblogCalendarModel
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(BigWeblogCalendarModel.class);
    
    protected static final SimpleDateFormat mStarDateFormat = 
        DateUtil.get8charDateFormat();
        
    protected static final SimpleDateFormat mSingleDayFormat = 
        new SimpleDateFormat("dd");

    /**
     * @param rreq
     * @param res
     * @param url
     * @param cat
     */
    public BigWeblogCalendarModel(RollerRequest rreq, HttpServletResponse res, 
                                  String url, String cat)
    {
        super(rreq, res, url, cat);
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
            mMonthMap = mgr.getWeblogEntryObjectMap(
                            mRollerReq.getWebsite(),   // website
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

    /** 
     * Create URL for use on view-weblog page, ignores query-string.
     * @param day   Day for URL
     * @param valid Always return a URL, never return null 
     * @return URL for day, or null if no weblog entry on that day
     */
    public String computeUrl(Date day, boolean valid)
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
                String dateString = null;
                List entries = 
                   (List)mMonthMap.get( day );
                if ( entries != null && day != null )
                {
                	WeblogEntryData entry = (WeblogEntryData)entries.get(0);
                    dateString = 
                        mStarDateFormat.format(entry.getPubTime());
                    
                    // append 8 char date string on end of selfurl
                    url = mSelfUrl+"/"+dateString+mCatName;
                }
                else if ( entries != null )
                {
                    url = mSelfUrl+"/"+mCatName;
                }
                else if ( valid ) 
                {
                    // Make the date yyyyMMdd and append it to URL
                    dateString = mStarDateFormat.format( day );
                    url = mSelfUrl+"/"+dateString+mCatName;
                }
            }
        }
        catch (Exception e)
        {
            RollerRequest.getRollerRequest(mReq)
                .getServletContext().log("ERROR: creating URL",e);
        }
        return url;
    } 
    
    /**
     * @see org.apache.roller.presentation.tags.calendar.CalendarModel#getContent(Date, boolean)
     */
    public String getContent(Date day)
    {
        String content = null;
        try 
        {
			RollerRequest rreq = RollerRequest.getRollerRequest(mReq);
			RollerContext rctx = RollerContext.getRollerContext();
            StringBuffer sb = new StringBuffer();
            
            // get the 8 char YYYYMMDD datestring for day, returns null 
            // if no weblog entry on that day
            String dateString = null;
            List entries = (List)mMonthMap.get(day);
            if ( entries != null )
            {
                dateString = mStarDateFormat.format( 
                    ((WeblogEntryData)entries.get(0)).getPubTime());
                                
                // append 8 char date string on end of selfurl
                String dayUrl = mSelfUrl+"/"+ dateString+mCatName;

                sb.append("<div class=\"hCalendarDayTitleBig\">");
                sb.append("<a href=\"");
                sb.append( dayUrl );
                sb.append("\">");
                sb.append( mSingleDayFormat.format( day ) );
                sb.append("</a></div>");
                
                for ( int i=0; i<entries.size(); i++ )
                {
                    sb.append("<div class=\"bCalendarDayContentBig\">");
                    sb.append("<a href=\"");
                    sb.append(rctx.createEntryPermalink(
                        (WeblogEntryData)entries.get(i),mReq,false));
                    sb.append("\">");
                    
                    String title = ((WeblogEntryData)entries.get(i)).getTitle().trim();
                    if ( title.length()==0 )
                    {
                        title = ((WeblogEntryData)entries.get(i)).getAnchor();
                    }
                    if ( title.length() > 20 )
                    {
                        title = title.substring(0,20)+"...";
                    }
                   
                    sb.append( title );
                    sb.append("</a></div>");                       
                }  
                
            }
            else
            {
                sb.append("<div class=\"hCalendarDayTitleBig\">");
                sb.append( mSingleDayFormat.format( day ) );
                sb.append("</div>");
                sb.append("<div class=\"bCalendarDayContentBig\"/>");
            }
            content = sb.toString();              
        }
        catch (Exception e)
        {
            RollerRequest.getRollerRequest(mReq)
                .getServletContext().log("ERROR: creating URL",e);
        }
        return content;
    }
}
