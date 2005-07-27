package org.roller.presentation.website.actions;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerSession;

/**
 * Allows website admin to change website member permissions.
 * 
 * @struts.action path="/editor/memberPermissions" parameter="method" name="memberPermissionsForm"
 * @struts.action-forward name="memberPermissions.page" path="/website/MemberPermissions.jsp"
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
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        request.setAttribute("model", 
                new MemberPermissionsPageModel(request, response, mapping));
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
        MemberPermissionsPageModel model = 
            new MemberPermissionsPageModel(request, response, mapping);
        Iterator iter = model.getPermissions().iterator();
        while (iter.hasNext())
        {
            PermissionsData perms = (PermissionsData)iter.next();
            String sval = request.getParameter("perm-" + perms.getId());
            if (sval != null)
            {
                short val = Short.parseShort(sval);
                if (val == -1) perms.remove();
                else perms.setPermissionMask(val);
                RollerFactory.getRoller().commit();
            }
        }
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
            super(request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            WebsiteData website = rollerSession.getCurrentWebsite();
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
