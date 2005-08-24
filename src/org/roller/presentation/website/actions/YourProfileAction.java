package org.roller.presentation.website.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.UserFormEx;
import org.roller.util.StringUtils;

/**
 * Allows user to edit his/her profile.
 * 
 * @struts.action name="userFormEx" path="/editor/yourProfile" parameter="method"
 * @struts.action-forward name="yourProfile.page" path=".YourProfile"
 */
public class YourProfileAction extends UserBaseAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(YourProfileAction.class);

    /** If method param is not specified, use HTTP verb to pick method to call */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        if (request.getMethod().equals("GET"))
        {
            return edit(mapping, actionForm, request, response);
        }
        return save(mapping, actionForm, request, response);
    }
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception
    {
        return mapping.findForward("yourWebsites");
    }
    
    /** Load form with authenticated user and forward to your-profile page */
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("yourProfile.page");
        try
        {
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            UserData ud = rollerSession.getAuthenticatedUser();
            UserFormEx form = (UserFormEx)actionForm;
            form.copyFrom(ud, request.getLocale());
            form.setPasswordText(null);
            form.setPasswordConfirm(null);
            form.setLocale(ud.getLocale());
            form.setTimeZone(ud.getTimeZone());
            request.setAttribute("model", new BasePageModel(
                "yourProfile.title", request, response, mapping));
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
                         new ActionMessage("yourProfile.cookieLogin"));
            saveMessages(request, messages);
        }
        return forward;
    }

    /** Update user based on posted form data */
    public ActionForward save(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        UserFormEx form = (UserFormEx)actionForm;
        ActionForward forward = mapping.findForward("yourProfile.page");
        ActionMessages msgs = new ActionMessages();
        try
        {
            ActionMessages errors = validate(form, new ActionErrors());
            if (errors.size() == 0)
            {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                UserData data = mgr.getUser( form.getUserName() );
                
                // Need system user to update user
                RollerFactory.getRoller().setUser(UserData.SYSTEM_USER);
                
                // Copy data from form to object (won't copy over password)
                form.copyTo(data, request.getLocale());
                
                // If user set both password and passwordConfirm then reset password
                if (    !StringUtils.isEmpty(form.getPasswordText()) 
                     && !StringUtils.isEmpty(form.getPasswordConfirm()))
                {
                    try
                    {
                        data.resetPassword(RollerFactory.getRoller(), 
                           form.getPasswordText(), 
                           form.getPasswordConfirm());
                    }
                    catch (RollerException e)
                    {
                        msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                            new ActionMessage("yourProfile.passwordResetError"));
                    }
                } 
                RollerSession rses = RollerSession.getRollerSession(request);
                rses.setAuthenticatedUser(data);
                mgr.storeUser( data );
                
                RollerFactory.getRoller().commit();

                request.setAttribute("model", new BasePageModel(
                        "yourProfile.title", request, response, mapping));
                
                //msgs.add(null, new ActionMessage("yourProfile.saved"));
                saveMessages(request, msgs);
            }
            else 
            {
                saveErrors(request, errors);
            }
            return mapping.findForward("yourWebsites");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
    }
    
}






