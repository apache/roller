package org.roller.presentation.velocity;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.roller.RollerException;
import org.roller.model.PlanetManager;
import org.roller.presentation.RollerRequest;
import org.roller.util.Utilities;

/////////////////////////////////////////////////////////////////////////////
/**
 * Planet Roller (i.e. NOT for Planet Tool) RSS feed. 
 * @author David M Johnson
 * @web.servlet name="PlanetFeedServlet"
 * @web.servlet-mapping url-pattern="/planetrss/*"
 */
public class PlanetFeedServlet extends VelocityServlet
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerRequest.class);
    
    public Template handleRequest(HttpServletRequest request,
                                  HttpServletResponse response, Context context)
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
            response.setContentType("application/rss+xml;charset=utf-8");
            PlanetManager planet = 
                rreq.getRoller().getPlanetManager();
            if (request.getParameter("group") != null) 
            {
                context.put("group", 
                        planet.getGroup(request.getParameter("group")));
            }
            context.put("planet", planet);
            context.put("date", new Date());
            context.put("utilities", new Utilities());
            return getTemplate("planetrss.vm");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in PlanetFeedServlet", e);
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
        mLogger.warn("ERROR in PlanetFeedServlet",e);
    }
}

