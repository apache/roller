
package org.roller.presentation;

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
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.ParsedRequest;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.PingTargetData;
import org.roller.util.DateUtil;
import org.roller.util.Utilities;
 
//////////////////////////////////////////////////////////////////////////////
/**
 * Access to objects and values specified by request. Parses out arguments from
 * request URL needed for various parts of Roller and makes them available via
 * getter methods.
 * <br/><br/> 
 * 
 * These forms of pathinfo get special support:
 * <br/><br/>
 * 
 * <pre>
 * [username] - get default page for user for today's date 
 * [username]/[date] - get default page for user for specified date 
 * [username]/[pagelink] - get specified page for today's date 
 * [username]/[pagelink]/[date] - get specified page for specified date
 * [username]/[pagelink]/[anchor] - get specified page & entry (by anchor)
 * [username]/[pagelink]/[date]/[anchor] - get specified page & entry (by anchor)
 * </pre>
 *  
 * @author David M Johnson
 */
public class RollerRequest implements ParsedRequest
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
    private PageData           mPage;
    private PageContext        mPageContext = null;
    private HttpServletRequest mRequest = null;
    private WebsiteData        mWebsite;
    private WeblogEntryData    mWeblogEntry;
    private WeblogCategoryData mWeblogCategory;
    private boolean           mIsDateSpecified = false;
        
    private static ThreadLocal mRollerRequestTLS = new ThreadLocal();
    
    public static final String ANCHOR_KEY             = "entry";
    public static final String ANCHOR_KEY_OLD         = "anchor";
    public static final String USERNAME_KEY           = "username";
    public static final String WEBSITEID_KEY          = "websiteid";
    public static final String FOLDERID_KEY           = "folderid";
    public static final String PARENTID_KEY           = "parentid";
    public static final String NEWSFEEDID_KEY         = "feedid";
    public static final String PAGEID_KEY             = "pageid";
    public static final String PAGELINK_KEY           = "pagelink";
    public static final String PINGTARGETID_KEY       = "pingtargetid";
    public static final String EXCERPTS_KEY           = "excerpts";
    public static final String BOOKMARKID_KEY         = "bookmarkid";
    public static final String REFERERID_KEY          = "refid";
    public static final String WEBLOGENTRYID_KEY      = "entryid";
    public static final String WEBLOGENTRY_COUNT      = "count";
    public static final String WEBLOGCATEGORYNAME_KEY = "catname";
    public static final String WEBLOGCATEGORYID_KEY   = "catid";
    public static final String WEBLOGENTRIES_KEY      = "entries";
    public static final String WEBLOGDAY_KEY          = "day";
    public static final String WEBLOGCOMMENTID_KEY    = "catid";
    public static final String LOGIN_COOKIE           = "sessionId";
    
    public static final String OWNING_USER            = "OWNING_USER";
    
    private static final String ROLLER_REQUEST        = "roller_request";
    
    private SimpleDateFormat mFmt = DateUtil.get8charDateFormat();

    //----------------------------------------------------------- Constructors

    /** Construct Roller request for a Servlet request */
    public RollerRequest( HttpServletRequest req, ServletContext ctx )
        throws RollerException
    {
        mRequest = req;
        mContext = ctx;
        init();
    }
    
    //------------------------------------------------------------------------
    /** Convenience */
    public RollerRequest( ServletRequest req, ServletContext ctx ) 
        throws RollerException
    {
        mRequest = (HttpServletRequest)req;
        mContext = ctx;
        init();
    }
    
    //------------------------------------------------------------------------
    public RollerRequest( PageContext pCtx) throws RollerException
    {
        mRequest = (HttpServletRequest) pCtx.getRequest();
        mContext = pCtx.getServletContext();
        mPageContext = pCtx;
        init();
    }

    //------------------------------------------------------------------------
    private void init() throws RollerException
    {
        mRollerRequestTLS.set(this);
        if (mRequest.getContextPath().equals("/atom"))
        {
            return; // Atom servlet request needs no init
        }
        
        // Bind persistence session to authenticated user, if we have one
        RollerContext rctx = RollerContext.getRollerContext(mContext); 
        Authenticator auth = rctx.getAuthenticator();
        String userName = auth.getAuthenticatedUserName(mRequest);
        if (userName != null)
        {
            UserManager userMgr = getRoller().getUserManager();
            UserData currentUser = userMgr.getUser(userName);
            getRoller().setUser(currentUser);
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
            UserManager userMgr = getRoller().getUserManager();
            mWebsite = userMgr.getWebsite(pathInfo[0]);
            if (mWebsite != null)
            {
                if ( pathInfo.length == 1 )
                {
                    // we have the /username form of URL
                    mDate = getDate(true);
                    mDateString = DateUtil.format8chars(mDate);
                    mPage = userMgr.retrievePage(mWebsite.getDefaultPageId());
                }
                else if ( pathInfo.length == 2 )
                {
                    mDate = parseDate(pathInfo[1]);
                    if ( mDate == null ) // pre-jdk1.4 --> || mDate.getYear() <= 70 )
                    {
                        // we have the /username/pagelink form of URL
                        mDate = getDate(true);
                        mDateString = DateUtil.format8chars(mDate);
                        mPageLink = pathInfo[1];
                        mPage = userMgr.getPageByLink(mWebsite, pathInfo[1]);
                    }
                    else
                    {
                        // we have the /username/datestring form of URL
                        mDateString = pathInfo[1];
                        mPage = userMgr.retrievePage(mWebsite.getDefaultPageId());
                        mIsDateSpecified = true;
                    }               
                }
                else if ( pathInfo.length == 3 )
                {
                    mPageLink = pathInfo[1];
                    mPage = userMgr.getPageByLink(mWebsite, pathInfo[1]);
                    
                    mDate = parseDate(pathInfo[2]);
                    if ( mDate == null ) // pre-jdk1.4 --> || mDate.getYear() <= 70 )
                    {
                        // we have the /username/pagelink/anchor form of URL
                        try
                        {
                            WeblogManager weblogMgr = getRoller().getWeblogManager();
                            mWeblogEntry = weblogMgr.getWeblogEntryByAnchor(
                                mWebsite, pathInfo[2]);
                            mDate = mWeblogEntry.getPubTime();
                            mDateString = DateUtil.format8chars(mDate);
                        }
                        catch (Exception damn)
                        {
                            // doesn't really matter, we've got the Page anyway
                            mLogger.debug("Damn", damn);
                        }
                    }
                    else
                    {
                        // we have the /username/pagelink/datestring form of URL
                        mDateString = pathInfo[2];
                        mIsDateSpecified = true;
                    }
                }
                else if ( pathInfo.length == 4 )
                {
                    // we have the /username/pagelink/datestring/anchor form of URL
                    mPageLink = pathInfo[1];
                    mPage = userMgr.getPageByLink(mWebsite, pathInfo[1]);
                    
                    mDate = parseDate(pathInfo[2]);
                    mDateString = pathInfo[2];
                    mIsDateSpecified = true;

                    // we have the /username/pagelink/date/anchor form of URL
                    WeblogManager weblogMgr = getRoller().getWeblogManager();
                    mWeblogEntry = weblogMgr.getWeblogEntryByAnchor(
                                    mWebsite, pathInfo[3]);
                }                
            }
        }
        catch ( Exception ignored )
        {
            mLogger.debug("Exception parsing pathInfo",ignored);
        }
        
        if ( mWebsite==null || mDate==null || mPage==null )
        {            
            String msg = "Invalid pathInfo: "+StringUtils.join(pathInfo,"|");
            mLogger.info(msg);                       
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
            UserManager userMgr = getRoller().getUserManager();            
            String userName = mRequest.getParameter(USERNAME_KEY);
            if ( userName == null )
            {
                // then try remote user
                userName = mRequest.getRemoteUser(); 
            }
            
            if ( userName != null )
            {
                mWebsite = userMgr.getWebsite(userName);
            }
            
            // Look for page ID in request params
            String pageId = mRequest.getParameter(RollerRequest.PAGEID_KEY);                    
            if ( pageId != null )
            {
                mPage = userMgr.retrievePage(pageId);
                
                // We can use page to find the user, if we don't have one yet
                if ( mWebsite == null )
                {
                    mWebsite = mPage.getWebsite();
                }                    
            }
            else if (mWebsite != null) 
            {
                mPage = userMgr.retrievePage( mWebsite.getDefaultPageId() );
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
                    mIsDateSpecified = true;
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
    {
        HttpServletRequest r = (HttpServletRequest)p.getRequest();
        RollerRequest ret = (RollerRequest)r.getAttribute(ROLLER_REQUEST);
        if (ret == null)
        {
            try
            {
                ret = new RollerRequest( p );
                r.setAttribute( ROLLER_REQUEST, ret );
            }
            catch (Exception e)
            {
                mLogger.debug("Unable to create a RollerRequest", e);
            }
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

    //------------------------------------------------------------------------
    /** Get Roller instance from */
    public Roller getRoller()
    {
        return RollerContext.getRoller( mRequest );
    }
    
    //------------------------------------------------------------------------
    /** Is mRequest's user the admin user? */
    public boolean isAdminUser() throws RollerException
    {
        UserData user = getUser();
        if (user != null && user.hasRole("admin")) 
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //------------------------------------------------------------------------
    /** Is mRequest's user authorized to edit the mRequested resource */
    public boolean isUserAuthorizedToEdit() throws RollerException
    {
        // Make sure authenticated user is the owner of this item
        // Session's principal's name must match user name in mRequest
        
        RollerContext rctx = RollerContext.getRollerContext(mContext); 
        Authenticator auth = rctx.getAuthenticator();
        
        String userName = auth.getAuthenticatedUserName(mRequest);
            
        // TODO: A hack to be replaced by Object.canEdit()
        UserData owningUser = null;
        if (mRequest.getAttribute(OWNING_USER) != null)
        {
            owningUser = (UserData)mRequest.getAttribute(OWNING_USER);
        }
        else
        {
            owningUser = getUser(); 
        }
        
        if (    userName != null 
             && owningUser != null
             && userName.equalsIgnoreCase( owningUser.getUserName() )
             && auth.isAuthenticatedUserInRole(mRequest,"editor"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //------------------------------------------------------------------------
    /**
     * Get user by name.
     */
    public UserData getUser( String userName ) throws Exception
    {
        return getRoller().getUserManager().getUser(userName);
    }

    //------------------------------------------------------------------------
    
    public boolean isDateSpecified()
    {
        return mIsDateSpecified;
    }
            
    //------------------------------------------------------------------------
    
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
     * Gets the date specified by the request, or null.
     * @return Date
     */
    public Date getDate()
    {
        return getDate(false);
    }
    
    //------------------------------------------------------------------------
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

    //------------------------------------------------------------------------
    /**
     * Gets the YYYYMMDD date string specified by the request, or null.
     * @return String
     */
    public String getDateString()
    {
        return getDateString(false);
    }

    //------------------------------------------------------------------------
    /**
     * Gets the date specified by the request
     * @param orToday If no date specified, then use today's date.
     * @return Date
     */
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
            ret = mFmt.format(todayCal.getTime());            
        }
        return ret;
    }
    
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
                    mBookmark = 
                        getRoller().getBookmarkManager().retrieveBookmark(id);
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
                        getRoller().getWeblogManager().retrieveWeblogCategory(id);
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
                        getRoller().getWeblogManager().getWeblogCategoryByPath(
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
                    folder = 
                        getRoller().getBookmarkManager().retrieveFolder(id);
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
     * Gets the PageData specified by the request, or null.
     * @return PageData
     */
    public PageData getPage()
    {
        if (mPage == null)
        {
            String id = getFromRequest(PAGEID_KEY);
            if ( id != null )
            {
                try
                {
                    mPage = getRoller().getUserManager().retrievePage(id);
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
    public void setPage(PageData page) 
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
     * Gets the UserData specified by the request, or null.
     * @return UserData
     */
    public UserData getUser()
    {
        if (mWebsite != null) return mWebsite.getUser();
        return null;
    }

    /**
     * Gets the WebsiteData specified by the request, or null.
     * @return WeblogCategory
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
                    WeblogManager weblogMgr = getRoller().getWeblogManager();
                    mWeblogEntry = weblogMgr.retrieveWeblogEntry(entryid);                
                
                    // We can use entry to find the website, if we don't have one
                    if ( mWeblogEntry != null && mWebsite == null )
                    {
                        mWebsite = mWeblogEntry.getWebsite();
                    }
                } 
                else if ( mWebsite != null && anchor != null )
                {
                    WeblogManager weblogMgr = getRoller().getWeblogManager();
                    mWeblogEntry = weblogMgr.getWeblogEntryByAnchor(
                        mWebsite,anchor);
                }                             
            }
            catch (RollerException e)
            {
                mLogger.error("EXCEPTION getting weblog entry",e);
                mLogger.error("user=" + mWebsite.getUser());
                mLogger.error("anchor=" + anchor);
                mLogger.error("entryid=" + entryid);
            }
        }           
        return mWeblogEntry;
    }

    //------------------------------------------------------------------------

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

    //------------------------------------------------------------------------

    private Date parseDate( String dateString )
    {
        Date ret = null;        
        if (   dateString!=null 
            && dateString.length()==8
            && StringUtils.isNumeric(dateString) )
        {
            ParsePosition pos = new ParsePosition(0);
            ret = mFmt.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
            
            // since a specific date was requested set time to end of day
            ret = DateUtil.getEndOfDay(ret);
        }
        return ret;
    }
    
    //------------------------------------------------------------------------

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
     * @see org.roller.pojos.ParsedRequest#isLinkbackEnabled()
     */
    public boolean isEnableLinkback()
    {
        return RollerRuntimeConfig.getBooleanProperty("site.linkbacks.enabled");
    }
}

