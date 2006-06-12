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
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.formbeans.YourWebsitesForm;


/**
 * Allows user to view and pick from list of his/her websites.
 *
 * @struts.action path="/roller-ui/authoring/yourWebsites" name="yourWebsitesForm" parameter="method"
 * @struts.action-forward name="yourWebsites.page" path=".YourWebsites"
 */
public class YourWebsitesAction extends DispatchAction {
    
    private static Log mLogger = LogFactory.getLog(YourWebsitesAction.class);
    
    
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
        return edit(mapping, actionForm, request, response);
    }
    
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        request.setAttribute("model",
                new YourWebsitesPageModel(request, response, mapping));
        
        ActionForward forward = mapping.findForward("yourWebsites.page");
        return forward;
    }
    
    
    public ActionForward accept(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        
        UserManager userMgr = RollerFactory.getRoller().getUserManager();
        PermissionsData perms = userMgr.getPermissions(form.getInviteId());
        
        // TODO ROLLER_2.0: notify inviter that invitee has accepted invitation
        // TODO EXCEPTIONS: better exception handling
        perms.setPending(false);
        userMgr.savePermissions(perms);
        RollerFactory.getRoller().flush();
        
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
            throws Exception {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        
        UserManager userMgr = RollerFactory.getRoller().getUserManager();
        PermissionsData perms = userMgr.getPermissions(form.getInviteId());
        
        // TODO ROLLER_2.0: notify inviter that invitee has declined invitation
        // TODO EXCEPTIONS: better exception handling here
        userMgr.removePermissions(perms);
        RollerFactory.getRoller().flush();
        
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
            throws Exception {
        YourWebsitesForm form = (YourWebsitesForm)actionForm;
        
        RollerSession rses = RollerSession.getRollerSession(request);
        UserData user = rses.getAuthenticatedUser();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData website = rreq.getWebsite();
        
        UserManager userMgr = RollerFactory.getRoller().getUserManager();
        PermissionsData perms = userMgr.getPermissions(website, user);
        
        if (perms != null) {
            // TODO ROLLER_2.0: notify website members that user has resigned
            // TODO EXCEPTIONS: better exception handling
            userMgr.removePermissions(perms);
            RollerFactory.getRoller().flush();
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
    
    
    public static class YourWebsitesPageModel extends BasePageModel {
        private boolean planetAggregatorEnabled = false;
        private boolean groupBloggingEnabled = false;
        private List permissions = new ArrayList();
        private List pendings = new ArrayList();
        private int userWeblogCount = 0;
        
        public YourWebsitesPageModel(HttpServletRequest request,
                HttpServletResponse response, ActionMapping mapping) throws RollerException {
            super("yourWebsites.title", request, response, mapping);
            Roller roller = RollerFactory.getRoller();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            UserData user = rollerSession.getAuthenticatedUser();
            permissions = roller.getUserManager().getAllPermissions(user);
            userWeblogCount = permissions.size();
            pendings = roller.getUserManager().getPendingPermissions(user);
            groupBloggingEnabled =
                    RollerConfig.getBooleanProperty("groupblogging.enabled");
            setPlanetAggregatorEnabled(RollerConfig.getBooleanProperty("planet.aggregator.enabled"));
        }
        public List getPermissions() {
            return permissions;
        }
        public void setPermissions(List permissions) {
            this.permissions = permissions;
        }
        public List getPendings() {
            return pendings;
        }
        public void setPendings(List pendings) {
            this.pendings = pendings;
        }
        
        public boolean isGroupBloggingEnabled() {
            return groupBloggingEnabled;
        }
        
        public void setGroupBloggingEnabled(boolean groupBloggingEnabled) {
            this.groupBloggingEnabled = groupBloggingEnabled;
        }
        
        public boolean isPlanetAggregatorEnabled() {
            return planetAggregatorEnabled;
        }
        
        public void setPlanetAggregatorEnabled(boolean planetAggregatorEnabled) {
            this.planetAggregatorEnabled = planetAggregatorEnabled;
        }
        
        public int getUserWeblogCount() {
            return userWeblogCount;
        }
        public void setUserWeblogCount(int count) {
            userWeblogCount = count;
        }
    }
    
}
