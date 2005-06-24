
package org.roller.presentation.weblog.tags;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;

/** 
  * @jsp.tag name="RssBadge"
  */
public class RssBadgeTag extends org.roller.presentation.tags.HybridTag 
{
    static final long serialVersionUID = 8569693454388788128L;
    
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

			WebsiteData website = rreq.getWebsite();
			pw.println(
        		   "<a href="+"\"" + req.getContextPath() + "/rss/"
                + website.getHandle() + "\">"
                + "<img "+"src=\"" + req.getContextPath() + "/images/rssbadge.gif\" "
                + "class=\"rssbadge\" "
                + "alt=\"XML\""
                + "/>"
                + "</a>");
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

