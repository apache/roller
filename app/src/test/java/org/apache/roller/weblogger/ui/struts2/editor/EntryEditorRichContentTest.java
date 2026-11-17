/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Summernote runs event handlers and scripts in the HTML it loads, so the rich
 * editor in EntryEditor.jsp checks the entry with rich-editor-content.js first
 * and opens entries that fail the check in the textarea. Summernote also starts
 * itself on any call, so each call must be behind the same check. The check
 * itself is verified in a browser; this keeps the page wired to it.
 */
class EntryEditorRichContentTest {

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    void summernoteStartsOnlyAfterTheContentCheck() throws IOException {
        String jsp = read("src/main/webapp/WEB-INF/jsps/editor/EntryEditor.jsp");
        assertTrue(jsp.contains("/roller-ui/scripts/rich-editor-content.js"));

        int check = jsp.indexOf("RollerRichEditor.hasActiveHtml(");
        int start = jsp.indexOf("richEditorStarted = true;");
        int init = jsp.indexOf("$('#edit_content').summernote({");
        assertTrue(check > 0 && check < start && start < init, "check, then start, then init");

        // every other call is guarded by the flag
        Matcher call = Pattern.compile("\\.summernote\\(\\s*\"").matcher(jsp);
        int calls = 0;
        while (call.find()) {
            calls++;
            String before = jsp.substring(Math.max(0, call.start() - 120), call.start());
            assertTrue(before.contains("if (richEditorStarted)"), "unguarded call near: " + before);
        }
        assertEquals(1, calls);
    }

    @Test
    void contentCheckScriptExists() throws IOException {
        String script = read("src/main/webapp/roller-ui/scripts/rich-editor-content.js");
        assertTrue(script.contains("function hasActiveHtml(html)"));
        assertTrue(script.contains("DOMParser"));
    }
}
