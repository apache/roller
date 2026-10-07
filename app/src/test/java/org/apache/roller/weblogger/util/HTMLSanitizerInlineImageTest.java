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
package org.apache.roller.weblogger.util;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HTMLSanitizerInlineImageTest {

    @Test
    void keepsValidatedImageData() {
        String html = HTMLSanitizer.sanitize("<img src=\"" + InlineImageDataTest.PNG + "\">");
        assertTrue(html.contains(InlineImageDataTest.PNG), html);
    }

    @Test
    void removesOtherDataUrls() {
        String svg = "data:image/svg+xml;base64,"
                + Base64.getEncoder().encodeToString("<svg onload=alert(1)/>".getBytes());
        assertFalse(HTMLSanitizer.sanitize("<img src=\"" + svg + "\">").contains("data:"));
        assertFalse(HTMLSanitizer.sanitize(
                "<img src=\"data:text/html;base64,PHNjcmlwdD4=\">").contains("data:"));
        assertFalse(HTMLSanitizer.sanitize(
                "<img src=\"data:image/png;base64,R0lGODlhAQ==\">").contains("data:"));
    }

    @Test
    void removesImageDataOutsideImgTags() {
        assertFalse(HTMLSanitizer.sanitize(
                "<embed src=\"" + InlineImageDataTest.PNG + "\">").contains("data:"));
    }
}
