
package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.website.formbeans.UserFormEx;
import org.roller.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.roller.config.RollerRuntimeConfig;

/////////////////////////////////////////////////////////////////////////////
/**
 * Actions for creating a new user.
 * 
 * @struts.action name="userFormEx" path="/user" 
 * 		scope="session" parameter="method"
 * 
 * @struts.action-forward name="registerUser.page" path="/website/UserNew.jsp"
 * @struts.action-forward name="welcome.page" path="/website/welcome.jsp"
 */
public class UserNewAction extends UserBaseAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserNewAction.class);

    //------------------------------------------------------------------------
    /** Process GET of new user page (allows admin to create a user) */
    public ActionForward createUser(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        UserFormEx userForm = (UserFormEx)actionForm;
        userForm.setAdminCreated(true);
        return registerUser(mapping, actionForm, request, response);
    }
    
    //------------------------------------------------------------------------
    /** Process GET of user registration page (allows users to register themselves. */
    public ActionForward registerUser(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("registerUser.page");
        ActionErrors errors = new ActionErrors();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        try
        {
            UserFormEx userForm = (UserFormEx)actionForm;
            loadRequestObjects(request, rreq, null, userForm);
            userForm.setLocale(request.getLocale().toString());
                
            // User must set new password twice
            userForm.setPasswordText(null);
            userForm.setPasswordConfirm(null);           
        }
        catch (Exception e)
        {
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.editing.user", e.toString()));
            mLogger.error("ERROR in newUser", e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /** Process POST of new user information. */
    public ActionForward add(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        UserFormEx form = (UserFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        ServletContext ctx = rreq.getServletContext();
        RollerContext rollerContext = RollerContext.getRollerContext(ctx);

        boolean reg_allowed = 
                RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");

        if ( !reg_allowed && !request.isUserInRole("admin"))
        {
            throw new ServletException("New users disabled!");
        }

        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = validate(form, new ActionErrors());
        if (!errors.isEmpty())
        {
            saveErrors(request, errors);
        }
        else try
        {
            // Add new user
            UserManager mgr = rreq.getRoller().getUserManager(); 
            
            // Need system user to add new user
            rreq.getRoller().setUser(UserData.SYSTEM_USER);

            UserData ud = new UserData();
            form.copyTo(ud, request.getLocale()); // doesn't copy password
            ud.setId(null);
            ud.setDateCreated(new java.util.Date());

            // If user set both password and passwordConfirm then reset password
            if (    !StringUtils.isEmpty(form.getPasswordText()) 
                 && !StringUtils.isEmpty(form.getPasswordConfirm()))
            {
               ud.resetPassword(rreq.getRoller(), 
                  form.getPasswordText(), form.getPasswordConfirm());
            }
            
            String theme = form.getTheme();
            HashMap pages = rollerContext.readThemeMacros(theme);
            mgr.addUser( ud, pages, theme, form.getLocale(), form.getTimezone() );
            rreq.getRoller().commit();

			// Flush cache so user will immediately appear on index page
            PageCacheFilter.removeFromCache( request, ud );
            MainPageAction.flushMainPageCache();

            if (form.getAdminCreated()) 
            {
                // User created for admin, so return to new user page with empty form
                msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("newUser.created"));
                saveMessages(request, msgs);
                form.reset(mapping, request);
                return createUser(mapping, actionForm, request, response);
            }
            else 
            {
                // User registered, so go to welcome page
                String weblogURL = rollerContext.getAbsoluteContextUrl(request)
                                 + "/page/"+ud.getUserName();
                request.setAttribute("weblogURL",weblogURL);   
                String rssURL = rollerContext.getAbsoluteContextUrl(request)
                              + "/rss/"+ud.getUserName();
                request.setAttribute("rssURL",rssURL);  
                request.setAttribute("contextURL", 
                                 rollerContext.getAbsoluteContextUrl(request));
                return mapping.findForward("welcome.page");
            }
        }
        catch (RollerException e)
        {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(e.getMessage()));
            saveErrors(request,errors);          
            mLogger.error("ERROR in addUser", e);
            form.setUserName("");
        }

        if (form.getAdminCreated()) 
        {
            return mapping.findForward("createUser");
        }
        else 
        {
            // Error occured, send user back to new user form
            return mapping.findForward("registerUser");
        }
    }

    //------------------------------------------------------------------------
    /** Validate user form. TODO: replace with Struts validation. */
    protected ActionMessages validate( UserFormEx form, ActionMessages errors ) {
        super.validate(form, errors);
        if (    StringUtils.isEmpty(form.getPasswordText())
             && StringUtils.isEmpty(form.getPasswordConfirm()))
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("error.add.user.missingPassword"));
        }
        return errors;
    }
}