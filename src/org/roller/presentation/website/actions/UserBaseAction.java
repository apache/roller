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

package org.roller.presentation.website.actions;

import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.config.RollerConfig;
import org.roller.presentation.website.formbeans.UserFormEx;


/////////////////////////////////////////////////////////////////////////////
/**
 * Base class for user actions.
 */
public class UserBaseAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserBaseAction.class);
    
    protected static String DEFAULT_ALLOWED_CHARS = "A-Za-z0-9";    

    //------------------------------------------------------------------------
    /** Validate user form. TODO: replace with Struts validation. */
    protected ActionMessages validate( UserFormEx form, ActionMessages errors ) {

    	String allowed = RollerConfig.getProperty("username.allowedChars");
    	if(allowed == null || allowed.trim().length() == 0) {
    	       allowed = DEFAULT_ALLOWED_CHARS;
    	}
    	String safe = CharSetUtils.keep(form.getUserName(), allowed);
    	
        if ( "".equals(form.getUserName().trim()))
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("error.add.user.missingUserName"));
        }
        else if ( !safe.equals(form.getUserName()) )
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("error.add.user.badUserName"));
        }

        if ( "".equals(form.getEmailAddress().trim()))
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("error.add.user.missingEmailAddress"));
        }
        return errors;
    }
}






