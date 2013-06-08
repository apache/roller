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
 * SchemeEnforcementFilter.java
 *
 * Created on September 16, 2005, 3:17 PM
 */

package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * The SchemeEnforcementFilter is provided for Roller sites that enable secure
 * logins and want to ensure that only login urls are used under https.
 * 
 * @author Allen Gilliland
 * 
 * @web.filter name="SchemeEnforcementFilter"
 */
public class SchemeEnforcementFilter implements Filter {

	private static Log log = LogFactory.getLog(SchemeEnforcementFilter.class);

	private boolean schemeEnforcementEnabled = false;
	private boolean secureLoginEnabled = false;
	private int httpPort = 80;
	private int httpsPort = 443;

	private Set<String> allowedUrls = new HashSet<String>();
	private Set<String> ignored = new HashSet<String>();

	/**
	 * Process filter.
	 * 
	 * We'll take the incoming request and first determine if this is a secure
	 * request. If the request is secure then we'll see if it matches one of the
	 * allowed secure urls, if not then we will redirect back out of https.
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (this.schemeEnforcementEnabled && this.secureLoginEnabled) {

			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;

			if (log.isDebugEnabled())
				log.debug("checking path = " + req.getServletPath());

			if (!request.isSecure()
					&& allowedUrls.contains(req.getServletPath())) {

				// http insecure request that should be over https
				String redirect = "https://" + req.getServerName();

				if (this.httpsPort != 443)
					redirect += ":" + this.httpsPort;

				redirect += req.getRequestURI();

				if (req.getQueryString() != null)
					redirect += "?" + req.getQueryString();

				if (log.isDebugEnabled())
					log.debug("Redirecting to " + redirect);

				res.sendRedirect(redirect);
				return;

			} else if (request.isSecure()
					&& !isIgnoredURL(req.getServletPath())
					&& !allowedUrls.contains(req.getServletPath())) {

				// https secure request that should be over http
				String redirect = "http://" + req.getServerName();

				if (this.httpPort != 80)
					redirect += ":" + this.httpPort;

				redirect += req.getRequestURI();

				if (req.getQueryString() != null)
					redirect += "?" + req.getQueryString();

				if (log.isDebugEnabled())
					log.debug("Redirecting to " + redirect);

				res.sendRedirect(redirect);
				return;
			}
		}

		chain.doFilter(request, response);
	}

	/**
	 * Checks if the url is to be ignored.
	 * 
	 * @param theUrl
	 *            the the url
	 * 
	 * @return true, if the url is to be ignored.
	 */
	private boolean isIgnoredURL(String theUrl) {

		int i = theUrl.lastIndexOf(".");

		if (i <= 0 || i == theUrl.length() - 1)
			return true;

		return ignored.contains(theUrl.substring(i + 1));

	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * Filter init.
	 * 
	 * We are just collecting init properties which we'll use for each request.
	 */
	public void init(FilterConfig filterConfig) {

		// determine if we are doing scheme enforcement
		this.schemeEnforcementEnabled = WebloggerConfig
				.getBooleanProperty("schemeenforcement.enabled");
		this.secureLoginEnabled = WebloggerConfig
				.getBooleanProperty("securelogin.enabled");

		if (this.schemeEnforcementEnabled && this.secureLoginEnabled) {
			// gather some more properties
			String http_port = WebloggerConfig
					.getProperty("securelogin.http.port");
			String https_port = WebloggerConfig
					.getProperty("securelogin.https.port");

			try {
				this.httpPort = Integer.parseInt(http_port);
				this.httpsPort = Integer.parseInt(https_port);
			} catch (NumberFormatException nfe) {
				// ignored ... guess we'll have to use the defaults
				log.warn("error with secure login ports", nfe);
			}

			// finally, construct our list of allowable https urls and ignored
			// resources
			String cfgs = WebloggerConfig
					.getProperty("schemeenforcement.https.urls");
			String[] cfgsArray = cfgs.split(",");
			for (int i = 0; i < cfgsArray.length; i++)
				this.allowedUrls.add(cfgsArray[i]);

			cfgs = WebloggerConfig
					.getProperty("schemeenforcement.https.ignored");
			cfgsArray = StringUtils.stripAll(StringUtils.split(cfgs, ","));
			for (int i = 0; i < cfgsArray.length; i++)
				this.ignored.add(cfgsArray[i]);

			// some logging for the curious
			log.info("Scheme enforcement = enabled");
			if (log.isDebugEnabled()) {
				log.debug("allowed urls are:");
				for (String allowedUrl : allowedUrls)
					log.debug(allowedUrl);
				log.debug("ignored extensions are:");
				for (String ignore : ignored)
					log.debug(ignore);
			}
		}
	}

}
