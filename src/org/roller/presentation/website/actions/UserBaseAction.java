
package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.website.formbeans.UserAdminForm;
import org.roller.presentation.website.formbeans.UserFormEx;
import org.roller.util.Utilities;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.roller.model.RollerFactory;



/////////////////////////////////////////////////////////////////////////////
/**
 * Base class for user actions.
 */
public class UserBaseAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserBaseAction.class);

    protected static Collection locales = null;
    protected static Collection timezones = null;

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

    //-----------------------------------------------------------------------
    /**
     * Load Themes, Timezones, and Locales into the
     * request for use in the UI.  As possible, also
     * set the User's Website's Timezone and Locale
     * into the Form object.
     *
     * @param request
     * @param rreq
     * @param ud
     * @param form
     * @throws RollerException
     */
    protected void loadRequestObjects(
        HttpServletRequest request,
        RollerRequest rreq,
        UserData ud,
        UserFormEx form)
        throws RollerException
    {
        // prepare themes for interface
        ServletContext ctx = rreq.getServletContext();
        RollerContext rollerContext = RollerContext.getRollerContext(ctx);
        List themes = 
                RollerFactory.getRoller().getThemeManager().getEnabledThemesList();
        request.setAttribute( "themes", themes );

        // prepare locales & timezones
        if (ud != null)
        {
            UserManager mgr = rreq.getRoller().getUserManager();
            WebsiteData website = mgr.getWebsite(ud.getUserName());
            form.setLocale( website.getLocale() );
            form.setTimezone( website.getTimezone() );
            form.setTheme( website.getEditorTheme() );
        }
        else
        {
            form.setLocale( Locale.getDefault().getDisplayName() );
            form.setTimezone( TimeZone.getDefault().getID() );
        }
        loadOptionLists(request);
    }

    //-----------------------------------------------------------------------
    /**
     * If necessary, load the available Locales and Timezones
     * into the static members.
     * As a convenience this method places the Collections into
     * request attributes roller.locales and roller.timezones.
     *
     * @author lance.lavandowska
     */
    protected void loadOptionLists(HttpServletRequest request)
    {
        // load Locales if necessary
        if (UserBaseAction.locales == null)
        {
			loadLocaleCollection();
        }
        request.setAttribute("roller.locales", UserBaseAction.locales);

        // load Timezones if necessary
        if (UserBaseAction.timezones == null)
        {
			loadTimeZoneCollection();
        }
        request.setAttribute("roller.timezones", UserBaseAction.timezones);
    }

    //-----------------------------------------------------------------------
	/**
     * LabelValueBeans are Comparable but violate the
     * equals() part of the TreeSet requirements.
     * And the html:options tag won't recognize
     * toString as a property.  So we have to put the
     * Locales into a TreeSet to sort them, then convert
     * them to LabelValueBeans to display them.
     * Glad we only have to do this once.
	 * 
	 */
    protected void loadLocaleCollection() 
    {
		java.util.ArrayList myLocales = new java.util.ArrayList();
		TreeSet locTree = new TreeSet(new org.roller.util.LocaleComparator());
		Locale[] localeArray = Locale.getAvailableLocales();
		for (int i=0; i<localeArray.length; i++)
		{
			locTree.add(localeArray[i]);
		}
		java.util.Iterator it = locTree.iterator();
		while (it.hasNext())
		{
			Locale loc = (Locale)it.next();
		    myLocales.add(new org.apache.struts.util.LabelValueBean(
		       loc.getDisplayName(),
		       loc.toString()));
		}
		UserBaseAction.locales = myLocales;
	}

    //-----------------------------------------------------------------------
    /**
     * html:options tag recognizes "ID" as a property
     * so we don't have to go through all the rigamarole (sp?)
     * that we did for Locales.
     * 
     */
    protected void loadTimeZoneCollection() 
    {
        Date today = new Date();
        java.util.ArrayList myZones = new java.util.ArrayList();
        TreeSet zoneTree = new TreeSet(new org.roller.util.TimeZoneComparator());
        String[] zoneArray = TimeZone.getAvailableIDs();
        for (int i=0; i<zoneArray.length; i++)
        {
            zoneTree.add((TimeZone)TimeZone.getTimeZone(zoneArray[i]));
        }
        java.util.Iterator it = zoneTree.iterator();
        while (it.hasNext())
        {
            StringBuffer sb = new StringBuffer();
            TimeZone zone = (TimeZone)it.next();
            sb.append(zone.getDisplayName(zone.inDaylightTime(today), TimeZone.SHORT));
            sb.append(" - ");
            sb.append(zone.getID());
            myZones.add(new org.apache.struts.util.LabelValueBean(
               sb.toString(),
               zone.getID()));
        }
        UserBaseAction.timezones = myZones;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Check to see if the value of "userEnabled" has changed.
     * If so, update User's Website and save, and refresh the Index page cache.
     * 
     * @param request
     * @param rreq
     * @param uaf
     * @throws RollerException
     */
    protected void refreshIndexCache(HttpServletRequest request,
            RollerRequest rreq, UserAdminForm uaf) throws RollerException
    {
        WebsiteData website = rreq.getRoller().getUserManager().getWebsite(
                uaf.getUserName(), false);
        boolean refreshIndexCache = false;
        if (request.getParameter("userEnabled") == null)
        {
            // only change it if it is a change
            if (uaf.getUserEnabled() == null
                    || website.getIsEnabled().booleanValue())
            {
                uaf.setUserEnabled(Boolean.FALSE);
                refreshIndexCache = true;
            }
        }
        else
        {
            // only change it if it is a change
            if (uaf.getUserEnabled() == null
                    || website.getIsEnabled().booleanValue() == false)
            {
                uaf.setUserEnabled(Boolean.TRUE);
                refreshIndexCache = true;
            }
        }

        if (refreshIndexCache)
        {
            // set Website.isEnabled to match uaf.getUserEnabled()
            website.setIsEnabled(uaf.getUserEnabled());
            website.save();

            // refresh the front page cache
            MainPageAction.flushMainPageCache();
        }
    }
}






