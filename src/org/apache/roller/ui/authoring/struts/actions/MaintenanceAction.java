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

package org.apache.roller.ui.authoring.struts.actions;

import java.io.IOException;
import java.util.Date;
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
import org.apache.roller.RollerException;
import org.apache.roller.model.IndexManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;


/**
 * Allows user to perform Website maintenence operations such as flushing
 * the website page cache or re-indexing the website search index.
 *
 * @struts.action path="/roller-ui/authoring/maintenance" name="maintenanceForm"
 *     scope="request" parameter="method"
 *
 * @struts.action-forward name="maintenance.page" path=".Maintenance"
 */
public class MaintenanceAction extends DispatchAction {
    
    private static Log mLogger = LogFactory.getLog(MaintenanceAction.class);
    
    
    /* (non-Javadoc)
     * @see org.apache.struts.actions.DispatchAction#unspecified(
     * 	org.apache.struts.action.ActionMapping,
     *  org.apache.struts.action.ActionForm,
     *  javax.servlet.http.HttpServletRequest,
     *  javax.servlet.http.HttpServletResponse)
     */
    protected ActionForward unspecified(
            ActionMapping   mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws ServletException {
        
        request.setAttribute("model", new BasePageModel(
                "maintenance.title", request, response, mapping));
        return mapping.findForward("maintenance.page");
    }
    
    
    /**
     * Respond to user's request to rebuild search index.
     */
    public ActionForward index(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        try {
            RollerRequest rreq  = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            RollerSession rses = RollerSession.getRollerSession(request);
            
            if (rses.isUserAuthorizedToAdmin(website) ) {
                IndexManager manager =
                        RollerFactory.getRoller().getIndexManager();
                manager.rebuildWebsiteIndex(website);
                
                ActionMessages messages = new ActionMessages();
                messages.add(null, new ActionMessage("maintenance.message.indexed"));
                saveMessages(request, messages);
            }
            
            request.setAttribute("model", new BasePageModel(
                    "maintenance.title", request, response, mapping));
            
        } catch (RollerException re) {
            mLogger.error("Unexpected exception",re.getRootCause());
            throw new ServletException(re);
        } catch (Exception e) {
            mLogger.error("Unexpected exception",e);
            throw new ServletException(e);
        }
        
        return mapping.findForward("maintenance.page");
    }
    
    
    /**
     * Respond to request to flush a user's page cache.
     */
    public ActionForward flushCache(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        try {
            RollerRequest rreq  = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            RollerSession rses = RollerSession.getRollerSession(request);
            
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                
                // some caches are based on weblog last-modified, so update it
                website.setLastModified(new Date());
                
                try {
                    UserManager umgr = RollerFactory.getRoller().getUserManager();
                    umgr.saveWebsite(website);
                    RollerFactory.getRoller().flush();
                } catch (RollerException ex) {
                    mLogger.error("Error saving website", ex);
                }
                
                // also notify cache manager
                CacheManager.invalidate(website);
                
                ActionMessages messages = new ActionMessages();
                messages.add(null, new ActionMessage("maintenance.message.flushed"));
                saveMessages(request, messages);
                
            }
            
            request.setAttribute("model", new BasePageModel(
                    "maintenance.title", request, response, mapping));
            
        } catch (Exception e) {
            throw new ServletException(e);
        }
        
        return mapping.findForward("maintenance.page");
    }
    
}
