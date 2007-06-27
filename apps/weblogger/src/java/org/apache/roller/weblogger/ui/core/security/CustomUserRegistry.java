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

import java.util.Locale;
import java.util.TimeZone;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;

/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 *
 */
public class CustomUserRegistry {
    
    private static Log log = LogFactory.getLog(CustomUserRegistry.class);
    
    private static String DEFAULT_SNAME_LDAP_ATTRIBUTE = "screenname";
    private static String DEFAULT_NAME_LDAP_ATTRIBUTE = "cn";
    private static String DEFAULT_EMAIL_LDAP_ATTRIBUTE = "mail";
    private static String DEFAULT_LOCALE_LDAP_ATTRIBUTE = "locale";
    private static String DEFAULT_TIMEZONE_LDAP_ATTRIBUTE = "timezone";
    
    private static String SNAME_LDAP_PROPERTY = "users.sso.registry.ldap.attributes.screenname";
    private static String NAME_LDAP_PROPERTY = "users.sso.registry.ldap.attributes.name";
    private static String EMAIL_LDAP_PROPERTY = "users.sso.registry.ldap.attributes.email";
    private static String LOCALE_LDAP_PROPERTY = "users.sso.registry.ldap.attributes.locale";
    private static String TIMEZONE_LDAP_PROPERTY = "users.sso.registry.ldap.attributes.timezone";
    
    public static User getUserDetailsFromAuthentication() {
        boolean usingSSO = WebloggerConfig.getBooleanProperty("users.sso.enabled");
        if(!usingSSO) {
            log.info("SSO is not enabled. Skipping CustomUserRegistry functionality.");
            return null;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if(authentication == null) {
            log.warn("No Authentication found in SecurityContextHolder.");
            return null;
        }
        
        Object oPrincipal = authentication.getPrincipal();
        
        if(oPrincipal == null) {
            log.warn("Principal is null. Skipping auto-registration.");
            return null;
        }
        
        if (!(oPrincipal instanceof UserDetails)) {
            log.warn("Unsupported Principal type in Authentication. Skipping auto-registration.");
            return null;
        }
        
        UserDetails userDetails = (UserDetails) oPrincipal;
        
        String userName = userDetails.getUsername();
        String password = userDetails.getPassword();
        boolean enabled = userDetails.isEnabled();
        
        User ud = new User();
        ud.setId(null);
        ud.setUserName(userName);
        
        boolean storePassword = WebloggerConfig.getBooleanProperty("users.sso.passwords.save");
        if(!storePassword) {
            password = WebloggerConfig.getProperty("users.sso.passwords.defaultValue","<unknown>");
        }
        ud.setPassword(password);
        ud.setEnabled(enabled ? Boolean.TRUE : Boolean.FALSE);
        ud.setLocale(Locale.getDefault().toString());
        ud.setTimeZone(TimeZone.getDefault().getID());
        ud.setDateCreated(new java.util.Date());
        
        if(userDetails instanceof RollerUserDetails) {
            RollerUserDetails rollerDetails = (RollerUserDetails) userDetails;
            
            ud.setScreenName(rollerDetails.getScreenName());
            
            ud.setFullName(rollerDetails.getFullName());

            //TODO: Bug here as setting email addy to a full name value?
            ud.setEmailAddress(rollerDetails.getFullName());
            if(rollerDetails.getTimeZone() != null) {
                ud.setTimeZone(rollerDetails.getTimeZone());
            }
            
            if(rollerDetails.getLocale() != null) {
                ud.setLocale(rollerDetails.getLocale());
            }
            
        } else if(userDetails instanceof LdapUserDetails) {
            LdapUserDetails ldapDetails = (LdapUserDetails) userDetails;
            Attributes attributes = ldapDetails.getAttributes();
            String sname = getLdapAttribute(attributes, WebloggerConfig.getProperty(SNAME_LDAP_PROPERTY, DEFAULT_SNAME_LDAP_ATTRIBUTE));
            String name = getLdapAttribute(attributes, WebloggerConfig.getProperty(NAME_LDAP_PROPERTY, DEFAULT_NAME_LDAP_ATTRIBUTE));
            String email = getLdapAttribute(attributes, WebloggerConfig.getProperty(EMAIL_LDAP_PROPERTY, DEFAULT_EMAIL_LDAP_ATTRIBUTE));

            ud.setScreenName(sname);
            ud.setFullName(name);
            ud.setEmailAddress(email);
            
            String locale = getLdapAttribute(attributes, WebloggerConfig.getProperty(LOCALE_LDAP_PROPERTY, DEFAULT_LOCALE_LDAP_ATTRIBUTE));
            String timezone = getLdapAttribute(attributes, WebloggerConfig.getProperty(TIMEZONE_LDAP_PROPERTY, DEFAULT_TIMEZONE_LDAP_ATTRIBUTE));
            
            if(locale != null) {
                ud.setLocale(locale);
            }
            if(timezone != null) {
                ud.setTimeZone(timezone);
            }
        }
        
        return ud;
    }
    
    private static String getLdapAttribute(Attributes attributes, String name) {
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
        
        if(oValue == null) {
            return null;
        }
        
        return oValue.toString();
    }
    
}
