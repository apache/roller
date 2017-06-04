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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.util;

import org.tightblog.WebloggerTest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test utilities.
 */
public class UtilitiesTest extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testRemoveHTML() {
        String test = "<br><br><p>a <b>bold</b> sentence with a <a href=\"http://example.com\">link</a></p>";
        String expect = "a bold sentence with a link";
        String result = Utilities.removeHTML(test);
        assertEquals(expect, result);
    }

    @Test
    public void testInsertLineBreaksIfMissing() {
        String convertLinesStart = "paragraph1\n\nparagraph2\nline2\nline3\n\nparagraph3";
        String convertLinesFormatted = "<p>paragraph1</p><p>paragraph2 line2 line3</p><p>paragraph3</p>";

        // reformat
        String output = Utilities.insertLineBreaksIfMissing(convertLinesStart);

        // make sure it turned out how we planned
        assertEquals(convertLinesFormatted, output);
    }

}
