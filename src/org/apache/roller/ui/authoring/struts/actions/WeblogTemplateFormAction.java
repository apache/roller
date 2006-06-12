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
import java.util.HashSet;
import java.util.Iterator;
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
import org.apache.roller.RollerException;
import org.apache.roller.RollerPermissionsException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.authoring.struts.forms.WeblogTemplateForm;
import org.apache.roller.util.StringUtils;
import org.apache.roller.util.Utilities;
/////////////////////////////////////////////////////////////////////////////
/**
 * Page form action.
 * @struts.action name="weblogTemplateForm" path="/roller-ui/authoring/page"
 *  	scope="session" parameter="method"
 * 
 * @struts.action-forward name="removePage.page" path=".remove-page"
 * @struts.action-forward name="editPage.page" path=".edit-page"
 * @struts.action-forward name="editPages.page" path=".edit-pages"
 */
public final class WeblogTemplateFormAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(WeblogTemplateFormAction.class);
        
    public ActionForward add(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editPages.page");
        try
        {
            request.setAttribute("model", new BasePageModel(
                "pagesForm.title", request, response, mapping));   
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
                WeblogTemplate data = new WeblogTemplate();
                form.copyTo(data, request.getLocale());
                data.setWebsite(website);
                data.setLastModified( new java.util.Date() );
                data.setDescription("");
                data.setContents("");
                validateLink( data );

                UserManager mgr = RollerFactory.getRoller().getUserManager();
                mgr.savePage( data );
                RollerFactory.getRoller().flush();
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pagesForm.addNewPage.success", 
                                data.getName()));
                saveMessages(request, uiMessages);
                    
                actionForm.reset(mapping,request);                
                
                addModelObjects(request, response, mapping, website);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editPage.page");
        try
        {            
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogTemplate pd = (WeblogTemplate)rreq.getPage();
            
            RollerSession rses = RollerSession.getRollerSession(request);            
            if ( rses.isUserAuthorizedToAdmin(pd.getWebsite()) )
            {
                BasePageModel pageModel = new BasePageModel(
                    "pageForm.title", request, response, mapping);
                pageModel.setWebsite(pd.getWebsite());
                request.setAttribute("model", pageModel); 
                
                WeblogTemplateForm pf = (WeblogTemplateForm)actionForm;
                pf.copyFrom(pd, request.getLocale());
                
                addModelObjects(request, response, mapping, pd.getWebsite());
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward editPages(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editPages.page");
        try
        {
            WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            request.setAttribute("model", new BasePageModel(
                "pagesForm.title", request, response, mapping)); 
            
            WebsiteData website = rreq.getWebsite();
            if (website == null && form.getId()!=null) 
            {
                UserManager mgr = RollerFactory.getRoller().getUserManager();                
                WeblogTemplate template = mgr.getPage(form.getId());
                website = template.getWebsite();
            }
            
            if ( rses.isUserAuthorizedToAdmin(website))
            {
                addModelObjects(request, response, mapping, website);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
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
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editPages");
        request.setAttribute("model", new BasePageModel(
            "pagesForm.title", request, response, mapping));
        try
        {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
            WeblogTemplate template = mgr.getPage(form.getId());
            WebsiteData website = template.getWebsite();
            
            RollerSession rses = RollerSession.getRollerSession(request);          
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                if(!template.isRequired()) {
                    
                    mgr.removePage(template);
                    RollerFactory.getRoller().flush();
                    
                    // notify cache
                    CacheManager.invalidate(template);
                } else {
                    
                    // someone trying to remove a required template
                    throw new RollerException("Cannot remove required page");
                }
                
                addModelObjects(
                        request, response, mapping, template.getWebsite());
                actionForm.reset(mapping, request);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (RollerException e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError(
                    "error.internationalized",e.getRootCauseMessage()));
            saveErrors(request, errors);       
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /** Send user to remove confirmation page */
    public ActionForward removeOk(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("removePage.page");
        try
        {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogTemplate page = (WeblogTemplate) rreq.getPage();
            WebsiteData website = page.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
                form.copyFrom(page, request.getLocale());

                addModelObjects(request, response, mapping, page.getWebsite());
                
                BasePageModel pageModel = new BasePageModel(
                    "editPages.title.removeOK", request, response, mapping);
                pageModel.setWebsite(website);
                request.setAttribute("model", pageModel);

                UserData ud = rses.getAuthenticatedUser();
                request.setAttribute("user",ud);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
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
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editPage.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplate data = mgr.getPage(form.getId());
            WebsiteData website = data.getWebsite();
            
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAdmin(website))
            {
                form.copyTo(data, request.getLocale());
                data.setLastModified( new java.util.Date() );

                validateLink( data );

                mgr.savePage( data );
                RollerFactory.getRoller().flush();
                
                // set the (possibly) new link back into the Form bean
                ((WeblogTemplateForm)actionForm).setLink( data.getLink() );

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pageForm.save.success", 
                                data.getName()));
                saveMessages(request, uiMessages);

                CacheManager.invalidate(data);
                
                BasePageModel pageModel = new BasePageModel(
                    "pageForm.title", request, response, mapping);
                pageModel.setWebsite(website);
                request.setAttribute("model", pageModel);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
              
            // Don't reset this form. Allow user to keep on tweaking.        
            //actionForm.reset(mapping,request);
        }
        catch (RollerPermissionsException e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Ensures that the page has a safe value for the link
     * field.  "Safe" is defined as containing no html
     * or any other non-alphanumeric characters.
     * While this is overly strict (there are non-alphanum
     * characters that are web-safe), this is a much easier
     * test-and-correct.  Otherwise we would need a RegEx package.
     */
    private void validateLink( WeblogTemplate data )
    {
        // if data.getLink() is null or empty
        // use the title ( data.getName() )
        if ( StringUtils.isEmpty( data.getLink() ) )
        {
            data.setLink( data.getName() );
        }

        // if link contains any nonAlphanumeric, strip them
        // first we must remove any html, as this is
        // non-instructional markup.  Then do a straight
        // removeNonAlphanumeric.
        if ( !StringUtils.isAlphanumeric( data.getLink() ) )
        {
            String link = Utilities.removeHTML( data.getLink() );
            link = Utilities.removeNonAlphanumeric( link );
            data.setLink( link );
        }
    }
    
    //-----------------------------------------------------------------------
    public ActionForward cancel(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        request.setAttribute("model", new BasePageModel(
            "pagesForm.title", request, response, mapping));
        return (mapping.findForward("editPages"));
    }
    
    //-----------------------------------------------------------------------
    private void addModelObjects( 
        HttpServletRequest  request,
        HttpServletResponse response,
        ActionMapping mapping, 
        WebsiteData website)
    throws RollerException 
    {             
        UserManager mgr = RollerFactory.getRoller().getUserManager();        
        RollerSession rses = RollerSession.getRollerSession(request);
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
                
        UserData user = rses.getAuthenticatedUser();
        request.setAttribute("user", user);

        WebsiteData wd = rreq.getWebsite();
        request.setAttribute("website", website);

        List pages = mgr.getPages(website);
        request.setAttribute("pages", pages);
        
        request.setAttribute("page", rreq.getPage());
    }
}

