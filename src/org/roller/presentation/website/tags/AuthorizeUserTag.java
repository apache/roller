package org.roller.presentation.website.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.RequestUtils;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerRequest;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;


/** 
  * If user is not authorized, forward to specified global forward. 
  * @jsp.tag name="AuthorizeUser" 
  */
public class AuthorizeUserTag extends TagSupport 
{
    private static Log mLogger = LogFactory.getFactory().getInstance(
        AuthorizeUserTag.class);

    private String name = null;
	/** @jsp.attribute required="true" */ 
    public String getFailureForward() { return (this.name); }
    public void setFailureForward(String name) {this.name = name;}

 	//----------------------------------------------------------- 
    /**
     * Defer generation until the end of this tag is encountered.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException 
	{
		return (SKIP_BODY);
    }

 	//----------------------------------------------------------- 
    /**
     * Process start tag.
     * @return EVAL_SKIP_BODY
     */
    public int doEndTag() throws JspException 
	{
		HttpServletRequest req = null;
		UserData user = null; 
		try
		{
			req = (HttpServletRequest)pageContext.getRequest();
			user = RollerRequest.getRollerRequest(req).getUser();
		}
		catch (Exception e)
		{
            mLogger.error("ERROR in tag",e);
			throw new JspException(e);
		}

		Principal prince = req.getUserPrincipal(); 
		if ( prince == null || !prince.getName().equals( user.getUserName() ) )
		{
			ServletContext ctx = req.getSession().getServletContext();
			ModuleConfig mConfig = RequestUtils.getModuleConfig(req,ctx);
			ForwardConfig fConfig = mConfig.findForwardConfig(name);
			if (fConfig==null)
				throw new JspException("Forward "+name+"not found");
			
			// Forward or redirect to the corresponding actual path
			String path = fConfig.getPath();
			if (fConfig.getRedirect()) 
			{
				HttpServletResponse response =
					(HttpServletResponse) pageContext.getResponse();
				try 
				{
					response.sendRedirect(response.encodeRedirectURL(path));
				} 
				catch (Exception e) 
				{
                  mLogger.error("ERROR in tag",e);
				  throw new JspException("Error redirecting to forward "+name);
				}
			} 
			else 
			{
				try 
				{
					pageContext.forward(path);
				} 
				catch (Exception e) 
				{
                    mLogger.error("ERROR in tag",e);
					throw new JspException("Error forward to "+name);
				}
			}

			// Skip the remainder of this page
			return (SKIP_PAGE);
		}
		return Tag.SKIP_BODY;
    }

	//------------------------------------------------------------------------
    /**
     * Release any acquired resources.
     */
    public void release() 
	{
		super.release();
		name = null;
    }
}

