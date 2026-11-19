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

package org.apache.roller.weblogger.util;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class HTMLSanitizerTimeLimitTest {

    @Test
    void slowMarkupIsStoppedAndEscaped() {
        String[] inputs = {
            // each of these took the sanitizer 40 seconds or more
            "<p style=\"" + "a:b ".repeat(20_000) + "\">x</p>",
            "<p style=\"background:url(" + "'".repeat(50_000) + "\">x</p>",
            "<b" + " ".repeat(50_000) + "x",
            "<b>x</b>".repeat(400_000),
        };
        for (String input : inputs) {
            String html = assertTimeoutPreemptively(Duration.ofSeconds(8),
                    () -> HTMLSanitizer.sanitize(input));
            assertFalse(html.contains("<"), start(html));
        }
    }

    @Test
    void closingTagsLeftOpenIsStopped() {
        assertStoppedAtTheLimit("");
    }

    @Test
    void closingTagsInsideAClosedTagIsStopped() {
        assertStoppedAtTheLimit("</i>");
    }

    /**
     * Sanitizes more and more open tags, until that takes too long and they
     * are escaped. Whatever the machine's speed, one of the sizes leaves the
     * tags to be closed when the time is nearly up, and closing them must not
     * run past the limit.
     */
    private static void assertStoppedAtTheLimit(String end) {
        // the margin covers a pause on a busy build machine; closing the
        // tags without the limit ran over by more than this
        Duration allowed = Duration.ofMillis(HTMLSanitizer.TIME_LIMIT_MILLIS + 1000);
        String html = "<";
        for (int n = 20_000; html.contains("<"); n += n / 10) {
            String input = "<i>" + "<b>".repeat(n) + end;
            html = assertTimeoutPreemptively(allowed, () -> HTMLSanitizer.sanitize(input),
                    n + " open tags");
        }
    }

    private static String start(String html) {
        return html.substring(0, Math.min(40, html.length()));
    }

    @Test
    void ordinaryMarkupIsStillSanitized() {
        assertEquals("<p>Hi <b>there</b></p>alert(1)",
                HTMLSanitizer.sanitize("<p>Hi <b>there</b></p><script>alert(1)</script>"));
    }
}
