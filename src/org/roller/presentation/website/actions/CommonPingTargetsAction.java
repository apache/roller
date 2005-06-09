/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.PingTargetManager;
import org.roller.pojos.PingTargetData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.forms.PingTargetForm;
import org.roller.presentation.weblog.actions.BasePingTargetsAction;

import java.util.List;

/**
 * Administer common ping targets.
 *
 * @struts.action name="pingTargetForm" path="/admin/commonPingTargets" scope="request" parameter="method"
 * @struts.action-forward name="pingTargets.page" path="/website/CommonPingTargets.jsp"
 * @struts.action-forward name="pingTargetEdit.page" path="/website/CommonPingTargetEdit.jsp"
 * @struts.action-forward name="pingTargetDeleteOK.page" path="/website/CommonPingTargetDeleteOK.jsp"
 */
public class CommonPingTargetsAction extends BasePingTargetsAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(CommonPingTargetsAction.class);

    protected Log getLogger() {
        return mLogger;
    }

    /*
     * Get the ping targets for the view.  Here we return the common ping targets for the
     * entire site.
     */
    protected List getPingTargets(RollerRequest rreq) throws RollerException
    {
        PingTargetManager pingTargetMgr = rreq.getRoller().getPingTargetManager();
        return pingTargetMgr.getCommonPingTargets();
    }

    /*
     * Create a new ping target (blank). Here we create a common ping target.
     */
    protected PingTargetData createPingTarget(RollerRequest rreq, PingTargetForm pingTargetForm)
        throws RollerException
    {
        PingTargetManager pingTargetMgr = rreq.getRoller().getPingTargetManager();
        return pingTargetMgr.createCommonPingTarget(
            pingTargetForm.getName(), pingTargetForm.getPingUrl());
    }


    /*
     * Check if request carries admin rights.
     */
    protected boolean hasRequiredRights(RollerRequest rreq) throws RollerException
    {
        // This mimics the check in other admin actions, but not sure why the latter is not sufficient.
        return (rreq.isUserAuthorizedToEdit() && rreq.isAdminUser());
    }
}
