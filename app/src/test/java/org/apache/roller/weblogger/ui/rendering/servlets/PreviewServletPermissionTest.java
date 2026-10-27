/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.Collections;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Previews are for users with a role in the weblog being previewed.
 */
class PreviewServletPermissionTest {

    private User owner;
    private User other;
    private Weblog weblog;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        owner = TestUtils.setupUser("previewOwner");
        other = TestUtils.setupUser("previewOther");
        weblog = TestUtils.setupWeblog("previewWeblog", owner);
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(owner.getUserName());
        TestUtils.teardownUser(other.getUserName());
        TestUtils.endSession(true);
    }

    @Test
    void ownerCanPreview() throws Exception {
        assertTrue(PreviewServlet.canPreview(TestUtils.getManagedWebsite(weblog), user(owner)));
    }

    @Test
    void userWithoutRoleCannotPreview() throws Exception {
        assertFalse(PreviewServlet.canPreview(TestUtils.getManagedWebsite(weblog), user(other)));
        assertFalse(PreviewServlet.canPreview(TestUtils.getManagedWebsite(weblog), null));
    }

    @Test
    void memberWithDraftRoleCanPreview() throws Exception {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        mgr.grantWeblogPermission(TestUtils.getManagedWebsite(weblog), user(other),
                Collections.singletonList(WeblogPermission.EDIT_DRAFT));
        TestUtils.endSession(true);
        assertTrue(PreviewServlet.canPreview(TestUtils.getManagedWebsite(weblog), user(other)));
    }

    @Test
    void disabledOwnerCannotPreview() throws Exception {
        // a detached copy, so the stored owner stays enabled
        User disabled = new User();
        disabled.setUserName(owner.getUserName());
        disabled.setEnabled(Boolean.FALSE);
        assertFalse(PreviewServlet.canPreview(TestUtils.getManagedWebsite(weblog), disabled));
    }

    private static User user(User u) throws Exception {
        return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(u.getUserName());
    }
}
