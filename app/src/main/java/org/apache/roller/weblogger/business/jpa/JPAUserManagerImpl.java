
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
package org.apache.roller.weblogger.business.jpa;

import java.sql.Timestamp;
import javax.persistence.NoResultException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.TypedQuery;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.RollerPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;


@com.google.inject.Singleton
public class JPAUserManagerImpl implements UserManager {
    private static Log log = LogFactory.getLog(JPAUserManagerImpl.class);

    private final JPAPersistenceStrategy strategy;
    
    // cached mapping of userNames -> userIds
    private Map<String, String> userNameToIdMap = Collections.synchronizedMap(new HashMap<String, String>());
    

    @com.google.inject.Inject
    protected JPAUserManagerImpl(JPAPersistenceStrategy strat) {
        log.debug("Instantiating JPA User Manager");
        this.strategy = strat;
    }


    public void release() {}
    
    
    //--------------------------------------------------------------- user CRUD
 
    public void saveUser(User data) throws WebloggerException {
        this.strategy.store(data);
    }

    
    public void removeUser(User user) throws WebloggerException {
        String userName = user.getUserName();
        
        // remove permissions, maintaining both sides of relationship
        List<WeblogPermission> perms = getWeblogPermissions(user);
        for (WeblogPermission perm : perms) {
            this.strategy.remove(perm);
        }
        this.strategy.remove(user);

        // remove entry from cache mapping
        this.userNameToIdMap.remove(userName);
    }

    
    public void addUser(User newUser) throws WebloggerException {

        if (newUser == null) {
            throw new WebloggerException("cannot add null user");
        }
        
        boolean adminUser = false;
        List existingUsers = this.getUsers(Boolean.TRUE, null, null, 0, 1);
        boolean firstUserAdmin = WebloggerConfig.getBooleanProperty("users.firstUserAdmin");
        if (existingUsers.size() == 0 && firstUserAdmin) {
            // Make first user an admin
            adminUser = true;

            //if user was disabled (because of activation user 
            // account with e-mail property), enable it for admin user
            newUser.setEnabled(Boolean.TRUE);
            newUser.setActivationCode(null);
        }

        if (getUserByUserName(newUser.getUserName()) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new WebloggerException("error.add.user.userNameInUse");
        }

        this.strategy.store(newUser);

        grantRole("editor", newUser);
        if (adminUser) {
            grantRole("admin", newUser);
        }
    }

    @Override
    public User getUser(String id) throws WebloggerException {
        return (User)this.strategy.load(User.class, id);
    }

    //------------------------------------------------------------ user queries

    public User getUserByUserName(String userName) throws WebloggerException {
        return getUserByUserName(userName, Boolean.TRUE);
    }

    @Override
    public User getUserByOpenIdUrl(String openIdUrl) throws WebloggerException {
        if (openIdUrl == null) {
            throw new WebloggerException("OpenID URL cannot be null");
        }

        TypedQuery<User> query;
        User user;
        query = strategy.getNamedQuery(
                "User.getByOpenIdUrl", User.class);
        query.setParameter(1, openIdUrl);
        try {
            user = query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }
        return user;
    }


