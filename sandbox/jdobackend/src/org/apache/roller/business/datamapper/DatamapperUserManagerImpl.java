/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.business.datamapper;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.PingQueueEntryData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;

/*
 * DatamapperUserManagerImpl.java
 *
 * Created on May 29, 2006, 3:15 PM
 *
 */
public class DatamapperUserManagerImpl implements UserManager {
    
    private DatamapperPersistenceStrategy strategy;
    
    /** The logger instance for this class. */
    private static Log logger = LogFactory
            .getFactory().getInstance(DatamapperPingTargetManagerImpl.class);

    /** Creates a new instance of DatamapperPropertiesManagerImpl */
    public DatamapperUserManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void addUser(UserData newUser) 
            throws RollerException {
    }

    public void saveUser(UserData data) 
           throws RollerException {
        strategy.store(data);
    }

    public void removeUser(UserData user) 
            throws RollerException {
        strategy.remove(user);
    }

    public UserData getUser(String id) 
            throws RollerException {
        return (UserData)strategy.load(UserData.class, id);
    }

    /**
     * Get user object by user name (only enabled users)
     */
    public UserData getUserByUserName(String userName) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get user object by user name, optionally include dis-enabled users
     */
    public UserData getUserByUserName(String userName, Boolean enabled)
            throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get all enabled users
     */
    public List getUsers(int offset, int length) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get all users, optionally include dis-enabled users.
     * @param enabled True for enabled only, false for disabled only, null for
     * all
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     */
    public List getUsers(Boolean enabled, Date startDate, Date endDate,
            int offset, int length) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get all users or a website.
     * @param website Get all users of this website (or null for all)
     * @returns List of UserData objects.
     */
    public List getUsers(WebsiteData website, Boolean enabled, int offset,
            int length) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns users whose usernames or email addresses start with a string.
     * @param startsWith String to match userNames and emailAddresses against
     * @param offset Offset into results (for paging)
     * @param length Max to return (for paging)
     * @param enabled True for only enalbed, false for disabled, null for all
     * @return List of (up to length) users that match startsWith string
     */
    public List getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get map with 26 entries, one for each letter A-Z and containing integers
     * reflecting the number of users whose names start with each letter.
     */
    public Map getUserNameLetterMap() throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get collection of users whose names begin with specified letter
     */
    public List getUsersByLetter(char letter, int offset, int length)
            throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get map with 26 entries, one for each letter A-Z and containing integers
     * reflecting the number of weblogs whose names start with each letter.
     */
    public Map getWeblogHandleLetterMap() throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get collection of weblogs whose handles begin with specified letter
     */
    public List getWeblogsByLetter(char letter, int offset, int length)
            throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public UserData getUserByUsername(String userName) 
            throws RollerException {
        return getUserByUsername(userName, Boolean.TRUE);
    }

    public UserData getUserByUsername(String userName, Boolean enabled) 
            throws RollerException {
        if (userName==null )
            throw new RollerException("userName cannot be null");
        if (enabled == null) {
            return (UserData)strategy.newQuery(UserData.class,
                    "getUniqueByName")
                .execute(userName);
        } else {
            return (UserData)strategy.newQuery(UserData.class,
                    "getUniqueByName&&Enabled")
                .execute(new Object[]{userName, enabled});
        }
    }

    public List getUsers() 
            throws RollerException {
        return getUsers(Boolean.TRUE);
    }

    public List getUsers(Boolean enabled) 
            throws RollerException {
        if (enabled == null) {
            return (List)strategy.newQuery(UserData.class,
                    "all")
                .execute();
        } else {
            return (List)strategy.newQuery(UserData.class,
                    "getByEnabled")
                .execute(enabled);
        }
    }

    public List getUsers(WebsiteData website, Boolean enabled) 
            throws RollerException {
        return (List)strategy.newQuery(UserData.class,
                "getByWebsite&&Enabled")
            .execute(new Object[]{website, enabled});
    }

    public List getUsersStartingWith
            (String startsWith, int offset, int length, Boolean enabled) 
            throws RollerException {
        return (List)strategy.newQuery(UserData.class,
                "getByEnabled&&UserNameStartsWith||EmailStartsWith.range")
            .setRange(offset, offset+length)
            .execute(new Object[]{enabled, startsWith});
    }

