package org.roller.presentation;

import org.roller.presentation.bookmarks.BookmarksActionTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Run the essential presentation layer tests.
 * @author Dave M Johnson
 */
public class TestAll extends TestCase
{
    public TestAll(String testName)
    {
        super(testName);
    }

    public static void main(String[] args)
    {
        String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(BookmarksActionTest.suite());        
        return suite;
    }
}