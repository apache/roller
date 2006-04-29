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

package org.roller.presentation.website.actions;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.PingTargetManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.forms.PingTargetForm;
import org.roller.presentation.weblog.actions.BasePingTargetsAction;

/**
 * Administer common ping targets.
 *
 * @struts.action name="pingTargetForm" path="/admin/commonPingTargets" scope="request" parameter="method"
 * @struts.action-forward name="pingTargets.page" path=".CommonPingTargets"
 * @struts.action-forward name="pingTargetEdit.page" path=".CommonPingTargetEdit"
 * @struts.action-forward name="pingTargetDeleteOK.page" path=".CommonPingTargetDeleteOK"
 */
public class CommonPingTargetsAction extends BasePingTargetsAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(CommonPingTargetsAction.class);

    protected Log getLogger() {
        return mLogger;
    }

    public String getPingTargetsTitle() 
    {
        return "commonPingTargets.commonPingTargets";    
    }
    public String getPingTargetEditTitle()
    {
        return "pingTarget.pingTarget";    
    }
    public String getPingTargetDeleteOKTitle() 
    {
        return "pingTarget.confirmRemoveTitle";    
    }
    
    /*
     * Get the ping targets for the view.  Here we return the common ping targets for the
     * entire site.
     */
    protected List getPingTargets(RollerRequest rreq) throws RollerException
    {
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        return pingTargetMgr.getCommonPingTargets();
    }

    /*
     * Create a new ping target (blank). Here we create a common ping target.
     */
    protected PingTargetData createPingTarget(RollerRequest rreq, PingTargetForm pingTargetForm)
        throws RollerException
    {
        return new PingTargetData(null, pingTargetForm.getName(), 
                pingTargetForm.getPingUrl(), null);
    }


    /*
     * Check if request carries admin rights.
     */
    protected boolean hasRequiredRights(
            RollerRequest rreq, WebsiteData website) throws RollerException
    {
        RollerSession rollerSession = 
                RollerSession.getRollerSession(rreq.getRequest());
        return rollerSession.isGlobalAdminUser();
    }
}
