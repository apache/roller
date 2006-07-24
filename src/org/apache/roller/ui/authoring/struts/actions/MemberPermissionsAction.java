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
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.formbeans.MemberPermissionsForm;

/**
 * Allows website admin to change website member permissions.
 *
 * @struts.action path="/roller-ui/authoring/memberPermissions" parameter="method" name="memberPermissionsForm"
 * @struts.action-forward name="memberPermissions.page" path=".MemberPermissions"
 */
public class MemberPermissionsAction extends DispatchAction {
    private static Log mLogger =
            LogFactory.getFactory().getInstance(MemberPermissionsAction.class);
    
    /** If method param is not specified, use HTTP verb to pick method to call */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("GET")) {
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
            throws Exception {
        return edit(mapping, actionForm, request, response);
    }
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return edit(mapping, actionForm, request, response);
    }
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        
        MemberPermissionsPageModel pageModel =
                new MemberPermissionsPageModel(request, response, mapping);
        request.setAttribute("model", pageModel);
        RollerSession rses = RollerSession.getRollerSession(request);
        
        // Ensure use has admin perms for this weblog
        if (pageModel.getWebsite() != null && rses.isUserAuthorizedToAdmin(pageModel.getWebsite())) {
            MemberPermissionsForm form = (MemberPermissionsForm)actionForm;
            form.setWebsiteId(pageModel.getWebsite().getId());
            ActionForward forward = mapping.findForward("memberPermissions.page");
            return forward;
        } else {
            return mapping.findForward("access-denied");
        }
    }
    
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        ActionErrors errors = new ActionErrors();
        ActionMessages msgs = new ActionMessages();
        RollerSession rses = RollerSession.getRollerSession(request);
        MemberPermissionsPageModel model =
                new MemberPermissionsPageModel(request, response, mapping);
        
        // Ensure use has admin perms for this weblog
        if (model.getWebsite() != null && rses.isUserAuthorizedToAdmin(model.getWebsite())) {
            
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            Iterator iter = model.getPermissions().iterator();
            int removed = 0;
            int changed = 0;
            while (iter.hasNext()) {
                PermissionsData perms = (PermissionsData)iter.next();
                String sval = request.getParameter("perm-" + perms.getId());
                if (sval != null) {
                    short val = Short.parseShort(sval);
                    UserData user = rses.getAuthenticatedUser();
                    if (perms.getUser().getId().equals(user.getId())
                    && val < perms.getPermissionMask()) {
                        errors.add(null,new ActionError(
                                "memberPermissions.noSelfDemotions"));
                    } else if (val != perms.getPermissionMask()) {
                        if (val == -1) {
                            userMgr.removePermissions(perms);
                            removed++;
                        } else {
                            perms.setPermissionMask(val);
                            userMgr.savePermissions(perms);
                            changed++;
                        }
                    }
                }
            }
            if (removed > 0 || changed > 0) {
                RollerFactory.getRoller().flush();
            }
            if (removed > 0) {
                msgs.add(null,new ActionMessage(
                        "memberPermissions.membersRemoved", new Integer(removed)));
            }
            if (changed > 0) {
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
            
        } else {
            return mapping.findForward("access-denied");
        }
    }
    
    public static class MemberPermissionsPageModel extends BasePageModel {
        private List permissions = new ArrayList();
        public MemberPermissionsPageModel(HttpServletRequest request,
                HttpServletResponse response, ActionMapping mapping) throws RollerException {
            super("memberPermissions.title", request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            WebsiteData website = rreq.getWebsite();
            permissions = roller.getUserManager().getAllPermissions(website);
        }
        public List getPermissions() {
            return permissions;
        }
        public void setWebsites(List permissions) {
            this.permissions = permissions;
        }
    }
}
