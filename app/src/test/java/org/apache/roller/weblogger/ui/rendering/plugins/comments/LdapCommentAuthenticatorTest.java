/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdapCommentAuthenticatorTest {

    @Test
    void rendersEmptyFieldsOnFirstVisit() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);
        String html = render(request);
        assertFields(html, "", "");
        verify(session).setAttribute("ldapUser", "");
        verify(session).setAttribute("ldapPass", "");
    }

    @Test
    void rendersMissingValuesAsEmpty() {
        assertFields(renderReturningVisit(null, null), "", "");
    }

    @Test
    void preservesOrdinaryFormValues() {
        assertFields(renderReturningVisit("reader", "sample-pass"), "reader", "sample-pass");
    }

    @Test
    void formatsPunctuationInFormValues() {
        String value = "A&B \"quoted\" <label> 'name'";
        String formatted = "A&amp;B &quot;quoted&quot; &lt;label&gt; 'name'";
        assertFields(renderReturningVisit(value, value), formatted, formatted);
    }

    @Test
    void preservesLiteralEntityText() {
        assertFields(renderReturningVisit("&quot;", "&#34;"), "&amp;quot;", "&amp;#34;");
    }

    private String renderReturningVisit(String user, String password) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("ldapUser")).thenReturn("");
        when(request.getParameter("ldapUser")).thenReturn(user);
        when(request.getParameter("ldapPass")).thenReturn(password);
        return render(request);
    }

    private String render(HttpServletRequest request) {
        try (MockedStatic<CommentAuthenticatorUtils> locales = mockStatic(CommentAuthenticatorUtils.class)) {
            locales.when(() -> CommentAuthenticatorUtils.getLocale(request)).thenReturn(Locale.ENGLISH);
            return new LdapCommentAuthenticator().getHtml(request);
        }
    }

    private void assertFields(String html, String user, String password) {
        assertTrue(html.contains("<input name=\"ldapUser\" value=\"" + user + "\">"));
        assertTrue(html.contains("<input type=\"password\" name=\"ldapPass\" value=\"" + password + "\">"));
    }
}
