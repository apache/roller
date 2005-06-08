/*
 * Created on Nov 2, 2003
 */
package org.roller.util;

import junit.framework.TestCase;

/**
 * @author lance
 */
public class UtilitiesTest extends TestCase
{
    /**
     * Constructor for LinkbackExtractorTest.
     * @param arg0
     */
    public UtilitiesTest(String arg0)
    {
        super(arg0);
    }

    public static void main(String[] args)
    {
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testExtractHTML()
    {
        String test = "<a>keep me</a>";
        String expect = "<a></a>";
        String result = Utilities.extractHTML(test);
        assertEquals(expect, result);
    }
    
    public void testTruncateNicely1()
    {
        String test = "blah blah blah blah blah";
        String expect = "blah blah blah";
        String result = Utilities.truncateNicely(test, 11, 15, "");
        assertEquals(expect, result);
    }
    
    public void testTruncateNicely2()
    {
        String test = "<p><b>blah1 blah2</b> <i>blah3 blah4 blah5</i></p>";
        String expect = "<p><b>blah1 blah2</b> <i>blah3</i></p>";
        String result = Utilities.truncateNicely(test, 15, 20, "");
        //System.out.println(result);
        assertEquals(expect, result);
    }
}
