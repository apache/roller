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
package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.util.StringUtils;

/**
 * Requires the commenter to authenticate to a central LDAP server.  Here are the roller.properties that need to 
 * be present for this {@link CommentAuthenticator} to work correctly:
 * <br/>
 * <pre>
 * 		# default port is 389
 * 		comment.authenticator.ldap.port=389
 * 		# fully qualified host
 * 		comment.authenticator.ldap.host=
 * 		# name of dc to check against 
 * 		comment.authenticator.ldap.dc=
 * 		# csv list of dc names, ex: example,com
 * 		comment.authenticator.ldap.ou=
 * 		# options are "none" "simple" "strong", not required
 * 		comment.authenticator.ldap.securityLevel=
 * </pre>
 * <br/>
 * You can add these properties to the roller-custom.properties to ensure correct operations.  The property "securityLevel
 * is not required, will use the settings from the registered service provider; sets this property {@link Context#SECURITY_AUTHENTICATION}.
 * @author Nicholas Padilla (<a href="mailto:nicholas@monstersoftwarellc.com">nicholas@monstersoftwarellc.com</a>)
 *
 */
public class LdapCommentAuthenticator implements CommentAuthenticator {

	private transient ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

	private static Log LOG = LogFactory.getLog(LdapCommentAuthenticator.class);

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

		StringBuffer sb = new StringBuffer();

		sb.append("<p>");
		sb.append(bundle.getString("comments.ldapAuthenticatorUserName"));
		sb.append("</p>");
		sb.append("<p>");
		sb.append("<input name=\"ldapUser\" value=\"");
		sb.append(ldapUser + "\">");
		sb.append("</p>");
		sb.append("<p>");
		sb.append(bundle.getString("comments.ldapAuthenticatorPassword"));
		sb.append("</p>");
		sb.append("<p>");
		sb.append("<input type=\"password\" name=\"ldapPass\" value=\"");
		sb.append(ldapPass + "\">");
		sb.append("</p>");

		return sb.toString();
	}

	public boolean authenticate(HttpServletRequest request) {
		boolean validUser = false;
		LdapContext context = null;

		String ldapDc = WebloggerConfig.getProperty("comment.authenticator.ldap.dc");
		String ldapOu = WebloggerConfig.getProperty("comment.authenticator.ldap.ou");
		String ldapPort = WebloggerConfig.getProperty("comment.authenticator.ldap.port");
		String ldapHost = WebloggerConfig.getProperty("comment.authenticator.ldap.host");
		String ldapSecurityLevel = WebloggerConfig.getProperty("comment.authenticator.ldap.securityLevel");
		
		boolean rollerPropertiesValid = validateRollerProperties(ldapDc, ldapOu, ldapPort, ldapHost);
		
		String ldapUser = request.getParameter("ldapUser");
		String ldapPass = request.getParameter("ldapPass");
		
		boolean userDataValid = validateUsernamePass(ldapUser, ldapPass);
		
		if(rollerPropertiesValid && userDataValid){
			try {
				Hashtable<String,String> env = new Hashtable<String,String>();  
				env.put(Context.INITIAL_CONTEXT_FACTORY,  
						"com.sun.jndi.ldap.LdapCtxFactory"); 
				if(ldapSecurityLevel != null 
						&& (ldapSecurityLevel.equalsIgnoreCase("none")
								|| ldapSecurityLevel.equalsIgnoreCase("simple")
								|| ldapSecurityLevel.equalsIgnoreCase("strong"))){
					env.put(Context.SECURITY_AUTHENTICATION, ldapSecurityLevel);	
				}  
				env.put(Context.SECURITY_PRINCIPAL,  getQualifedDc(ldapDc, ldapOu, ldapUser));  
				env.put(Context.SECURITY_CREDENTIALS, ldapPass);
				env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);  
				context = new InitialLdapContext(env, null);  
				validUser = true;
				LOG.info("LDAP Authentication Successful. user: " + ldapUser);
			} catch (Exception e) {
				// unexpected
				LOG.error(e);
			} finally {
				if(context != null){
					try {
						context.close();
					} catch (NamingException e) {
						LOG.error(e);
					}
				}
			}
		}
		return validUser;
	}

	/**
	 * Get the username string LDAP expects.
	 * @param ldapDc
	 * @param ldapOu
	 * @param ldapUser
	 * @return
	 */
	private String getQualifedDc(String ldapDc, String ldapOu, String ldapUser) {
		String qualifedDc = "";
		for(String token : StringUtils.delimitedListToStringArray(ldapDc, ",")){
			if(!qualifedDc.isEmpty()){
				qualifedDc += ",";
			}
			qualifedDc += "dc=" + token;
		}
		
		String qualifedUser = "uid=" + ldapUser + ", ou=" + ldapOu + "," + qualifedDc;
		return qualifedUser;
	}

	/**
	 * Validate user provided data.
	 * @param ldapUser
	 * @param ldapPass
	 * @return
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
	 * Validate required roller.properties, specified in custom-roller.properties.
	 * @param ldapDc
	 * @param ldapOu
	 * @param ldapPort
	 * @param ldapHost
	 * @return
	 */
	private boolean validateRollerProperties(String ldapDc, String ldapOu, String ldapPort, String ldapHost) {
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
