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
package org.apache.roller.weblogger.ui.rendering;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.ui.rendering.model.ConfigModel;
import org.apache.roller.weblogger.ui.rendering.model.URLModel;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.TrackbackLinkbackCommentValidator;
import org.apache.roller.weblogger.util.BannedwordslistChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncomingTrackbackRemovalTest {

    @Test
    void incomingTrackbackEndpointsAndSettingsAreRemoved() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "org.apache.roller.weblogger.ui.rendering.servlets.TrackbackServlet"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "org.apache.roller.weblogger.ui.rendering.util.WeblogTrackbackRequest"));
        assertThrows(NoSuchMethodException.class,
                () -> ConfigModel.class.getMethod("getTrackbacksEnabled"));
        assertThrows(NoSuchMethodException.class,
                () -> BannedwordslistChecker.class.getMethod(
                        "checkTrackback",
                        org.apache.roller.weblogger.pojos.WeblogEntryComment.class));
    }

    @Test
    @SuppressWarnings("removal")
    void legacyExtensionPointsRemainHarmlessForOneRelease() throws Exception {
        assertEquals("", new URLModel().trackback("entry"));
        assertEquals(RollerConstants.PERCENT_100,
                new TrackbackLinkbackCommentValidator().validate(null, null));
        assertFileContains(
                "src/main/webapp/WEB-INF/velocity/weblog.vm",
                "#macro(showTrackbackAutodiscovery $entry)\n#end");
    }

    @Test
    void deploymentAndRuntimeConfigurationDoNotExposeTrackbacks() throws Exception {
        assertFileDoesNotContain("src/main/webapp/WEB-INF/web.xml", "trackback");
        assertResourceDoesNotContain(
                "org/apache/roller/weblogger/config/runtimeConfigDefs.xml", "trackback");
        assertFileDoesNotContain("../docs/roller-user-guide.adoc", "trackback");
        assertFileDoesNotContain("../docs/roller-template-guide.adoc", "trackback");
    }

    private void assertFileDoesNotContain(String path, String value) throws Exception {
        String content = Files.readString(resolveAppPath(path), StandardCharsets.UTF_8);
        assertFalse(content.toLowerCase().contains(value), path);
    }

    private void assertFileContains(String path, String value) throws Exception {
        String content = Files.readString(resolveAppPath(path), StandardCharsets.UTF_8);
        assertTrue(content.contains(value), path);
    }

    private Path resolveAppPath(String path) throws Exception {
        Path current = Path.of(getClass().getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("src/main"))) {
                return current.resolve(path).normalize();
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to locate the app module");
    }

    private void assertResourceDoesNotContain(String path, String value) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertFalse(content.toLowerCase().contains(value), path);
        }
    }
}
