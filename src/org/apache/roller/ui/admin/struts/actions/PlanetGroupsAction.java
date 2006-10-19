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
package org.apache.roller.ui.admin.struts.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.apache.roller.RollerException;
import org.apache.roller.planet.model.PlanetManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.ui.authoring.struts.forms.PlanetGroupForm;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;


/////////////////////////////////////////////////////////////////////////////
/**
 * Add, remove, and view user defined groups.
 * 
 * @struts.action name="planetGroupForm" path="/roller-ui/admin/planetGroups"
 *                scope="request" parameter="method"
 * 
 * @struts.action-forward name="planetGroups.page" 
 *                        path=".PlanetGroups"
 */
public final class PlanetGroupsAction extends DispatchAction
{
    private static Log logger = LogFactory.getFactory().getInstance(
            PlanetGroupsAction.class);

    /** Populate page model and forward to subscription page */
    public ActionForward getGroups(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetGroups.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetGroupForm form = (PlanetGroupForm)actionForm;
                if (request.getParameter("groupHandle") != null)
                {
                    String feedUrl = request.getParameter("groupHandle");
                    PlanetGroupData group = planet.getGroup(feedUrl);
                    form.copyFrom(group, request.getLocale());
                }
                else 
                {
                    form.doReset(mapping, request);
                }
                request.setAttribute("model", 
                    new GroupsPageModel(request, response, mapping));
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR", e);
            throw new ServletException(e);
        }
        return forward;
    }

    /** Cancel editing, reset form */
    public ActionForward cancelEditing(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetGroups.page");
        try
        {
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                PlanetGroupForm form = (PlanetGroupForm)actionForm;              
                form.doReset(mapping, request);
                
                request.setAttribute("model", 
                    new GroupsPageModel(request, response, mapping));
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR", e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    /** Delete subscription, reset form  */
    public ActionForward deleteGroup(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException 
    {
        ActionForward forward = mapping.findForward("planetGroups.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetGroupForm form = (PlanetGroupForm)actionForm;
                if (form.getHandle() != null)
                {
                    PlanetGroupData group = planet.getGroup(form.getHandle());
                    planet.deleteGroup(group);
                    roller.flush();
                    // TODO: why release here?
                    roller.release();
                    
                    form.doReset(mapping, request);
                    
                    request.setAttribute("model", 
                        new GroupsPageModel(request, response, mapping));
                    
                    ActionMessages messages = new ActionMessages();
                    messages.add(null, 
                        new ActionMessage("planetSubscription.success.deleted"));
                    saveMessages(request, messages);
                }
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("planetGroup.error.deleting"));
            saveErrors(request, errors);       
        }
        return forward;
    }

    /** Save subscription, add to "external" group */
    public ActionForward saveGroup(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetGroups.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                PlanetGroupForm form = (PlanetGroupForm)actionForm;
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                ActionErrors errors = validate(planet, form);
                if (errors.isEmpty())
                {
                    PlanetGroupData group = null;
                    if (form.getId() == null || form.getId().trim().length() == 0)
                    {
                        group = new PlanetGroupData();
                    }
                    else 
                    {
                        group = planet.getGroupById(form.getId());
                    }                
                    form.copyTo(group, request.getLocale());
                    planet.saveGroup(group);  
                    roller.flush();

                    ActionMessages messages = new ActionMessages();
                    messages.add(null, 
                            new ActionMessage("planetGroups.success.saved"));
                    saveMessages(request, messages);
                    form.doReset(mapping, request);

                    request.setAttribute("model", 
                            new GroupsPageModel(request, response, mapping));
                }
                else
                {
                    saveErrors(request, errors);
                }
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (RollerException e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError(
              "planetSubscriptions.error.duringSave",e.getRootCauseMessage()));
            saveErrors(request, errors);
        }
        return forward;
    }
    
    /** Validate posted group */
    private ActionErrors validate(
            PlanetManager planet, PlanetGroupForm form)
    {
        ActionErrors errors = new ActionErrors();
        if (form.getTitle()==null || form.getTitle().trim().length()==0)
        {            
            errors.add(null, new ActionError("planetGroups.error.title"));
        }
        if (form.getHandle()==null || form.getHandle().trim().length()==0)
        {            
            errors.add(null, new ActionError("planetGroups.error.handle"));
        }
        if (form.getHandle() != null && 
        (form.getHandle().equals("all") || form.getHandle().equals("external")))
        {
           errors.add(null, new ActionError("planetGroups.error.nameReserved"));
        }
        return errors;
    }

    /** Page model */
    public class GroupsPageModel extends BasePageModel
    {
        private List groups = new ArrayList();
        private boolean unconfigured = false;
        public GroupsPageModel(
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping) throws RollerException
        {
            super("planetGroups.pagetitle", request, response, mapping);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            Roller roller = RollerFactory.getRoller();
            PlanetManager planet = roller.getPlanetManager();            
            PlanetGroupData externalGroup = planet.getGroup("external");
            if (externalGroup != null) 
            {
                Iterator allgroups = planet.getGroups().iterator();
                while (allgroups.hasNext()) 
                {
                    PlanetGroupData agroup = (PlanetGroupData)allgroups.next();
                    if (    !agroup.getHandle().equals("external")
                         && !agroup.getHandle().equals("all")) 
                      {
                          groups.add(agroup);
                      }
                }
            }
            else 
            {
                unconfigured = true;
            }
        }
        public List getGroups()
        {
            return groups;
        }
        public boolean isUnconfigured()
        {
            return unconfigured;
        }
    }
}
