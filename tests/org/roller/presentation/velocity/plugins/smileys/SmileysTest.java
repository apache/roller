/*
 * Created on Jun 8, 2004
 */
package org.roller.presentation.velocity.plugins.smileys;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;

import org.roller.presentation.MockRollerRequest;

import junit.framework.TestCase;

/**
 * @author lance.lavandowska
 */
public class SmileysTest extends TestCase
{
    SmileysPlugin plugin = new SmileysPlugin();
    MockHttpServletRequest request = new MockHttpServletRequest();
 
    public void testLoadProperties()
    {        
        assertTrue( plugin.smileyPatterns.length > 0 );
        /*
        for (int i=0; i<plugin.imageTags.length; i++) {
            System.out.println(plugin.imageTags[i]);
        }
        */
    }
    
    public void testSmileEmoticon()
    {
        String test = "put on a happy :-) face";
        String expected = "put on a happy <img src=\"/roller/images/smileys/smile.gif" +
            "\" height=\"20\" width=\"20\" alt=\":-)\" title=\":-)\"> face";
        String result = plugin.render(test);
        System.out.println(result);
        assertEquals(expected, result);
    }
    
    public void setUp() throws Exception
    {
        super.setUp();

        request.setContextPath("/roller");
        plugin.init( new MockRollerRequest(
                           request,
                           new MockServletContext()), null);
    }
    
    /**
     * 
     */
    public SmileysTest()
    {
        super();
    }

    /**
     * @param arg0
     */
    public SmileysTest(String arg0)
    {
        super(arg0);
    }

}
