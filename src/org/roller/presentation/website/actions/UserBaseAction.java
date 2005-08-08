
package org.roller.presentation.website.actions;

import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.website.formbeans.UserFormEx;
import org.roller.util.DateUtil;
import org.roller.util.Utilities;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeSet;

import org.roller.model.RollerFactory;


/////////////////////////////////////////////////////////////////////////////
/**
 * Base class for user actions.
 */
public class UserBaseAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserBaseAction.class);

    //------------------------------------------------------------------------
    /** Validate user form. TODO: replace with Struts validation. */
    protected ActionMessages validate( UserFormEx form, ActionMessages errors ) {

        String safe = Utilities.replaceNonAlphanumeric(form.getUserName());
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






