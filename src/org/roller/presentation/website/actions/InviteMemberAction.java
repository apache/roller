package org.roller.presentation.website.actions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.website.formbeans.InviteMemberForm;
import org.roller.util.MailUtil;

/**
 * Allows website admin to invite new members to website.
 * 
 * @struts.action path="/editor/inviteMember" parameter="method" name="inviteMemberForm"
 * @struts.action-forward name="inviteMember.page"     path=".InviteMember"
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
    
    /** If method param is not specified, use HTTP verb to pick method to call */
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        return mapping.findForward("memberPermissions");
    }
    
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("inviteMember.page");        
        request.setAttribute("model", new BasePageModel(
            "inviteMember.title", request, response, mapping));
        return forward; 
    }
    
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        ActionForward forward = mapping.findForward("inviteMember.page");
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionErrors();
        InviteMemberForm form = (InviteMemberForm)actionForm;
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        UserData user = umgr.getUser(form.getUserName());
        
        request.setAttribute("model", new BasePageModel(
            "inviteMember.title", request, response, mapping));

        if (user == null)
        {
            errors.add(ActionErrors.GLOBAL_ERROR, 
                new ActionError("inviteMember.error.userNotFound"));
        }
        else 
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            PermissionsData perms = umgr.getPermissions(website, user);
            if (perms != null && perms.isPending())
            {
                errors.add(ActionErrors.GLOBAL_ERROR, 
                    new ActionError("inviteMember.error.userAlreadyInvited"));
            }
            else if (perms != null)
            {
                errors.add(ActionErrors.GLOBAL_ERROR, 
                    new ActionError("inviteMember.error.userAlreadyMember"));
            }
            else
            {
                String mask = request.getParameter("permissionsMask");
                umgr.inviteUser(website, user, Short.parseShort(mask));
                request.setAttribute("user", user);
                try 
                {
                    notifyInvitee(request, website, user);
                }
                catch (RollerException e)
                {
                    errors.add(ActionErrors.GLOBAL_ERROR, 
                        new ActionError("error.untranslated", e.getMessage()));                
                }               
                msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("inviteMember.userInvited"));
                
                forward = mapping.findForward("memberPermissions");                
            }
        }
        saveErrors(request, errors);
        saveMessages(request, msgs);
        return forward; 
    }
    
    /**
     * Inform invitee of new invitation.
     */
    private void notifyInvitee(
            HttpServletRequest request, WebsiteData website, UserData user) 
            throws RollerException
    {
        try
        {
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            javax.naming.Context ctx = (javax.naming.Context)
                new InitialContext().lookup("java:comp/env");
            Session mailSession = 
                (Session)ctx.lookup("mail/Session");
            if (mailSession != null)
            {
                String userName = user.getUserName();
                String from = website.getEmailAddress();
                String cc[] = new String[] {from};
                String bcc[] = new String[0];
                String to[] = new String[] {user.getEmailAddress()};
                String subject;
                String content;
                
                // Figure URL to entry edit page
                RollerContext rc = RollerContext.getRollerContext(request);
                String rootURL = rc.getAbsoluteContextUrl(request);
                if (rootURL == null || rootURL.trim().length()==0)
                {
                    rootURL = RequestUtils.serverURL(request) 
                                  + request.getContextPath();
                }               
                String url = rootURL + "/editor/yourWebsites.do";
                
                ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", 
                    website.getLocaleInstance());
                StringBuffer sb = new StringBuffer();
                sb.append(MessageFormat.format(
                   resources.getString("inviteMember.notificationSubject"),
                   new Object[] {
                           website.getName(), 
                           website.getHandle()})
                );
                subject = sb.toString();
                sb = new StringBuffer();
                sb.append(MessageFormat.format(
                   resources.getString("inviteMember.notificationContent"),
                   new Object[] {
                           website.getName(), 
                           website.getHandle(), 
                           user.getUserName(), 
                           url
                }));
                content = sb.toString();
                MailUtil.sendTextMessage(
                        mailSession, from, to, cc, bcc, subject, content);
            }
        }
        catch (NamingException e)
        {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                    + "Roller's mail session not properly configured", e);
        }
        catch (MessagingException e)
        {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                + "due to Roller configuration or mail server problem.", e);
        }
        catch (MalformedURLException e)
        {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                    + "Roller site URL is malformed?", e);
        }
        catch (RollerException e)
        {
            throw new RuntimeException(
                    "FATAL ERROR: unable to find Roller object", e);
        }
    }


}
