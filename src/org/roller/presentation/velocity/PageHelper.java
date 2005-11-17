package org.roller.presentation.velocity;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.util.RequestUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.wrapper.RefererDataWrapper;
import org.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.roller.presentation.LanguageUtil;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.tags.calendar.CalendarModel;
import org.roller.presentation.tags.calendar.CalendarTag;
import org.roller.presentation.tags.menu.EditorNavigationBarTag;
import org.roller.presentation.tags.menu.MenuTag;
import org.roller.presentation.weblog.tags.BigWeblogCalendarModel;
import org.roller.presentation.weblog.tags.WeblogCalendarModel;
import org.roller.util.StringUtils;

/**
 * Provides assistance to VelociMacros, filling in where Velocity falls.
 * 
 * @author llavandowska
 * @author David M Johnson
 * 
 */
public class PageHelper
{
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(PageHelper.class);
    
    private Context              mVelocityContext = null;
    private PageContext          mPageContext = null;
    private HttpServletResponse  mResponse = null;     
    private RollerRequest        mRollerReq = null;  
    private Map                  mPagePlugins = new HashMap();  // Plugins keyed by name   
    private boolean              mSkipFlag = false;
    private WebsiteData          mWebsite = null;
    
    //------------------------------------------------------------------------
    
    /**
     * Initialize VelocityHelper, setting the variables it will be hiding from
     * the Velocimacros.
     */
    public PageHelper(
            RollerRequest rreq, HttpServletResponse response, Context ctx)
    {
        mVelocityContext = ctx;
        mRollerReq = rreq;
        mResponse = response;
        if (rreq != null) 
        {
            mPageContext = rreq.getPageContext();
            if ( rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE) != null)
            {
                mWebsite = (WebsiteData)
                    rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE);
            }
            else if ( rreq.getWebsite() != null )
            {
                mWebsite = rreq.getWebsite();
            }
        }
        
        if (mVelocityContext == null) mVelocityContext = new VelocityContext();
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Initialized VelocityHelper without a Velocity Context.
     */
    public PageHelper(RollerRequest rreq, HttpServletResponse response)
    {
        this(rreq, response, null);
    }

    //------------------------------------------------------------------------
    /**
     * Return a PageHelper with a new VelocityContext, 
     * added to support the ApplyPlugins JSP tag.
     */
    public static PageHelper createPageHelper(
        HttpServletRequest request, HttpServletResponse response)
    {
        Context ctx = (Context)(new VelocityContext());
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        PageHelper pageHelper = new PageHelper(rreq, response, ctx);
        pageHelper.initializePlugins(ContextLoader.getPagePluginClasses());

        return pageHelper;
    }

    //------------------------------------------------------------------------
    /**
     * Create individual instances of the PagePlugins for use by this PageHelper.
     */
    protected void initializePlugins(Map pluginClasses) 
    {
        Iterator it = pluginClasses.values().iterator();
        while (it.hasNext()) 
        {
            try
            {
                Class pluginClass = (Class)it.next();
                PagePlugin plugin = (PagePlugin)pluginClass.newInstance();
                plugin.init(mRollerReq, mVelocityContext);
                mPagePlugins.put(plugin.getName(), plugin);
            }
            catch (Exception e)
            {
                mLogger.warn("Unable to init() PagePlugin: ", e    );
            }
        }
    }

    //------------------------------------------------------------------------
    /**
     * This is a quasi-hack: there are places we don't want to render the
     * ReadMore Plugin in particular (I cannot think of other plugins
     * which warrant this treatment).  The "skip flag" will be made
     * available to the Plugin if it wants to check to see if it should
     * be skipped.
     */
    public void setSkipFlag(boolean skip) 
    {
        mSkipFlag = skip;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Another stupid helper method to make up for the shortcomings of Velocity.
     * @return HashMap
     */
    public Hashtable addParam(String key, String value, Hashtable map)
    {
        if (map == null) map = new Hashtable();
        if (key != null && value != null)
            map.put(key, value);
        return map;
    }
        
    //------------------------------------------------------------------------
    
    /**
     * Evaluates the String as a Velocimacro, returning the results.
     *
     * @deprecated shouldn't be used anymore because it's dangerous
     * 
     * @param str String
     * @return String
     */
    public String evaluateString(String str)
    {
        // we no longer allow users to do this because it is dangerous
        return str;
    }
   
    /** Build the URL for editing an WeblogEntry **/
    public String getEntryEditUrl(WeblogEntryDataWrapper entry)
    {
        Hashtable params = new Hashtable();
        params.put( RollerRequest.WEBLOGENTRYID_KEY, entry.getId());
        params.put( RollerRequest.ANCHOR_KEY,        entry.getAnchor());
        if (mWebsite != null)
        {    
            params.put( RollerRequest.USERNAME_KEY,  mWebsite.getHandle());
        }
        try
        {
            return RequestUtils.computeURL( mPageContext,
                "weblogEdit", null, null, null, params, null, false);
        }
        catch (MalformedURLException mue)
        {
            mLogger.warn("RollerRequest.editEntryUrl exception: ", mue);
        }
        return 
           mRollerReq.getRequest().getContextPath() + "edtior/weblog.do?method=edit";
    }
    
    //-------------------------------------------------------------------------
    public String getToggleLinkbackDisplayHTML(RefererDataWrapper referer)
    {
        String ret = "";
        String link = null;
        try
        {
            RollerSession rollerSession = 
                RollerSession.getRollerSession(mRollerReq.getRequest());
            if ( mRollerReq.getWebsite() != null 
              && rollerSession.isUserAuthorizedToAdmin(mRollerReq.getWebsite()))
            {
                Hashtable params = new Hashtable();
                params.put( RollerRequest.REFERERID_KEY, referer.getId());
                params.put( RollerRequest.USERNAME_KEY, mWebsite.getHandle());
                link = RequestUtils.computeURL( mPageContext,
                    "toggleLinkback", null, null, null, params,null,false);
                    
                StringBuffer sb = new StringBuffer();
                sb.append("[<a href=\"");
                sb.append(link);
                if ( referer.getVisible().booleanValue() )
                {
                    sb.append("\">Visible</a>] ");
                }
                else
                {
                    sb.append("\">Hidden</a>] ");
                }
                ret = sb.toString();
            }
        }
        catch (Exception e)
        {
           // should never happen, but if it does:
           mLogger.error("ERROR creating toggle-linkback URL",e);
        }
        
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    public boolean isUserAuthorizedToEdit()
    {
        try
        {
            RollerSession rses = 
                RollerSession.getRollerSession(mRollerReq.getRequest());
            if ( rses.getAuthenticatedUser() != null 
                    && mRollerReq.getWebsite() != null)
            {
                return rses.isUserAuthorizedToAdmin(mRollerReq.getWebsite());
            }
        }
        catch (Exception e)
        {
            mLogger.warn("PageHelper.isUserAuthorizedToEdit)", e);
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    public void setContentType( String type )
    {
        mResponse.setContentType(type);
    }

    //------------------------------------------------------------------------
    /** 
     * Display big weblog calendar, well suited for an archive page.
     * @return HTML for calendar.
     */
    public String showBigWeblogCalendar()
    {
        return showWeblogCalendar(true, null);
    }
        
    //------------------------------------------------------------------------
    
    /** 
     * Call hybrid EditorNavBarTag to render editor navbar.
     * @param vertical True for vertical navbar.
     * @return String HTML for navbar.
     */
    public String showEditorNavBar(boolean vertical)
    {
        EditorNavigationBarTag editorTag = new EditorNavigationBarTag();
        editorTag.setPageContext(mPageContext);
        if ( vertical )
        {
            editorTag.setView("/navbar-vertical.vm");
        }
        else
        {
            editorTag.setView("/navbar-horizontal.vm");
        }
        editorTag.setModel("editor-menu.xml");
        return editorTag.emit();
    }
    
    //------------------------------------------------------------------------
    
    /** 
     * Call hybrid EditorNavBarTag to render editor navbar.
     * @param model Name of XML file in WEB-INF that contains XML for menu.
     * @param template Name of Velocity template in classpath to display menu.
     * @return String HTML for menu.
     */
    public String showMenu(String model, String template)
    {
        MenuTag menuTag = new MenuTag();
        menuTag.setPageContext(mPageContext);
        menuTag.setModel(model);
        menuTag.setView(template);
        return menuTag.emit();
    }
    
    //------------------------------------------------- WeblogCalendar methods

    /** 
     * Display weblog calendar.
     * @return HTML for calendar.
     */
    public String showWeblogCalendar()
    {
        return showWeblogCalendar(false, null);
    }

    //------------------------------------------------------------------------
    /** 
     * Weblog calendar display implementation.
     * @param big Show big archive style calendar.
     * @return HTML for calendar.
     */
    public String showWeblogCalendar( boolean big, String cat )
    {
        if (PageModel.VELOCITY_NULL.equals(cat)) cat = null;
        String ret = null;
        try
        {
            HttpServletRequest request =
                (HttpServletRequest)mPageContext.getRequest();
            HttpServletResponse response =
                (HttpServletResponse)mPageContext.getResponse();

            String selfUrl = null;
            String pageLink = mRollerReq.getPageLink();
            if ( pageLink != null )
            {
                selfUrl = request.getContextPath() + "/page/" 
                          + mWebsite.getHandle() + "/"+pageLink;
            }
            else
            {
                selfUrl = request.getContextPath()+"/page/" + mWebsite.getHandle();
            }

            // setup weblog calendar model
            CalendarModel model = null;
            if ( big )
            {
                model = new BigWeblogCalendarModel(
                           mRollerReq, response, selfUrl, cat);
            }
            else
            {
                model = new WeblogCalendarModel(
                            mRollerReq, response, selfUrl, cat);
            }

            // save model in JSP page context so CalendarTag can find it
            mPageContext.setAttribute("calendarModel",model);

            // Create and setup calendar tag
            CalendarTag calTag = new CalendarTag();
            calTag.setPageContext(mPageContext);
            calTag.setName("calendar");
            calTag.setModel("calendarModel");
            //calTag.setLocale(mRollerReq.getWebsite().getLocaleInstance());
            calTag.setLocale(LanguageUtil.getViewLocale(request));
            //calTag.setTimeZone(mRollerReq.getWebsite().getTimeZoneInstance());
            if ( big )
            {
                calTag.setClassSuffix("Big");
            }
            ret = calTag.emit();
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception",e);
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Convenience method, contrived helper for Velocity.
     * @param useIds
     * @param isAction
     * @param path
     * @param val1
     * @param val2
     * @return String
     */
    public String strutsUrlHelper( boolean useIds, boolean isAction, 
        String path, String val1, String val2)
    {
        Hashtable params = new Hashtable();
        return strutsUrlHelper1( useIds, isAction, path, val1, val2, params);
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Very contrived helper method for Velocimacros generating Struts links.
     * This is really only of use to the showNavBar macro.
     * @param useIds
     * @param isAction
     * @param path
     * @param val1
     * @param val2
     * @return String
     */
    public String strutsUrlHelper1( boolean useIds, boolean isAction, 
        String path, String val1, String val2, Hashtable params)
    {
        if (useIds)
        {
            if (mRollerReq.getFolder() != null) 
            {
                params.put(RollerRequest.FOLDERID_KEY,
                mRollerReq.getFolder().getId());
            } 
            if (mWebsite != null)
            {
                params.put(RollerRequest.WEBLOG_KEY, mWebsite.getHandle());
            }
        }
        
        if (StringUtils.isNotEmpty(val1) && !val1.equals("null"))
        {
            params.clear();
            params.put("weblog", val1);
        }
        
        String returnUrl = "";
        try
        {
            if (isAction)
            {
                returnUrl = RequestUtils.computeURL( mPageContext,
                                path, null, null, null, params, null, false);
            }
            else
            {
                returnUrl = RequestUtils.computeURL( mPageContext,
                                null, path, null, null, params, null, false);
            }
        }
        catch (MalformedURLException mue)
        {
            mLogger.warn("RollerRequest.strutsUrlHelper exception: ", mue);
            returnUrl = "<span class=\"error\">ERROR generating link</span>";
        }
        return returnUrl;
    }
        
    /**
     * Pass the String through any PagePlugins that have been
     * assigned to the PageHelper, as selected by the Entry.
     * 
     * @param str
     * @return
     */
    public String renderPlugins(WeblogEntryDataWrapper entry)
    {
        mLogger.debug("Rendering page plugins on WeblogEntryData");
        
        // we have to make a copy to temporarily store the
        // changes wrought by Plugins (otherwise they might
        // end up persisted!).
        WeblogEntryData copy = new WeblogEntryData(entry.getPojo());
        
        if (mPagePlugins != null)
        {
            List entryPlugins = copy.getPluginsList();
            // if no Entry plugins, don't bother looping.
            if (entryPlugins != null && !entryPlugins.isEmpty())
            {    
                // need to do this to tell ReadMore not to do its job
                // if we are in the "view one Entry" page.
                if (mRollerReq == null || mRollerReq.getWeblogEntry() != null)
                {
                    mSkipFlag = true;
                }
                
                // now loop over mPagePlugins, matching
                // against Entry plugins (by name):
                // where a match is found render Plugin.
                Iterator iter = mPagePlugins.keySet().iterator();
                while (iter.hasNext())
                {
                    String key = (String)iter.next();
                    if (entryPlugins.contains(key))
                    {
                        PagePlugin pagePlugin = (PagePlugin)mPagePlugins.get(key);
                        copy.setText((pagePlugin).render(copy, mSkipFlag));
                    }
                }
            }
        }
        
        return copy.getText();
    }
    
    /**
     * This method returns an array of Locales for each supported
     * language available, with the exeception of the language of the 
     * current locale, if that language is supported.
     * 
     * So, if English and Dutch are supported, and the current Locale is Dutch,
     * only English is returned. If the current Locale is Spanish, both English and Dutch are
     * returned.
     *    
     * @return
     */
    public Locale[] getSupportedLanguages()
    {
        Locale currentLocale =
            (Locale) mPageContext.getSession().getAttribute(Globals.LOCALE_KEY);
        if (currentLocale==null) 
        {
            currentLocale = mPageContext.getRequest().getLocale();
        }
            
        Locale[] supportedLanguages =
            LanguageUtil.getSupportedLanguages(mPageContext.getServletContext());
        if (supportedLanguages==null) {
            return null;
        }
        
        // filter out the current selected language
        Vector result = new Vector();
        for (int i = 0; i < supportedLanguages.length; i++)
        {
            if (currentLocale == null
                || (!supportedLanguages[i].equals(currentLocale)
                && !supportedLanguages[i].equals(
                    new Locale(currentLocale.getLanguage())))
                )
            {
                result.add(supportedLanguages[i]);
            }
        }
        return (Locale[]) result.toArray(new Locale[result.size()]);
    }

    /**
     * @return relative URL to page, starting with /username
     */ 
    public String getPathInfo() 
    {
        return mRollerReq.getPathInfo();
    }
    
    public String getCommentAuthenticatorHtml()
    {
        RollerContext rctx = 
            RollerContext.getRollerContext(mRollerReq.getRequest());
        return rctx.getCommentAuthenticator().getHtml(
            mVelocityContext, mRollerReq.getRequest(), mResponse);
    }
}
