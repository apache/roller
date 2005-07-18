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
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
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
 * @struts.action-forward name="yourWebsites.page" path="/website/YourWebsites.jsp"
 */
public class YourWebsitesAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(YourWebsitesAction.class);
    
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
        
    public ActionForward select(
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
        }
        
        request.setAttribute("model",
                new YourWebsitesPageModel(request, response, mapping));

        ActionForward forward = mapping.findForward("yourWebsites.page");
        return forward;
    }
        
    public static class YourWebsitesPageModel extends BasePageModel
    {
        private List websites = new ArrayList();
        private List pendings = new ArrayList();
        public YourWebsitesPageModel(HttpServletRequest request,
          HttpServletResponse response, ActionMapping mapping) throws RollerException
        {
            super(request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            UserData user = rollerSession.getAuthenticatedUser();
            websites = roller.getUserManager().getWebsites(user, Boolean.TRUE);
            pendings = roller.getUserManager().getPendingPermissions(user);
        }
        public List getWebsites()
        {
            return websites;
        }
        public void setWebsites(List websitePermissions)
        {
            this.websites = websitePermissions;
        }
        public List getPendings()
        {
            return pendings;
        }
        public void setPendings(List pendings)
        {
            this.pendings = pendings;
        }
    }
}
