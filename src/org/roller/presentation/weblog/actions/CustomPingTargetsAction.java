/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.presentation.weblog.actions;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.PingConfig;
import org.roller.model.PingTargetManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.PingTargetData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.forms.PingTargetForm;

/**
 * Administer custom ping targets.
 *
 * @struts.action name="pingTargetForm" path="/editor/customPingTargets" scope="request" parameter="method"
 * @struts.action-forward name="pingTargets.page" path="/weblog/CustomPingTargets.jsp"
 * @struts.action-forward name="pingTargetEdit.page" path="/weblog/CustomPingTargetEdit.jsp"
 * @struts.action-forward name="pingTargetDeleteOK.page" path="/weblog/CustomPingTargetDeleteOK.jsp"
 */
public class CustomPingTargetsAction
    extends BasePingTargetsAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(CustomPingTargetsAction.class);


    public CustomPingTargetsAction() {
        super();
    }

    protected Log getLogger() {
        return mLogger;
    }

    /*
     * Get the ping targets for the view.  Here we return the custom ping targets for the
     * website and  set the value of attribute <code>allowCustomTargets</code> in the request.
     * If custom ping targets have been disallowed, we just return the empty list.
     */
    protected List getPingTargets(RollerRequest rreq) throws RollerException
    {
        HttpServletRequest req = rreq.getRequest();
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();

        Boolean allowCustomTargets = new Boolean(!PingConfig.getDisallowCustomTargets());
        req.setAttribute("allowCustomTargets", allowCustomTargets);

        List customPingTargets = allowCustomTargets.booleanValue() ?
            pingTargetMgr.getCustomPingTargets(RollerSession.getRollerSession(req).getCurrentWebsite()) : Collections.EMPTY_LIST;

        return customPingTargets;
    }

    /*
     * Create a new ping target (blank). Here we create a custom ping target for the website.
     */
    protected PingTargetData createPingTarget(RollerRequest rreq, PingTargetForm pingTargetForm)
        throws RollerException
    {
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        return pingTargetMgr.createCustomPingTarget(
            pingTargetForm.getName(), pingTargetForm.getPingUrl(), 
            RollerSession.getRollerSession(rreq.getRequest()).getCurrentWebsite());
    }


    /*
     *  Check if the user has editing rights.
     */
    protected boolean hasRequiredRights(RollerRequest rreq) throws RollerException
    {
        RollerSession rollerSession = RollerSession.getRollerSession(rreq.getRequest());
        return (rollerSession.isUserAuthorizedToEdit() && !PingConfig.getDisallowCustomTargets());
    }
}
