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

package org.apache.roller.weblogger.ui.core.tags.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.RequestUtils;

import org.apache.roller.RollerException;
import org.apache.roller.weblogger.ui.core.RollerContext;

/////////////////////////////////////////////////////////////////////////

/** MenuImpl model has collection of menus */
public class MenuImpl extends BaseRollerMenu implements Menu {
    private String mMenuId = null;
    
    /** Vector of MenuItemImpl objects */
    Vector mMenuItems = new Vector();
    
    /** Is this the default menu? */
    boolean mDefault = false;
    
    public MenuImpl() {}
    
    /** Construct with name */
    public MenuImpl(String n) { super(n, null); }
    
    /** Add MenuItemImpl to MenuImpl */
    public void addItem( MenuItemImpl item ) { mMenuItems.addElement(item); };
    
    /** Parent menu's ID */
    public void setMenuId( String v ) { mMenuId = v; }
    
    /** Parent menu's ID */
    public String getMenuId() { return mMenuId; }
    
    /** Collection of MenuItemImpl objects */
    public Vector getMenuItems() { return mMenuItems; }
    
    /** Get currently selected menu item in this menu
     * @throws RollerException*/
    public MenuItem getSelectedMenuItem( HttpServletRequest req ) throws RollerException {
        return getSelectedMenuItem( req, true ) ;
    }
    
    /**
     * Get currently selected menu item in this menu
     * @throws RollerException
     */
    public MenuItem getSelectedMenuItem( HttpServletRequest req,
            boolean returnDefault ) throws RollerException {
        MenuItemImpl def = null;
        MenuItemImpl selected = null;
        for ( int i=0; i<mMenuItems.size(); i++ ) {
            MenuItemImpl item = (MenuItemImpl)mMenuItems.elementAt(i);
            if ( item.isSelected( req ) ) {
                selected = item;
                break;
            }
            // Set first permitted and enabled menu item in each menu as default
            if ( item.isPermitted(req) && def == null) {
                def = item;
            }
        }
        if ( selected != null ) {
            return selected;
        } else if ( returnDefault ) {
            return def;
        } else {
            return null;
        }
    }
    
    /**
     * Get default menu item (first one that is permitted)
     * @throws RollerException
     */
    public MenuItem getDefaultMenuItem( HttpServletRequest req )
    throws RollerException {
        MenuItemImpl def = null;
        MenuItemImpl selected = null;
        for ( int i=0; i<mMenuItems.size(); i++ ) {
            // Set first permitted and enabled menu item in each menu as default
            MenuItemImpl item = (MenuItemImpl)mMenuItems.elementAt(i);
            if (item.isPermitted(req) && def == null) {
                def = item;
            }
        }
        return def;
    }
    
    /**
     * Is this menu selected?
     * @throws RollerException
     */
    public boolean isSelected( HttpServletRequest req ) throws RollerException {
        boolean selected = false;
        HttpSession ses = req.getSession(false);
        
        // try to get state from request param then attribute
        String menuKey = req.getParameter(RollerMenuModel.MENU_KEY );
        if (null == menuKey) {
            menuKey = (String)req.getAttribute(RollerMenuModel.MENU_KEY);
        }
        if (menuKey != null && menuKey.equals(mName)) {
            selected = true;
        }
        // next, if submenu is selected, then we're selected
        else if (getSelectedMenuItem(req, false) != null) {
            selected = true;
        }
        // next, try to use Struts forward to determine state
        else if (mForward != null) {
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
                if  (fwdPath.equals(amapping.getPath())) {
                    selected = true;
                    break;
                }
            }
        }
        return selected;
    }
    
    /** Name of Struts forward menu item should link to */
    public String getUrl(PageContext pctx) {
        String url = null;
        try {
            // If no forward specified, use default submenu URL
            if (mForward == null && mMenuItems != null && mMenuItems.size() > 0) {
                HttpServletRequest req = (HttpServletRequest)pctx.getRequest();
                String surl = getDefaultMenuItem( req ).getUrl( pctx );
                StringBuffer sb = new StringBuffer( surl );
                if ( surl.indexOf("?") == -1 ) {
                    sb.append( "?" );
                } else {
                    sb.append( "&amp;" );
                }
                sb.append( RollerMenuModel.MENU_KEY );
                sb.append( "=" );
                sb.append( getName() );
                url = sb.toString();
            } else {
                return super.getUrl(pctx);
            }
        } catch (Exception e) {
            pctx.getServletContext().log(
                    "ERROR in menu creating URL",e);
        }
        return url;
    }
    
}

