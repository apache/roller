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

package org.apache.roller.presentation.website.actions;

import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.RollerSession;
import org.apache.roller.presentation.forms.PingTargetForm;
import org.apache.roller.presentation.weblog.actions.BasePingTargetsAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Administer common ping targets.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
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
     * Set a ping target auto enabled to true.
     */
    public ActionForward enableSelected(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        PingTargetData pingTarget = select(rreq);
        try
        {
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward("access-denied");
            }
            pingTarget.setAutoEnabled(true);
            RollerFactory.getRoller().flush();

            return view(mapping, form, req, res);
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
    }

    /*
     * Set a pint target auto enabled to false.
     */
    public ActionForward disableSelected(ActionMapping mapping, ActionForm form,
                                         HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        PingTargetData pingTarget = select(rreq);
        try
        {
            if (!hasRequiredRights(rreq, rreq.getWebsite()))
            {
                return mapping.findForward("access-denied");
            }
            pingTarget.setAutoEnabled(false);
            RollerFactory.getRoller().flush();
        
            return view(mapping, form, req, res);
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action", e);
            throw new ServletException(e);
        }
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
                pingTargetForm.getPingUrl(), null, pingTargetForm.isAutoEnabled());
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
