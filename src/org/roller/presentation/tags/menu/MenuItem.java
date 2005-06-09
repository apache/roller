
package org.roller.presentation.tags.menu;
import javax.servlet.http.HttpServletRequest;

/** An individual menu item, contained in a Menu */ 
public interface MenuItem 
{
	/** Name of menu */
	public String getName();

	/** Url to be displayed in menu */ 
	public String getUrl( javax.servlet.jsp.PageContext pctx );

	/** Determine if this menu item is selected based on request */
	public boolean isSelected( HttpServletRequest req );
    
    /** Name of true/false configuration property that enables this menu */ 
    public void setEnabledProperty( String enabledProperty );
    
    /** Name of true/false configuration property that disables this menu */ 
    public void setDisabledProperty( String disabledProperty );
}

