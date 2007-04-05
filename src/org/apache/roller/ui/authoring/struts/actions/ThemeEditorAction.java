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
package org.apache.roller.ui.authoring.struts.actions;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.actions.DispatchAction;
import org.apache.roller.RollerException;
import org.apache.roller.business.themes.ThemeNotFoundException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.themes.ThemeManager;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.themes.SharedTheme;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.ThemeResource;
import org.apache.roller.pojos.ThemeTemplate;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;


/**
 * Struts Action class that handles the website theme chooser page.
 *
 * @author Allen Gilliland
 *
 * @struts.action name="themeEditorForm" path="/roller-ui/authoring/themeEditor"
 *    scope="session" parameter="method"
 *
 * @struts.action-forward name="editTheme.page" path=".theme-editor"
 */
public class ThemeEditorAction extends DispatchAction {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(ThemeEditorAction.class);
    
    
    /**
     * Default action method.
     */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        // make "edit" our default action
        return this.edit(mapping, actionForm, request, response);
    }
    
    
    /**
     * Base action method.
     * 
     * Shows the theme chooser page with this users current theme selected.
     **/
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          form,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionErrors errors = new ActionErrors();
        ActionForward forward = mapping.findForward("editTheme.page");
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                
                BasePageModel pageModel = new BasePageModel(
                        "themeEditor.title", request, response, mapping);
                request.setAttribute("model",pageModel);          
                    
                // get users current theme and our themes list
                Roller roller = RollerFactory.getRoller();
                ThemeManager themeMgr = roller.getThemeManager();
                
                String username = rses.getAuthenticatedUser().getUserName();
                List themes = themeMgr.getEnabledThemesList();
                
                Theme currentTheme = website.getTheme();
                
                // this checks if the website has a default page template
                // if not then we don't allow for a custom theme
                boolean allowCustomTheme = true;
                if(website.getDefaultPageId() == null
                        || website.getDefaultPageId().equals("dummy")
                        || website.getDefaultPageId().trim().equals(""))
                    allowCustomTheme = false;
                
                // if we allow custom themes then add it to the end of the list
                if(RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")
                        && allowCustomTheme)
                    request.setAttribute("allowCustomOption", Boolean.TRUE);
                
                // on the first pass just show a preview of the current theme
                request.setAttribute("previewTheme", currentTheme);
                request.setAttribute("currentTheme", currentTheme);
                request.setAttribute("themesList", themes);
                
                mLogger.debug("Previewing theme "+currentTheme.getName()+" to "+username);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        return forward;
    }
    

    /**
     * Preview action method.
     *
     * Happens when the user selects a new preview theme from the dropdown menu.
     * Shows a new preview theme.
     */
    public ActionForward preview(
            ActionMapping       mapping,
            ActionForm          form,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionErrors errors = new ActionErrors();
        ActionForward forward = mapping.findForward("editTheme.page");
        try {
            RollerSession rses = RollerSession.getRollerSession(request);            
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website)) {

                // get users current theme
                Roller roller = RollerFactory.getRoller();
                ThemeManager themeMgr = roller.getThemeManager();
                
                    
                BasePageModel pageModel = new BasePageModel(
                        "themeEditor.title", request, response, mapping);
                request.setAttribute("model",pageModel);          
                    
                String username = rses.getAuthenticatedUser().getUserName();
                List themes = themeMgr.getEnabledThemesList();
                
                Theme currentTheme = website.getTheme();
                
                // this checks if the website has a default page template
                // if not then we don't allow for a custom theme
                boolean allowCustomTheme = true;
                if(website.getDefaultPageId() == null
                        || website.getDefaultPageId().equals("dummy")
                        || website.getDefaultPageId().trim().equals(""))
                    allowCustomTheme = false;
                
                // if we allow custom themes then add it to the end of the list
                if(RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")
                        && allowCustomTheme)
                    request.setAttribute("allowCustomOption", Boolean.TRUE);
                
                // set the current theme in the request
                request.setAttribute("currentTheme", currentTheme);
                request.setAttribute("themesList", themes);
                
                String theme = request.getParameter("theme");
                try {
                    Theme previewTheme = themeMgr.getTheme(theme);
                    
                    if(previewTheme.isEnabled()) {
                        // make sure the view knows what theme to preview
                        request.setAttribute("previewTheme", previewTheme);
                    
                        mLogger.debug("Previewing theme "+previewTheme.getName()+
                                " to "+username);
                    } else {
                        request.setAttribute("previewTheme", currentTheme);
                        errors.add(null, new ActionMessage("Theme not enabled"));
                        saveErrors(request, errors);
                    }

                } catch(ThemeNotFoundException tnfe) {
                    // hmm ... maybe they chose "custom"?
                    if(theme != null && theme.equals(WeblogTheme.CUSTOM)) {
                        // TODO: total hack, this needs fixing
                        Theme customTheme = new WorkaroundCustomTheme();
                        request.setAttribute("previewTheme", customTheme);
                    } else {
                        // we should never get here
                        request.setAttribute("previewTheme", currentTheme);
                        errors.add(null, new ActionMessage("Theme not found"));
                        saveErrors(request, errors);
                    }
                }
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        return forward;
    }
    

    /**
     * Save action method.
     *
     * Happens when the user clicks the "Save" button to set a new theme.
     * This method simply updates the WebsiteData.editorTheme property with
     * the value of the new theme.
     */
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          form,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionErrors errors = new ActionErrors();
        ActionForward forward = mapping.findForward("editTheme.page");
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                
                BasePageModel pageModel = new BasePageModel(
                        "themeEditor.title", request, response, mapping);
                request.setAttribute("model",pageModel);          
                    
                String newTheme = null;
                
                // lookup what theme the user wants first
                String theme = request.getParameter("theme");
                try {
                    Roller roller = RollerFactory.getRoller();
                    ThemeManager themeMgr = roller.getThemeManager();
                    Theme previewTheme = themeMgr.getTheme(theme);
                    
                    if(previewTheme.isEnabled()) {
                        newTheme = previewTheme.getId();
                    } else {
                        errors.add(null, new ActionMessage("Theme not enabled"));
                        saveErrors(request, errors);
                    }
                    
                } catch(ThemeNotFoundException tnfe) {
                    // possibly selected "custom"
                    if(theme != null && theme.equals(WeblogTheme.CUSTOM)) {
                        newTheme = WeblogTheme.CUSTOM;
                    } else {
                        // hmm ... that's weird
                        mLogger.warn(tnfe);
                        errors.add(null, new ActionMessage("Theme not found"));
                        saveErrors(request, errors);
                    }
                }
                
                // update theme for website and save
                if(newTheme != null) {
                    try {
                        String username = rses.getAuthenticatedUser().getUserName();
                        
                        website.setEditorTheme(newTheme);
                        
                        UserManager userMgr = RollerFactory.getRoller().getUserManager();
                        userMgr.saveWebsite(website);
                        RollerFactory.getRoller().flush();
                        
                        mLogger.debug("Saved theme "+newTheme+" for "+username);
                        
                        // make sure to flush the page cache so ppl can see the change
                        CacheManager.invalidate(website);
                        
                        // update complete ... now just send them back to edit
                        return this.edit(mapping, form, request, response);
                        
                    } catch(RollerException re) {
                        mLogger.error(re);
                        errors.add(null, new ActionMessage("Error setting theme"));
                        saveErrors(request, errors);
                    }
                }
                
                // if we got down here then there was an error :(
                // send the user back to preview page with errors already set
                return this.preview(mapping, form, request, response);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
   
    
    /**
     * Customize action method.
     *
     * Happens when a user clicks the "Customize" button on their current theme.
     * This method copies down all the theme templates from the currently
     * selected theme into the users custom template pages and updates the users
     * theme to "custom".
     */
    public ActionForward customize(
            ActionMapping       mapping,
            ActionForm          form,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionErrors errors = new ActionErrors();
        ActionForward forward = mapping.findForward("editTheme.page");
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                
                BasePageModel pageModel = new BasePageModel(
                        "themeEditor.title", request, response, mapping);
                request.setAttribute("model",pageModel);          
                    
                // copy down current theme to weblog templates
                Roller roller = RollerFactory.getRoller();
                ThemeManager themeMgr = roller.getThemeManager();
                
                String username = rses.getAuthenticatedUser().getUserName();
                
                try {
                    SharedTheme usersTheme = themeMgr.getTheme(website.getEditorTheme());
                    
                    // only if custom themes are allowed
                    if(RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
                        try {
                            themeMgr.importTheme(website, usersTheme);
                            RollerFactory.getRoller().flush();
                        } catch(RollerException re) {
                            mLogger.error(re);
                            errors.add(null, new ActionMessage("Error customizing theme"));
                            saveErrors(request, errors);
                        }
                        
                        // make sure to flush the page cache so ppl can see the change
                        //PageCacheFilter.removeFromCache(request, website);
                        CacheManager.invalidate(website);
                    }
                    
                } catch(ThemeNotFoundException tnfe) {
                    // this catches the potential case where someone customizes
                    // a theme and has their theme as "custom" but then hits the
                    // browser back button and presses the button again, so
                    // they are basically trying to customize a "custom" theme
                    
                    // log this as a warning just in case
                    mLogger.warn(tnfe);
                    
                    // show the user an error message and let things go back
                    // to the edit page
                    errors.add(null, new ActionMessage("Oops!  You already have a custom theme."));
                }
                
                // just take the user back home to the edit theme page
                return this.edit(mapping, form, request, response);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    class WorkaroundCustomTheme implements Theme {
        
        public String getId() {
            return WeblogTheme.CUSTOM;
        }
        
        public String getName() {
            return WeblogTheme.CUSTOM;
        }
        
        public String getDescription() {
            return WeblogTheme.CUSTOM;
        }
        
        public String getAuthor() {
            return WeblogTheme.CUSTOM;
        }
        
        public String getCustomStylesheet() {
            return null;
        }
        
        public Date getLastModified() {
            return null;
        }
        
        public boolean isEnabled() {
            return true;
        }
        
        public List getTemplates() throws RollerException { return null; }
        
        public ThemeTemplate getDefaultTemplate() throws RollerException { return null; }
        
        public ThemeTemplate getTemplateByAction(String action) throws RollerException { return null; }
        
        public ThemeTemplate getTemplateByName(String name) throws RollerException { return null; }
        
        public ThemeTemplate getTemplateByLink(String link) throws RollerException { return null; }
        
        public ThemeResource getResource(String path) { return null; }
    }

}
