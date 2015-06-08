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
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test blacklist functionality.
 */
public class BlacklistTest extends TestCase {
    public static Log log =    
        LogFactory.getLog(BlacklistTest.class);  
    
    private Blacklist blacklist;
    private List<String> blacklistStr = new LinkedList<String>();
    private List<Pattern> blacklistRegex = new LinkedList<Pattern>();

      
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
        Blacklist.populateSpamRules("www.myblacklistedsite.com", blacklistStr,
                blacklistRegex, null);
    }
    
    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        //System.out.println(blacklist);
    }
    
    public void testIsBlacklisted0() {
        assertFalse(blacklist.isBlacklisted("four score and seven years ago.com", blacklistStr, blacklistRegex));
    }
    
    // test non-regex
    public void testIsBlacklisted1() {
        assertTrue(blacklist.isBlacklisted("www.myblacklistedsite.com", blacklistStr, blacklistRegex));
    }

    public static Test suite() {
        return new TestSuite(BlacklistTest.class);
    }
    
}
