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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.WebloggerFactory;
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


/**
 * User bean.
 */
@Entity
@Table(name="roller_user")
@NamedQueries({
        @NamedQuery(name="User.getAll",
                query="SELECT u FROM User u"),
        @NamedQuery(name="User.getByEnabled",
                query="SELECT u FROM User u WHERE u.enabled = ?1"),
        @NamedQuery(name="User.getUserByActivationCode",
                query="SELECT u FROM User u WHERE u.activationCode = ?1"),
        @NamedQuery(name="User.getByEnabled&EndDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.enabled = ?1 AND u.dateCreated < ?2 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getByEnabled&EndDate&StartDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.enabled = ?1 AND u.dateCreated < ?2 AND u.dateCreated > ?3 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getByEnabled&UserNameOrEmailAddressStartsWith",
                query="SELECT u FROM User u WHERE u.enabled = ?1 AND (u.userName LIKE ?2 OR u.emailAddress LIKE ?3)"),
        @NamedQuery(name="User.getByEndDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.dateCreated < ?1 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getByUserName",
                query="SELECT u FROM User u WHERE u.userName= ?1"),
        @NamedQuery(name="User.getByUserName&Enabled",
                query="SELECT u FROM User u WHERE u.userName= ?1 AND u.enabled = ?2"),
        @NamedQuery(name="User.getByOpenIdUrl",
                query="SELECT u FROM User u WHERE u.openIdUrl = ?1"),
        @NamedQuery(name="User.getByUserNameOrEmailAddressStartsWith",
                query="SELECT u FROM User u WHERE u.userName LIKE ?1 OR u.emailAddress LIKE ?1"),
        @NamedQuery(name="User.getByUserNameOrderByUserName",
                query="SELECT u FROM User u WHERE u.userName= ?1 ORDER BY u.userName"),
        @NamedQuery(name="User.getByEndDate&StartDateOrderByStartDateDesc",
                query="SELECT u FROM User u WHERE u.dateCreated < ?1 AND u.dateCreated > ?2 ORDER BY u.dateCreated DESC"),
        @NamedQuery(name="User.getGlobalRole",
                query="SELECT u.globalRole FROM User u WHERE u.userName = ?1"),
        @NamedQuery(name="User.getCountByUserNameLike",
                query="SELECT COUNT(u) FROM User u WHERE UPPER(u.userName) LIKE ?1"),
        @NamedQuery(name="User.getCountEnabledDistinct",
                query="SELECT COUNT(u) FROM User u WHERE u.enabled = ?1")
})
public class User implements Serializable {
    
    public static final long serialVersionUID = -6354583200913127874L;

    private String  id = UUIDGenerator.generateUUID();
    private String  userName;
    private String  password;
    private GlobalRole globalRole;
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
    
    public User(String userName,
            String password,
            GlobalRole globalRole,
            String fullName,
            String emailAddress,
            String locale, String timeZone,
            Date dateCreated,
            Boolean isEnabled) {
        this.userName = userName;
        this.password = password;
        this.globalRole = globalRole;
        this.fullName = fullName;
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

    public boolean isGlobalAdmin() {
        return this.globalRole == GlobalRole.ADMIN;
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
        
        String encrypt = WebloggerConfig.getProperty("passwds.encryption.enabled");
        String algorithm = WebloggerConfig.getProperty("passwds.encryption.algorithm");
        if (Boolean.valueOf(encrypt)) {
            setPassword(Utilities.encodePassword(newPassword, algorithm));
        } else {
            setPassword(newPassword);
        }
    }

    @Column(name="openid_url", nullable=false)
    public String getOpenIdUrl() {
        return openIdUrl;
    }

    public void setOpenIdUrl(String openIdUrl) {
        this.openIdUrl = openIdUrl;
    }

    @Basic(optional=false)
    public String getScreenName() {
        return this.screenName;
    }
    
    public void setScreenName( String screenName ) {
        this.screenName = screenName;
    }

    @Basic(optional=false)
    public String getFullName() {
        return this.fullName;
    }
    
    public void setFullName( String fullName ) {
        this.fullName = fullName;
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
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getUserName());
        buf.append(", ").append(getFullName());
        buf.append(", ").append(getGlobalRole());
        buf.append(", ").append(getEmailAddress());
        buf.append(", ").append(getDateCreated());
        buf.append(", ").append(getEnabled());
        buf.append("}");
        return buf.toString();
    }
    
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
    
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).toHashCode();
    }

}