    public void addWebsite(WebsiteData newWebsite) 
            throws RollerException {
        strategy.store(newWebsite);
        
        // grant weblog creator ADMIN permissions
        PermissionsData perms = new PermissionsData();
        perms.setUser(newWebsite.getCreator());
        perms.setWebsite(newWebsite);
        perms.setPending(false);
        perms.setPermissionMask(PermissionsData.ADMIN);
        strategy.store(perms);
        
        // add default categories
        WeblogCategoryData rootCat = new WeblogCategoryData(
                null,      // id
                newWebsite, // newWeblog
                null,      // parent
                "root",    // name
                "root",    // description
                null );    // image
        strategy.store(rootCat);
        
        String cats = getProperty("newuser.categories");
        WeblogCategoryData firstCat = rootCat; 
        if (cats != null) {
            String[] splitcats = cats.split(",");
            for (int i = 0; i < splitcats.length; i++) {
                WeblogCategoryData c = new WeblogCategoryData(
                    null,            // id
                    newWebsite,      // newWebsite
                    rootCat,         // parent
                    splitcats[i],    // name
                    splitcats[i],    // description
                    null );          // image
                if (i == 0) firstCat = c;
                strategy.store(c);
            }
        }
        // Use first category as default for Blogger API
        newWebsite.setBloggerCategory(firstCat);
        
        // But default category for weblog itself should be  root
        newWebsite.setDefaultCategory(rootCat);

        strategy.store(newWebsite);
        
        // add default bookmarks
        FolderData root = new FolderData(null, "root", "root", newWebsite);
        strategy.store(root);
        
        Integer zero = new Integer(0);
        String blogroll = getProperty("newuser.blogroll");
        if (blogroll != null) {
            String[] splitroll = blogroll.split(",");
            for (int i = 0; i < splitroll.length; i++) {
                String[] rollitems = splitroll[i].split("\\|");
                if (rollitems != null && rollitems.length > 1) {
                    strategy.store(new BookmarkData(
                        root,                // parent
                        rollitems[0],        // name
                        "",                  // description
                        rollitems[1].trim(), // url
                        null,                // feedurl
                        zero,                // weight
                        zero,                // priority
                        null));              // image
                }
            }
        }
        
        // add any auto enabled ping targets
        Collection pingTargets = (Collection)strategy.newQuery(
                PingTargetData.class,
                "getByWebsiteNull&&AutoPingTrue")
            .execute();
        Iterator pingTargetIterator = pingTargets.iterator();
        PingTargetData pingTarget = null;
        while(pingTargetIterator.hasNext()) {
             pingTarget = (PingTargetData)pingTargetIterator.next();
            strategy.store(new AutoPingData(null, pingTarget, newWebsite));
        }
    }

    public void saveWebsite(WebsiteData data) 
            throws RollerException {
        strategy.store(data);
    }

    public void removeWebsite(WebsiteData website) 
            throws RollerException {

        // Remove the website's ping queue entries
        strategy.newQuery(PingQueueEntryData.class,
                "getByWebsite")
            .execute(website);
        
        // Remove the website's auto ping configurations
        strategy.newQuery(AutoPingData.class,
                "getByWebsite")
            .execute(website);
        
        // Remove the website's custom ping targets
        strategy.newQuery(PingTargetData.class,
                "getByWebsite")
            .execute(website);
        
        // Remove the website's weblog entries
        strategy.newQuery(WeblogEntryData.class,
                "getByWebsite")
            .execute(website);
        
        // Remove the website's associated referrers
        strategy.newQuery(RefererData.class,
                "getByWebsite")
            .execute(website);
                
        // Remove the website's associated templates
        strategy.newQuery(WeblogTemplate.class,
                "getByWebsite")
            .execute(website);

        // Remove all the website's bookmarks in all folders
        strategy.newQuery(BookmarkData.class,
                "getByFolderByWebsite")
            .execute(website);
        
        // Remove all the website's folders
        strategy.newQuery(FolderData.class,
                "getByWebsite")
            .execute(website);
                
        // Remove the website's categories
        strategy.newQuery(WeblogCategoryData.class,
                "getByWebsite")
            .execute(website);

        // Finally, remove the website
        strategy.remove(website);
    }

    public WebsiteData getWebsite(String id) 
            throws RollerException {
        return (WebsiteData)strategy.load(WebsiteData.class, id);
    }

