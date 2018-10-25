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
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tightblog.repository.UserCredentialsRepository;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.UserWeblogRoleRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component("userManager")
public class UserManagerImpl implements UserManager {

    private UserRepository userRepository;
    private UserWeblogRoleRepository userWeblogRoleRepository;
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    public UserManagerImpl(UserRepository userRepository, UserWeblogRoleRepository uwrRepository,
                           UserCredentialsRepository userCredentialsRepository) {
        this.userRepository = userRepository;
        this.userWeblogRoleRepository = uwrRepository;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    @Override
    public void removeUser(User user) {
        userWeblogRoleRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    public void updateCredentials(String userId, String newPassword) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPwd = encoder.encode(newPassword);
        userCredentialsRepository.updatePassword(userId, encodedPwd);
    }

    @Override
    public boolean checkWeblogRole(String username, Weblog weblog, WeblogRole role) {
        User userToCheck = userRepository.findEnabledByUserName(username);
        if (userToCheck != null) {
            return checkWeblogRole(userToCheck, weblog, role);
        }
        return false;
    }

    @Override
    public boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role) {
        boolean hasRole = false;

        if (user != null & weblog != null) {
            if (GlobalRole.ADMIN.equals(user.getGlobalRole())) {
                hasRole = true;
            } else {
                UserWeblogRole existingRole = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(user, weblog);
                if (existingRole != null && existingRole.hasEffectiveWeblogRole(role)) {
                    hasRole = true;
                }
            }
        }
        return hasRole;
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
    public String generateMFAQRUrl(User user) {
        String url = "";
        UserCredentials uc = userCredentialsRepository.findByUserName(user.getUserName());

        if (uc != null) {
            if (uc.getMfaSecret() == null) {
                uc.setMfaSecret(Base32.random());
                userCredentialsRepository.saveAndFlush(uc);
            }
            url = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl="
                    + URLEncoder.encode(String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    "TightBlog", user.getEmailAddress(), uc.getMfaSecret(), "TightBlog"),
                    StandardCharsets.UTF_8);
        }

        return url;
    }
}
