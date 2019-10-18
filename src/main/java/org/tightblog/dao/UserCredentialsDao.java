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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.domain.UserCredentials;

@Repository
public interface UserCredentialsDao extends JpaRepository<UserCredentials, String> {

    @Query("SELECT uc FROM UserCredentials uc, User u WHERE uc.userName= ?1 " +
            "AND u.id = uc.id AND u.status = org.tightblog.domain.UserStatus.ENABLED")
    UserCredentials findByUserName(String userName);

    @Transactional("transactionManager")
    @Modifying
    @Query("UPDATE UserCredentials u SET u.password = ?2 WHERE u.id = ?1")
    void updatePassword(String userId, String newPassword);

    @Transactional("transactionManager")
    @Modifying
    @Query("UPDATE UserCredentials u SET u.mfaSecret = null WHERE u.id = ?1")
    void eraseMfaCode(String userId);
}
