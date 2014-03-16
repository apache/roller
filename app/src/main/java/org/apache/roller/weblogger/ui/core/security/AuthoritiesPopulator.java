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

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 */
public class AuthoritiesPopulator implements LdapAuthoritiesPopulator {

    /** A default role which will be assigned to all authenticated users if set */
    private GrantedAuthority defaultRole = null;

    
    /* (non-Javadoc)
     * @see org.springframework.security.ldap.LdapAuthoritiesPopulator#getGrantedAuthorities(org.springframework.ldap.core.DirContextOperations, String)
     */
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {

        // This check is probably unnecessary.
        if (userData == null) {
            throw new IllegalArgumentException("The userData argument should not be null at this point.");
        }

        User user = null;
        List<String> roles = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            UserManager umgr = roller.getUserManager();
            user = umgr.getUserByUserName(username, Boolean.TRUE);
            if (user != null) {
                roles = umgr.getRoles(user);
            }
        } catch (WebloggerException ex) {
            throw new DataRetrievalFailureException("ERROR in user lookup", ex);
        }

        if (user == null) {
            throw new UsernameNotFoundException("ERROR user: " + username + " not found while granting authorities");
        }

        int roleCount = roles.size() + (defaultRole != null ? 1 : 0);
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(roleCount); // SimpleGrantedAuthority[roleCount];
        int i = 0;
        for(String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        
        if (defaultRole != null) {
            authorities.add(defaultRole);
        }

        if (authorities.size() == 0) {
            // TODO: This doesn't seem like the right type of exception to throw here, but retained it, fixed the message
            throw new UsernameNotFoundException("User " + username + " has no roles granted and there is no default role set.");
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
        this.defaultRole = new SimpleGrantedAuthority(defaultRole);
    }
}
