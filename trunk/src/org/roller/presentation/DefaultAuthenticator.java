
package org.roller.presentation;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;


/** Class used by Roller to check user authentication and role */
public class DefaultAuthenticator implements Authenticator
{
    /** Return the name of the request's authenticated user, or null if none */
    public String getAuthenticatedUserName( HttpServletRequest req )
    {
        String ret = null;
        Principal prince = req.getUserPrincipal(); 
        if ( prince != null )
        {
            ret = prince.getName();
        }
        return ret;
    }

    /** Return true if authenticated user is in the specified role */
    public boolean isAuthenticatedUserInRole(HttpServletRequest req,String role)
    {
        return req.isUserInRole( role );
    }
}

