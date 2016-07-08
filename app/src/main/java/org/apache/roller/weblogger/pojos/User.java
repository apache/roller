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

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;


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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    
    private String  id;

    @NotBlank(message = "{error.add.user.missingUserName}")
    @Pattern(regexp = "[a-z0-9]*", message = "{error.add.user.badUserName}")
    private String  userName;
    private GlobalRole globalRole;

    @NotBlank(message = "{Register.error.screenNameNull}")
    private String  screenName;

    @NotBlank(message = "{Register.error.emailAddressNull}")
    @Email(message = "{error.add.user.badEmail}")
    private String  emailAddress;
    private Instant dateCreated;
    private String  locale;
    private Boolean enabled = Boolean.FALSE;
    private Boolean approved = Boolean.FALSE;
    private String  activationCode;
    private Instant lastLogin;

    public User() {
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
    
    @Column(name="global_role", nullable=false)
    @Enumerated(EnumType.STRING)
    public GlobalRole getGlobalRole() {
        return this.globalRole;
    }

    public void setGlobalRole(GlobalRole globalRole) {
        this.globalRole = globalRole;
    }

    public boolean hasEffectiveGlobalRole(GlobalRole roleToCheck) {
        return globalRole.getWeight() >= roleToCheck.getWeight();
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
    public Instant getDateCreated() {
         return dateCreated;
    }

    public void setDateCreated(Instant dateTime) {
        this.dateCreated = dateTime;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Is this user account enabled?  Disabled accounts cannot login.
     */
    @Basic(optional=false)
    public Boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * New user with validated email account whose registration has been
     * approved yet by a system administrator?  Unapproved accounts cannot
     * log in.
     */
    @Basic(optional=false)
    public Boolean isApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
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
        stringVal += ", " + isEnabled();
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
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

}
