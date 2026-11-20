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
/* Created on Nov 11, 2003 */
package org.apache.roller.weblogger.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.util.DateUtil;

/**
 * Loads MT-Bannedwordslist style bannedwordslist from disk and allows callers to test
 * strings against the bannedwordslist and (optionally) addition bannedwordslists.
 * <br />
 * First looks for bannedwordslist.txt in uploads directory, than in classpath
 * as /bannedwordslist.txt. Download from web feature disabled.
 * <br />
 * Bannedwordslist is formatted one entry per line.
 * Any line that begins with # is considered to be a comment. 
 * Any line that begins with ( is considered to be a regex expression. 
 * <br />
 * For more information on the (discontinued) MT-Bannedwordslist service:
 * http://www.jayallen.org/projects/mt-bannedwordslist.
 *
 * @author Lance Lavandowska
 * @author Allen Gilliland
 */
public final class Bannedwordslist {
    
    private static final Log mLogger = LogFactory.getLog(Bannedwordslist.class);
    
    private static final Bannedwordslist bannedwordslist;
    private static final String BANNEDWORDSLIST_FILE = "bannedwordslist.txt";
    private static final String LAST_UPDATE_STR = "Last update:";

    /** We no longer have a bannedwordslist update URL */
    private static final String BANNEDWORDSLIST_URL = null;

    private Date lastModified = null;
    private final List<String> bannedwordslistStr = new ArrayList<>();
    private final List<Pattern> bannedwordslistRegex = new ArrayList<>();
    
    // setup our singleton at class loading time
    static {
        mLogger.info("Initializing MT Bannedwordslist");
        bannedwordslist = new Bannedwordslist();
        bannedwordslist.loadBannedwordslistFromFile(null);
    }
    
    /** Hide constructor */
    private Bannedwordslist() {
    }
      
    /** Singleton factory method. */
    public static Bannedwordslist getBannedwordslist() {
        return bannedwordslist;
    }
    
    /** Non-Static update method. */
    public void update() {
        if (BANNEDWORDSLIST_URL != null) {
            boolean bannedwordslist_updated = this.downloadBannedwordslist();
            if (bannedwordslist_updated) {
                this.loadBannedwordslistFromFile(null);
            }
        }
    }
        
    /** Download the MT bannedwordslist from the web to our uploads directory. */
    private boolean downloadBannedwordslist() {
        
        boolean bannedwordslistUpdated = false;
        try {
            mLogger.debug("Attempting to download MT bannedwordslist");
            
            URL url = new URL(BANNEDWORDSLIST_URL);
            HttpURLConnection connection = 
                    (HttpURLConnection) url.openConnection();
            
            // after spending way too much time debugging i've discovered
            // that the bannedwordslist server is selective based on the User-Agent
            // header.  without this header set i always get a 403 response :(
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            if (this.lastModified != null) {
                connection.setRequestProperty("If-Modified-Since",
                        DateUtil.formatRfc822(this.lastModified));
            }
            
            int responseCode = connection.getResponseCode();
            
            mLogger.debug("HttpConnection response = "+responseCode);
            
            // did the connection return NotModified? If so, no need to parse
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                mLogger.debug("MT bannedwordslist site says we are current");
                return false;
            }
            
            // did the connection return a LastModified header?
            long lastModifiedLong = 
                    connection.getHeaderFieldDate("Last-Modified", -1);
            
            // if the file is newer than our current then we need do update it
            if (responseCode == HttpURLConnection.HTTP_OK &&
                    (this.lastModified == null ||
                    this.lastModified.getTime() < lastModifiedLong)) {

                mLogger.debug("my last modified = " + (this.lastModified == null ? "(null)" :
                        this.lastModified.getTime()));
                mLogger.debug("MT last modified = " + lastModifiedLong);
                
                // save the new bannedwordslist
                try (InputStream instream = connection.getInputStream()) {
                    String uploadDir = WebloggerConfig.getProperty("uploads.dir");
                    String path = uploadDir + File.separator + BANNEDWORDSLIST_FILE;
                    
                    mLogger.debug("writing updated MT bannedwordslist to "+path);
                    
                    // read from url and write to file
                    try (FileOutputStream outstream = new FileOutputStream(path)) {
                        instream.transferTo(outstream);
                    }
                }
                
                bannedwordslistUpdated = true;
                
                mLogger.debug("MT bannedwordslist download completed.");
                
            } else {
                mLogger.debug("bannedwordslist *NOT* saved, assuming we are current");
            }
            
        } catch (Exception e) {
            mLogger.error("error downloading bannedwordslist", e);
        }
        
