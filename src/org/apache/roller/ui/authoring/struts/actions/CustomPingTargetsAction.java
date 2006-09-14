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
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.authoring.struts.forms.PingTargetForm;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;


/**
 * Administer custom ping targets.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @struts.action name="pingTargetForm" path="/roller-ui/authoring/customPingTargets" scope="request" parameter="method"
 * @struts.action-forward name="pingTargets.page" path=".CustomPingTargets"
 * @struts.action-forward name="pingTargetEdit.page" path=".CustomPingTargetEdit"
 * @struts.action-forward name="pingTargetDeleteOK.page" path=".CustomPingTargetDeleteOK"
 */
public class CustomPingTargetsAction extends BasePingTargetsAction {
    private static Log mLogger = LogFactory.getFactory().getInstance(CustomPingTargetsAction.class);

    public String getPingTargetsTitle() {
        return "customPingTargets.customPingTargets";
    }

    public String getPingTargetEditTitle() {
        return "pingTarget.pingTarget";
    }

    public String getPingTargetDeleteOKTitle() {
        return "pingTarget.confirmRemoveTitle";
    }

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
    protected List getPingTargets(RollerRequest rreq) throws RollerException {
        HttpServletRequest req = rreq.getRequest();
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();

        Boolean allowCustomTargets = new Boolean(!PingConfig.getDisallowCustomTargets());
        req.setAttribute("allowCustomTargets", allowCustomTargets);

        List customPingTargets = allowCustomTargets.booleanValue() ? pingTargetMgr.getCustomPingTargets(rreq.getWebsite()) : Collections.EMPTY_LIST;

        return customPingTargets;
    }

    /*
     * Create a new ping target (blank). Here we create a custom ping target for the website.
     */
    protected PingTargetData createPingTarget(RollerRequest rreq, PingTargetForm pingTargetForm) throws RollerException {
        return new PingTargetData(null, pingTargetForm.getName(), pingTargetForm.getPingUrl(), rreq.getWebsite(), false);
    }


    /*
     *  Check if the user has editing rights.
     */
    protected boolean hasRequiredRights(RollerRequest rreq, WebsiteData website) throws RollerException {
        RollerSession rses = RollerSession.getRollerSession(rreq.getRequest());
        return (rses.isUserAuthorizedToAdmin(website) && !PingConfig.getDisallowCustomTargets());
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return view(mapping, actionForm, request, response);
    }
}
