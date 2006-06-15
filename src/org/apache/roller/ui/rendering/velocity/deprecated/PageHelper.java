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
package org.apache.roller.ui.rendering.velocity.deprecated;
import java.net.MalformedURLException;
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
import org.apache.roller.RollerException;
import org.apache.roller.model.WeblogEntryPlugin;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.RefererDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.ui.core.LanguageUtil;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.core.tags.calendar.CalendarModel;
import org.apache.roller.ui.core.tags.calendar.CalendarTag;
import org.apache.roller.ui.core.tags.menu.EditorNavigationBarTag;
import org.apache.roller.ui.core.tags.menu.MenuTag;
import org.apache.roller.ui.authoring.tags.BigWeblogCalendarModel;
import org.apache.roller.ui.authoring.tags.WeblogCalendarModel;
import org.apache.roller.util.StringUtils;
import org.apache.velocity.VelocityContext;

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
    
    private Map              mVelocityContext = null;
    private PageContext          mPageContext = null;
    private HttpServletResponse  mResponse = null;     
    private RollerRequest        mRollerReq = null;  
    private Map                  mPagePlugins = null;  // Plugins keyed by name   
    private WebsiteData          mWebsite = null;
    
    //------------------------------------------------------------------------
    
    /**
     * Initialize VelocityHelper, setting the variables it will be hiding from
     * the Velocimacros.
     */
    public PageHelper( 
            HttpServletRequest request, 
            HttpServletResponse response, 
            Map ctx) throws RollerException
    {
        mVelocityContext = ctx;
        mRollerReq = RollerRequest.getRollerRequest(request);
        mResponse = response;
        if (mRollerReq != null) 
        {
            mPageContext = mRollerReq.getPageContext();
            if (request.getAttribute(RollerRequest.OWNING_WEBSITE) != null)
            {
                mWebsite = (WebsiteData)
                    request.getAttribute(RollerRequest.OWNING_WEBSITE);
            }
            else if (mRollerReq.getWebsite() != null )
            {
                mWebsite = mRollerReq.getWebsite();
            }
        }
        if (mVelocityContext == null) mVelocityContext = new HashMap();
        Roller roller = RollerFactory.getRoller(); 
        PluginManager ppmgr = roller.getPagePluginManager();
        mPagePlugins = ppmgr.createAndInitPagePlugins(mWebsite, ctx);
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
                params.put( RollerRequest.WEBLOG_KEY, mWebsite.getHandle());
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
            editorTag.setView("templates/navbar/navbar-vertical.vm");
        }
        else
        {
            editorTag.setView("templates/navbar/navbar-horizontal.vm");
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
        if (OldWeblogPageModel.VELOCITY_NULL.equals(cat)) cat = null;
        String ret = null;
        try
        {
            HttpServletRequest request =
                (HttpServletRequest) mRollerReq.getRequest();
            HttpServletResponse response = mResponse;

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
     * @param entry Entry being rendered.
     * @param str   String to which plugins are to be applied.
     * @return      Result of applying plugins to str.
     */
    public String renderPlugins(WeblogEntryDataWrapper entry, String str)
    {
        String ret = str;
        mLogger.debug("Applying page plugins to string");
                
        if (mPagePlugins != null)
        {
            List entryPlugins = entry.getPluginsList();
            // if no Entry plugins, don't bother looping.
            if (entryPlugins != null && !entryPlugins.isEmpty())
            {    
                // need to do this to tell ReadMore not to do its job
                // if we are in the "view one Entry" page.
                boolean singleEntry = false;
                if (mRollerReq == null || mRollerReq.getWeblogEntry() != null)
                {
                    singleEntry = true;
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
                        WeblogEntryPlugin pagePlugin = (WeblogEntryPlugin)mPagePlugins.get(key);
                        try {
                            ret = pagePlugin.render(entry.getPojo(), ret);
                        } catch (Throwable t) {
                            mLogger.error("ERROR from plugin: " + pagePlugin.getName(), t);
                        }
                    }
                }
            }
        }
        
        return ret;
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
            (Locale) mRollerReq.getRequest().getSession().getAttribute(Globals.LOCALE_KEY);
        if (currentLocale==null) 
        {
            currentLocale = mRollerReq.getRequest().getLocale();
        }
            
        Locale[] supportedLanguages =
            LanguageUtil.getSupportedLanguages(RollerContext.getServletContext());
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
        /* no longer used -- Allen G
        RollerContext rctx = 
            RollerContext.getRollerContext(mRollerReq.getRequest());
        return rctx.getCommentAuthenticator().getHtml(
            mVelocityContext, mRollerReq.getRequest(), mResponse);
         */
        
        return "";
    }
        
}
