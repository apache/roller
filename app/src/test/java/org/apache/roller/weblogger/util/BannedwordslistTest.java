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

import static org.junit.jupiter.api.Assertions.assertFalse;
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

}
