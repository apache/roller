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

import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The path a brand new Roller install puts its first visitor through: register
 * an account, sign in, create a weblog, then publish and read back an entry.
 *
 * <p>This is the journey the old Selenium suite covered. Roller only accepts
 * registrations on an install with no users yet, so the test skips when the
 * instance already has one (restart {@code mvn -pl app jetty:run} for a fresh
 * in-memory database) or when it delegates login to an identity provider.
 */
@DisplayName("New user journey")
class NewUserJourneyIT extends BaseIT {

    private static final String USERNAME = "bsmith";
    private static final String FULL_NAME = "Bob Smith";
    private static final String PASSWORD = "roller123";
    private static final String EMAIL = "bsmith@example.com";
    private static final String BLOG_HANDLE = "bobsblog";
    private static final String BLOG_NAME = "Bob's Blog";
    private static final String ENTRY_TITLE = "My First Blog Entry";
    private static final String ENTRY_TEXT = "Welcome to my blog!";

    // pages
    private static final String LOGIN_PAGE = "roller-ui/login.rol";
    private static final String REGISTER_PAGE = "roller-ui/register.rol";
    private static final String MENU_PAGE = "roller-ui/menu.rol";
    private static final String CREATE_WEBLOG_PAGE = "roller-ui/createWeblog.rol";

    // login form
    private static final String LOGIN_USERNAME = "input[name='j_username']";
    private static final String LOGIN_PASSWORD = "input[name='j_password']";
    private static final String LOGIN_SUBMIT = "#loginForm input[type='submit'], #loginForm button[type='submit']";

    // registration form
    private static final String REGISTER_USERNAME = "#register_bean_userName";
    private static final String REGISTER_SCREEN_NAME = "#register_bean_screenName";
    private static final String REGISTER_FULL_NAME = "#register_bean_fullName";
    private static final String REGISTER_EMAIL = "#register_bean_emailAddress";
    private static final String REGISTER_PASSWORD = "#register_bean_passwordText";
    private static final String REGISTER_PASSWORD_CONFIRM = "#register_bean_passwordConfirm";
    private static final String REGISTER_SUBMIT = "#submit";

    // create-weblog form
    private static final String WEBLOG_NAME = "#createWeblog_bean_name";
    private static final String WEBLOG_HANDLE = "#createWeblog_bean_handle";
    private static final String WEBLOG_EMAIL = "#createWeblog_bean_emailAddress";
    private static final String WEBLOG_SUBMIT = "#createWeblog_0";

    @Test
    @DisplayName("registers an account, creates a weblog, then publishes and reads an entry")
    void firstUserCanRegisterAndPublish() {
        goTo(LOGIN_PAGE);
        assumeTrue(page.locator(LOGIN_USERNAME).count() > 0,
                "Roller is not using its own user database");

        register();
        signIn();
        createWeblog();
        publishEntry(BLOG_HANDLE, ENTRY_TITLE, ENTRY_TEXT);
        assertEntryOnBlog(BLOG_HANDLE, ENTRY_TITLE, ENTRY_TEXT);
    }

    private void register() {
        goTo(REGISTER_PAGE);
        assertThat(page).hasTitle(Pattern.compile("New User Registration"));

        // on a fresh install registration must be open; against a developer's
        // long-running instance (no expected auth declared) skip instead
        boolean registrationOpen = page.locator(REGISTER_USERNAME).count() > 0;
        if (expectedAuth().isEmpty()) {
            assumeTrue(registrationOpen,
                    "registration is closed: this journey needs a fresh install with no users yet");
        } else {
            Assertions.assertTrue(registrationOpen,
                    "registration is closed, but a fresh install was expected to accept its first user");
        }

        page.locator(REGISTER_USERNAME).fill(USERNAME);
        page.locator(REGISTER_SCREEN_NAME).fill(USERNAME);
        page.locator(REGISTER_FULL_NAME).fill(FULL_NAME);
        page.locator(REGISTER_EMAIL).fill(EMAIL);
        page.locator(REGISTER_PASSWORD).fill(PASSWORD);
        page.locator(REGISTER_PASSWORD_CONFIRM).fill(PASSWORD);

        // the form enables its submit button from an onkeyup handler, which
        // fill() does not fire, so send a real key press to trigger validation
        page.locator(REGISTER_PASSWORD_CONFIRM).press("End");
        assertThat(page.locator(REGISTER_SUBMIT)).isEnabled();
        page.locator(REGISTER_SUBMIT).click();
    }

    private void signIn() {
        goTo(LOGIN_PAGE);
        page.locator(LOGIN_USERNAME).fill(USERNAME);
        page.locator(LOGIN_PASSWORD).fill(PASSWORD);
        page.locator(LOGIN_SUBMIT).first().click();

        goTo(MENU_PAGE);
        assertThat(page).hasTitle(Pattern.compile("Your Weblogs"));
    }

    private void createWeblog() {
        goTo(CREATE_WEBLOG_PAGE);
        assertThat(page).hasTitle(Pattern.compile("Create Weblog"));

        page.locator(WEBLOG_NAME).fill(BLOG_NAME);
        page.locator(WEBLOG_HANDLE).fill(BLOG_HANDLE);
        page.locator(WEBLOG_EMAIL).fill(EMAIL);
        page.locator(WEBLOG_SUBMIT).click();

        // the new weblog is now listed on the main menu
        goTo(MENU_PAGE);
        assertThat(page.getByText(BLOG_NAME).first()).isVisible();
    }
}
