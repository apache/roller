/*
   Copyright 2015 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.apache.roller.weblogger.pojos;

// GlobalRole defines system-wide roles as opposed to individual blog permissions
// Each role includes the roles of those of lower weight as defined in this enum
// LOGIN and COMMENT presently unused/unncoded
public enum GlobalRole {
    // For pages that don't need authenticated users (initial install, user registration, login, etc.)
    NOAUTHNEEDED(0),
    // User can log into Roller
    LOGIN(1),
    // User can leave blog entry comments
    COMMENT(2),
    // Users can edit weblogs for which they have permission
    BLOGGER(3),
    // Blog server admin rights: server settings, user management, etc.
    ADMIN(4);

    private int weight;

    public int getWeight() {
        return weight;
    }

    GlobalRole(int weight) {
        this.weight = weight;
    }
}
