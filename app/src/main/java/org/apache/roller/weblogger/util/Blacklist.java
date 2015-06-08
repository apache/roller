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

import java.util.ArrayList;
import java.util.LinkedList;
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

    private static Blacklist blacklist;
    private List<String> blacklistStr = new LinkedList<String>();
    private List<Pattern> blacklistRegex = new LinkedList<Pattern>();

    // setup our singleton at class loading time
    static {
        blacklist = new Blacklist();
    }

    /** Hide constructor */
    private Blacklist() {
    }

    /** Singleton factory method. */
    public static Blacklist getBlacklist() {
        return blacklist;
    }

    /**
     * Does the String argument match any of the rules in the built-in blacklist? 
     */
/*
    public boolean isBlacklisted(String str) {
        return isBlacklisted(str, null, null);
    }
*/
    /** 
     * Does the String argument match any of the rules in the blacklists
     * provided by caller?
     * @param str             String to be checked against blacklist
     * @param moreStringRules Additional string rules to consider
     * @param moreRegexRules  Additional regex rules to consider 
     */
    public boolean isBlacklisted(
         String str, List<String> moreStringRules, List<Pattern> moreRegexRules) {
        if (str == null || StringUtils.isEmpty(str)) {
            return false;
        }

        // First iterate over blacklist, doing indexOf.
        // Then iterate over blacklistRegex and test.
        // As soon as there is a hit in either case return true
        
        // test plain String.indexOf
        List<String> stringRules = blacklistStr;
        if (moreStringRules != null && moreStringRules.size() > 0) {
            stringRules = new ArrayList<String>();
            stringRules.addAll(moreStringRules);
        }
        if (testStringRules(str, stringRules)) {
            return true;
        }
        
        // test regex blacklisted
        List<Pattern> regexRules = blacklistRegex;
        if (moreRegexRules != null && moreRegexRules.size() > 0) {
            regexRules = new ArrayList<Pattern>();
            regexRules.addAll(moreRegexRules);
        }
        return testRegExRules(str, regexRules);
    }      

    /** 
     * Test string only against rules provided by caller, NOT against built-in blacklist.
     * @param str             String to be checked against rules
     * @param stringRules String rules to consider
     * @param regexRules  Regex rules to consider
     */
    public static boolean matchesRulesOnly(
        String str, List<String> stringRules, List<Pattern> regexRules) {
        return testStringRules(str, stringRules) || testRegExRules(str, regexRules);
    }
        
    /** Test String against the RegularExpression rules. */
    private static boolean testRegExRules(String str, List<Pattern> regexRules) {
        for (Pattern testPattern : regexRules) {
            // want to see what it is matching on, but only in debug mode
            if (mLogger.isDebugEnabled()) {
                Matcher matcher = testPattern.matcher(str);
                if (matcher.find()) {
                    mLogger.debug(matcher.group()
                            + " matched by " + testPattern.pattern());
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
                    break;
                }
            }
            catch (PatternSyntaxException e) {
                matches = source.contains(rule);
                if (matches) {
                    break;
                }
            }
            finally {
                if (matches && mLogger.isDebugEnabled()) {
                    // Log the matched rule in debug mode
                    mLogger.debug("matched:" + rule + ":");
                }
            }
        }
        
        return matches;
    }   
    
    /** Utility method to populate lists based a blacklist in string form */
    public static void populateSpamRules(
        String blacklist, List<String> stringRules, List<Pattern> regexRules, String addendum) {
        String weblogWords = blacklist;
        weblogWords = (weblogWords == null) ? "" : weblogWords;
        String siteWords = (addendum != null) ? addendum : "";
        StringTokenizer toker = new StringTokenizer(siteWords + "\n" + weblogWords, "\n");
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
        
    /** Return pretty list of String and RegEx rules. */
    public String toString() {
        String val = "blacklist " + blacklistStr;
        val += "\nRegex blacklist " + blacklistRegex;
        return val;
    }
}