        return bannedwordslistUpdated;
    }
        
    /**
     * Load the MT bannedwordslist from the file system.
     * We look for a previously downloaded version of the bannedwordslist first and
     * if it's not found then we load the default bannedwordslist packed with Roller.
     * Only public for purposes of unit testing.
     */
    public void loadBannedwordslistFromFile(String bannedwordslistFilePath) {
        
        InputStream txtStream;
        try {
            String path = bannedwordslistFilePath;
            if (path == null) {
                String uploadDir = WebloggerConfig.getProperty("uploads.dir");
                path = uploadDir + File.separator + BANNEDWORDSLIST_FILE;
            }
            File bannedwordslistFile = new File(path);
            
            // check our lastModified date to see if we need to re-read the file
            if (this.lastModified != null &&
                    this.lastModified.getTime() >= bannedwordslistFile.lastModified()) {
                mLogger.debug("Bannedwordslist is current, no need to load again");
                return;
            } else {
                this.lastModified = new Date(bannedwordslistFile.lastModified());
            }           
            txtStream = new FileInputStream(bannedwordslistFile);
            mLogger.info("Loading bannedwordslist from "+path);
            
        } catch (Exception e) {
            // Roller keeps a copy in the webapp just in case
            txtStream = getClass().getResourceAsStream("/bannedwordslist.txt");
            mLogger.warn(
                "Couldn't find downloaded bannedwordslist, loaded bannedwordslist.txt from classpath instead");
        }
        
        if (txtStream != null) {
            readFromStream(txtStream, false);
        } else {
            mLogger.error("Couldn't load a bannedwordslist file from anywhere, "
                        + "this means bannedwordslist checking is disabled for now.");
        }
        mLogger.info("Number of bannedwordslist string rules: "+bannedwordslistStr.size());
        mLogger.info("Number of bannedwordslist regex rules: "+bannedwordslistRegex.size());
    }
       
    /**
     * Read in the InputStream for rules.
     * @param txtStream stream to read from
     */
    private String readFromStream(InputStream txtStream, boolean saveStream) {
        String line;
        StringBuilder buf = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader( txtStream, StandardCharsets.UTF_8) );
            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) {
                    readComment(line);
                } else {
                    readRule(line);
                }
                
                if (saveStream) {
                    buf.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            mLogger.error(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e1) {
                mLogger.error(e1);
            }
        }
        return buf.toString();
    }
    
    private void readRule(String str) {
        // check for bad condition
        if (StringUtils.isEmpty(str)) {
            return;
        }
        
        String rule = str.trim();

        // line has a comment?
        if (str.indexOf('#') > 0) {
            int commentLoc = str.indexOf('#');
            // strip comment
            rule = str.substring(0, commentLoc-1).trim();
        }

        // regex rule?
        if (rule.indexOf( '(' ) > -1) {
            // pre-compile patterns since they will be frequently used
            bannedwordslistRegex.add(Pattern.compile(rule));
        } else if (StringUtils.isNotEmpty(rule)) {
            bannedwordslistStr.add(rule);
        }
    }
        
    /** Read comment and try to parse out "Last update" value */
    private void readComment(String str) {
        int lastUpdatePos = str.indexOf(LAST_UPDATE_STR);
        if (lastUpdatePos > -1) {
            str = str.substring(lastUpdatePos + LAST_UPDATE_STR.length());
            str = str.trim();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                lastModified = DateUtil.parse(str, sdf);
            } catch (ParseException e) {
                mLogger.debug("ParseException reading " + str);
            }
        }
    }
       
    /** 
     * Does the String argument match any of the rules in the built-in bannedwordslist?
     */
    public boolean isBannedwordslisted(String str) {
        return isBannedwordslisted(str, null, null);
    }
    
    /** 
     * Does the String argument match any of the rules in the built-in bannedwordslist
     * plus additional bannedwordslists provided by caller?
     * @param str             String to be checked against bannedwordslist
     * @param moreStringRules Additional string rules to consider
     * @param moreRegexRules  Additional regex rules to consider 
     */
    public boolean isBannedwordslisted(
         String str, List<String> moreStringRules, List<Pattern> moreRegexRules) {
        if (str == null || StringUtils.isEmpty(str)) {
            return false;
        }

        // First iterate over bannedwordslist, doing indexOf.
        // Then iterate over bannedwordslistRegex and test.
        // As soon as there is a hit in either case return true
        
        // test plain String.indexOf
        List<String> stringRules = bannedwordslistStr;
        if (moreStringRules != null && !moreStringRules.isEmpty()) {
            stringRules = new ArrayList<>();
            stringRules.addAll(moreStringRules);
            stringRules.addAll(bannedwordslistStr);
        }
        try {
            if (testStringRules(str, stringRules)) {
                return true;
            }
        
            // test regex bannedwordslisted
            List<Pattern> regexRules = bannedwordslistRegex;
            if (moreRegexRules != null && !moreRegexRules.isEmpty()) {
                regexRules = new ArrayList<>();
                regexRules.addAll(moreRegexRules);
                regexRules.addAll(bannedwordslistRegex);
            }
            return testRegExRules(str, regexRules);
        } catch (RuleNotCheckedException e) {
            return true;
        }
    }

    /** 
     * Test string only against rules provided by caller, NOT against built-in bannedwordslist.
     * @param str             String to be checked against rules
     * @param stringRules String rules to consider
     * @param regexRules  Regex rules to consider
     */
    public static boolean matchesRulesOnly(
        String str, List<String> stringRules, List<Pattern> regexRules) {
        return matchesRulesOnly(str, stringRules, regexRules, true);
    }

    /**
     * Test string only against rules provided by caller, NOT against built-in bannedwordslist.
     * @param str             String to be checked against rules
     * @param stringRules     String rules to consider
     * @param regexRules      Regex rules to consider
     * @param uncheckedMatches what to return when a rule can't be checked in
     *                        time: true for a comment, which the weblog owner
     *                        can review, false for a referrer, which would
     *                        refuse the reader the page
     */
    public static boolean matchesRulesOnly(String str, List<String> stringRules,
            List<Pattern> regexRules, boolean uncheckedMatches) {
        if (str == null) {
            return false;
        }
        try {
            return testStringRules(str, stringRules) || testRegExRules(str, regexRules);
        } catch (RuleNotCheckedException e) {
            return uncheckedMatches;
        }
    }
        
    /** Test String against the RegularExpression rules. */
    private static boolean testRegExRules(String str, List<Pattern> regexRules) {
        for (Pattern testPattern : regexRules) {
            Matcher matcher = testPattern.matcher(new DeadlineText(str));
            if (find(matcher, testPattern.pattern())) {
                // want to see what it is matching on, but only in debug mode
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug(matcher.group()
                            + " matched by " + testPattern.pattern());
                }
                return true;
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
        
        for (String rule : rules) {

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
                matcher = pattern.matcher(new DeadlineText(source));

                matches = find(matcher, rule);
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
    
    /** Utility method to populate lists based a bannedwordslist in string form */
    public static void populateSpamRules(
        String bannedwordslist, List<String> stringRules, List<Pattern> regexRules, String addendum) {
        populateSpamRules(bannedwordslist, stringRules, regexRules, addendum, false);
    }

    /**
     * Populates the rule lists from a weblog's banned words and the site-wide
     * addendum. With weblogRulesAsWords, the weblog's own lines are matched as
     * plain words (case-insensitive, at word boundaries) rather than as
     * regular expressions, so an untrusted weblog admin can't write a rule
     * that backtracks; the addendum's rules are still regular expressions.
     * A regex rule that doesn't compile is logged and matched as a plain
     * word; use validateRules() to refuse it when the list is saved.
     */
    public static void populateSpamRules(String bannedwordslist, List<String> stringRules,
            List<Pattern> regexRules, String addendum, boolean weblogRulesAsWords) {
        if (weblogRulesAsWords) {
            readRules(addendum, stringRules, regexRules, false);
            for (String rule : ruleLines(bannedwordslist)) {
                regexRules.add(wordPattern(rule));
            }
        } else {
            String weblogWords = (bannedwordslist == null) ? "" : bannedwordslist;
            String siteWords = (addendum != null) ? addendum : "";
            readRules(siteWords + "\n" + weblogWords, stringRules, regexRules, false);
        }
    }

    /**
     * Checks that each regex rule in a banned words list compiles.
     * @throws PatternSyntaxException for the first one that doesn't
     */
    public static void validateRules(String bannedwordslist) {
        readRules(bannedwordslist, new ArrayList<>(), new ArrayList<>(), true);
    }

    /**
     * The lines of a weblog's banned words that only mean something as a
     * regular expression, so that a weblog admin can be told they are
     * matched as plain words.
     */
    public static List<String> regexStyleRules(String bannedwordslist) {
        List<String> rules = new ArrayList<>();
        for (String rule : ruleLines(bannedwordslist)) {
            if (rule.startsWith("(") || StringUtils.containsAny(rule, "|\\[*+?{^$")) {
                rules.add(rule);
            }
        }
        return rules;
    }

    /** The lines of a list that are rules, without comments or blank lines. */
    private static List<String> ruleLines(String rules) {
        List<String> lines = new ArrayList<>();
        StringTokenizer toker = new StringTokenizer(rules == null ? "" : rules, "\n");
        while (toker.hasMoreTokens()) {
            String token = stripSpaces(toker.nextToken());
            if (!token.isEmpty() && !token.startsWith("#")) {
                lines.add(token);
            }
        }
        return lines;
    }

    /**
     * Removes white space from both ends, including spaces such as U+00A0
     * and U+3000 that trim() keeps and that look blank in a text box.
     */
    private static String stripSpaces(String str) {
        int start = 0;
        int end = str.length();
        while (start < end && isSpace(str.charAt(start))) {
            start++;
        }
        while (end > start && isSpace(str.charAt(end - 1))) {
            end--;
        }
        return str.substring(start, end);
    }

    private static boolean isSpace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    private static void readRules(String rules, List<String> stringRules, List<Pattern> regexRules,
            boolean strict) {
        for (String token : ruleLines(rules)) {
            if (token.startsWith("(")) {
                try {
                    regexRules.add(Pattern.compile(token));
                } catch (PatternSyntaxException e) {
                    if (strict) {
                        throw e;
                    }
                    mLogger.warn("Banned words rule is not a valid regex and is matched as plain text: "
                            + token);
                    regexRules.add(wordPattern(token));
                }
            } else {
                stringRules.add(token);
            }
        }
    }

    /** A letter, combining mark, digit or underscore, in any script. */
    private static final String WORD_CHAR = "[\\p{L}\\p{M}\\p{N}_]";
    private static final Pattern WORD_CHAR_PATTERN = Pattern.compile(WORD_CHAR);

    /**
     * The literal text, case-insensitive in any script, with a word edge
     * check on each end that is a word character; a check next to "(" or
     * "/" would need a word character beside it and so would rarely match.
     * There is no check next to a letter of a script written without
     * spaces, since such a word is rarely at a word edge.
     */
    private static Pattern wordPattern(String word) {
        StringBuilder regex = new StringBuilder();
        if (needsEdgeCheck(word.codePointAt(0))) {
            regex.append("(?<!").append(WORD_CHAR).append(')');
        }
        regex.append(Pattern.quote(word));
        if (needsEdgeCheck(word.codePointBefore(word.length()))) {
            regex.append("(?!").append(WORD_CHAR).append(')');
        }
        return Pattern.compile(regex.toString(),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private static boolean needsEdgeCheck(int codePoint) {
        return WORD_CHAR_PATTERN.matcher(new String(Character.toChars(codePoint))).matches()
                && !SCRIPTS_WITHOUT_SPACES.contains(Character.UnicodeScript.of(codePoint));
    }

    /**
     * Scripts written without spaces between words, where a word is
     * usually next to other letters.
     */
    private static final Set<Character.UnicodeScript> SCRIPTS_WITHOUT_SPACES = EnumSet.of(
            Character.UnicodeScript.HAN, Character.UnicodeScript.HIRAGANA,
            Character.UnicodeScript.KATAKANA, Character.UnicodeScript.THAI,
            Character.UnicodeScript.LAO, Character.UnicodeScript.KHMER,
            Character.UnicodeScript.MYANMAR);

    /**
     * Longest one rule may take to check one value, such as a comment's
     * text. A regex rule that backtracks without end would otherwise hold
     * the request. The clock is checked as the match reads the text, so a
     * regex that repeats without reading it, such as nested counted repeats
     * of an empty group, isn't stopped; such rules come only from the
     * site-wide list or from trusted weblog admins.
     */
    static final long MATCH_TIME_LIMIT_MILLIS = 1000;

    private static boolean find(Matcher matcher, String rule) {
        try {
            return matcher.find();
        } catch (MatchTimeLimitException e) {
            mLogger.warn("Banned words rule took over " + MATCH_TIME_LIMIT_MILLIS
                    + " ms and the value could not be checked; consider simplifying it: " + rule);
            throw new RuleNotCheckedException();
        } catch (StackOverflowError e) {
            // a regex such as (a|b)+c recurses once per repetition
            mLogger.warn("Banned words rule ran out of stack and the value could not be checked;"
                    + " consider simplifying it: " + rule);
            throw new RuleNotCheckedException();
        }
    }

    private static final class MatchTimeLimitException extends RuntimeException {
        MatchTimeLimitException() {
            super(null, null, false, false);
        }
    }

    /** A rule could not be checked against the value, which then counts as a match for a comment. */
    private static final class RuleNotCheckedException extends RuntimeException {
        RuleNotCheckedException() {
            super(null, null, false, false);
        }
    }

    /** The checked text, which stops a match once the time limit has passed. */
    private static final class DeadlineText implements CharSequence {
        private final CharSequence text;
        private final long deadline;
        private int reads;

        DeadlineText(CharSequence text) {
            this(text, System.nanoTime() + MATCH_TIME_LIMIT_MILLIS * 1_000_000L);
        }

        private DeadlineText(CharSequence text, long deadline) {
            this.text = text;
            this.deadline = deadline;
        }

        @Override
        public char charAt(int index) {
            // checking the clock on every read would slow ordinary matches
            if ((++reads & 0x3FF) == 0 && System.nanoTime() - deadline > 0) {
                throw new MatchTimeLimitException();
            }
            return text.charAt(index);
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new DeadlineText(text.subSequence(start, end), deadline);
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }

    /** Return pretty list of String and RegEx rules. */
    @Override
    public String toString() {
        String val = "bannedwordslist " + bannedwordslistStr;
        val += "\nRegex bannedwordslist " + bannedwordslistRegex;
        return val;
    }
}
