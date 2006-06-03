/*
 * Created on Nov 2, 2003
 */
package org.roller.util;

import org.roller.presentation.bookmarks.BookmarksActionTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
    
    public void testRemoveHTML()
    {
        String test = "<br><br><p>a <b>bold</b> sentence with a <a href=\"http://example.com\">link</a></p>";
        String expect = "a bold sentence with a link";
        String result = Utilities.removeHTML(test, false);
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

    public void testAddNoFollow() {
        String test1 = "<p>this some text with a <a href=\"http://example.com\">link</a>";
        String expect1 = "<p>this some text with a <a href=\"http://example.com\" rel=\"nofollow\">link</a>";
        String result1 = Utilities.addNofollow(test1);
        assertEquals(expect1, result1);

        String test2 = "<p>this some text with a <A href=\"http://example.com\">link</a>";
        String expect2 = "<p>this some text with a <A href=\"http://example.com\" rel=\"nofollow\">link</a>";
        String result2 = Utilities.addNofollow(test2);
        assertEquals(expect2, result2);

    }

    public static Test suite() 
    {
        return new TestSuite(UtilitiesTest.class);
    }
}
