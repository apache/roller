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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

// GlobalRole defines system-wide roles as opposed to individual blog permissions
// Each role includes the roles of those of lower weight as defined in this enum
public enum GlobalRole {
    // For pages that don't need authenticated users (initial install, user registration, login, etc.)
    NOAUTHNEEDED(0),
    // For users missing Authenticator app secret and system requires multifactor authentication
    MISSING_MFA_SECRET(1),
    // Users can edit weblogs for which they have permission
    BLOGGER(2),
    // Users can create new blogs
    BLOGCREATOR(3),
    // Blog server admin rights: server settings, user management, etc.
    ADMIN(4);

    private int weight;

    public int getWeight() {
        return weight;
    }

    GlobalRole(int weight) {
        this.weight = weight;
    }

    @JsonCreator
    public static GlobalRole forValue(String value) {
        return GlobalRole.valueOf(value);
    }

}
