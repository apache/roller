package org.roller.presentation.tags.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.RollerFactory;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
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
    protected List mPerms = new ArrayList();
    
    public BaseRollerMenu() 
    {
        init();
    }
    
    public BaseRollerMenu(String name) 
    { 
        mName = name; 
        init();
    }
    
    /**
     * Set defaults as described in WEB-INF/editor-menu.xml
     */
    public void init() 
    {
        mRoles.add("admin"); 
        mRoles.add("editor");
        
        mPerms.add("admin");
        mPerms.add("author");
    }
    
    /** Name of menu */ 
    public void setName( String v ) { mName = v; }

    /** Name of menu */
    public String getName() { return mName; }
    
    /** Roles allowed to view menu, comma separated */ 
    public void setRoles( String roles ) 
    {
        mRoles = Arrays.asList(Utilities.stringToStringArray(roles,","));
    }
    
    /** Website permissions required to view menu, comma separated */ 
    public void setPerms( String perms ) 
    {
        mPerms = Arrays.asList(Utilities.stringToStringArray(perms,","));
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
        // first, bail out if menu is disabled
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
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        boolean ret = false;
   
        // next, make sure that users role permits it
        if (mRoles != null && mRoles.size() > 0)
        {
            Iterator roles = mRoles.iterator();
            while (roles.hasNext())
            {
                String role = (String)roles.next();
                if (    req.isUserInRole(role) || role.equals("any")
                     || (role.equals("admin") 
                             && rollerSession != null 
                             && rollerSession.isAdminUser()))  
                {
                    ret = true;
                    break;
                }
            }
        }
        
        // finally make sure that user has required website permissions
        if (ret && mPerms != null && mPerms.size() > 0)
        {
            UserData user = null;
            if (rollerSession != null) user = rollerSession.getAuthenticatedUser();
            WebsiteData website = RollerSession.getRollerSession(req).getCurrentWebsite();
            PermissionsData permsData = null;
            if (user != null && website != null) 
            {
                permsData =
                    RollerFactory.getRoller().getUserManager().getPermissions(website, user);
            }
            ret = false;
            Iterator perms = mPerms.iterator();
            while (perms.hasNext())
            {
               String perm = (String)perms.next();
               if (perm.equals("any")) 
               {
                   ret = true; // any permission will do (including none)
                   break;
               }
               if (permsData != null && 
                  ((perm.equals("admin")  && permsData.has(PermissionsData.ADMIN)) 
               || (perm.equals("author")  && permsData.has(PermissionsData.AUTHOR))
               || (perm.equals("limited") && permsData.has(PermissionsData.LIMITED))))                     
               {
                   ret = true; // user has one of the required permissions
                   break;
               }
            }
        }
        return ret;
    }

}
