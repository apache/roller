package org.roller.presentation.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.presentation.RollerSession;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * If there is an status message, then print it in red.
 * @jsp.tag name="StatusMessage"
 */
public class StatusMessageTag extends TagSupport
{
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(StatusMessageTag.class);

    public int doStartTag() throws JspException
	{
		try
		{
			JspWriter pw = pageContext.getOut();
			HttpServletRequest req = 
				(HttpServletRequest)pageContext.getRequest();

			String msg = null;

			msg = (String)req.getSession().getAttribute(
                RollerSession.ERROR_MESSAGE);
			if (msg != null)
			{
				pw.println("<span class=\"error\">");
				pw.println(msg);
				pw.println("</span>");
				req.getSession().removeAttribute(RollerSession.ERROR_MESSAGE);
			}

			msg = (String)req.getSession().getAttribute(
                RollerSession.STATUS_MESSAGE);
			if (msg != null)
			{
				pw.println("<span class=\"statusMsg\">");
				pw.println(msg);
				pw.println("</span>");
				req.getSession().removeAttribute(RollerSession.STATUS_MESSAGE);
			}
		}
		catch (IOException e)
		{
            mLogger.error("Exception",e);
			throw new JspException(e);
		}
		return Tag.SKIP_BODY;
    }
}

