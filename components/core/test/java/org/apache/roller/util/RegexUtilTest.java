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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.roller.util.RegexUtil;


/**
 * Test regex utils.
 */
public class RegexUtilTest extends TestCase {
    
    /**
     *
     */
    public RegexUtilTest() {
        super();
    }
    
    /**
     * @param arg0
     */
    public RegexUtilTest(String arg0) {
        super(arg0);
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
    
    public void testEncodingEmail() {
        // test mailto: escaping
        String test = "test <a href='mailto:this@email.com'>email</a> string";
        String expect = "test <a href='mailto:%74%68%69%73%40%65%6d%61%69%6c%2e%63%6f%6d'>email</a> string";
        String result = RegexUtil.encodeEmail(test) ;
        //System.out.println(result);
        assertEquals(expect, result);
    }
    
    public void testObfuscateEmail() {
        // test "plaintext" escaping
        String test = "this@email.com";
        String expect = "this-AT-email-DOT-com";
        String result = RegexUtil.encodeEmail(test);
        assertEquals(expect, result);
    }
    
    public void testHexEmail() {
        // test hex & obfuscate together
        String test = "test <a href='mailto:this@email.com'>this@email.com</a> string, and this@email.com";
        String expect = "test <a href='mailto:%74%68%69%73%40%65%6d%61%69%6c%2e%63%6f%6d'>this-AT-email-DOT-com</a> string, and this-AT-email-DOT-com";
        String result = RegexUtil.encodeEmail(test);
        //System.out.println(result);
        assertEquals(expect, result);
    }
    
    public static Test suite() {
        return new TestSuite(RegexUtilTest.class);
    }

}