    public WebsiteData getWebsiteByHandle(String handle) 
            throws RollerException {
        return getWebsiteByHandle(handle, Boolean.TRUE);
    }

    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled) 
            throws RollerException {
        // XXX cache websites by handle?
        return (WebsiteData)strategy.newQuery(WebsiteData.class,
                "getByHandle&&Enabled")
            .execute(new Object[]{handle, enabled});
    }

    public List getWebsites(UserData user, Boolean enabled, Boolean active) 
            throws RollerException {
        return (List)strategy.newQuery(WebsiteData.class,
                "getByPermissionsContainsUser&&Enabled&&Active")
            .execute(new Object[]{user, enabled, active});
    }

    /**
     * Get websites optionally restricted by user, enabled and active status.
     * @param user    Get all websites for this user (or null for all)
     * @param offset  Offset into results (for paging)
     * @param len     Maximum number of results to return (for paging)
     * @param enabled Get all with this enabled state (or null or all)
     * @param active  Get all with this active state (or null or all)
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @returns List of WebsiteData objects.
     */
    public List getWebsites(
            UserData user, 
            Boolean  enabled, 
            Boolean  active, 
            Date     startDate, 
            Date     endDate, 
            int      offset, 
            int      length) 
            throws RollerException {
        return null;
    }
    
    /**
     * Get websites ordered by descending number of comments.
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging)
     * @return List of WebsiteData objects.
     */
    public List getMostCommentedWebsites(
            Date startDate, 
            Date endDate, 
            int  offset, 
            int  length) 
            throws RollerException {
        return null;
    }
    
    public void savePermissions(PermissionsData perms) 
            throws RollerException {
        strategy.store(perms);
    }

    public void removePermissions(PermissionsData perms) 
            throws RollerException {
        strategy.remove(perms);
    }

    public PermissionsData getPermissions(String id) 
            throws RollerException {
        return (PermissionsData)strategy.load(PermissionsData.class, id);
    }

    public List getPendingPermissions(UserData user) 
            throws RollerException {
        return (List)strategy.newQuery(PermissionsData.class,
                "getByUser&&Pending")
            .execute(user);
    }

    public List getPendingPermissions(WebsiteData website) 
            throws RollerException {
        return (List)strategy.newQuery(PermissionsData.class,
                "getByWebsite&&Pending")
            .execute(website);
    }

    public PermissionsData getPermissions(WebsiteData website, UserData user) 
            throws RollerException {
        return (PermissionsData)strategy.newQuery(PermissionsData.class,
                "getUniqueByWebsite&&User")
            .execute(new Object[]{website, user});
    }

    public List getAllPermissions(WebsiteData website) 
            throws RollerException {
        return (List)strategy.newQuery(PermissionsData.class,
                "getByWebsite&&NotPending")
            .execute(website);
    }

    public List getAllPermissions(UserData user) 
            throws RollerException {
        return (List)strategy.newQuery(PermissionsData.class,
                "getByUser&&NotPending")
            .execute(user);
    }

    public PermissionsData inviteUser
            (WebsiteData website, UserData user, short mask) 
            throws RollerException {
        if (website == null) 
            throw new RollerException("inviteUser: Website cannot be null");
        if (user == null) 
            throw new RollerException("inviteUser: User cannot be null");

        PermissionsData perms = new PermissionsData();
        perms.setWebsite(website);
        perms.setUser(user);
        perms.setPermissionMask(mask);
        strategy.store(perms);
        
        return perms;
    }

    public void retireUser(WebsiteData website, UserData user) 
            throws RollerException {
        if (website == null) 
            throw new RollerException("retireUser: Website cannot be null");
        if (user == null) 
            throw new RollerException("retireUser: User cannot be null");

        strategy.newQuery(PermissionsData.class,
                "getByWebsite&&UserId")
            .execute(new Object[]{website, user});
    }

    public void savePage(WeblogTemplate data) 
            throws RollerException {
        strategy.store(data);
        saveWebsite(data.getWebsite());
    }

    public void removePage(WeblogTemplate page) 
            throws RollerException {
        strategy.remove(page);
    }

    public WeblogTemplate getPage(String id) 
            throws RollerException {
        // For templates stored on disk, just return
        if (id != null && id.endsWith(".vm")) 
            return null;
        return (WeblogTemplate)strategy.load(WeblogTemplate.class, id);
    }

    public WeblogTemplate getPageByName(WebsiteData website, String pageName) 
            throws RollerException {
        if (website == null)
            throw new RollerException(
                    "getPageByName: website cannot be null");
        if (pageName == null)
            throw new RollerException(
                    "getPageByName: Page name cannot be null");
        return (WeblogTemplate)strategy.newQuery(WeblogTemplate.class,
                "getUniqueByWebsite&&Name")
            .execute(new Object[]{website, pageName});
    }

    public WeblogTemplate getPageByLink(WebsiteData website, String pageLink) 
            throws RollerException {
        if (website == null)
            throw new RollerException(
                    "getPageByLink: website cannot be null");
        if (pageLink == null)
            throw new RollerException(
                    "getPageByLink: Page link cannot be null");
        return (WeblogTemplate)strategy.newQuery(WeblogTemplate.class,
                "getUniqueByWebsite&&Link")
            .execute(new Object[]{website, pageLink});
    }

    public List getPages(WebsiteData website) 
            throws RollerException {
        return (List)strategy.newQuery(WeblogTemplate.class,
                "getByWebsite.orderByName")
            .execute(website);
    }

    public void release() {
    }

    private String getProperty(String property) throws RollerException {
        return RollerFactory.getRoller().getPropertiesManager()
            .getProperty(property).getValue();
    }

}
