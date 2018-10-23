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
package org.tightblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;

import java.util.List;

@Repository
@Transactional("transactionManager")
public interface UserWeblogRoleRepository extends JpaRepository<UserWeblogRole, String> {

    List<UserWeblogRole> findByUser(User user);

    List<UserWeblogRole> findByUserAndPendingFalse(User user);

    List<UserWeblogRole> findByWeblog(Weblog weblog);

    List<UserWeblogRole> findByWeblogAndPendingTrue(Weblog weblog);

    List<UserWeblogRole> findByWeblogAndPendingFalse(Weblog weblog);

    UserWeblogRole findByUserAndWeblog(User user, Weblog weblog);

    UserWeblogRole findByUserAndWeblogAndPendingFalse(User user, Weblog weblog);

    Long deleteByUser(User user);

    Long deleteByWeblog(Weblog weblog);
}
