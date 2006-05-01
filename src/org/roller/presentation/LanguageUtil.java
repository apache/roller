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
/*
 * Filename: LanguageUtil.java
 * 
 * Created on 04-May-04
 */
package org.roller.presentation;

import java.security.Principal;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.roller.pojos.WebsiteData;

/**
 * This class provides utility methods to deal with 
 * multiple languages and other i18n stuff. 
 * 
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.8 $
 */
public class LanguageUtil
{
	private static Log mLogger = 
	   LogFactory.getFactory().getInstance(LanguageUtil.class);

	/**
	 * reference to supported languages
	 */
	public static final String SUPPORTED_LANGUAGES =
		"org.roller.presentation.supported.languages";

	/**
	 * Extracts langauges from the ServletContext. If
	 * not present, returns null.
	 * 
	 * @param ctx
	 * @return
	 */
	public static Locale[] getSupportedLanguages(ServletContext ctx)
	{
		return (Locale[]) ctx.getAttribute(SUPPORTED_LANGUAGES);
	}

	/**
	 * Tests if a language is supported by roller as configured
	 * in the LanguageServlet init paramater.
	 * 
	 * @param lang
	 * @param ctx
	 * @return
	 */
	public static boolean isSupported(String lang, ServletContext ctx)
	{
		return isSupported(createLocale(lang), ctx);
	}
		
	/**
	 * Tests if a language is supported by roller as configured
	 * in the LanguageServlet init paramater.
	 * 
	 * If no supported languages are available, false is returned.
	 * 
	 * @param locale
	 * @param ctx
	 * @return
	 */
	public static boolean isSupported(Locale locale, ServletContext ctx)
	{
		mLogger.debug("isSupported( locale = "+locale+" )");
		boolean isSupported = false;
		Locale[] supportedLanguages = getSupportedLanguages(ctx);

		if (supportedLanguages == null)
		{
			return false;
		}
		
		for (int i=0; i<supportedLanguages.length; i++) 
		{
			Locale l = (Locale) supportedLanguages[i];
			if (l.equals(locale)) {
				isSupported = true;
				break;
			} else {
				Locale langOnly = new Locale(locale.getLanguage());
				if (l.equals(langOnly)) {
					isSupported = true;
					break;
				}
			}
		}

		return isSupported;
	}

	/**
	 * This method returns the Locale in which the current viewer wants
	 * to view the website. This 'View Locale' is derived as follows:
	 * <ul>
	 * <li>look for existing Locale in Session</li>
	 * <li>if not available, get Locale from request</li>
	 * <li>if not available, use {@link org.roller.presentation.LanguageUtil.getDefaultLocale()} 
	 * (= Locale from <code>WebsiteData</code>)</li>
	 * <li>if a Locale is available in the request, verify it against the locales that 
	 * are supported by Roller; if it is not supported replace it with the default Locale</li>
	 * 
	 * The reason why I don't want to resort to the standard default mechanism of the
	 * Java ResourceBundles, is that this only works for the messages and not for
	 * other things like the dates and calendars (standard Java classes supports all locales). 
	 * I think it looks silly to have the dates and calendars appear in French (e.g.) while 
	 * the messages appear in English.
	 * 
	 * @param request
	 * @return
	 */
	public static Locale getViewLocale(HttpServletRequest request)
	{
		mLogger.debug("getViewLocale()");
		Locale viewLocale = null;

	    // if user is logged in and the user looking at his own website, 
        // take website-locale user's don;t have locales, websites do and a user
        // can belong to multiple websites, each with a different locale.
        // So this check no longer makes sense.
		//if (isWebsiteOfPrincipal(request)) 
        //{
			//viewLocale = getDefaultLocale(request);
			//mLogger.debug("websiteLocale = "+viewLocale);
		//} 
        
        if (request.getSession(false) != null) 
        {
			// check session for existing Locale
			viewLocale = (Locale) request.getSession().getAttribute(Globals.LOCALE_KEY);
			mLogger.debug("sessionLocale = "+viewLocale);
		}

		// if not found, look in many places
		if (viewLocale == null)
		{
			// get from request
			viewLocale = request.getLocale();
			mLogger.debug("requestLocale = "+viewLocale);

			//still not there? take default
			if (viewLocale == null)
			{
				viewLocale = getDefaultLocale(request);
				mLogger.debug("defaultLocale = "+viewLocale);
			}
		}
		
		/*
		 * If viewLocale is not supported, switch back to default.
		 * 
		 * Note: I do this here under the assumption
		 * that the Locale in the Session is always supported. So,
		 * no checks on the Session Locale.
		 */
        ServletContext ctx = RollerContext.getServletContext();
		if (!LanguageUtil.isSupported(viewLocale, ctx))
		{
			viewLocale = Locale.getDefault();
		}
		mLogger.debug("return Locale = "+viewLocale);
		
		// add to session (for Velocity text tool)
		request.getSession().setAttribute(Globals.LOCALE_KEY, viewLocale);
		return viewLocale;
	}
	
