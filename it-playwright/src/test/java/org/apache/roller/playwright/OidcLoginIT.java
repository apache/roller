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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Signing in through an external identity provider.
 *
 * <p>Skipped unless Roller is running with an OIDC provider configured, so the
 * same suite is usable against a database-authenticated instance.
 */
@DisplayName("OIDC login")
class OidcLoginIT extends BaseIT {

    private static final String ADMIN_USER = "admin";
    private static final String REGULAR_USER = "user";
    private static final String BLOG_HANDLE = "oidcblog";
    private static final String BLOG_NAME = "OIDC Blog";
    private static final String ENTRY_TITLE = "Signed in with OIDC";
    private static final String ENTRY_TEXT = "This entry was published by an OIDC-authenticated user.";

    // pages
    private static final String LOGIN_PAGE = "roller-ui/login.rol";
    private static final String MENU_PAGE = "roller-ui/menu.rol";
    private static final String ADMIN_PAGE = "roller-ui/admin/globalConfig.rol";
    private static final String CREATE_WEBLOG_PAGE = "roller-ui/createWeblog.rol";

    // create-weblog form
    private static final String WEBLOG_NAME = "#createWeblog_bean_name";
    private static final String WEBLOG_HANDLE = "#createWeblog_bean_handle";
    private static final String WEBLOG_EMAIL = "#createWeblog_bean_emailAddress";
    private static final String WEBLOG_SUBMIT = "#createWeblog_0";

    // Roller's login page
    private static final String PROVIDER_BUTTON = "a[href*='/oauth2/authorization/']";
    private static final String SERVER_ADMIN_LINK = "Server administration";

    // the provider's own login form
    private static final Pattern PROVIDER_USERNAME_LABEL =
            Pattern.compile("username", Pattern.CASE_INSENSITIVE);
    private static final String PROVIDER_PASSWORD = "input[type='password']";
    private static final String PROVIDER_SUBMIT = "input[type='submit'], button[type='submit']";

    @BeforeEach
    void requireOidcProvider() {
        goTo(LOGIN_PAGE);
        assumeTrue(providerButton().count() > 0,
                "Roller is not configured with an OIDC provider");
    }

    @Test
    @DisplayName("an administrator lands on the main menu and can reach server administration")
    void administratorSignsIn() {
        signIn(ADMIN_USER, ADMIN_USER);

        assertThat(page).hasURL(baseUrl + MENU_PAGE);
        Locator serverAdmin = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(SERVER_ADMIN_LINK));
        assertThat(serverAdmin).isVisible();

        serverAdmin.click();
        assertThat(page).hasURL(baseUrl + ADMIN_PAGE);
        assertThat(page).hasTitle(Pattern.compile("Roller Configuration"));
    }

    @Test
    @DisplayName("a non-administrator is denied server administration")
    void nonAdministratorIsDeniedAdmin() {
        signIn(REGULAR_USER, REGULAR_USER);

        assertThat(page).hasURL(baseUrl + MENU_PAGE);
        assertThat(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(SERVER_ADMIN_LINK))).hasCount(0);

        // hiding the link is not the control; the URL itself must be refused
        var response = page.navigate(ADMIN_PAGE);
        assertThat(page).hasTitle(Pattern.compile("Access Denied"));
        Assertions.assertEquals(403, response.status(),
                "direct navigation to the admin page should be forbidden");
    }

    /**
     * The authoring journey as an OIDC user: create a weblog, publish an entry
     * and read it back on the blog. Also a regression test: rendered weblog
     * pages resolve the signed-in user from the principal name, which for OIDC
     * users was the provider's opaque subject ID; every weblog page then
     * failed with a Velocity error.
     */
    @Test
    @DisplayName("an OIDC user can create a weblog, publish an entry and read it while signed in")
    void oidcUserCanPublish() {
        signIn(ADMIN_USER, ADMIN_USER);

        // create the weblog unless an earlier run against this instance already did
        if (page.navigate(BLOG_HANDLE + "/").status() == 404) {
            goTo(CREATE_WEBLOG_PAGE);
            page.locator(WEBLOG_NAME).fill(BLOG_NAME);
            page.locator(WEBLOG_HANDLE).fill(BLOG_HANDLE);
            page.locator(WEBLOG_EMAIL).fill(ADMIN_USER + "@example.com");
            page.locator(WEBLOG_SUBMIT).click();
        }

        publishEntry(BLOG_HANDLE, ENTRY_TITLE, ENTRY_TEXT);

        var response = page.navigate(BLOG_HANDLE + "/");
        Assertions.assertEquals(200, response.status());
        assertThat(page.locator("body")).not().containsText("Velocity template error");
        assertEntryOnBlog(BLOG_HANDLE, ENTRY_TITLE, ENTRY_TEXT);
    }

    /** Clicks through Roller's provider button and the provider's own login form. */
    private void signIn(String username, String password) {
        goTo(LOGIN_PAGE);
        providerButton().first().click();

        page.getByLabel(PROVIDER_USERNAME_LABEL).first().fill(username);
        page.locator(PROVIDER_PASSWORD).first().fill(password);
        page.locator(PROVIDER_SUBMIT).first().click();
    }

    private Locator providerButton() {
        return page.locator(PROVIDER_BUTTON);
    }
}
