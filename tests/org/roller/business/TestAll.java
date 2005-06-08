package org.roller.business;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Run the essential business layer tests.
 * 
 * @author llavandowska
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

        suite.addTest(UserManagerTest.suite());
        suite.addTest(WeblogManagerTest.suite());
        suite.addTest(RefererManagerTest.suite());
		suite.addTest(BookmarkManagerTest.suite());
        suite.addTest(ConfigManagerTest.suite());
        
        return suite;
    }
}