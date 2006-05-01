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

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.xml.sax.SAXException;

//////////////////////////////////////////////////////////////////////////////

/** 
 * @author David M Johnson
 */ 
public class RollerMenuModel extends BaseRollerMenu implements MenuModel
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerMenuModel.class);
        
	private String mMenuId = null;

	/** Vector of MenuImpl objects */
	private Vector mMenus = new Vector();

	/** Key used to store selected menu in request */
	public static final String MENU_KEY = "rmk";

	/** Key used to store selected menu item in request */
	public static final String MENU_ITEM_KEY = "rmik";

	//------------------------------------------------------------------------

	/** Construct menu model based on menu XML */	
	public RollerMenuModel( String menuId, String config, ServletContext ctx )
	{
		try
		{
			mMenuId = menuId;

			Digester digester = new Digester();
			digester.push(this);
			//digester.setDebug(99);
			//digester.setClassLoader( getClass().getClassLoader() );
			//digester.setValidating(true);

			String menuPath = "menu-bar/menu";
			String menuItemPath = "menu-bar/menu/menu-item";

			digester.addObjectCreate( menuPath,
			 	"org.roller.presentation.tags.menu.MenuImpl");
			digester.addSetProperties( menuPath );
			digester.addSetNext( menuPath,"addMenu",
			 	"org.roller.presentation.tags.menu.Menu");

			digester.addObjectCreate( menuItemPath,
			 	"org.roller.presentation.tags.menu.MenuItemImpl");
			digester.addSetProperties( menuItemPath );
			digester.addSetNext( menuItemPath, "addItem",
			 	"org.roller.presentation.tags.menu.MenuItemImpl");

			InputStream input = ctx.getResourceAsStream(config);
			try 
			{
				digester.parse(input);

				if ( getMenus() != null )
				{
					Vector menus = getMenus();
					for (int i=0; i<menus.size(); i++)
					{
						MenuImpl menu = (MenuImpl)menus.elementAt(i);
						menu.setMenuId( mMenuId );
						Vector menuItems = menu.getMenuItems();
						if ( menuItems != null )
						{
						  for (int j=0; j<menuItems.size(); j++ )
						  {
							  MenuItemImpl item = 
							  	(MenuItemImpl)menuItems.elementAt(j);
							  item.setMenuId( mMenuId );
						  }
						}
					}
				}
			} 
			catch (SAXException e) 
			{
                mLogger.error("Unexpected exception",e);
			}
            finally 
            {
                if ( input!=null )
                {
                    try { input.close(); } 
                    catch (Exception e) { mLogger.error("Unexpected exception",e); };
                }
            }
		}	
		catch (Exception e)
       	{
            mLogger.error("Unexpected exception",e);
		}
	}

	//----------------------------------------------- MenuModel implementation

	public Vector getMenus()
	{
		return mMenus;
	}

	//----------------------------------------------------
	public Menu getSelectedMenu( HttpServletRequest req ) throws RollerException
	{
		MenuImpl def = null;
		MenuImpl selected = null; 
		for ( int i=0; i<mMenus.size(); i++ ) 
		{
			MenuImpl menu = (MenuImpl)mMenus.elementAt(i);
			if ( menu.isSelected( req ) )
			{
				selected = menu;
				break;
			}
			if (def == null)
			{
				def = menu;
			}
		}
		if ( selected != null )
		{
			return selected;
		}
		else
		{
			return def;
		}
	}

	//----------------------------------------------------
	public void addMenu( Menu menu )
	{
		mMenus.addElement( menu );
	}

	//------------------------------------------------------------------------

	/** Create params based on incoming request */
	static Hashtable createParams( HttpServletRequest req )
	{
		Hashtable params = new Hashtable();
		RollerRequest rreq = RollerRequest.getRollerRequest(req);
		try
		{
            WebsiteData website = rreq.getWebsite();
            BasePageModel pageModel = (BasePageModel)req.getAttribute("model");
            if (website == null && pageModel != null) 
            {
                website = pageModel.getWebsite();              
            }
            if (website != null)
            {
                params.put(RollerRequest.WEBLOG_KEY, website.getHandle());
            }
        }
		catch (Exception e)
		{
			mLogger.error("ERROR getting user in menu model", e);
		}
		return params;
	}
}


