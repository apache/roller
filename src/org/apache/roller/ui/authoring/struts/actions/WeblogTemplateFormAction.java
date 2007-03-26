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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.RollerPermissionsException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogTemplateFormEx;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.Utilities;
import org.apache.roller.util.cache.CacheManager;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.LabelValueBean;


/**
 * Handles actions for weblog template management.
 *
 * @struts.action name="weblogTemplateFormEx" path="/roller-ui/authoring/page"
 *  	scope="session" parameter="method"
 *
 * @struts.action-forward name="removePage.page" path=".remove-page"
 * @struts.action-forward name="editPage.page" path=".edit-page"
 * @struts.action-forward name="editPages.page" path=".edit-pages"
 */
public final class WeblogTemplateFormAction extends DispatchAction {
    
    private static Log log = LogFactory.getLog(WeblogTemplateFormAction.class);
    
    protected static ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");
    
    
    public ActionForward add(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("editPages.page");
        WeblogTemplateFormEx form = (WeblogTemplateFormEx)actionForm;
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                

                UserManager mgr = RollerFactory.getRoller().getUserManager();
                
                // first off, check if template already exists
                WeblogTemplate existingPage = mgr.getPageByName(website, form.getName());
                if(existingPage != null) {
                    ActionErrors errors = new ActionErrors();
                    errors.add(null, new ActionError("pagesForm.error.alreadyExists", form.getName()));
                    saveErrors(request, errors);
                    request.setAttribute("model",
                        new WeblogTemplateFormModel("pagesForm.title", request, response, mapping, null));
                    return forward;
                }
                
                WeblogTemplate page = new WeblogTemplate();
                form.copyTo(page, request.getLocale());
                page.setWebsite(website);
                page.setLastModified( new Date() );
                page.setDescription(page.getName());
                page.setContents(bundle.getString("pageForm.newTemplateContent"));
                
                // if no action specified then it's a custom page
                if(page.getAction() == null) {
                    page.setAction(WeblogTemplate.ACTION_CUSTOM);
                }
                
                validateLink(page);
                
                // all templates start out as velocity templates
                page.setTemplateLanguage("velocity");
                
                // for now, all templates just use _decorator
                if(!"_decorator".equals(page.getName())) {
                    page.setDecoratorName("_decorator");
                }
                
                // save the page
                mgr.savePage( page );
                
                // if this person happened to create a Weblog template from
                // scratch then make sure and set the defaultPageId
                if(WeblogTemplate.DEFAULT_PAGE.equals(page.getName())) {
                    website.setDefaultPageId(page.getId());
                    mgr.saveWebsite(website);
                }
                
                // flush results to db
                RollerFactory.getRoller().flush();
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("pagesForm.addNewPage.success",
                        page.getName()));
                saveMessages(request, uiMessages);
                
                actionForm.reset(mapping,request);
                
                request.setAttribute("model",
                    new WeblogTemplateFormModel("pagesForm.title", request, response, mapping, page));
                
