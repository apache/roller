/*
 * Created on Oct 31, 2003
 */package org.roller.presentation.velocity.plugins.textile;import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.roller.presentation.bookmarks.BookmarksActionTest;
import org.roller.presentation.velocity.PagePlugin;
import org.roller.presentation.velocity.plugins.textile.TextilePlugin;
/** * We really need to solve webtesting to adequately test our Presentation classes. *  * @author lance */public class TextileTest extends TestCase{    String textileStr = "*strong* plain _emphasis_ * _emphaticStrong_ * ";    String expected = "<p><strong>strong</strong> plain <em>emphasis</em> <strong> <em>emphaticStrong</em> </strong></p>";      /* 
     * This fails because Textile4J appears to place a tab (\t)
     * at the beginning of the result.  If the result is .trim()'ed
     * then it passes.     */    public void testTextile()    {        PagePlugin textile = new TextilePlugin();                String result = textile.render(textileStr);
        //System.out.println(expected);
        //System.out.println(result);        assertEquals("this will fail until Textile4J is fixed.", expected, result);             }    	/* (non-Javadoc)	 * @see junit.framework.TestCase#setUp()	 */	protected void setUp() throws Exception	{        super.setUp();	       	}	/* (non-Javadoc)	 * @see junit.framework.TestCase#tearDown()	 */	protected void tearDown() throws Exception	{	   super.tearDown();	}

    public static Test suite() 
    {
        return new TestSuite(TextileTest.class);
    }}