/*
 * Created on Jun 8, 2004
 */
package org.roller.presentation.velocity.plugins.smileys;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.presentation.MockRollerRequest;
import org.roller.presentation.VelocityServletTestBase;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;

/**
 * @author lance.lavandowska
 */
public class SmileysTest extends VelocityServletTestBase
{
    public void testSmileEmoticon() throws Exception
    {
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setServletContextName("/roller");      
        
        MockHttpServletRequest request = getMockFactory().getMockRequest();
        request.setContextPath("/roller");
       
        doFilters();

        SmileysPlugin plugin = new SmileysPlugin();
        plugin.init(mWebsite, ctx, "/roller", null);
        assertTrue( SmileysPlugin.smileyPatterns.length > 0 );

        String test = "put on a happy :-) face";
        String expected = 
            "put on a happy <img src=\"/roller/images/smileys/smile.gif" +
            "\" class=\"smiley\" alt=\":-)\" title=\":-)\"> face";
        String result = plugin.render(test);
        //System.out.println(result);
        assertEquals(expected, result);
    }
        
    public SmileysTest()
    {
        super();
    }

    public static Test suite() 
    {
        return new TestSuite(SmileysTest.class);
    }
}
