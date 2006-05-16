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

package org.apache.roller.ui.admin.struts.formbeans;

import java.util.Locale;
import org.apache.roller.ui.authoring.struts.formbeans.UserFormEx;

import org.apache.struts.action.ActionMapping;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.authoring.struts.forms.UserForm;

import javax.servlet.http.HttpServletRequest;

/**
 * These properties are not persistent and are only needed for the UI.
 *
 * @struts.form name="userAdminForm"
 * @author dmj
 */
public class UserAdminForm extends UserFormEx
{
    private boolean mDelete = false;
    //private Boolean mUserEnabled = Boolean.FALSE;
    private Boolean mUserAdmin = Boolean.FALSE;
    private boolean newUser = false;

    public UserAdminForm()
    {
        super();
    }

    public UserAdminForm(UserData userData, java.util.Locale locale ) throws RollerException
    {
        super(userData, locale);
    }

    /**
     * Returns true if delete requested.
     * @return boolean
    **/
    public boolean getDelete()
    {
        return mDelete;
    }

    /**
     * Sets the Delete member.
     */
    public void setDelete(boolean delete)
    {
        mDelete = delete;
    }
    
	public void reset(ActionMapping mapping, HttpServletRequest request) 
	{
		super.reset(mapping, request);
		mDelete = false;
	}

    /*
     * @return Returns the mEnabled.
     */
    //public Boolean getUserEnabled()
    //{
        //return this.mUserEnabled;
    //}
    
    /*
     * @param enabled The mEnabled to set.
     */
    //public void setUserEnabled(Boolean enabled)
    //{
        //this.mUserEnabled = enabled;
    //}
    
    /**
     * @return Returns the mUserAdmin.
     */
    public Boolean getUserAdmin() {
        return mUserAdmin;
    }
    
    /**
     * @param userAdmin The mUserAdmin to set.
     */
    public void setUserAdmin(Boolean userAdmin) {
        mUserAdmin = userAdmin;
    }

    /** Override to grant/revoke admin role depending on form */
    public void copyTo(UserData user, Locale locale)
            throws RollerException
    {
        super.copyTo(user, locale);
        if (mUserAdmin.booleanValue()) 
        {
            user.grantRole("admin");
        }
        else
        {
            user.revokeRole("admin");
        }
    }

    /** Override to set "administration" checkbox depending on user's roles */
    public void copyFrom(UserData user, Locale locale)
            throws RollerException
    {
        super.copyFrom(user, locale);
        mUserAdmin = user.hasRole("admin") ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean isNewUser() 
    {
        return newUser;
    }

    public void setNewUser(boolean newUser) 
    {
        this.newUser = newUser;
    }
}
