
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
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCache;
import org.roller.presentation.weblog.search.IndexManager;
import org.roller.presentation.weblog.search.operations.RebuildUserIndexOperation;
import org.roller.presentation.weblog.search.operations.RemoveUserIndexOperation;
import org.roller.presentation.website.formbeans.UserAdminForm;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * New user form action.
 * @struts.action name="userAdminForm" path="/adminUser"
 *  scope="request" parameter="method"
 */
public final class UserAdminAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(UserAdminAction.class);

    //-----------------------------------------------------------------------
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("adminUser.page");
        ActionMessages warnings = new ActionMessages();
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
            {
                UserManager mgr = rreq.getRoller().getUserManager();

                List users = mgr.getUsers(false);
                request.setAttribute("users", users);

                UserAdminForm uaf = (UserAdminForm)actionForm;
                if (uaf != null && uaf.getUserName() != null)
                {
                    UserData ud = mgr.getUser( uaf.getUserName(), false );
                    
                    if ( ud != null)
                    {
                        uaf.copyFrom(ud, request.getLocale());
                        
                        WebsiteData website = mgr.getWebsite(uaf.getUserName(), false);
                        uaf.setUserEnabled( website.getIsEnabled() );
                        PageCache.removeFromCache( request,ud );
                    }
                    else
                    {
                        warnings.add(ActionMessages.GLOBAL_MESSAGE,
                            new ActionMessage("Unable to find user: " + uaf.getUserName()));

                        uaf.setUserName("");
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
        
        // if user logged in with a cookie, display a warning that they
        // can't change passwords
        if (mLogger.isDebugEnabled()) 
        {
            log.debug("checking for cookieLogin...");
        }

        if (request.getSession().getAttribute("cookieLogin") != null) {
            // add warning messages
            warnings.add(ActionMessages.GLOBAL_MESSAGE,
                         new ActionMessage("userAdmin.cookieLogin"));
        }
        
        if (!warnings.isEmpty()) 
        {
            saveMessages(request, warnings);
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
        ActionForward forward = mapping.findForward("adminUser.page");
        
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
            {
                UserAdminForm uaf = (UserAdminForm)actionForm;
                UserManager mgr = rreq.getRoller().getUserManager();

                List users = mgr.getUsers(false);
                request.setAttribute("users", users);

				UserData ud = new UserData();
                uaf.copyTo(ud, request.getLocale());

                if ( uaf.getDelete() )
                {
                    // need to 'freshen' UserData
                    ud = mgr.retrieveUser(uaf.getId());
                    ud = deleteUser(mapping, request, rreq, uaf, mgr, ud);
                }
                else
                {
                    protectAgainstNulls(uaf, ud);
                    
                    refreshIndexCache(request, rreq, uaf);

                    changePassword(request, uaf, mgr, ud);

                    mgr.storeUser( ud );
                    rreq.getRoller().commit();   
                    uaf.copyFrom(ud, request.getLocale());                  
                }

                if (ud != null)
                {
                    PageCache.removeFromCache( request, ud );
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
        return forward;
    }

    /**
     * Check to see if the password has been changed.  If so, encrypt
     * as necessary and save to database.
     * 
     * @param request
     * @param uaf
     * @param mgr
     * @param ud
     * @throws RollerException
     */
	private void changePassword(HttpServletRequest request, UserAdminForm uaf, 
                                UserManager mgr, UserData ud) throws RollerException
    {
        UserData existing = mgr.getUser(uaf.getUserName(), false);
        boolean samePassword = uaf.getPassword().equals(existing.getPassword());
        
        if (!samePassword) 
        {
            RollerContext rollerContext = RollerContext.getRollerContext(request);
            RollerConfig rollerConfig = rollerContext.getRollerConfig();
            if (rollerConfig.getEncryptPasswords().booleanValue())
            {
                // encrypt the user's password
                ud.setPassword(Utilities.encodePassword(uaf.getPassword(), 
                               rollerConfig.getAlgorithm()));  
                uaf.setPassword(ud.getPassword());
            }
            else
            {
                // set the unencrypted version
                ud.setPassword(uaf.getPassword());
            }
        }
    }

    /**
     * Check to see if the value of "userEnabled" has changed.
     * If so, update User's Website and save, and refresh the Index page cache.
     * 
     * @param request
     * @param rreq
     * @param uaf
     * @throws RollerException
     */
    private void refreshIndexCache(HttpServletRequest request, RollerRequest rreq, UserAdminForm uaf) throws RollerException
    {
        WebsiteData website = rreq.getRoller().getUserManager()
                                              .getWebsite(uaf.getUserName(), false);
        boolean refreshIndexCache = false;
        if ( request.getParameter("userEnabled") == null )
        {
        	// only change it if it is a change
        	if (uaf.getUserEnabled() == null || 
        		website.getIsEnabled().booleanValue())
        	{
            	uaf.setUserEnabled( Boolean.FALSE );
            	
        		refreshIndexCache = true;
        	}
        }
        else
        {
        	// only change it if it is a change
        	if (uaf.getUserEnabled() == null || 
                website.getIsEnabled().booleanValue() == false)
        	{
        		uaf.setUserEnabled( Boolean.TRUE );

        		refreshIndexCache = true;
        	}
        }
        
        if (refreshIndexCache)
        {
            // set Website.isEnabled to match uaf.getUserEnabled()
            website.setIsEnabled(uaf.getUserEnabled());
            website.save();
            
        	// refresh the front page cache
            MainPageAction.flushMainPageCache();
        }
    }

    private void protectAgainstNulls(UserAdminForm uaf, UserData ud)
    {
        // if an empty/null value is passed in for
        // password, don't change the value.
        if ( StringUtils.isEmpty( uaf.getPassword() ) )
        {
            uaf.setPassword( ud.getPassword() );
        } 

        // if an empty/null value is passed in for
        // email, don't change the value.
        if ( StringUtils.isEmpty( uaf.getEmailAddress() ) )
        {
            uaf.setEmailAddress( ud.getEmailAddress() );
        }
    }

    private UserData deleteUser(ActionMapping mapping, HttpServletRequest request, 
                                RollerRequest rreq, UserAdminForm uaf, UserManager mgr, 
                                UserData ud) throws RollerException
    {
        // remove user's Entries from Lucene index
        RollerContext.getRollerContext(
            RollerContext.getServletContext()).getIndexManager()
            .scheduleIndexOperation(new RemoveUserIndexOperation(ud));
        
        // delete user from database
        ud.remove();
        rreq.getRoller().commit();
        PageCache.removeFromCache( request, ud );
        ud = null;

        request.getSession().setAttribute(
            RollerSession.STATUS_MESSAGE,
                uaf.getUserName() + " has been deleted");

        uaf.reset(mapping, request);
        
        List users = mgr.getUsers(false);
        request.setAttribute("users", users);
        return ud;
    }

    /**
	 * Rebuild a user's search index
	 * @param mapping
	 * @param actionForm
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ServletException
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
				IndexManager manager =
					RollerContext.getRollerContext(RollerContext.getServletContext())
								 .getIndexManager();
								 
				manager.scheduleIndexOperation(new RebuildUserIndexOperation(null));
				request.getSession().setAttribute(
					RollerSession.STATUS_MESSAGE,
						"Successfully scheduled rebuild of index for '" 
						+ uaf.getUserName() + "'");
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

