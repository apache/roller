package org.roller.presentation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.roller.BlacklistTest;
import org.roller.DateTest;
import org.roller.presentation.bookmarks.BookmarksActionTest;
import org.roller.presentation.filters.RequestFilterTest;
import org.roller.presentation.velocity.plugins.smileys.SmileysTest;
import org.roller.presentation.velocity.plugins.textile.TextileTest;
import org.roller.presentation.weblog.WeblogEntryActionTest;
import org.roller.presentation.xmlrpc.RollerXmlRpcServerTest;
import org.roller.util.LRUCache2Test;
import org.roller.util.LinkbackExtractorTest;
import org.roller.util.RegexUtilTest;
import org.roller.util.UtilitiesTest;
import org.roller.util.rome.DiskFeedInfoCacheTest;

/**
 * Run the essential presentation layer tests (convenience test for IDEs)
 * @author Dave M Johnson
 */
public class TestAll extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        //suite.addTest(BookmarksActionTest.suite());
        //suite.addTest(WeblogEntryActionTest.suite());
        //suite.addTest(BlacklistTest.suite());
        //suite.addTest(DateTest.suite());
        //suite.addTest(RequestFilterTest.suite());
        suite.addTest(SearchServletTest.suite());
        suite.addTest(SmileysTest.suite());
        suite.addTest(TextileTest.suite());
        //suite.addTest(RollerXmlRpcServerTest.suite());
        //suite.addTest(LinkbackExtractorTest.suite());
        //suite.addTest(LRUCache2Test.suite());
        //suite.addTest(RegexUtilTest.suite());
        suite.addTest(DiskFeedInfoCacheTest.suite());
        //suite.addTest(UtilitiesTest.suite());  
        
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