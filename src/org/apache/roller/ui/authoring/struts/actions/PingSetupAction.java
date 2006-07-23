/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.PingConfig;
import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.core.pings.WeblogUpdatePinger;
import org.apache.struts.action.*;
import org.apache.struts.actions.DispatchAction;
import org.apache.xmlrpc.XmlRpcException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Actions for setting up automatic ping configuration for a weblog.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @struts.action name="pingSetupForm" path="/roller-ui/authoring/pingSetup" scope="request" parameter="method"
 * @struts.action-forward name="pingSetup.page" path=".Pings"
 * @struts.action-forward name="pingResult.page" path=".PingResult"
 */
public class PingSetupAction extends DispatchAction {
    private static Log mLogger = LogFactory.getFactory().getInstance(PingSetupAction.class);

    private static final String PING_SETUP_PAGE = "pingSetup.page";

    /* (non-Javadoc)
     * @see org.apache.struts.actions.DispatchAction#unspecified(
     * 	org.apache.struts.action.ActionMapping,
     *  org.apache.struts.action.ActionForm,
     *  javax.servlet.http.HttpServletRequest,
     *  javax.servlet.http.HttpServletResponse)
     */
    protected ActionForward unspecified(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return view(mapping, actionForm, request, response);
    }

    /*
     * Display the common ping targets with page
     */
    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ActionForward forward = mapping.findForward(PING_SETUP_PAGE);
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        WebsiteData website = rreq.getWebsite();
        try {
            if (!isAuthorized(rreq, website)) {
                return mapping.findForward("access-denied");
            }

            BasePageModel pageModel = new BasePageModel("pings.title", req, res, mapping);
            req.setAttribute("model", pageModel);

            List commonPingTargets = pingTargetMgr.getCommonPingTargets();
            req.setAttribute("commonPingTargets", commonPingTargets);

            Boolean allowCustomTargets = new Boolean(!PingConfig.getDisallowCustomTargets());
            req.setAttribute("allowCustomTargets", allowCustomTargets);

            List customPingTargets = allowCustomTargets.booleanValue() ? pingTargetMgr.getCustomPingTargets(website) : Collections.EMPTY_LIST;
            req.setAttribute("customPingTargets", customPingTargets);

            // Build isEnabled map (keyed by ping target id and values Boolean.TRUE/Boolean.FALSE)
            Map isEnabled = buildIsEnabledMap(rreq, commonPingTargets, customPingTargets);
            req.setAttribute("isEnabled", isEnabled);

            return forward;
        } catch (Exception e) {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Private helper to build a map indexed by ping target id with values Boolean.TRUE and Boolean.FALSE
     * based on whether the ping target is enabled (has a corresponding auto ping configuration).
     */
    private Map buildIsEnabledMap(RollerRequest rreq, List commonPingTargets, List customPingTargets) throws RollerException {
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        WebsiteData website = rreq.getWebsite();

        // Build isEnabled map (keyed by ping target id and values Boolean.TRUE/Boolean.FALSE)
        Map isEnabled = new HashMap();
        List autopings = autoPingMgr.getAutoPingsByWebsite(website);
        // Add the enabled auto ping configs with TRUE
        for (Iterator i = autopings.iterator(); i.hasNext();) {
            AutoPingData autoPing = (AutoPingData) i.next();
            isEnabled.put(autoPing.getPingTarget().getId(), Boolean.TRUE);
        }
        // Somewhat awkward, but the two loops save building a separate combined list.
        // Add disabled common ones with FALSE
        for (Iterator i = commonPingTargets.iterator(); i.hasNext();) {
            PingTargetData pingTarget = (PingTargetData) i.next();
            if (isEnabled.get(pingTarget.getId()) == null) {
                isEnabled.put(pingTarget.getId(), Boolean.FALSE);
            }
        }
        // Add disabled custom ones with FALSE
        for (Iterator i = customPingTargets.iterator(); i.hasNext();) {
            PingTargetData pingTarget = (PingTargetData) i.next();
            if (isEnabled.get(pingTarget.getId()) == null) {
                isEnabled.put(pingTarget.getId(), Boolean.FALSE);
            }
        }
        return isEnabled;
    }

    /*
     * Enable a ping target.
     */
    public ActionForward enableSelected(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        PingTargetData pingTarget = select(rreq);
        try {
            if (!isAuthorized(rreq, rreq.getWebsite())) {
                return mapping.findForward("access-denied");
            }
            AutoPingData autoPing = new AutoPingData(null, pingTarget, rreq.getWebsite());
            autoPingMgr.saveAutoPing(autoPing);
            RollerFactory.getRoller().flush();

            return view(mapping, form, req, res);
        } catch (Exception e) {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Load delete confirmation view.
     */
    public ActionForward disableSelected(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        PingTargetData pingTarget = select(rreq);
        try {
            if (!isAuthorized(rreq, rreq.getWebsite())) {
                return mapping.findForward("access-denied");
            }
            autoPingMgr.removeAutoPing(pingTarget, rreq.getWebsite());
            RollerFactory.getRoller().flush();

            return view(mapping, form, req, res);
        } catch (Exception e) {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Ping the selected target now.
     */
    public ActionForward pingSelectedNow(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws Exception {
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(req);
            PingTargetData pingTarget = select(rreq);
            WebsiteData website = rreq.getWebsite();
            try {
                if (!isAuthorized(rreq, website)) {
                    return mapping.findForward("access-denied");
                }
                if (PingConfig.getSuspendPingProcessing()) {
                    if (mLogger.isDebugEnabled()) mLogger.debug("Ping processing is disabled.");
                    ActionMessages errors = new ActionMessages();
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.pingProcessingIsSuspended"));
                    saveErrors(req, errors);
                } else {
                    WeblogUpdatePinger.PingResult pingResult = WeblogUpdatePinger.sendPing(pingTarget, website);
                    if (pingResult.isError()) {
                        if (mLogger.isDebugEnabled()) mLogger.debug("Ping Result: " + pingResult);
                        ActionMessages errors = new ActionMessages();
                        if (pingResult.getMessage() != null && pingResult.getMessage().trim().length() > 0) {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.transmittedButError"));
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(pingResult.getMessage()));
                        } else {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.transmissionFailed"));
                        }
                        saveErrors(req, errors);
                    } else {
                        ActionMessages messages = new ActionMessages();
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.successful"));
                        saveMessages(req, messages);
                    }
                }
            } catch (IOException ex) {
                mLogger.debug(ex);
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.transmissionFailed"));
                addSpecificMessages(ex, errors);
                saveErrors(req, errors);
            } catch (XmlRpcException ex) {
                mLogger.debug(ex);
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.transmissionFailed"));
                addSpecificMessages(ex, errors);
                saveErrors(req, errors);
            }
            return view(mapping, form, req, res);
        } catch (Exception ex) {
            mLogger.error("ERROR in action", ex);
            throw new ServletException(ex);
        }
    }

    // TODO: Consider unifying with other RollerRequest methods
    // Private helper to get ping target specified by request
    private PingTargetData select(RollerRequest rreq) throws RollerException {
        String pingTargetId = rreq.getRequest().getParameter(RequestConstants.PINGTARGET_ID);
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        if (pingTargetId == null || pingTargetId.length() == 0) {
            throw new RollerException("Missing ping target id: " + pingTargetId);
        }

        PingTargetData pingTarget = pingTargetMgr.getPingTarget(pingTargetId);
        if (pingTarget == null) {
            throw new RollerException("No such ping target id: " + pingTargetId);
        }
        return pingTarget;
    }

    private void addSpecificMessages(Exception ex, ActionMessages errors) {
        if (ex instanceof UnknownHostException) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.unknownHost"));
        } else if (ex instanceof SocketException) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("ping.networkConnectionFailed"));
        }
    }

    private boolean isAuthorized(RollerRequest rreq, WebsiteData website) throws RollerException {
        RollerSession rses = RollerSession.getRollerSession(rreq.getRequest());
        return rses.isUserAuthorizedToAdmin(website) && !PingConfig.getDisablePingUsage();
    }
}
