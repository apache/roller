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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;


/**
 * User bean.
 */
@Entity
@Table(name="weblogger_user")
@NamedQueries({
        @NamedQuery(name="User.getUserByActivationCode",
                query="SELECT u FROM User u WHERE u.activationCode = ?1"),
        @NamedQuery(name="User.getByEnabled&EndDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.enabled = ?1 AND u.dateCreated < ?2 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getByEndDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.dateCreated < ?1 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getByUserName",
                query="SELECT u FROM User u WHERE u.userName= ?1"),
        @NamedQuery(name="User.getByScreenName",
                query="SELECT u FROM User u WHERE u.screenName= ?1"),
        @NamedQuery(name="User.getByUserName&Enabled",
                query="SELECT u FROM User u WHERE u.userName= ?1 AND u.enabled = ?2"),
        @NamedQuery(name="User.getByEndDate&StartDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.dateCreated < ?1 AND u.dateCreated > ?2 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getGlobalRole",
                query="SELECT u.globalRole FROM User u WHERE u.userName = ?1"),
        @NamedQuery(name="User.getCountEnabledDistinct",
                query="SELECT COUNT(u) FROM User u WHERE u.enabled = ?1")
})
public class User implements Serializable {
    
    public static final long serialVersionUID = -6354583200913127874L;

    private String  id;
    private String  userName;
    private String  password;
    private GlobalRole globalRole;
    private String  screenName;
    private String  emailAddress;
    private Date    dateCreated;
    private String  locale;
    private String  timeZone;
    private Boolean enabled = Boolean.TRUE;
    private String  activationCode;

    // below two fields not persisted but used for password entry and confirmation
    // on new user & user update forms.
    private String passwordText;
    private String passwordConfirm;

    public User() {
    }
    
    public User(String userName,
            String password,
            GlobalRole globalRole,
            String emailAddress,
            String locale, String timeZone,
            Date dateCreated,
            Boolean isEnabled) {
        this.id = WebloggerCommon.generateUUID();
        this.userName = userName;
        this.password = password;
        this.globalRole = globalRole;
        this.emailAddress = emailAddress;
        this.dateCreated = (Date)dateCreated.clone();
        this.locale = locale;
        this.timeZone = timeZone;
        this.enabled = isEnabled;
    }

    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    
    @Basic(optional=false)
    public String getUserName() {
        return this.userName;
    }
    
    public void setUserName( String userName ) {
        this.userName = userName;
    }
    
    /**
     * Get password.
     * If password encryption is enabled, will return encrypted password.
     */
    @Column(name="passphrase", nullable=false)
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

    @Column(name="global_role", nullable=false)
    @Enumerated(EnumType.STRING)
    public GlobalRole getGlobalRole() {
        return this.globalRole;
    }

    @Transient
    public boolean isGlobalAdmin() {
        return this.globalRole == GlobalRole.ADMIN;
    }

    public void setGlobalAdmin(boolean administrator) {
        setGlobalRole(administrator ? GlobalRole.ADMIN : GlobalRole.BLOGGER);
    }

    public boolean hasEffectiveGlobalRole(GlobalRole roleToCheck) {
        return globalRole.getWeight() >= roleToCheck.getWeight();
    }

    public void setGlobalRole(GlobalRole globalRole) {
        this.globalRole = globalRole;
    }

    /**
     * Reset this user's password, handles encryption if configured.
     *
     * @param newPassword The new password to be set.
     */
    public void resetPassword(String newPassword) throws WebloggerException {
        
        String encrypt = WebloggerStaticConfig.getProperty("passwds.encryption.enabled");
        String algorithm = WebloggerStaticConfig.getProperty("passwds.encryption.algorithm");
        if (Boolean.valueOf(encrypt)) {
            setPassword(Utilities.encodePassword(newPassword, algorithm));
        } else {
            setPassword(newPassword);
        }
    }

    @Basic(optional=false)
    public String getScreenName() {
        return this.screenName;
    }
    
    public void setScreenName( String screenName ) {
        this.screenName = screenName;
    }

    @Basic(optional=false)
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    public void setEmailAddress( String emailAddress ) {
        this.emailAddress = emailAddress;
    }

    @Basic(optional=false)
    @Temporal(TemporalType.TIMESTAMP)
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

    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    
    /**
     * Is this user account enabled?  Disabled accounts cannot login.
     */
    @Column(name="isenabled", nullable=false)
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
        this.activationCode = activationCode;
    }

    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        String stringVal = "{" + getId();
        stringVal += ", " + getUserName();
        stringVal += ", " + getScreenName();
        stringVal += ", " + getGlobalRole();
        stringVal += ", " + getEmailAddress();
        stringVal += ", " + getDateCreated();
        stringVal += ", " + getEnabled();
        stringVal += "}";
        return stringVal;
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        User o = (User)other;
        return new EqualsBuilder().append(getUserName(), o.getUserName())
                .append(getId(), o.getId()).isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).toHashCode();
    }

    @Transient
    public String getPasswordText() {
        return passwordText;
    }

    public void setPasswordText(String passwordText) {
        this.passwordText = passwordText;
    }

    @Transient
    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
