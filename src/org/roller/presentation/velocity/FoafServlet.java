package org.roller.presentation.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.util.Utilities;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.presentation.RollerSession;


//////////////////////////////////////////////////////////////////////////////

/**
  * <p>ROLLER_2.0: FOAF is broken in Roller 2.0.
  * Responsible for rendering FOAF feed.  This servlet requires
  * that the RequestFilter is in place for it, and should also
  * have the IfModifiedFilter configured.</p>
  * 
  * <p>Resources:<br />
  * <a href="http://xmlns.com/foaf/0.1/"
  *     >FOAF Vocabulary Specification</a><br />
  * <a href="http://www.xml.com/lpt/a/2004/02/04/foaf.html"
  *     >An Introduction to FOAF</a></p>
  * 
  * <p>FOAF Autodiscovery: <br />
  * <link rel="meta" type="application/rdf+xml" title="FOAF" 
  *     href="$absBaseURL/foaf/$userName" /> </p>
  *
  * @author Lance Lavandowska
  *
  * @web.servlet name="FoafServlet"
  * @web.servlet-mapping url-pattern="/foaf/*"
  */
public class FoafServlet extends VelocityServlet
{
    static final long serialVersionUID = -1893244416537298619L;
    
    private static Log mLogger = LogFactory.getFactory()
                                           .getInstance(RollerRequest.class);

    /**
     * This Velocity servlet does not make use of ContextLoader and associated
     * classes (as do FlavorServlet and PageServlet) because that is more
     * work than is really necessary.  It implements its own setupContext()
     * to load necessary values into the Velocity Context.
     * 
     * @param ctx
     * @param rreq
     * @throws RollerException
     */
    public Template handleRequest(HttpServletRequest request,
                                  HttpServletResponse response, Context ctx)
    {
        RollerRequest rreq = null;
        try
        {
            rreq = RollerRequest.getRollerRequest(request, getServletContext());
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
            setupContext(ctx, rreq);

            response.setContentType("application/rdf+xml");
            return getTemplate("/flavors/foaf.vm");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in FoafServlet", e);
        }
        return null;
    }

    /**
	 * @param ctx
	 */
	private void setupContext(Context ctx, RollerRequest rreq) throws RollerException
	{
        HttpServletRequest request = rreq.getRequest();
        RollerContext rollerCtx = RollerContext.getRollerContext( request );
        RollerSession rses = RollerSession.getRollerSession(request);
        Roller roller = RollerFactory.getRoller();
        // ROLLER_2.0 : figure out how to fix FOAF servlet (does anybody use it?)
        // UserData user = 
        //   roller.getUserManager().getUser(userName, Boolean.TRUE);
        // ctx.put("fullName", user.getFullName()); // name for FlavorServlet compatibility
        
        // foaf:homepage to equal base URL for user
        //String homepage = Utilities.escapeHTML( 
                //rollerCtx.getAbsoluteContextUrl(request) + 
                    //"/page/" + user.getUserName() );
        //ctx.put("websiteURL", homepage); // name for FlavorServlet compatibility

        // see if foaf:weblog is different Page
        WebsiteData website = rreq.getWebsite();
        UserManager usrMgr = RollerContext.getRoller(request).getUserManager();
        org.roller.pojos.Template weblog = website.getPageByName("Weblog");
        
        // if weblog != homepage, add to context
        if (weblog != null && !website.getDefaultPageId().equals(weblog.getId()))
        {
            //String weblogUrl = Utilities.escapeHTML( 
                    //rollerCtx.getAbsoluteContextUrl(request) + 
                        //"/page/" + user.getUserName() + 
                            //"/" + weblog.getLink() );
        	//ctx.put("weblog", weblogUrl);
        }
        
        // use SHA1 encrypted email address, including mailto: prefix
        //String shaEmail = Utilities.encodePassword(
                //"mailto:" + user.getEmailAddress(), "SHA");
        //ctx.put("shaEmail", shaEmail);
	}

	//------------------------------------------------------------------------
    /**
     * Handle error in Velocity processing.
     */
    protected void error( HttpServletRequest req, HttpServletResponse res,
        Exception e) throws ServletException, IOException
    {
        mLogger.warn("ERROR in FoafServlet",e);
    }
}
