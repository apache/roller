
package org.roller.presentation.website.actions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.forms.WebsiteForm;
import org.roller.presentation.website.formbeans.WebsiteFormEx;


/////////////////////////////////////////////////////////////////////////////
/**
 * Website Settings action.
 * 
 * @struts.action name="websiteFormEx" path="/editor/website" 
 * 		scope="session" parameter="method"
 * 
 * @struts.action-forward name="editWebsite.page" path="/website/edit-website.jsp"
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
                    .getWeblogCategories(rreq.getWebsite(), true);
                request.setAttribute("bloggerCategories",bcd);

                List pages = rreq.getRoller().getUserManager().getPages(rreq.getWebsite());
                request.setAttribute("pages",pages);

                ServletContext ctx = request.getSession().getServletContext();
                String editorPages = 
                        RollerRuntimeConfig.getProperty("users.editor.pages");
                
                List epages = Arrays.asList(StringUtils.split(StringUtils.deleteWhitespace(editorPages), ","));
                request.setAttribute("editorPagesList", epages);
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
            UserManager umgr = rreq.getRoller().getUserManager();
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
                if (form.getEnableBloggerApi() == null)
                {
                    form.setEnableBloggerApi( Boolean.FALSE );
                }

                /* we don't need this check any longer -- Allen G
                if(!form.getDefaultPageId().equals(form.getWeblogDayPageId()))
                {
                */               
                    WebsiteData wd = umgr.retrieveWebsite(form.getId());
                    wd.save(); // should throw if save not permitted

                    // ensure isEnabled can't be changed
                    form.setIsEnabled(wd.getIsEnabled());
                    form.copyTo(wd, request.getLocale());
                    
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
                    rreq.getRoller().getRefererManager().applyRefererFilters(wd);                    
                    rreq.getRoller().commit();

                    request.getSession().setAttribute(
                        RollerSession.STATUS_MESSAGE,
                        "Successfully submitted new Weblog templates");

                    request.getSession().setAttribute(
                        RollerRequest.WEBSITEID_KEY,form.getId());

                    // clear the page cache for this user
                    PageCacheFilter.removeFromCache( request, wd.getUser() );

                    // set the Editor Page list
                    ServletContext ctx = request.getSession().getServletContext();
                    String editorPages = 
                        RollerRuntimeConfig.getProperty("users.editor.pages");
                
                    List epages = Arrays.asList(StringUtils.split(
                       StringUtils.deleteWhitespace(editorPages), ","));
                    request.setAttribute("editorPagesList", epages);
                /*
                }
                else
                {
                    request.getSession().setAttribute(
                        RollerSession.ERROR_MESSAGE,
                        "CHANGES REJECTED: Cannot set default page template "
                        +"and day template to same template");
                }
                */
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
                    
            actionForm.reset(mapping,request);            
        }
        catch (RollerPermissionsException e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
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

