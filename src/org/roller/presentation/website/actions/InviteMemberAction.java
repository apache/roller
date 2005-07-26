package org.roller.presentation.website.actions;

import java.io.IOException;

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
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.InviteMemberForm;

/**
 * Allows website admin to invite new members to website.
 * 
 * @struts.action path="/editor/inviteMember" parameter="method" name="inviteMemberForm"
 * @struts.action-forward name="inviteMembers.page" path="/website/InviteMember.jsp"
 */
public class InviteMemberAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(InviteMemberAction.class);

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
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("inviteMembers.page");
        return forward; 
    }
    
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionErrors();
        InviteMemberForm form = (InviteMemberForm)actionForm;
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        UserData user = umgr.getUser(form.getUserName());
        if (user == null)
        {
            errors.add(ActionErrors.GLOBAL_ERROR, 
                new ActionError("inviteMembers.error.userNotFound"));
        }
        else 
        {
            RollerSession rses = RollerSession.getRollerSession(request);
            WebsiteData website = rses.getCurrentWebsite();
            PermissionsData perms = umgr.getPermissions(website, user);
            if (perms != null && perms.isPending())
            {
                errors.add(ActionErrors.GLOBAL_ERROR, 
                    new ActionError("inviteMembers.error.userAlreadyInvited"));
            }
            else if (perms != null)
            {
                errors.add(ActionErrors.GLOBAL_ERROR, 
                    new ActionError("inviteMembers.error.userAlreadyMember"));
            }
            else
            {
                umgr.inviteUser(website, user, PermissionsData.AUTHOR);
                // ROLLER_2.0: notify user by email of invitation
                msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("inviteMembers.userInvited"));
            }
        }
        if (!errors.isEmpty()) saveErrors(request, errors);
        if (!msgs.isEmpty()) saveMessages(request, msgs);
        ActionForward forward = mapping.findForward("inviteMembers.page");
        return forward; 
    }
}
