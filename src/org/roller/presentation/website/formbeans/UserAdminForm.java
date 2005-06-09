
package org.roller.presentation.website.formbeans;

import java.util.Locale;

import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.pojos.UserData;
import org.roller.presentation.forms.UserForm;

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
    private Boolean mUserEnabled = Boolean.FALSE;
    private Boolean mUserAdmin = Boolean.FALSE;

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

    /**
     * @return Returns the mEnabled.
     */
    public Boolean getUserEnabled()
    {
        return this.mUserEnabled;
    }
    
    /**
     * @param enabled The mEnabled to set.
     */
    public void setUserEnabled(Boolean enabled)
    {
        this.mUserEnabled = enabled;
    }
    
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
}
