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

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomUnusedPasswordTest {

    @Test
    void everyEncoderAcceptsIt() {
        String password = RollerPasswordEncoders.randomUnusedPassword();
        assertEquals(64, password.length());
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        assertTrue(bcrypt.matches(password, bcrypt.encode(password)));
        assertNotEquals(password, RollerPasswordEncoders.randomUnusedPassword());
    }

    @Test
    void oidcAccountFormsUseIt() throws Exception {
        for (String action : new String[] {"core/Register", "core/Profile", "admin/UserEdit"}) {
            String source = new String(Files.readAllBytes(Paths.get(
                    "src/main/java/org/apache/roller/weblogger/ui/struts2/" + action + ".java")));
            assertFalse(source.contains("randomAlphanumeric(255)"), action);
            assertTrue(source.contains("RollerPasswordEncoders.randomUnusedPassword()"), action);
        }
    }
}
