
package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.forms.PageForm;
import org.roller.presentation.pagecache.PageCache;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * Page form action.
 * @struts.action name="pageForm" path="/page"
 *  scope="session" parameter="method"
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
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                PageForm form = (PageForm)actionForm;
                PageData data = new PageData();
                form.copyTo(data, request.getLocale());
                WebsiteData hd = rreq.getWebsite();

                data.setWebsite( hd );
                data.setUpdateTime( new java.util.Date() );
                data.setDescription("");
                data.setTemplate("");
                validateLink( data );

                UserManager mgr = rreq.getRoller().getUserManager();
                mgr.storePage( data );
                rreq.getRoller().commit();

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pagesForm.addNewPage.success", 
                                data.getName()));
                saveMessages(request, uiMessages);
                
                UserData user = rreq.getUser();
                PageCache.removeFromCache( request, user );
                    
                actionForm.reset(mapping,request);                
                
                addModelObjects(rreq);
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
            if ( rreq.isUserAuthorizedToEdit() )
            {
                UserData ud = rreq.getUser();
                PageData pd = rreq.getPage();
                PageForm pf = (PageForm)actionForm;
                pf.copyFrom(pd, request.getLocale());

                PageCache.removeFromCache( request,ud );
                
                addModelObjects(rreq);
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
            if ( rreq.isUserAuthorizedToEdit() )
            {
                addModelObjects(rreq);
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
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                PageForm form = (PageForm)actionForm;
                PageData data = new PageData();
                form.copyTo(data, request.getLocale());

                UserManager mgr = rreq.getRoller().getUserManager();
                mgr.removePage( data.getId() );
                rreq.getRoller().commit();

                UserData user = rreq.getUser();
                PageCache.removeFromCache( request,user );
                    
                addModelObjects(rreq);

                actionForm.reset(mapping,request);
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
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                PageData cd = rreq.getPage();
                PageForm pf = (PageForm)actionForm;
                pf.copyFrom(cd, request.getLocale());

                UserData ud = rreq.getUser();
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
            if ( rreq.isUserAuthorizedToEdit() )
            {
                PageForm form = (PageForm)actionForm;
                PageData data = new PageData();
                form.copyTo(data, request.getLocale());
                data.setUpdateTime( new java.util.Date() );
                data.setWebsite( rreq.getWebsite() );

                validateLink( data );

                UserManager mgr = rreq.getRoller().getUserManager();
                mgr.storePage( data );
                rreq.getRoller().commit();

                // set the (possibly) new link back into the Form bean
                ((PageForm)actionForm).setLink( data.getLink() );

                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(ActionMessages.GLOBAL_MESSAGE, 
                        new ActionMessage("pageForm.save.success", 
                                data.getName()));
                saveMessages(request, uiMessages);

                UserData user = rreq.getUser();
                PageCache.removeFromCache( request,user );
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
              
            // Don't reset this form. Allow user to keep on tweaking.        
            //actionForm.reset(mapping,request);
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
    private void addModelObjects( RollerRequest rreq ) 
        throws RollerException {  
            
        HttpServletRequest request = rreq.getRequest();
            
        UserManager mgr = rreq.getRoller().getUserManager();

        UserData user = rreq.getUser();
        request.setAttribute("user",user);
                            
        List pages = mgr.getPages(rreq.getWebsite());
        request.setAttribute("pages",pages);                                   
    }
}

