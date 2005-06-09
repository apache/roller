/*
 * Created on Apr 11, 2003
 */
package org.roller.presentation.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.util.Utilities;

import java.io.Serializable;
import org.roller.config.RollerConfig;

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
    
    public static final int BLOGGERAPI_DISABLED = 1000;
    public static final String BLOGGERAPI_DISABLED_MSG = 
            "You have not enabled Blogger API support for your weblog";
    
    public static final int UNSUPPORTED_EXCEPTION = 1001;
    public static final String UNSUPPORTED_EXCEPTION_MSG = 
            "Unsupported method - Roller does not support this method";
            
    public static final int INVALID_POSTID = 2000;
    public static final String INVALID_POSTID_MSG = 
            "The entry postid you submitted is invalid";
            
    public static final int NOBLOGS_EXCEPTION = 3000;
    public static final String NOBLOGS_EXCEPTION_MSG = 
            "There are no categories defined for your user";

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
    protected WebsiteData validate(String username, String password) throws Exception
    {
        boolean authenticated = false;
        boolean enabled = false;
        WebsiteData website = null;
        try
        {
            // Get Roller request object for current thread
            RollerRequest rreq = RollerRequest.getRollerRequest();
            
            UserManager userMgr = rreq.getRoller().getUserManager();
            website = userMgr.getWebsite(username);
            
            enabled = website.getEnableBloggerApi().booleanValue();
            if (enabled)
            {    
                // are passwords encrypted?
                RollerContext rollerContext = 
                    RollerContext.getRollerContext(rreq.getRequest());
                String encrypted = 
                        RollerConfig.getProperty("passwds.encryption.enabled");
                //System.out.print("password was [" + password + "] ");
                if ("true".equalsIgnoreCase(encrypted)) 
                {
                	password = Utilities.encodePassword(password, 
                      RollerConfig.getProperty("passwds.encryption.algorithm"));
                }
                //System.out.println("is now [" + password + "]");
    			authenticated= website.getUser().getPassword().equals(password);
                if (authenticated)
                {
                    rreq.getRoller().setUser(website.getUser());
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
        return website;
    }

    //------------------------------------------------------------------------
    protected void flushPageCache(String user) throws Exception
    {
        // Get Roller request object for current thread
        RollerRequest rreq = RollerRequest.getRollerRequest();
        
        UserManager umgr = rreq.getRoller().getUserManager();
        UserData ud = umgr.getUser(user);
        
        PageCacheFilter.removeFromCache( rreq.getRequest(), ud );
        MainPageAction.flushMainPageCache();
    }
}
