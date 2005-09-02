
package org.roller.presentation.tags.menu;

import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;

import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.RequestUtils;
import org.roller.presentation.RollerContext;


/////////////////////////////////////////////////////////////////////////

/** 
 * MenuImpls has collection of menu items. Multiple menus can be used 
 * in one session, but they must have unique names.
 */ 
public class MenuItemImpl extends BaseRollerMenu implements MenuItem
{
	private String mMenuId = null;
    
    //private Vector mMenuItems = new Vector();
		
	/** Is this the default menu? */
	boolean mDefault = false;

	//---------------------------------------------------

	public MenuItemImpl() {}

	/** Construct with name and Struts forward */
	public MenuItemImpl(String n, String f) 
    { 
        super(n, f); 
    }

	/** Parent menu's ID */ 
	public void setMenuId( String v ) { mMenuId = v; }

	/** Parent menu's ID */
	public String getMenuId() { return mMenuId; }

	/** Given a request, tells if menu item is selected */ 
	public boolean isSelected( HttpServletRequest req )
	{
		boolean selected = false;
        HttpSession ses = req.getSession(false);
        
        // first look for menu state in request params, then attributes
        String itemKey = req.getParameter(RollerMenuModel.MENU_ITEM_KEY );
        if (null == itemKey) 
        {
            itemKey = (String)req.getAttribute(RollerMenuModel.MENU_ITEM_KEY);
        }
        
        if (itemKey != null && itemKey.equals(mName)) 
        {
            selected = true;
        }
		else if (mForward != null) 
		{
			// next, can we use Struts forward name to find menu state
            ServletContext ctx = RollerContext.getServletContext();     
			ModuleConfig mConfig = RequestUtils.getModuleConfig(req, ctx);
			ForwardConfig fconfig = mConfig.findForwardConfig(mForward);
            ActionMapping amapping = 
                    (ActionMapping)req.getAttribute(Globals.MAPPING_KEY);            
			if (fconfig != null && amapping != null)
			{
                String reqPath = amapping.getPath();
                String fwdPath = fconfig.getPath();
                int end = fwdPath.indexOf(".do");
                fwdPath = (end == -1) ? fwdPath : fwdPath.substring(0, end);
                if  (fwdPath.equals(reqPath))
                {
                    selected = true;
                }
			}
		}
        
        // still not found, look for menu state in session attributes
		if (ses != null && selected)
		{
			ses.setAttribute(mMenuId+"_"+RollerMenuModel.MENU_ITEM_KEY, mName);
		}
		return selected;
	}
    
    /*
    public Vector getMenuItems() 
    {
        return mMenuItems;
    }

    public addMenuItem(MenuItem item)
    {
        mMenuItems.add(item);
    }
    */
}



//// get menu key from request param or from cookie
//String menuKeyName = mMenuId+"rmk";
//String menuKey = req.getParameter("rmk");
//if (menuKey == null) 
//{
//  Cookie menuCookie = RequestUtil.getCookie(req, menuKeyName);
//  if (menuCookie != null)
//  {
//      menuKey = menuCookie.getValue();
//      req.setAttribute("rmk", menuKey);
//  }
//}
//// save menu key in cookie
//RequestUtil.setCookie(res, menuKeyName, menuKey, req.getContextPath());
//
//// get menu item key from request param or from cookie
//String itemKeyName = mMenuId+"rmik";
//String itemKey = req.getParameter("rmik");
//if (itemKey == null) 
//{
//  Cookie itemCookie = RequestUtil.getCookie(req, itemKeyName);
//  if (itemCookie != null)
//  {
//      itemKey = itemCookie.getValue();
//      req.setAttribute("rmik", itemKey);
//  }
//}
//// save menu item key in cookie
//RequestUtil.setCookie(res, itemKeyName, itemKey, req.getContextPath());
        

