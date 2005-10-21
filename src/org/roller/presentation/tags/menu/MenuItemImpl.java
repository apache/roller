
package org.roller.presentation.tags.menu;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.presentation.RollerContext;


/////////////////////////////////////////////////////////////////////////

/** 
 * MenuImpls has collection of menu items. Multiple menus can be used 
 * in one session, but they must have unique names.
 */ 
public class MenuItemImpl extends BaseRollerMenu implements MenuItem
{
	private String mMenuId = null;
	
	/** Name of Struts forward */
	String mForward = null;
	
	/** Is this the default menu? */
	boolean mDefault = false;

	//---------------------------------------------------

	public MenuItemImpl() {}

	/** Construct with name and Struts forward */
	public MenuItemImpl(String n, String f) 
    { 
        super(n); 
        mForward = f; 
    }

	/** Parent menu's ID */ 
	public void setMenuId( String v ) { mMenuId = v; }

	/** Parent menu's ID */
	public String getMenuId() { return mMenuId; }

	/** Struts forward */ 
	public String getForward() { return mForward; }

	/** Struts forward */ 
	public void setForward( String forward ) { mForward = forward; }

	/** Name of Struts forward menu item should link to */
	public String getUrl( PageContext pctx ) 
	{
		String url = null;
		try 
		{
			Hashtable params = RollerMenuModel.createParams(
					(HttpServletRequest)pctx.getRequest());
			params.put( RollerMenuModel.MENU_ITEM_KEY, getName() );

			url = RequestUtils.computeURL( 
				pctx, 
				mForward, // forward
				null,     // href
				null,     // page
				null,
				params,   // params 
				null,     // anchor
				false );  // redirect
		}
		catch (Exception e)
		{
			pctx.getServletContext().log(
				"ERROR in menu item creating URL",e);
		}
		return url;
	}

	/** Given a request, tells if menu item is selected */ 
	public boolean isSelected( HttpServletRequest req )
	{
		boolean selected = false;
        HttpSession ses = req.getSession(false);
        
        String itemKey = req.getParameter(RollerMenuModel.MENU_ITEM_KEY );
        if (null == itemKey) 
        {
            itemKey = (String)req.getAttribute(RollerMenuModel.MENU_ITEM_KEY);
        }
        if (null == itemKey) 
        {
            itemKey = (String)ses.getAttribute(mMenuId+"_"+RollerMenuModel.MENU_ITEM_KEY);
        }

        if (itemKey != null && itemKey.equals(mName)) 
        {
            selected = true;
        }
		else
		{
			// Is this item's forward the one being requested?
            ServletContext ctx = RollerContext.getServletContext();     
			ModuleConfig mConfig = RequestUtils.getModuleConfig(req,ctx);
			ForwardConfig fConfig = mConfig.findForwardConfig(mForward);						
			if (fConfig != null)
			{
				// Is the forward path in the request's URL?
				String url = req.getRequestURL().toString();
				
				if ( url.indexOf( fConfig.getPath() ) != -1 )
				{
					//  Yes it is, so return true - this item is selected
					selected = true;
				}
				
			}
		}
		if (ses != null && selected)
		{
			ses.setAttribute(mMenuId+"_"+RollerMenuModel.MENU_ITEM_KEY, mName);
		}
		return selected;
	}

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
        

