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

package org.apache.roller.presentation.weblog.tags;

import javax.servlet.http.HttpServletResponse;

import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.tags.menu.RollerMenuModel;
import org.apache.roller.util.DateUtil;

/** 
 * Calendar model for calendar intended for use on edit-weblog page.
 */
public class EditWeblogCalendarModel extends WeblogCalendarModel
{    
    protected String mQueryString = null;

    /**
     * @param req
     * @param resp
     * @param selfUrl
     * @param queryString  */    
	public EditWeblogCalendarModel(
        RollerRequest rreq,  HttpServletResponse res, String url)
	{
        super( rreq, res, url, null );
	}
    
    /** 
     * Create URL for use on edit-weblog page, preserves the request
     * parameters used by the tabbed-menu tag for navigation.
     * 
     * @param day   Day for URL
     * @param valid Always return a URL, never return null 
     * @return URL for day, or null if no weblog entry on that day
     */
    public String computeUrl( java.util.Date day, boolean valid )
    {
        String url = null;
        try 
        {
            boolean haveWeblogEntry = true;

            if ( day == null )
            {
                url = mRes.encodeURL(mSelfUrl + mCatName);
            }
            else
            {
                // get the 8 char YYYYMMDD datestring for day, returns null 
                // if no weblog entry on that day
                String dateString = (String)mMonthMap.get( day );

                if ( dateString == null )
                {
                    haveWeblogEntry = false;

                    // no weblog entry and no date, so use today as the date 
                    dateString = DateUtil.format8chars( day );
                }

                if ( haveWeblogEntry || valid )
                {
                    // create URL with params for day, menu, and menu item
                    StringBuffer sb = new StringBuffer();
                    sb.append( mSelfUrl );

                    sb.append( '&' );  
                    sb.append( RollerRequest.WEBLOGDAY_KEY);
                    sb.append( '=' );  
                    sb.append( dateString );  
                
                    //sb.append( "&method=edit" );

                    if (mReq.getParameter(RollerMenuModel.MENU_KEY) != null )
                    {
                        sb.append( '&' );
                        sb.append( RollerMenuModel.MENU_KEY );
                        sb.append( '=' );  
                        sb.append(mReq.getParameter(RollerMenuModel.MENU_KEY));  
                    }
                    if (mReq.getParameter(RollerMenuModel.MENU_ITEM_KEY)!=null)
                    {
                        sb.append( '&' );  
                        sb.append( RollerMenuModel.MENU_ITEM_KEY );
                        sb.append( '=' );  
                        sb.append(
                            mReq.getParameter(RollerMenuModel.MENU_ITEM_KEY));  
                    }
                    url = mRes.encodeURL( sb.toString() );
                }
            }
        }
        catch (Exception e)
        {
            RollerRequest.getRollerRequest(mReq).getServletContext().log("ERROR: creating URL",e);
        }
        return url;
   }
}

