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
/*
 * CacheInfoAction.java
 *
 * Created on November 11, 2005, 1:12 PM
 */

package org.apache.roller.ui.admin.struts.actions;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;


/**
 * Struts Action class which handles requests to the System Info page.
 *
 * @struts.action path="/admin/cacheInfo" scope="request" parameter="method"
 *
 * @struts.action-forward name="cacheInfo.page" path=".cacheInfo"
 *
 * @author Allen Gilliland
 */
public class CacheInfoAction extends DispatchAction {
    
    private static Log mLogger = LogFactory.getLog(CacheInfoAction.class);
    
    
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("cacheInfo.page");
        
        try {
            BasePageModel pageModel = new BasePageModel(
                    "cacheInfo.title", request, response, mapping);
            request.setAttribute("model",pageModel);                
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if (rollerSession.isGlobalAdminUser() ) {
                
                // caching instrumentation
                Map cacheStats = CacheManager.getStats();
                request.setAttribute("cacheStats", cacheStats);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        return forward;
    }
    
    
    /**
     * clear action.
     *
     * this is triggered when someone has indicated that they want to clear
     * one or all of the caches.
     */
    public ActionForward clear(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("cacheInfo.page");
        
        try {
            BasePageModel pageModel = new BasePageModel(
                    "cacheInfo.title", request, response, mapping);
            request.setAttribute("model",pageModel);                
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if (rollerSession.isGlobalAdminUser() ) {
                
                // see if a specific cache was specified
                String handlerClass = request.getParameter("cache");
                if(handlerClass != null && handlerClass.length() > 0) {
                    CacheManager.clear(handlerClass);
                } else {
                    CacheManager.clear();
                }
                
                // caching instrumentation
                Map cacheStats = CacheManager.getStats();
                request.setAttribute("cacheStats", cacheStats);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        return forward;
    }
}
