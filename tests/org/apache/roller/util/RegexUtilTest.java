/*
 * Created on Nov 8, 2003
 */
package org.apache.roller.util;

import org.apache.roller.presentation.bookmarks.BookmarksActionTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author lance
 */
public class RegexUtilTest extends TestCase
{

	/**
	 * 
	 */
	public RegexUtilTest()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public RegexUtilTest(String arg0)
	{
		super(arg0);
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
    
    public void testEncodingEmail()
    {
        // test mailto: escaping
        String test = "test <a href='mailto:this@email.com'>email</a> string";
        String expect = "test <a href='mailto:%74%68%69%73%40%65%6d%61%69%6c%2e%63%6f%6d'>email</a> string";
        String result = RegexUtil.encodeEmail(test) ;
        //System.out.println(result);
        assertEquals(expect, result);        
    }
    
    public void testObfuscateEmail()
    {
        // test "plaintext" escaping
        String test = "this@email.com";
        String expect = "this-AT-email-DOT-com";
        String result = RegexUtil.encodeEmail(test);
        assertEquals(expect, result);        
    }
    
    public void testHexEmail()
    {
        // test hex & obfuscate together
        String test = "test <a href='mailto:this@email.com'>this@email.com</a> string, and this@email.com";
        String expect = "test <a href='mailto:%74%68%69%73%40%65%6d%61%69%6c%2e%63%6f%6d'>this-AT-email-DOT-com</a> string, and this-AT-email-DOT-com";
        String result = RegexUtil.encodeEmail(test);
        //System.out.println(result);
        assertEquals(expect, result);
    }

    public static Test suite() 
    {
        return new TestSuite(RegexUtilTest.class);
    }}
