/*
 * Created on Nov 12, 2003
 */
package org.roller;

import java.io.File;

import org.roller.business.FileManagerTest;
import org.roller.config.RollerConfig;
import org.roller.util.Blacklist;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author lance
 */
public class BlacklistTest extends TestCase
{
    private Blacklist blacklist;

	/**
	 * 
	 */
	public BlacklistTest()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public BlacklistTest(String arg0)
	{
		super(arg0);
	}
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();   
        blacklist = Blacklist.getBlacklist();
        String FS = File.separator;              
        blacklist.loadBlacklistFromFile(
            ".." + FS + "WEB-INF" + FS + "classes" + FS + "blacklist.txt");
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //System.out.println(blacklist);
    }
    
    public void testIsBlacklisted0()
    {
        assertFalse(blacklist.isBlacklisted("four score and seven years ago.com"));
    }
    
    // test non-regex
    public void testIsBlacklisted1()
    {
        assertTrue(blacklist.isBlacklisted("00000-online-casino.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted2()
    {
        assertTrue(blacklist.isBlacklisted("www.lsotr.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted3()
    {
        assertTrue(blacklist.isBlacklisted("www.lsotr.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted4()
    {
        assertTrue(blacklist.isBlacklisted("blow-job.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted5()
    {
        assertTrue(blacklist.isBlacklisted("buymoreonline.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted6()
    {
        assertTrue(blacklist.isBlacklisted("diet-enlargement.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted7()
    {
        assertTrue(blacklist.isBlacklisted("viagra.com"));
    }
    
    // test the regex patterns
    public void testIsBlacklisted8()
    {
        assertTrue(blacklist.isBlacklisted("ragazze-something.com"));
    }

    public static Test suite()
    {
        return new TestSuite(BlacklistTest.class);
    }
}
