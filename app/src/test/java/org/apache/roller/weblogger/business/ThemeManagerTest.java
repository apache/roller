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

package org.apache.roller.weblogger.business;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ThemeManagerTest  {
    public static Log log = LogFactory.getLog(ThemeManagerTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testThemeAssumptions() throws Exception {

        ThemeManager themeManager = WebloggerFactory.getWeblogger().getThemeManager();
        themeManager.initialize();

        assertNotNull( themeManager.getTheme("basic") );
        assertNotNull( themeManager.getTheme("basic").getStylesheet() );
        assertNull( themeManager.getTheme("frontpage").getStylesheet() );
        assertNotNull( themeManager.getTheme("hyde") );
        assertNotNull( themeManager.getTheme("hyde").getStylesheet() );
        assertNotNull( themeManager.getTheme("papermod") );
        assertNotNull( themeManager.getTheme("papermod").getStylesheet() );
        assertNotNull( themeManager.getTheme("terminal") );
        assertNotNull( themeManager.getTheme("terminal").getStylesheet() );
    }

    /** A weblog with no default media folder still gets a theme's root-level images. */
    @Test
    public void testImportThemeIntoWeblogWithoutDefaultMediaFolder() throws Exception {
        User user = TestUtils.setupUser("themeImportUser");
        Weblog weblog = TestUtils.setupWeblog("themeImportWeblog", user);
        TestUtils.endSession(true);

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        weblog = TestUtils.getManagedWebsite(weblog);
        mfMgr.removeMediaFileDirectory(mfMgr.getOrCreateDefaultMediaFileDirectory(weblog));
        WebloggerFactory.getWeblogger().flush();
        TestUtils.endSession(true);

        ThemeManager themeManager = WebloggerFactory.getWeblogger().getThemeManager();
        themeManager.initialize();
        weblog = TestUtils.getManagedWebsite(weblog);
        assertNull(mfMgr.getDefaultMediaFileDirectory(weblog));

        // the frontpage theme lists root-level images such as roller-tan-logo.gif
        themeManager.importTheme(weblog, themeManager.getTheme("frontpage"), false);
        WebloggerFactory.getWeblogger().flush();
        TestUtils.endSession(true);

        weblog = TestUtils.getManagedWebsite(weblog);
        MediaFileDirectory root = mfMgr.getDefaultMediaFileDirectory(weblog);
        assertNotNull(root);
        assertEquals(1, root.getMediaFiles().stream()
                .filter(f -> "roller-tan-logo.gif".equals(f.getName())).count());

        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
    }
        
}



