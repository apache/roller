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

package org.apache.roller.weblogger.ui.rendering.servlets;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentServletTest {

    @Test
    void shippedMaxLengthMatchesDefault() {
        assertEquals(CommentServlet.DEFAULT_MAX_CONTENT_LENGTH,
                WebloggerConfig.getIntProperty("comment.maxLength"));
    }

    @Test
    void contentUpToMaxLengthIsAccepted() {
        assertFalse(CommentServlet.isTooLong(null, 10));
        assertFalse(CommentServlet.isTooLong("", 10));
        assertFalse(CommentServlet.isTooLong("x".repeat(10), 10));
    }

    @Test
    void contentOverMaxLengthIsRefused() {
        assertTrue(CommentServlet.isTooLong("x".repeat(11), 10));
    }
}
