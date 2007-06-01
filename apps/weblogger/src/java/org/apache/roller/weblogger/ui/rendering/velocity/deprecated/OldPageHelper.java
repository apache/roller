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

package org.apache.roller.weblogger.ui.rendering.velocity.deprecated;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.PluginManager;
import org.apache.roller.weblogger.business.Roller;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.RefererDataWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.core.tags.calendar.CalendarModel;
import org.apache.roller.weblogger.ui.core.tags.calendar.CalendarTag;
import org.apache.roller.weblogger.ui.core.tags.menu.EditorNavigationBarTag;
import org.apache.roller.weblogger.ui.core.tags.menu.MenuTag;
import org.apache.roller.weblogger.ui.core.tags.calendar.BigWeblogCalendarModel;
import org.apache.roller.weblogger.ui.core.tags.calendar.WeblogCalendarModel;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.util.URLUtilities;

/**
 * Provides assistance to VelociMacros, filling in where Velocity falls.
 */
public class OldPageHelper {
    
    private static Log mLogger = LogFactory.getLog(OldPageHelper.class);
    
    private PageContext mPageContext = null;
    private HttpServletRequest mRequest = null;
    private HttpServletResponse mResponse = null;
    
    private Map mPagePlugins = null;  // Plugins keyed by name
    private Weblog mWebsite = null;
    private Date mDate = null;
    private WeblogBookmarkFolder mFolder = null;
    private String mPageName = null;
    private WeblogPageRequest mPageRequest = null;
    
