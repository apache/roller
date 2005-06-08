/*
 * Filename: LanguageServlet.java
 * 
 * Created on 02-May-04
 */
package org.roller.presentation.velocity;

import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.roller.presentation.LanguageUtil;

/**
 * Changes the language of the current Locale to the language specified
 * by the user. The new language must be supported by Roller.  
 * 
 * @web.servlet name="LanguageServlet" load-on-startup="10"
 * @web.servlet-init-param name="org.roller.presentation.supported.languages" value="en,nl,zh,vi"
 *  
 * @web.servlet-mapping url-pattern="/language/*"
 * 
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.5 $
 */
public class LanguageServlet extends BasePageServlet
{
	/**
	 * Logger
	 */
	private static Log mLogger =
		LogFactory.getFactory().getInstance(LanguageServlet.class);

	/**
	 * @see org.roller.presentation.velocity.BasePageServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		// load supported languages
		ServletContext ctx = config.getServletContext();
		String supportedLanguages =
			config.getInitParameter(LanguageUtil.SUPPORTED_LANGUAGES);
		if (supportedLanguages != null
			&& supportedLanguages.trim().length() > 0)
		{
			// extract langauges
			Vector lang = new Vector();
			StringTokenizer st = new StringTokenizer(supportedLanguages, ",");
			while (st.hasMoreTokens())
			{
				lang.add(st.nextToken());
			}
			mLogger.debug("supported languages: "+lang);
			ctx.setAttribute(LanguageUtil.SUPPORTED_LANGUAGES, lang);
		}
	}

	/**
	 * @see org.roller.presentation.velocity.BasePageServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)
	 */
	public Template handleRequest(
		HttpServletRequest request,
		HttpServletResponse response,
		Context ctx) throws Exception
	{
		ServletContext servletContext =
			request.getSession().getServletContext();
		Vector supportedLanguages =
			LanguageUtil.getSupportedLanguages(servletContext);
		
		if (supportedLanguages==null) 
		{
			// add error message
			ctx.put("languageError", "Unable to switch language: no supported languages defined.");
			// proceed with request serving
			return super.handleRequest(request, response, ctx);
		}

		String newLang = request.getParameter("language");
		if (newLang==null || newLang.length()==0) {
			// add error message
			ctx.put("languageError", "Unable to switch language: no new language specified.");
			// proceed with request serving
			return super.handleRequest(request, response, ctx);
		}

		// verify if new language is supported
		if (!LanguageUtil.isSupported(newLang, servletContext)) {
			// add error message
			ctx.put("languageError", "Unable to switch language: new language '"+newLang+"' is not supported.");
			// proceed with request serving
			return super.handleRequest(request, response, ctx);
		}
		 
		// by now, all should be fine: change Locale,
		// but preserve existing country
		Locale existingLocale = LanguageUtil.getViewLocale(request);
		Locale newLocale = new Locale(newLang, existingLocale.getCountry());

		HttpSession session = request.getSession();
		session.setAttribute(Globals.LOCALE_KEY, newLocale);
		mLogger.debug("Changed language to: "+newLang);

		// proceed with request serving
		return super.handleRequest(request, response, ctx);

	}

}
