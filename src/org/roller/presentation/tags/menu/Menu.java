/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/

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

