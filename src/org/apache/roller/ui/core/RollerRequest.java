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

package org.apache.roller.ui.core;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.Template;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.Utilities;
 
//////////////////////////////////////////////////////////////////////////////
/**
 * Access to objects and values specified by request. Parses out arguments from
 * request URL needed for various parts of Roller and makes them available via
 * getter methods.
 * <br/><br/> 
 * 
 * These forms of pathinfo get special support (where 'handle' indicates website):
 * <br/><br/>
 * 
 * <pre>
 * [handle] - get default page for user for today's date 
 * [handle]/[date] - get default page for user for specified date 
 * [handle]/[pagelink] - get specified page for today's date 
 * [handle]/[pagelink]/[date] - get specified page for specified date
 * [handle]/[pagelink]/[anchor] - get specified page & entry (by anchor)
 * [handle]/[pagelink]/[date]/[anchor] - get specified page & entry (by anchor)
 * </pre>
 *  
 * @author David M Johnson
 */
public class RollerRequest
{
    //----------------------------------------------------------------- Fields
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerRequest.class);

    private BookmarkData       mBookmark;
    private ServletContext     mContext = null;    
    private Date               mDate = null;
    private String             mDateString = null;
    private String             mPathInfo = null; 
    private String             mPageLink = null;
    private Template           mPage;
    private PageContext        mPageContext = null;
    private HttpServletRequest mRequest = null;
    private WebsiteData        mWebsite;
    private WeblogEntryData    mWeblogEntry;
    private WeblogCategoryData mWeblogCategory;
    
    private boolean            mIsDaySpecified = false;
    private boolean            mIsMonthSpecified = false;
        
    private static ThreadLocal mRollerRequestTLS = new ThreadLocal();
    
    public static final String WEBLOG_KEY             = "weblog";
    public static final String ANCHOR_KEY             = "entry";
    public static final String ANCHOR_KEY_OLD         = "anchor";
    public static final String USERNAME_KEY           = "username";

    public static final String PAGELINK_KEY           = "pagelink";
    public static final String EXCERPTS_KEY           = "excerpts";
    public static final String WEBLOGENTRY_COUNT      = "count";
    public static final String WEBLOGCATEGORYNAME_KEY = "catname";
    public static final String WEBLOGENTRIES_KEY      = "entries";
    public static final String WEBLOGDAY_KEY          = "day";
    
    public static final String WEBLOGENTRYID_KEY      = "entryid";
    
    public static final String WEBLOGCATEGORYID_KEY   = "categoryId";
    public static final String PINGTARGETID_KEY       = "pingtargetId";
    public static final String REFERERID_KEY          = "refId";
    public static final String WEBLOGCOMMENTID_KEY    = "commentId";
    public static final String WEBSITEID_KEY          = "websiteId";
    public static final String BOOKMARKID_KEY         = "bookmarkId";
    public static final String FOLDERID_KEY           = "folderId";
    public static final String PARENTID_KEY           = "parentId";
    public static final String NEWSFEEDID_KEY         = "feedId";
    public static final String PAGEID_KEY             = "pageId";
    public static final String LOGIN_COOKIE           = "sessionId";
    
    public static final String OWNING_WEBSITE         = "OWNING_WEBSITE";
    
    public static final String ROLLER_REQUEST        = "roller_request";
    
    private SimpleDateFormat m8charDateFormat = DateUtil.get8charDateFormat();
    private SimpleDateFormat m6charDateFormat = DateUtil.get6charDateFormat();

    //----------------------------------------------------------- Constructors

    /** Construct Roller request for a Servlet request */
    public RollerRequest( HttpServletRequest req, ServletContext ctx )
        throws RollerException
    {
        mRequest = req;
        mContext = ctx;
        init();
    }
    
    /** Convenience */
    public RollerRequest( ServletRequest req, ServletContext ctx ) 
        throws RollerException
    {
        mRequest = (HttpServletRequest)req;
        mContext = ctx;
        init();
    }
    
    public RollerRequest( PageContext pCtx) throws RollerException
    {
        mRequest = (HttpServletRequest) pCtx.getRequest();
        mContext = pCtx.getServletContext();
        mPageContext = pCtx;
        init();
    }

    private void init() throws RollerException
    {
        mRollerRequestTLS.set(this);
        if (mRequest.getContextPath().equals("/atom"))
        {
            return; // Atom servlet request needs no init
        }
        
        // Bind persistence session to authenticated user, if we have one
        RollerContext rctx = RollerContext.getRollerContext(); 
        Authenticator auth = rctx.getAuthenticator();
        String userName = auth.getAuthenticatedUserName(mRequest);
        if (userName != null)
        {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            UserData currentUser = userMgr.getUserByUserName(userName);
            // TODO: possible fix for backend refactoryings
            //RollerFactory.getRoller().setUser(currentUser);
        }
        
        // path info may be null, (e.g. on JSP error page)
        mPathInfo = mRequest.getPathInfo();
        mPathInfo = (mPathInfo!=null) ? mPathInfo : "";            
        
        String[] pathInfo = StringUtils.split(mPathInfo,"/");  
        if ( pathInfo.length > 0 )
        {
            // Parse pathInfo and throw exception if it is invalid
            if (!"j_security_check".equals(pathInfo[0]))
            {
                parsePathInfo( pathInfo );
                return;
            }
        }

        // Parse user, page, and entry from request params if possible
        parseRequestParams();
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(getRequestURL());
        sb.append(", ");
        sb.append(getRefererURL());
        sb.append("]");
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /** 
     * Bind persistence session to specific user.
     */
    private void bindUser() throws RollerException
    {
    }
    
    //------------------------------------------------------------------------
    /** Parse pathInfo and throw exception if it is invalid */
    private void parsePathInfo( String[] pathInfo ) throws RollerException
    {
        try 
        {
            // If there is any path info, it must be in one of the 
            // below formats or the request is considered to be invalid.
            //
            //   /username 
            //   /username/datestring 
            //   /username/pagelink
            //   /username/pagelink/datestring
            //   /username/pagelink/anchor (specific entry)
            //   /username/pagelink/datestring/anchor (specific entry)
            Roller roller = RollerFactory.getRoller();
            UserManager userMgr = roller.getUserManager();
            mWebsite = userMgr.getWebsiteByHandle(pathInfo[0]);
            if (mWebsite != null)
            {
                if ( pathInfo.length == 1 )
                {
                    // we have the /username form of URL
                    mPage = mWebsite.getDefaultPage();
                }
                else if ( pathInfo.length == 2 )
                {
                    mDate = parseDate(pathInfo[1]);
                    if ( mDate == null ) // pre-jdk1.4 --> || mDate.getYear() <= 70 )
                    {
                        // we have the /username/pagelink form of URL
                        mPageLink = pathInfo[1];
                        mPage = mWebsite.getPageByLink(pathInfo[1]);
                    }
                    else
                    {
                        // we have the /username/datestring form of URL
                        mDateString = pathInfo[1];
                        mPage = mWebsite.getDefaultPage();
                        if (pathInfo[1].length() == 8) {
                            mIsDaySpecified = true;
                        } else {
                            mIsMonthSpecified = true;
                        }
                    }               
                }
                else if ( pathInfo.length == 3 )
                {
                    mPageLink = pathInfo[1];
                    mPage = mWebsite.getPageByLink(pathInfo[1]);
                    
                    mDate = parseDate(pathInfo[2]);
                    if ( mDate == null ) // pre-jdk1.4 --> || mDate.getYear() <= 70 )
                    {
                        // we have the /username/pagelink/anchor form of URL
                        try
                        {
                            WeblogManager weblogMgr = roller.getWeblogManager();
                            mWeblogEntry = weblogMgr.getWeblogEntryByAnchor(
                                mWebsite, pathInfo[2]);
                        }
                        catch (Exception e)
                        {
                            mLogger.debug("Can't find entry by anchor", e);
                        }
                    }
                    else
                    {
                        // we have the /username/pagelink/datestring form of URL
                        mDateString = pathInfo[2];
                        if (pathInfo[1].length() == 8) {
                            mIsDaySpecified = true;
                        } else {
                            mIsMonthSpecified = true;
                        }
                    }
                }
                else if ( pathInfo.length == 4 )
                {
                    // we have the /username/pagelink/datestring/anchor form of URL
                    mPageLink = pathInfo[1];
                    mPage = mWebsite.getPageByLink(pathInfo[1]);
                    
                    mDate = parseDate(pathInfo[2]);
                    mDateString = pathInfo[2];
                    if (pathInfo[1].length() == 8) {
                        mIsDaySpecified = true;
                    } else {
                        mIsMonthSpecified = true;
                    }

                    // we have the /username/pagelink/date/anchor form of URL
                    WeblogManager weblogMgr = RollerFactory.getRoller().getWeblogManager();
                    mWeblogEntry = weblogMgr.getWeblogEntryByAnchor(
                                    mWebsite, pathInfo[3]);
                }                
            }
            if (mDate == null && mWeblogEntry != null)
            {
                mDate = mWeblogEntry.getPubTime();
                //mDateString = DateUtil.format8chars(mDate);
            }
        }
        catch ( Exception ignored )
        {
            mLogger.debug("Exception parsing pathInfo",ignored);
        }
        
        // NOTE: let the caller handle missing website/page instead
        if ( mWebsite==null || mPage==null )
        {            
            String msg = "Invalid pathInfo: "+StringUtils.join(pathInfo,"|");
            throw new RollerException(msg);
        }
    }       
    
    //------------------------------------------------------------------------
    /** Parse user, page, and entry from request params if possible */
    private void parseRequestParams()
    {
        try
        {
            // No path info means that we need to parse request params
            
            // First, look for user in the request params
            UserManager userMgr = RollerFactory.getRoller().getUserManager();            
            String userName = mRequest.getParameter(USERNAME_KEY);
            if ( userName == null )
            {
                // then try remote user
                userName = mRequest.getRemoteUser(); 
            }
            
            String handle = mRequest.getParameter(RollerRequest.WEBLOG_KEY);
            String websiteid = mRequest.getParameter(RollerRequest.WEBSITEID_KEY);
            if (handle != null && mWebsite == null) 
            {
                mWebsite = userMgr.getWebsiteByHandle(handle); 
            }
            else if (websiteid != null && mWebsite == null )
            {
                mWebsite = userMgr.getWebsite(websiteid); 
            }
            
            // Look for page ID in request params
            String pageId = mRequest.getParameter(RollerRequest.PAGEID_KEY);                    
            if ( pageId != null )
            {
                mPage = userMgr.getPage(pageId);                 
            }
            else if (mWebsite != null) 
            {
                mPage = mWebsite.getDefaultPage();
            }
                                       
            // Look for day in request params 
            String daystr = mRequest.getParameter(WEBLOGDAY_KEY);
            if ( daystr != null )
            {
                mDate = parseDate(daystr);
                if ( mDate != null )
                {
                    // we have the /username/datestring form of URL
                    mDateString = daystr;
                    mIsDaySpecified = true;
                }               
            }                  
        }
        catch ( Exception ignored )
        {
            mLogger.debug("Exception parsing request params",ignored);
        }
    }

    //------------------------------------------------------------------------
    /** Get HttpServletmRequest that is wrapped by this RollerRequest */
    public PageContext getPageContext()
    {
        return mPageContext;
    }
    
    public void setPageContext(PageContext p)
    {
        mPageContext = p;
    }

    //------------------------------------------------------------------------
    /** Get HttpServletmRequest that is wrapped by this RollerRequest */
    public HttpServletRequest getRequest()
    {
        return mRequest;
    }

    //------------------------------------------------------------------------
    /**
     * Get the RollerRequest object that is stored in the request. Creates
     * RollerRequest if one not found in mRequest.
     */
    public static RollerRequest getRollerRequest( 
        HttpServletRequest r, ServletContext ctx ) throws RollerException
    {
        RollerRequest ret= (RollerRequest)r.getAttribute(ROLLER_REQUEST);
        if ( ret == null )
        {
            ret = new RollerRequest(r,ctx);
            r.setAttribute( ROLLER_REQUEST, ret );
        }
        return ret;
    }

    //------------------------------------------------------------------------
    /**
     * Get the RollerRequest object that is stored in the request. Creates
     * RollerRequest if one not found in mRequest.
     */
    public static RollerRequest getRollerRequest( HttpServletRequest r )
    {
        try
        {
            return getRollerRequest(r, RollerContext.getServletContext());
        }
        catch (Exception e)
        {
            mLogger.debug("Unable to create a RollerRequest", e);
        }
        return null;
    }

    //------------------------------------------------------------------------
    /**
     * Get the RollerRequest object that is stored in the request. Creates
     * RollerRequest if one not found in mRequest.
     */
    public static RollerRequest getRollerRequest( PageContext p )
            throws RollerException {
        
        HttpServletRequest r = (HttpServletRequest)p.getRequest();
        RollerRequest ret = (RollerRequest)r.getAttribute(ROLLER_REQUEST);
        if (ret == null)
        {
                ret = new RollerRequest( p );
                r.setAttribute( ROLLER_REQUEST, ret );
        }
        else
        {
            ret.setPageContext( p );
        }
        return ret;
    }

    //------------------------------------------------------------------------
    /**
     * Get RollerRequest object for the current thread using EVIL MAGIC, 
     * do not use unless you absolutely, positively, cannot use on of the 
     * getRollerRequest() methods.
     */
    public static RollerRequest getRollerRequest()
    {
        return (RollerRequest)mRollerRequestTLS.get();
    }

    //------------------------------------------------------------------------
    /**
     * Get the RollerRequest object that is stored in the requeset.
     * Creates RollerRequest if one not found in mRequest.
     */
    public ServletContext getServletContext()
    {
        return mContext;
    }
    
    //-----------------------------------------------------------------------------
    
    public boolean isDaySpecified()
    {
        return mIsDaySpecified;
    }    
    
    public boolean isMonthSpecified()
    {
        return mIsMonthSpecified;
    }
                
    //--------------------------------------------------- Date specified by request
    
    /**
     * Gets the date specified by the request, or null.
     * @return Date
     */
    public Date getDate()
    {
        return getDate(false);
    }
    
    /**
     * Gets the date specified by the request
     * @param orToday If no date specified, then use today's date.
     * @return Date
     */
    public Date getDate( boolean orToday )
    {
        Date ret = null;
        if ( mDate != null )
        {
            ret = mDate;
        }
        else if ( orToday )
        {
            ret = getToday();
        }
        return ret;
    }
 
    /**
     * Gets the current date based on Website's Locale & Timezone.
     * @return
     */
    private Date getToday()
    {
        Calendar todayCal = Calendar.getInstance();
        if (this.getWebsite() != null)
        {
            todayCal = Calendar.getInstance(
                    this.getWebsite().getTimeZoneInstance(),
                    this.getWebsite().getLocaleInstance());
        }
        todayCal.setTime( new Date() );
        return todayCal.getTime(); 
    }

    /*
     * Gets the YYYYMMDD or YYYYMM date string specified by the request, or null.
     * @return String
     */
    public String getDateString1()
    {
        return mDateString;
    }

    /**
     * Gets the date specified by the request
     * @param orToday If no date specified, then use today's date.
     * @return Date    
    public String getDateString( boolean orToday )
    {
        String ret = null;
        if ( mDateString != null )
        {
            ret = mDateString;
        }
        else if ( orToday )
        {
            Calendar todayCal = Calendar.getInstance();
            if (this.getWebsite() != null)
            {
            	todayCal = Calendar.getInstance(
                        this.getWebsite().getTimeZoneInstance(),
                        this.getWebsite().getLocaleInstance());
            }
            todayCal.setTime( new Date() );
            ret = m8DigitDateFormat.format(todayCal.getTime());            
        }
        return ret;
    }*/
    
    //------------------------------------------------------------------------
    /**
     * Gets the path-info specified by the request, or null.
     * @return String
     */
    public String getPathInfo()
    {
        return mPathInfo;
    }

    //------------------------------------------------------------------------
    /**
     * Gets the page link name specified by the request, or null.
     * @return String
     */
    public String getPageLink()
    {
        return mPageLink;
    }

    public int getWeblogEntryCount()
    {
        // Get count of entries to return, or 20 if null
        int count = 20;
        if ( mRequest.getParameter("count") != null )
        {
            count= Utilities.stringToInt(mRequest.getParameter("count"));
            if ( count==0 || count>50 )
            {
                count = 20;
            } 
        } 
        return count; 
    }         
            
    //------------------------------------------------------------------------
    /**
     * Gets the BookmarkData specified by the request, or null.
     * @return BookmarkData
     */
    public BookmarkData getBookmark( )
    {
        if ( mBookmark == null )
        {
            String id = getFromRequest(BOOKMARKID_KEY);
            if ( id != null )
            {
                try
                {
                    mBookmark = RollerFactory.getRoller()
                        .getBookmarkManager().getBookmark(id);
                }
                catch (RollerException e)
                {
                    mLogger.error("Getting bookmark from request",e);
                }
            }
        }
        return mBookmark;
    }

    //------------------------------------------------------------------------
    /**
     * Gets the WeblogCategoryData specified by the request, or null.
     * @return 
     */
    public WeblogCategoryData getWeblogCategory()
    {
        if ( mWeblogCategory == null )
        {
            String id = getFromRequest(WEBLOGCATEGORYID_KEY);
            if ( id != null )
            {
                try
                {
                    mWeblogCategory = 
                        RollerFactory.getRoller()
                            .getWeblogManager().getWeblogCategory(id);
                }
                catch (RollerException e)
                {
                    mLogger.error("Getting weblog category by id from request",e);
                }
            }
            else if (StringUtils.isNotEmpty(id = getFromRequest(WEBLOGCATEGORYNAME_KEY)))
            {
                try
                {
                    mWeblogCategory = 
                        RollerFactory.getRoller()
                            .getWeblogManager().getWeblogCategoryByPath(
                                    getWebsite(), null, id);
                }
                catch (RollerException e)
                {
                    mLogger.error("Getting weblog category by name from request",e);
                }
            }
        }
        return mWeblogCategory;
    }

    //------------------------------------------------------------------------
    /**
     * Gets the FolderData specified by the request, or null.
     * @return FolderData
     */
    public FolderData getFolder( )
    {
        FolderData folder = null;
        //if ( folder == null )
        //{
            String id = getFromRequest(FOLDERID_KEY);
            if ( id != null )
            {
                try
                {
                    folder = RollerFactory.getRoller()
                        .getBookmarkManager().getFolder(id);
                }
                catch (RollerException e)
                {
                    mLogger.error("Getting folder from request",e);
                }
            }
        //}
        return folder;
    }

    //------------------------------------------------------------------------
    /**
     * Gets the WeblogTemplate specified by the request, or null.
     * @return WeblogTemplate
     */
    public Template getPage()
    {
        if (mPage == null)
        {
            String id = getFromRequest(PAGEID_KEY);
            if ( id != null )
            {
                try
                {
                    mPage = RollerFactory.getRoller()
                        .getUserManager().getPage(id);
                }
                catch (RollerException e)
                {
                    mLogger.error("Getting page from request",e);
                }
            }
        }
        return mPage;
    }
    
    /**
     * Allow comment servlet to inject page that it has chosen.
     */
    public void setPage(org.apache.roller.pojos.Template page) 
    {
        mPage = page;
    }
    
    //------------------------------------------------------------------------
    /**
     * Gets the Request URL specified by the request, or null.
     * @return String
     */
    public String getRequestURL( )
    {
        return mRequest.getRequestURL().toString();
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Gets the Referer URL specified by the request, or null.
     * @return String
     */
    public String getRefererURL( )
    {
        return mRequest.getHeader("referer");
    }
     
    /**
     * Gets the WebsiteData specified in the path info of the request URI, this is 
     * NOT the same thing as the "current website" (i.e. the one that the session's 
     * authenticated user is currently editing).
     * @return WebsiteData object specified by request URI.
     */
    public WebsiteData getWebsite()
    {
        return mWebsite;
    }
    public void setWebsite(WebsiteData wd)
    {
        mWebsite = wd;
    }
    
    /**
     * Gets the WeblogEntryData specified by the request, or null.
     * 
     * Why is this done lazily in the parseRequestParameters() method?
     * 
     * Because: that method is called from init(), which is called from
     * a ServletFilter, and sometimes request parameters are not available
     * in a ServletFiler. They ARE available when the URL points to a JSP,
     * but they ARE NOT available when the URL points to the PageServlet.
     * This may be a Tomcat bug, I'm not sure.
     * 
     * @return WeblogEntryData
     */
    public WeblogEntryData getWeblogEntry( )
    {
        if ( mWeblogEntry == null )
        {        
            // Look for anchor or entry ID that identifies a specific entry 
            String anchor = mRequest.getParameter(ANCHOR_KEY);
            if (anchor == null) anchor = mRequest.getParameter(ANCHOR_KEY_OLD);
            String entryid = mRequest.getParameter(WEBLOGENTRYID_KEY);
            if (entryid == null) 
            {
                entryid = (String)mRequest.getAttribute(WEBLOGENTRYID_KEY);
            }
            try
            {
                if ( entryid != null )
                {
                    WeblogManager weblogMgr = RollerFactory.getRoller().getWeblogManager();
                    mWeblogEntry = weblogMgr.getWeblogEntry(entryid);                
                
                    // We can use entry to find the website, if we don't have one
                    if ( mWeblogEntry != null && mWebsite == null )
                    {
                        mWebsite = mWeblogEntry.getWebsite();
                    }
                } 
                else if ( mWebsite != null && anchor != null )
                {
                    WeblogManager weblogMgr = 
                        RollerFactory.getRoller().getWeblogManager();
                    mWeblogEntry = weblogMgr.getWeblogEntryByAnchor(
                        mWebsite,anchor);
                }                             
            }
            catch (RollerException e)
            {
                mLogger.error("EXCEPTION getting weblog entry",e);
                mLogger.error("anchor=" + anchor);
                mLogger.error("entryid=" + entryid);
            }
            if (mDate == null && mWeblogEntry != null)
            {
                mDate = mWeblogEntry.getPubTime();
                mDateString = DateUtil.format8chars(mDate);
            }
        }           
        return mWeblogEntry;
    }

    //-----------------------------------------------------------------------------

    /** Get attribute from mRequest, and if that fails try session */
    private String getFromRequest( String key )
    {
        String ret = (String)mRequest.getAttribute( key );
        if ( ret == null )
        {
            ret = mRequest.getParameter( key );
            if (ret == null && mRequest.getSession(false) != null)
            {
                ret = (String)mRequest.getSession().getAttribute( key );
            }
        }
        return ret;
    }

    private Date parseDate( String dateString )
    {
        Date ret = null;        
        if (   dateString!=null 
            && dateString.length()==8
            && StringUtils.isNumeric(dateString) )
        {
            ParsePosition pos = new ParsePosition(0);
            ret = m8charDateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
            
            // Do this later, once we know what timezone to use 
            // ret = DateUtil.getEndOfDay(ret);
        } 
        if (   dateString!=null 
            && dateString.length()==6
            && StringUtils.isNumeric(dateString) )
        {
            ParsePosition pos = new ParsePosition(0);
            ret = m6charDateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
            
            // Do this later, once we know what timezone to use 
            // ret = DateUtil.getEndOfMonth(ret); 
        }
        return ret;
    }
    
    /** 
     * @see org.apache.roller.pojos.ParsedRequest#isLinkbackEnabled()
     */
    public boolean isEnableLinkback()
    {
        return RollerRuntimeConfig.getBooleanProperty("site.linkbacks.enabled");
    }

}

