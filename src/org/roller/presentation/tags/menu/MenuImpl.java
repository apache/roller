
package org.roller.presentation.tags.menu;

import org.roller.RollerException;
import org.roller.presentation.RollerRequest;
import org.roller.util.Utilities;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/////////////////////////////////////////////////////////////////////////

/** MenuImpl model has collection of menus */
public class MenuImpl implements Menu
{
	private String mMenuId = null;
    private List mRoles = new ArrayList();
    
	/** Vector of MenuItemImpl objects */
	Vector mMenuItems = new Vector();
	
	/** Name of menu */
	String mName = null;

	/** Is this the default menu? */
	boolean mDefault = false;


	//---------------------------------------------------
	
	public MenuImpl() {}

	/** Construct with name */
	public MenuImpl(String n) { mName = n ;}
	
	//---------------------------------------------------

	/** Add MenuItemImpl to MenuImpl */
	public void addItem( MenuItemImpl item ) { mMenuItems.addElement(item); };
	
	//---------------------------------------------------

	/** Parent menu's ID */ 
	public void setMenuId( String v ) { mMenuId = v; }

	/** Parent menu's ID */
	public String getMenuId() { return mMenuId; }

	//---------------------------------------------------

	/** Is this the default menu? */
	public void setDefault( boolean d ) { mDefault = d; }

	/** Is this the default menu? */
	public boolean isDefault() { return mDefault; }

	//---------------------------------------------------

	/** Name of menu */ 
	public void setName( String v ) { mName = v; }

	/** Name of menu */
	public String getName() { return mName; }
	
    //---------------------------------------------------
    /** Roles allowed to use menu, comma separated */ 
    public void setRoles( String roles ) {
        mRoles = Arrays.asList(Utilities.stringToStringArray(roles,","));
    }
    
	//---------------------------------------------------

	/** Collection of MenuItemImpl objects */
	public Vector getMenuItems() { return mMenuItems; }

	//---------------------------------------------------

	/** Get currently selected menu item in this menu */
	public MenuItem getSelectedMenuItem( HttpServletRequest req )
	{
		return getSelectedMenuItem( req, true ) ;
	}

	/** Get currently selected menu item in this menu */
	public MenuItem getSelectedMenuItem( HttpServletRequest req, 
			boolean returnDefault )
	{
		MenuItemImpl def = null;
		MenuItemImpl selected = null;
		for ( int i=0; i<mMenuItems.size(); i++ ) 
		{
			MenuItemImpl item = (MenuItemImpl)mMenuItems.elementAt(i);
			if ( item.isSelected( req ) )
			{
				selected = item;
				break;
			}
			if ( item.isDefault() )
			{
				def = item;
			}
		}
		if ( selected != null )
		{
			return selected;
		}
		else if ( returnDefault )
		{
			return def;
		}
		else 
		{
			return null;
		}
	}

	//---------------------------------------------------

	/** Is this menu selected? */ 
	public boolean isSelected( HttpServletRequest req )
	{
        boolean selected = false;
        HttpSession ses = req.getSession(false);
        
        String menuKey = req.getParameter(RollerMenuModel.MENU_KEY );
        if (null == menuKey) 
        {
            menuKey = (String)req.getAttribute(RollerMenuModel.MENU_KEY);
        }
        if (null == menuKey) 
        {
            menuKey = (String)ses.getAttribute(mMenuId+"_"+RollerMenuModel.MENU_KEY);
        }

        if (menuKey != null && menuKey.equals(mName)) 
        {
            selected = true;
        }
		else
		{
			if ( getSelectedMenuItem( req, false ) != null ) 
			{
				selected = true;
			}
		}

		if ( ses != null && selected )
		{
			ses.setAttribute(mMenuId + "_" + RollerMenuModel.MENU_KEY, mName);
		}

		return selected;
	}

	//---------------------------------------------------

	/** Name of Struts forward menu item should link to */
	public String getUrl( PageContext pctx ) 
	{
		String url = null;
		try 
		{
			HttpServletRequest req = (HttpServletRequest)pctx.getRequest();
			String surl = getSelectedMenuItem( req ).getUrl( pctx ); 
			StringBuffer sb = new StringBuffer( surl ); 
			if ( surl.indexOf("?") == -1 )
			{
				sb.append( "?" ); 
			}
			else
			{
				sb.append( "&amp;" ); 
			}
			sb.append( RollerMenuModel.MENU_KEY );
			sb.append( "=" ); 
			sb.append( getName() );	
			url = sb.toString();
		}
		catch (Exception e)
		{
			pctx.getServletContext().log(
				"ERROR in menu creating URL",e);
		}
		return url;
	}

    /** 
     * @see org.roller.presentation.tags.menu.Menu#isPermitted(javax.servlet.jsp.PageContext)
     */
    public boolean isPermitted( HttpServletRequest req ) throws RollerException
    {
        if (mRoles.size() > 0)
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

