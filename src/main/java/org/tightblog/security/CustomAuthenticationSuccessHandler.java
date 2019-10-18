/*
 * Copyright 2016 the original author or authors.
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
package org.tightblog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.tightblog.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private UserDao userDao;

    @Autowired
    public CustomAuthenticationSuccessHandler(UserDao userDao) {
        this.userDao = userDao;
        /* useReferer works in conjuction with CustomAccessDeniedHandlerImpl, so non-AJAX CSRF exceptions
        can redirect back to appropriate page after re-authentication */
        setUseReferer(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        Object authPrincipal = authentication.getPrincipal();

        UserDetails springUser = (UserDetails) authPrincipal;
        User user = userDao.findEnabledByUserName(springUser.getUsername());
        super.onAuthenticationSuccess(request, response, authentication);
        user.setLastLogin(Instant.now());
        userDao.saveAndFlush(user);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // If user went to login page directly instead of being routed there by Spring, the
        // destination page will remain the login page.  In such circumstances, forward on to the
        // Menu page instead.
        String targetUrl = request.getHeader("Referer");
        if (targetUrl != null) {
            if (targetUrl.endsWith("/login")) {
                return targetUrl.substring(0, targetUrl.lastIndexOf("/login")) + "/home";
            } else if (targetUrl.contains("/login?activationCode=")) {
                // here, login page via email activation of account.
                return targetUrl.substring(0, targetUrl.lastIndexOf("/login?activationCode=")) + "/home";
            }
        }

        return super.determineTargetUrl(request, response);
    }
}
