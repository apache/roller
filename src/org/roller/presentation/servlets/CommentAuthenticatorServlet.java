/*
 * CommentAuthenticatorServlet.java
 *
 * Created on January 5, 2006, 12:37 PM
 */

package org.roller.presentation.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.RollerConfig;
import org.roller.presentation.velocity.CommentAuthenticator;
import org.roller.presentation.velocity.DefaultCommentAuthenticator;


/**
 * The CommentAuthenticatorServlet is used for generating the html used for
 * comment authentication.  This is done outside of the normal rendering process
 * so that we can cache full pages and still set the comment authentication
 * section dynamically.
 *
 * @web.servlet name="CommentAuthenticatorServlet"
 * @web.servlet-mapping url-pattern="/CommentAuthenticatorServlet"
 */
public class CommentAuthenticatorServlet extends HttpServlet {
    
    private static Log mLogger = 
        LogFactory.getLog(CommentAuthenticatorServlet.class);
    
    private CommentAuthenticator authenticator = null;
    
    
    /**
     * Handle incoming http GET requests.
     *
     * We only handle get requests.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        
        PrintWriter out = response.getWriter();
        
        response.setContentType("text/html");
        out.println(this.authenticator.getHtml(null, request, response));
    }
    
    
    /** 
     * Initialization.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // lookup the authenticator we are going to use and instantiate it
        try {
            String name = RollerConfig.getProperty("comment.authenticator.classname");
            
            Class clazz = Class.forName(name);
            this.authenticator = (CommentAuthenticator) clazz.newInstance();
            
        } catch(Exception e) {
            mLogger.error(e);
            this.authenticator = new DefaultCommentAuthenticator();
        }

    }
    
    /** 
     * Destruction.
     */
    public void destroy() {}
    
}
