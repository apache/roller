
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
package org.apache.roller.business.datamapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.PingQueueEntryData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogEntryTagData;
import org.apache.roller.pojos.WeblogEntryTagAggregateData;
import org.apache.roller.pojos.RoleData;
import org.apache.roller.pojos.PersistentObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

/*
 * DatamapperUserManagerImpl.java
 *
 * Created on May 29, 2006, 3:15 PM
 *
 */
public abstract class DatamapperUserManagerImpl implements UserManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(DatamapperUserManagerImpl.class);    

    protected DatamapperPersistenceStrategy strategy;
    

    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable();

    // cached mapping of userNames -> userIds
    private Map userNameToIdMap = new Hashtable();

    public DatamapperUserManagerImpl(DatamapperPersistenceStrategy strat) {
        log.debug("Instantiating Datamapper User Manager");

        this.strategy = strat;
    }

    /**
     * Update existing website.
     */
    public void saveWebsite(WebsiteData website) throws RollerException {

        website.setLastModified(new java.util.Date());
        strategy.store(website);
    }

    public void removeWebsite(WebsiteData weblog) throws RollerException {

        // remove contents first, then remove website
        this.removeWebsiteContents(weblog);
        this.strategy.remove(weblog);

        // remove entry from cache mapping
        this.weblogHandleToIdMap.remove(weblog.getHandle());
    }

    /**
     * convenience method for removing contents of a weblog.
     * TODO BACKEND: use manager methods instead of queries here
     * TODO DatamapperPort: Use bulk deletes instead of current approach
     */
    private void removeWebsiteContents(WebsiteData website) 
            throws  RollerException {

        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();

        // remove tags
        DatamapperQuery tagQuery = strategy.newQuery(WeblogEntryTagData.class,
                "WeblogEntryTagData.getByWeblog");
        for(Iterator iter = ( (List)tagQuery.execute(website) ) .iterator(); iter.hasNext();) {
            WeblogEntryTagData tagData = (WeblogEntryTagData) iter.next();
            this.strategy.remove(tagData);
        }

        // remove site tag aggregates
        List tags = wmgr.getTags(website, null, null, -1);
        updateTagAggregates(tags);

        // delete all weblog tag aggregates
        strategy.newRemoveQuery(
                WeblogEntryTagAggregateData.class,
                "WeblogEntryTagAggregateData.deleteByWeblog").removeAll(website);

        // delete all bad counts
        strategy.newRemoveQuery(
                WeblogEntryTagAggregateData.class,
                "WeblogEntryTagAggregateData.deleteByTotalLEZero").removeAll();


        // Remove the website's ping queue entries
        List queueEntries = (List)strategy.newQuery(PingQueueEntryData.class,
                "PingQueueEntryData.getByWebsite").execute(website);
        Iterator it = queueEntries.iterator();
        while(it.hasNext()) {
            this.strategy.remove((PingQueueEntryData) it.next());
        }

        // Remove the website's auto ping configurations
        AutoPingManager autoPingMgr = RollerFactory.getRoller()
            .getAutopingManager();
        List autopings = autoPingMgr.getAutoPingsByWebsite(website);
        it = autopings.iterator();
        while(it.hasNext()) {
            this.strategy.remove((AutoPingData) it.next());
        }

        // Remove the website's custom ping targets
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        List pingtargets = pingTargetMgr.getCustomPingTargets(website);
        it = pingtargets.iterator();
        while(it.hasNext()) {
            this.strategy.remove((PingTargetData) it.next());
        }

        // remove entries
        List entries = (List)strategy.newQuery(PingQueueEntryData.class,
                "WeblogEntryData.getByWebsite").execute(website);

        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            WeblogEntryData entry = (WeblogEntryData) iter.next();

            this.strategy.remove(entry);
        }

        // remove associated referers
        List referers = (List)strategy.newQuery(RefererData.class,
                "RefererData.getByWebsite").execute(website);
        for (Iterator iter = referers.iterator(); iter.hasNext();) {
            RefererData referer = (RefererData) iter.next();
            this.strategy.remove(referer);
        }

        // remove associated pages
        List pages = (List)strategy.newQuery(WeblogTemplate.class,
                "WeblogTemplate.getByWebsite").execute(website);

        for (Iterator iter = pages.iterator(); iter.hasNext();) {
            WeblogTemplate page = (WeblogTemplate) iter.next();
            this.strategy.remove(page);
        }

        // remove folders (including bookmarks)
        FolderData rootFolder = bmgr.getRootFolder(website);
        if (null != rootFolder) {
            this.strategy.remove(rootFolder);

            // Still cannot get all Bookmarks cleared!
//            Iterator allFolders = bmgr.getAllFolders(website).iterator();
//            while (allFolders.hasNext()) {
//                FolderData aFolder = (FolderData)allFolders.next();
//                bmgr.removeFolderContents(aFolder);
//                this.strategy.remove(aFolder);
//            }
        }

        // remove categories
        WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(website);
        if (null != rootCat) {
            this.strategy.remove(rootCat);
        }

        //remove permissions
        //TODO: Datamapper: this is a workaround for toplink bug that requires
        //to clean up from non owning side for removed objects.
        for (Iterator iterator = website.getPermissions().iterator(); iterator.hasNext();) {
            PermissionsData perms = (PermissionsData) iterator.next();
            //Remove it from database
            this.strategy.remove(perms);
            //Remove it from website
            iterator.remove();
            //Remove it from corresponding user
            UserData user = (UserData) getManagedObject(perms.getUser());
            user.getPermissions().remove(perms);
        }

        // flush the changes before returning. This is required as there is a
        // circular dependency between WeblogCategoryData and WebsiteData
        this.strategy.flush();
    }

    protected abstract void updateTagAggregates(List tags)
        throws RollerException;

    public void saveUser(UserData data) throws RollerException {
        this.strategy.store(data);
    }

    public void removeUser(UserData user) throws RollerException {
        //remove permissions
        //TODO: Datamapper: this is a workaround for toplink bug that requires
        //to clean up from non owning side for removed objects.
        for (Iterator iterator = user.getPermissions().iterator(); iterator.hasNext();) {
                PermissionsData perms = (PermissionsData) iterator.next();
                //Remove it from database
                this.strategy.remove(perms);
                //Remove it from website
                iterator.remove();
                //Remove it from corresponding user
                WebsiteData website = (WebsiteData) getManagedObject(perms.getWebsite());
                website.getPermissions().remove(perms);
        }

        this.strategy.remove(user);

        // remove entry from cache mapping
        this.userNameToIdMap.remove(user.getUserName());
    }

    public void savePermissions(PermissionsData perms) 
            throws RollerException {
        if(!PersistentObjectHelper.isObjectPersistent(perms)) {
            // This is a new object make sure that relationship is set on managed
            // copy of other side
            WebsiteData website = (WebsiteData) getManagedObject(perms.getWebsite());
            website.getPermissions().add(perms);

            UserData user = (UserData) getManagedObject(perms.getUser());
            user.getPermissions().add(perms);
        }
        this.strategy.store(perms);
    }

    public void removePermissions(PermissionsData perms) 
            throws RollerException {
        this.strategy.remove(perms);
        // make sure that relationship is set on managed
        // copy of other side
        WebsiteData website = (WebsiteData) getManagedObject(perms.getWebsite());
        website.getPermissions().remove(perms);

        UserData user = (UserData) getManagedObject(perms.getUser());
        user.getPermissions().remove(perms);
    }

    /**
     * @see org.apache.roller.model.UserManager#storePage(org.apache.roller.pojos.WeblogTemplate)
     */
    public void savePage(WeblogTemplate page) throws RollerException {
        this.strategy.store(page);

        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
            .saveWebsite(page.getWebsite());
    }

    public void removePage(WeblogTemplate page) throws RollerException {
        this.strategy.remove(page);
    }

    public void addUser(UserData newUser) throws RollerException {

        if(newUser == null)
            throw new RollerException("cannot add null user");

        // TODO BACKEND: we must do this in a better fashion, like getUserCnt()?
        boolean adminUser = false;
        List existingUsers = this.getUsers(null, Boolean.TRUE, null, null, 0, 1);
        if(existingUsers.size() == 0) {
            // Make first user an admin
            adminUser = true;
        }

        if(getUserByUserName(newUser.getUserName()) != null ||
            getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new RollerException("error.add.user.userNameInUse");
        }

        newUser.grantRole("editor");
        if(adminUser) {
            newUser.grantRole("admin");
        }

        this.strategy.store(newUser);
    }

    public void addWebsite(WebsiteData newWeblog) throws RollerException {

        this.strategy.store(newWeblog);
        this.addWeblogContents(newWeblog);
    }

    private void addWeblogContents(WebsiteData newWeblog) 
            throws RollerException {

        // grant weblog creator ADMIN permissions
        PermissionsData perms = new PermissionsData();
        perms.setUser(newWeblog.getCreator());
        perms.setWebsite(newWeblog);
        perms.setPending(false);
        perms.setPermissionMask(PermissionsData.ADMIN);
        savePermissions(perms);

        // add default category
        WeblogCategoryData rootCat = new WeblogCategoryData(
                newWeblog, // newWeblog
                null,      // parent
                "root",    // name
                "root",    // description
                null );    // image
        this.strategy.store(rootCat);

        String cats = RollerConfig.getProperty("newuser.categories");
        WeblogCategoryData firstCat = rootCat;
        if (cats != null && cats.trim().length() > 0) {
            String[] splitcats = cats.split(",");
            for (int i=0; i<splitcats.length; i++) {
                WeblogCategoryData c = new WeblogCategoryData(
                        newWeblog,       // newWeblog
                        rootCat,         // parent
                        splitcats[i],    // name
                        splitcats[i],    // description
                        null );          // image
                if (i == 0) firstCat = c;
                this.strategy.store(c);
            }
        }

        // Use first category as default for Blogger API
        newWeblog.setBloggerCategory(firstCat);

        // But default category for weblog itself should be  root
        newWeblog.setDefaultCategory(rootCat);

        this.strategy.store(newWeblog);

        // add default bookmarks
        FolderData root = new FolderData(
                null, "root", "root", newWeblog);
        this.strategy.store(root);

        Integer zero = new Integer(0);
        String blogroll = RollerConfig.getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (int i=0; i<splitroll.length; i++) {
                String[] rollitems = splitroll[i].split("\\|");
                if (rollitems != null && rollitems.length > 1) {
                    BookmarkData b = new BookmarkData(
                            root,                // parent
                            rollitems[0],        // name
                            "",                  // description
                            rollitems[1].trim(), // url
                            null,                // feedurl
                            zero,                // weight
                            zero,                // priority
                            null);               // image
                    this.strategy.store(b);
                }
            }
        }

        // add any auto enabled ping targets
        PingTargetManager pingTargetMgr = RollerFactory.getRoller()
            .getPingTargetManager();
        AutoPingManager autoPingMgr = RollerFactory.getRoller()
            .getAutopingManager();

        Iterator pingTargets = pingTargetMgr.getCommonPingTargets().iterator();
        PingTargetData pingTarget = null;
        while(pingTargets.hasNext()) {
            pingTarget = (PingTargetData) pingTargets.next();

            if(pingTarget.isAutoEnabled()) {
                AutoPingData autoPing = new AutoPingData(
                   null, pingTarget, newWeblog);
                autoPingMgr.saveAutoPing(autoPing);
            }
        }
    }

    /**
     * Creates and stores a pending PermissionsData for user and website specified.
     * TODO BACKEND: do we really need this?  can't we just use storePermissions()?
     */
    public PermissionsData inviteUser(WebsiteData website,
            UserData user, short mask) throws RollerException {

        if (website == null) throw new RollerException("Website cannot be null");
        if (user == null) throw new RollerException("User cannot be null");

        PermissionsData perms = new PermissionsData();
        perms.setWebsite(website);
        perms.setUser(user);
        perms.setPermissionMask(mask);
        savePermissions(perms);

        return perms;
    }

    /**
     * Remove user permissions from a website.
     *
     * TODO: replace this with a domain model method like weblog.retireUser(user)
     */
    public void retireUser(WebsiteData website, UserData user) throws RollerException {

        if (website == null) throw new RollerException("Website cannot be null");
        if (user == null) throw new RollerException("User cannot be null");

        Iterator perms = website.getPermissions().iterator();
        PermissionsData target = null;
        while (perms.hasNext()) {
            PermissionsData pd = (PermissionsData)perms.next();
            if (pd.getUser().getId().equals(user.getId())) {
                target = pd;
                break;
            }
        }
        if (target == null) throw new RollerException("User not member of website");

        website.removePermission(target);
        this.strategy.remove(target);
    }

    public void revokeRole(String roleName, UserData user) throws RollerException {
        Collection roles = user.getRoles();
        Iterator iter = roles.iterator();
        while (iter.hasNext()) {
            RoleData role = (RoleData) iter.next();
            if (role.getRole().equals(roleName)) {
                this.strategy.remove(role);
                iter.remove();
            }
        }
    }

    public WebsiteData getWebsite(String id) throws RollerException {
        return (WebsiteData) this.strategy.load(WebsiteData.class, id);
    }

    public WebsiteData getWebsiteByHandle(String handle) throws RollerException {
        return getWebsiteByHandle(handle, Boolean.TRUE);
    }

    /**
     * Return website specified by handle.
     */
    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled)
    throws RollerException {

        if (handle==null )
            throw new RollerException("Handle cannot be null");

        // check cache first
        // NOTE: if we ever allow changing handles then this needs updating
        if(this.weblogHandleToIdMap.containsKey(handle)) {

            WebsiteData weblog = this.getWebsite(
                (String) this.weblogHandleToIdMap.get(handle));
            if(weblog != null) {
                // only return weblog if enabled status matches
                if(enabled == null || enabled.equals(weblog.getEnabled())) {
                    log.debug("weblogHandleToId CACHE HIT - "+handle);
                    return weblog;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.weblogHandleToIdMap.remove(handle);
            }
        }

        DatamapperQuery query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByHandle");
        query.setUnique();
        WebsiteData website = (WebsiteData) query.execute(handle);

        // add mapping to cache
        if(website != null) {
            log.debug("weblogHandleToId CACHE MISS - "+handle);
            this.weblogHandleToIdMap.put(website.getHandle(), website.getId());
        }

        if(website != null &&
                (enabled == null || enabled.equals(website.getEnabled()))) {
            return website;
        } else {
            return null;
        }
    }

    /**
     * Get websites of a user
     */
    public List getWebsites(
        UserData user, Boolean enabled, Boolean active,
        Date startDate, Date endDate, int offset, int length) throws RollerException {
        
        DatamapperQuery query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;
        
        // TODO: ATLAS getWebsites DONE TESTED
        if (endDate == null) endDate = new Date();

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }
        
        if (startDate != null) {
           if (enabled != null) {
               if (active != null) {
                  if (user != null) {
                      query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&Enabled&Active&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                      if (setRange) query.setRange(offset, offset + length);
                      results = (List) query.execute(new Object[] {endDate, startDate, enabled, active, user, Boolean.FALSE});                
                  } else {
                      query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&Enabled&ActiveOrderByDateCreatedDesc");
                      if (setRange) query.setRange(offset, offset + length);
                      results = (List) query.execute(new Object[] {endDate, startDate, enabled, active});                                      
                  }
               } else {
                   if (user != null) {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&Enabled&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, startDate, enabled, user, Boolean.FALSE});                
                   } else {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&EnabledOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, startDate, enabled});                
                   }
               }
           } else {
               if (active != null) {
                   if (user != null) {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&Active&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, startDate, active, user, Boolean.FALSE});                
                   } else {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&ActiveOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, startDate, active});                
                   }
               } else {
                   if (user != null) {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDate&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, startDate, user, Boolean.FALSE});                
                   } else {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&StartDateOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, startDate});                
                   }
               }
           }
        } else {
            if (enabled != null) {
                if (active != null) {
                   if (user != null) {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&Enabled&Active&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, enabled, active, user, Boolean.FALSE});                
                   } else {
                       query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&Enabled&ActiveOrderByDateCreatedDesc");
                       if (setRange) query.setRange(offset, offset + length);
                       results = (List) query.execute(new Object[] {endDate, enabled, active});                                      
                   }
                } else {
                    if (user != null) {
                        query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&Enabled&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                        if (setRange) query.setRange(offset, offset + length);
                        results = (List) query.execute(new Object[] {endDate, enabled, user, Boolean.FALSE});                
                    } else {
                        query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&EnabledOrderByDateCreatedDesc");
                        if (setRange) query.setRange(offset, offset + length);
                        results = (List) query.execute(new Object[] {endDate, enabled});                
                    }
                }
            } else {
                if (active != null) {
                    if (user != null) {
                        query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&Active&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                        if (setRange) query.setRange(offset, offset + length);
                        results = (List) query.execute(new Object[] {endDate, active, user, Boolean.FALSE});                
                    } else {
                        query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&ActiveOrderByDateCreatedDesc");
                        if (setRange) query.setRange(offset, offset + length);
                        results = (List) query.execute(new Object[] {endDate, active});                
                    }
                } else {
                    if (user != null) {
                        query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDate&Permissions.user&Permissions.pendingOrderByDateCreatedDesc");
                        if (setRange) query.setRange(offset, offset + length);
                        results = (List) query.execute(new Object[] {endDate, user, Boolean.FALSE});                
                    } else {
                        query = strategy.newQuery(WebsiteData.class, "WebsiteData.getByEndDateOrderByDateCreatedDesc");
                        if (setRange) query.setRange(offset, offset + length);
                        results = (List) query.execute(endDate);                
                    }
                }
            }
        }        
        
        return results;
    }

    public UserData getUser(String id) throws RollerException {
        return (UserData)this.strategy.load(UserData.class, id);
    }

    public UserData getUserByUserName(String userName) throws RollerException {
        return getUserByUserName(userName, Boolean.TRUE);
    }

    public UserData getUserByUserName(String userName, Boolean enabled)
    throws RollerException {

        if (userName==null )
            throw new RollerException("userName cannot be null");

        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {

            UserData user = this.getUser(
                (String) this.userNameToIdMap.get(userName));
            if(user != null) {
                // only return the user if the enabled status matches
                if(enabled == null || enabled.equals(user.getEnabled())) {
                    log.debug("userNameToIdMap CACHE HIT - "+userName);
                    return user;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.userNameToIdMap.remove(userName);
            }
        }

        // cache failed, do lookup
        DatamapperQuery query;
        Object[] params;
        if (enabled != null) {
            query = strategy.newQuery(UserData.class,
                    "UserData.getByUserName&Enabled");
            params = new Object[] {userName, enabled};
        } else {
            query = strategy.newQuery(WebsiteData.class,
                "UserData.getByUserName");
            params = new Object[] {userName};
        }
        query.setUnique();
        UserData user = (UserData) query.execute(params);

        // add mapping to cache
        if(user != null) {
            log.debug("userNameToIdMap CACHE MISS - "+userName);
            this.userNameToIdMap.put(user.getUserName(), user.getId());
        }

        return user;
    }

    public List getUsers(WebsiteData weblog, Boolean enabled, Date startDate, 
                         Date endDate, int offset, int length) 
            throws RollerException {
        DatamapperQuery query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }
        
        // if we are doing date range then we must have an end date
        if (startDate != null && endDate == null) {
            endDate = new Date();
        }
        
        if (weblog != null) {
            if (enabled != null) {
                if (startDate != null) {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByPermissions.website&Enabled&EndDate&StartDate");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(new Object[] {weblog, enabled, endDate, startDate});
                } else {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByEnabled&Permissions.website");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(
                        new Object[] {enabled, weblog});
                }
            } else {
                if (startDate != null) {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByPermissions.website&EndDate&StartDate");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(
                        new Object[] {weblog, endDate, startDate});
                } else {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByPermissions.website");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(weblog);
                }
            }
        } else {
            if (enabled != null) {
                if (startDate != null) {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByEnabled&EndDate&StartDate");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(
                        new Object[] {enabled, endDate, startDate});
                } else {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByEnabled");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(new Object[] {enabled});
                }
            } else {
                if (startDate != null) {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getByPermissions.website&EndDate&StartDate");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute(
                        new Object[] {endDate, startDate});
                } else {
                    query = strategy.newQuery(UserData.class, 
                        "UserData.getAll");
                    if (setRange) query.setRange(offset, offset + length);
                    results = (List) query.execute();
                }
            }
        }
        
        return results;
    }

    public List getUsers(int offset, int length) throws RollerException {
        return getUsers(Boolean.TRUE, null, null, offset, length);
    }

    public List getUsers(Boolean enabled, Date startDate, Date endDate, 
            int offset, int length) 
            throws RollerException {
        DatamapperQuery query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;

        if (endDate == null) endDate = new Date();

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        if (enabled != null) {
            if (startDate != null) {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEnabled&EndDate&StartDateOrderByStartDateDesc");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(
                    new Object[] {enabled, endDate, startDate});                
            } else {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEnabled&EndDateOrderByStartDateDesc");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(
                    new Object[] {enabled, endDate});                
              }
        } else {
            if (startDate != null) {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEndDate&StartDateOrderByStartDateDesc");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(
                    new Object[] {endDate, startDate});                
            } else {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEndDateOrderByStartDateDesc");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(endDate);                
            }
        }

        return results;
    }

    /**
     * Get users of a website
     */
    public List getUsers(WebsiteData website, Boolean enabled, int offset, int length) throws RollerException {
        DatamapperQuery query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        if (enabled != null) {
            if (website != null) {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEnabled&Permissions.website");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(new Object[] {enabled, website});                
            } else {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEnabled");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(enabled);                
            }
        } else {
            if (website != null) {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByPermissions.website");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(website);                
            } else {
                query = strategy.newQuery(UserData.class, "UserData.getAll");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute();                
            }
        }
        return results;
    }

    public List getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws RollerException {
        DatamapperQuery query = null;
        List results = null;
        boolean setRange = offset != 0 || length != -1;

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        if (enabled != null) {
            if (startsWith != null) {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEnabled&UserNameOrEmailAddressStartsWith");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(
                    new Object[] {enabled, startsWith + '%'});
            } else {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByEnabled");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(enabled);                
            }
        } else { 
            if (startsWith != null) {
                query = strategy.newQuery(UserData.class, 
                    "UserData.getByUserNameOrEmailAddressStartsWith");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute(startsWith +  '%');
            } else {
                query = strategy.newQuery(UserData.class, "UserData.getAll");
                if (setRange) query.setRange(offset, offset + length);
                results = (List) query.execute();                
            }
        }
        return results;
    }

    public WeblogTemplate getPage(String id) throws RollerException {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) return null;

        return (WeblogTemplate)this.strategy.load(WeblogTemplate.class,id);
    }

    /**
     * Use Datamapper directly because Roller's Query API does too much allocation.
     */
    public WeblogTemplate getPageByLink(WebsiteData website, String pagelink)
            throws RollerException {

        if (website == null)
            throw new RollerException("userName is null");

        if (pagelink == null)
            throw new RollerException("Pagelink is null");

        DatamapperQuery query = strategy.newQuery(
                WeblogTemplate.class, "WeblogTemplate.getByWebsite&Link");
        query.setRange(0, 1);  // => query.setFirstResult(1).setMaxResult(1)
        List list = (List)query.execute(new Object[] {website, pagelink});
        return list.size()!=0 ? (WeblogTemplate)list.get(0) : null;
    }

    /**
     * @see org.apache.roller.model.UserManager#getPageByName(WebsiteData, java.lang.String)
     */
    public WeblogTemplate getPageByName(WebsiteData website, String pagename)
            throws RollerException {

        if (website == null)
            throw new RollerException("website is null");

        if (pagename == null)
            throw new RollerException("Page name is null");

        DatamapperQuery query = strategy.newQuery(
                WeblogTemplate.class, "WeblogTemplate.getByWebsite&Name");
        query.setRange(0, 1);  // => query.setFirstResult(1).setMaxResult(1)
        List list = (List)query.execute(new Object[] {website, pagename}); 
        return list.size()!=0? (WeblogTemplate)list.get(0) : null;
    }

    /**
     * @see org.apache.roller.model.UserManager#getPages(WebsiteData)
     */
    public List getPages(WebsiteData website) throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        return (List) strategy.newQuery(WeblogTemplate.class,
                "WeblogTemplate.getByWebsiteOrderByName").execute(website);
    }

    public PermissionsData getPermissions(String inviteId) 
            throws RollerException {
        return (PermissionsData)this.strategy.load(
            PermissionsData.class, inviteId);
    }

    /**
     * Return permissions for specified user in website
     */
    public PermissionsData getPermissions(
            WebsiteData website, UserData user) throws RollerException {
        List list = (List) strategy.newQuery(PermissionsData.class, 
            "PermissionsData.getByWebsiteAndUser").
                execute(new Object[] {website, user} );
        return list.size()!=0 ? (PermissionsData)list.get(0) : null;
    }

    /**
     * Get pending permissions for user
     */
    public List getPendingPermissions(UserData user) throws RollerException {
        return (List) strategy.newQuery(PermissionsData.class, 
            "PermissionsData.getByUserAndPending")
                .execute(new Object[] {user, Boolean.TRUE} );
    }

    /**
     * Get pending permissions for website
     */
    public List getPendingPermissions(WebsiteData website) throws RollerException {
        return (List) strategy.newQuery(PermissionsData.class, 
            "PermissionsData.getByWebsiteAndPending")
            .execute(new Object[] {website, Boolean.TRUE} );
    }

    /**
     * Get all permissions of a website (pendings not including)
     */
    public List getAllPermissions(WebsiteData website) throws RollerException {
        return (List) strategy.newQuery(PermissionsData.class, 
            "PermissionsData.getByWebsiteAndPending")
            .execute(new Object[] {website, Boolean.FALSE} );
    }

    /**
     * Get all permissions of a user.
     */
    public List getAllPermissions(UserData user) throws RollerException {
        return (List) strategy.newQuery(PermissionsData.class, 
            "PermissionsData.getByUserAndPending")
            .execute(new Object[] {user, Boolean.FALSE} );

    }

    public void release() {}

    public Map getUserNameLetterMap() throws RollerException {
        // TODO: ATLAS getUserNameLetterMap DONE TESTED
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map results = new TreeMap();
        DatamapperQuery query = strategy.newQuery(UserData.class, 
            "UserData.getCountByUserNameLike");
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            List row = (List) query.execute(currentChar + "%");
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }

    public List getUsersByLetter(char letter, int offset, int length)
        throws RollerException {
        // TODO: ATLAS getUsersByLetter DONE
        DatamapperQuery query = strategy.newQuery(UserData.class, 
            "UserData.getByUserNameOrderByUserName");
        query.setRange(offset, offset + length);
        return (List) query.execute(letter + "%");
    }

    public Map getWeblogHandleLetterMap() throws RollerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map results = new TreeMap();
        DatamapperQuery query = strategy.newQuery(WebsiteData.class, 
            "WebsiteData.getCountByHandleLike");
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            List row = (List) query.execute(currentChar + "%");
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }

    public List getWeblogsByLetter(char letter, int offset, int length)
        throws RollerException {
        // TODO: ATLAS getWeblogsByLetter DONE
        DatamapperQuery query = strategy.newQuery(WebsiteData.class, 
            "WebsiteData.getByHandleOrderByHandle");
        if (offset != 0 || length != -1) {
            if (length == -1) {
                length = Integer.MAX_VALUE - offset;
            }            
            query.setRange(offset, offset + length);
        }
        return (List) query.execute(letter + "%");
    }

    public List getMostCommentedWebsites(Date startDate, Date endDate, 
        int offset, int length)
        throws RollerException {
        // TODO: ATLAS getMostCommentedWebsites DONE TESTED

        DatamapperQuery query = null;
        List queryResults = null;
        boolean setRange = offset != 0 || length != -1;

        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        if (endDate == null) endDate = new Date();
        
        if (startDate != null) {
            query = strategy.newQuery(CommentData.class, 
                "CommentData.getMostCommentedWebsiteByEndDate&StartDate");
            if (setRange) query.setRange(offset, offset + length);
            queryResults = (List) query.execute(
                new Object[] {endDate, startDate});            
        } else {
            query = strategy.newQuery(CommentData.class, 
                "CommentData.getMostCommentedWebsiteByEndDate");
            if (setRange) query.setRange(offset, offset + length);
            queryResults = (List) query.execute(endDate);
        }

        // TODO: DatamapperPort - The original query list column not present in group by clause in select clause
        // this is not allowed by JPA spec (and many db vendors).
        // Changed the select clause to match group by clause.
        // Currently, the only caller of this method is SiteModel.getMostCommentedWeblogs() (apart from a junit test)
        // check with roller developers what is the expected behavior of this query 

        List results = new ArrayList();
        for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();
            results.add(new StatCount(
                    (String)row[1],                     // website id
                    (String)row[2],                     // website handle
                    (String)row[3],                     // website name
                    "statCount.weblogCommentCountType", // stat type 
                    ((Long)row[0]).longValue())); // # comments
        }
        //TODO Uncomment following once integrated with code
        //Collections.sort(results, StatCount.getComparator());
        Collections.reverse(results);
        return results;
    }

    /**
     * Get count of weblogs, active and inactive
     */    
    public long getWeblogCount() throws RollerException {
        long ret = 0;
        List results = (List) strategy.newQuery(WebsiteData.class, 
            "WebsiteData.getCountAllDistinct").execute();

        ret = ((Long)results.get(0)).longValue();
        
        return ret;
    }

    
    /**
     * Get count of users, enabled only
     */    
    public long getUserCount() throws RollerException {
        long ret = 0;
        List results = (List) strategy.newQuery(UserData.class, 
            "UserData.getCountEnabledDistinct").execute(Boolean.TRUE);

        ret =((Long)results.get(0)).longValue();

        return ret;
    }

    /**
     * Returns a manged copy of given persistent object.
     * @param obj Given persistent object
     * @return Managed version of obj
     * @throws RollerException
     * The method is required to handle case when a newly created object is made
     * to refer to a object stored in web session. The object stored in web session
     * should be merged into current persistence context before it is being
     * referred by any other object.
     * It should be our goal to not require this method. All the caller of this
     * method should be modfied to see make sure that the obj passed managed
     */
    private PersistentObject getManagedObject(PersistentObject obj) throws RollerException {
        return (PersistentObject) strategy.load(obj.getClass(), obj.getId());
    }
    
}