	/**
	 * Returns the default website locale using <code>WebsiteData.getLocaleInstance()</code> and
	 * returns <code>null</code> if the WebsiteData object is <code>null</code>.
	 * 
	 * Note: This <code>null</code> situation occurs if a User logs in, but his or her website has
	 * been disabled. The website data is not loaded in that case.
	 * 
	 * @param request
	 * @return
	 */
	public static Locale getDefaultLocale(HttpServletRequest request) {
		mLogger.debug("getDefaultLocale()");
		RollerRequest rreq = RollerRequest.getRollerRequest(request);
		WebsiteData website = rreq.getWebsite();
		if (website==null) {
			return null;
		} else {
			return website.getLocaleInstance();
		}
	}
	
	/**
	 * Verifies if the user is logged in and if so, if the website he
	 * is currently viewing, is the user's website. In that case, true is returned.
	 * Otherwise false.
	 * 
	 * The reason for this additional check is that I only want to enforce the
	 * user's own locale if he at his own website. If I didn't, a logged-in user would be forced to 
	 * view other sites in their default locale. That could lead to confusing situations.
	 * 
	 * TODO: Maybe what I am saying is: shouldn't we store the user's locale as the user's locale,
	 * instead of as the website's locale?
	 * 
	 * @param request
	 * @return
	 */
//	public static boolean isWebsiteOfPrincipal(HttpServletRequest request) {
//		boolean result = false;
//		RollerRequest rreq = RollerRequest.getRollerRequest(request);
//		if (rreq != null) {            
//            WebsiteData website = rreq.getWebsite();
//            Principal principal = request.getUserPrincipal(); 
//            if (website != null && principal != null) {
//                return website.getUser().getUserName().equals(principal.getName());
//            }
//        }
//        return result;
//	}
	
	/**
	 * Helper method to convert a language string to a <code>Locale</code> object.
	 * Example language stringds are 'en' or 'en_US'.
	 * 
	 * @param lang
	 * @return
	 */
	public static Locale createLocale(String lang) {
		mLogger.debug("createLocale( lang = "+lang+" )");
		Locale l = null;
		if (lang.indexOf("_")>0) {
			String tmpLang = lang.substring(0, lang.indexOf("_"));
			String tmpCoun = lang.substring(lang.indexOf("_")+1, lang.length());
			l = new Locale(tmpLang, tmpCoun); 
		} else
		{
			l = new Locale(lang);
		}
		return l;
	}

	/**
	 * Helper method to extract a collection of <code>Locale</code> from the
	 * supported languages config string for the LanguageServlet.
	 *  
	 * @param supportedLanguages
	 * @return
	 */
	public static Locale[] extractLanguages(String supportedLanguages) {
		mLogger.debug("extractLanguages( lang = "+supportedLanguages+" )");
		
		Vector langs = new Vector();
		StringTokenizer st = new StringTokenizer(supportedLanguages, ",");
		while (st.hasMoreTokens())
		{
			Locale l = LanguageUtil.createLocale(st.nextToken());
			langs.add(l);
		}
		mLogger.debug("supported languages: "+langs);
		
		return (Locale[]) langs.toArray(new Locale[langs.size()]);
	}

}
