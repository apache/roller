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
package org.apache.roller.selenium.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BootstrapTokenPageTest {

    @Test
    public void readsLatestBoxedToken() {
        String first = "a".repeat(43);
        String latest = "_-" + "b".repeat(41);
        String log = box(first, "\n") + box(latest, "\r\n");
        assertEquals(latest, BootstrapTokenPage.latestToken(log));
    }

    @Test
    public void ignoresUnrelatedOrIncompleteLogMessages() {
        assertNull(BootstrapTokenPage.latestToken("| " + "a".repeat(43) + " |\n"));
        assertNull(BootstrapTokenPage.latestToken(
                "Enter this one-time setup token (expires in 60 minutes): |\n| partial"));
    }

    private static String box(String token, String newline) {
        return "WARN BootstrapSecurity -" + newline
                + "| Enter this one-time setup token (expires in 60 minutes): |" + newline
                + "| " + token + "            |" + newline;
    }
}
