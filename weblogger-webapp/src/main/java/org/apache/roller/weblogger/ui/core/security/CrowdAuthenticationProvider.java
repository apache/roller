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
package org.apache.roller.weblogger.ui.core.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.CredentialsExpiredException;
import org.springframework.security.DisabledException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.SpringSecurityMessageSource;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.CrowdClient;

/**
 * Provides authentication and permissions assignment from a Atlassian Crowd instance.  Must have the crowd settings in
 * the roller-custom.properties file.  Here is an example file:
 * <br/>
 * 	<pre>
 *		#required fields
 *		crowd.application.name=roller
 *		crowd.application.password=password
 *		crowd.port=8095
 *		crowd.host=localhost
 *		crowd.context=crowd
 *		#end required fields
 *		#this setting allows the use of https, defaults to false; not present we will use plain socket.
 *		crowd.useSecureConnection=false
 *	 	crowd.default.timezone=
 *		crowd.default.locale=
 *	</pre>
 * <br/>
 * If the required fields are not provided crowd authentication is not attempted.  There will be a LOG out if this
 * condition occurs.  
 * <br/>
 * <br/>
 * Here are the other settings needed in the roller-custom.properties file to make CrowdAuthentication work with Roller.
 * <br/>
 *  <pre>
 *		# Crowd Auth, need these settings to be enabled
 *		users.sso.enabled=true
 *		users.sso.autoProvision.enabled=true
 *	</pre>
 * <br/>
 * If these are not set Crowd authentication will not work correctly.  The AutoProvision is what makes this all work, the users from 
 * Crowd and not in Roller will be saved to Rollers db the first time the log in. The reason this is needed is so that permissions can 
 * be written for Roller. Will still need to add some code to ensure when users get promoted or demoted, those changes make it to the
 * Roller DB.
 * <br/>
 * <br/>
 * <b>NOTE:</b> Once an Roller user has been authenticated by Crowd the user account will not longer authenticate through Roller.  If
 * the Crowd user doesn't exist in the Roller db the user will be created in Roller.  There are two types of users "editor" and "admin".
 * If a user doesn't belong to any group when the Roller account is created then the user will only have "editor" rights.  User has 
 * to belong to an "admin" group to be considered an Admin in Roller, an "editor" group is not needed but good to ensure easy administration
 * of users.
 * <br/>
 * @author Nicholas Padilla (<a href="mailto:nicholas@monstersoftwarellc.com">nicholas@monstersoftwarellc.com</a>)
 *
 */
public class CrowdAuthenticationProvider implements AuthenticationProvider {
	private static Log LOG = LogFactory.getLog(CrowdAuthenticationProvider.class);

	private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CrowdClient crowdClient = null;
	private String crowdTimezone = "";
	private String crowdLocale = "";

