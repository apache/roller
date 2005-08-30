package org.roller.presentation.website.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.MemberPermissionsForm;

/**
 * Allows website admin to change website member permissions.
 * 
 * @struts.action path="/editor/memberPermissions" parameter="method" name="memberPermissionsForm"
 * @struts.action-forward name="memberPermissions.page" path=".MemberPermissions"
 */
public class MemberPermissionsAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MemberPermissionsAction.class);
    
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
        return save(mapping, actionForm, request, response);
    }
    
    /** Called after invite user action posted */
    public ActionForward send(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        return edit(mapping, actionForm, request, response);
    }
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        return edit(mapping, actionForm, request, response);
    }
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        MemberPermissionsPageModel pageModel = 
           new MemberPermissionsPageModel(request, response, mapping);
        request.setAttribute("model", pageModel);
        
        MemberPermissionsForm form = (MemberPermissionsForm)actionForm;
        form.setWebsiteId(pageModel.getWebsite().getId());
        ActionForward forward = mapping.findForward("memberPermissions.page");
        return forward;
    }
    
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        ActionErrors errors = new ActionErrors();
        ActionMessages msgs = new ActionMessages();
        
        MemberPermissionsPageModel model = 
            new MemberPermissionsPageModel(request, response, mapping);
        
        Iterator iter = model.getPermissions().iterator();
        int removed = 0;
        int changed = 0;
        while (iter.hasNext())
        {
            PermissionsData perms = (PermissionsData)iter.next();
            String sval = request.getParameter("perm-" + perms.getId());
            if (sval != null)
            {
                short val = Short.parseShort(sval);
                RollerSession rses = RollerSession.getRollerSession(request);
                UserData user = rses.getAuthenticatedUser();
                if (perms.getUser().getId().equals(user.getId()) 
                        && val < perms.getPermissionMask())
                {
                    errors.add(null,new ActionError(
                        "memberPermissions.noSelfDemotions"));
                }
                else if (val != perms.getPermissionMask()) 
                {
                    if (val == -1) 
                    {
                        perms.remove();
                        removed++;
                    }
                    else
                    {
                        perms.setPermissionMask(val);
                        changed++;
                    }
                }
            }
        }
        if (removed > 0 || changed > 0)
        {
            RollerFactory.getRoller().commit();  
        }
        if (removed > 0) 
        {
            msgs.add(null,new ActionMessage(
                "memberPermissions.membersRemoved", new Integer(removed)));
        }
        if (changed > 0)
        {
            msgs.add(null,new ActionMessage(
                "memberPermissions.membersChanged", new Integer(changed)));
        }
        saveErrors(request, errors);
        saveMessages(request, msgs);
        MemberPermissionsPageModel updatedModel = 
            new MemberPermissionsPageModel(request, response, mapping);
        request.setAttribute("model", updatedModel);
        ActionForward forward = mapping.findForward("memberPermissions.page");
        return forward;
    }
    
    public static class MemberPermissionsPageModel extends BasePageModel
    {
        private List permissions = new ArrayList();
        public MemberPermissionsPageModel(HttpServletRequest request,
          HttpServletResponse response, ActionMapping mapping) throws RollerException
        {
            super("memberPermissions.title", request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            WebsiteData website = rreq.getWebsite();
            permissions = roller.getUserManager().getAllPermissions(website);
        }
        public List getPermissions()
        {
            return permissions;
        }
        public void setWebsites(List permissions)
        {
            this.permissions = permissions;
        }
    }    
}
