
package org.roller.presentation.website.actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.roller.RollerException;
import org.roller.business.search.operations.RebuildWebsiteIndexOperation;
import org.roller.business.search.operations.RemoveWebsiteIndexOperation;
import org.roller.model.IndexManager;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.website.formbeans.UserAdminForm;
import org.roller.util.StringUtils;


/////////////////////////////////////////////////////////////////////////////
/**
 * Actions for administering a user allow user query, edit, and delete of users.
 * 
 * @struts.action name="userAdminForm" path="/admin/user"
 *  	scope="request" parameter="method"
 * 
 * @struts.action-forward name="adminUser.page" path="/website/UserAdmin.jsp"
 */
public final class UserAdminAction extends UserBaseAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(UserAdminAction.class);

    //-----------------------------------------------------------------------
    /** 
     * Show query for user page or, if userName specified in request, show
     * the admin user page for the specified user.
     */
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("adminUser.page");
        ActionMessages msgs = new ActionMessages();
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
            {
                UserAdminForm userForm = (UserAdminForm)actionForm;
                UserManager mgr = rreq.getRoller().getUserManager();
                
                if (userForm != null && userForm.getUserName() != null)
                {
                    UserData user = mgr.getUser( userForm.getUserName(), false );                    
                    if (user != null)
                    {
                        userForm.copyFrom(user, request.getLocale());
                        
                        // User must set new password twice
                        userForm.setPasswordText(null);
                        userForm.setPasswordConfirm(null);
                    }
                    else
                    {
                        msgs.add(ActionMessages.GLOBAL_MESSAGE,
                            new ActionMessage("userAdmin.invalidNewUserName"));
                        userForm.setUserName("");
                    }
                }
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
        
        if (request.getSession().getAttribute("cookieLogin") != null) 
        {
            // TODO: make it possible to change passwords regardless of remember me
            msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("userAdmin.cookieLogin"));
        }
        
         saveMessages(request, msgs);
         return forward;
    }

    //-----------------------------------------------------------------------
    /** 
     * Process POST of edited user data, may cause delete of user. 
     */
    public ActionForward update(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("adminUser.page");
        ActionMessages msgs = new ActionMessages();
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
            {
                UserAdminForm userForm = (UserAdminForm)actionForm;
                UserManager mgr = rreq.getRoller().getUserManager();
                
                // Need system user to update user
                rreq.getRoller().setUser(UserData.SYSTEM_USER);

			   UserData user = mgr.retrieveUser(userForm.getId());
                userForm.copyTo(user, request.getLocale()); // doesn't copy password

                if (userForm.getDelete())
                {
                    // TODO: ask are you sure before deleting user
                    user = deleteUser(mapping, request, rreq, userForm, mgr, user);
                    
                    msgs.add(ActionMessages.GLOBAL_MESSAGE,
                            new ActionMessage("userSettings.deleted"));
                    saveMessages(request, msgs);
                }
                else
                {
                    // If user set both password and passwordConfirm then reset 
                    if (    !StringUtils.isEmpty(userForm.getPasswordText()) 
                         && !StringUtils.isEmpty(userForm.getPasswordConfirm()))
                    {
                        try
                        {
                            user.resetPassword(rreq.getRoller(), 
                               userForm.getPasswordText(), 
                               userForm.getPasswordConfirm());
                        }
                        catch (RollerException e)
                        {
                            msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                              new ActionMessage("userSettings.passwordResetError"));
                        }
                    }
                    
                    // Persist changes to user
                    mgr.storeUser( user );
                    rreq.getRoller().commit(); 
                    
                    msgs.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("userSettings.saved"));
                    saveMessages(request, msgs);
                }
                
                // Operation complete, return to edit action
                userForm.setUserName(null); 
                return edit(mapping, actionForm, request, response);
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
	/** Delete specified user and remove associated entries from the cache. */
    private UserData deleteUser(
            ActionMapping mapping, 
            HttpServletRequest request, 
            RollerRequest rreq, 
            UserAdminForm uaf, 
            UserManager mgr, 
            UserData ud) throws RollerException
    {
        // remove user's Entries from Lucene index
        IndexManager indexManager = rreq.getRoller().getIndexManager();
        WebsiteData website = rreq.getCurrentWebsite();
        indexManager.removeWebsiteIndex(website); 
        
        // delete user from database
        ud.remove();
        rreq.getRoller().commit();
        ud = null;

        request.getSession().setAttribute(
            RollerSession.STATUS_MESSAGE,
                uaf.getUserName() + " has been deleted");

        uaf.reset(mapping, request);
        
        List users = mgr.getUsers(null, null); 
        request.setAttribute("users", users);
        return ud;
    }

    //-----------------------------------------------------------------------
    /**
	 * Rebuild a user's search index.
	 */
	public ActionForward index(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		try
		{
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
			{
				UserAdminForm uaf = (UserAdminForm)actionForm;
				
				// if admin requests an index be re-built, do it
				IndexManager manager = rreq.getRoller().getIndexManager();								 
				manager.rebuildWebsiteIndex();
 				request.getSession().setAttribute(
					RollerSession.STATUS_MESSAGE,
						"Successfully scheduled rebuild of index for");
			}
		}
		catch (Exception e)
		{
			mLogger.error("ERROR in action",e);
			throw new ServletException(e);
		}
		return edit(mapping, actionForm, request, response);
	}

}

