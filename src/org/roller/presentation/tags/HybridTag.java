/*
 * HybridTag.java
 *
 * Created on February 10, 2002, 11:12 PM
 */

package org.roller.presentation.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * JSP tag designed to be used from JSP page or from Velocity page.
 * Tag must be a standalone tag, design precludes contents.
 * @author David M Johnson
 */
public abstract class HybridTag extends TagSupport 
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(HybridTag.class);

    public HybridTag() 
    {
    }

	public String toString()
	{
        String ret = null;
        try 
        {
            StringWriter sw = new StringWriter();
            doStartTag( new PrintWriter( sw, true ));
			// See, design precludes contents 
            doEndTag( new PrintWriter( sw, true ));
            ret = sw.toString();
        }
        catch (Exception e)
        {
            ret = "Exception in tag";
            mLogger.error(ret,e);
        }
        return ret;
	}
    
	public String emit()
	{
		return toString();
	}

	public int doStartTag() throws JspException 
	{
		return doStartTag( new PrintWriter( pageContext.getOut(), true) );
	}


	public int doEndTag() throws JspException 
	{
		return doEndTag( new PrintWriter( pageContext.getOut(), true) );
	}

	/** Default processing of the end tag returning SKIP_BODY. */
	public int doStartTag( PrintWriter pw ) throws JspException
	{
		return SKIP_BODY;
	}

	/** Default processing of the end tag returning EVAL_PAGE. */
	public int doEndTag( PrintWriter pw ) throws JspException
	{
		return EVAL_PAGE;
	}

}
