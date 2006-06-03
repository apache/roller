
package org.roller.presentation.tags.menu;
import javax.servlet.http.HttpServletRequest;

import org.roller.RollerException;

/** An individual menu which contains MenuItems */ 
public interface Menu 
{
	/** Name of Menu */
	public String getName();

	/** Collection of MenuItem objects contained in this menu */
	public java.util.Vector getMenuItems();

	/** Determine if this menu is selected based on request */
	public boolean isSelected( HttpServletRequest req ) throws RollerException;

	/** Get currently selected menu item in this menu */
	public MenuItem getSelectedMenuItem( HttpServletRequest req ) throws RollerException;

    /** Url to be displayed in menu */ 
    public String getUrl( javax.servlet.jsp.PageContext pctx );
    
    /** Is user principal permitted to use this menu? */ 
    public boolean isPermitted( HttpServletRequest req ) throws RollerException;
    
    /** Set roles allowed to use this menu (comma separated list). */ 
    public void setRoles( String roles );

    /** Name of true/false configuration property that enables this menu */ 
    public void setEnabledProperty( String enabledProperty );

    /** Name of true/false configuration property that disables this menu */ 
    public void setDisabledProperty( String disabledProperty );
}

