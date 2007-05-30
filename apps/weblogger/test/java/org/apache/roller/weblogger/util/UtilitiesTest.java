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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test utilities.
 */
public class UtilitiesTest extends TestCase {
    
    /**
     * Constructor for LinkbackExtractorTest.
     * @param arg0
     */
    public UtilitiesTest(String arg0) {
        super(arg0);
    }
    
    public static void main(String[] args) {
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testExtractHTML() {
        String test = "<a>keep me</a>";
        String expect = "<a></a>";
        String result = Utilities.extractHTML(test);
        assertEquals(expect, result);
    }
    
    public void testRemoveHTML() {
        String test = "<br><br><p>a <b>bold</b> sentence with a <a href=\"http://example.com\">link</a></p>";
        String expect = "a bold sentence with a link";
        String result = Utilities.removeHTML(test, false);
        assertEquals(expect, result);
    }
    
    public void testTruncateNicely1() {
        String test = "blah blah blah blah blah";
        String expect = "blah blah blah";
        String result = Utilities.truncateNicely(test, 11, 15, "");
        assertEquals(expect, result);
    }
    
    public void testTruncateNicely2() {
        String test = "<p><b>blah1 blah2</b> <i>blah3 blah4 blah5</i></p>";
        String expect = "<p><b>blah1 blah2</b> <i>blah3</i></p>";
        String result = Utilities.truncateNicely(test, 15, 20, "");
        //System.out.println(result);
        assertEquals(expect, result);
    }
    
    /* broken because it uses UtilitiesModel which is part of .ui.* package
    public void testAddNoFollow() {
        String test1 = "<p>this some text with a <a href=\"http://example.com\">link</a>";
        String expect1 = "<p>this some text with a <a href=\"http://example.com\" rel=\"nofollow\">link</a>";
        String result1 = UtilitiesModel.addNofollow(test1);
        assertEquals(expect1, result1);
     
        String test2 = "<p>this some text with a <A href=\"http://example.com\">link</a>";
        String expect2 = "<p>this some text with a <A href=\"http://example.com\" rel=\"nofollow\">link</a>";
        String result2 = UtilitiesModel.addNofollow(test2);
        assertEquals(expect2, result2);
     
    }
     */
    
    public static Test suite() {
        return new TestSuite(UtilitiesTest.class);
    }
    
}
