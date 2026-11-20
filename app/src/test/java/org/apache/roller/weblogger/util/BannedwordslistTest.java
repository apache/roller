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

package org.apache.roller.weblogger.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test bannedwordslist functionality.
 */
public class BannedwordslistTest  {
    public static Log log = LogFactory.getLog(BannedwordslistTest.class);
    
    private Bannedwordslist bannedwordslist;
    
    @BeforeEach
    public void setUp() throws Exception {
        bannedwordslist = Bannedwordslist.getBannedwordslist();
        String FS = File.separator;
        String bannedwordslistName = System.getProperty("project.build.directory") + FS + "classes" + "bannedwordslist.txt";
        log.info("Processing Banned-words list file: " + bannedwordslistName);
        bannedwordslist.loadBannedwordslistFromFile(bannedwordslistName);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsBannedwordslisted0() {
        assertFalse(bannedwordslist.isBannedwordslisted("four score and seven years ago.com"));
    }
    
    // test the regex patterns
    @Test
    public void testIsBannedwordslisted2() {
        assertTrue(bannedwordslist.isBannedwordslisted("www.lsotr.com"));
    }
    
    // test the regex patterns
    @Test
    public void testIsBannedwordslisted3() {
        assertTrue(bannedwordslist.isBannedwordslisted("buymoreonline.com"));
    }
    
    // test the regex patterns
    @Test
    public void testIsBannedwordslisted4() {
        assertTrue(bannedwordslist.isBannedwordslisted("diet-enlargement.com"));
    }
    
    // test the regex patterns
    @Test
    public void testIsBannedwordslisted5() {
        assertTrue(bannedwordslist.isBannedwordslisted("viagra.com"));
    }
    

    // each bundled regex rule still matches the kind of domain it targets
    @Test
    public void testBundledRegexRules() {
        assertTrue(bannedwordslist.isBannedwordslisted("see http://sub.lostr.info/x"));
        assertTrue(bannedwordslist.isBannedwordslisted("cheap.dietpills-now.biz"));
        assertTrue(bannedwordslist.isBannedwordslisted("onlinecasino-royal.net"));
        assertTrue(bannedwordslist.isBannedwordslisted("prozac-store.org"));
        assertTrue(bannedwordslist.isBannedwordslisted("tramadol.example.com"));
        assertTrue(bannedwordslist.isBannedwordslisted("nexium.co"));
        assertFalse(bannedwordslist.isBannedwordslisted("I went online to buy a casino book."));
    }

    // long text is checked in time proportional to its length
    @Test
    public void testLongTextIsCheckedQuickly() {
        String[] units = {"a", "buy", "buyonline", "dietpills", "onlinecasino", "viagra"};
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (String unit : units) {
                String text = unit.repeat(40000 / unit.length());
                assertFalse(bannedwordslist.isBannedwordslisted(text), unit);
            }
        });
    }

    // a weblog's rule that backtracks without end can't hold up the check;
    // the text counts as a match, so a comment with it is caught as spam
    @Test
    public void testRuleThatBacktracksIsStopped() {
        String text = "x".repeat(40) + "!";
        // a regex rule, and a word rule, which is also used as a regex
        for (String rule : new String[] {"((x+)+)+y", "x((x+)+)+y"}) {
            List<String> stringRules = new ArrayList<>();
            List<Pattern> regexRules = new ArrayList<>();
            Bannedwordslist.populateSpamRules(rule, stringRules, regexRules, null);
            assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
                assertTrue(Bannedwordslist.matchesRulesOnly(text, stringRules, regexRules), rule);
                assertTrue(bannedwordslist.isBannedwordslisted(text, stringRules, regexRules), rule);
            }, rule);
        }
        // the limit applies to each value, not to every check that follows
        assertFalse(bannedwordslist.isBannedwordslisted("an ordinary comment"));
    }

    // with weblogAdminsUntrusted, a weblog's own rules are matched as words,
    // not as regular expressions; the site-wide rules are still regexes
    @Test
    public void testWeblogRulesAsWords() {
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Bannedwordslist.populateSpamRules("(a|b)\nViagra\n(foo)\nexample.com/", stringRules, regexRules,
                "(c|d)", true);
        assertFalse(Bannedwordslist.matchesRulesOnly("a", stringRules, regexRules));
        assertTrue(Bannedwordslist.matchesRulesOnly("spam (a|b) here", stringRules, regexRules));
        assertTrue(Bannedwordslist.matchesRulesOnly("buy VIAGRA now", stringRules, regexRules));
        assertFalse(Bannedwordslist.matchesRulesOnly("viagraphone", stringRules, regexRules));
        assertTrue(Bannedwordslist.matchesRulesOnly("see (foo) now", stringRules, regexRules));
        assertTrue(Bannedwordslist.matchesRulesOnly("http://example.com/x", stringRules, regexRules));
        // the site-wide rule is still a regex
        assertTrue(Bannedwordslist.matchesRulesOnly("d", stringRules, regexRules));

        // with the setting off, the weblog's rules are regexes as before
        stringRules.clear();
        regexRules.clear();
        Bannedwordslist.populateSpamRules("(a|b)", stringRules, regexRules, null, false);
        assertTrue(Bannedwordslist.matchesRulesOnly("a", stringRules, regexRules));
    }

    @Test
    public void testWeblogRulesAsUnicodeWords() {
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        // "café" is cafe with an acute accent, "élan" is elan
        Bannedwordslist.populateSpamRules("café\nélan", stringRules, regexRules, null, true);
        assertTrue(Bannedwordslist.matchesRulesOnly("at the café now", stringRules, regexRules));
        assertTrue(Bannedwordslist.matchesRulesOnly("AT THE CAFÉ NOW", stringRules, regexRules));
        assertFalse(Bannedwordslist.matchesRulesOnly("too much caféine", stringRules, regexRules));
        assertTrue(Bannedwordslist.matchesRulesOnly("with ÉLAN", stringRules, regexRules));
        assertFalse(Bannedwordslist.matchesRulesOnly("réélan", stringRules, regexRules));
    }

    // a line that looks blank, such as one non-breaking space, is not a rule
    @Test
    public void testBlankLinesAreNotRules() {
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Bannedwordslist.populateSpamRules("foo\n\u00A0\n\u3000\nbar", stringRules, regexRules, null, true);
        assertEquals(2, regexRules.size());
        assertFalse(Bannedwordslist.matchesRulesOnly("price:\u00A010 EUR", stringRules, regexRules));

        // a browser ends lines with \r\n, so an empty line leaves "\r"
        stringRules.clear();
        regexRules.clear();
        Bannedwordslist.populateSpamRules("foo\r\n\r\nbar", stringRules, regexRules, null, false);
        assertEquals(List.of("foo", "bar"), stringRules);
        assertFalse(Bannedwordslist.matchesRulesOnly("a perfectly ordinary comment", stringRules, regexRules));
    }

    // a line saved as a plain word while weblog admins were untrusted isn't
    // always a valid regex once they are trusted; it is then matched as text
    @Test
    public void testInvalidRegexRuleIsMatchedAsText() {
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Bannedwordslist.populateSpamRules("(18+", stringRules, regexRules, null, false);
        assertTrue(Bannedwordslist.matchesRulesOnly("only (18+ here", stringRules, regexRules));
        assertFalse(Bannedwordslist.matchesRulesOnly("only 18 here", stringRules, regexRules));
        // saving the list as regexes still refuses it
        assertThrows(PatternSyntaxException.class, () -> Bannedwordslist.validateRules("(18+"));
        Bannedwordslist.validateRules("(casino)\nviagra");
    }

    // each rule has its own time limit, so a long list of plain words
    // against a long comment is slow but isn't counted as a match
    @Test
    public void testTimeLimitIsPerRule() {
        StringBuilder rules = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            rules.append("spamword").append(i).append('\n');
        }
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Bannedwordslist.populateSpamRules(rules.toString(), stringRules, regexRules, null, true);
        String text = "an ordinary comment ".repeat(500);
        assertTimeoutPreemptively(Duration.ofSeconds(60), () ->
                assertFalse(Bannedwordslist.matchesRulesOnly(text, stringRules, regexRules)));
    }

    // a referrer that can't be checked in time is let through, since the
    // reader would otherwise be refused the page
    @Test
    public void testReferrerThatCantBeCheckedIsNoMatch() {
        String text = "x".repeat(40) + "!";
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Bannedwordslist.populateSpamRules("((x+)+)+y", stringRules, regexRules, null);
        assertTimeoutPreemptively(Duration.ofSeconds(8), () ->
                assertFalse(Bannedwordslist.matchesRulesOnly(text, stringRules, regexRules, false)));
    }

    // a rule that recurses once per repetition can run out of stack on long
    // text; the text is then handled as if the time limit had passed
    @Test
    public void testRuleThatRunsOutOfStack() {
        String text = "ab".repeat(50000);
        List<String> stringRules = new ArrayList<>();
        List<Pattern> regexRules = new ArrayList<>();
        Bannedwordslist.populateSpamRules("(a|b)+c", stringRules, regexRules, null);
        assertTrue(bannedwordslist.isBannedwordslisted(text, stringRules, regexRules));
        assertFalse(Bannedwordslist.matchesRulesOnly(text, stringRules, regexRules, false));
    }

}
