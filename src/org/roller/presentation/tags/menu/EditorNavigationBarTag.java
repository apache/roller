
package org.roller.presentation.tags.menu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;


/**
 * Draws the most complete possible Roller navigation bar based on request
 * parameters userName, folderId and authenticated user (if there is one).
 * @jsp.tag name="EditorNavigationBar"
 */
public class EditorNavigationBarTag extends MenuTag 
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(EditorNavigationBarTag.class);

   	//------------------------------------------------------------- 
	public EditorNavigationBarTag()
	{
	}

   	//------------------------------------------------------------- 
	public String view()
	{
		return emit();
	}

   	//------------------------------------------------------------- 
    public int doEndTag(java.io.PrintWriter pw) throws JspException
    {
		try 
		{
			HttpServletRequest request = 
				(HttpServletRequest)pageContext.getRequest(); 
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			RollerSession rollerSession = RollerSession.getRollerSession(request);
			if ( rollerSession.isGlobalAdminUser() )
			{
				return super.doEndTag(pw);
			}
			else
			{
				return EVAL_PAGE;
			}
		}
		catch (Exception e)
		{
			mLogger.error("EditorNavigationBarTag exception",e);
			throw new JspException(e);
		}
    }
}

