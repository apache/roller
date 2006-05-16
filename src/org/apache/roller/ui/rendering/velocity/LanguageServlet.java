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
 * Filename: LanguageServlet.java
 *
 * Created on 02-May-04
 */
package org.apache.roller.ui.rendering.velocity;

import java.io.IOException;
import java.util.Locale;

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
import org.apache.roller.ui.core.LanguageUtil;
import org.apache.roller.ui.core.RollerContext;


/**
 * Changes the language of the current Locale to the language specified
 * by the user. The new language must be supported by Roller.
 *
 * And new supported languages to the web.servlet-init-param value. Make sure you add language-only
 * values at the end of a chain. So "en_US,en" instead of "en,en_US". And no spaces.
 *
 * @web.servlet name="LanguageServlet" load-on-startup="10"
 * @web.servlet-init-param name="org.apache.roller.presentation.supported.languages" value="en,nl,zh_cn,zh_tw,vi"
 *
 * @web.servlet-mapping url-pattern="/language/*"
 *
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.8 $
 */
public class LanguageServlet extends PageServlet {
    
    static final long serialVersionUID = -6548723098429557183L;
    
    private static Log mLogger = LogFactory.getLog(LanguageServlet.class);
    
    
    /**
     * @see org.apache.roller.presentation.velocity.BasePageServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        
        super.init(config);
        
        // load supported languages
        ServletContext ctx = config.getServletContext();
        String supportedLanguages =
                config.getInitParameter(LanguageUtil.SUPPORTED_LANGUAGES);
        if (supportedLanguages != null
                && supportedLanguages.trim().length() > 0) {
            // extract langauges
            ctx.setAttribute(
                    LanguageUtil.SUPPORTED_LANGUAGES,
                    LanguageUtil.extractLanguages(supportedLanguages));
        }
    }
    
    
    /**
     * @see org.apache.roller.presentation.velocity.BasePageServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)
     */
    public Template handleRequest(HttpServletRequest request,
                                HttpServletResponse response,
                                Context ctx) 
            throws Exception {
        
        mLogger.debug("Processing language change...");
        ServletContext servletContext = RollerContext.getServletContext();
        
        Locale[] supportedLanguages =
                LanguageUtil.getSupportedLanguages(servletContext);
        
        if (supportedLanguages == null || supportedLanguages.length == 0) {
            // add error message
            ctx.put("languageError", "Unable to switch language: no supported languages defined.");
            // proceed with request serving
            return super.handleRequest(request, response, ctx);
        }
        
        String newLang = request.getParameter("language");
        mLogger.debug("New language in Request: " + newLang);
        if (newLang == null || newLang.length() == 0) {
            // add error message
            ctx.put("languageError", "Unable to switch language: no new language specified.");
            // proceed with request serving
            return super.handleRequest(request, response, ctx);
        }
        
        Locale newLocale = LanguageUtil.createLocale(newLang);
        
        // verify if new language is supported
        if (!LanguageUtil.isSupported(newLocale, servletContext)) {
            // add error message
            ctx.put("languageError", "Unable to switch language: new language '"+newLang+"' is not supported.");
            // proceed with request serving
            return super.handleRequest(request, response, ctx);
        }
        
        // by now, all should be fine: change Locale
        HttpSession session = request.getSession();
        session.setAttribute(Globals.LOCALE_KEY, newLocale);
        mLogger.debug("Changed language to: " + newLocale);
        
        // proceed with request serving
        return super.handleRequest(request, response, ctx);
    }
    
}
