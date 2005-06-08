
package org.roller.presentation.weblog.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerRequest;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/** 
  * @jsp.tag name="RssBadge"
  */
public class RssBadgeTag extends org.roller.presentation.tags.HybridTag 
{
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(RssBadgeTag.class);

  	//------------------------------------------------------------------------ 
    /**
     * Process start tag.
     * @return EVAL_SKIP_BODY
     */
    public int doStartTag( PrintWriter pw ) throws JspException 
	{
		try
		{
			HttpServletRequest req = 
				(HttpServletRequest)pageContext.getRequest();
			RollerRequest rreq = RollerRequest.getRollerRequest(req);

			UserData ud = rreq.getUser();

			pw.println(
				"<a href="+"\""+req.getContextPath()+"/rss/"
                +ud.getUserName()+"\">"
				+"<img "+"src=\""+req.getContextPath()+"/images/rssbadge.gif\" "
				+"class=\"rssbadge\" "
				+"alt=\"XML\""
				+"/>"
				+"</a>");
		}
		catch (Exception e)
		{
            mLogger.error("Exception",e);
			throw new JspException(
				e.getClass().toString()+": "+e.getMessage(),e);
		}
		return Tag.SKIP_BODY;
    }
}

