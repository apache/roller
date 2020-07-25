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
 */

package org.apache.roller.weblogger.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
    

}
