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

/* Indicator of user's ability to log into the TightBlog system. Depending on authentication
 * mechanism being used and security settings, not all statuses may be relevant.
 */
public enum UserStatus {
    // Once-enabled user may no longer log in unless an admin re-enables the account.
    DISABLED(0),
    // User has applied for a registration but not yet verified his email.
    REGISTERED(1),
    // User has verified his email, admin may now approve (or delete) his account request.
    EMAILVERIFIED(2),
    // User is enabled and can log in.
    ENABLED(3);

    private int weight;

    public int getWeight() {
        return weight;
    }

    UserStatus(int weight) {
        this.weight = weight;
    }
}