    public User getUserByUserName(String userName, Boolean enabled)
            throws WebloggerException {

        if (userName==null) {
            throw new WebloggerException("userName cannot be null");
        }
        
        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {

            User user = this.getUser(
                    this.userNameToIdMap.get(userName));
            if (user != null) {
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
            log.debug("userNameToIdMap CACHE MISS - " + userName);
            this.userNameToIdMap.put(user.getUserName(), user.getId());
        }

        return user;
    }

    public List<User> getUsers(Boolean enabled, Date startDate, Date endDate,
            int offset, int length)
            throws WebloggerException {
        TypedQuery<User> query;

        Timestamp end = new Timestamp(endDate != null ? endDate.getTime() : new Date().getTime());

        if (enabled != null) {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "User.getByEnabled&EndDate&StartDateOrderByStartDateDesc", User.class);
                query.setParameter(1, enabled);
                query.setParameter(2, end);
                query.setParameter(3, start);
            } else {
                query = strategy.getNamedQuery(
                        "User.getByEnabled&EndDateOrderByStartDateDesc", User.class);
                query.setParameter(1, enabled);
                query.setParameter(2, end);
            }
        } else {
            if (startDate != null) {
                Timestamp start = new Timestamp(startDate.getTime());
                query = strategy.getNamedQuery(
                        "User.getByEndDate&StartDateOrderByStartDateDesc", User.class);
                query.setParameter(1, end);
                query.setParameter(2, start);
            } else {
                query = strategy.getNamedQuery(
                        "User.getByEndDateOrderByStartDateDesc", User.class);
                query.setParameter(1, end);
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

    public List<User> getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws WebloggerException {
        TypedQuery<User> query;

        if (enabled != null) {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "User.getByEnabled&UserNameOrEmailAddressStartsWith", User.class);
                query.setParameter(1, enabled);
                query.setParameter(2, startsWith + '%');
                query.setParameter(3, startsWith + '%');
            } else {
                query = strategy.getNamedQuery(
                        "User.getByEnabled", User.class);
                query.setParameter(1, enabled);
            }
        } else {
            if (startsWith != null) {
                query = strategy.getNamedQuery(
                        "User.getByUserNameOrEmailAddressStartsWith", User.class);
                query.setParameter(1, startsWith +  '%');
            } else {
                query = strategy.getNamedQuery("User.getAll", User.class);
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

    
    public Map<String, Long> getUserNameLetterMap() throws WebloggerException {
        String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<String, Long> results = new TreeMap<String, Long>();
        TypedQuery<Long> query = strategy.getNamedQuery(
                "User.getCountByUserNameLike", Long.class);
        for (int i=0; i<26; i++) {
            char currentChar = lc.charAt(i);
            query.setParameter(1, currentChar + "%");
            List row = query.getResultList();
            Long count = (Long) row.get(0);
            results.put(String.valueOf(currentChar), count);
        }
        return results;
    }

    
    public List<User> getUsersByLetter(char letter, int offset, int length)
            throws WebloggerException {
        TypedQuery<User> query = strategy.getNamedQuery(
                "User.getByUserNameOrderByUserName", User.class);
        query.setParameter(1, letter + "%");
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }

    
    /**
     * Get count of users, enabled only
     */
    public long getUserCount() throws WebloggerException {
        TypedQuery<Long> q = strategy.getNamedQuery("User.getCountEnabledDistinct", Long.class);
        q.setParameter(1, Boolean.TRUE);
        List<Long> results = q.getResultList();
        return results.get(0);
    }

    public User getUserByActivationCode(String activationCode) throws WebloggerException {
        if (activationCode == null) {
            throw new WebloggerException("activationcode is null");
        }
        TypedQuery<User> q = strategy.getNamedQuery("User.getUserByActivationCode", User.class);
        q.setParameter(1, activationCode);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    
    //-------------------------------------------------------- permissions CRUD
 
    public boolean checkPermission(RollerPermission perm, User user) throws WebloggerException {

        // if permission a weblog permission
        if (perm instanceof WeblogPermission) {
            // if user has specified permission in weblog return true
            WeblogPermission permToCheck = (WeblogPermission)perm;
            try {
                RollerPermission existingPerm = getWeblogPermission(permToCheck.getWeblog(), user);
                if (existingPerm != null && existingPerm.implies(perm)) {
                    return true;
                }
            } catch (WebloggerException ignored) {
            }
        }

        // if Blog Server admin would still have weblog permission above
        GlobalPermission globalPerm = new GlobalPermission(user);
        if (globalPerm.implies(perm)) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("PERM CHECK FAILED: user " + user.getUserName() + " does not have " + perm.toString());
        }
        return false;
    }

    
    public WeblogPermission getWeblogPermission(Weblog weblog, User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogId"
                , WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public WeblogPermission getWeblogPermissionIncludingPending(Weblog weblog, User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public void grantWeblogPermission(Weblog weblog, User user, List<String> actions) throws WebloggerException {

        // first, see if user already has a permission for the specified object
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        WeblogPermission existingPerm = null;
        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // permission already exists, so add any actions specified in perm argument
        if (existingPerm != null) {
            existingPerm.addActions(actions);
            this.strategy.store(existingPerm);
        } else {
            // it's a new permission, so store it
            WeblogPermission perm = new WeblogPermission(weblog, user, actions);
            this.strategy.store(perm);
        }
    }

    
    public void grantWeblogPermissionPending(Weblog weblog, User user, List<String> actions) throws WebloggerException {

        // first, see if user already has a permission for the specified object
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        WeblogPermission existingPerm = null;
        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // permission already exists, so complain 
        if (existingPerm != null) {
            throw new WebloggerException("Cannot make existing permission into pending permission");

        } else {
            // it's a new permission, so store it
            WeblogPermission perm = new WeblogPermission(weblog, user, actions);
            perm.setPending(true);
            this.strategy.store(perm);
        }
    }

    
    public void confirmWeblogPermission(Weblog weblog, User user) throws WebloggerException {

        // get specified permission
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        WeblogPermission existingPerm;
        try {
            existingPerm = q.getSingleResult();

        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }
        // set pending to false
        existingPerm.setPending(false);
        this.strategy.store(existingPerm);
    }

    
    public void declineWeblogPermission(Weblog weblog, User user) throws WebloggerException {

        // get specified permission
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        WeblogPermission existingPerm;
        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }
        // remove permission
        this.strategy.remove(existingPerm);
    }

    
    public void revokeWeblogPermission(Weblog weblog, User user, List<String> actions) throws WebloggerException {

        // get specified permission
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&WeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getHandle());
        WeblogPermission oldperm;
        try {
            oldperm = q.getSingleResult();
        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }

        // remove actions specified in perm argument
        oldperm.removeActions(actions);

        if (oldperm.isEmpty()) {
            // no actions left in permission so remove it
            this.strategy.remove(oldperm);
        } else {
            // otherwise save it
            this.strategy.store(oldperm);
        }
    }

    
    public List<WeblogPermission> getWeblogPermissions(User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public List<WeblogPermission> getWeblogPermissions(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByWeblogId",
                WeblogPermission.class);
        q.setParameter(1, weblog.getHandle());
        return q.getResultList();
    }

    public List<WeblogPermission> getWeblogPermissionsIncludingPending(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByWeblogIdIncludingPending",
                WeblogPermission.class);
        q.setParameter(1, weblog.getHandle());
        return q.getResultList();
    }

    public List<WeblogPermission> getPendingWeblogPermissions(User user) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByUserName&Pending",
                WeblogPermission.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public List<WeblogPermission> getPendingWeblogPermissions(Weblog weblog) throws WebloggerException {
        TypedQuery<WeblogPermission> q = strategy.getNamedQuery("WeblogPermission.getByWeblogId&Pending",
                WeblogPermission.class);
        q.setParameter(1, weblog.getHandle());
        return q.getResultList();
    }

//-------------------------------------------------------------- role CRUD
 
    
    /**
     * Returns true if user has role specified.
     */
    public boolean hasRole(String roleName, User user) throws WebloggerException {
        TypedQuery<UserRole> q = strategy.getNamedQuery("UserRole.getByUserNameAndRole", UserRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, roleName);
        try {
            q.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    
    /**
     * Get all of user's roles.
     */
    public List<String> getRoles(User user) throws WebloggerException {
        TypedQuery<UserRole> q = strategy.getNamedQuery("UserRole.getByUserName", UserRole.class);
        q.setParameter(1, user.getUserName());
        List<UserRole> roles = q.getResultList();
        List<String> roleNames = new ArrayList<String>();
        if (roles != null) {
            for (UserRole userRole : roles) {
                roleNames.add(userRole.getRole());
            }
        }
        return roleNames;
    }

    /**
     * Grant to user role specified by role name.
     */
    public void grantRole(String roleName, User user) throws WebloggerException {
        if (!hasRole(roleName, user)) {
            UserRole role = new UserRole(user.getUserName(), roleName);
            this.strategy.store(role);
        }
    }

    
    public void revokeRole(String roleName, User user) throws WebloggerException {
        TypedQuery<UserRole> q = strategy.getNamedQuery("UserRole.getByUserNameAndRole", UserRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, roleName);
        try {
            UserRole role = q.getSingleResult();
            this.strategy.remove(role);

        } catch (NoResultException e) {
            throw new WebloggerException("ERROR: removing role", e);
        }
    }
}
