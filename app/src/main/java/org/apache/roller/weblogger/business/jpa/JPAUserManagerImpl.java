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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.jpa;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.menu.Menu;
import org.apache.roller.weblogger.ui.core.menu.MenuHelper;
import org.apache.roller.weblogger.util.cache.ExpiringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPAUserManagerImpl implements UserManager {

    private static Logger log = LoggerFactory.getLogger(JPAUserManagerImpl.class);

    private final JPAPersistenceStrategy strategy;

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private MenuHelper menuHelper;

    public void setMenuHelper(MenuHelper menuHelper) {
        this.menuHelper = menuHelper;
    }

    private ExpiringCache editorMenuCache = null;

    public void setEditorMenuCache(ExpiringCache editorMenuCache) {
        this.editorMenuCache = editorMenuCache;
    }

    // cached mapping of userNames -> userIds
    private Map<String, String> userNameToIdMap = new HashMap<>();

    // cached mapping of screenNames -> userIds
    private Map<String, String> screenNameToIdMap = new HashMap<>();

    private Boolean makeFirstUserAdmin = true;

    public void setMakeFirstUserAdmin(Boolean makeFirstUserAdmin) {
        this.makeFirstUserAdmin = makeFirstUserAdmin;
    }

    protected JPAUserManagerImpl(JPAPersistenceStrategy strat) {
        this.strategy = strat;
    }

    @Override
    public void removeUser(User user) {
        String userName = user.getUserName();
        
        // remove permissions, maintaining both sides of relationship
        List<UserWeblogRole> perms = getWeblogRolesIncludingPending(user);
        for (UserWeblogRole perm : perms) {
            this.strategy.remove(perm);
        }
        this.strategy.remove(user);

        // remove entry from cache mapping
        this.userNameToIdMap.remove(userName);
    }

    @Override
    public void saveUser(User data) {
        if (data == null) {
            throw new IllegalArgumentException("cannot save null user");
        }

        List existingUsers = this.getUsers(null, Boolean.TRUE, 0, 1);

        if (existingUsers.size() == 0 && makeFirstUserAdmin) {
            // Make first user an admin
            data.setGlobalRole(GlobalRole.ADMIN);

            //if user was disabled (because of activation user
            // account with e-mail property), enable it for admin user
            data.setEnabled(Boolean.TRUE);
            data.setActivationCode(null);
        }

        this.strategy.store(data);
    }

    @Override
    public User getUser(String id) {
        return this.strategy.load(User.class, id);
    }

    @Override
    public SafeUser getSafeUser(String id) {
        return this.strategy.load(SafeUser.class, id);
    }

    @Override
    public User getUserByUserName(String userName) {
        return getUserByUserName(userName, Boolean.TRUE);
    }

    @Override
    public User getUserByUserName(String userName, Boolean enabled) {

        if (userName==null) {
            throw new IllegalArgumentException("userName cannot be null");
        }
        
        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {

            User user = this.getUser(
                    this.userNameToIdMap.get(userName));
            if (user != null) {
                // only return the user if the enabled status matches
                if(enabled == null || enabled.equals(user.getEnabled())) {
                    log.debug("userNameToIdMap CACHE HIT - {}", userName);
                    return user;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.userNameToIdMap.remove(userName);
            }
        }

        // cache failed, do lookup
        TypedQuery<User> query;
        Object[] params;
        if (enabled != null) {
            query = strategy.getNamedQuery(
                    "User.getByUserName&Enabled", User.class);
            params = new Object[] {userName, enabled};
        } else {
            query = strategy.getNamedQuery(
                    "User.getByUserName", User.class);
            params = new Object[] {userName};
        }
        for (int i=0; i<params.length; i++) {
            query.setParameter(i+1, params[i]);
        }
        User user;
        try {
            user = query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }

        // add mapping to cache
        if(user != null) {
            log.debug("userNameToIdMap CACHE MISS - {}", userName);
            this.userNameToIdMap.put(user.getUserName(), user.getId());
        }

        return user;
    }


    @Override
    public User getUserByScreenName(String screenName) {

        if (screenName==null) {
            throw new IllegalArgumentException("screenName cannot be null");
        }

        // check cache first
        if(this.screenNameToIdMap.containsKey(screenName)) {
            User user = this.getUser(this.screenNameToIdMap.get(screenName));
            if (user != null) {
                log.debug("screenNameToIdMap CACHE HIT - {}", screenName);
                return user;
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.screenNameToIdMap.remove(screenName);
            }
        }

        // cache failed, do lookup
        TypedQuery<User> query;
        Object[] params;
        query = strategy.getNamedQuery(
                "User.getByScreenName", User.class);
        params = new Object[] {screenName};

        for (int i=0; i<params.length; i++) {
            query.setParameter(i+1, params[i]);
        }
        User user;
        try {
            user = query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }

        // add mapping to cache
        if(user != null) {
            log.debug("screenNameToIdMap CACHE MISS - {}", screenName);
            this.screenNameToIdMap.put(user.getUserName(), user.getId());
        }

        return user;
    }

    @Override
    public List<SafeUser> getUsers(String startsWith, Boolean enabled, int offset, int length) {
        TypedQuery<SafeUser> query;

        if (enabled != null) {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "SafeUser.getByEnabled&ScreenNameOrEmailAddressStartsWith", SafeUser.class);
                query.setParameter(1, enabled);
                query.setParameter(2, startsWith + '%');
                query.setParameter(3, startsWith + '%');
            } else {
                query = strategy.getNamedQuery("SafeUser.getByEnabled", SafeUser.class);
                query.setParameter(1, enabled);
            }
        } else {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "SafeUser.getByScreenNameOrEmailAddressStartsWith", SafeUser.class);
                query.setParameter(1, startsWith +  '%');
            } else {
                query = strategy.getNamedQuery("SafeUser.getAll", SafeUser.class);
                query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            }
        }
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    @Override
    public long getUserCount() {
        TypedQuery<Long> q = strategy.getNamedQuery("User.getCountEnabledDistinct", Long.class);
        q.setParameter(1, Boolean.TRUE);
        List<Long> results = q.getResultList();
        return results.get(0);
    }

    @Override
    public User getUserByActivationCode(String activationCode) {
        if (activationCode == null) {
            throw new IllegalArgumentException("activationcode is null");
        }
        TypedQuery<User> q = strategy.getNamedQuery("User.getUserByActivationCode", User.class);
        q.setParameter(1, activationCode);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean checkWeblogRole(String username, String weblogHandle, WeblogRole role) {
        User userToCheck = getUserByUserName(username, true);
        Weblog weblogToCheck = weblogManager.getWeblogByHandle(weblogHandle);
        return !(userToCheck == null || weblogToCheck == null) && checkWeblogRole(userToCheck, weblogToCheck, role);
    }

    @Override
    public boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role) {

        // if user has specified permission in weblog return true
        UserWeblogRole existingRole = getWeblogRole(user, weblog);
        if (existingRole != null && existingRole.hasEffectiveWeblogRole(role)) {
            return true;
        }

        // if Blog Server admin would still have any weblog role
        if (user.isGlobalAdmin()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("ROLE CHECK FAILED: user {} does not have {} or greater rights on weblog {}", weblog.getHandle(),
                    user.getUserName(), role.name());
        }
        return false;
    }

    @Override
    public UserWeblogRole getWeblogRole(String username, String weblogHandle) {
        User userToCheck = getUserByUserName(username, true);
        Weblog weblogToCheck = weblogManager.getWeblogByHandle(weblogHandle);
        return getWeblogRole(userToCheck, weblogToCheck);
    }

    @Override
    public UserWeblogRole getWeblogRole(User user, Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogId"
                , UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    @Override
    public UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    @Override
    public void grantWeblogRole(User user, Weblog weblog, WeblogRole role) {

        // first, see if user already has a permission for the specified object
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm = null;

        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // role already exists, so update it
        if (existingPerm != null) {
            existingPerm.setWeblogRole(role);
            existingPerm.setPending(false);
            this.strategy.store(existingPerm);
        } else {
            // it's a new association, so store it
            UserWeblogRole perm = new UserWeblogRole(user, weblog, role);
            this.strategy.store(perm);
        }
        editorMenuCache.remove(generateMenuCacheKey(user.getUserName(), weblog.getHandle()));
    }

    @Override
    public void grantPendingWeblogRole(User user, Weblog weblog, WeblogRole desiredRole) {

        // first, see if user already has a role for the specified weblog
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingRole = null;
        try {
            existingRole = q.getSingleResult();
        } catch (NoResultException ignored) {}

        if (existingRole == null) {
            UserWeblogRole newRole = new UserWeblogRole(user, weblog, desiredRole);
            newRole.setPending(true);
            this.strategy.store(newRole);
        }
    }

    
    @Override
    public void acceptWeblogInvitation(User user, Weblog weblog) {

        // get specified permission
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm;
        try {
            existingPerm = q.getSingleResult();
            existingPerm.setPending(false);
            this.strategy.store(existingPerm);
            editorMenuCache.remove(generateMenuCacheKey(user.getUserName(), weblog.getHandle()));
        } catch (NoResultException ignored) {
            // invitation rescinded
        }
    }

    @Override
    public void declineWeblogInvitation(User user, Weblog weblog) {
        // get specified role
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingRole;
        try {
            existingRole = q.getSingleResult();
            this.strategy.remove(existingRole);
        } catch (NoResultException ignored) {
        }
    }

    @Override
    public void revokeWeblogRole(User user, Weblog weblog) {
        // get specified role
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());
        UserWeblogRole oldrole;
        try {
            oldrole = q.getSingleResult();
            this.strategy.remove(oldrole);
        } catch (NoResultException ignored) {
        }
        editorMenuCache.remove(generateMenuCacheKey(user.getUserName(), weblog.getHandle()));
    }

    
    @Override
    public List<UserWeblogRole> getWeblogRoles(User user) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        return q.getResultList();
    }

    @Override
    public List<UserWeblogRole> getWeblogRolesIncludingPending(User user) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        return q.getResultList();
    }

    @Override
    public List<UserWeblogRole> getWeblogRoles(Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogId",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    @Override
    public List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    @Override
    public List<UserWeblogRole> getPendingWeblogRoles(Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogId&Pending",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    @Override
    public Menu getEditorMenu(String username, String weblogHandle) {
        String cacheKey = generateMenuCacheKey(username, weblogHandle);
        Menu menu = (Menu) editorMenuCache.get(cacheKey);
        if (menu == null) {
            User user = getUserByUserName(username);
            UserWeblogRole uwr = getWeblogRole(username, weblogHandle);
            menu = menuHelper.getMenu("editor", user.getGlobalRole(), uwr == null ? null : uwr.getWeblogRole(), null);
            editorMenuCache.put(cacheKey, menu);
        }
        return menu;
    }

    private String generateMenuCacheKey(String username, String weblogHandle) {
        return "user/" + username + "/handle/" + weblogHandle;
    }

}
