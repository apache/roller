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

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Users who log in through LDAP or the servlet container have no Roller
 * password; Roller stores users.passwords.externalAuthValue in its place.
 * That placeholder is not a password, so this encoder never matches it.
 */
public class ExternalLoginAwarePasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;

    public ExternalLoginAwarePasswordEncoder(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String marker = WebloggerConfig.getProperty("users.passwords.externalAuthValue", "<externalAuth>");
        if (rawPassword != null && marker != null && !marker.isEmpty() && marker.contentEquals(rawPassword)) {
            return false;
        }
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }
}