                // TODO: when a new template is successfully added then we should
                // take the user directly to edit that new page
                
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        return forward;
    }
    
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("editPage.page");
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogTemplate page = (WeblogTemplate)rreq.getPage();
            
            RollerSession rses = RollerSession.getRollerSession(request);
            if ( rses.isUserAuthorizedToAdmin(page.getWebsite()) ) {
                
                WeblogTemplateFormEx form = (WeblogTemplateFormEx)actionForm;
                form.copyFrom(page, request.getLocale());
                
                // empty content-type indicates that page uses auto content-type detection
                if (StringUtils.isEmpty(page.getOutputContentType())) {
                    form.setAutoContentType(Boolean.TRUE);
                } else {
                    form.setAutoContentType(Boolean.FALSE);
                    form.setManualContentType(page.getOutputContentType());
                }
                
                request.setAttribute("model",
                    new WeblogTemplateFormModel("pageForm.title", request, response, mapping, page));

            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    public ActionForward editPages(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("editPages.page");
        try {
            WeblogTemplateFormEx form = (WeblogTemplateFormEx)actionForm;
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);

            WebsiteData website = rreq.getWebsite();
            if (website == null && form.getId()!=null) {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                WeblogTemplate template = mgr.getPage(form.getId());
                website = template.getWebsite();
            }
            
            WeblogTemplateFormModel model = 
                    new WeblogTemplateFormModel("pagesForm.title", request, response, mapping, null);
            request.setAttribute("model", model);
            
            List availableActions = new ArrayList();
            availableActions.add(WeblogTemplate.ACTION_CUSTOM);
            
            if(Theme.CUSTOM.equals(website.getEditorTheme())) {
                // if the weblog is using a custom theme then determine which
                // action templates are still available to be created
                availableActions.add(WeblogTemplate.ACTION_PERMALINK);
                availableActions.add(WeblogTemplate.ACTION_SEARCH);
                availableActions.add(WeblogTemplate.ACTION_WEBLOG);
                availableActions.add(WeblogTemplate.ACTION_TAGSINDEX);
                
                WeblogTemplate tmpPage = null;
                Iterator pagesIter = model.getPages().iterator();
                while(pagesIter.hasNext()) {
                    tmpPage = (WeblogTemplate) pagesIter.next();
                    
                    if(!WeblogTemplate.ACTION_CUSTOM.equals(tmpPage.getAction())) {
                        availableActions.remove(tmpPage.getAction());
                    }
                }
            }
            request.setAttribute("availableActions", availableActions);
            
            if (!rses.isUserAuthorizedToAdmin(website)) {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    public ActionForward remove(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("editPages");
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplateFormEx form = (WeblogTemplateFormEx)actionForm;
            WeblogTemplate template = mgr.getPage(form.getId());
            WebsiteData website = template.getWebsite();
            
            WeblogTemplateFormModel pageModel = 
                new WeblogTemplateFormModel("pagesForm.title", request, response, mapping, null);
            request.setAttribute("model", pageModel);

            RollerSession rses = RollerSession.getRollerSession(request);
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                if(!template.isRequired()) {
                    
                    mgr.removePage(template);
                    RollerFactory.getRoller().flush();
                    
                    // notify cache
                    CacheManager.invalidate(template);
                } else {
                    
                    // someone trying to remove a required template
                    throw new RollerException("Cannot remove required page");
                }
                
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
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    /** Send user to remove confirmation page */
    public ActionForward removeOk(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("removePage.page");
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogTemplate page = (WeblogTemplate) rreq.getPage();
            WebsiteData website = page.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) ) {
                WeblogTemplateFormEx form = (WeblogTemplateFormEx)actionForm;
                form.copyFrom(page, request.getLocale());
                
                WeblogTemplateFormModel pageModel = 
                    new WeblogTemplateFormModel("editPages.title.removeOK", request, response, mapping, page);
                request.setAttribute("model", pageModel);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    public ActionForward update(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("editPage.page");
        try {
            WeblogTemplateFormEx form = (WeblogTemplateFormEx)actionForm;

            RollerRequest  rreq = RollerRequest.getRollerRequest(request);
            UserManager    mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplate page = mgr.getPage(form.getId());
            WebsiteData    website = page.getWebsite();
            
            
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAdmin(website)) {
                

                form.copyTo(page, request.getLocale());
                page.setLastModified( new Date() );                                
                
                if (form.getAutoContentType() == null || !form.getAutoContentType().booleanValue()) { 
                    page.setOutputContentType(form.getManualContentType());
                } else {
                    // empty content-type indicates that page uses auto content-type detection
                    page.setOutputContentType(null);
                }
                
                validateLink(page);

                mgr.savePage( page );
                RollerFactory.getRoller().flush();
                
                // set the (possibly) new link back into the Form bean
                ((WeblogTemplateFormEx)actionForm).setLink( page.getLink() );
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("pageForm.save.success",
                        page.getName()));
                saveMessages(request, uiMessages);
                
                CacheManager.invalidate(page);
                
                WeblogTemplateFormModel pageModel = 
                    new WeblogTemplateFormModel("pageForm.title", request, response, mapping, page);
                request.setAttribute("model", pageModel);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
            // Don't reset this form. Allow user to keep on tweaking.
            //actionForm.reset(mapping,request);
        } catch (RollerPermissionsException e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    /**
     * Ensures that the page has a safe value for the link
     * field.  "Safe" is defined as containing no html
     * or any other non-alphanumeric characters.
     * While this is overly strict (there are non-alphanum
     * characters that are web-safe), this is a much easier
     * test-and-correct.  Otherwise we would need a RegEx package.
     */
    private void validateLink( WeblogTemplate page ) {
        // if page.getLink() is null or empty
        // use the title ( page.getName() )
        if ( StringUtils.isEmpty( page.getLink() ) ) {
            page.setLink( page.getName() );
        }
        
        // if link contains any nonAlphanumeric, strip them
        // first we must remove any html, as this is
        // non-instructional markup.  Then do a straight
        // removeNonAlphanumeric.
        if ( !StringUtils.isAlphanumeric( page.getLink() ) ) {
            String link = Utilities.removeHTML( page.getLink() );
            link = Utilities.removeNonAlphanumeric( link );
            page.setLink( link );
        }
    }
    
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        try {
            request.setAttribute("model", new WeblogTemplateFormModel(
                    "pagesForm.title", request, response, mapping, null));
            return (mapping.findForward("editPages"));
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new ServletException(e);
        }
    }
    
    public class WeblogTemplateFormModel extends BasePageModel {
        private UserData       user;
        private WeblogTemplate page;
        private List           pages;
        private List           languages = new ArrayList();
        
        public WeblogTemplateFormModel(
                String titleKey,
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                WeblogTemplate page) throws RollerException {

            super(titleKey, request, response, mapping);

            UserManager mgr = RollerFactory.getRoller().getUserManager();
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);  
            
            if (page != null) {
                this.setWebsite(page.getWebsite());
            } else {
                this.setWebsite(rreq.getWebsite());
            }
 
            this.setUser(rses.getAuthenticatedUser());        
            this.setPages(mgr.getPages(getWebsite()));
            this.setPage(page);
            
            if (page != null) {
                String langs = RollerConfig.getProperty("rendering.templateLanguages","velocity");
                String[] langsArray = Utilities.stringToStringArray(langs, ",");
                for (int i = 0; i < langsArray.length; i++) {
                    getLanguages().add(new LabelValueBean(langsArray[i], langsArray[i]));
                }
            }
        }

        public UserData getUser() {
            return user;
        }

        public void setUser(UserData user) {
            this.user = user;
        }

        public WeblogTemplate getPage() {
            return page;
        }

        public void setPage(WeblogTemplate page) {
            this.page = page;
        }

        public List getPages() {
            return pages;
        }

        public void setPages(List pages) {
            this.pages = pages;
        }

        public List getLanguages() {
            return languages;
        }

        public void setLanguages(List languages) {
            this.languages = languages;
        }
    }

}



