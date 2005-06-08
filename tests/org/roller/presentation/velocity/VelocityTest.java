/*
 * Created on Oct 31, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.roller.presentation.velocity;

import junit.framework.TestCase;

import org.roller.presentation.velocity.plugins.textile.TextilePlugin;

/**
 * We really need to solve webtesting to adequately test our Presentation classes.
 * 
 * @author lance
 */
public class VelocityTest extends TestCase
{
    String textileStr = "*strong* plain _emphasis_ * _emphaticStrong_ * ";
    String expected = "<p><strong>strong</strong>plain <em>emphasis</em> <strong> <em>emphaticStrong</em> </strong></p>";
    
    /* this is failing, but I think it is due to a problem in Textile4J:
     * for instance, it is stripping a space after closing  a "strong" tag.
     */
    public void testTextile()
    {
        PagePlugin textile = new TextilePlugin();
        
        String result = textile.render(textileStr);
        assertEquals("this will fail until Textile4J is fixed.", expected, result);         
    }
    
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
        super.setUp();	       
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
	   super.tearDown();
	}

}
