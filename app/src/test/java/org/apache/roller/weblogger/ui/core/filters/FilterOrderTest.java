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
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.core.filters;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterOrderTest {

    @Test
    void persistenceSessionFilterWrapsEveryOtherFilter() throws Exception {
        String webXml = new String(Files.readAllBytes(Paths.get("src/main/webapp/WEB-INF/web.xml")),
                StandardCharsets.UTF_8);
        List<String> order = filterMappingOrder(webXml);
        // Spring Security reads users for logins and answers those requests
        // itself, so the filter that releases the persistence session has to
        // run around it
        assertEquals("PersistenceSessionFilter", order.get(0), order.toString());
    }

    @Test
    void commentedOutMappingsAreIgnored() {
        String webXml = "<web-app>\n"
                + "  <!-- <filter-mapping>\n"
                + "    <filter-name>OldFilter</filter-name>\n"
                + "  </filter-mapping> -->\n"
                + "  <filter-mapping>\n"
                + "    <filter-name>FirstFilter</filter-name>\n"
                + "  </filter-mapping>\n"
                + "</web-app>\n";
        assertEquals(Collections.singletonList("FirstFilter"), filterMappingOrder(webXml));
    }

    // the filter names of web.xml's filter mappings, in order, leaving out
    // any that are commented out
    private static List<String> filterMappingOrder(String webXml) {
        String uncommented = webXml.replaceAll("(?s)<!--.*?-->", "");
        Matcher mapping = Pattern.compile("<filter-mapping>\\s*<filter-name>([^<]+)</filter-name>")
                .matcher(uncommented);
        List<String> order = new ArrayList<>();
        while (mapping.find()) {
            order.add(mapping.group(1).trim());
        }
        return order;
    }
}
