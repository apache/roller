package org.roller.presentation.tags.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.presentation.RollerRequest;
import org.roller.util.Utilities;

/**
 * Base class for Roller menu objects.
 * @author Dave Johnson
 */
public abstract class BaseRollerMenu
{
    protected String mName = null;
    protected String mEnabledProperty = null;
    protected String mDisabledProperty = null;
    protected List mRoles = new ArrayList();
    
    public BaseRollerMenu() {}
    
    public BaseRollerMenu(String name) { mName = name; }
    
    /** Name of menu */ 
    public void setName( String v ) { mName = v; }

    /** Name of menu */
    public String getName() { return mName; }
    
    /** Roles allowed to use menu, comma separated */ 
    public void setRoles( String roles ) 
    {
        mRoles = Arrays.asList(Utilities.stringToStringArray(roles,","));
    }
    
    /** Name of property that enables menu (or null if always enabled) */
    public void setEnabledProperty(String enabledProperty)
    {
        mEnabledProperty = enabledProperty;
    }

    /** Name of property that disable menu (or null if always enabled) */
    public void setDisabledProperty(String disabledProperty)
    {
        mDisabledProperty = disabledProperty;
    }
    
    /** Determine if menu  should be shown to use of specified request */
    public boolean isPermitted(HttpServletRequest req) throws RollerException
    {
        if (mEnabledProperty != null) 
        {
            String enabledProp = RollerConfig.getProperty(mEnabledProperty);
            if (enabledProp != null && enabledProp.equalsIgnoreCase("false"))
            {
                return false;
            }
        }
        if (mDisabledProperty != null) 
        {
            String disabledProp = RollerConfig.getProperty(mDisabledProperty);
            if (disabledProp != null && disabledProp.equalsIgnoreCase("true"))
            {
                return false;
            }
        }
        if (mRoles != null && mRoles.size() > 0)
        {
            Iterator roles = mRoles.iterator();
            while (roles.hasNext())
            {
                RollerRequest rreq = RollerRequest.getRollerRequest(req);
                String role = (String)roles.next();
                if (req.isUserInRole(role)) 
                {
                    return true;
                }
                else if (role.equals("admin") && rreq.isAdminUser()) 
                {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
