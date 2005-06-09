
package org.roller.presentation.tags.menu;
import javax.servlet.http.HttpServletRequest;

import org.roller.RollerException;


/** Menu model contains Menus */ 
public interface MenuModel 
{
	/** Collection of Menu objects contained in this menu model */
	public java.util.Vector getMenus();

	/** Return menu selected by current request or first menu.
	 *  If request does not indicate a menu then first menu is returned.
	 * @throws RollerException
	 */
	public Menu getSelectedMenu( HttpServletRequest req ) throws RollerException;
}

