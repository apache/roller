/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.util;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.Weblog;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test blacklist functionality.
 */
public class BlacklistTest extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testIsNotBlacklisted() {
        Weblog weblog = new Weblog();
        assertFalse(weblogManager.getWeblogBlacklist(weblog).isBlacklisted("four score and seven years ago.com"));
    }

    @Test
    public void testIsBlacklisted() {
        Weblog weblog = new Weblog();
        weblog.setBlacklist("www.myblacklistedsite.com");
        assertTrue(weblogManager.getWeblogBlacklist(weblog).isBlacklisted("www.myblacklistedsite.com"));
    }

}
