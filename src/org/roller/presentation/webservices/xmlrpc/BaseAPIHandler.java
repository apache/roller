/*
 * Created on Apr 11, 2003
 */
package org.roller.presentation.webservices.xmlrpc;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.roller.config.RollerConfig;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.cache.CacheManager;
import org.roller.util.Utilities;

/**
 * Base API handler does user validation, provides exception types, etc.
 * @author David M Johnson
 */
public class BaseAPIHandler implements Serializable
{
    static final long serialVersionUID = -698186274794937582L;
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(BaseAPIHandler.class);

    public static final int AUTHORIZATION_EXCEPTION = 0001;
    public static final String AUTHORIZATION_EXCEPTION_MSG = 
            "Invalid Username and/or Password";
            
    public static final int UNKNOWN_EXCEPTION = 1000;
    public static final String UNKNOWN_EXCEPTION_MSG = 
            "An error occured processing your request";
    
    public static final int UNSUPPORTED_EXCEPTION = 1001;
    public static final String UNSUPPORTED_EXCEPTION_MSG = 
            "Unsupported method - Roller does not support this method";
            
    public static final int USER_DISABLED = 1002;
    public static final String USER_DISABLED_MSG = 
            "User is disabled";
    
    public static final int WEBLOG_NOT_FOUND = 1003;
    public static final String WEBLOG_NOT_FOUND_MSG = 
            "Weblog is not found or is disabled";
    
    public static final int WEBLOG_DISABLED = 1004;
    public static final String WEBLOG_DISABLED_MSG = 
            "Weblog is not found or is disabled";
    
    public static final int BLOGGERAPI_DISABLED = 1005;
    public static final String BLOGGERAPI_DISABLED_MSG = 
            "Weblog does not exist or XML-RPC disabled in web";
    
    public static final int BLOGGERAPI_INCOMPLETE_POST = 1006;
    public static final String BLOGGERAPI_INCOMPLETE_POST_MSG = 
            "Incomplete weblog entry";
    
    public static final int INVALID_POSTID = 2000;
    public static final String INVALID_POSTID_MSG = 
            "The entry postid you submitted is invalid";
            
    //public static final int NOBLOGS_EXCEPTION = 3000;
    //public static final String NOBLOGS_EXCEPTION_MSG = 
            //"There are no categories defined for your user";

    public static final int UPLOAD_DENIED_EXCEPTION = 4000;
    public static final String UPLOAD_DENIED_EXCEPTION_MSG = 
            "Upload denied";

    //------------------------------------------------------------------------
    public BaseAPIHandler()
    {
    }
    
    //------------------------------------------------------------------------
    //public void prep( HttpServletRequest req )
    //{
        //mRoller = RollerContext.getRoller(req);
        //mContextUrl = RollerContext.getRollerContext(req).getAbsoluteContextUrl(req);
    //

    //------------------------------------------------------------------------
    /**
     * Returns website, but only if user authenticates and is authorized to edit.
     * @param blogid   Blogid sent in request (used as website's hanldle)
     * @param username Username sent in request
     * @param password Password sent in requeset
     */
    protected WebsiteData validate(String blogid, String username, String password) 
    throws Exception
    {
        boolean authenticated = false;
        boolean userEnabled = false;
        boolean weblogEnabled = false;
        boolean apiEnabled = false;
        boolean weblogFound = false;
        WebsiteData website = null;
        UserData user = null;
        try
        {
            // Get Roller request object for current thread
            RollerRequest rreq = RollerRequest.getRollerRequest();
            
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            user = userMgr.getUserByUsername(username);
            userEnabled = user.getEnabled().booleanValue();
            
            website = userMgr.getWebsiteByHandle(blogid);
            if (website != null) {
                weblogFound = true;
                weblogEnabled = website.getEnabled().booleanValue();
                apiEnabled = website.getEnableBloggerApi().booleanValue();
            }
            
            if (user != null)
            {    
                // are passwords encrypted?
                RollerContext rollerContext = 
                    RollerContext.getRollerContext();
                String encrypted = 
                        RollerConfig.getProperty("passwds.encryption.enabled");
                //System.out.print("password was [" + password + "] ");
                if ("true".equalsIgnoreCase(encrypted)) 
                {
                	password = Utilities.encodePassword(password, 
                      RollerConfig.getProperty("passwds.encryption.algorithm"));
                }
                //System.out.println("is now [" + password + "]");
    			   authenticated= user.getPassword().equals(password);
                if (authenticated)
                {
                    //RollerFactory.getRoller().setUser(user);
                }
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR internal error validating user", e);
        }
        
        if ( !authenticated )
        {
            throw new XmlRpcException(
                AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
        if ( !userEnabled )
        {
            throw new XmlRpcException(
                USER_DISABLED, USER_DISABLED_MSG);
        }        
        if ( !weblogEnabled )
        {
            throw new XmlRpcException(
                WEBLOG_DISABLED, WEBLOG_DISABLED_MSG);
        }        
        if ( !weblogFound )
        {
            throw new XmlRpcException(
                WEBLOG_NOT_FOUND, WEBLOG_NOT_FOUND_MSG);
        }        
        if ( !apiEnabled )
        {
            throw new XmlRpcException(
                BLOGGERAPI_DISABLED, BLOGGERAPI_DISABLED_MSG);
        }        
        return website;
    }

    //------------------------------------------------------------------------
    /**
     * Returns true if username/password are valid and user is not disabled.
     * @param username Username sent in request
     * @param password Password sent in requeset
     */
    protected boolean validateUser(String username, String password) 
    throws Exception
    {
        boolean authenticated = false;
        boolean enabled = false;
        UserData user = null;
        try
        {
            // Get Roller request object for current thread
            RollerRequest rreq = RollerRequest.getRollerRequest();
            
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            user = userMgr.getUserByUsername(username);
            
            enabled = user.getEnabled().booleanValue();
            if (enabled)
            {    
                // are passwords encrypted?
                RollerContext rollerContext = 
                    RollerContext.getRollerContext();
                String encrypted = 
                        RollerConfig.getProperty("passwds.encryption.enabled");
                //System.out.print("password was [" + password + "] ");
                if ("true".equalsIgnoreCase(encrypted)) 
                {
                    password = Utilities.encodePassword(password, 
                      RollerConfig.getProperty("passwds.encryption.algorithm"));
                }
                //System.out.println("is now [" + password + "]");
                authenticated = user.getPassword().equals(password);
                if (authenticated)
                {
                    //RollerFactory.getRoller().setUser(user);
                }
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR internal error validating user", e);
        }
        
        if ( !enabled )
        {
            throw new XmlRpcException(
                BLOGGERAPI_DISABLED, BLOGGERAPI_DISABLED_MSG);
        }
        
        if ( !authenticated )
        {
            throw new XmlRpcException(
                AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
        return authenticated;
    }
    
    //------------------------------------------------------------------------
    protected void flushPageCache(WebsiteData website) throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest();
        //PageCacheFilter.removeFromCache( rreq.getRequest(), website);
        CacheManager.invalidate(website);
    }
}
