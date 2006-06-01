/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/

package org.apache.roller.ui.admin.struts.actions;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.authoring.struts.actions.UserBaseAction;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.roller.RollerException;
import org.apache.roller.model.IndexManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.admin.struts.formbeans.UserAdminForm;
import org.apache.roller.util.StringUtils;


/////////////////////////////////////////////////////////////////////////////
/**
 * Actions for administering a user allow user query, edit, and delete of users.
 * 
 * @struts.action name="userAdminForm" path="/admin/user"
 *  	scope="request" parameter="method"
 * 
 * @struts.action-forward name="adminUser.page" path=".UserAdmin"
 */
public final class UserAdminAction extends UserBaseAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(UserAdminAction.class);

    //-----------------------------------------------------------------------
    /** 
     * Show query for user page or, if userName specified in request, 
     * show the admin user page for the specified user.
     */
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("adminUser.page");
        try
        {
            UserData user = null;
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if (rollerSession.isGlobalAdminUser() )
            {
                UserAdminForm userForm = (UserAdminForm)actionForm;
                UserManager mgr = RollerFactory.getRoller().getUserManager(); 
                if (userForm!=null 
                      && userForm.getUserName()!=null && !userForm.isNewUser())
                {
                    ActionMessages msgs = getErrors(request);
                    msgs = (msgs == null) ? new ActionMessages() : msgs;
                    user = mgr.getUserByUserName(userForm.getUserName(), null);                    
                    if (user != null)
                    {
                        userForm.copyFrom(user, request.getLocale());                        
                        // User must set new password twice
                        userForm.setPasswordText(null);
                        userForm.setPasswordConfirm(null);
                    }
                    else
                    {
                        msgs.add(ActionErrors.GLOBAL_ERROR,
                            new ActionMessage("userAdmin.invalidNewUserName"));
                        userForm.setUserName("");
                    }
                    if (request.getSession().getAttribute("cookieLogin")!=null) 
                    {
                        // TODO: make it possible to change passwords 
                        // regardless of remember me
                        msgs.add(ActionErrors.GLOBAL_ERROR, 
                                new ActionMessage("userAdmin.cookieLogin"));
                    }
                    saveErrors(request, msgs);
                }
                request.setAttribute("model", new UserAdminPageModel(
                    request, response, mapping, userForm, user));
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
            RollerSession rollerSession = 
                RollerSession.getRollerSession(request);
            if (rollerSession.isGlobalAdminUser() )
            {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                UserAdminForm userForm = (UserAdminForm)actionForm;
                
                if (userForm.isNewUser()) {
                    UserData user = new UserData();
                    userForm.copyTo(user, request.getLocale()); 
                    user.setId(null);
                    user.setDateCreated(new java.util.Date());
                    user.setEnabled(Boolean.TRUE);
                    
                    // Check username and email addresses
                    msgs = validate(userForm, msgs);

                    // Must have matching passwords and confirm passwords
                    if (    !StringUtils.isEmpty(userForm.getPasswordText()) 
                         && !StringUtils.isEmpty(userForm.getPasswordConfirm()))
                    {
                        try {
                            user.resetPassword(RollerFactory.getRoller(), 
                               userForm.getPasswordText(), 
                               userForm.getPasswordConfirm());
                        } catch (RollerException e) {
                            msgs.add(ActionErrors.GLOBAL_ERROR, 
                            new ActionError("userSettings.passwordResetError"));
                        }
                    } else {
                        msgs.add(ActionErrors.GLOBAL_ERROR, 
                            new ActionError("userSettings.needPasswordTwice"));
                    }
                    
                    // If no error messages, then add user
                    if (msgs.isEmpty()) {
                        try {
                            // Save new user to database
                            mgr.addUser(user);                            
                            RollerFactory.getRoller().flush();
                            
                            msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                                new ActionMessage("userSettings.saved"));
                            saveMessages(request, msgs);
                            
                            // Operation complete, return to edit action
                            userForm.setUserName(null); 
                            userForm.setNewUser((false));
                            
                        } catch (RollerException e) {
                            // Add and commit failed, so show nice error message
                            msgs.add(ActionErrors.GLOBAL_ERROR, 
                                new ActionError(e.getMessage())); 
                            saveErrors(request, msgs);
                        }
                    } else {
                        saveErrors(request, msgs);
                    } 
                    return edit(mapping, actionForm, request, response);  
                    
                } else {
                    
                    UserData user = mgr.getUser(userForm.getId());
                    userForm.copyTo(user, request.getLocale()); 
                
                    // Check username and email addresses
                    msgs = validate(userForm, msgs);
                    
                    // If user set both password and passwordConfirm then reset 
                    if (    !StringUtils.isEmpty(userForm.getPasswordText()) 
                         && !StringUtils.isEmpty(userForm.getPasswordConfirm()))
                    {
                        try {
                            user.resetPassword(RollerFactory.getRoller(), 
                               userForm.getPasswordText(), 
                               userForm.getPasswordConfirm());
                        } catch (RollerException e) {
                            msgs.add(ActionErrors.GLOBAL_ERROR, 
                                new ActionMessage(
                                    "userSettings.passwordResetError"));
                        }
                    } else if (!StringUtils.isEmpty(userForm.getPasswordText())
                            || !StringUtils.isEmpty(userForm.getPasswordConfirm())) {
                        // But it's an error to specify only one of the two
                        msgs.add(ActionErrors.GLOBAL_ERROR, 
                            new ActionMessage(
                                "userSettings.needPasswordTwice"));
                    }
                    
                    if (msgs.isEmpty()) {
                        try {
                           // Persist changes to user
                            mgr.saveUser(user);
                            RollerFactory.getRoller().flush();
                            
                            msgs.add(ActionMessages.GLOBAL_MESSAGE,
                                new ActionMessage("userSettings.saved"));
                            saveMessages(request, msgs);

                            // Operation complete, return to edit action
                            userForm.setUserName(null);  
                            
                        } catch (RollerException e) {
                            msgs.add(ActionErrors.GLOBAL_ERROR, 
                                new ActionMessage(e.getMessage()));
                            saveErrors(request, msgs);
                        }
                    } else {
                        saveErrors(request, msgs);
                    } 
                }
                
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
    /**
	 * Cancel from edit user. 
	 */
	public ActionForward cancel(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
         UserAdminForm userForm = (UserAdminForm)actionForm;
         userForm.setUserName(null);         
         userForm.setNewUser(false);
         return edit(mapping, actionForm, request, response);
    }
    
    //-----------------------------------------------------------------------
    /**
	 * Create new user. 
	 */
	public ActionForward newUser(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
         UserAdminForm userForm = (UserAdminForm)actionForm;
         userForm.setNewUser(true);
         userForm.setEnabled(Boolean.TRUE);
         return edit(mapping, actionForm, request, response);
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
             RollerSession rollerSession = RollerSession.getRollerSession(request);
			if (rollerSession.isGlobalAdminUser() )
			{
				UserAdminForm uaf = (UserAdminForm)actionForm;
				
				// if admin requests an index be re-built, do it
				IndexManager manager = RollerFactory.getRoller().getIndexManager();								 
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

    public class UserAdminPageModel extends BasePageModel 
    {
        private UserAdminForm userAdminForm = null;
        private List permissions = new ArrayList();
        
        public UserAdminPageModel(
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping,
            UserAdminForm form,
            UserData user) throws RollerException
        {
            super("dummy", request, response, mapping);
            userAdminForm = form;
            
            if (user != null)
            {
                Roller roller = RollerFactory.getRoller();
                permissions = roller.getUserManager().getAllPermissions(user);
            }
        }
        public String getTitle() 
        {
            if (StringUtils.isEmpty(userAdminForm.getUserName())) 
            {
                return bundle.getString("userAdmin.title.searchUser");
            }
            return MessageFormat.format(
                    bundle.getString("userAdmin.title.editUser"), 
                    new Object[] { userAdminForm.getUserName() } );
        }
        public List getPermissions()
        {
            return permissions;
        }
        public void setPermissions(List permissions)
        {
            this.permissions = permissions;
        }
    }
}

