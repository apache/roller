package org.roller.presentation.website.actions;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.CharSetUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ThemeManager;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.CreateWebsiteForm;


/**
 * Allows user to create a new website.
 * 
 * @struts.action path="/editor/createWebsite" parameter="method" name="createWebsiteForm"
 * @struts.action-forward name="createWebsite.page" path=".CreateWebsite"
 * @struts.action-forward name="createWebsiteDone.page" path=".CreateWebsiteDone"
 */
public class CreateWebsiteAction extends DispatchAction
{
    protected static String DEFAULT_ALLOWED_CHARS = "A-Za-z0-9";    

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
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        return mapping.findForward("yourWebsites");
    }
    
    /** Present new website form to user */
    public ActionForward create(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
         ActionForward forward = mapping.findForward("createWebsite.page");
        CreateWebsiteForm form = (CreateWebsiteForm)actionForm;
        
        RollerSession rses = RollerSession.getRollerSession(request);
        UserData user = rses.getAuthenticatedUser();        
        form.setLocale(user.getLocale());
        form.setTimeZone(user.getTimeZone());         
        form.setEmailAddress(user.getEmailAddress());

        if (!RollerConfig.getBooleanProperty("groupblogging.enabled")) {
            Roller roller = RollerFactory.getRoller();            
            List permissions = roller.getUserManager().getAllPermissions(user);
            if (permissions.size() > 0) {
                // sneaky user trying to get around 1 blog limit that applies
                // only when group blogging is disabled
                return mapping.findForward("access-denied");
            }           
        }
        
        request.setAttribute("model", 
                new CreateWebsitePageModel(request, response, mapping, null));
        
        return forward;
    }
    
    /** Save new website created by user */    
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        CreateWebsiteForm form = (CreateWebsiteForm)actionForm;
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = validate(form, new ActionErrors());
        ActionForward forward = mapping.findForward("yourWebsites"); 
        Roller roller = RollerFactory.getRoller();
        WebsiteData website = null;
        if (!errors.isEmpty())
        {
            saveErrors(request, errors);
            forward = mapping.findForward("createWebsite.page");
        }
        else try
        {
            RollerContext rollerContext = RollerContext.getRollerContext(request);
            UserData user = 
                RollerSession.getRollerSession(request).getAuthenticatedUser();
            UserManager mgr = roller.getUserManager(); 
            
            if (!RollerConfig.getBooleanProperty("groupblogging.enabled")) {          
                List permissions = roller.getUserManager().getAllPermissions(user);
                if (permissions.size() > 0) {
                    // sneaky user trying to get around 1 blog limit that applies
                    // only when group blogging is disabled
                    return mapping.findForward("access-denied");
                }
            }
            
            // Need system user to create website
            roller.setUser(UserData.SYSTEM_USER);
            HashMap pages = null; //rollerContext.readThemeMacros(form.getTheme());
            website = mgr.createWebsite(
                    user,
                    pages,
                    form.getHandle(),
                    form.getName(),
                    form.getDescription(),
                    form.getEmailAddress(),
                    form.getTheme(),
                    form.getLocale(),
                    form.getTimeZone());
            roller.commit();
            
            request.setAttribute("model",
                    new CreateWebsitePageModel(request, response, mapping, website));
            
            msgs.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("createWebsite.created", form.getHandle()));
            saveMessages(request, msgs);
        }
        catch (RollerException e)
        {
            errors.add(ActionErrors.GLOBAL_ERROR, 
                new ActionError(e.getMessage()));
            saveErrors(request, errors);          
            mLogger.error("ERROR in createWebsite", e);
        }
        
        request.setAttribute("model", 
            new CreateWebsitePageModel(request, response, mapping, website));  
        
        return forward; 
    }
        
    private ActionMessages validate(CreateWebsiteForm form, ActionErrors messages)
        throws RollerException
    {        
        String allowed = RollerConfig.getProperty("username.allowedChars");
    	if(allowed == null || allowed.trim().length() == 0) {
    	       allowed = DEFAULT_ALLOWED_CHARS;
    	}
    	String safe = CharSetUtils.keep(form.getHandle(), allowed);

        if (form.getHandle() == null || "".equals(form.getHandle().trim()))
        {
            messages.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("createWeblog.error.missingHandle"));
        }
        else if (!safe.equals(form.getHandle()) )
        {
            messages.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("createWeblog.error.invalidHandle"));
        }        
        if (form.getEmailAddress() == null || "".equals(form.getEmailAddress().trim()))
        {
            messages.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("createWeblog.error.missingEmailAddress"));
        }
        
        Roller roller = RollerFactory.getRoller();
        if (roller.getUserManager().getWebsiteByHandle(form.getHandle()) != null) 
        {
            messages.add(ActionErrors.GLOBAL_ERROR, 
                    new ActionError("createWeblog.error.handleExists"));
        }
        return messages;
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
            throws RollerException
        {
            super("createWebsite.title", request, response, mapping);
            RollerContext rollerContext = RollerContext.getRollerContext(request);
            Roller roller = RollerFactory.getRoller();
            ThemeManager themeMgr = roller.getThemeManager();
            themes = themeMgr.getEnabledThemesList();
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
