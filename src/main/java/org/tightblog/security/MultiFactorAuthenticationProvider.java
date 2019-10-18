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
package org.tightblog.security;

import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.tightblog.domain.UserCredentials;
import org.tightblog.dao.UserCredentialsDao;

@Component
public class MultiFactorAuthenticationProvider extends DaoAuthenticationProvider {

    private UserCredentialsDao userCredentialsDao;

    @Autowired
    public MultiFactorAuthenticationProvider(UserCredentialsDao userCredentialsDao,
                                             UserDetailsService userDetailsService,
                                             MessageSource messageSource) {
        this.userCredentialsDao = userCredentialsDao;
        setPasswordEncoder(new BCryptPasswordEncoder());
        setUserDetailsService(userDetailsService);
        setMessageSource(messageSource);
    }

    @Value("${mfa.enabled}")
    private boolean mfaEnabled;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        // check username & password first
        Authentication result = super.authenticate(auth);

        // if here, username & password were correct, so check validation code if we're using MFA
        if (mfaEnabled) {
            String verificationCode = ((CustomWebAuthenticationDetails) auth.getDetails()).getVerificationCode();

            UserCredentials creds = userCredentialsDao.findByUserName(auth.getName());

            if (creds.getMfaSecret() != null) {
                Totp totp = new Totp(creds.getMfaSecret());
                if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
                    throw new InvalidVerificationCodeException("Authenticator app code invalid");
                }
            }
        }

        return result;
    }

    private static boolean isValidLong(String code) {
        try {
            NumberUtils.createLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static class InvalidVerificationCodeException extends BadCredentialsException {
        InvalidVerificationCodeException(String msg) {
            super(msg);
        }
    }
}
