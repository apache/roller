
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
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.website.formbeans.WebsiteFormEx;


/////////////////////////////////////////////////////////////////////////////
/**
 * Website Settings action.
 * 
 * @struts.action name="websiteFormEx" path="/editor/website" 
 * 		scope="session" parameter="method"
 * 
 * @struts.action-forward name="editWebsite.page" path=".edit-website"
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
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAdmin())
            {
                Roller roller = RollerFactory.getRoller();
                UserManager umgr = roller.getUserManager();
                WeblogManager wmgr = roller.getWeblogManager();
                UserData ud = rses.getAuthenticatedUser();
                request.setAttribute("user",ud);

                WebsiteData website = 
                    umgr.retrieveWebsite(rses.getCurrentWebsite().getId());
                WebsiteFormEx wf = (WebsiteFormEx)actionForm;
                wf.copyFrom(website, request.getLocale());

                List cd = wmgr.getWeblogCategories(website, true);
                request.setAttribute("categories",cd);

                List bcd = wmgr.getWeblogCategories(website, true);
                request.setAttribute("bloggerCategories",bcd);

                List pages = umgr.getPages(website);
                request.setAttribute("pages",pages);

                ServletContext ctx = request.getSession().getServletContext();
                String editorPages = 
                        RollerRuntimeConfig.getProperty("users.editor.pages");
                
                List epages = Arrays.asList(StringUtils.split(
                        StringUtils.deleteWhitespace(editorPages), ","));
                request.setAttribute("editorPagesList", epages);
                
                    
                BasePageModel pageModel = new BasePageModel(request, response, mapping);
                request.setAttribute("model",pageModel);   
                    
                rses.setCurrentWebsite(website);
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
        ActionErrors errors = new ActionErrors();
        ActionMessages messages = new ActionMessages();        
        ActionForward forward = mapping.findForward("editWebsite");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToAdmin() )
            {
                WebsiteFormEx form = (WebsiteFormEx)actionForm;

                /* we don't need this check any longer -- Allen G
                if(!form.getDefaultPageId().equals(form.getWeblogDayPageId()))
                {
                */               
                    WebsiteData wd = umgr.retrieveWebsite(form.getId());
                    wd.save(); // should throw if save not permitted

                    // ensure getEnabled can't be changed
                    form.setEnabled(wd.getEnabled());
                    form.copyTo(wd, request.getLocale());
                                        
                    wd.save();
                    RollerFactory.getRoller().getRefererManager().applyRefererFilters(wd);                    
                    RollerFactory.getRoller().commit();

                   messages.add(null, 
                     new ActionMessage("websiteSettings.savedChanges"));

                    request.getSession().setAttribute(
                        RollerRequest.WEBSITEID_KEY, form.getId());

                    // clear the page cache for this user
                    PageCacheFilter.removeFromCache(request, wd);

                    // set the Editor Page list
                    ServletContext ctx = request.getSession().getServletContext();
                    String editorPages = 
                        RollerRuntimeConfig.getProperty("users.editor.pages");
                
                    List epages = Arrays.asList(StringUtils.split(
                        org.apache.commons.lang.StringUtils.deleteWhitespace(editorPages), ","));
                    request.setAttribute("editorPagesList", epages);   
                    
                    BasePageModel pageModel = new BasePageModel(request, response, mapping);
                    request.setAttribute("model",pageModel);                
                /*
                }
                else
                {
                    errors.add(null, 
                        new ActionError("websiteSettings.error.sameTemplate"));                   
                    saveErrors(request, errors);
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
            errors.add(null, new ActionError("error.permissions.deniedSave"));
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
        if (errors.size() > 0) saveErrors(request, errors);
        if (messages.size() > 0) saveMessages(request, messages);  
        return forward;
    }

}

