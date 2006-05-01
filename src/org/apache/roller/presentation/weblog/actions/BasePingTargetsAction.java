/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

package org.apache.roller.presentation.weblog.actions;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.roller.RollerException;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.presentation.BasePageModel;
import org.apache.roller.presentation.forms.PingTargetForm;
import org.apache.roller.presentation.RollerRequest;


/**
 * Base class for both common and custom ping target operations.  The methods 
 * here apply to creating, editing and removing ping targets.  Operations for 
 * maintaining automatic ping configurations are handled by 
 * {@link PingSetupAction}.
 */
public abstract class BasePingTargetsAction extends DispatchAction
{
    // These are expected to be defined forwards by the concrete subclass actions.
    protected static final String VIEW_PAGE = "pingTargets.page";
    protected static final String PING_TARGET_EDIT_PAGE = "pingTargetEdit.page";
    protected static final String PING_TARGET_DELETE_PAGE = "pingTargetDeleteOK.page";
    protected static final String ACCESS_DENIED_PAGE = "access-denied";
    
    public abstract String getPingTargetsTitle();
    public abstract String getPingTargetEditTitle();
    public abstract String getPingTargetDeleteOKTitle();    

    public BasePingTargetsAction() {

    }

    /**
     * Implements the default action (view) if the method is not specified.
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return the same result as <code>view()</code>
     * @throws Exception
     * @see org.apache.struts.actions.DispatchAction#unspecified(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected ActionForward unspecified(ActionMapping mapping,
                                        ActionForm actionForm,
                                        HttpServletRequest request,
                                        HttpServletResponse response)
        throws Exception
    {
        return view(mapping, actionForm, request, response);
    }

    /**
     * Display the ping targets.
     *
     * @param mapping
     * @param form
     * @param req
     * @param res
     * @return forward to the ping targets page
     * @throws Exception
     */
    public ActionForward view(ActionMapping mapping, ActionForm form,
                              HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        ActionForward forward = mapping.findForward(VIEW_PAGE);
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        try
        {
            BasePageModel pageModel = 
                new BasePageModel(getPingTargetsTitle(), req, res, mapping);
            req.setAttribute("model",pageModel);            
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward(ACCESS_DENIED_PAGE);
            }

            List pingTargets = getPingTargets(rreq);
            req.setAttribute("pingTargets", pingTargets);
            return forward;
        }
        catch (Exception e)
        {
            getLogger().error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /**
     * Save a ping target, new or existing (depending on whether the id is non-empty).
     *
     * @param mapping
     * @param form
     * @param req
     * @param res
     * @return the result of <code>view()</code> after the target is saved.
     * @throws Exception
     */
    public ActionForward save(ActionMapping mapping, ActionForm form,
                              HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        PingTargetManager pingTargetMgr = 
                RollerFactory.getRoller().getPingTargetManager();
        PingTargetForm pingTargetForm = (PingTargetForm) form;
        try
        {
            BasePageModel pageModel = 
                new BasePageModel(getPingTargetEditTitle(), req, res, mapping);
            req.setAttribute("model",pageModel);            
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward(ACCESS_DENIED_PAGE);
            }

            PingTargetData pingTarget = null;
            String pingTargetId = pingTargetForm.getId();
            if (pingTargetId != null && pingTargetId.length() > 0)
            {
                pingTarget = pingTargetMgr.getPingTarget(
                        pingTargetForm.getId());
                if (pingTarget == null) 
                    throw new RollerException(
                            "No such ping target id: " + pingTargetId);
                pingTargetForm.copyTo(pingTarget, req.getLocale());
            }
            else
            {
                pingTarget = createPingTarget(rreq, pingTargetForm);
            }

            // Call private helper to validate ping target
            // If there are errors, go back to the target edit page.
            ActionMessages errors = validate(rreq, pingTarget);
            if (!errors.isEmpty()) {
                saveErrors(rreq.getRequest(), errors);
                return mapping.findForward(PING_TARGET_EDIT_PAGE);
            }

            // Appears to be ok.  
            // Save it, commit and return refreshed view of target list.
            pingTargetMgr.savePingTarget(pingTarget);
            RollerFactory.getRoller().flush();
            
            ActionMessages msgs = new ActionMessages();
            msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("pingTarget.saved"));
            saveMessages(req, msgs);     
            
            return view(mapping, form, req, res);
        }
        catch (Exception e)
        {
            getLogger().error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /**
     * Add a new ping target. Loads the edit view blank.
     *
     * @param mapping
     * @param form
     * @param req
     * @param res
     * @return the edit page (blank)
     * @throws Exception
     */
    public ActionForward addNew(ActionMapping mapping, ActionForm form,
                                HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        BasePageModel pageModel = 
            new BasePageModel(getPingTargetEditTitle(), req, res, mapping);
        req.setAttribute("model",pageModel);            
        return mapping.findForward(PING_TARGET_EDIT_PAGE);
    }

    /**
     * Edit a ping target (load edit view)
     *
     * @param mapping
     * @param form
     * @param req
     * @param res
     * @return the edit view with the form populated with the ping target specified by the id in the request.
     * @throws Exception
     */
    public ActionForward editSelected(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        ActionForward forward = mapping.findForward(PING_TARGET_EDIT_PAGE);
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        try
        {
            BasePageModel pageModel = 
                new BasePageModel(getPingTargetEditTitle(), req, res, mapping);
            req.setAttribute("model",pageModel);            
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward(ACCESS_DENIED_PAGE);
            }
            PingTargetData pingTarget = select(rreq);
            ((PingTargetForm) form).copyFrom(pingTarget, req.getLocale());
            return forward;
        }
        catch (Exception e)
        {
            getLogger().error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /**
     * Delete a ping target (load delete confirmation view).
     *
     * @param mapping
     * @param form
     * @param req
     * @param res
     * @return the delete confirmation view with the form populated with the ping target specified by the id in the request.
     * @throws Exception
     */
    public ActionForward deleteSelected(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        ActionForward forward = mapping.findForward(PING_TARGET_DELETE_PAGE);
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        try
        {
            BasePageModel pageModel = new BasePageModel(
                    getPingTargetDeleteOKTitle(), req, res, mapping);
            req.setAttribute("model",pageModel);            
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward(ACCESS_DENIED_PAGE);
            }
            PingTargetData pingTarget = select(rreq);
            ((PingTargetForm) form).copyFrom(pingTarget, req.getLocale());
            return forward;
        }
        catch (Exception e)
        {
            getLogger().error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /**
     * Delete a ping target (already confirmed).  This performs the actual deletion.
     *
     * @param mapping
     * @param form
     * @param req
     * @param res
     * @return the result of <code>view()</code> after the deletion
     * @throws Exception
     */
    public ActionForward deleteConfirmed(ActionMapping mapping, ActionForm form,
                                         HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        PingTargetForm pingTargetForm = (PingTargetForm) form;
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        try
        {
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward(ACCESS_DENIED_PAGE);
            }
            String pingTargetId = pingTargetForm.getId();
            if (pingTargetId == null || pingTargetId.length() == 0)
            {
                throw new RollerException("Missing ping target id.");
            }
            PingTargetData ping = pingTargetMgr.getPingTarget(pingTargetId);
            pingTargetMgr.removePingTarget(ping);
            RollerFactory.getRoller().flush();
            return view(mapping, form, req, res);
        }
        catch (Exception e)
        {
            getLogger().error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    // TODO: Consider unifying with other RollerRequest methods
    /**
     * Helper to select the ping target specified by the id in the request.
     * @param rreq
     * @return the ping target specified by the id in the request
     * @throws RollerException
     */
    private PingTargetData select(RollerRequest rreq) throws RollerException
    {
        String pingTargetId = rreq.getRequest().getParameter(RollerRequest.PINGTARGETID_KEY);
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        if (pingTargetId == null || pingTargetId.length() == 0)
        {
            throw new RollerException("Missing ping target id: " + pingTargetId);
        }

        PingTargetData pingTarget = pingTargetMgr.getPingTarget(pingTargetId);
        if (pingTarget == null)
        {
            throw new RollerException("No such ping target id: " + pingTargetId);
        }
        return pingTarget;
    }

    /**
     * Private helper to validate a ping target.
     * @param rreq the request
     * @param pingTarget the ping target to validate
     * @return an <code>ActionMessages</code> object with 
     *         <code>ActionMessage</code> for each error encountered, 
     *         empty if no errors were encountered.
     * @throws RollerException
     */
    private ActionMessages validate(
        RollerRequest rreq, PingTargetData pingTarget) throws RollerException
    {
        ActionMessages errors = new ActionMessages();

        PingTargetManager pingTargetMgr = 
                RollerFactory.getRoller().getPingTargetManager();
        if (!pingTargetMgr.isNameUnique(pingTarget))
        {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("pingTarget.nameNotUnique"));
        }
        if (!pingTargetMgr.isUrlWellFormed(pingTarget))
        {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("pingTarget.malformedUrl"));
        } else if (!pingTargetMgr.isHostnameKnown(pingTarget))
        {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("pingTarget.unknownHost"));
        }
        return errors;
    }


    /**
     * Helper defined by the subclass to determine if user has adequate 
     * rights for the action. This and the 
     * {@link org.apache.roller.pojos.PingTargetData#canSave()} method determine the 
     * access control for the action.
     */
    protected abstract boolean hasRequiredRights(
            RollerRequest rreq, WebsiteData website) throws RollerException;

    /**
     * Get the logger from the concrete subclass
     */
    protected abstract Log getLogger();

    /**
     * Get the ping targets for the view.  This is implemented differently in 
     * the concrete subclasses.
     */
    protected abstract List getPingTargets(RollerRequest rreq) 
        throws RollerException;


    /**
     * Create a new ping target (blank). This is implemented differently in 
     * the concrete subclasses.
     */
    protected abstract PingTargetData createPingTarget(
        RollerRequest rreq, PingTargetForm pingTargetForm) 
            throws RollerException;
}
