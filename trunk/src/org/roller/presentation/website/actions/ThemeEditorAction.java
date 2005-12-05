package org.roller.presentation.website.actions;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
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
import org.roller.RollerException;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.ThemeNotFoundException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ThemeManager;
import org.roller.model.UserManager;
import org.roller.pojos.Theme;
import org.roller.pojos.ThemeTemplate;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;


/**
 * Struts Action class that handles the website theme chooser page.
 *
 * @author Allen Gilliland
 *
 * @struts.action name="themeEditorForm" path="/editor/themeEditor"
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
                String currentTheme = website.getEditorTheme();
                List themes = themeMgr.getEnabledThemesList();
                
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
                    themes.add(Theme.CUSTOM);
                
                // on the first pass just show a preview of the current theme
                request.setAttribute("previewTheme", currentTheme);
                request.setAttribute("currentTheme", currentTheme);
                request.setAttribute("themesList", themes);
                
                mLogger.debug("Previewing theme "+currentTheme+" to "+username);
                
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
                String currentTheme = website.getEditorTheme();
                List themes = themeMgr.getEnabledThemesList();
                
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
                    themes.add(Theme.CUSTOM);
                
                // set the current theme in the request
                request.setAttribute("currentTheme", currentTheme);
                request.setAttribute("themesList", themes);
                
                String theme = request.getParameter("theme");
                try {
                    Theme previewTheme = themeMgr.getTheme(theme);
                    
                    if(previewTheme.isEnabled()) {
                        // make sure the view knows what theme to preview
                        request.setAttribute("previewTheme", previewTheme.getName());
                    
                        mLogger.debug("Previewing theme "+previewTheme.getName()+
                                " to "+username);
                    } else {
                        request.setAttribute("previewTheme", currentTheme);
                        errors.add(null, new ActionMessage("Theme not enabled"));
                        saveErrors(request, errors);
                    }

                } catch(ThemeNotFoundException tnfe) {
                    // hmm ... maybe they chose "custom"?
                    if(theme != null && theme.equals(Theme.CUSTOM)) {
                        request.setAttribute("previewTheme", Theme.CUSTOM);
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
                        newTheme = previewTheme.getName();
                    } else {
                        errors.add(null, new ActionMessage("Theme not enabled"));
                        saveErrors(request, errors);
                    }
                    
                } catch(ThemeNotFoundException tnfe) {
                    // possibly selected "custom"
                    if(theme != null && theme.equals(Theme.CUSTOM)) {
                        newTheme = Theme.CUSTOM;
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
                        Roller roller = RollerFactory.getRoller();
                        String username = rses.getAuthenticatedUser().getUserName();
                        website.setEditorTheme(newTheme);
                        website.save();
                        
                        mLogger.debug("Saved theme "+newTheme+
                                " for "+username);
                        
                        // make sure to flush the page cache so ppl can see the change
                        PageCacheFilter.removeFromCache(request, website);
                
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
                    Theme usersTheme = themeMgr.getTheme(website.getEditorTheme());
                    
                    // only if custom themes are allowed
                    if(RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
                        try {
                            this.saveThemePages(website, usersTheme);
                        } catch(RollerException re) {
                            mLogger.error(re);
                            errors.add(null, new ActionMessage("Error customizing theme"));
                            saveErrors(request, errors);
                        }
                        
                        // make sure to flush the page cache so ppl can see the change
                        PageCacheFilter.removeFromCache(request, website);
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
    
    
    /**
     * Helper method that copies down the pages from a given theme into a
     * users weblog templates.
     *
     * @param rreq Request wrapper.
     * @param theme Name of theme to save.
     * @throws RollerException
     */
    private void saveThemePages(WebsiteData website, Theme theme)
        throws RollerException {
        
        mLogger.debug("Setting custom templates for website: "+website.getName());
        
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            Collection templates = theme.getTemplates();
            Iterator iter = templates.iterator();
            ThemeTemplate theme_template = null;
            while ( iter.hasNext() ) {
                theme_template = (ThemeTemplate) iter.next();
                //String pageContent = (String) templates.get( pageName );
                
                WeblogTemplate template = 
                        userMgr.getPageByName(website, theme_template.getName());
                if (template != null) {
                    // User already has page by that name, so overwrite it.
                    template.setContents(theme_template.getContents());
                    
                } else {
                    // User does not have page by that name, so create new page.
                    template = new WeblogTemplate( null,
                            website,                            // website
                            theme_template.getName(),           // name
                            theme_template.getDescription(),    // description
                            theme_template.getName(),           // link
                            theme_template.getContents(),       // contents
                            new Date()                          // last mod
                            );
                    userMgr.storePage( template );
                }
            }
            
            // now update this website's theme to custom
            website.setEditorTheme(Theme.CUSTOM);
            
            // if this is the first time someone is customizing a theme then
            // we need to set a default page
            if(website.getDefaultPageId() == null ||
                    website.getDefaultPageId().trim().equals("") ||
                    website.getDefaultPageId().equals("dummy")) {
                // we have to go back to the db to figure out the id
                WeblogTemplate template = userMgr.getPageByName(website, "Weblog");
                if(template != null) {
                    mLogger.debug("Setting default page to "+template.getId());
                    website.setDefaultPageId(template.getId());
                }
            }
            
            // we also want to set the weblogdayid
            WeblogTemplate dayTemplate = userMgr.getPageByName(website, "_day");
            if(dayTemplate != null) {
                mLogger.debug("Setting default day page to "+dayTemplate.getId());
                website.setWeblogDayPageId(dayTemplate.getId());
            }
            
            // save our updated website
            userMgr.storeWebsite(website);
            
            // commit?  i still don't understand when this is needed :/
            RollerFactory.getRoller().commit();
            
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new RollerException( e );
        }
        
    }
}
