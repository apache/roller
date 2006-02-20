package org.roller.business;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Run the essential business layer tests (convenience test for IDEs)
 * 
 * @author llavandowska
 */
public class TestAll extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        
        suite.addTest(WebsiteTest.suite());
        suite.addTest(PermissionsTest.suite());        
        suite.addTest(UserManagerTest.suite());       
        suite.addTest(WeblogManagerTest.suite());        
        suite.addTest(RefererManagerTest.suite());
        suite.addTest(IndexManagerTest.suite());             
        suite.addTest(BookmarkManagerTest.suite());
        suite.addTest(FileManagerTest.suite());
        suite.addTest(PlanetManagerTest.suite()); 
        suite.addTest(PlanetManagerLocalTest.suite()); 
        
        return suite;
    }
    public TestAll(String testName)
    {
        super(testName);
    }

    public static void main(String[] args)
    {
        String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}