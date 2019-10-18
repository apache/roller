/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.dao;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.domain.User;
import org.tightblog.domain.UserStatus;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface UserWeblogRoleDao extends JpaRepository<UserWeblogRole, String> {

    List<UserWeblogRole> findByUser(User user);

    List<UserWeblogRole> findByWeblog(Weblog weblog);

    default List<User> findByWeblogAndStatusEnabled(Weblog weblog) {
        return findByWeblog(weblog).stream().map(UserWeblogRole::getUser).filter(
                u->UserStatus.ENABLED.equals(u.getStatus())).collect(Collectors.toList());
    }

    List<UserWeblogRole> findByWeblogAndEmailCommentsTrue(Weblog weblog);

    default UserWeblogRole findByIdOrNull(String id) {
        return findById(id).orElse(null);
    }

    @Cacheable(value = "userWeblogRoles", key = "{#user.userName, #weblog.handle}")
    UserWeblogRole findByUserAndWeblog(User user, Weblog weblog);

    @CacheEvict(cacheNames = {"userWeblogRoles"}, key = "{#user.userName, #weblog.handle}")
    default void evictUserWeblogRole(User user, Weblog weblog) {
        // no-op
    }

    @Transactional("transactionManager")
    @CacheEvict(cacheNames = {"userWeblogRoles"}, allEntries = true)
    Long deleteByUser(User user);

    @Transactional("transactionManager")
    @CacheEvict(cacheNames = {"userWeblogRoles"}, allEntries = true)
    Long deleteByWeblog(Weblog weblog);
}
