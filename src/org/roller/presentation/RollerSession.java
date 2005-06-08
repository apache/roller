package org.roller.presentation;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


//////////////////////////////////////////////////////////////////////////////
/**
 * Roller session handles session startup and shutdown.
 * @web.listener
 */
public class RollerSession
    implements HttpSessionListener, HttpSessionActivationListener
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerSession.class);
        
    public static final String BREADCRUMB = "org.roller.breadcrumb";        
    public static final String ERROR_MESSAGE   = "rollererror_message";
    public static final String STATUS_MESSAGE  = "rollerstatus_message";


    //------------------------------------------------------------------------
    /** Create session's Roller instance */
    public void sessionCreated(HttpSessionEvent se)
    {
        // put this in session too, so we can purge on passivation
        se.getSession().setAttribute( "dummy010", this );
        
        RollerContext rctx = RollerContext.getRollerContext(
            se.getSession().getServletContext());
        rctx.sessionCreated(se);           
    }    

    //------------------------------------------------------------------------
    public void sessionDestroyed(HttpSessionEvent se)
    {
        RollerContext rctx = RollerContext.getRollerContext(
            se.getSession().getServletContext());
        rctx.sessionDestroyed(se);           
        
        clearSession(se);        
    }

    //------------------------------------------------------------------------
    /** Init session as if it was new */
    public void sessionDidActivate(HttpSessionEvent se)
    {
    }

    //------------------------------------------------------------------------
    /**
     * Clear bread crumb trail.
     * @param useReferer If true try to return the "referer" header.
     */
    public static void clearBreadCrumbTrail( HttpServletRequest req )
    { 
        HttpSession ses = req.getSession(false);
        if (ses != null && ses.getAttribute(BREADCRUMB) != null)
        {
            ArrayStack stack = (ArrayStack)ses.getAttribute(BREADCRUMB);
            stack.clear();
        }
    }
    
    //------------------------------------------------------------------------
    /**
     * Store the url of the latest request stored in the session.
     * @param useReferer If true try to return the "referer" header.
     */
    public static String getBreadCrumb( 
        HttpServletRequest req, boolean useReferer )
    {
        String crumb = null;
        
        HttpSession ses = req.getSession(false);
        if (ses != null && ses.getAttribute(BREADCRUMB) != null)
        {
            ArrayStack stack = (ArrayStack) ses.getAttribute(BREADCRUMB);
            if (stack != null && !stack.empty())
            {
                crumb = (String)stack.peek();
            }
        }

        if ( crumb == null && useReferer )
        {
            crumb = req.getHeader("referer");
        }
        
        return crumb;
    }
    
    //------------------------------------------------------------------------
    /**
     * Store the url of the latest request stored in the session.
     * Else try to return the "referer" header.
     */
    public static String getBreadCrumb( HttpServletRequest req )
    {
        return getBreadCrumb(req,true);
    }

    //------------------------------------------------------------------------
    /** Purge session before passivation. Because Roller currently does not
      * support session recovery, failover, migration, or whatever you want
      * to call it when sessions are saved and then restored at some later
      * point in time.
      */
    public void sessionWillPassivate(HttpSessionEvent se)
    {
        clearSession(se);
    }

    //------------------------------------------------------------------------    /*
    private  void clearSession( HttpSessionEvent se )
    {
        HttpSession session = se.getSession();
        try
        {
            session.removeAttribute( BREADCRUMB );
            session.removeAttribute("org.roller.users");
        }
        catch (Throwable e)
        {
            if (mLogger.isDebugEnabled())
            {
                // ignore purge exceptions
                mLogger.debug("EXCEPTION PURGING session attributes",e);
            }
        }
    }
}

