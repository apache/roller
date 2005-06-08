
package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.WeblogManager;
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.forms.WebsiteForm;
import org.roller.presentation.pagecache.PageCache;
import org.roller.presentation.website.formbeans.WebsiteFormEx;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * Website Settings action.
 * @struts.action name="websiteFormEx" path="/website" scope="session" parameter="method"
 */
public final class WebsiteFormAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(WebsiteFormAction.class);

    public ActionForward add(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        return update( mapping, actionForm, request, response );
    }

    //-----------------------------------------------------------------------
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editWebsite.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                UserData ud = rreq.getUser();
                request.setAttribute("user",ud);

                WebsiteData hd = rreq.getWebsite();
                WebsiteForm wf = (WebsiteFormEx)actionForm;
                wf.copyFrom(hd, request.getLocale());

                List cd = rreq.getRoller().getWeblogManager()
                   .getWeblogCategories(rreq.getWebsite(), true);
                request.setAttribute("categories",cd);

                List bcd = rreq.getRoller().getWeblogManager()
                    .getWeblogCategories(rreq.getWebsite(), false);
                request.setAttribute("bloggerCategories",bcd);

                List pages = rreq.getRoller().getUserManager().getPages(rreq.getWebsite());
                request.setAttribute("pages",pages);

                ServletContext ctx = request.getSession().getServletContext();
                RollerConfig rollerConfig =
                    RollerContext.getRollerContext( ctx ).getRollerConfig();
                request.setAttribute("editorPagesList", rollerConfig.getEditorPagesList());
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
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
        ActionForward forward = mapping.findForward("editWebsite");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogManager wmgr = rreq.getRoller().getWeblogManager();
            if ( rreq.isUserAuthorizedToEdit() )
            {
                WebsiteFormEx form = (WebsiteFormEx)actionForm;
                
                // checkboxes return no value when not checked
                if (form.getAllowComments() == null)
                {
                    form.setAllowComments( Boolean.FALSE );
                }
                if (form.getEmailComments() == null)
                {
                    form.setEmailComments( Boolean.FALSE );
                }

                if(!form.getDefaultPageId().equals(form.getWeblogDayPageId()))
                {
                    // Make sure isEnabled cannot be changed
                    WebsiteData website = rreq.getWebsite();
                    form.setIsEnabled( website.getIsEnabled() );
                    
                    UserData ud = rreq.getUser();
                    
                    WebsiteData wd = new WebsiteData();
                    form.copyTo(wd, request.getLocale());
                    wd.setUser(ud);
                    
                    if (form.getDefaultCategoryId() != null) 
                    {
                        WeblogCategoryData defaultCat = 
                            wmgr.retrieveWeblogCategory(form.getDefaultCategoryId());
                        wd.setDefaultCategory(defaultCat);
                    }
                    
                    if (form.getBloggerCategoryId() != null) 
                    {
                        WeblogCategoryData bloggerCat = 
                            wmgr.retrieveWeblogCategory(form.getBloggerCategoryId());
                        wd.setBloggerCategory(bloggerCat);
                    }
                    
                    wd.save();
                    rreq.getRoller().commit();

                    request.getSession().setAttribute(
                        RollerSession.STATUS_MESSAGE,
                        "Successfully submitted new Weblog templates");

                    request.getSession().setAttribute(
                        RollerRequest.WEBSITEID_KEY,form.getId());

                    // clear the page cache for this user
                    PageCache.removeFromCache( request, ud );

                    // set the Editor Page list
                    ServletContext ctx = request.getSession().getServletContext();
                    RollerConfig rollerConfig =
                        RollerContext.getRollerContext( ctx ).getRollerConfig();
                    request.setAttribute("editorPagesList", rollerConfig.getEditorPagesList());
                }
                else
                {
                    request.getSession().setAttribute(
                        RollerSession.ERROR_MESSAGE,
                        "CHANGES REJECTED: Cannot set default page template "
                        +"and day template to same template");
                }
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
                    
            actionForm.reset(mapping,request);            
        }
        catch (RollerException re)
        {
            mLogger.error("Unexpected exception",re.getRootCause());
            throw new ServletException(re);
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception",e);
            throw new ServletException(e);
        }
        return forward;
    }

}

