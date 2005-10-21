
package org.roller.presentation;

import javax.servlet.http.HttpServletRequest;

/** Interface used by Roller to check user authentication and role */
public interface Authenticator
{
/** Return the name of the request's authenticated user, or null if none */
public String getAuthenticatedUserName( HttpServletRequest req );

/** Return true if authenticated user is in the specified role */
public boolean isAuthenticatedUserInRole( HttpServletRequest req, String role);
}

