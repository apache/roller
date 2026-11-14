/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaltConfigurationTest {

    @Test
    public void testSubmittedSaltIsValidatedBeforeResponseSaltIsLoaded() throws Exception {
        Path projectDirectory = Path.of(
                System.getProperty("project.build.directory")).getParent();
        String webXml = Files.readString(projectDirectory.resolve(
                "src/main/webapp/WEB-INF/web.xml"));

        int validateMapping = filterMappingPosition(webXml, "ValidateSaltFilter");
        int loadMapping = filterMappingPosition(webXml, "LoadSaltFilter");

        assertTrue(validateMapping >= 0, "ValidateSaltFilter mapping is missing");
        assertTrue(loadMapping >= 0, "LoadSaltFilter mapping is missing");
        assertTrue(validateMapping < loadMapping,
                "ValidateSaltFilter must run before LoadSaltFilter");
    }

    /**
     * The comment form on a weblog carries no salt. It posts to the entry's
     * permalink, which is outside /roller-ui, and RequestMappingFilter forwards
     * that to the comment servlet under /roller-ui/rendering. ValidateSaltFilter
     * must stay off forwards, or it would reject the comment of anyone with a
     * session: a signed-in user, or a visitor the comment authenticator gave one.
     */
    @Test
    public void testSubmittedSaltIsNotValidatedOnForwards() throws Exception {
        Path projectDirectory = Path.of(
                System.getProperty("project.build.directory")).getParent();
        String webXml = Files.readString(projectDirectory.resolve(
                "src/main/webapp/WEB-INF/web.xml"));

        int start = filterMappingPosition(webXml, "ValidateSaltFilter");
        assertTrue(start >= 0, "ValidateSaltFilter mapping is missing");
        String mapping = webXml.substring(start,
                webXml.indexOf("</filter-mapping>", start));

        assertFalse(mapping.contains("<dispatcher>FORWARD</dispatcher>"),
                "ValidateSaltFilter must not run on forwards, comment posts have no salt");
    }

    @Test
    public void testMultipartSaltValidationImmediatelyFollowsExceptionInterceptor() throws Exception {
        String strutsXml = readResource("/struts.xml");

        Pattern adjacentInterceptors = Pattern.compile(
                "<interceptor-ref name=\"exception\"/>\\s*"
                + "<interceptor-ref name=\"ValidateSaltInterceptor\"/>");

        assertTrue(adjacentInterceptors.matcher(strutsXml).find(),
                "ValidateSaltInterceptor must immediately follow the exception interceptor");
    }

    private int filterMappingPosition(String webXml, String filterName) {
        Pattern pattern = Pattern.compile("<filter-mapping>\\s*<filter-name>"
                + Pattern.quote(filterName) + "</filter-name>");
        Matcher matcher = pattern.matcher(webXml);
        return matcher.find() ? matcher.start() : -1;
    }

    private String readResource(String path) throws IOException {
        try (java.io.InputStream stream =
                     SaltConfigurationTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Test resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
