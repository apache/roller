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
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.ui.admin.struts.forms.PlanetGroupForm;
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
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetGroupForm form = (PlanetGroupForm)actionForm;
                if (request.getParameter("groupHandle") != null)
                {
                    String handle = request.getParameter("groupHandle");
                    PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                    PlanetGroupData group = pmgr.getGroup(defaultPlanet, handle);
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
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetGroupForm form = (PlanetGroupForm)actionForm;
                if (form.getHandle() != null)
                {
                    PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                    PlanetGroupData group = pmgr.getGroup(defaultPlanet, form.getHandle());
                    pmgr.deleteGroup(group);
                    PlanetFactory.getPlanet().flush();
                    // TODO: why release here?
                    PlanetFactory.getPlanet().release();
                    
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
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
                ActionErrors errors = validate(pmgr, form);
                if (errors.isEmpty())
                {
                    PlanetGroupData group = null;
                    if (form.getId() == null || form.getId().trim().length() == 0)
                    {
                        group = new PlanetGroupData();
                        group.setPlanet(defaultPlanet);
                    }
                    else 
                    {
                        group = pmgr.getGroupById(form.getId());
                    }                
                    form.copyTo(group, request.getLocale());
                    
                    // the form copy is a little dumb and will set the id value
                    // to empty string if it didn't have a value before, which means
                    // that this object would not be considered new
                    if(group.getId() != null && group.getId().trim().equals("")) {
                        group.setId(null);
                    }
                    
                    pmgr.saveGroup(group);  
                    PlanetFactory.getPlanet().flush();

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
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();            
            PlanetData defaultPlanet = pmgr.getPlanet("zzz_default_planet_zzz");
            PlanetGroupData externalGroup = pmgr.getGroup(defaultPlanet, "external");
            Iterator allgroups = defaultPlanet.getGroups().iterator();
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
        public List getGroups()
        {
            return groups;
        }
    }
}
