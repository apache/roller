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
 */
package org.apache.roller.playwright;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pins the login page to the authentication method the instance is supposed
 * to be running: {@code db} must offer only the username/password form,
 * {@code oidc} only the identity-provider buttons, {@code db-oidc} both.
 *
 * <p>The other suites skip whatever their instance does not offer, so on a
 * misconfigured instance they could all skip and still pass. This test runs
 * only when {@code -Droller.expectedAuth} says what the instance is meant to
 * be, and then a missing or surplus sign-in mechanism is a failure.
 */
@DisplayName("Login page")
class LoginPageIT extends BaseIT {

    private static final String LOGIN_PAGE = "roller-ui/login.rol";
    private static final String LOGIN_FORM = "input[name='j_username']";
    private static final String PROVIDER_BUTTON = "a[href*='/oauth2/authorization/']";

    @Test
    @DisplayName("offers exactly the sign-in mechanisms of the configured authentication method")
    void offersConfiguredMechanisms() {
        String expected = expectedAuth();
        assumeTrue(!expected.isEmpty(),
                "pass -Droller.expectedAuth=db|oidc|db-oidc to pin what the login page must offer");

        boolean expectForm = switch (expected) {
            case "db", "db-oidc" -> true;
            case "oidc" -> false;
            default -> Assertions.fail("unknown roller.expectedAuth value: " + expected);
        };
        boolean expectProviders = !expected.equals("db");

        goTo(LOGIN_PAGE);
        Assertions.assertEquals(expectForm, page.locator(LOGIN_FORM).count() > 0,
                "username/password form on the login page");
        Assertions.assertEquals(expectProviders, page.locator(PROVIDER_BUTTON).count() > 0,
                "identity-provider buttons on the login page");
    }
}
