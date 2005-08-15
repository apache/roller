package org.roller.presentation.website.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.YourWebsitesForm;

/**
 * Allows user to view and pick from list of his/her websites.
 * 
 * @struts.action path="/editor/yourWebsites" name="yourWebsitesForm" parameter="method"
 * @struts.action-forward name="yourWebsites.page" path=".YourWebsites"
 */
public class YourWebsitesAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(YourWebsitesAction.class);
    
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
            return edit(mapping, actionForm, request, response);
        }
        return edit(mapping, actionForm, request, response);
    }
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        request.setAttribute("model",
                new YourWebsitesPageModel(request, response, mapping));
        
        ActionForward forward = mapping.findForward("yourWebsites.page");
        return forward;
    }
        
    public ActionForward newEntry(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        selectWebsite(mapping, actionForm, request, response);
        return mapping.findForward("weblogCreate");
    }
    
    public ActionForward editEntries(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        selectWebsite(mapping, actionForm, request, response);
        return mapping.findForward("weblogQuery");
    }
    
    public ActionForward manageWeblog(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        selectWebsite(mapping, actionForm, request, response);
        return mapping.findForward("editWebsite");
    }
    
    public void selectWebsite(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        Roller roller = RollerFactory.getRoller();
        WebsiteData selectedWebsite = 
            roller.getUserManager().retrieveWebsite(form.getWebsiteId());
        RollerSession rollerSession = RollerSession.getRollerSession(request);
        UserData user = rollerSession.getAuthenticatedUser();
        if (selectedWebsite.hasUserPermissions(user, PermissionsData.LIMITED))
        {
            rollerSession.setCurrentWebsite(selectedWebsite);
            //ActionMessages msgs = new ActionMessages();
            //msgs.add(null, new ActionMessage(
                    //"yourWebsites.selected", selectedWebsite.getHandle()));
            //saveMessages(request, msgs);
        }        
    }
        
    public ActionForward accept(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        Roller roller = RollerFactory.getRoller();
        PermissionsData perms = 
            roller.getUserManager().retrievePermissions(form.getInviteId());
        
        perms.setPending(false);
        // ROLLER_2.0: notify inviter that invitee has accepted invitation  
        roller.commit();

        ActionMessages msgs = new ActionMessages();
        msgs.add(null, new ActionMessage(
                "yourWebsites.accepted", perms.getWebsite().getHandle()));
        saveMessages(request, msgs);
        
        request.setAttribute("model",
                new YourWebsitesPageModel(request, response, mapping));
        ActionForward forward = mapping.findForward("yourWebsites.page");
        return forward;
    }
        
    public ActionForward decline(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        Roller roller = RollerFactory.getRoller();
        PermissionsData perms = 
            roller.getUserManager().retrievePermissions(form.getInviteId());
        
        perms.remove();
        // ROLLER_2.0: notify inviter that invitee has declined invitation  
        roller.commit();
        
        ActionMessages msgs = new ActionMessages();
        msgs.add(null, new ActionMessage(
                "yourWebsites.declined", perms.getWebsite().getHandle()));
        saveMessages(request, msgs);
        
        request.setAttribute("model",
                new YourWebsitesPageModel(request, response, mapping));
        ActionForward forward = mapping.findForward("yourWebsites.page");
        return forward;
    }
   
    public ActionForward resign(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        Roller roller = RollerFactory.getRoller();
        RollerSession rses = RollerSession.getRollerSession(request);
        UserData user = rses.getAuthenticatedUser();
        WebsiteData website = 
            roller.getUserManager().retrieveWebsite(form.getWebsiteId());
        PermissionsData perms = 
            roller.getUserManager().getPermissions(website, user);
        if (perms != null) 
        {
            // ROLLER_2.0: notify website members that user has resigned  
            perms.remove();
            roller.commit();
        }
        
        ActionMessages msgs = new ActionMessages();
        msgs.add(null, new ActionMessage(
                "yourWebsites.resigned", perms.getWebsite().getHandle()));
        saveMessages(request, msgs);
        
        request.setAttribute("model",
                new YourWebsitesPageModel(request, response, mapping));
        ActionForward forward = mapping.findForward("yourWebsites.page");
        return forward;
    }
    
    public static class YourWebsitesPageModel extends BasePageModel
    {
        private boolean groupBloggingEnabled = true;
        private List permissions = new ArrayList();
        private List pendings = new ArrayList();
        public YourWebsitesPageModel(HttpServletRequest request,
          HttpServletResponse response, ActionMapping mapping) throws RollerException
        {
            super(request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            UserData user = rollerSession.getAuthenticatedUser();
            permissions = roller.getUserManager().getAllPermissions(user);
            pendings = roller.getUserManager().getPendingPermissions(user); 
            groupBloggingEnabled = 
                    RollerConfig.getBooleanProperty("groupblogging.enabled");
        }
        public List getPermissions()
        {
            return permissions;
        }
        public void setPermissions(List permissions)
        {
            this.permissions = permissions;
        }
        public List getPendings()
        {
            return pendings;
        }
        public void setPendings(List pendings)
        {
            this.pendings = pendings;
        }

        public boolean isGroupBloggingEnabled() {
            return groupBloggingEnabled;
        }

        public void setGroupBloggingEnabled(boolean groupBloggingEnabled) {
            this.groupBloggingEnabled = groupBloggingEnabled;
        }
    }
}
