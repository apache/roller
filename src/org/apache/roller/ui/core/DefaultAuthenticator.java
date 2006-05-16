/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/

package org.apache.roller.ui.core;

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

