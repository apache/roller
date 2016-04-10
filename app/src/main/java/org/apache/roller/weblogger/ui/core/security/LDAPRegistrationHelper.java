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

import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.WebloggerCommon.AuthMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.User;

/**
 * Reads LDAP authentication info and populates any available User fields with its data.
 * ROL-2042 may provide the ability to obtain more fields.
 */
public class LDAPRegistrationHelper {
    
    private static final Log log = LogFactory.getLog(LDAPRegistrationHelper.class);

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

    private String ldapTimezoneAttribute = "timezone";

    public void setLdapTimezoneAttribute(String ldapTimezoneAttribute) {
        this.ldapTimezoneAttribute = ldapTimezoneAttribute;
    }

    public User getUserDetailsFromAuthentication(HttpServletRequest request) {

        if (!(WebloggerStaticConfig.getAuthMethod() == AuthMethod.LDAP)) {
            log.info("LDAP is not enabled. Skipping LDAPRegistrationHelper functionality.");
            return null;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        User ud = new User();
        // setting default
        ud.setId(null);
        ud.setLocale(Locale.getDefault().toString());
        ud.setTimeZone(TimeZone.getDefault().getID());
        ud.setDateCreated(new java.util.Date());

        String userName;
        String fullName = null;
        String email = null;
        String screenName = null;
        String locale = null;
        String timezone = null;
        boolean enabled;

        if (authentication == null) {
            // Try to get SSO data from HttpServletRequest
            userName = getRequestAttribute(request, ldapUidAttribute);
            screenName = getRequestAttribute(request, ldapScreennameAttribute);
            fullName = getRequestAttribute(request, ldapNameAttribute);
            email = getRequestAttribute(request, ldapEmailAttribute);
            locale = getRequestAttribute(request, ldapLocaleAttribute);
            timezone = getRequestAttribute(request, ldapTimezoneAttribute);

            if (userName == null && fullName == null && screenName == null &&
                    email == null && locale == null && timezone == null) {

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
                log.warn("oPrincipal found of type " + oPrincipal.getClass().getName() + "; needs to be UserDetails");
                return null;
            }
        
            UserDetails userDetails = (UserDetails) oPrincipal;
        
            userName = userDetails.getUsername();
            enabled = userDetails.isEnabled();
        
        
            if (userDetails instanceof RollerUserDetails) {
                RollerUserDetails rollerDetails = (RollerUserDetails) userDetails;

                screenName = rollerDetails.getScreenName();
                fullName = rollerDetails.getFullName();
                email = rollerDetails.getEmailAddress();
                locale = rollerDetails.getLocale();
                timezone = rollerDetails.getTimeZone();
            } // Future: bring in fields from LDAP, see ROL-2042
        }

        ud.setEnabled(enabled ? Boolean.TRUE : Boolean.FALSE);

        ud.setUserName(userName);
        ud.setFullName(fullName);
        ud.setEmailAddress(email);
        ud.setScreenName(screenName);
        if (locale != null) {
            ud.setLocale(locale);
        }
        if (timezone != null) {
            ud.setTimeZone(timezone);
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
        
        Object oValue  = null;
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
