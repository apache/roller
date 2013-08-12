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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 * 
 */
public class BasicUserAutoProvision implements AutoProvision {

	private static Log log = LogFactory.getFactory().getInstance(BasicUserAutoProvision.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.roller.weblogger.ui.core.security.AutoProvision#execute()
	 */
	public boolean execute(HttpServletRequest request) {
		User ud = CustomUserRegistry.getUserDetailsFromAuthentication(request);

		if (ud != null) {
			UserManager mgr;
			try {
				mgr = WebloggerFactory.getWeblogger().getUserManager();

				// need to give an id to the new user if none exist
				if (ud.getId() == null) {
					ud.setId(UUIDGenerator.generateUUID());
				}
				mgr.addUser(ud);

				// for some reason the User object doesn't contain a isAdmin setting
				// so it makes it difficult to add grants without that info, so setting
				// them manually here
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				for (GrantedAuthority auth : authentication.getAuthorities()) {
					if (auth.getAuthority().contains("admin") || auth.getAuthority().contains("ADMIN")) {
						mgr.grantRole("admin", ud);
					}
				}
				WebloggerFactory.getWeblogger().flush();

			} catch (WebloggerException e) {
				log.warn("Error while auto-provisioning user from SSO.", e);
			}
		}

		return true;
	}
}
