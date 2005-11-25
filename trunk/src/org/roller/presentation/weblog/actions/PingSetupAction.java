/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.presentation.weblog.actions;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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
import org.apache.xmlrpc.XmlRpcException;
import org.roller.RollerException;
import org.roller.config.PingConfig;
import org.roller.model.AutoPingManager;
import org.roller.model.PingTargetManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pings.WeblogUpdatePinger;


/**
 * Actions for setting up automatic ping configuration for a weblog.
 *
 * @struts.action name="pingSetupForm" path="/editor/pingSetup" scope="request" parameter="method"
 * @struts.action-forward name="pingSetup.page" path=".Pings"
 * @struts.action-forward name="pingResult.page" path=".PingResult"
 */
public class PingSetupAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(PingSetupAction.class);

    private static final String PING_SETUP_PAGE = "pingSetup.page";
    
    // Changing this to take your back to the pings setup page instead of
    // ping result page, no need for extra page.
    private static final String PING_RESULT_PAGE = "pingSetup.page"; // "pingResult.page";

    /* (non-Javadoc)
     * @see org.apache.struts.actions.DispatchAction#unspecified(
     * 	org.apache.struts.action.ActionMapping,
     *  org.apache.struts.action.ActionForm,
     *  javax.servlet.http.HttpServletRequest,
     *  javax.servlet.http.HttpServletResponse)
     */
    protected ActionForward unspecified(ActionMapping mapping,
                                        ActionForm actionForm,
                                        HttpServletRequest request,
                                        HttpServletResponse response)
        throws Exception
    {
        return view(mapping, actionForm, request, response);
    }

    /*
     * Display the common ping targets with page
     */
    public ActionForward view(ActionMapping mapping, ActionForm form,
                              HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        ActionForward forward = mapping.findForward(PING_SETUP_PAGE);
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        WebsiteData website = rreq.getWebsite();
        try
        {
            if (!isAuthorized(rreq, website))
            {
                return mapping.findForward("access-denied");
            }

            BasePageModel pageModel = 
                    new BasePageModel("pings.title", req, res, mapping);
            req.setAttribute("model",pageModel);
        
            List commonPingTargets = pingTargetMgr.getCommonPingTargets();
            req.setAttribute("commonPingTargets", commonPingTargets);

            Boolean allowCustomTargets = new Boolean(!PingConfig.getDisallowCustomTargets());
            req.setAttribute("allowCustomTargets", allowCustomTargets);

            List customPingTargets = allowCustomTargets.booleanValue() ?
                pingTargetMgr.getCustomPingTargets(website) : Collections.EMPTY_LIST;
            req.setAttribute("customPingTargets", customPingTargets);

            // Build isEnabled map (keyed by ping target id and values Boolean.TRUE/Boolean.FALSE)
            Map isEnabled = buildIsEnabledMap(rreq, commonPingTargets, customPingTargets);
            req.setAttribute("isEnabled", isEnabled);

            return forward;
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Private helper to build a map indexed by ping target id with values Boolean.TRUE and Boolean.FALSE
     * based on whether the ping target is enabled (has a corresponding auto ping configuration).
     */
    private Map buildIsEnabledMap(RollerRequest rreq, List commonPingTargets, List customPingTargets)
        throws RollerException
    {
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        WebsiteData website = rreq.getWebsite();

        // Build isEnabled map (keyed by ping target id and values Boolean.TRUE/Boolean.FALSE)
        Map isEnabled = new HashMap();
        List autopings = autoPingMgr.getAutoPingsByWebsite(website);
        // Add the enabled auto ping configs with TRUE
        for (Iterator i = autopings.iterator(); i.hasNext();)
        {
            AutoPingData autoPing = (AutoPingData) i.next();
            isEnabled.put(autoPing.getPingTarget().getId(), Boolean.TRUE);
        }
        // Somewhat awkward, but the two loops save building a separate combined list.
        // Add disabled common ones with FALSE
        for (Iterator i = commonPingTargets.iterator(); i.hasNext();)
        {
            PingTargetData pingTarget = (PingTargetData) i.next();
            if (isEnabled.get(pingTarget.getId()) == null)
            {
                isEnabled.put(pingTarget.getId(), Boolean.FALSE);
            }
        }
        // Add disabled custom ones with FALSE
        for (Iterator i = customPingTargets.iterator(); i.hasNext();)
        {
            PingTargetData pingTarget = (PingTargetData) i.next();
            if (isEnabled.get(pingTarget.getId()) == null)
            {
                isEnabled.put(pingTarget.getId(), Boolean.FALSE);
            }
        }
        return isEnabled;
    }

    /*
     * Enable a ping target.
     */
    public ActionForward enableSelected(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        PingTargetData pingTarget = select(rreq);
        try
        {
            if (!isAuthorized(rreq, rreq.getWebsite()))
            {
                return mapping.findForward("access-denied");
            }
            AutoPingData autoPing = autoPingMgr.createAutoPing(pingTarget, 
                    rreq.getWebsite());
            autoPingMgr.storeAutoPing(autoPing);
            RollerFactory.getRoller().commit();

            return view(mapping, form, req, res);
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Load delete confirmation view.
     */
    public ActionForward disableSelected(ActionMapping mapping, ActionForm form,
                                         HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        PingTargetData pingTarget = select(rreq);
        try
        {
            if (!isAuthorized(rreq, rreq.getWebsite()))
            {
                return mapping.findForward("access-denied");
            }
            autoPingMgr.removeAutoPing(pingTarget, rreq.getWebsite());
            RollerFactory.getRoller().commit();
        
            return view(mapping, form, req, res);
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Ping the selected target now.
     */
    public ActionForward pingSelectedNow(ActionMapping mapping, ActionForm form,
                                         HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(req);
            String absoluteUrl = RollerContext.getRollerContext(req).getAbsoluteContextUrl(req);
            PingTargetData pingTarget = select(rreq);
            WebsiteData website = rreq.getWebsite();
            try
            {
                if (!isAuthorized(rreq, website))
                {
                    return mapping.findForward("access-denied");
                }
                if (PingConfig.getSuspendPingProcessing())
                {
                    if (mLogger.isDebugEnabled()) mLogger.debug("Ping processing is disabled.");
                    ActionMessages errors = new ActionMessages();
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.pingProcessingIsSuspended"));
                    saveErrors(req, errors);
                }
                else
                {
                    WeblogUpdatePinger.PingResult pingResult = 
                        WeblogUpdatePinger.sendPing(absoluteUrl, pingTarget, website);
                    if (pingResult.isError())
                    {
                        if (mLogger.isDebugEnabled()) mLogger.debug("Ping Result: " + pingResult);
                        ActionMessages errors = new ActionMessages();
                        if (pingResult.getMessage() != null && pingResult.getMessage().trim().length() > 0)
                        {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, 
                                new ActionMessage("ping.transmittedButError"));
                            errors.add(ActionMessages.GLOBAL_MESSAGE, 
                                new ActionMessage(pingResult.getMessage()));
                        }
                        else
                        {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, 
                                new ActionMessage("ping.transmissionFailed"));
                        }
                        saveErrors(req, errors);
                    }
                    else
                    {
                        ActionMessages messages = new ActionMessages();
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.successful"));
                        saveMessages(req, messages);
                    }
                }
            }
            catch (IOException ex)
            {
                mLogger.debug(ex);
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.transmissionFailed"));
                addSpecificMessages(ex, errors);
                saveErrors(req, errors);
            }
            catch (XmlRpcException ex)
            {
                mLogger.debug(ex);
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.transmissionFailed"));
                addSpecificMessages(ex, errors);
                saveErrors(req, errors);
            }
            return view(mapping, form , req, res);
        }
        catch (Exception ex)
        {
            mLogger.error("ERROR in action", ex);
            throw new ServletException(ex);
        }
    }

    // TODO: Consider unifying with other RollerRequest methods
    // Private helper to get ping target specified by request
    private PingTargetData select(RollerRequest rreq) throws RollerException
    {
        String pingTargetId = rreq.getRequest().getParameter(RollerRequest.PINGTARGETID_KEY);
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        if (pingTargetId == null || pingTargetId.length() == 0)
        {
            throw new RollerException("Missing ping target id: " + pingTargetId);
        }

        PingTargetData pingTarget = pingTargetMgr.retrievePingTarget(pingTargetId);
        if (pingTarget == null)
        {
            throw new RollerException("No such ping target id: " + pingTargetId);
        }
        return pingTarget;
    }

    private void addSpecificMessages(Exception ex, ActionMessages errors)
    {
        if (ex instanceof UnknownHostException)
        {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.unknownHost"));
        }
        else if (ex instanceof SocketException)
        {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.networkConnectionFailed"));
        }
    }

    private boolean isAuthorized(RollerRequest rreq, WebsiteData website) 
        throws RollerException
    {
        RollerSession rses = RollerSession.getRollerSession(rreq.getRequest());
        return rses.isUserAuthorizedToAdmin(website) 
            && !PingConfig.getDisablePingUsage();
    }
}
