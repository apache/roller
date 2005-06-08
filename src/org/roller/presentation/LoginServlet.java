package org.roller.presentation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.pojos.RollerConfig;
import org.roller.util.Utilities;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Implementation of <strong>HttpServlet</strong> that is used
 * to get a username and password and mEncrypt the password
 * before sending to container-managed authentication.
 *
 * <p><a href="LoginServlet.java.html"><i>View Source</i></a></p>
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @version $Revision: 1.9 $ $Date: 2004/07/30 03:43:41 $
 *
 * @web.servlet
 *     display-name="Login Servlet"
 *     load-on-startup="3"
 *     name="login"
 *
 * @web.servlet-init-param
 *     name="authURL"
 *     value="/j_security_check"
 *
 * @web.servlet-mapping
 *     url-pattern="/auth/*"
 */
public class LoginServlet extends HttpServlet
{
    protected static String mAuthURL = "/j_security_check";
    protected static String mAlgorithm = "SHA";
    protected static Boolean mEncrypt = Boolean.FALSE;

    //=========================================================================
    // Private Member Variables
    //=========================================================================
    private Log mLogger = LogFactory.getLog(LoginServlet.class);

    // --------------------------------------------------------- Public Methods

    /**
     * Validates the Init and Context parameters, configures authentication URL
     *
     * @throws ServletException if the init parameters are invalid or any
     * other problems occur during initialisation
     */
    public void init() throws ServletException
    {
        // Get the container authentication URL for FORM-based Authentication
        // J2EE spec says should be j_security_check
        if (getInitParameter("authURL") != null) 
        {
        	mAuthURL = getInitParameter("authURL");
        }
    }

    /**
     * Route the user to the execute method
     *
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
               throws IOException, ServletException
    {
        execute(request, response);
    }

    /**
     * Route the user to the execute method
     *
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException
    {
        execute(request, response);
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     *
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void execute(HttpServletRequest request, 
                        HttpServletResponse response) throws IOException, 
                                                             ServletException
    {
        // if user is already authenticated, it means they probably bookmarked
        // or typed in the URL to login.jsp directly, route them to the main
        // menu if this is the case
        if (request.getRemoteUser() != null) {
            if (mLogger.isDebugEnabled()) {
                mLogger.debug("User '" + request.getRemoteUser() +
                          "' already logged in, routing to main");
            }
            response.sendRedirect(request.getContextPath() + "/main.do");
            return;
        }
        
        // Extract attributes we will need
        String username = request.getParameter("j_username");
        String password = request.getParameter("j_password");
        
        String encryptedPassword = getEncryptedPassword(request, username, password);

        String req = request.getContextPath() + mAuthURL + "?j_username=" + 
                     username + "&j_password=" + encryptedPassword + "&j_uri=" + 
                     request.getParameter("j_uri");

        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Authenticating user '" + username + "'");
        }

        response.sendRedirect(response.encodeRedirectURL(req));
    }

    /**
     * Encode the user's password (if necessary) before redirecting to
     * the Container Managed Security servlet.
     * 
     * @param request
     * @param username
     * @param password
     * @return
     */
    protected String getEncryptedPassword(HttpServletRequest request, 
                                          String username, String password)
    {
        RollerContext rc = RollerContext.getRollerContext( request );
        RollerConfig rollerConfig = rc.getRollerConfig();

        // This determines if the password should be encrypted programmatically 
        mEncrypt = rollerConfig.getEncryptPasswords();
        mAlgorithm = rollerConfig.getAlgorithm();
        
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Authentication URL: " + mAuthURL);
            mLogger.debug("Programmatic encryption of password? " + mEncrypt);
            mLogger.debug("Encryption algorithm: " + mAlgorithm);
        }

        if (request.getParameter("rememberMe") != null) {
            request.getSession().setAttribute(RollerRequest.LOGIN_COOKIE, "true");
        }
        
        String encryptedPassword = "";

        if (mEncrypt.booleanValue() &&
                (request.getAttribute("encrypt") == null))
        {
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("Encrypting password for user '" + username + 
                              "'");
            }
            encryptedPassword = Utilities.encodePassword(password, mAlgorithm);
        }
        else
        {
            encryptedPassword = password;
        }
        return encryptedPassword;
    }
}