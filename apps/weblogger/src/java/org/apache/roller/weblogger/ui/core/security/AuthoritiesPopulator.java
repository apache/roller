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

import java.util.Iterator;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.ldap.LdapDataAccessException;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserRole;
import org.springframework.util.Assert;


/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 */
public class AuthoritiesPopulator implements LdapAuthoritiesPopulator {

    /** A default role which will be assigned to all authenticated users if set */
    private GrantedAuthority defaultRole = null;

    
    /* (non-Javadoc)
     * @see org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator#getGrantedAuthorities(org.acegisecurity.userdetails.ldap.LdapUserDetails)
     */
    public GrantedAuthority[] getGrantedAuthorities(LdapUserDetails userDetails) throws LdapDataAccessException {


        User userData = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            UserManager umgr = roller.getUserManager();
            userData = umgr.getUserByUserName(userDetails.getUsername(), Boolean.TRUE);
        } catch (WebloggerException ex) {
            throw new LdapDataAccessException("ERROR in user lookup", ex);
        }

        if (userData == null) {
            throw new LdapDataAccessException("ERROR no user: " + userDetails.getUsername());
        }

        int roleCount = userData.getRoles().size();
        if (defaultRole != null) roleCount++;
        GrantedAuthority[] authorities = new GrantedAuthorityImpl[roleCount];
        int i = 0;
        for (Iterator it = userData.getRoles().iterator(); it.hasNext();) {
            UserRole role = (UserRole) it.next();
            authorities[i++] = new GrantedAuthorityImpl(role.getRole());
        }
        
        if (defaultRole != null) {
            authorities[roleCount-1] = defaultRole;
        }

        if (authorities.length == 0) {
            throw new UsernameNotFoundException("User has no GrantedAuthority");
        }

        return authorities;
    }

    /**
     * The default role which will be assigned to all users.
     *
     * @param defaultRole the role name, including any desired prefix.
     */
    public void setDefaultRole(String defaultRole) {
        Assert.notNull(defaultRole, "The defaultRole property cannot be set to null");
        this.defaultRole = new GrantedAuthorityImpl(defaultRole);
    }
}
