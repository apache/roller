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

package org.apache.roller.weblogger.business.plugins.entry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Converts ASCII emoticons into Unicode emoji.
 *
 * <p>Each emoticon listed in <code>smileys.properties</code> becomes
 * <code>&lt;span class="smiley" title=":-)"&gt;&amp;#x1F642;&lt;/span&gt;</code>,
 * so no image files are needed and the emoji scales with the text around it.
 */
public class SmileysPlugin implements WeblogEntryPlugin {

    private static final Log log = LogFactory.getLog(SmileysPlugin.class);

    private static final String NAME = "Emoticons";
    private static final String DESCRIPTION =
            "Change ASCII emoticons to emoji. :-) becomes a smiling face.";

    /** Matches any of the emoticons, longest first. */
    private static final Pattern SMILEY_PATTERN;

    /** The markup that replaces each emoticon. */
    private static final Map<String, String> REPLACEMENTS;

    static {
        Properties smileyDefs = new Properties();
        try (InputStream in = SmileysPlugin.class.getResourceAsStream("smileys.properties")) {
            if (in == null) {
                throw new IOException("smileys.properties is not on the classpath");
            }
            smileyDefs.load(in);
        } catch (IOException e) {
            log.error("Unable to load smileys.properties", e);
        }

        Map<String, String> replacements = new HashMap<>();
        for (String smiley : smileyDefs.stringPropertyNames()) {
            String codePoint = smileyDefs.getProperty(smiley).trim();
            String title = StringEscapeUtils.escapeHtml4(smiley);
            replacements.put(smiley, "<span class=\"smiley\" title=\"" + title + "\">&#x"
                    + codePoint + ";</span>");
        }
        REPLACEMENTS = Collections.unmodifiableMap(replacements);

        // One pass with the longest emoticons tried first, so that ":))" is
        // not read as ":)" followed by ")", and markup that has already been
        // inserted is never matched again
        List<String> smileys = new ArrayList<>(replacements.keySet());
        smileys.sort((a, b) -> b.length() != a.length()
                ? b.length() - a.length() : a.compareTo(b));
        SMILEY_PATTERN = smileys.isEmpty() ? null : Pattern.compile(
                smileys.stream().map(Pattern::quote).collect(Collectors.joining("|")));
    }


    public SmileysPlugin() {
        log.debug("SmileysPlugin instantiated.");
    }


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public String getDescription() {
        return StringEscapeUtils.escapeEcmaScript(DESCRIPTION);
    }


    @Override
    public void init(Weblog website) throws WebloggerException {
        // nothing to do, the emoticons are loaded once when the class loads
    }


    /**
     * Find occurrences of ASCII emoticons and turn them into emoji.
     */
    @Override
    public String render(WeblogEntry entry, String text) {
        if (SMILEY_PATTERN == null || text == null) {
            return text;
        }
        Matcher matcher = SMILEY_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        while (matcher.find()) {
            matcher.appendReplacement(result,
                    Matcher.quoteReplacement(REPLACEMENTS.get(matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
