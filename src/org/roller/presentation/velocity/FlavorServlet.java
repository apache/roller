package org.roller.presentation.velocity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.roller.RollerException;
import org.roller.presentation.RollerRequest;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

/////////////////////////////////////////////////////////////////////////////
/**
  * <p>Responsible for rendering RSS feeds and other "flavors" of output for a
  * weblog.</p>
  *
  * <p>If Servlet is mapped to <code>/rss</code> and user has defined
  * an RSS override page (i.e. a page named "_rss"), then that Velocity
  * template will be used for rendering.</p>
  *
  * <p>If there is a request parameter named "flavor", then the Velocity
  * template specified by that parameter will be used for rendering. For
  * example if the flavor is "rss092" then the template at classpath
  * "/flavors/rss092.vm" will be used for rendering.</p>
  *
  * <p>Otherwise, the template /flavors/rss.vm" will be used for rendering.</p>
  *
  * <p>Assumes that If-Modified-Since has already been handled.</p>
  *
  * @author David M Johnson
  *
  * @web.servlet name="RssServlet"
  * @web.servlet-mapping url-pattern="/rss/*"
  * @web.servlet-mapping url-pattern="/flavor/*"
  */
public class FlavorServlet extends VelocityServlet
{
    static final long serialVersionUID = -2720532269434186051L;
    
    private static Log mLogger = LogFactory.getFactory()
                                           .getInstance(RollerRequest.class);
    public Template handleRequest(HttpServletRequest request,
                                  HttpServletResponse response, Context ctx)
    {
        RollerRequest rreq = null;
        try
        {
            rreq = RollerRequest.getRollerRequest(request,getServletContext());
            
            // This is an ugly hack to fix the following bug: 
            // ROL-547: "Site wide RSS feed is your own if you are logged in"
            String[] pathInfo = StringUtils.split(rreq.getPathInfo(),"/"); 
            if (pathInfo.length < 1) 
            {
                // If website not specified in URL, set it to null
                rreq.setWebsite(null);
            }
        }
        catch (RollerException e)
        {
            // An error initializing the request is considered to be a 404
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("RollerRequest threw Exception", e);
            }
            try
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            catch (IOException e1)
            {
                if (mLogger.isDebugEnabled())
                {
                    mLogger.debug("IOException sending error", e);
                }
            }
            return null;
        }
        try
        {
            // Needed to init request attributes, etc.
            PageContext pageContext =
                JspFactory.getDefaultFactory().getPageContext(
                this, request,  response, "", true, 8192, true);
            rreq.setPageContext(pageContext);
            ContextLoader.setupContext(ctx, rreq, response);

            final String useTemplate;
            PageModel pageModel = (PageModel)ctx.get("pageModel");
            if (    request.getServletPath().endsWith("rss")
                 && pageModel.getPageByName("_rss") != null )
            {
                // If the request specified the "/rss" mapping and the
                // user has defined an RSS override page, we will use that.
                useTemplate = pageModel.getPageByName("_rss").getId();
            }
            else if (request.getParameter("flavor") != null)
            {
                // If request specifies a "flavor" then use that.
                String flavor = request.getParameter("flavor");
                useTemplate = "/flavors/" + flavor + ".vm";
            }
            else
            {
                // Fall through to default RSS page template.
                useTemplate = "/flavors/rss.vm";
            }
            return getTemplate(useTemplate);
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in RssServlet", e);
        }
        return null;
    }

    //------------------------------------------------------------------------
    /**
     * Handle error in Velocity processing.
     */
    protected void error( HttpServletRequest req, HttpServletResponse res,
        Exception e) throws ServletException, IOException
    {
        mLogger.warn("ERROR in FlavorServlet",e);
    }
}

