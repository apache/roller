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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Smoke tests for the web service endpoints whose servlets were forked from
 * javax-only libraries during the Jakarta migration: the Blogger/MetaWeblog
 * XML-RPC API and the Atom Publishing Protocol. Nothing else exercises that
 * forked code, so these prove the endpoints still answer.
 *
 * <p>Runs after {@link NewUserJourneyIT} (alphabetical run order) and reuses
 * the account and weblog it created. Both APIs ship disabled, so the test
 * first turns them on from the server admin page. Skips on an instance
 * without form login, because both APIs authenticate against database
 * accounts.
 */
@DisplayName("Web services smoke")
class WebServicesIT extends BaseIT {

    private static final String USERNAME = "bsmith";
    private static final String PASSWORD = "roller123";
    private static final String BLOG_HANDLE = "bobsblog";

    private static final String LOGIN_USERNAME = "input[name='j_username']";
    private static final String LOGIN_PASSWORD = "input[name='j_password']";
    private static final String LOGIN_SUBMIT = "#loginForm input[type='submit'], #loginForm button[type='submit']";

    private static final String GLOBAL_CONFIG_PAGE = "roller-ui/admin/globalConfig.rol";
    private static final String XMLRPC_ENABLED = "input[name='webservices.enableXmlRpc']";
    private static final String ATOMPUB_ENABLED = "input[name='webservices.enableAtomPub']";
    private static final String GLOBAL_CONFIG_SAVE = "#saveButton";

    private static final String XMLRPC_ENDPOINT = "roller-services/xmlrpc";
    private static final String ATOMPUB_ENDPOINT = "roller-services/app";

    @BeforeEach
    void enableWebServices() {
        goTo("roller-ui/login.rol");
        boolean formLogin = page.locator(LOGIN_USERNAME).count() > 0;
        if ("oidc".equals(expectedAuth()) || expectedAuth().isEmpty()) {
            assumeTrue(formLogin,
                    "no form login: the XML-RPC and AtomPub APIs authenticate against database accounts");
        } else {
            Assertions.assertTrue(formLogin,
                    "form login expected for auth method " + expectedAuth() + " but the login page has none");
        }

        page.locator(LOGIN_USERNAME).fill(USERNAME);
        page.locator(LOGIN_PASSWORD).fill(PASSWORD);
        page.locator(LOGIN_SUBMIT).first().click();
        goTo("roller-ui/menu.rol");
        boolean signedIn = page.title().contains("Your Weblogs");
        if (expectedAuth().isEmpty()) {
            assumeTrue(signedIn,
                    "the journey's account is not available: this smoke test needs the fresh install NewUserJourneyIT sets up");
        } else {
            Assertions.assertTrue(signedIn, "could not sign in as the account NewUserJourneyIT registered");
        }

        // both APIs ship disabled; the journey's first user is the admin
        goTo(GLOBAL_CONFIG_PAGE);
        page.locator(XMLRPC_ENABLED).check();
        page.locator(ATOMPUB_ENABLED).check();
        page.locator(GLOBAL_CONFIG_SAVE).click();
        assertThat(page.locator(XMLRPC_ENABLED)).isChecked();
    }

    @Test
    @DisplayName("XML-RPC lists the user's weblogs and MetaWeblog publishes an entry")
    void xmlRpcCanListBlogsAndPost() {
        APIResponse listing = postXml(bloggerGetUsersBlogs());
        Assertions.assertEquals(200, listing.status(), listing.text());
        String body = listing.text();
        Assertions.assertFalse(body.contains("faultCode"), body);
        Assertions.assertTrue(body.contains(BLOG_HANDLE), body);

        String title = "Posted via MetaWeblog";
        APIResponse posted = postXml(metaWeblogNewPost(title, "This entry arrived over XML-RPC."));
        Assertions.assertEquals(200, posted.status(), posted.text());
        Assertions.assertFalse(posted.text().contains("faultCode"), posted.text());

        // the entry it created is the newest one rendered on the blog
        goTo(BLOG_HANDLE + "/");
        assertThat(page.locator("p.entryTitle, .entryTitle").first()).containsText(title);
    }

    @Test
    @DisplayName("AtomPub serves the account's service document over basic auth")
    void atomPubServesServiceDocument() {
        String credentials = Base64.getEncoder()
                .encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8));
        APIResponse response = page.request().get(baseUrl + ATOMPUB_ENDPOINT,
                RequestOptions.create().setHeader("Authorization", "Basic " + credentials));
        Assertions.assertEquals(200, response.status(), response.text());

        String body = response.text();
        Assertions.assertTrue(Pattern.compile("<(\\w+:)?service").matcher(body).find(),
                "expected an AtomPub service document, got: " + body);
        Assertions.assertTrue(body.contains(BLOG_HANDLE), body);
    }

    private APIResponse postXml(String methodCall) {
        return page.request().post(baseUrl + XMLRPC_ENDPOINT, RequestOptions.create()
                .setHeader("Content-Type", "text/xml")
                .setData(methodCall));
    }

    private static String bloggerGetUsersBlogs() {
        return """
                <?xml version="1.0"?>
                <methodCall>
                    <methodName>blogger.getUsersBlogs</methodName>
                    <params>
                        <param><value><string>ignored</string></value></param>
                        <param><value><string>%s</string></value></param>
                        <param><value><string>%s</string></value></param>
                    </params>
                </methodCall>
                """.formatted(USERNAME, PASSWORD);
    }

    private static String metaWeblogNewPost(String title, String description) {
        return """
                <?xml version="1.0"?>
                <methodCall>
                    <methodName>metaWeblog.newPost</methodName>
                    <params>
                        <param><value><string>%s</string></value></param>
                        <param><value><string>%s</string></value></param>
                        <param><value><string>%s</string></value></param>
                        <param><value><struct>
                            <member><name>title</name><value><string>%s</string></value></member>
                            <member><name>description</name><value><string>%s</string></value></member>
                        </struct></value></param>
                        <param><value><boolean>1</boolean></value></param>
                    </params>
                </methodCall>
                """.formatted(BLOG_HANDLE, USERNAME, PASSWORD, title, description);
    }
}
