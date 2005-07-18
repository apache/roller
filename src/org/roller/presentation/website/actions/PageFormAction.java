
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
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.forms.PageForm;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;


/////////////////////////////////////////////////////////////////////////////
/**
 * Page form action.
 * @struts.action name="pageForm" path="/editor/page"
 *  	scope="session" parameter="method"
 * 
 * @struts.action-forward name="removePage.page" path="/website/remove-page.jsp"
 * @struts.action-forward name="editPage.page" path="/website/edit-page.jsp"
 * @struts.action-forward name="editPages.page" path="/website/edit-pages.jsp"
 */
public final class PageFormAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(PageFormAction.class);
        
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
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToEdit() )
            {
                PageForm form = (PageForm)actionForm;
                PageData data = new PageData();
                form.copyTo(data, request.getLocale());
                WebsiteData hd = RollerSession.getRollerSession(request).getCurrentWebsite();

                data.setWebsite( hd );
                data.setUpdateTime( new java.util.Date() );
                data.setDescription("");
                data.setTemplate("");
                validateLink( data );

                UserManager mgr = RollerFactory.getRoller().getUserManager();
                mgr.storePage( data );
                RollerFactory.getRoller().commit();

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pagesForm.addNewPage.success", 
                                data.getName()));
                saveMessages(request, uiMessages);
                
                PageCacheFilter.removeFromCache( request, RollerSession.getRollerSession(request).getCurrentWebsite() );
                    
                actionForm.reset(mapping,request);                
                
                addModelObjects(request);
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
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToEdit() )
            {
                PageData pd = rreq.getPage();
                PageForm pf = (PageForm)actionForm;
                pf.copyFrom(pd, request.getLocale());

                PageCacheFilter.removeFromCache( request, RollerSession.getRollerSession(request).getCurrentWebsite() );
                
                addModelObjects(request);
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
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToEdit() )
            {
                addModelObjects(request);
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
        try
        {
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToEdit() )
            {
                PageForm form = (PageForm)actionForm;
                PageData data = new PageData();
                form.copyTo(data, request.getLocale());

                UserManager mgr = RollerFactory.getRoller().getUserManager();
                mgr.removePageSafely( data.getId() );
                RollerFactory.getRoller().commit();

                PageCacheFilter.removeFromCache( request, RollerSession.getRollerSession(request).getCurrentWebsite() );
                    
                addModelObjects(request);

                actionForm.reset(mapping,request);
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
        try
        {
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rollerSession.isUserAuthorizedToEdit() )
            {
                PageData cd = rreq.getPage();
                PageForm pf = (PageForm)actionForm;
                pf.copyFrom(cd, request.getLocale());

                UserData ud = RollerSession.getRollerSession(request).getAuthenticatedUser();
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
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToEdit() )
            {
                PageForm form = (PageForm)actionForm;
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                PageData data = mgr.retrievePage(form.getId());
                data.save(); // should through exception if no save permission
                form.copyTo(data, request.getLocale());
                data.setUpdateTime( new java.util.Date() );
                data.setWebsite( RollerSession.getRollerSession(request).getCurrentWebsite() );

                validateLink( data );

                mgr.storePage( data );
                RollerFactory.getRoller().commit();

                // set the (possibly) new link back into the Form bean
                ((PageForm)actionForm).setLink( data.getLink() );

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pageForm.save.success", 
                                data.getName()));
                saveMessages(request, uiMessages);

                PageCacheFilter.removeFromCache(request, RollerSession.getRollerSession(request).getCurrentWebsite());
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
    private void validateLink( PageData data )
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
        return (mapping.findForward("editPages"));
    }
    
    //-----------------------------------------------------------------------
    private void addModelObjects( HttpServletRequest request ) 
        throws RollerException {  
                        
        UserManager mgr = RollerFactory.getRoller().getUserManager();

        UserData user = RollerSession.getRollerSession(request).getAuthenticatedUser();
        request.setAttribute("user",user);

        WebsiteData wd = RollerSession.getRollerSession(request).getCurrentWebsite();
        request.setAttribute("website", wd);

        List pages = mgr.getPages(wd);
        request.setAttribute("pages",pages);
    }
}

