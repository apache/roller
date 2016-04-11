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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.comment;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.util.I18nMessages;
import org.springframework.util.StringUtils;

/**
 * Requires the commenter to authenticate to a central LDAP server.  The property "securityLevel"
 * which sets this property {@link Context#SECURITY_AUTHENTICATION} is not required, will use
 * the settings from the registered service provider if not provided
 */
public class LdapCommentAuthenticator implements CommentAuthenticator {

	private static Log log = LogFactory.getLog(LdapCommentAuthenticator.class);

	private String ldapPort;

	private String ldapHost;

	private String ldapDc;

	private String ldapOu;

	// options are "none" "simple" "strong", not required
	private String securityLevel;

	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}

	public LdapCommentAuthenticator(String host, String port, String dc, String ou) {
		this.ldapHost = host;
		this.ldapPort = port;
		this.ldapDc = dc;
		this.ldapOu = ou;
	}

	public String getHtml(HttpServletRequest request) {
		String ldapUser = "";
		String ldapPass  = "";
		HttpSession session = request.getSession(true);
		if (session.getAttribute("ldapUser") == null) {
			session.setAttribute("ldapUser", "");
			session.setAttribute("ldapPass", "");
		} else {
			// preserve user data
			String ldapUserTemp = request.getParameter("ldapUser");
			String ldapPassTemp = request.getParameter("ldapPass");
			ldapUser = ldapUserTemp != null ? ldapUserTemp : "";
			ldapPass = ldapPassTemp != null ? ldapPassTemp : "";
		}

		I18nMessages messages = I18nMessages.getMessages(request.getLocale());
		String str = "<p>" + messages.getString("comments.ldapAuthenticatorUserName") + "</p>";
		str += "<p><input name=\"ldapUser\" value=\"" + ldapUser + "\"></p>";
		str += "<p>" + messages.getString("comments.ldapAuthenticatorPassword") + "</p>";
		str += "<p><input type=\"password\" name=\"ldapPass\" value=\"" + ldapPass + "\"></p>";
		return str;
	}

	public boolean authenticate(HttpServletRequest request) {
		boolean validUser = false;
		LdapContext context = null;

		boolean propertiesValid = validateLdapProperties(ldapDc, ldapOu, ldapPort, ldapHost);
		
		String ldapUser = request.getParameter("ldapUser");
		String ldapPass = request.getParameter("ldapPass");
		
		boolean userDataValid = validateUsernamePass(ldapUser, ldapPass);
		
		if (propertiesValid && userDataValid) {
			try {
				Hashtable<String,String> env = new Hashtable<>();
				env.put(Context.INITIAL_CONTEXT_FACTORY,  
						"com.sun.jndi.ldap.LdapCtxFactory"); 
				if(securityLevel != null
						&& (securityLevel.equalsIgnoreCase("none")
								|| securityLevel.equalsIgnoreCase("simple")
								|| securityLevel.equalsIgnoreCase("strong"))){
					env.put(Context.SECURITY_AUTHENTICATION, securityLevel);
				}  
				env.put(Context.SECURITY_PRINCIPAL,  getQualifedDc(ldapDc, ldapOu, ldapUser));  
				env.put(Context.SECURITY_CREDENTIALS, ldapPass);
				env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);  
				context = new InitialLdapContext(env, null);
				validUser = true;
				log.info("LDAP Authentication Successful. user: " + ldapUser);
			} catch (Exception e) {
				// unexpected
				log.error(e);
			} finally {
				if(context != null){
					try {
						context.close();
					} catch (NamingException e) {
						log.error(e);
					}
				}
			}
		}
		return validUser;
	}

	/**
	 * Get the qualified username string LDAP expects.
	 */
	private String getQualifedDc(String ldapDc, String ldapOu, String ldapUser) {
		String qualifedDc = "";
		for (String token : StringUtils.delimitedListToStringArray(ldapDc, ",")) {
			if (!qualifedDc.isEmpty()) {
				qualifedDc += ",";
			}
			qualifedDc += "dc=" + token;
		}
		
		return "uid=" + ldapUser + ", ou=" + ldapOu + "," + qualifedDc;
	}

	/**
	 * Validate user provided data.
	 */
	private boolean validateUsernamePass(String ldapUser, String ldapPass) {
		boolean ret = false;
		
		if((ldapUser != null && !ldapUser.isEmpty()) 
				&& (ldapPass != null && !ldapPass.isEmpty())){
			ret = true;
		}
		
		return ret;
	}

	/**
	 * Validate required properties, specified in tightblog-custom.properties.
	 */
	private boolean validateLdapProperties(String ldapDc, String ldapOu, String ldapPort, String ldapHost) {
		boolean ret = false;
		
		if((ldapDc != null && !ldapDc.isEmpty())
				&& (ldapOu != null && !ldapOu.isEmpty())
				&& (ldapPort != null && !ldapPort.isEmpty())
				&& (ldapHost != null && !ldapHost.isEmpty())){
			ret = true;
		}
		
		return ret;
	}

}