	public CrowdAuthenticationProvider() {
		String appName = WebloggerConfig.getProperty("crowd.application.name");
		String appPass = WebloggerConfig.getProperty("crowd.application.password");
		String crowdHost = WebloggerConfig.getProperty("crowd.host");
		String crowdPort = WebloggerConfig.getProperty("crowd.port");
		crowdTimezone = WebloggerConfig.getProperty("crowd.default.timezone");
		crowdLocale = WebloggerConfig.getProperty("crowd.default.locale");
		String crowdUrlContext = WebloggerConfig.getProperty("crowd.context");
		boolean useSecureLogin = WebloggerConfig.getBooleanProperty("crowd.useSecureConnection", false);

		if(!appName.isEmpty() 
				&& !appPass.isEmpty() 
				&& !crowdHost.isEmpty()
				&& !crowdPort.isEmpty() 
				&& !crowdUrlContext.isEmpty()){

			String url = "";
			if(useSecureLogin){
				url = "https://";
			} else {
				url = "http://";
			}
			url += crowdHost + ":" + crowdPort + "/" + crowdUrlContext;
			crowdClient = new RestCrowdClientFactory().newInstance(url, appName, appPass);
		}else{
			LOG.warn("Required Crowd Properties Not Found! - Crowd Authentication Not Attempted!");
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.providers.AuthenticationProvider#authenticate(org.springframework.security.Authentication)
	 */
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication, 
				messages.getMessage("AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only UsernamePasswordAuthenticationToken is supported"));

		UsernamePasswordAuthenticationToken authenticationToken = null;
		if(crowdClient != null){
			UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken)authentication;
			String password = (String) authentication.getCredentials();
			String username = userToken.getName();

			Assert.notNull(password, "Null password was supplied in authentication token");

			if(!StringUtils.hasLength(username)) {
				throw new BadCredentialsException(messages.getMessage("CrowdAuthenticationProvider.emptyUsername", "Empty Username"));
			}

			if(password.length() == 0) {
				LOG.debug("Rejecting empty password for user " + username);
				throw new BadCredentialsException(messages.getMessage("CrowdAuthenticationProvider.emptyPassword", "Empty Password"));
			}			

			try {

				User user = crowdClient.authenticateUser(authentication.getName(), authentication.getCredentials().toString());

				GrantedAuthority[] grantedAuthorities = getGrantedAuthorities(user);
				// this is the required constructor, since we don't know any of the boolean values
				// and we can assume if the employee is active and we have gotten this far, these values
				// can be set to the isActive() field on the crowd User object.
				// NOTE: null values for timeZone and locale are okay, they are dealt with at another level.
				CrowdRollerUserDetails crowdRollerUserDetails = 
						new CrowdRollerUserDetails(user, authentication.getCredentials().toString(), crowdTimezone, crowdLocale, grantedAuthorities);

				authenticationToken = new UsernamePasswordAuthenticationToken(crowdRollerUserDetails, authentication.getCredentials(), grantedAuthorities);

			} catch (UserNotFoundException e) {
				throw new UsernameNotFoundException(e.getMessage(), e);
			} catch (InactiveAccountException e) {
				throw new DisabledException(e.getMessage(), e);
			} catch (ExpiredCredentialException e) {
				throw new CredentialsExpiredException(e.getMessage(), e);
			} catch (InvalidAuthenticationException e) {
				throw new BadCredentialsException(e.getMessage(), e);
			} catch (ApplicationPermissionException e) {
				throw new AuthenticationServiceException(e.getMessage(), e);
			} catch (OperationFailedException e) {
				throw new AuthenticationServiceException(e.getMessage(), e);
			}
		}
		return authenticationToken;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.providers.AuthenticationProvider#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}

	private List<String> getPermissions(User user)	throws OperationFailedException, InvalidAuthenticationException,
															ApplicationPermissionException, UserNotFoundException {
		List<String> authorities = new ArrayList<String>();
		// not sure why this is needed??
		authorities.add("ROLE_USER");

		// Optional: Define granted authorities based on
		// groups to which the user is a member.
		List<Group> groups = crowdClient.getGroupsForUser(user.getName(), 0, -1);
		for(Group group: groups) {
			if(group.isActive()){
				if (group.getName().contains("admin") || group.getName().contains("ADMIN")){
					// setup admin here
					authorities.add("admin");
				}else if(group.getName().contains("editor") || group.getName().contains("EDITOR")){
					// setup editor
					authorities.add("editor");
				}
			}
		}
		return authorities;
	}

	private GrantedAuthority[] getGrantedAuthorities(User user) throws UserNotFoundException, OperationFailedException, 
																		InvalidAuthenticationException, ApplicationPermissionException {
		List<String> roles = getPermissions(user);
		GrantedAuthority[] authorities = new GrantedAuthorityImpl[roles.size()];
		int i = 0;
		for (String role : roles) {
			authorities[i++] = new GrantedAuthorityImpl(role);
		}
		return authorities;
	}

}
