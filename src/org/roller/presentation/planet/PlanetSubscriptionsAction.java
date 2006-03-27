/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.roller.presentation.planet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.roller.RollerException;
import org.roller.model.PlanetManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerSession;
import org.roller.util.Technorati;


/////////////////////////////////////////////////////////////////////////////
/**
 * Add, remove, and view existing subscriptions in a group.
 * If no group is specified via the groupHandle parameter, then uses "external".
 *
 * @struts.action name="planetSubscriptionFormEx" path="/admin/planetSubscriptions"
 *                scope="request" parameter="method"
 *
 * @struts.action-forward name="planetSubscriptions.page"
 *                        path=".PlanetSubscriptions"
 */
public final class PlanetSubscriptionsAction extends DispatchAction {
    private static Log logger = LogFactory.getFactory().getInstance(
            PlanetSubscriptionsAction.class);
    
    /** Populate page model and forward to subscription page */
    public ActionForward getSubscriptions(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetSubscriptionFormEx form = (PlanetSubscriptionFormEx)actionForm;
                if (request.getParameter("feedUrl") != null) {
                    String feedUrl = request.getParameter("feedUrl");
                    PlanetSubscriptionData sub =
                            planet.getSubscription(feedUrl);
                    form.copyFrom(sub, request.getLocale());
                } else {
                    form.doReset(mapping, request);
                }
                
                String groupHandle = request.getParameter("groupHandle");
                groupHandle = (groupHandle == null) ? form.getGroupHandle() : groupHandle;
                groupHandle = (groupHandle == null) ? "external" : groupHandle;
                
                PlanetGroupData targetGroup = planet.getGroup(groupHandle);
                form.setGroupHandle(groupHandle);
                request.setAttribute("model",
                        new SubscriptionsPageModel(
                        targetGroup, request, response, mapping, form));
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            request.getSession().getServletContext().log("ERROR", e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    /** Cancel editing, reset form */
    public ActionForward cancelEditing(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetSubscriptionFormEx form = (PlanetSubscriptionFormEx)actionForm;
                
                form.doReset(mapping, request);
                
                String groupHandle = request.getParameter("groupHandle");
                groupHandle = (groupHandle == null) ? form.getGroupHandle() : groupHandle;
                groupHandle = (groupHandle == null) ? "external" : groupHandle;
                
                PlanetGroupData targetGroup = planet.getGroup(groupHandle);
                form.setGroupHandle(groupHandle);
                request.setAttribute("model",
                        new SubscriptionsPageModel(
                        targetGroup, request, response, mapping, form));
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            request.getSession().getServletContext().log("ERROR", e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    /** Delete subscription, reset form  */
    public ActionForward deleteSubscription(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            //RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetSubscriptionFormEx form = (PlanetSubscriptionFormEx)actionForm;
                if (form.getId() != null) {
                    PlanetSubscriptionData sub =
                            planet.getSubscriptionById(form.getId());
                    
                    String groupHandle = request.getParameter("groupHandle");
                    groupHandle = (groupHandle == null) ? form.getGroupHandle() : groupHandle;
                    groupHandle = (groupHandle == null) ? "external" : groupHandle;
                    
                    PlanetGroupData targetGroup = planet.getGroup(groupHandle);
                    
                    targetGroup.removeSubscription(sub);
                    planet.deleteSubscription(sub);
                    roller.commit();
                    roller.release();
                    
                    roller.begin();
                    form.doReset(mapping, request);
                    
                    form.setGroupHandle(groupHandle);
                    request.setAttribute("model",
                            new SubscriptionsPageModel(
                            targetGroup, request, response, mapping, form));
                    
                    ActionMessages messages = new ActionMessages();
                    messages.add(null,
                            new ActionMessage("planetSubscription.success.deleted"));
                    saveMessages(request, messages);
                }
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("planetSubscription.error.deleting"));
            saveErrors(request, errors);
        }
        return forward;
    }
    
    /** Save subscription, add to current group */
    public ActionForward saveSubscription(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            Roller roller = RollerFactory.getRoller();
            PlanetManager planet = roller.getPlanetManager();
            PlanetSubscriptionFormEx form = (PlanetSubscriptionFormEx)actionForm;
            
            String groupHandle = request.getParameter("groupHandle");
            groupHandle = (groupHandle == null) ? form.getGroupHandle() : groupHandle;
            groupHandle = (groupHandle == null) ? "external" : groupHandle;
            
            PlanetGroupData targetGroup = planet.getGroup(groupHandle);
            
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                
                ActionMessages messages = new ActionMessages();
                PlanetSubscriptionData sub = null;
                ActionErrors errors = validate(planet, form);
                if (errors.isEmpty()) {
                    if (form.getId() == null || form.getId().trim().length() == 0) {                        
                        // Adding new subscription to group                        
                        // But, does subscription to that feed already exist?
                        if (form.getFeedUrl() != null) {
                            sub = planet.getSubscription(form.getFeedUrl()); 
                        }
                        if (sub != null) {
                            // Yes, we'll use it instead
                            messages.add(null, new ActionMessage(
                                "planetSubscription.foundExisting", sub.getTitle()));
                        } else {
                            // No, add new subscription
                            sub = new PlanetSubscriptionData(); 
                            form.copyTo(sub, request.getLocale());
                        }                        
                        targetGroup.addSubscription(sub);
                        
                    } else {
                        // User editing an existing subscription within a group
                        sub = planet.getSubscriptionById(form.getId());
                        form.copyTo(sub, request.getLocale());                        
                    }                    
                    form.setGroupHandle(groupHandle);
                    planet.saveSubscription(sub);
                    planet.saveGroup(targetGroup);
                    roller.commit();
                    
                    messages.add(null,
                            new ActionMessage("planetSubscription.success.saved"));
                    saveMessages(request, messages);
                    form.doReset(mapping, request);
                } else {
                    saveErrors(request, errors);
                }
            } else {
                forward = mapping.findForward("access-denied");
            }
            request.setAttribute("model",
                    new SubscriptionsPageModel(
                    targetGroup, request, response, mapping, form));
        } catch (RollerException e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError(
                    "planetSubscriptions.error.duringSave",e.getRootCauseMessage()));
            saveErrors(request, errors);
        }
        return forward;
    }
    
    /** Validate posted subscription, fill in blanks via Technorati */
    private ActionErrors validate(
            PlanetManager planet, PlanetSubscriptionFormEx form) {
        String technoratiTitle = null;
        String technoratiFeedUrl = null;
        int inboundlinks = -1;
        int inboundblogs = -1;
        if (form.getSiteUrl()!=null && form.getSiteUrl().trim().length() > 0) {
            try {
                PlanetConfigData config = planet.getConfiguration();
                Technorati technorati = null;
                if (config.getProxyHost()!=null && config.getProxyPort() > 0) {
                    technorati = new Technorati(
                            config.getProxyHost(), config.getProxyPort());
                } else {
                    technorati = new Technorati();
                }
                Technorati.Result result =
                        technorati.getBloginfo(form.getSiteUrl());
                technoratiTitle = result.getWeblog().getName();
                technoratiFeedUrl = result.getWeblog().getRssurl();
                form.setInboundlinks(result.getWeblog().getInboundlinks());
                form.setInboundblogs(result.getWeblog().getInboundblogs());
            } catch (Exception e) {
                logger.debug("Unable to contact Technorati", e);
            }
        }
        
        ActionErrors errors = new ActionErrors();
        if (form.getTitle()==null || form.getTitle().trim().length()==0) {
            if (technoratiTitle!=null && technoratiTitle.trim().length()>0) {
                form.setTitle(technoratiTitle);
            } else {
                errors.add(null,
                        new ActionError("planetSubscription.error.title"));
            }
        }
        if (form.getFeedUrl()==null || form.getFeedUrl().trim().length()==0) {
            if (technoratiFeedUrl!=null && technoratiFeedUrl.trim().length()>0) {
                form.setFeedUrl(technoratiFeedUrl);
            } else {
                errors.add(null,
                        new ActionError("planetSubscription.error.feedUrl"));
            }
        }
        if (form.getSiteUrl()==null || form.getSiteUrl().trim().length()==0) {
            errors.add(null,
                    new ActionError("planetSubscription.error.siteUrl"));
        }
        return errors;
    }
    
    /** Page model, includes subscriptions in "external" group */
    public class SubscriptionsPageModel extends BasePageModel {
        private List subscriptions = null;
        private boolean unconfigured = false;
        private PlanetSubscriptionFormEx form = null;
        
        public SubscriptionsPageModel(
                PlanetGroupData group,
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                PlanetSubscriptionFormEx form) throws RollerException {
            super("dummy", request, response, mapping);
            this.form = form;
            if (group != null) {
                Set subsSet = group.getSubscriptions();
                subscriptions = new ArrayList(subsSet);
            } else {
                unconfigured = true;
            }
        }
        
        public String getTitle() {
            if (!form.getGroupHandle().equals("external")) {
                return MessageFormat.format(
                        bundle.getString("planetSubscriptions.titleGroup"),
                        new Object[] {form.getGroupHandle()});
            } else {
                return bundle.getString("planetSubscriptions.title");
            }
        }
        
        public List getSubscriptions() {
            return subscriptions;
        }
        
        public boolean isUnconfigured() {
            return unconfigured;
        }
    }
}

