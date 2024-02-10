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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * User bean.
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = -6354583200913127874L;
    
    private String  id = UUIDGenerator.generateUUID();
    private String  userName;
    private String  password;
    private String  openIdUrl;
    private String  screenName;
    private String  fullName;
    private String  emailAddress;
    private Date    dateCreated;
    private String  locale;
    private String  timeZone;
    private Boolean enabled = Boolean.TRUE;
    private String  activationCode;
    
    public User() {
    }
    
    public User( String id, String userName,
            String password, String fullName,
            String emailAddress,
            String locale, String timeZone,
            Date dateCreated,
            Boolean isEnabled) {

        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.dateCreated = (Date)dateCreated.clone();
        this.enabled = isEnabled;
        setFullName(fullName);
        setLocale(locale);
        setTimeZone(timeZone);
    }

    /**
     * Id of the User.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    
    /**
     * User name of the user.
     */
    public String getUserName() {
        return this.userName;
    }
    
    public void setUserName( String userName ) {
        this.userName =  HTMLSanitizer.conditionallySanitize(userName);
    }
    
    /**
     * Get password.
     * If password encryption is enabled, will return encrypted password.
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Set password.
     * If password encryption is turned on, then pass in an encrypted password.
     */
    public void setPassword( String password ) {
        this.password = password;
    }
    
    /**
     * Reset this user's password, handles encryption if configured.
     *
     * @param newPassword The new password to be set.
     */
    public void resetPassword(String newPassword) {
        PasswordEncoder encoder = RollerContext.getPasswordEncoder();
        setPassword(encoder.encode(newPassword));
    }

    /**
     * Open ID URL of the user, if provided.
     */
    public String getOpenIdUrl() {
        return openIdUrl;
    }

    public void setOpenIdUrl(String openIdUrl) {
        this.openIdUrl =  HTMLSanitizer.conditionallySanitize(openIdUrl);
    }

    /**
     * Screen name of the user.
     */
    public String getScreenName() {
        return this.screenName;
    }
    
    public void setScreenName( String screenName ) {
        this.screenName =  HTMLSanitizer.conditionallySanitize(screenName);
    }
    
    /**
     * Full name of the user.
     */
    public String getFullName() {
        return this.fullName;
    }
    
    public void setFullName( String fullName ) {
        this.fullName =  HTMLSanitizer.conditionallySanitize(fullName);
    }

    /**
     * E-mail address of the user.
     */
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    public void setEmailAddress( String emailAddress ) {
        this.emailAddress =  HTMLSanitizer.conditionallySanitize(emailAddress);
    }
    
    
    /**
     * The date the user was created.
     */
    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        } else {
            return (Date)dateCreated.clone();
        }
    }
    
    public void setDateCreated(final Date date) {
        if (date != null) {
            dateCreated = (Date)date.clone();
        } else {
            dateCreated = null;
        }
    }

    /**
     * Locale of the user, must be valid Java locale.
     */
    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Timezone of the user, must be valid Java timezone.
     */
    public String getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    
    /**
     * Is this user account enabled?  Disabled accounts cannot login.
     */
    public Boolean getEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getActivationCode() {
        return activationCode;
    }
    
    public void setActivationCode(String activationCode) {
        this.activationCode =  HTMLSanitizer.conditionallySanitize(activationCode);
    }
    
     
    public boolean hasGlobalPermission(String action) {
        return hasGlobalPermissions(Collections.singletonList(action));
    }
    
    public boolean hasGlobalPermissions(List<String> actions) {
        try {
            GlobalPermission perm = new GlobalPermission(actions);
            return WebloggerFactory.getWeblogger().getUserManager().checkPermission(perm, this);
        } catch (WebloggerException ex) {
            return false;
        }
    }

    //------------------------------------------------------- Good citizenship
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getUserName());
        buf.append(", ").append(getFullName());
        buf.append(", ").append(getEmailAddress());
        buf.append(", ").append(getDateCreated());
        buf.append(", ").append(getEnabled());
        buf.append("}");
        return buf.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        User o = (User)other;
        return new EqualsBuilder().append(getUserName(), o.getUserName()).isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).toHashCode();
    }

}
