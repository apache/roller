
package org.roller.presentation.website.actions;

import java.io.IOException;
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
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.forms.WeblogTemplateForm;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;


/////////////////////////////////////////////////////////////////////////////
/**
 * Page form action.
 * @struts.action name="weblogTemplateForm" path="/editor/page"
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
                data.setWebsite( website );
                data.setLastModified( new java.util.Date() );
                data.setDescription("");
                data.setContents("");
                validateLink( data );

                UserManager mgr = RollerFactory.getRoller().getUserManager();
                mgr.storePage( data );
                RollerFactory.getRoller().commit();

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pagesForm.addNewPage.success", 
                                data.getName()));
                saveMessages(request, uiMessages);
                
                UserData user = rses.getAuthenticatedUser();
                PageCacheFilter.removeFromCache( request, website );
                    
                actionForm.reset(mapping,request);                
                
                addModelObjects(request, response, mapping);
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
            request.setAttribute("model", new BasePageModel(
                "pageForm.title", request, response, mapping));   
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                UserData ud = rses.getAuthenticatedUser();
                WeblogTemplate pd = (WeblogTemplate) rreq.getPage();
                WeblogTemplateForm pf = (WeblogTemplateForm)actionForm;
                pf.copyFrom(pd, request.getLocale());

                PageCacheFilter.removeFromCache( request, pd.getWebsite() );
                
                addModelObjects(request, response, mapping);
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
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            request.setAttribute("model", new BasePageModel(
                "pagesForm.title", request, response, mapping));            
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                addModelObjects(request, response, mapping);
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
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                
                // fetch template to be deleted (we'll need its website)
                WeblogTemplate template = mgr.retrievePage(form.getId());
                
                mgr.removePageSafely(template.getId());
                RollerFactory.getRoller().commit();

                UserData user = rses.getAuthenticatedUser();
                PageCacheFilter.removeFromCache(request, template.getWebsite());
                    
                addModelObjects(request, response, mapping);
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
    public ActionForward removeOk(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("removePage.page");
        request.setAttribute("model", new BasePageModel(
            "editPages.title.removeOK", request, response, mapping));
        try
        {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                WeblogTemplate cd = (WeblogTemplate) rreq.getPage();
                WeblogTemplateForm pf = (WeblogTemplateForm)actionForm;
                pf.copyFrom(cd, request.getLocale());

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
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            request.setAttribute("model", new BasePageModel(
                "pageForm.title", request, response, mapping));
            WebsiteData website = rreq.getWebsite();
            if ( rses.isUserAuthorizedToAdmin(website) )
            {
                WeblogTemplateForm form = (WeblogTemplateForm)actionForm;
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                WeblogTemplate data = mgr.retrievePage(form.getId());
                data.save(); // should through exception if no save permission
                form.copyTo(data, request.getLocale());
                data.setLastModified( new java.util.Date() );
                data.setWebsite(mgr.retrieveWebsite(form.getWebsite().getId()));

                validateLink( data );

                mgr.storePage( data );
                RollerFactory.getRoller().commit();

                // set the (possibly) new link back into the Form bean
                ((WeblogTemplateForm)actionForm).setLink( data.getLink() );

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pageForm.save.success", 
                                data.getName()));
                saveMessages(request, uiMessages);

                UserData user = rses.getAuthenticatedUser();
                PageCacheFilter.removeFromCache(request, data.getWebsite());
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
        ActionMapping mapping)
    throws RollerException 
    {             
        UserManager mgr = RollerFactory.getRoller().getUserManager();        
        RollerSession rses = RollerSession.getRollerSession(request);
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
                
        UserData user = rses.getAuthenticatedUser();
        request.setAttribute("user",user);

        WebsiteData wd = rreq.getWebsite();
        request.setAttribute("website", wd);

        List pages = mgr.getPages(wd);
        request.setAttribute("pages",pages);
    }
}

