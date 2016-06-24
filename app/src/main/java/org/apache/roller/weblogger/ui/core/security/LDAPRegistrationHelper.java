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

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerCommon.AuthMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads LDAP authentication info and populates any available User fields with its data.
 * ROL-2042 may provide the ability to obtain more fields.
 */
public class LDAPRegistrationHelper {

    private static Logger log = LoggerFactory.getLogger(LDAPRegistrationHelper.class);

    private String ldapUidAttribute = "uid";

    public void setLdapUidAttribute(String ldapUidAttribute) {
        this.ldapUidAttribute = ldapUidAttribute;
    }

    private String ldapNameAttribute = "cn";

    public void setLdapNameAttribute(String ldapNameAttribute) {
        this.ldapNameAttribute = ldapNameAttribute;
    }

    private String ldapScreennameAttribute = "screenname";

    public void setLdapScreennameAttribute(String ldapScreennameAttribute) {
        this.ldapScreennameAttribute = ldapScreennameAttribute;
    }

    private String ldapEmailAttribute = "mail";

    public void setLdapEmailAttribute(String ldapEmailAttribute) {
        this.ldapEmailAttribute = ldapEmailAttribute;
    }

    private String ldapLocaleAttribute = "locale";

    public void setLdapLocaleAttribute(String ldapLocaleAttribute) {
        this.ldapLocaleAttribute = ldapLocaleAttribute;
    }

    public User getUserDetailsFromAuthentication(HttpServletRequest request) {

        if (!(WebloggerStaticConfig.getAuthMethod() == AuthMethod.LDAP)) {
            log.info("LDAP is not enabled. Skipping LDAPRegistrationHelper functionality.");
            return null;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        User ud = new User();
        // setting default
        ud.setId(WebloggerCommon.generateUUID());
        ud.setLocale(Locale.getDefault().toString());
        ud.setDateCreated(Instant.now());

        String userName;
        String email = null;
        String screenName = null;
        String locale = null;
        boolean enabled;

        if (authentication == null) {
            // Try to get SSO data from HttpServletRequest
            userName = getRequestAttribute(request, ldapUidAttribute);
            screenName = getRequestAttribute(request, ldapScreennameAttribute);
            email = getRequestAttribute(request, ldapEmailAttribute);
            locale = getRequestAttribute(request, ldapLocaleAttribute);

            if (userName == null && screenName == null && email == null && locale == null) {
                log.warn("No Authentication found in SecurityContextHolder and HttpServletRequest.");
                return null;
            } else {
                enabled = true;
            }
        } else {
        
            Object oPrincipal = authentication.getPrincipal();
        
            if(oPrincipal == null) {
                log.warn("Principal is null. Skipping auto-registration.");
                return null;
            }
        
            if (!(oPrincipal instanceof UserDetails)) {
                log.warn("Unsupported Principal type in Authentication. Skipping auto-registration.");
                log.warn("oPrincipal found of type {}; needs to be UserDetails", oPrincipal.getClass().getName());
                return null;
            }
        
            UserDetails userDetails = (UserDetails) oPrincipal;
        
            userName = userDetails.getUsername();
            enabled = userDetails.isEnabled();
        
        
            if (userDetails instanceof RollerUserDetails) {
                RollerUserDetails rollerDetails = (RollerUserDetails) userDetails;

                screenName = rollerDetails.getScreenName();
                email = rollerDetails.getEmailAddress();
                locale = rollerDetails.getLocale();
            } // Future: bring in fields from LDAP, see ROL-2042
        }

        ud.setEnabled(enabled ? Boolean.TRUE : Boolean.FALSE);

        ud.setUserName(userName);
        ud.setEmailAddress(email);
        ud.setScreenName(screenName);
        if (locale != null) {
            ud.setLocale(locale);
        }

        return ud;
    }
    
    private String getLdapAttribute(Attributes attributes, String name) {
        if(attributes == null) {
            return null;
        }
        
        Attribute attribute = attributes.get(name);
        
        if(attribute == null) {
            return null;
        }
        
        Object oValue;
        try {
            oValue = attribute.get();
        } catch (NamingException e) {
            return null;
        }
        
        if (oValue == null) {
            return null;
        }
        
        return oValue.toString();
    }

    private String getRequestAttribute(HttpServletRequest request, String attributeName) {

        String attr = null;
        Object attrObj = request.getAttribute(attributeName);
        if (attrObj instanceof String) {
            attr = (String)attrObj;
        } else if (attrObj instanceof Set) {
            Set attrSet = (Set)attrObj;           
            if (!attrSet.isEmpty()) {
                attr = (String)attrSet.iterator().next();
            }
        }

        return attr;
    }
    
}
