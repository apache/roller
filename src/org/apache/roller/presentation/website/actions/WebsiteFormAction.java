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

package org.apache.roller.presentation.website.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.BooleanUtils;

import org.apache.commons.lang.StringUtils;
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
import org.apache.velocity.VelocityContext;
import org.apache.roller.RollerException;
import org.apache.roller.RollerPermissionsException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PagePluginManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.BasePageModel;
import org.apache.roller.presentation.RollerContext;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.presentation.website.formbeans.WebsiteFormEx;
import org.apache.roller.util.Blacklist;


/////////////////////////////////////////////////////////////////////////////
/**
 * Website Settings action.
 *
 * @struts.action name="websiteFormEx" path="/editor/website"
 * 		scope="session" parameter="method"
 *
 * @struts.action-forward name="editWebsite.page" path=".edit-website"
 * @struts.action-forward name="removeWebsite.page" path=".WebsiteRemove"
 */
public final class WebsiteFormAction extends DispatchAction {
    private static Log mLogger =
            LogFactory.getFactory().getInstance(WebsiteFormAction.class);
    
    public ActionForward add(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return update( mapping, actionForm, request, response );
    }
    
    //-----------------------------------------------------------------------
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("editWebsite.page");
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAdmin(website)) {
                Roller roller = RollerFactory.getRoller();
                UserManager umgr = roller.getUserManager();
                WeblogManager wmgr = roller.getWeblogManager();
                UserData ud = rses.getAuthenticatedUser();
                request.setAttribute("user",ud);
                
                WebsiteFormEx wf = (WebsiteFormEx)actionForm;
                wf.copyFrom(website, request.getLocale());
                
                List cd = wmgr.getWeblogCategories(website, true);
                request.setAttribute("categories",cd);
                
                List bcd = wmgr.getWeblogCategories(website, false);
                request.setAttribute("bloggerCategories",bcd);
                
                List pages = umgr.getPages(website);
                request.setAttribute("pages",pages);
                
                ServletContext ctx = request.getSession().getServletContext();
                String editorPages =
                        RollerRuntimeConfig.getProperty("users.editor.pages");
                
                List epages = Arrays.asList(StringUtils.split(
                        StringUtils.deleteWhitespace(editorPages), ","));
                request.setAttribute("editorPagesList", epages);
                
                
                WebsitePageModel pageModel = new WebsitePageModel(
                        "websiteSettings.title", request, response, mapping, website);
                request.setAttribute("model",pageModel);
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    //-----------------------------------------------------------------------
    public ActionForward update(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        ActionErrors errors = new ActionErrors();
        ActionMessages messages = new ActionMessages();
        ActionForward forward = mapping.findForward("editWebsite");
        try {
            WebsiteFormEx form = (WebsiteFormEx)actionForm;
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            
            WebsiteData wd = umgr.getWebsite(form.getId());
            
            // Set website in request, so subsequent action gets it
            RollerRequest.getRollerRequest(request).setWebsite(wd);
            
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToAdmin(wd)) {
                checkBlacklist(errors, messages, form.getBlacklist());
                if (errors.isEmpty()) {
                    // ensure getEnabled can't be changed
                    form.setEnabled(wd.getEnabled());
                    form.copyTo(wd, request.getLocale());
                    
                    // ROL-485: comments not be allowed on inactive weblogs
                    if (wd.getActive() != null && !wd.getActive().booleanValue()) {
                        wd.setAllowComments(Boolean.FALSE);
                        messages.add(null, new ActionMessage(
                            "websiteSettings.commentsOffForInactiveWeblog"));
                    }
                                        
                    umgr.saveWebsite(wd);  
                    
                    // ROL-1050: apply comment defaults to existing entries
                    if (form.isApplyCommentDefaults()) {
                        wmgr.applyCommentDefaultsToEntries(wd);
                    }

                    RollerFactory.getRoller().getRefererManager().applyRefererFilters(wd);
                    
                    RollerFactory.getRoller().flush();
                    
                    messages.add(null,
                        new ActionMessage("websiteSettings.savedChanges"));
                    
                    request.getSession().setAttribute(
                        RollerRequest.WEBSITEID_KEY, form.getId());
                    
                    // Clear cache entries associated with website
                    CacheManager.invalidate(wd);
            
                    actionForm.reset(mapping,request);
                }
                
                // set the Editor Page list
                ServletContext ctx = request.getSession().getServletContext();
                String editorPages =
                        RollerRuntimeConfig.getProperty("users.editor.pages");
                
                List epages = Arrays.asList(StringUtils.split(
                        org.apache.commons.lang.StringUtils.deleteWhitespace(editorPages), ","));
                request.setAttribute("editorPagesList", epages);
                
                WebsitePageModel pageModel =
                        new WebsitePageModel("websiteSettings.title",
                        request, response, mapping, wd);
                request.setAttribute("model",pageModel);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (RollerPermissionsException e) {
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            forward = mapping.findForward("access-denied");
        } catch (RollerException re) {
            mLogger.error("Unexpected exception",re.getRootCause());
            throw new ServletException(re);
        } catch (Exception e) {
            mLogger.error("Unexpected exception",e);
            throw new ServletException(e);
        }
        if (errors.size() > 0) saveErrors(request, errors);
        if (messages.size() > 0) saveMessages(request, messages);
        return forward;
    }
    
    /** Count string and regex ignore words, check regex for Syntax errors */
    private void checkBlacklist(
            ActionErrors errors, ActionMessages messages, String blacklist) {
        List regexRules = new ArrayList();
        List stringRules = new ArrayList();
        try {
            // just for testing/counting, this does not persist rules in any way
            Blacklist.populateSpamRules(blacklist, stringRules, regexRules, null);
            messages.add(null, new ActionMessage(
                "websiteSettings.acceptedBlacklist",
                new Integer(stringRules.size()), new Integer(regexRules.size())));
        } catch (Throwable e) {
            errors.add(null, new ActionMessage(
                "websiteSettings.error.processingBlacklist", e.getMessage()));
        }
    }
    
    //-----------------------------------------------------------------------
    /** Send user to remove confirmation page */
    public ActionForward removeOk(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        WebsiteFormEx form = (WebsiteFormEx)actionForm;
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        WebsiteData website = umgr.getWebsite(form.getId());
        ActionForward forward = mapping.findForward("removeWebsite.page");
        request.setAttribute("model", new WebsitePageModel(
                "websiteRemove.title", request, response, mapping, website));
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAdmin(website)) {
                form.copyFrom(website, request.getLocale());
                request.setAttribute("website", website);
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    //-----------------------------------------------------------------------
    public ActionForward remove(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        ActionForward forward = mapping.findForward("yourWebsites");
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            WebsiteFormEx form = (WebsiteFormEx)actionForm;
            WebsiteData website = umgr.getWebsite(form.getId());
            
            RollerSession rses = RollerSession.getRollerSession(request);
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                
                // remove website
                umgr.removeWebsite(website);
                RollerFactory.getRoller().flush();
                
                CacheManager.invalidate(website);
                
                actionForm.reset(mapping, request);
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (RollerException e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError(
                    "error.internationalized",e.getRootCauseMessage()));
            saveErrors(request, errors);
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    public class WebsitePageModel extends BasePageModel {
        private List permissions = new ArrayList();
        private boolean groupBloggingEnabled = false;
        private boolean emailNotificationEnabled = false;
        private boolean moderationRequired = false;
        public WebsitePageModel(
                String titleKey,
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                WebsiteData website) throws RollerException {
            super(titleKey, request, response, mapping);
            this.website = website;
            Roller roller = RollerFactory.getRoller();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            UserData user = rollerSession.getAuthenticatedUser();
            permissions = roller.getUserManager().getAllPermissions(website);
            groupBloggingEnabled =
                RollerConfig.getBooleanProperty("groupblogging.enabled");
            emailNotificationEnabled = 
                RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
            moderationRequired =  
                RollerRuntimeConfig.getBooleanProperty("users.moderation.required");
        }
        public boolean isGroupBloggingEnabled() {
            return groupBloggingEnabled;
        }
        public boolean isEmailNotificationEnabled() {
            return emailNotificationEnabled;
        }
        public boolean isModerationRequired() {
            return moderationRequired;
        }
        public boolean getHasPagePlugins() {
            boolean ret = false;
            try {
                Roller roller = RollerFactory.getRoller();
                PagePluginManager ppmgr = roller.getPagePluginManager();
                ret = ppmgr.hasPagePlugins();
            } catch (RollerException e) {
                mLogger.error(e);
            }
            return ret;
        }
        
        public List getPagePlugins() {
            List list = new ArrayList();
            try {
                if (getHasPagePlugins()) {
                    Roller roller = RollerFactory.getRoller();
                    PagePluginManager ppmgr = roller.getPagePluginManager();
                    Map plugins = ppmgr.createAndInitPagePlugins(
                            getWebsite(),
                            RollerContext.getRollerContext().getServletContext(),
                            RollerContext.getRollerContext().getAbsoluteContextUrl(),
                            new VelocityContext());
                    Iterator it = plugins.values().iterator();
                    while (it.hasNext()) list.add(it.next());
                }
            } catch (Exception e) {
                mLogger.error(e);
            }
            return list;
        }
    }
}

