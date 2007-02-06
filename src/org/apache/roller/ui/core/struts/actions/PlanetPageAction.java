/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.ui.core.struts.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * Old Roller planet page action.  This action is no longer functional and 
 * instead just dispatches to a simple jsp which does a redirect.
 *
 * Since there is no guaranteed forwarding url for this page the planet.jsp
 * file is open so that it's fairly easy for site admins to set the redirect
 * to anywhere they choose.
 *
 * @struts.action name="planet" path="/planet" scope="request"
 * @struts.action-forward name="planet.page" path="/WEB-INF/jsps/core/planet.jsp"
 */
public class PlanetPageAction extends Action {
    
    
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                    HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // this has been EOLed, so this just goes to a single planet jsp now
        return mapping.findForward("planet.page");
    }
    
}
