/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.core.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Users who log in through LDAP or the servlet container have no Roller
 * password; Roller stores users.passwords.externalAuthValue in its place.
 * That placeholder is not a password, so this encoder never matches it, nor
 * the default or a previous placeholder, nor an empty password.
 */
public class ExternalLoginAwarePasswordEncoder implements PasswordEncoder {

    /** Stored in place of a password when no other value is configured. */
    public static final String DEFAULT_EXTERNAL_LOGIN_MARKER = "<externalAuth>";

    private final PasswordEncoder delegate;

    public ExternalLoginAwarePasswordEncoder(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    /**
     * The value stored in place of a Roller password for users who log in
     * through LDAP or the servlet container: users.passwords.externalAuthValue,
     * or the default when that setting is empty.
     */
    public static String externalLoginMarker() {
        String marker = WebloggerConfig.getProperty("users.passwords.externalAuthValue");
        return marker == null || marker.isEmpty() ? DEFAULT_EXTERNAL_LOGIN_MARKER : marker;
    }

    /**
     * Whether a password is the configured external-login marker, the default
     * one, or one listed in users.passwords.externalAuthValue.previous, which
     * accounts may still have stored from before the setting changed. None of
     * them matches at login, so none can be chosen as a Roller password.
     */
    public static boolean isExternalLoginMarker(CharSequence password) {
        if (password == null) {
            return false;
        }
        if (externalLoginMarker().contentEquals(password)
                || DEFAULT_EXTERNAL_LOGIN_MARKER.contentEquals(password)) {
            return true;
        }
        String previous = WebloggerConfig.getProperty("users.passwords.externalAuthValue.previous");
        for (String marker : StringUtils.split(StringUtils.defaultString(previous), ',')) {
            // an empty entry is never a marker, so an empty password isn't one
            String trimmed = marker.trim();
            if (!trimmed.isEmpty() && trimmed.contentEquals(password)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // a stored marker is checked before the delegate sees it, since a
        // marker such as {noop}secret would otherwise match its own suffix
        if (rawPassword == null || rawPassword.length() == 0 || isExternalLoginMarker(rawPassword)
                || isExternalLoginMarker(encodedPassword)) {
            return false;
        }
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }
}
