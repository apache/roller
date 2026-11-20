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
package org.apache.roller.weblogger.pojos.wrapper;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UserWrapperTest {

    /** A user whose stored names are exactly {@code name}. */
    private static User user(String name) {
        User user = mock(User.class);
        when(user.getUserName()).thenReturn("alice");
        when(user.getScreenName()).thenReturn(name);
        when(user.getFullName()).thenReturn(name);
        return user;
    }

    /** Themes print screen and full names straight into HTML. */
    @Test
    void namesAreHtmlEscaped() {
        // stored as typed (trusted admins)
        UserWrapper wrapper = UserWrapper.wrap(user("<script>alert(1)</script> & \"Tom\""));
        String expected = "&lt;script&gt;alert(1)&lt;/script&gt; &amp; &quot;Tom&quot;";
        assertEquals(expected, wrapper.getScreenName());
        assertEquals(expected, wrapper.getFullName());

        // already encoded by the sanitizer (untrusted admins): encoded once, not twice
        wrapper = UserWrapper.wrap(user("Tom &amp; &quot;Jerry&quot; &lt;b&gt;"));
        expected = "Tom &amp; &quot;Jerry&quot; &lt;b&gt;";
        assertEquals(expected, wrapper.getScreenName());
        assertEquals(expected, wrapper.getFullName());
    }

    @Test
    void hiddenUserNameShowsEscapedScreenName() {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getBooleanProperty("user.hideUserNames")).thenReturn(true);

            assertEquals("&lt;b&gt;Alice&lt;/b&gt;", UserWrapper.wrap(user("<b>Alice</b>")).getUserName());
        }
    }
}
