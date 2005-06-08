
package org.roller.presentation.tags.menu;
import javax.servlet.http.HttpServletRequest;


/** Menu model contains Menus */ 
public interface MenuModel 
{
	/** Collection of Menu objects contained in this menu model */
	public java.util.Vector getMenus();

	/** Return menu selected by current request or first menu.
	 *  If request does not indicate a menu then first menu is returned.
	 */
	public Menu getSelectedMenu( HttpServletRequest req );
}