    /**
     * Initialize VelocityHelper, setting the variables it will be hiding from
     * the Velocimacros.
     */
    public OldPageHelper(HttpServletRequest request,
                      HttpServletResponse response,
                      Map ctx,
                      Weblog website,
                      Date date,WeblogBookmarkFolder folder,
                      String pageName,
                      PageContext pageContext,
                      WeblogPageRequest pageRequest) throws RollerException {
        
        // general request objects
        mRequest = request;
        mResponse = response;
        mPageContext = pageContext;
        mPageRequest = pageRequest;
        
        // data that we'll be reusing
        mWebsite = website;
        mDate = date;
        mFolder = folder;
        
        // init plugins
        Roller roller = RollerFactory.getRoller();
        PluginManager ppmgr = roller.getPagePluginManager();
        mPagePlugins = ppmgr.getWeblogEntryPlugins(mWebsite);
    }
    
    
    /**
     * Another stupid helper method to make up for the shortcomings of Velocity.
     * @return HashMap
     */
    public Hashtable addParam(String key, String value, Hashtable map) {
        if (map == null) map = new Hashtable();
        if (key != null && value != null)
            map.put(key, value);
        return map;
    }
    
    
    /**
     * Evaluates the String as a Velocimacro, returning the results.
     *
     * @deprecated shouldn't be used anymore because it's dangerous
     *
     * @param str String
     * @return String
     */
    public String evaluateString(String str) {
        // we no longer allow users to do this because it is dangerous
        return str;
    }
    
    
    /** Build the URL for editing an WeblogEntry **/
    public String getEntryEditUrl(WeblogEntryDataWrapper entry) {
        return URLUtilities.getEntryEditURL(entry.getWebsite().getHandle(), entry.getId(), false);
    }
    
    
    public String getToggleLinkbackDisplayHTML(RefererDataWrapper referer) {
        // NOTE: this was EOLed as part of Roller 4.0 since we no longer
        // have an action for toggling linkback display
        return "";
    }
    
    
    public boolean isUserAuthorizedToEdit() {
        try {
            RollerSession rses =
                    RollerSession.getRollerSession(mRequest);
            if ( rses.getAuthenticatedUser() != null
                    && mWebsite != null) {
                return rses.isUserAuthorizedToAdmin(mWebsite);
            }
        } catch (Exception e) {
            mLogger.warn("PageHelper.isUserAuthorizedToEdit)", e);
        }
        return false;
    }
    
    
    public void setContentType( String type ) {
        mResponse.setContentType(type);
    }
    
    
    /**
     * Display big weblog calendar, well suited for an archive page.
     * @return HTML for calendar.
     */
    public String showBigWeblogCalendar() {
        return showWeblogCalendar(true, null);
    }
    
    
    /**
     * Call hybrid EditorNavBarTag to render editor navbar.
     * @param vertical True for vertical navbar.
     * @return String HTML for navbar.
     */
    public String showEditorNavBar(boolean vertical) {
        EditorNavigationBarTag editorTag = new EditorNavigationBarTag();
        editorTag.setPageContext(mPageContext);
        if ( vertical ) {
            editorTag.setView("templates/navbar/navbar-vertical.vm");
        } else {
            editorTag.setView("templates/navbar/navbar-horizontal.vm");
        }
        editorTag.setModel("editor-menu.xml");
        return editorTag.emit();
    }
    
    
    /**
     * Call hybrid EditorNavBarTag to render editor navbar.
     * @param model Name of XML file in WEB-INF that contains XML for menu.
     * @param template Name of Velocity template in classpath to display menu.
     * @return String HTML for menu.
     */
    public String showMenu(String model, String template) {
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
    public String showWeblogCalendar() {
        return showWeblogCalendar(false, null);
    }
    
    
    /**
     * Weblog calendar display implementation.
     * @param big Show big archive style calendar.
     * @return HTML for calendar.
     */
    public String showWeblogCalendar( boolean big, String cat ) {
        if (OldWeblogPageModel.VELOCITY_NULL.equals(cat)) cat = null;
        String ret = null;
        try {
            // setup weblog calendar model
            CalendarModel model = null;
            if ( big ) {
                model = new BigWeblogCalendarModel(mPageRequest, cat);
            } else {
                model = new WeblogCalendarModel(mPageRequest, cat);
            }
            
            // save model in JSP page context so CalendarTag can find it
            mPageContext.setAttribute("calendarModel", model);
            
            // Create and setup calendar tag
            CalendarTag calTag = new CalendarTag();
            calTag.setPageContext(mPageContext);
            calTag.setName("calendar");
            calTag.setModel("calendarModel");
            calTag.setLocale(mPageRequest.getLocaleInstance());
            if ( big ) {
                calTag.setClassSuffix("Big");
            }
            ret = calTag.emit();
        } catch (Exception e) {
            mLogger.error("Unexpected exception",e);
        }
        return ret;
    }
    
    
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
            String path, String val1, String val2) {
        Hashtable params = new Hashtable();
        return strutsUrlHelper1( useIds, isAction, path, val1, val2, params);
    }
    
    
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
            String path, String val1, String val2, Hashtable params) {
        
        // NOTE: this method is now official defunct since Roller 4.0
        // when we EOLed struts1 and had no real equivalent for this
        
        if(path == null) {
            return null;
        } else if("weblogCreate".equals(path)) {
            // a little hacky, but hopefully nobody is really using this anymore
            return RollerRuntimeConfig.getRelativeContextURL()+"/roller-ui/authoring/entryAdd.rol?weblog="+val1;
        } else if("editWebsite".equals(path)) {
            // a little hacky, but hopefully nobody is really using this anymore
            return RollerRuntimeConfig.getRelativeContextURL()+"/roller-ui/authoring/weblogConfig.rol?weblog="+val1;
        } else if("logout-redirect".equals(path)) {
            return URLUtilities.getLogoutURL(false);
        } else if("login-redirect".equals(path)) {
            return URLUtilities.getLoginURL(false);
        } else {
            return "<span class=\"error\">ERROR generating link</span>";
        }
    }
    
    
    /**
     * Pass the String through any PagePlugins that have been
     * assigned to the PageHelper, as selected by the Entry.
     *
     * @param entry Entry being rendered.
     * @param str   String to which plugins are to be applied.
     * @return      Result of applying plugins to str.
     */
    public String renderPlugins(WeblogEntryDataWrapper entry, String str) {
        String ret = str;
        mLogger.debug("Applying page plugins to string");
        
        if (mPagePlugins != null) {
            List entryPlugins = entry.getPluginsList();
            // if no Entry plugins, don't bother looping.
            if (entryPlugins != null && !entryPlugins.isEmpty()) {
                
                // now loop over mPagePlugins, matching
                // against Entry plugins (by name):
                // where a match is found render Plugin.
                Iterator iter = mPagePlugins.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    if (entryPlugins.contains(key)) {
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
     * This method used to return an array of supported locales based on some
     * of the old i18n work done in Roller, however, as of Roller 3.0 there is
     * no longer a list of supported languages.  The languages available to a
     * weblog are unbounded and are purely determined by the weblog author.
     *
     * This method always returns null.
     */
    public Locale[] getSupportedLanguages() {
        return null;
    }
    
    
    /**
     * @return relative URL to page, starting with /username
     */
    public String getPathInfo() {
        String pathInfo = mRequest.getPathInfo();
        if(pathInfo == null) {
            pathInfo = "";
        }
        
        return pathInfo;
    }
    
    
    public String getCommentAuthenticatorHtml() {
        // deprecated, does nothing now
        return "";
    }
    
}
