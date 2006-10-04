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

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.RequestUtils;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.authoring.struts.formbeans.InvitationsForm;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.MailUtil;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Allow viewing and deletion of invitations.
 *
 * @struts.action path="/roller-ui/authoring/invitations" parameter="method" name="invitationsForm"
 * @struts.action-forward name="invitations.page" path=".Invitations"
 */
public class InvitationsAction extends DispatchAction {
    private static Log mLogger =
            LogFactory.getFactory().getInstance(InvitationsAction.class);
    
    /** If method param is not specified, use HTTP verb to pick method to call */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return view(mapping, actionForm, request, response);
    }
    
    public ActionForward view(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        InvitationsPageModel pageModel = 
            new InvitationsPageModel(request, response, mapping);
        RollerSession rses = RollerSession.getRollerSession(request);
        if (pageModel.getWebsite() != null && 
                rses.isUserAuthorizedToAdmin(pageModel.getWebsite())) {
            request.setAttribute("model", pageModel);
            return mapping.findForward("invitations.page");
        }
        return mapping.findForward("access-denied");
    }
    
    /** Forwads back to the member permissions page */
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return mapping.findForward("memberPermissions");
    }  
    
    public ActionForward revoke(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        
        InvitationsForm invitationForm = (InvitationsForm)actionForm;
        Roller roller = RollerFactory.getRoller();
        UserManager umgr = roller.getUserManager();
        PermissionsData perms = umgr.getPermissions(invitationForm.getPermissionId());
        ActionErrors errors = new ActionErrors();
        if (perms == null) {
            errors.add(null, new ActionError("invitations.error.notFound"));
            saveErrors(request, errors);
            return view(mapping, actionForm, request, response);
        }
        RollerSession rses = RollerSession.getRollerSession(request);
        if (rses.isUserAuthorizedToAdmin(perms.getWebsite())) {
            umgr.removePermissions(perms);
            roller.flush();
            try {
                notifyInvitee(request, perms.getWebsite(), perms.getUser());
            } catch (RollerException e) {
                errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("error.untranslated", e.getMessage()));
            }
            ActionMessages msgs = new ActionMessages();
            msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                new ActionMessage("invitations.revoked"));
            saveMessages(request, msgs);
            return view(mapping, actionForm, request, response);
        }      
        return mapping.findForward("access-denied"); 
    }
    
    /**
     * Inform invitee that invitation has been revoked.
     */
    private void notifyInvitee(
            HttpServletRequest request, WebsiteData website, UserData user)
            throws RollerException {
        try {
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            javax.naming.Context ctx = (javax.naming.Context)
            new InitialContext().lookup("java:comp/env");
            Session mailSession =
                    (Session)ctx.lookup("mail/Session");
            if (mailSession != null) {
                String userName = user.getUserName();
                String from = website.getEmailAddress();
                String cc[] = new String[] {from};
                String bcc[] = new String[0];
                String to[] = new String[] {user.getEmailAddress()};
                String subject;
                String content;
                
                // Figure URL to entry edit page
                RollerContext rc = RollerContext.getRollerContext();
                String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
                if (rootURL == null || rootURL.trim().length()==0) {
                    rootURL = RequestUtils.serverURL(request)
                    + request.getContextPath();
                }
                
                ResourceBundle resources = ResourceBundle.getBundle(
                        "ApplicationResources",
                        website.getLocaleInstance());
                StringBuffer sb = new StringBuffer();
                sb.append(MessageFormat.format(
                        resources.getString("invitations.revokationSubject"),
                        new Object[] {
                    website.getName(),
                    website.getHandle()})
                    );
                subject = sb.toString();
                sb = new StringBuffer();
                sb.append(MessageFormat.format(
                        resources.getString("invitations.revokationContent"),
                        new Object[] {
                    website.getName(),
                    website.getHandle(),
                    user.getUserName()
                }));
                content = sb.toString();
                MailUtil.sendTextMessage(
                        mailSession, from, to, cc, bcc, subject, content);
            }
        } catch (NamingException e) {
            throw new RollerException("ERROR: Revokation email(s) not sent, "
                    + "Roller's mail session not properly configured", e);
        } catch (MessagingException e) {
            throw new RollerException("ERROR: Revokation email(s) not sent, "
                    + "due to Roller configuration or mail server problem.", e);
        } catch (MalformedURLException e) {
            throw new RollerException("ERROR: Revokation email(s) not sent, "
                    + "Roller site URL is malformed?", e);
        } catch (RollerException e) {
            throw new RuntimeException(
                    "FATAL ERROR: unable to find Roller object", e);
        }
    }
    
    public static class InvitationsPageModel extends BasePageModel {
        private List pendings = new ArrayList();
        
        public InvitationsPageModel(HttpServletRequest request,
                HttpServletResponse response, ActionMapping mapping) throws RollerException {
            super("invitations.title", request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            pendings = roller.getUserManager().getPendingPermissions(website);
        }
        public List getPendings() {
            return pendings;
        }
        public void setPendings(List pendings) {
            this.pendings = pendings;
        }
    }    
}
