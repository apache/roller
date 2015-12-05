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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JPAUserManagerImpl implements UserManager {
    private static Log log = LogFactory.getLog(JPAUserManagerImpl.class);

    private final JPAPersistenceStrategy strategy;
    
    // cached mapping of userNames -> userIds
    private Map<String, String> userNameToIdMap = new HashMap<String, String>();
    
    protected JPAUserManagerImpl(JPAPersistenceStrategy strat) {
        log.debug("Instantiating JPA User Manager");
        this.strategy = strat;
    }

    //--------------------------------------------------------------- user CRUD
 
    public void saveUser(User data) throws WebloggerException {
        this.strategy.store(data);
    }

    
    public void removeUser(User user) throws WebloggerException {
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

    
    public void addUser(User newUser) throws WebloggerException {
        if (newUser == null) {
            throw new WebloggerException("cannot add null user");
        }
        
        List existingUsers = this.getUsers(Boolean.TRUE, null, null, 0, 1);
        boolean firstUserAdmin = WebloggerConfig.getBooleanProperty("users.firstUserAdmin");

        if (existingUsers.size() == 0 && firstUserAdmin) {
            // Make first user an admin
            newUser.setGlobalRole(GlobalRole.ADMIN);

            //if user was disabled (because of activation user 
            // account with e-mail property), enable it for admin user
            newUser.setEnabled(Boolean.TRUE);
            newUser.setActivationCode(null);
        } else {
            newUser.setGlobalRole(GlobalRole.BLOGGER);
        }

        if (getUserByUserName(newUser.getUserName()) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new WebloggerException("error.add.user.userNameInUse");
        }

        this.strategy.store(newUser);
    }

    @Override
    public User getUser(String id) throws WebloggerException {
        return this.strategy.load(User.class, id);
    }

    //------------------------------------------------------------ user queries

    public User getUserByUserName(String userName) throws WebloggerException {
        return getUserByUserName(userName, Boolean.TRUE);
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
 
    public boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role) throws WebloggerException {

        // if user has specified permission in weblog return true
        try {
            UserWeblogRole existingPerm = getWeblogRole(user, weblog);
            if (existingPerm != null && existingPerm.hasEffectiveWeblogRole(role)) {
                return true;
            }
        } catch (WebloggerException ignored) {
        }

        // if Blog Server admin would still have weblog permission above
        if (user.isGlobalAdmin()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("PERM CHECK FAILED: user " + user.getUserName() + " does not have " + role.name()
                    + " or greater rights on weblog " + weblog.getHandle());
        }
        return false;
    }

    
    public UserWeblogRole getWeblogRole(User user, Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogId"
                , UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        try {
            return q.getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    public void grantWeblogRole(User user, Weblog weblog, WeblogRole role) throws WebloggerException {

        // first, see if user already has a permission for the specified object
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm = null;
        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // permission already exists, so add any actions specified in perm argument
        if (existingPerm != null) {
            existingPerm.setWeblogRole(role);
            existingPerm.setPending(false);
            this.strategy.store(existingPerm);
        } else {
            // it's a new permission, so store it
            UserWeblogRole perm = new UserWeblogRole(weblog, user, role);
            this.strategy.store(perm);
        }
    }

    @Override
    public void grantPendingWeblogRole(User user, Weblog weblog, WeblogRole role) throws WebloggerException {

        // first, see if user already has a permission for the specified object
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm = null;
        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {}

        // permission already exists, so complain 
        if (existingPerm != null) {
            throw new WebloggerException("User already has permissions for this weblog.");
        } else {
            // it's a new permission, so store it
            UserWeblogRole perm = new UserWeblogRole(weblog, user, role);
            perm.setPending(true);
            this.strategy.store(perm);
        }
    }

    
    public void acceptWeblogInvitation(User user, Weblog weblog) throws WebloggerException {

        // get specified permission
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm;
        try {
            existingPerm = q.getSingleResult();

        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }
        // set pending to false
        existingPerm.setPending(false);
        this.strategy.store(existingPerm);
    }

    
    public void declineWeblogInvitation(User user, Weblog weblog) throws WebloggerException {

        // get specified permission
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        UserWeblogRole existingPerm;
        try {
            existingPerm = q.getSingleResult();
        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }
        // remove permission
        this.strategy.remove(existingPerm);
    }

    @Override
    public void revokeWeblogRole(User user, Weblog weblog) throws WebloggerException {
        // get specified permission
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        q.setParameter(2, weblog.getId());
        UserWeblogRole oldperm;
        try {
            oldperm = q.getSingleResult();
        } catch (NoResultException ignored) {
            throw new WebloggerException("ERROR: permission not found");
        }

        this.strategy.remove(oldperm);
    }

    
    public List<UserWeblogRole> getWeblogRoles(User user) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public List<UserWeblogRole> getWeblogRolesIncludingPending(User user) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserNameIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public List<UserWeblogRole> getWeblogRoles(Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogId",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    public List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    public List<UserWeblogRole> getPendingWeblogRoles(User user) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserName&Pending",
                UserWeblogRole.class);
        q.setParameter(1, user.getUserName());
        return q.getResultList();
    }

    public List<UserWeblogRole> getPendingWeblogRoles(Weblog weblog) throws WebloggerException {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByWeblogId&Pending",
                UserWeblogRole.class);
        q.setParameter(1, weblog.getId());
        return q.getResultList();
    }

    @Override
    public GlobalRole getGlobalRole(User user) throws WebloggerException {
        TypedQuery<GlobalRole> q = strategy.getNamedQuery("User.getGlobalRole", GlobalRole.class);
        q.setParameter(1, user.getUserName());
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            // TODO: Log something.
            return GlobalRole.LOGIN;
        }
    }

    @Override
    public boolean isGlobalAdmin(User user) throws WebloggerException {
        return getGlobalRole(user) == GlobalRole.ADMIN;
    }

    @Override
    public boolean hasEffectiveGlobalRole(User user, GlobalRole roleToCheck) throws WebloggerException {
        return getGlobalRole(user).getWeight() >= roleToCheck.getWeight();
    }

}
