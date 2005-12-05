package org.roller.presentation.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.RollerConfig;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.util.RequestUtil;
import org.roller.util.Utilities;


/**
 * <p>Intercepts Login requests for "Remember Me" functionality.</p>
 *
 * @author Matt Raible
 * @version $Revision: 1.4 $ $Date: 2005/05/17 20:30:16 $
 *
 * @web.filter display-name="Login Filter" name="loginFilter"
 * @web.filter-init-param name="enabled" value="true"
 */
public final class LoginFilter implements Filter 
{
    //~ Instance fields ========================================================

    private Log mLogger = LogFactory.getLog(LoginFilter.class);
    private boolean enabled = true;

    //~ Methods ================================================================

    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain)
                  throws IOException, ServletException 
    {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // See if the user has a remember me cookie
        Cookie c = RequestUtil.getCookie(request, RollerRequest.LOGIN_COOKIE);

        try 
        {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
                
            // Check to see if the user is logging out, if so, remove all
            // login cookies
            if (request.getRequestURL().indexOf("logout") != -1) 
            {
                if (mLogger.isDebugEnabled()) 
                {
                    mLogger.debug("logging out '" + request.getRemoteUser() + "'");
                }
    
                mgr.removeLoginCookies(request.getRemoteUser());
                RollerFactory.getRoller().commit();
                RequestUtil.deleteCookie(response, c, request.getContextPath());
            } 
            else if (c != null && enabled) 
            {
                String loginCookie = mgr.checkLoginCookie(c.getValue());
                RollerFactory.getRoller().commit();

                if (loginCookie != null) 
                {
                    RequestUtil.setCookie(response, RollerRequest.LOGIN_COOKIE,
                                          loginCookie,
                                          request.getContextPath());
                    loginCookie = Utilities.decodeString(loginCookie);

                    String[] value = StringUtils.split(loginCookie, '|');

                    UserData user = mgr.getUser( value[0] );

                    // authenticate user without displaying login page
                    String route = "/auth?j_username=" +
                                   user.getUserName() + "&j_password=" +
                                   user.getPassword();

                    request.setAttribute("encrypt", "false");
                    request.getSession().setAttribute("cookieLogin", "true");

                    if (mLogger.isDebugEnabled()) 
                    {
                        mLogger.debug("I remember you '" + user.getUserName() +
                                  "', attempting to authenticate...");
                    }

                    RequestDispatcher dispatcher =
                        request.getRequestDispatcher(route);
                    dispatcher.forward(request, response);

                    return;
                }
            }
                
        } catch (Exception e) 
        {
            // no big deal if cookie-based authentication fails
            mLogger.warn(e.getMessage());
        }

        chain.doFilter(req, resp);
    }

    /**
     * Initialize controller values of filter.
     */
    public void init(FilterConfig config) 
    {
        mLogger.debug("Initializing Login Filter");
        
        String param = RollerConfig.getProperty("loginfilter.rememberme.enabled");
        enabled = Boolean.valueOf(param).booleanValue();

        mLogger.info("Remember Me enabled: " + enabled);

        config.getServletContext().setAttribute("rememberMeEnabled", param);
    }

    /**
     * destroy any instance values other than config *
     */
    public void destroy() {
    }
}
