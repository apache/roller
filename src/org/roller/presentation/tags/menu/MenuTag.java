
package org.roller.presentation.tags.menu;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.roller.RollerException;
import org.roller.presentation.RequestUtil;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.tags.VelocityTag;
import org.roller.presentation.velocity.ContextLoader;
//import javax.servlet.jsp.tagext.*;


/**
 * @jsp.tag name="Menu"
 */
public class MenuTag extends VelocityTag 
{
	private static Log mLogger = 
		LogFactory.getFactory().getInstance(RollerRequest.class);

	/** Unique ID for this menu within the user's session. 
	  * @jsp.attribute 
	  */
	public String getId() { return mMenuId; }
    public void setId( String v ) { mMenuId= v; }
	private String mMenuId;

	/** Name of the view to be used to render the menu.
      * The view is a Velocity template and it must be in the classpath. 
	  * Values: tabbed, vertical, horizontal.
	  * @jsp.attribute  required="true"
	  */
	public String getView() { return mView; }
    public void setView( String v ) { mView = v; }
	private String mView;

	/** Name of the model to be used.
	  * Must correspond to name of XML file in WEB-INF directory.
	  * @jsp.attribute required="true"
	  */
	public String getModel() { return mModel; }
    public void setModel( String v ) { mModel = v; }
	private String mModel;

    public String getTemplateClasspath()
    {
        return mView;
    }

   	//------------------------------------------------------------- 

	public void prepareContext( VelocityContext ctx )
	{
		HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse res = (HttpServletResponse)pageContext.getResponse();

		RollerMenuModel model = new RollerMenuModel( 
			mMenuId, "/WEB-INF/"+mModel, pageContext.getServletContext() );
		ctx.put("menuModel", model );
		ctx.put("ctx", pageContext );
		ctx.put("req", req );
		ctx.put("res", res );
		
		RollerRequest rreq = RollerRequest.getRollerRequest(req);
		rreq.setPageContext(pageContext);
		try
		{
			ContextLoader.setupContext( ctx, rreq, res );
		}
		catch (RollerException e)
		{
			// superclass says I can't throw an exception
			mLogger.error(e);
		}
	}

}

