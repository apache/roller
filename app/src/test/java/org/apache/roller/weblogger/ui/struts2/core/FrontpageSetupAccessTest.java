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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.roller.weblogger.business.FrontpageSettings;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks who is allowed to change the site frontpage setting.
 *
 * <p>The setup screen is reachable without a login, because a site with no users
 * has nobody who could log in. A page in that position should display bootstrap
 * guidance and nothing more, so the frontpage write lives on a separate action
 * that requires a global administrator. These tests pin that arrangement in
 * place: the display page exposes no write method, the write action requires the
 * permission, and both write paths validate through one service.
 */
public class FrontpageSetupAccessTest {

    private static final Path STRUTS_XML = Paths.get("src", "main", "resources", "struts.xml");
    private static final Path SETUP_JSP =
            Paths.get("src", "main", "webapp", "WEB-INF", "jsps", "core", "Setup.jsp");

    private String read(Path path) throws IOException {
        assertTrue(Files.isReadable(path),
                "cannot read " + path.toAbsolutePath() + " (run from the app module)");
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

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
    public void publicSetupPageStillNeedsNoUserButCannotWrite() throws IOException {
        Setup setup = new Setup();
        assertFalse(setup.isUserRequired(),
                "the bootstrap page must stay reachable on a site with no users");

        String struts = read(STRUTS_XML);
        int setupIdx = struts.indexOf("name=\"setup\"");
        assertTrue(setupIdx > 0, "setup action not found in struts.xml");
        String setupBlock = struts.substring(setupIdx, struts.indexOf("</action>", setupIdx));
        assertFalse(setupBlock.contains("save"),
                "the public setup action must expose no save method:\n" + setupBlock);
    }

    /** The separate action exists and exposes only its save method. */
    @Test
    public void frontpageSetupActionIsWiredAndSaveOnly() throws IOException {
        String struts = read(STRUTS_XML);
        int idx = struts.indexOf("name=\"frontpageSetup\"");
        assertTrue(idx > 0, "frontpageSetup action not wired in struts.xml");
        String block = struts.substring(idx, struts.indexOf("</action>", idx));
        assertTrue(block.contains("FrontpageSetup"), "wrong action class:\n" + block);
        assertTrue(block.contains("<allowed-methods>save</allowed-methods>"),
                "frontpageSetup must expose only save:\n" + block);
    }

    /** The form must post to the administrator-only action, over POST. */
    @Test
    public void setupFormPostsToTheAdminAction() throws IOException {
        String jsp = read(SETUP_JSP);
        assertFalse(jsp.contains("setup!save"),
                "the form must no longer target the public setup action");
        assertTrue(jsp.contains("frontpageSetup!save"),
                "the form must target the administrator-only action");
        assertTrue(jsp.contains("method=\"post\""),
                "the form must POST so the CSRF salt filter applies");
        assertTrue(jsp.contains("<s:hidden name=\"salt\"/>"),
                "the form must carry a CSRF salt");
    }

    /**
     * Both write paths must resolve the handle through the shared service, so
     * neither can store a weblog that does not exist.
     */
    @Test
    public void bothWritePathsValidateThroughTheSharedService() throws IOException {
        String globalConfig = read(Paths.get("src", "main", "java", "org", "apache", "roller",
                "weblogger", "ui", "struts2", "admin", "GlobalConfig.java"));
        assertTrue(globalConfig.contains("FrontpageSettings.resolveWeblog"),
                "the global configuration screen must validate the frontpage handle");

        String frontpageSetup = read(Paths.get("src", "main", "java", "org", "apache", "roller",
                "weblogger", "ui", "struts2", "core", "FrontpageSetup.java"));
        assertTrue(frontpageSetup.contains("FrontpageSettings.apply"),
                "the initial write must go through the shared service");
        assertTrue(frontpageSetup.contains("FrontpageSettings.isConfigured"),
                "the initial write must apply only while no frontpage is set");
    }

    /** The write action must reject requests that are not HTTP POST. */
    @Test
    public void frontpageSetupSaveEnforcesPost() throws IOException {
        String frontpageSetup = read(Paths.get("src", "main", "java", "org", "apache", "roller",
                "weblogger", "ui", "struts2", "core", "FrontpageSetup.java"));
        assertTrue(frontpageSetup.contains("\"POST\".equalsIgnoreCase"),
                "save() must reject non-POST requests");
        assertTrue(frontpageSetup.contains("getMethod()"),
                "save() must inspect the request method");
    }

    /** A blank handle can never resolve, whatever the database contains. */
    @Test
    public void blankHandlesNeverResolve() throws Exception {
        assertEquals(null, FrontpageSettings.resolveWeblog(null));
        assertEquals(null, FrontpageSettings.resolveWeblog(""));
        assertEquals(null, FrontpageSettings.resolveWeblog("   "));
    }
}
