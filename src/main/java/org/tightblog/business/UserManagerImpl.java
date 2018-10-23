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

import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserCredentials;
import org.tightblog.pojos.UserSearchCriteria;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.UserWeblogRoleRepository;
import org.tightblog.repository.WeblogRepository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component("userManager")
public class UserManagerImpl implements UserManager {

    private static Logger log = LoggerFactory.getLogger(UserManagerImpl.class);

    private JPAPersistenceStrategy strategy;
    private UserRepository userRepository;
    private UserWeblogRoleRepository userWeblogRoleRepository;
    private WeblogRepository weblogRepository;

    @Autowired
    public UserManagerImpl(JPAPersistenceStrategy strategy, UserRepository userRepository,
                           UserWeblogRoleRepository uwrRepository, WeblogRepository weblogRepository) {
        this.strategy = strategy;
        this.userRepository = userRepository;
        this.weblogRepository = weblogRepository;
        this.userWeblogRoleRepository = uwrRepository;
    }

    @Override
    public void removeUser(User user) {
        userWeblogRoleRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    public void saveUser(User data) {
        if (data == null) {
            throw new IllegalArgumentException("cannot save null user");
        }
        userRepository.saveAndFlush(data);
    }

    @Override
    public User getUser(String id) {
        return userRepository.findByIdOrNull(id);
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
        return userRepository.findByUserName(userName);
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
        return userRepository.count();
    }

    @Override
    public boolean checkWeblogRole(String username, String weblogHandle, WeblogRole role) {
        boolean hasRole = false;

        User userToCheck = getEnabledUserByUserName(username);
        if (userToCheck != null) {
            Weblog weblogToCheck = weblogRepository.findByHandle(weblogHandle);
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
        return userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(user, weblog);
    }

    @Override
    public UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog) {
        return userWeblogRoleRepository.findByUserAndWeblog(user, weblog);
    }

    @Override
    public void grantWeblogRole(User user, Weblog weblog, WeblogRole role, boolean pending) {
        UserWeblogRole roleCheck = userWeblogRoleRepository.findByUserAndWeblog(user, weblog);

        if (roleCheck != null) {
            roleCheck.setWeblogRole(role);
        } else {
            roleCheck = new UserWeblogRole(user, weblog, role);
        }
        roleCheck.setPending(pending);
        userWeblogRoleRepository.saveAndFlush(roleCheck);
    }

    @Override
    public void acceptWeblogInvitation(UserWeblogRole uwr) {
        // check role is still in DB
        UserWeblogRole existingRole = getUserWeblogRole(uwr.getId());
        if (existingRole != null) {
            existingRole.setPending(false);
            userWeblogRoleRepository.saveAndFlush(existingRole);
        }
    }

    @Override
    public void revokeWeblogRole(UserWeblogRole roleToRevoke) {
        userWeblogRoleRepository.delete(roleToRevoke);
    }

    @Override
    public List<UserWeblogRole> getWeblogRoles(User user) {
        return userWeblogRoleRepository.findByUserAndPendingFalse(user);
    }

    @Override
    public List<UserWeblogRole> getWeblogRoles(Weblog weblog) {
        return userWeblogRoleRepository.findByWeblogAndPendingFalse(weblog);
    }

    @Override
    public List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog) {
        return userWeblogRoleRepository.findByWeblog(weblog);
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
            url = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl="
                    + URLEncoder.encode(String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    "TightBlog", user.getEmailAddress(), uc.getMfaSecret(), "TightBlog"),
                    StandardCharsets.UTF_8);
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
