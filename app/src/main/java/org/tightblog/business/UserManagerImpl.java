/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.business;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserCredentials;
import org.tightblog.pojos.UserSearchCriteria;
import org.tightblog.pojos.UserStatus;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("userManager")
public class UserManagerImpl implements UserManager {

    private static Logger log = LoggerFactory.getLogger(UserManagerImpl.class);

    @Autowired
    private JPAPersistenceStrategy strategy;

    @Autowired
    private WeblogManager weblogManager;

    // cached mapping of userNames -> userIds
    private Map<String, String> userNameToIdMap = Collections.synchronizedMap(new HashMap<>());

    protected UserManagerImpl() {
    }

    @Override
    public void removeUser(User user) {
        String userName = user.getUserName();

        // remove roles, maintaining both sides of relationship
        List<UserWeblogRole> roles = getWeblogRolesIncludingPending(user);
        this.strategy.removeAll(roles);
        this.strategy.remove(user);

        // remove entry from cache mapping
        this.userNameToIdMap.remove(userName);
    }

    @Override
    public void saveUser(User data) {
        if (data == null) {
            throw new IllegalArgumentException("cannot save null user");
        }
        strategy.store(data);
        strategy.flush();
    }

    @Override
    public User getUser(String id) {
        return this.strategy.load(User.class, id);
    }

    @Override
    public UserCredentials getCredentialsByUserName(String userName) {
        if (userName == null) {
            throw new IllegalArgumentException("userName cannot be null");
        }

        TypedQuery<UserCredentials> query;
        query = strategy.getNamedQuery("UserCredentials.getByUserName", UserCredentials.class);
        query.setParameter(1, userName);
        query.setHint("javax.persistence.cache.storeMode", "REFRESH");

        UserCredentials creds;
        try {
            creds = query.getSingleResult();
        } catch (NoResultException e) {
            creds = null;
        }
        return creds;
    }

    @Override
    public void updateCredentials(String userId, String newPassword) {
        Query q = strategy.getNamedUpdate("UserCredentials.changePassword");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        q.setParameter(1, encoder.encode(newPassword));
        q.setParameter(2, userId);
        q.executeUpdate();
        strategy.flush();
    }

    @Override
    public User getEnabledUserByUserName(String userName) {

        User enabledUser = null;

        if (!StringUtils.isEmpty(userName)) {
            // check cache first
            if (userNameToIdMap.containsKey(userName)) {
                User tmpUser = getUser(userNameToIdMap.get(userName));
                if (tmpUser != null) {
                    // return the user only if enabled
                    if (UserStatus.ENABLED.equals(tmpUser.getStatus())) {
                        log.trace("userNameToIdMap CACHE HIT - {}", userName);
                        enabledUser = tmpUser;
                    }
                } else {
                    // mapping hit with lookup miss?  mapping must be old, remove it
                    userNameToIdMap.remove(userName);
                }
            }

            // cache failed? do lookup
            if (enabledUser == null) {
                TypedQuery<User> query = strategy.getNamedQuery("User.getByUserName&Enabled", User.class);
                query.setParameter(1, userName);

                try {
                    enabledUser = query.getSingleResult();
                } catch (NoResultException e) {
                    enabledUser = null;
                }

                // add mapping to cache
                if (enabledUser != null) {
                    log.trace("userNameToIdMap CACHE MISS - {}", userName);
                    this.userNameToIdMap.put(enabledUser.getUserName(), enabledUser.getId());
                }
            }
        }

        return enabledUser;
    }

