
package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCache;
import org.roller.presentation.website.formbeans.UserFormEx;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * New user form action.
 * @struts.action name="userFormEx" path="/user"
 *  scope="session" parameter="method"
 */
public class UserFormAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserFormAction.class);

    private static Collection locales = null;
    private static Collection timezones = null;

    public ActionForward add(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        UserFormEx form = (UserFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        ServletContext ctx = rreq.getServletContext();
        RollerContext rollerContext = RollerContext.getRollerContext(ctx);
        RollerConfig rollerConfig = rollerContext.getRollerConfig();
        if ( !rollerConfig.getNewUserAllowed().booleanValue() )
        {
            throw new ServletException("New users disabled!");
        }

        ActionErrors errors = validate(form, new ActionErrors());
        if ( !errors.isEmpty() )
        {
            saveErrors(request, errors);
            return mapping.findForward("newUser");
        }

        try
        {
            // Add new user
            UserManager mgr = rreq.getRoller().getUserManager();           

            UserData ud = new UserData();
            form.copyTo(ud, request.getLocale());
            ud.setId(null);
            ud.setDateCreated(new java.util.Date());

            // encrypt the user's password
            if (rollerConfig.getEncryptPasswords().booleanValue()) {
                ud.setPassword(Utilities.encodePassword(ud.getPassword(), 
                                    rollerConfig.getAlgorithm()));
            }
            
            String theme = form.getTheme();
            HashMap pages = rollerContext.readThemeMacros(theme);
            mgr.addUser( ud, pages, theme, form.getLocale(), form.getTimezone() );
            rreq.getRoller().commit();

			// Flush cache so user will immediately appear on index page
            PageCache.removeFromCache( request, ud );
            MainPageAction.flushMainPageCache();
            
			request.getSession(true).removeAttribute("org.roller.users");

            // Determine user's blog URL and show welcome page
            String weblogURL = rollerContext.getAbsoluteContextUrl(request)
                             + "/page/"+ud.getUserName();
            request.setAttribute("weblogURL",weblogURL);

            String rssURL = rollerContext.getAbsoluteContextUrl(request)
                          + "/rss/"+ud.getUserName();
            request.setAttribute("rssURL",rssURL);

            return mapping.findForward("welcome.page");
        }
        catch (RollerException e)
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError(e.getMessage()));
            saveErrors(request,errors);

            form.setUserName("");
        }

        // Error occured, send user back to new user form
        return mapping.findForward("newUser");
    }

    //------------------------------------------------------------------------
    /** Validate user form. TODO: replace with Struts validation. */
    private ActionErrors validate( UserFormEx form, ActionErrors errors ) {

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

        if ( "".equals(form.getPassword().trim()))
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("error.add.user.missingPassword"));
        }

        if ( "".equals(form.getEmailAddress().trim()))
        {
            errors.add( ActionErrors.GLOBAL_ERROR,
               new ActionError("error.add.user.missingEmailAddress"));
        }
        return errors;
    }

    //------------------------------------------------------------------------
    public ActionForward newUser(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("addUser.page");
        ActionErrors errors = new ActionErrors();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        try
        {
        		UserFormEx userForm = (UserFormEx)actionForm;
            loadRequestObjects(request, rreq, null, userForm);
            userForm.setLocale(request.getLocale().toString());
        }
        catch (Exception e)
        {
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.editing.user", e.toString()));
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editUser.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                UserData ud = rreq.getUser();
                request.setAttribute("user",ud);

                UserFormEx form = (UserFormEx)actionForm;
                form.copyFrom(ud, request.getLocale());

                loadRequestObjects(request, rreq, ud, form);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        // if user logged in with a cookie, display a warning that they
        // can't change passwords
        if (mLogger.isDebugEnabled()) 
        {
            log.debug("checking for cookieLogin...");
        }

        if (request.getSession().getAttribute("cookieLogin") != null) {
            ActionMessages messages = new ActionMessages();

            // add warning messages
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                         new ActionMessage("userSettings.cookieLogin"));
            saveMessages(request, messages);
        }
        
        return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward update(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("editUser.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() )
            {
                UserFormEx form = (UserFormEx)actionForm;

                UserManager mgr = rreq.getRoller().getUserManager();
                UserData data = mgr.getUser( form.getUserName() );

                // if an empty/null value is passed in for
                // password, reset it to previous value.
                if ( StringUtils.isEmpty( form.getPassword() ) )
                {
                    form.setPassword( data.getPassword() );
                } 
                
                boolean samePassword = form.getPassword().equals(data.getPassword());
                
                // now get the data value object from UserForm
                form.copyTo(data, request.getLocale());
                
                if (!samePassword) 
                {
                    RollerContext rollerContext = RollerContext.getRollerContext(request);
                    RollerConfig rollerConfig = rollerContext.getRollerConfig();
                    // encrypt the user's password
                    if (rollerConfig.getEncryptPasswords().booleanValue())
                    {
                        data.setPassword(Utilities.encodePassword(form.getPassword(), 
                                         rollerConfig.getAlgorithm()));
                        form.setPassword(data.getPassword());
                    }
                }
            
                WebsiteData website = mgr.getWebsite(data.getUserName());
                website.setEditorTheme(form.getTheme());
                website.setLocale(form.getLocale());
                website.setTimezone(form.getTimezone());

                mgr.storeUser( data );
                mgr.storeWebsite( website );
                rreq.getRoller().commit();

                request.getSession().setAttribute(
                    RollerSession.STATUS_MESSAGE,
                    "Successfully submitted User modifications");

                PageCache.removeFromCache( request,data );

                loadRequestObjects(request, rreq, data, form);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }

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
    private void loadRequestObjects(
        HttpServletRequest request,
        RollerRequest rreq,
        UserData ud,
        UserFormEx form)
        throws RollerException
    {
        // prepare themes for interface
        ServletContext ctx = rreq.getServletContext();
        RollerContext rollerContext = RollerContext.getRollerContext(ctx);
        String[] themes = rollerContext.getThemeNames();
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

    /**
     * If necessary, load the available Locales and Timezones
     * into the static members.
     * As a convenience this method places the Collections into
     * request attributes roller.locales and roller.timezones.
     *
     * @author lance.lavandowska
     */
    private void loadOptionLists(HttpServletRequest request)
    {
        // load Locales if necessary
        if (UserFormAction.locales == null)
        {
			loadLocaleCollection();
        }
        request.setAttribute("roller.locales", UserFormAction.locales);

        // load Timezones if necessary
        if (UserFormAction.timezones == null)
        {
			loadTimeZoneCollection();
        }
        request.setAttribute("roller.timezones", UserFormAction.timezones);
    }

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
	private void loadLocaleCollection() 
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
		UserFormAction.locales = myLocales;
	}

    /**
     * html:options tag recognizes "ID" as a property
     * so we don't have to go through all the rigamarole (sp?)
     * that we did for Locales.
     * 
     */
    private void loadTimeZoneCollection() 
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
        UserFormAction.timezones = myZones;
    }
}






