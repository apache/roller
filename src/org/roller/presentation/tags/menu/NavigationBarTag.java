package org.roller.presentation.tags.menu;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.roller.RollerException;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.presentation.velocity.PageModel;

/**
 * Draws the most complete possible Roller navigation bar based on request
 * parameters userName, folderId and authenticated user (if there is one).
 * 
 * By supplying a "view" attribute, you can replace the default display 
 * with a custom implementation of the Navigation Bar.  Implement by
 * creating a new VM file and placing it in /WEB-INF/classes.
 * 
 * @jsp.tag name="NavigationBar"
 */
public class NavigationBarTag extends MenuTag
{
    private static Log mLogger = 
    	LogFactory.getFactory().getInstance(RollerRequest.class);
    	
    private boolean mVertical = false;
    private String mDelimiter = "|";

    /** @jsp.attribute */
    public boolean getVertical()
    {
        return mVertical;
    }

    public void setVertical(boolean v)
    {
        mVertical = v;
    }

    /** @jsp.attribute */
    public String getDelimiter()
    {
        return mDelimiter;
    }

    public void setDelimiter(String v)
    {
        mDelimiter = v;
    }

   /**
    * Replace the 'standard' NavigationBar display with a custom vm file.
    *
    * @jsp.attribute  required="false"
    */
    public String getView() { return super.getView(); }
    public void setView( String v ) 
    { 
        super.setView(v);
    }

    /** Name of the model to be used.
      * Must correspond to name of XML file in WEB-INF directory.
      * @jsp.attribute required="false"
      */
    public String getModel() { return super.getModel(); }
    public void setModel( String v ) { super.setModel(v); }
    
    //-------------------------------------------------------------
    public String view(boolean isVertical)
    {
        mVertical = isVertical;

        return emit();
    }
    
    public void prepareContext( VelocityContext ctx )
    {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        HttpServletResponse res = (HttpServletResponse)pageContext.getResponse();

        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        rreq.setPageContext(pageContext);
        RollerContext rollerCtx = RollerContext.getRollerContext(req);
        try 
        {    
            ContextLoader.setupContext( ctx, rreq, res );
            PageModel pageModel = (PageModel)ctx.get("pageModel");
            ctx.put("model", pageModel);            
            ctx.put("pages", pageModel.getPages());            
            ctx.put("req", req);
            ctx.put("res", res);
            ctx.put("vertical", Boolean.valueOf(getVertical()));
            ctx.put("delimiter", getDelimiter());
            ctx.put("editorui", Boolean.TRUE);            
        }
        catch (Exception e)
        {
            // superclass says I can't throw an exception
            mLogger.error(e);
        }
    }

    //-------------------------------------------------------------

    /**
     * Evaluate any tags inside us.  This will also allow us to have child tags
     * send us messages.
     * @return
     * @throws JspException
     */
    public int doStartTag(java.io.PrintWriter pw)
        throws JspException
    {
        return TagSupport.EVAL_BODY_INCLUDE;
    }

    /**
     * @return
     * @throws JspException
     */
    public int doEndTag(java.io.PrintWriter pw) throws JspException
    {
        try 
        {
            // a special view VM has been defined
            if (getView() != null)
            {
                Template template = Velocity.getTemplate(
                    getVelocityClasspathResource( getTemplateClasspath() ) );
                VelocityContext context = getVelocityContext();
                prepareContext( context );
                template.merge(context, pw);
                return EVAL_PAGE;
            }
            else
            {
                //setView("/navbar.vm");
                //String myResource= getVelocityClasspathResource(getTemplateClasspath());
                
                String myResource= getVelocityClasspathResource("/navbar.vm");

                VelocityContext myVelocityContext = getVelocityContext();

                // ask concrete class to prepare context 
                prepareContext( myVelocityContext );
                if (myVelocityContext.get("pageHelper") == null)
                    throw new RollerException("Failure initializing ContextLoader.");

                StringWriter myStringWriter = new StringWriter();
                
                String[] vars = {"vertical", "delimiter" };
                Velocity.invokeVelocimacro("showNavBar", "NavigationBar", vars, 
                    myVelocityContext, myStringWriter);

                pw.println(myStringWriter);

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