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
package org.apache.roller.weblogger.ui.core.security;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


public class AuthoritiesPopulator implements LdapAuthoritiesPopulator {

    /**
     * The default global role which will be assigned to incoming users who have
     * been successfully validated in LDAP but are not yet registered in Roller
     */
    private GlobalRole defaultRole = GlobalRole.BLOGGER;

    /* (non-Javadoc)
     * @see org.springframework.security.ldap.LdapAuthoritiesPopulator#getGrantedAuthorities(org.springframework.ldap.core.DirContextOperations, String)
     */
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {

        // This check is probably unnecessary.
        if (userData == null) {
            throw new IllegalArgumentException("The userData argument should not be null at this point.");
        }

        User user;
        GlobalRole role;
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            user = umgr.getUserByUserName(username, Boolean.TRUE);
            if (user != null) {
                role = umgr.getGlobalRole(user);
            } else {
                role = defaultRole;
            }
        } catch (WebloggerException ex) {
            throw new DataRetrievalFailureException("ERROR in user lookup", ex);
        }

        List<GrantedAuthority> authorities = new ArrayList<>(1);
        authorities.add(new SimpleGrantedAuthority(role.name()));

        return authorities;
    }

    public void setDefaultRole(GlobalRole defaultRole) {
        this.defaultRole = defaultRole;
    }

}
