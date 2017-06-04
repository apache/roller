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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows callers to test strings against site-level and weblog-level defined blacklist terms.
 * <br />
 * Blacklist is formatted one entry per line.
 * Any line that begins with # is considered to be a comment.
 * Any line that begins with ( is considered to be a regex expression.
 * <br />
 */
public final class Blacklist {
    private static Logger log = LoggerFactory.getLogger(Blacklist.class);

    private List<String> stringRules = new ArrayList<>();
    private List<Pattern> regexRules = new ArrayList<>();

    public Blacklist(String weblogBlacklist, Blacklist parentBlacklist) {
        populateSpamRules(weblogBlacklist, parentBlacklist);
    }

    public boolean isBlacklisted(String str) {
        return !StringUtils.isEmpty(str) && (testStringRules(str) || testRegExRules(str));
    }

    private List<String> getStringRules() {
        return stringRules;
    }

    private List<Pattern> getRegexRules() {
        return regexRules;
    }

    /**
     * Test String against the RegularExpression rules.
     */
    private boolean testRegExRules(String str) {
        for (Pattern testPattern : regexRules) {
            // want to see what it is matching on, but only in debug mode
            if (log.isDebugEnabled()) {
                Matcher matcher = testPattern.matcher(str);
                if (matcher.find()) {
                    log.debug("{} matched by {}", matcher.group(), testPattern.pattern());
                    return true;
                }
            } else {
                if (testPattern.matcher(str).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests the source text against the String rules. Each String rule is
     * first treated as a word-boundary, case insensitive regular expression.
     * If a PatternSyntaxException is encountered, a simple contains test
     * is performed.
     *
     * @param source The text in which to apply the matching rules.
     * @return true if a match was found, otherwise false
     */
    private boolean testStringRules(String source) {
        boolean matches = false;

        for (Object ruleObj : stringRules) {
            String rule;
            rule = (String) ruleObj;

            try {
                StringBuilder patternBuilder;
                patternBuilder = new StringBuilder();
                patternBuilder.append("\\b(");
                patternBuilder.append(rule);
                patternBuilder.append(")\\b");

                Pattern pattern;
                pattern = Pattern.compile(patternBuilder.toString(),
                        Pattern.CASE_INSENSITIVE);

                Matcher matcher;
                matcher = pattern.matcher(source);

                matches = matcher.find();
                if (matches) {
                    log.debug("matched: {}:", rule);
                    break;
                }
            } catch (PatternSyntaxException e) {
                matches = source.contains(rule);
                if (matches) {
                    log.debug("matched: {}:", rule);
                    break;
                }
            }
        }

        return matches;
    }

    /**
     * Populate rules for this instance
     *
     * @param weblogBlacklist String of string and/or regEx rules
     * @param parentBlacklist Optional blacklist whose elements are to be added to this instance.
     **/
    private void populateSpamRules(String weblogBlacklist, Blacklist parentBlacklist) {
        if (weblogBlacklist == null) {
            weblogBlacklist = "";
        }

        if (parentBlacklist != null) {
            regexRules.addAll(parentBlacklist.getRegexRules());
            stringRules.addAll(parentBlacklist.getStringRules());
        }

        StringTokenizer toker = new StringTokenizer(weblogBlacklist, "\n");

        while (toker.hasMoreTokens()) {
            String token = toker.nextToken().trim();
            if (token.startsWith("#")) {
                continue;
            }
            if (token.startsWith("(")) {
                regexRules.add(Pattern.compile(token));
            } else {
                stringRules.add(token);
            }
        }
    }

}
