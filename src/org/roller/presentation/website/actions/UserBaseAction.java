
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






