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

import org.springframework.security.GrantedAuthority;

import com.atlassian.crowd.model.user.User;

/**
 * Provides a {@link RollerUserDetails} that gets populated by the Atlassian Crowd Spring Security integration so that Roller 
 * can correctly build a Roller {@link org.apache.roller.weblogger.pojos.User}.
 * @author Nicholas Padilla (<a href="mailto:nicholas@monstersoftwarellc.com">nicholas@monstersoftwarellc.com</a>)
 *
 */
public class CrowdRollerUserDetails implements RollerUserDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7160979756917382584L;
	private User user;
	private String password;
	private String timeZone;
	private String locale;
	private GrantedAuthority[] grantedAuthorities;

	/**
	 * This constructor will build all the needed data needed to correctly authenticate
	 * and persist a roller user. This is needed because of the need to control the 
	 * actions based on roller_user_permissions table.  Authentication will still 
	 * happen on Crowd side.
	 * @param user
	 * @param password
	 * @param timeZone
	 * @param locale
	 * @param grantedAuthorities
	 */
	public CrowdRollerUserDetails(User user, String password, String timeZone,
			String locale, GrantedAuthority[] grantedAuthorities) {
		super();
		this.user = user;
		this.password = password;
		this.timeZone = timeZone;
		this.locale = locale;
		this.grantedAuthorities = grantedAuthorities;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#getAuthorities()
	 */
	public GrantedAuthority[] getAuthorities() {
		return grantedAuthorities;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#getUsername()
	 */
	public String getUsername() {
		return user.getName();
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isAccountNonExpired()
	 */
	public boolean isAccountNonExpired() {
		return user.isActive();
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isAccountNonLocked()
	 */
	public boolean isAccountNonLocked() {
		return user.isActive();
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	public boolean isCredentialsNonExpired() {
		return user.isActive();
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetails#isEnabled()
	 */
	public boolean isEnabled() {
		return user.isActive();
	}

	/* (non-Javadoc)
	 * @see org.apache.roller.weblogger.ui.core.security.RollerUserDetails#getTimeZone()
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/* (non-Javadoc)
	 * @see org.apache.roller.weblogger.ui.core.security.RollerUserDetails#getLocale()
	 */
	public String getLocale() {
		return locale;
	}

	/* (non-Javadoc)
	 * @see org.apache.roller.weblogger.ui.core.security.RollerUserDetails#getScreenName()
	 */
	public String getScreenName() {
		return user.getDisplayName();
	}

	/* (non-Javadoc)
	 * @see org.apache.roller.weblogger.ui.core.security.RollerUserDetails#getFullName()
	 */
	public String getFullName() {
		return user.getFirstName() + " " + user.getLastName();
	}

	/* (non-Javadoc)
	 * @see org.apache.roller.weblogger.ui.core.security.RollerUserDetails#getEmailAddress()
	 */
	public String getEmailAddress() {
		return user.getEmailAddress();
	}

}
