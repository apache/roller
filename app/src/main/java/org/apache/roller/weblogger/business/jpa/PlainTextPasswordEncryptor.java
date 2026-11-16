/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.business.jpa;

import org.eclipse.persistence.security.Securable;

/**
 * Hands the database password to EclipseLink unchanged.
 * <p>
 * By default EclipseLink tries to decrypt every JDBC password, and a password
 * made only of hex digits looks encrypted to it: decryption fails and Roller
 * cannot start. Roller passes plain-text passwords, so it doesn't need that.
 */
public class PlainTextPasswordEncryptor implements Securable {

    @Override
    public String encryptPassword(String password) {
        return password;
    }

    @Override
    public String decryptPassword(String encryptedPassword) {
        return encryptedPassword;
    }
}
