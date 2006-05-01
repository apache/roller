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
/*
 * Created on Dec 13, 2005
 */
package org.apache.roller.business.jdo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.PersistenceStrategy;
import org.apache.roller.business.UserManagerImpl;
import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.PingQueueManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.RoleData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOUserManagerImpl extends UserManagerImpl {
    public JDOUserManagerImpl(PersistenceStrategy strategy) {
        super(strategy);
    }

    /**
     * Get websites of a user
     */
    public List getWebsites(UserData user, Boolean enabled)
            throws RollerException {
        return null;
    }

    /**
     * Get users of a website
     */
    public List getUsers(WebsiteData website, Boolean enabled)
            throws RollerException {
        return null;
    }

    /**
     * Use Hibernate directly because Roller's Query API does too much
     * allocation.
     */
    public WeblogTemplate getPageByLink(WebsiteData website, String pagelink)
            throws RollerException {
        return null;
    }

    /**
     * Return website specified by handle.
     */
    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled)
            throws RollerException {
        return null;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public UserData getUser(String userName, Boolean enabled)
            throws RollerException {
        return null;
    }

    //------------------------------------------------------------------------
    /**
     * @see org.apache.roller.model.UserManager#getPages(WebsiteData)
     */
    public List getPages(WebsiteData website) throws RollerException {
        return null;
    }

    /**
     * @see org.apache.roller.model.UserManager#getPageByName(WebsiteData,
     *      java.lang.String)
     */
    public WeblogTemplate getPageByName(WebsiteData website, String pagename)
            throws RollerException {
        return null;
    }

    /*
     * @see org.apache.roller.business.UserManagerBase#getRoles(org.apache.roller.pojos.UserData)
     */
    public List getUserRoles(UserData user) throws RollerException {
        return null;
    }

    public List getUsers(Boolean enabled) throws RollerException {
        return null;
    }

    /**
     * @see org.apache.roller.model.UserManager#removeWebsiteContents(org.apache.roller.pojos.WebsiteData)
     */
    public void removeWebsiteContents(WebsiteData website)
            throws RollerException {
    }

    /**
     * Return permissions for specified user in website
     */
    public PermissionsData getPermissions(WebsiteData website, UserData user)
            throws RollerException {
        return null;
    }

    /**
     * Get pending permissions for user
     */
    public List getPendingPermissions(UserData user) throws RollerException {
        return null;
    }

    /**
     * Get pending permissions for website
     */
    public List getPendingPermissions(WebsiteData website)
            throws RollerException {
        return null;
    }

    /**
     * Get all permissions of a website (pendings not including)
     */
    public List getAllPermissions(WebsiteData website) throws RollerException {
        return null;
    }

    /**
     * Get all permissions of a user.
     */
    public List getAllPermissions(UserData user) throws RollerException {
        return null;
    }

    public List getUsersStartingWith(String startsWith, int offset, int length,
            Boolean enabled) throws RollerException {
        return null;
    }
}

