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
package org.apache.roller.ui.authoring.struts.actions;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.admin.struts.actions.*;
import org.apache.roller.ui.core.tasks.*;
import org.apache.roller.ui.core.tasks.RefreshEntriesTask;
import org.apache.roller.ui.core.tasks.SyncWebsitesTask;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.forms.PlanetConfigForm;

/////////////////////////////////////////////////////////////////////////////
/**
 * Allows configuration of Planet Roller.
 * 
 * @struts.action name="planetConfigForm" path="/roller-ui/admin/planetConfig"
 *                scope="request" parameter="method"
 * 
 * @struts.action-forward name="planetConfig.page" 
 *                        path=".PlanetConfig"
 */
public final class PlanetConfigAction extends DispatchAction
{
    private static Log logger = 
        LogFactory.getFactory().getInstance(PlanetConfigAction.class);

    /** Populate config form and forward to config page */
    public ActionForward getConfig(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetConfig.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                BasePageModel pageModel = new BasePageModel(
                    "planetConfig.pageTitle", request, response, mapping);
                request.setAttribute("model",pageModel);                
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetConfigData config = planet.getConfiguration();
                PlanetConfigForm form = (PlanetConfigForm)actionForm;
                if (config != null)
                {
                    form.copyFrom(config, request.getLocale());
                }
                else 
                {
                    form.setTitle("Planet Roller");
                    form.setAdminEmail(RollerRuntimeConfig.getProperty("site.adminemail"));
                    form.setSiteURL(RollerRuntimeConfig.getProperty("site.absoluteurl"));
                    form.setCacheDir("/tmp");
                }
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

    /** Save posted config form data */
    public ActionForward saveConfig(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetConfig.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                BasePageModel pageModel = new BasePageModel(
                    "planetConfig.pageTitle", request, response, mapping);
                request.setAttribute("model",pageModel);                
                Roller roller = RollerFactory.getRoller();
                PlanetManager planet = roller.getPlanetManager();
                PlanetConfigData config = planet.getConfiguration();
                if (config == null)
                {
                    config = new PlanetConfigData();
                }
                PlanetConfigForm form = (PlanetConfigForm) actionForm;
                ActionErrors errors = validate(form);
                if (errors.isEmpty())
                {
                    form.copyTo(config, request.getLocale());
                    planet.saveConfiguration(config);
                    if (planet.getGroup("external") == null) 
                    {
                        PlanetGroupData group = new PlanetGroupData();
                        group.setHandle("external");
                        group.setTitle("external");
                        planet.saveGroup(group);
                    }
                    roller.flush();
                    ActionMessages messages = new ActionMessages();
                    messages.add(null, new ActionMessage("planetConfig.success.saved"));
                    saveMessages(request, messages);
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
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR", e);
            throw new ServletException(e);
        }
        return forward;
    }

    /** Refresh entries in backgrounded thread (for testing) */
    public ActionForward refreshEntries(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetConfig.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                BasePageModel pageModel = new BasePageModel(
                    "planetConfig.pageTitle", request, response, mapping);
                request.setAttribute("model",pageModel);                
                Roller roller = RollerFactory.getRoller();
                RefreshEntriesTask task = new RefreshEntriesTask();
                task.init(roller, "dummy");
                roller.getThreadManager().executeInBackground(task);
                
                ActionMessages messages = new ActionMessages();
                messages.add(null, 
                        new ActionMessage("planetConfig.success.refreshed"));
                saveMessages(request, messages);
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

    /** Sync websites in backgrounded thread (for testing) */
    public ActionForward syncWebsites(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("planetConfig.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (RollerSession.getRollerSession(request).isGlobalAdminUser())
            {
                BasePageModel pageModel = new BasePageModel(
                    "planetConfig.pageTitle", request, response, mapping);
                request.setAttribute("model",pageModel);                
                Roller roller = (Roller)RollerFactory.getRoller();
                SyncWebsitesTask task = new SyncWebsitesTask();
                task.init(roller, "dummy");
                roller.getThreadManager().executeInBackground(task);
                ActionMessages messages = new ActionMessages();
                messages.add(null, 
                        new ActionMessage("planetConfig.success.synced"));
                saveMessages(request, messages);
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
    
    /** Validate config form, returns empty collection if all OK */
    public ActionErrors validate(PlanetConfigForm form)
    {
        ActionErrors errors = new ActionErrors();
        if (form.getProxyHost()!=null && form.getProxyHost().trim().length()>0)
        {
            if (form.getProxyPort()<1)
            {
                errors.add(null, new ActionError(
                        "planetConfig.error.badProxyPort"));
            }
        }
        return errors;
    }
}

