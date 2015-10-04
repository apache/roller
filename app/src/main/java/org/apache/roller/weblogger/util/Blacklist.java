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
package org.apache.roller.weblogger.util;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows callers to test strings against site-level and/or weblog-level
 * defined blacklist terms.
 * <br />
 * Blacklist is formatted one entry per line.
 * Any line that begins with # is considered to be a comment. 
 * Any line that begins with ( is considered to be a regex expression. 
 * <br />
 */
public final class Blacklist {
    
    private static Log mLogger = LogFactory.getLog(Blacklist.class);

    /** Hide constructor */
    private Blacklist() {
    }

    /**
     * Does the String argument match any of the rules in the blacklists provided by caller?
     * @param str String to be checked against blacklist
     * @param stringRules String rules to consider
     * @param regexRules  Regex rules to consider
     */
    public static boolean isBlacklisted(String str, List<String> stringRules, List<Pattern> regexRules) {
        return !StringUtils.isEmpty(str) && (testStringRules(str, stringRules) || testRegExRules(str, regexRules));
    }

    /** Test String against the RegularExpression rules. */
    private static boolean testRegExRules(String str, List<Pattern> regexRules) {
        for (Pattern testPattern : regexRules) {
            // want to see what it is matching on, but only in debug mode
            if (mLogger.isDebugEnabled()) {
                Matcher matcher = testPattern.matcher(str);
                if (matcher.find()) {
                    mLogger.debug(matcher.group() + " matched by " + testPattern.pattern());
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
     * @param rules A list a simple matching rules.
     *
     * @return true if a match was found, otherwise false
     */
    private static boolean testStringRules(String source, List<String> rules) {
        boolean matches = false;
        
        for (Object ruleObj : rules) {
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
                    mLogger.debug("matched:" + rule + ":");
                    break;
                }
            }
            catch (PatternSyntaxException e) {
                matches = source.contains(rule);
                if (matches) {
                    mLogger.debug("matched:" + rule + ":");
                    break;
                }
            }
        }
        
        return matches;
    }   
    
    /** Utility method to populate lists based a blacklist in string form
     * @param source1 String of string and/or regEx rules (e.g., weblog list), can be null
     * @param source2 Another string of string and/or regEx rules (e.g., site-wide list), can be null
     * @param stringRules List (can be non-empty) to append found string rules to.
     * @param regexRules List (can be non-empty) to append regex rules to.
     **/
    public static void populateSpamRules(
            String source1, String source2, List<String> stringRules, List<Pattern> regexRules) {
        String source1words = (source1 != null) ? source1 : "";
        String source2words = (source2 != null) ? source2 : "";
        StringTokenizer toker = new StringTokenizer(source2words + "\n" + source1words, "\n");
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