    @Override
    public List<User> getUsers(UserSearchCriteria criteria) {
        List<Object> params = new ArrayList<>();
        int size = 0;
        StringBuilder queryString = new StringBuilder();

        queryString.append("SELECT u FROM User u WHERE 1=1 ");

        if (criteria.getStatus() != null) {
            params.add(size++, criteria.getStatus());
            queryString.append(" and u.status = ?").append(size);
        }

        if (criteria.getActivationCode() != null) {
            params.add(size++, criteria.getActivationCode());
            queryString.append(" and u.activationCode = ?").append(size);
        }

        if (criteria.getScreenName() != null) {
            params.add(size++, criteria.getScreenName());
            queryString.append(" and u.screenName = ?").append(size);
        }

        if (criteria.getUserName() != null) {
            params.add(size++, criteria.getUserName());
            queryString.append(" and u.userName = ?").append(size);
        }

        if (criteria.getGlobalRole() != null) {
            params.add(size++, criteria.getGlobalRole());
            queryString.append(" and u.globalRole = ?").append(size);
        }

        queryString.append(" ORDER BY u.screenName ");

        TypedQuery<User> query = strategy.getDynamicQuery(queryString.toString(), User.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        query.setHint("javax.persistence.cache.storeMode", "REFRESH");

        if (criteria.getOffset() != 0) {
            query.setFirstResult(criteria.getOffset());
        }
        if (criteria.getMaxResults() != null) {
            query.setMaxResults(criteria.getMaxResults());
        }

        return query.getResultList();
    }

    @Override
    public long getUserCount() {
        TypedQuery<Long> q = strategy.getNamedQuery("User.getCountEnabledDistinct", Long.class);
        List<Long> results = q.getResultList();
        return results.get(0);
    }

    @Override
    public boolean checkWeblogRole(String username, String weblogHandle, WeblogRole role) {
        boolean hasRole = false;

        User userToCheck = getEnabledUserByUserName(username);
        if (userToCheck != null) {
            Weblog weblogToCheck = weblogManager.getWeblogByHandle(weblogHandle, null);
            hasRole = weblogToCheck != null && checkWeblogRole(userToCheck, weblogToCheck, role);
        }
        return hasRole;
    }

    @Override
    public boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role) {
        boolean hasRole = false;

        if (user != null & weblog != null) {
            if (GlobalRole.ADMIN.equals(user.getGlobalRole())) {
                hasRole = true;
            } else {
                UserWeblogRole existingRole = getWeblogRole(user, weblog);
                if (existingRole != null && existingRole.hasEffectiveWeblogRole(role)) {
                    hasRole = true;
                }
            }
        }
        return hasRole;
    }

    @Override
    public UserWeblogRole getUserWeblogRole(String id) {
        return this.strategy.load(UserWeblogRole.class, id);
    }

    @Override
    public UserWeblogRole getWeblogRole(User user, Weblog weblog) {
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogId",
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
    public void grantWeblogRole(User user, Weblog weblog, WeblogRole role, boolean pending) {

        // first, see if user already has a permission for the specified object
        TypedQuery<UserWeblogRole> q = strategy.getNamedQuery("UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                UserWeblogRole.class);
        q.setParameter(1, user.getId());
        q.setParameter(2, weblog.getId());

        UserWeblogRole existingRole = null;
        try {
            existingRole = q.getSingleResult();
        } catch (NoResultException ignored) {
        }

        // role already exists, so update it keeping its pending status.
        if (existingRole != null) {
            existingRole.setWeblogRole(role);
            this.strategy.store(existingRole);
        } else {
            // it's a new association, so store it
            UserWeblogRole newRole = new UserWeblogRole(user, weblog, role);
            newRole.setPending(pending);
            this.strategy.store(newRole);
        }
    }

    @Override
    public void acceptWeblogInvitation(UserWeblogRole uwr) {
        // check role is still in DB
        UserWeblogRole existingRole = getUserWeblogRole(uwr.getId());
        if (existingRole != null) {
            existingRole.setPending(false);
            this.strategy.store(existingRole);
        }
    }

    @Override
    public void revokeWeblogRole(UserWeblogRole roleToRevoke) {
        // get specified role
        UserWeblogRole existingRole = getUserWeblogRole(roleToRevoke.getId());
        if (existingRole != null) {
            this.strategy.remove(existingRole);
        }
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
    public String generateMFAQRUrl(User user) {
        String url = "";
        UserCredentials uc = getCredentialsByUserName(user.getUserName());

        if (uc != null) {
            if (uc.getMfaSecret() == null) {
                uc.setMfaSecret(Base32.random());
                strategy.store(uc);
                strategy.flush();
            }
            try {
                url = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl="
                        + URLEncoder.encode(String.format(
                        "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                        "TightBlog", user.getEmailAddress(), uc.getMfaSecret(), "TightBlog"),
                        "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
            }
        }

        return url;
    }

    @Override
    public void eraseMFASecret(String userId) {
        Query q = strategy.getNamedUpdate("UserCredentials.eraseMfaCode");
        q.setParameter(1, userId);
        q.executeUpdate();
        strategy.flush();
    }
}
