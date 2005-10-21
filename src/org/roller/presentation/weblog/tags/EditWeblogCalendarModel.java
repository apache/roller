
package org.roller.presentation.weblog.tags;

import org.roller.model.WeblogManager;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.tags.menu.RollerMenuModel;
import org.roller.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

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

