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

package org.apache.roller.presentation.weblog.actions;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.BasePageModel;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.RollerSession;
import org.apache.roller.util.cache.CacheManager;

/**
 * Display today's referers.
 * @struts.action name="refererForm" path="/editor/referers"
 *      scope="session" parameter="method"
 * 
 * @struts.action-forward name="referers.page" path=".referers"
 */
public class ReferersAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(ReferersAction.class);

    public ActionForward unspecified(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        return view(mapping, actionForm, request, response);
    }

    /**
     * execute
     */
    public ActionForward view(
        ActionMapping mapping, ActionForm form,
        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        ActionForward forward = mapping.findForward("referers.page");
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        RefererManager refmgr = RollerFactory.getRoller().getRefererManager();
        try
        {
            if (rreq.getWebsite() != null
                 && rollerSession.isUserAuthorizedToAuthor(rreq.getWebsite()) )
            {
                BasePageModel pageModel = new BasePageModel(
                        "referers.todaysReferers", req, res, mapping);
                req.setAttribute("model", pageModel);
                req.setAttribute("pageHits",
                    new Integer(refmgr.getDayHits(rreq.getWebsite())));

                req.setAttribute("totalHits",
                    new Integer(refmgr.getTotalHits(rreq.getWebsite())));

                List refs = refmgr.getTodaysReferers(rreq.getWebsite());
                req.setAttribute("referers",refs);
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }

        return forward;
    }

    public ActionForward reset(
        ActionMapping mapping, ActionForm form,
        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        this.servlet.log("ReferersAction.reset()");
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        try
        {
            if (rreq.getWebsite() != null
                  && rollerSession.isUserAuthorizedToAuthor(rreq.getWebsite()) )
            {
                RefererManager refmgr = RollerFactory.getRoller().getRefererManager();
                WebsiteData website = rreq.getWebsite();
                refmgr.clearReferrers(website);
                RollerFactory.getRoller().flush();
                
                CacheManager.invalidate(website);
            }
            this.servlet.log("ReferersAction.reset(): don't have permission");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return view(mapping, form, req, res);
    }

    public ActionForward delete(
            ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        //this.servlet.log("ReferersAction.delete()");
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        try
        {
            if (rreq.getWebsite() != null
                 && rollerSession.isUserAuthorizedToAuthor(rreq.getWebsite()) )
            {
                RefererManager refmgr = RollerFactory.getRoller().getRefererManager();
                WebsiteData website = rreq.getWebsite();
                
                RefererData referer = null;
                String[] deleteIds = req.getParameterValues("id");
                if (deleteIds != null)
                {
                    for (int i=0; i<deleteIds.length; i++)
                    {
                        referer = refmgr.getReferer(deleteIds[i]);
                        refmgr.removeReferer(referer);
                    }
                    RollerFactory.getRoller().flush();
                    
                    CacheManager.invalidate(website);
                    
                    ActionMessages messages = new ActionMessages();
                    messages.add(null, new ActionMessage("referers.deletedReferers"));
                    saveMessages(req, messages);
                }
                else
                {
                    ActionErrors errors = new ActionErrors();
                    errors.add(null, new ActionError("referers.noReferersSpecified"));
                    saveErrors(req, errors);
                }
            }
            //this.servlet.log("ReferersAction.delete(): don't have permission");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return view(mapping, form, req, res);
    }
}
