
package org.roller.presentation.tags.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
            ServletContext ctx = RollerContext.getServletContext();     
			ModuleConfig mConfig = RequestUtils.getModuleConfig(req, ctx);
            ActionMapping amapping = (ActionMapping)req.getAttribute(Globals.MAPPING_KEY);
            List fconfigs = new ArrayList();
			fconfigs.add(mConfig.findForwardConfig(mForward));
            if (mSubforwards != null) {
                String[] subforwards = mSubforwards.split(",");
                for (int i=0; i<subforwards.length; i++) {
                    fconfigs.add(mConfig.findForwardConfig(subforwards[i]));
                }
            }
            for (Iterator iter = fconfigs.iterator(); iter.hasNext();) {
                ForwardConfig fconfig = (ForwardConfig)iter.next();
                String fwdPath = fconfig.getPath();
                int end = fwdPath.indexOf(".do");
                fwdPath = (end == -1) ? fwdPath : fwdPath.substring(0, end);
                if  (fwdPath.equals(amapping.getPath()))
                {
                    selected = true;
                    break;
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
        

