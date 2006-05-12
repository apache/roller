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
package org.apache.roller.presentation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.roller.BlacklistTest;
import org.apache.roller.DateTest;
import org.apache.roller.presentation.bookmarks.BookmarksActionTest;
import org.apache.roller.presentation.filters.RequestFilterTest;
import org.apache.roller.presentation.velocity.plugins.smileys.SmileysTest;
import org.apache.roller.presentation.velocity.plugins.textile.TextileTest;
import org.apache.roller.presentation.weblog.WeblogEntryActionTest;
import org.apache.roller.presentation.xmlrpc.RollerXmlRpcServerTest;
import org.apache.roller.util.LRUCache2Test;
import org.apache.roller.util.LinkbackExtractorTest;
import org.apache.roller.util.RegexUtilTest;
import org.apache.roller.util.UtilitiesTest;
import org.apache.roller.util.rome.DiskFeedInfoCacheTest;

/**
 * Run the essential presentation layer tests (convenience test for IDEs)
 * @author Dave M Johnson
 */
public class TestAll extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(BookmarksActionTest.suite());
        suite.addTest(WeblogEntryActionTest.suite());
        suite.addTest(BlacklistTest.suite());
        suite.addTest(DateTest.suite());
        suite.addTest(RequestFilterTest.suite());
        suite.addTest(SearchServletTest.suite());
        suite.addTest(SmileysTest.suite());
        suite.addTest(TextileTest.suite());
        suite.addTest(RollerXmlRpcServerTest.suite());
        suite.addTest(LinkbackExtractorTest.suite());
        suite.addTest(LRUCache2Test.suite());
        suite.addTest(RegexUtilTest.suite());
        suite.addTest(DiskFeedInfoCacheTest.suite());
        suite.addTest(UtilitiesTest.suite());  
        
        // TODO: suite.addTest(ApplicationResourcesTest.suite());
        // TODO: suite.addTest(ArchiveParserTest.suite());
        // TODO: suite.addTest(AtomCollectionTest.suite());
        // TODO: suite.addTest(AtomServletTest.suite());
        // TODO: suite.addTest(ExportRssTest.suite());
        // TODO: suite.addTest(LanguageUtilTest.suite());
        
        return suite;
    }
    public TestAll(String testName)
    {
        super(testName);
    }
    public static void main(String[] args)
    {
        //String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.run(TestAll.suite());
    }
}