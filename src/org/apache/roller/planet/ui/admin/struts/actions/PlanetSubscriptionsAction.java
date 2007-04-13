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

package org.apache.roller.planet.ui.admin.struts.actions;

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
import org.apache.roller.planet.ui.admin.struts.forms.PlanetSubscriptionForm;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerSession;


/**
 * Add, remove, and view existing subscriptions in a group.
 * If no group is specified via the groupHandle parameter, then uses "external".
 *
 * @struts.action name="planetSubscriptionForm" path="/roller-ui/admin/planetSubscriptions"
 *                scope="request" parameter="method"
 *
 * @struts.action-forward name="planetSubscriptions.page"
 *                        path=".PlanetSubscriptions"
 */
public final class PlanetSubscriptionsAction extends DispatchAction {
    
    private static Log log = LogFactory.getLog(PlanetSubscriptionsAction.class);
    
    
    /** 
     * Populate page model and forward to subscription page 
     */
    public ActionForward getSubscriptions(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetSubscriptionForm form = (PlanetSubscriptionForm)actionForm;
                if (request.getParameter("feedUrl") != null) {
                    String feedUrl = request.getParameter("feedUrl");
                    PlanetSubscriptionData sub =
                            pmgr.getSubscription(feedUrl);
                    form.copyFrom(sub, request.getLocale());
                } else {
                    form.doReset(mapping, request);
                }
                
                String groupHandle = form.getGroupHandle();
                if (groupHandle == null) {
                    groupHandle = "all";
                }
                
                PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                PlanetGroupData targetGroup = pmgr.getGroup(defaultPlanet, groupHandle);
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
    
    
    /** 
     * Cancel editing, reset form 
     */
    public ActionForward cancelEditing(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetSubscriptionForm form = (PlanetSubscriptionForm)actionForm;
                
                form.doReset(mapping, request);
                
                String groupHandle = form.getGroupHandle();
                if (groupHandle == null) {
                    groupHandle = "all";
                }
                
                PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                PlanetGroupData targetGroup = pmgr.getGroup(defaultPlanet, groupHandle);
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
    
    
    /** 
     * Delete subscription, reset form  
     */
    public ActionForward deleteSubscription(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            //RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetSubscriptionForm form = (PlanetSubscriptionForm)actionForm;
                if (form.getId() != null) {
                    PlanetSubscriptionData sub =
                            pmgr.getSubscriptionById(form.getId());
                    
                    String groupHandle = form.getGroupHandle();
                    if (groupHandle == null) {
                        groupHandle = "all";
                    }
                    
                    PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                    PlanetGroupData targetGroup = pmgr.getGroup(defaultPlanet, groupHandle);
                    
                    targetGroup.getSubscriptions().remove(sub);
                    pmgr.deleteSubscription(sub);
                    PlanetFactory.getPlanet().flush();
                    
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
    
    
    /** 
     * Save subscription, add to current group 
     */
    public ActionForward saveSubscription(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        
        log.debug("Save Subscription");
        
        ActionForward forward = mapping.findForward("planetSubscriptions.page");
        try {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            PlanetSubscriptionForm form = (PlanetSubscriptionForm)actionForm;
            
            if (RollerSession.getRollerSession(request).isGlobalAdminUser()) {
                
                String groupHandle = form.getGroupHandle();
                if (groupHandle == null) {
                    groupHandle = "all";
                }
                
                log.debug("Selected group = "+groupHandle);
                
                PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                PlanetGroupData targetGroup = pmgr.getGroup(defaultPlanet, groupHandle);
                
                ActionMessages messages = new ActionMessages();
                ActionErrors errors = validate(pmgr, form);
                if (errors.isEmpty()) {
                    
                    PlanetSubscriptionData sub = null;
                    if (form.getId() == null || form.getId().trim().length() == 0) {
                        
                        // Adding new subscription to group
                        // Does subscription to that feed already exist?
                        sub = pmgr.getSubscription(form.getFeedURL());
                        if (sub != null) {
                            log.debug("Adding Existing Subscription");
                            
                            // Subscription already exists
                            messages.add(null, new ActionMessage(
                                    "planetSubscription.foundExisting", sub.getTitle()));
                        } else {
                            log.debug("Adding New Subscription");
                            
                            // Add new subscription
                            sub = new PlanetSubscriptionData();
                            form.copyTo(sub, request.getLocale());
                            
                            // the form copy is a little dumb and will set the id value
                            // to empty string if it didn't have a value before, which means
                            // that this object would not be considered new
                            sub.setId(null);
                            
                            pmgr.saveSubscription(sub);
                        }
                        
                        // add the subscription to our group
                        targetGroup.getSubscriptions().add(sub);
                        
                    } else {
                        log.debug("Updating Subscription");
                        
                        // User editing an existing subscription within a group
                        sub = pmgr.getSubscriptionById(form.getId());
                        form.copyTo(sub, request.getLocale());
                    }
                    
                    form.setGroupHandle(groupHandle);
                    pmgr.saveGroup(targetGroup);
                    PlanetFactory.getPlanet().flush();
                    
                    messages.add(null,
                            new ActionMessage("planetSubscription.success.saved"));
                    saveMessages(request, messages);
                    form.doReset(mapping, request);
                } else {
                    saveErrors(request, errors);
                }
                
                request.setAttribute("model",
                    new SubscriptionsPageModel(
                    targetGroup, request, response, mapping, form));
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (RollerException re) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError(
                    "planetSubscriptions.error.duringSave", re.getRootCauseMessage()));
            saveErrors(request, errors);
        } catch (Exception e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError(
                    "planetSubscriptions.error.duringSave", e.getMessage()));
            saveErrors(request, errors);
            this.log.error("Unexpected error saving subscription", e);
        }
        
        return forward;
    }
    
    
    /** 
     * Validate posted subscription, fill in blanks via Technorati 
     */
    private ActionErrors validate(
            PlanetManager pmgr, PlanetSubscriptionForm form) {
        
        ActionErrors errors = new ActionErrors();
        
        if (form.getTitle()==null || form.getTitle().trim().length()==0) {
            errors.add(null, new ActionError("planetSubscription.error.title"));
        }
        
        if (form.getFeedURL()==null || form.getFeedURL().trim().length()==0) {
            errors.add(null, new ActionError("planetSubscription.error.feedUrl"));
        }
        
        if (form.getSiteURL()==null || form.getSiteURL().trim().length()==0) {
            errors.add(null, new ActionError("planetSubscription.error.siteUrl"));
        }
        
        return errors;
    }
    
    
    /** 
     * Page model, includes subscriptions in "external" group 
     */
    public class SubscriptionsPageModel extends BasePageModel {
        private List subscriptions = null;
        private boolean unconfigured = false;
        private PlanetSubscriptionForm form = null;
        
        public SubscriptionsPageModel(
                PlanetGroupData group,
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                PlanetSubscriptionForm form) throws RollerException {
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
            if (!form.getGroupHandle().equals("all")) {
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
    }
    
}
