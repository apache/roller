package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.website.formbeans.UserFormEx;
import org.roller.util.StringUtils;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * Actions for editing your user information.
 * 
 * @struts.action name="userFormEx" path="/editor/user" 
 * 		scope="session" parameter="method"
 * 
 * @struts.action-forward name="editUser.page" path="/website/UserEdit.jsp"
 */
public class UserEditAction extends UserBaseAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserEditAction.class);

    //-----------------------------------------------------------------------
    /** Handle GET for user edit page */
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editUser.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                UserData ud = rreq.getAuthenticatedUser();
                request.setAttribute("user",ud);

                UserFormEx form = (UserFormEx)actionForm;
                form.copyFrom(ud, request.getLocale());
                
                // User must set new password twice
                form.setPasswordText(null);
                form.setPasswordConfirm(null);

                loadRequestObjects(request, rreq, ud, form);
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
        
        // if user logged in with a cookie, display a warning that they
        // can't change passwords
        if (mLogger.isDebugEnabled()) 
        {
            log.debug("checking for cookieLogin...");
        }

        if (request.getSession().getAttribute("cookieLogin") != null) {
            ActionMessages messages = new ActionMessages();

            // add warning messages
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                         new ActionMessage("userSettings.cookieLogin"));
            saveMessages(request, messages);
        }
        
        return forward;
    }

    //-----------------------------------------------------------------------
    /** Handle POST from user edit form */
    public ActionForward update(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        UserFormEx form = (UserFormEx)actionForm;
        ActionForward forward = mapping.findForward("editUser.page");
        ActionMessages msgs = new ActionMessages();
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (rreq.isUserAuthorizedToEdit())
            {
                ActionMessages errors = validate(form, new ActionErrors());
                if (errors.size() == 0)
                {
                    UserManager mgr = rreq.getRoller().getUserManager();
                    UserData data = mgr.getUser( form.getUserName() );
                    
                    // Need system user to update new user
                    rreq.getRoller().setUser(UserData.SYSTEM_USER);
                    
                    // Copy data from form to persistent object (won't copy over password)
                    form.copyTo(data, request.getLocale());
                    
                    // If user set both password and passwordConfirm then reset password
                    if (    !StringUtils.isEmpty(form.getPasswordText()) 
                         && !StringUtils.isEmpty(form.getPasswordConfirm()))
                    {
                        try
                        {
                            data.resetPassword(rreq.getRoller(), 
                               form.getPasswordText(), 
                               form.getPasswordConfirm());
                        }
                        catch (RollerException e)
                        {
                            msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                                new ActionMessage("userSettings.passwordResetError"));
                        }
                    } 
                
                    // ROLLER_2.0: user needs locale and timezone 
                    
//                    WebsiteData website = mgr.getWebsite(data.getUserName());
//                    website.setEditorTheme(form.getTheme());
//                    website.setLocale(form.getLocale());
//                    website.setTimezone(form.getTimezone());
    
                    // Persist changes
                    mgr.storeUser( data );
                    //mgr.storeWebsite( website );
                    rreq.getRoller().commit();
                    
                    // Changing user no longer requires cache flush
                    //PageCacheFilter.removeFromCache(request, data);
    
                    msgs.add(null, new ActionMessage("userSettings.saved"));
                    saveMessages(request, msgs);
                }
                else 
                {
                    saveErrors(request, errors);
                }
                return edit(mapping, actionForm, request, response);
            }
            return mapping.findForward("access-denied");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
    }
    
}






