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

package org.apache.roller.util;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test blacklist functionality.
 */
public class BlacklistTest extends TestCase {
    
    private Blacklist blacklist;
    
    
    public BlacklistTest() {
        super();
    }
    
    /**
     * @param arg0
     */
    public BlacklistTest(String arg0) {
        super(arg0);
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        blacklist = Blacklist.getBlacklist();
        String FS = File.separator;
        blacklist.loadBlacklistFromFile(
                ".." + FS + "WEB-INF" + FS + "classes" + FS + "blacklist.txt");
    }
    
    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        //System.out.println(blacklist);
    }
    
    public void testIsBlacklisted0() {
        assertFalse(blacklist.isBlacklisted("four score and seven years ago.com"));
    }
    
    // test non-regex
    public void testIsBlacklisted1() {
        assertTrue(blacklist.isBlacklisted("00000-online-casino.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted2() {
        assertTrue(blacklist.isBlacklisted("www.lsotr.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted3() {
        assertTrue(blacklist.isBlacklisted("www.lsotr.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted4() {
        assertTrue(blacklist.isBlacklisted("blow-job.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted5() {
        assertTrue(blacklist.isBlacklisted("buymoreonline.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted6() {
        assertTrue(blacklist.isBlacklisted("diet-enlargement.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted7() {
        assertTrue(blacklist.isBlacklisted("viagra.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted8() {
        assertTrue(blacklist.isBlacklisted("ragazze-something.com"));
    }
    
    public static Test suite() {
        return new TestSuite(BlacklistTest.class);
    }
    
}
