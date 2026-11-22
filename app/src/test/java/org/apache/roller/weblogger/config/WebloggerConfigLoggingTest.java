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
package org.apache.roller.weblogger.config;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.roller.weblogger.util.LogCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebloggerConfigLoggingTest {

    @Test
    void propertyLookupsLogKeysWithoutValues() {
        String key = "comment.maxLength";
        String resolved = WebloggerConfig.getProperty(key);
        String missingKey = "test.missing.property";
        String fallback = "example fallback value";

        try (LogCapture log = new LogCapture(WebloggerConfig.class, Level.DEBUG)) {
            assertEquals(resolved, WebloggerConfig.getProperty(key));
            assertEquals(resolved, WebloggerConfig.getProperty(key, fallback));
            assertEquals(fallback, WebloggerConfig.getProperty(missingKey, fallback));

            List<String> messages = log.events().stream()
                    .map(event -> event.getMessage().getFormattedMessage())
                    .collect(Collectors.toList());
            assertTrue(messages.stream().anyMatch(message -> message.contains(key)));
            assertTrue(messages.stream().anyMatch(message -> message.contains(missingKey)));
            assertFalse(messages.stream().anyMatch(message -> message.contains(resolved)
                    || message.contains(fallback)));
        }
    }
}
