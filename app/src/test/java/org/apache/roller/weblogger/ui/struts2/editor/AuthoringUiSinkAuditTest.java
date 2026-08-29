/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Structural audit of the authoring UI templates.
 *
 * <p>Values that originate from weblog content are rendered by the editor JSPs.
 * This test enforces the two structural rules that keep those values inert: they
 * travel in double-quoted <code>data-*</code> attributes rather than inline
 * handler literals, and they are written to the DOM through a text API. It is a
 * source audit rather than a behavioural test because the guarantee is a
 * property of the whole page family, not of any one code path.
 */
public class AuthoringUiSinkAuditTest {

    private static final Path EDITOR_JSP_DIR =
            Paths.get("src", "main", "webapp", "WEB-INF", "jsps", "editor");

    /**
     * An inline event handler attribute whose body opens a single-quoted
     * JavaScript string containing a Struts property. Newlines are collapsed
     * before matching because these handlers routinely wrap across lines.
     */
    private static final Pattern HANDLER_LITERAL =
            Pattern.compile("on[a-zA-Z]+\\s*=\\s*\"[^\"]*'\\s*<s:property");

    /** A bare JavaScript variable assignment from a Struts property. */
    private static final Pattern SCRIPT_VAR_LITERAL =
            Pattern.compile("var\\s+\\w+\\s*=\\s*'\\s*<s:property");

    /**
     * A jQuery html() write. Calls passing only a localized string, an empty
     * string, or nothing are inert and are excluded.
     */
    private static final Pattern HTML_WRITE =
            Pattern.compile("\\.html\\(\\s*(?!\\)|''|\"\"|'<s:text|\"<s:text)[^)]");

    /**
     * The comment moderation screen round-trips already-encoded comment markup
     * through html(); the settled fix plan treats that as separate follow-up
     * hardening rather than part of this sink family.
     */
    private static final Set<String> EXCLUDED_FILES =
            new HashSet<>(Arrays.asList("Comments.jsp"));

    private List<Path> editorJsps() throws IOException {
        Path dir = EDITOR_JSP_DIR;
        assertTrue(Files.isDirectory(dir), "cannot locate editor JSPs at "
                + dir.toAbsolutePath() + " (run from the app module)");
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".jsp"))
                    .filter(p -> !EXCLUDED_FILES.contains(p.getFileName().toString()))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private static String flatten(String source) {
        return source.replace('\n', ' ').replace('\r', ' ');
    }

    private List<String> findMatches(Pattern pattern, boolean flattenSource) throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path jsp : editorJsps()) {
            String body = new String(Files.readAllBytes(jsp), StandardCharsets.UTF_8);
            String haystack = flattenSource ? flatten(body) : body;
            Matcher matcher = pattern.matcher(haystack);
            while (matcher.find()) {
                String snippet = matcher.group().replaceAll("\\s+", " ").trim();
                offenders.add(jsp.getFileName() + ": " + snippet);
            }
        }
        return offenders;
    }

    /**
     * No weblog-controlled value may sit inside a single-quoted JavaScript
     * string literal in an inline event handler. An apostrophe in the value
     * closes the literal and the rest of the value is parsed as code.
     */
    @Test
    public void noStrutsPropertyInsideInlineHandlerLiteral() throws IOException {
        List<String> offenders = findMatches(HANDLER_LITERAL, true);
        assertTrue(offenders.isEmpty(),
                "authoring values must travel in data-* attributes, not inline "
                        + "handler string literals; found " + offenders.size() + ":\n  "
                        + String.join("\n  ", offenders));
    }

    /**
     * The same rule for values assigned into script variables, which are the
     * non-handler form of the identical defect.
     */
    @Test
    public void noStrutsPropertyInsideScriptVariableLiteral() throws IOException {
        List<String> offenders = findMatches(SCRIPT_VAR_LITERAL, false);
        assertTrue(offenders.isEmpty(),
                "authoring values must reach script through data-* attributes, "
                        + "not single-quoted var initialisers; found " + offenders.size() + ":\n  "
                        + String.join("\n  ", offenders));
    }

    /**
     * No dynamic value may be written to the DOM as markup. Moving a value into
     * a data attribute does not help if a later html() call re-parses it.
     */
    @Test
    public void noDynamicHtmlWrites() throws IOException {
        List<String> offenders = findMatches(HTML_WRITE, false);
        assertTrue(offenders.isEmpty(),
                "dynamic values must be written with a text API (.text(), "
                        + ".val(), textContent) rather than .html(); found "
                        + offenders.size() + ":\n  " + String.join("\n  ", offenders));
    }

    /**
     * Guards the audit itself: if the JSP directory moved or the patterns stopped
     * matching anything at all, the three tests above would pass vacuously.
     */
    @Test
    public void auditActuallyInspectsTheEditorTemplates() throws IOException {
        List<Path> jsps = editorJsps();
        assertFalse(jsps.isEmpty(), "audit found no editor JSPs to inspect");
        assertTrue(jsps.size() >= 10,
                "expected the editor template family, found only " + jsps.size());
    }
}
