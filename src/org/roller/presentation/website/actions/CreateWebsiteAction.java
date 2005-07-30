package org.roller.presentation.website.actions;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.CreateWebsiteForm;

/**
 * Allows user to create a new website.
 * 
 * @struts.action path="/editor/createWebsite" parameter="method" name="createWebsiteForm"
 * @struts.action-forward name="createWebsite.page" path="/website/CreateWebsite.jsp"
 * @struts.action-forward name="createWebsiteDone.page" path="/website/CreateWebsiteDone.jsp"
 */
public class CreateWebsiteAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(CreateWebsiteAction.class);
    
    /** If method param is not specified, use HTTP verb to pick method to call */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        if (request.getMethod().equals("GET"))
        {
            return create(mapping, actionForm, request, response);
        }
        return save(mapping, actionForm, request, response);
    }
    
    /** Present new website form to user */
    public ActionForward create(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException
    {
        CreateWebsiteForm form = (CreateWebsiteForm)actionForm;
        form.setLocale( Locale.getDefault().toString() );
        form.setTimeZone( TimeZone.getDefault().getID() );      

        request.setAttribute("model", 
            new CreateWebsitePageModel(request, response, mapping, null));
        
        ActionForward forward = mapping.findForward("createWebsite.page");
        return forward;
    }
    
    /** Save new website created by user */    
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException
    {
        CreateWebsiteForm form = (CreateWebsiteForm)actionForm;
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = validate(form, new ActionErrors());
        if (!errors.isEmpty())
        {
            saveErrors(request, errors);
        }
        else try
        {
            // Add new user
            RollerContext rollerContext = RollerContext.getRollerContext(request);
            UserData user = 
                RollerSession.getRollerSession(request).getAuthenticatedUser();
            UserManager mgr = RollerFactory.getRoller().getUserManager(); 
            
            // Need system user to add new user
            RollerFactory.getRoller().setUser(UserData.SYSTEM_USER);
            HashMap pages = rollerContext.readThemeMacros(form.getTheme());
            WebsiteData website = mgr.createWebsite(
               user, 
               pages, 
               form.getHandle(), 
               form.getName(), 
               form.getDescription(), 
               form.getTheme(), 
               form.getLocale(), 
               form.getTimeZone());
            RollerFactory.getRoller().commit();

            // Flush cache so user will immediately appear on index page
            //PageCacheFilter.removeFromCache( request, ud );
            //MainPageAction.flushMainPageCache();
            
            request.setAttribute("model", 
                    new CreateWebsitePageModel(request, response, mapping, website));                
        }
        catch (RollerException e)
        {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(e.getMessage()));
            saveErrors(request,errors);          
            mLogger.error("ERROR in createWebsite", e);
        }
        ActionForward forward = mapping.findForward("createWebsiteDone.page");
        return forward;
    }
        
    private ActionMessages validate(CreateWebsiteForm form, ActionErrors errors)
    {
        return new ActionMessages();
    }

    public static class CreateWebsitePageModel extends BasePageModel
    {
        private List themes; 
        private String contextURL = null;
        private String weblogURL = null;
        private String rssURL = null;
        private WebsiteData website = null;
        public CreateWebsitePageModel(HttpServletRequest request,
                HttpServletResponse response, ActionMapping mapping, WebsiteData wd)
        {
            super(request, response, mapping);
            RollerContext rollerContext = RollerContext.getRollerContext(request);
            themes = Arrays.asList(rollerContext.getThemeNames());
            if (wd != null) 
            {
                contextURL = rollerContext.getAbsoluteContextUrl(request);
                weblogURL = contextURL + "/page/" + wd.getHandle();   
                rssURL =    contextURL + "/rss/" + wd.getHandle();    
                website = wd;
            }
        }
        public String getContextURL()
        {
            return contextURL;
        }
        public void setContextURL(String contextURL)
        {
            this.contextURL = contextURL;
        }
        public String getRssURL()
        {
            return rssURL;
        }
        public void setRssURL(String rssURL)
        {
            this.rssURL = rssURL;
        }
        public List getThemes()
        {
            return themes;
        }
        public void setThemes(List themes)
        {
            this.themes = themes;
        }
        public String getWeblogURL()
        {
            return weblogURL;
        }
        public void setWeblogURL(String weblogURL)
        {
            this.weblogURL = weblogURL;
        }
        public WebsiteData getWebsite()
        {
            return website;
        }
        public void setWebsite(WebsiteData website)
        {
            this.website = website;
        }
    }
}
