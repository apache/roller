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

package org.apache.roller.ui.core.tags.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.apache.struts.util.RequestUtils;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.util.Utilities;


/**
 * Base class for Roller menu objects.
 */
public abstract class BaseRollerMenu {
    
    protected String mName = null;
    protected String mForward = null;
    protected String mSubforwards = null;
    protected String mEnabledProperty = null;
    protected String mDisabledProperty = null;
    protected List mRoles = new ArrayList();
    protected List mPerms = new ArrayList();
    
    public BaseRollerMenu() {
        init();
    }
    
    public BaseRollerMenu(String name, String forward) {
        mName = name;
        mForward = forward;
        init();
    }
    
    /**
     * Set defaults as described in WEB-INF/editor-menu.xml
     */
    public void init() {
        mRoles.add("admin");
        mRoles.add("editor");
        
        mPerms.add("admin");
        mPerms.add("author");
    }
    
    /** Name of menu */
    public void setName( String v ) { mName = v; }
    
    /** Name of menu */
    public String getName() { return mName; }
    
    /** Struts forward */
    public String getForward() { return mForward; }
    
    /** Struts forward */
    public void setForward( String forward ) { mForward = forward; }
    
    /** Subforward: other forwards grouped under this menu */
    public String getSubforwards() { return mSubforwards; }
    
    /** Subforwards: other forwards grouped under this menu */
    public void setSubforwards( String subforwards ) { mSubforwards = subforwards; }
    
    /** Roles allowed to view menu, comma separated */
    public void setRoles( String roles ) {
        mRoles = Arrays.asList(Utilities.stringToStringArray(roles,","));
    }
    
    /** Website permissions required to view menu, comma separated */
    public void setPerms( String perms ) {
        mPerms = Arrays.asList(Utilities.stringToStringArray(perms,","));
    }
    
    /** Name of property that enables menu (or null if always enabled) */
    public void setEnabledProperty(String enabledProperty) {
        mEnabledProperty = enabledProperty;
    }
    
    /** Name of property that disable menu (or null if always enabled) */
    public void setDisabledProperty(String disabledProperty) {
        mDisabledProperty = disabledProperty;
    }
    
    /** Determine if menu  should be shown to use of specified request */
    public boolean isPermitted(HttpServletRequest req) throws RollerException {
        // first, bail out if menu is disabled
        if (mEnabledProperty != null) {
            String enabledProp = RollerConfig.getProperty(mEnabledProperty);
            if (enabledProp != null && enabledProp.equalsIgnoreCase("false")) {
                return false;
            }
        }
        if (mDisabledProperty != null) {
            String disabledProp = RollerConfig.getProperty(mDisabledProperty);
            if (disabledProp != null && disabledProp.equalsIgnoreCase("true")) {
                return false;
            }
        }
        RollerSession rses = RollerSession.getRollerSession(req);
        boolean ret = true;
        
        if (rses != null && rses.isGlobalAdminUser()) return true;
        
        // next, make sure that users role permits it
        if (mRoles != null && mRoles.size() > 0) {
            ret = false;
            Iterator roles = mRoles.iterator();
            while (roles.hasNext()) {
                String role = (String)roles.next();
                if (req.isUserInRole(role) || role.equals("any")) {
                    ret = true;
                    break;
                }
            }
        }
        
        // finally make sure that user has required website permissions
        if (ret && mPerms != null && mPerms.size() > 0) {
            UserData user = null;
            if (rses != null) user = rses.getAuthenticatedUser();
            
            WebsiteData website = getRequestedWeblog(req);
            BasePageModel pageModel = (BasePageModel)req.getAttribute("model");
            if (pageModel != null) {
                website = pageModel.getWebsite();
            }
            
            PermissionsData permsData = null;
            if (user != null && website != null) {
                permsData = RollerFactory.getRoller()
                .getUserManager().getPermissions(website, user);
            }
            ret = false;
            Iterator perms = mPerms.iterator();
            while (perms.hasNext()) {
                String perm = (String)perms.next();
                if (perm.equals("any")) {
                    ret = true; // any permission will do (including none)
                    break;
                }
                if (permsData != null &&
                        ((perm.equals("admin")  && permsData.has(PermissionsData.ADMIN))
                        || (perm.equals("author")  && permsData.has(PermissionsData.AUTHOR))
                        || (perm.equals("limited") && permsData.has(PermissionsData.LIMITED)))) {
                    ret = true; // user has one of the required permissions
                    break;
                }
            }
        }
        return ret;
    }
    
    /** Name of Struts forward menu item should link to */
    public String getUrl( PageContext pctx ) {
        String url = null;
        try {
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
        } catch (Exception e) {
            pctx.getServletContext().log(
                    "ERROR in menu item creating URL",e);
        }
        return url;
    }
    
    /**
     * Currently, the menu tag can be used in both the authoring UI and the
     * rendering system, so we have to check both forms of URL to determine
     * the selected weblog.
     * 
     * TODO 3.0: more simple/consistent method for conveying weblog state across requests
     * 
     * NOTE: even better would be to separate this into 2 versions, one for
     *       the authoring/admin UI and one for rendering.  it doesn't make
     *       sense for this strange intermixing to be happening.
     */
    protected static WebsiteData getRequestedWeblog(HttpServletRequest request) throws RollerException {
        WebsiteData weblog = null;
        Roller roller = RollerFactory.getRoller();
        // first check authoring form of URL
        if (request.getParameter(RequestConstants.WEBLOG) != null) {
            String weblogHandle = request.getParameter(RequestConstants.WEBLOG);
            weblog = roller.getUserManager().getWebsiteByHandle(weblogHandle);
        } else if (request.getParameter(RequestConstants.WEBLOG_ID) != null) {
            String weblogId = request.getParameter(RequestConstants.WEBLOG_ID);
            weblog = roller.getUserManager().getWebsite(weblogId);
        } else if (request.getParameter(RequestConstants.WEBLOGENTRY_ID) != null) {
            String entryId = request.getParameter(RequestConstants.WEBLOGENTRY_ID);
            WeblogEntryData entry = roller.getWeblogManager().getWeblogEntry(entryId);
            if(entry != null) {
                weblog = entry.getWebsite();
            }
        } else if (request.getParameter(RequestConstants.WEBLOGCATEGORY_ID) != null) {
            String catId = request.getParameter(RequestConstants.WEBLOGCATEGORY_ID);
            WeblogCategoryData cat = roller.getWeblogManager().getWeblogCategory(catId);
            if(cat != null) {
                weblog = cat.getWebsite();
            }
        } else if (request.getParameter(RequestConstants.FOLDER_ID) != null) {
            String folderId = request.getParameter(RequestConstants.FOLDER_ID);
            FolderData folder = roller.getBookmarkManager().getFolder(folderId);
            if(folder != null) {
                weblog = folder.getWebsite();
            }
        } else if (request.getSession().getAttribute(RequestConstants.WEBLOG_SESSION_STASH) != null) {
            String handle = (String)request.getSession().getAttribute(RequestConstants.WEBLOG_SESSION_STASH);
            weblog = roller.getUserManager().getWebsiteByHandle(handle);
        } else { 
            // check rendering system form of URL
            // TODO: hack.  we expect the parsed request as an HttpRequest attr
            WeblogPageRequest pageRequest = (WeblogPageRequest) request.getAttribute("pageRequest");
            if(pageRequest != null) {
                weblog = pageRequest.getWeblog();
            }
        }
        return weblog;
    }  
}
