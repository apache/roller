
package org.roller.presentation.tags.menu;
import org.roller.RollerException;

import javax.servlet.http.HttpServletRequest;

/** An individual menu which contains MenuItems */ 
public interface Menu 
{
	/** Name of Menu */
	public String getName();

	/** Collection of MenuItem objects contained in this menu */
	public java.util.Vector getMenuItems();

	/** Determine if this is the default menu item with it's menu */
	public boolean isDefault();

	/** Determine if this menu is selected based on request */
	public boolean isSelected( HttpServletRequest req );

	/** Get currently selected menu item in this menu */
	public MenuItem getSelectedMenuItem( HttpServletRequest req );

    /** Url to be displayed in menu */ 
    public String getUrl( javax.servlet.jsp.PageContext pctx );
    
    /** Is user principal permitted to use this menu? */ 
    public boolean isPermitted( HttpServletRequest req ) throws RollerException;
    
    /** Set roles allowed to use this menu (comma separated list). */ 
    public void setRoles( String roles );
}

