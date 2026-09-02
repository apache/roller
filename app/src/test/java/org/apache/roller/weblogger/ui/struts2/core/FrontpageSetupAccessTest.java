/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.ui.struts2.core;

import java.util.List;

import org.apache.roller.weblogger.business.FrontpageSettings;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Checks who is allowed to change the site frontpage setting.
 *
 * <p>The setup screen is reachable without a login, because a site with no users
 * has nobody who could log in. A page in that position should display bootstrap
 * guidance and nothing more, so the frontpage write lives on a separate action
 * that requires a global administrator. These tests pin that arrangement in
 * place: the display page exposes no write method, the write action requires the
 * permission and validates requests before attempting a write.
 */
public class FrontpageSetupAccessTest {

    /**
     * The mutation action requires the global administrator permission. This is
     * the single check the whole fix rests on.
     */
    @Test
    public void frontpageSetupRequiresGlobalAdmin() {
        List<String> required = new FrontpageSetup().requiredGlobalPermissionActions();
        assertEquals(1, required.size(), "expected exactly one required permission");
        assertEquals(GlobalPermission.ADMIN, required.get(0),
                "the frontpage write must require a global administrator");
    }

    /**
     * The public setup page must not require a user, because it has to work on
     * an empty site. That is precisely why it must not be able to write.
     */
    @Test
    public void publicSetupPageStillNeedsNoUser() {
        Setup setup = new Setup();
        assertFalse(setup.isUserRequired(),
                "the bootstrap page must stay reachable on a site with no users");
    }

    /** The separate write action always requires an authenticated user. */
    @Test
    public void frontpageSetupRequiresAUser() {
        assertEquals(true, new FrontpageSetup().isUserRequired());
    }

    /** The write action must reject requests that are not HTTP POST. */
    @Test
    public void frontpageSetupSaveEnforcesPost() {
        FrontpageSetup action = new FrontpageSetup() {
            @Override
            protected boolean isPostRequest() {
                return false;
            }
        };
        assertEquals(FrontpageSetup.DENIED, action.save());
    }

    /** Blank and malformed handles are rejected before a database lookup. */
    @Test
    public void invalidHandlesNeverResolve() throws Exception {
        assertNull(FrontpageSettings.resolveWeblog(null));
        assertNull(FrontpageSettings.resolveWeblog(""));
        assertNull(FrontpageSettings.resolveWeblog("   "));
        assertNull(FrontpageSettings.resolveWeblog("not/a/handle"));
    }
}
