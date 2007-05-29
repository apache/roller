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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.business.Roller;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.util.Utilities;


/**
 * User bean.
 * 
 * @author David M Johnson
 * @ejb:bean name="User"
 * @hibernate.cache usage="read-write"
 * @hibernate.class lazy="true" table="rolleruser"
 * @struts.form include-all="true"
 */
public class User
        implements Serializable {
    public static final User SYSTEM_USER = new User(
            "n/a","systemuser","n/a","systemuser","n/a",
            "en_US_WIN", "America/Los_Angeles", new Date(), Boolean.TRUE);
    
    public static final User ANONYMOUS_USER = new User(
            "n/a","anonymoususer","n/a","anonymoususer","n/a",
            "en_US_WIN", "America/Los_Angeles", new Date(), Boolean.TRUE);
    
    static final long serialVersionUID = -6354583200913127874L;
    
    private String  id = UUIDGenerator.generateUUID();
    private String  userName;
    private String  password;
    private String  screenName;
    private String  fullName;
    private String  emailAddress;
    private Date    dateCreated;
    private String  locale;
    private String  timeZone;
    private Boolean enabled = Boolean.TRUE;
    private String  activationCode; 
    
    private Set roles = new HashSet();
    private List permissions = new ArrayList();
    
    
    public User() {
    }
    
    public User( String id, String userName,
            String password, String fullName,
            String emailAddress,
            String locale, String timeZone,
            Date dateCreated,
            Boolean isEnabled) {
        //this.id = id;
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.dateCreated = (Date)dateCreated.clone();
        this.locale = locale;
        this.timeZone = timeZone;
        this.enabled = isEnabled;
    }
    
    public User( User otherData ) {
        setData(otherData);
    }
    
    /**
     * @hibernate.bag lazy="true" inverse="true" cascade="none"
     * @hibernate.collection-key column="user_id"
     * @hibernate.collection-one-to-many
     *    class="org.apache.roller.pojos.WeblogPermission"
     */
    public List getPermissions() {
        return permissions;
    }
    public void setPermissions(List perms) {
        permissions = perms;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="isenabled" non-null="true" unique="false"
     */
    public Boolean getEnabled() {
        return this.enabled;
    }
    
    /** @ejb:persistent-field */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /** Id of the User.
     * Not remote since primary key may be extracted by other means.
     *
     * @struts.validator type="required" msgkey="errors.required"
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *  generator-class="assigned"  
     */
    public String getId() {
        return this.id;
    }
    
    /** @ejb:persistent-field */
    public void setId( String id ) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    
    /** User name of the user.
     * @ejb:persistent-field
     * @hibernate.property column="username" non-null="true" unique="true"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getUserName() {
        return this.userName;
    }
    /** @ejb:persistent-field */
    public void setUserName( String userName ) {
        this.userName = userName;
    }
    
    /**
     * Get password.
     * If password encryption is enabled, will return encrypted password.
     *
     * @ejb:persistent-field
     * @hibernate.property column="passphrase" non-null="true"
     */
    public String getPassword() {
        return this.password;
    }
    /**
     * Set password.
     * If password encryption is turned on, then pass in an encrypted password.
     * @ejb:persistent-field
     */
    public void setPassword( String password ) {
        this.password = password;
    }

    /**
     * Screen name of the user.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="screenname" non-null="true" unique="true"
     */
    public String getScreenName() {
        return this.screenName;
    }
    /** @ejb:persistent-field */
    public void setScreenName( String screenName ) {
        this.screenName = screenName;
    }

    /**
     * Full name of the user.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="fullname" non-null="true" unique="true"
     */
    public String getFullName() {
        return this.fullName;
    }
    /** @ejb:persistent-field */
    public void setFullName( String fullName ) {
        this.fullName = fullName;
    }
    
    /**
     * E-mail address of the user.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailaddress" non-null="true" unique="true"
     */
    public String getEmailAddress() {
        return this.emailAddress;
    }
    /** @ejb:persistent-field */
    public void setEmailAddress( String emailAddress ) {
        this.emailAddress = emailAddress;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="datecreated" non-null="true" unique="false"
     */
    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        } else {
            return (Date)dateCreated.clone();
        }
    }
    /** @ejb:persistent-field */
    public void setDateCreated(final Date date) {
        if (date != null) {
            dateCreated = (Date)date.clone();
        } else {
            dateCreated = null;
        }
    }
    
    /**
     * Locale of the user.
     * @ejb:persistent-field
     * @hibernate.property column="locale" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getLocale() {
        return this.locale;
    }
    
    /** @ejb:persistent-field */
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Timezone of the user.
     * @ejb:persistent-field
     * @hibernate.property column="timeZone" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getTimeZone() {
        return this.timeZone;
    }
    
    /** @ejb:persistent-field */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
        
    private boolean datesEquivalent(Date d1, Date d2) {
        boolean equiv = true;
        equiv = equiv && d1.getHours() == d1.getHours();
        equiv = equiv && d1.getMinutes() == d1.getMinutes();
        equiv = equiv && d1.getSeconds() == d1.getSeconds();
        equiv = equiv && d1.getMonth() == d1.getMonth();
        equiv = equiv && d1.getDay() == d1.getDay();
        equiv = equiv && d1.getYear() == d1.getYear();
        return equiv;
    }
    
    
    /**
     * Set bean properties based on other bean.
     */
    public void setData( User otherData ) {
        User other = (User)otherData;
        this.id =       other.getId();
        this.userName = other.getUserName();
        this.password = other.getPassword();
        this.screenName = other.getScreenName();
        this.fullName = other.getFullName();
        this.emailAddress = other.getEmailAddress();
        this.locale = other.getLocale();
        this.timeZone = other.getTimeZone();
        this.dateCreated = other.getDateCreated()!=null ? (Date)other.getDateCreated().clone() : null;
        this.activationCode = other.getActivationCode();
    }
    
    
    /**
     * Reset this user's password.
     * @param roller Roller instance to use for configuration information
     * @param new1 New password
     * @param new2 Confirm this matches new password
     * @author Dave Johnson
     */
    public void resetPassword(Roller roller, String new1, String new2) throws RollerException {
        if (!new1.equals(new2)) {
            throw new RollerException("newUser.error.mismatchedPasswords");
        }
        
        String encrypt = RollerConfig.getProperty("passwds.encryption.enabled");
        String algorithm = RollerConfig.getProperty("passwds.encryption.algorithm");
        if (new Boolean(encrypt).booleanValue()) {
            setPassword(Utilities.encodePassword(new1, algorithm));
        } else {
            setPassword(new1);
        }
    }
    
    
    /**
     * @hibernate.set lazy="true" inverse="true" cascade="all"
     * @hibernate.collection-key column="userid"
     * @hibernate.collection-one-to-many class="org.apache.roller.pojos.UserRole"
     */
    public Set getRoles() {
        return roles;
    }
    
    /**
     * this is private to force the use of grant/revokeRole() methods.
     */
    private void setRoles(Set roles) {
        this.roles = roles;
    }
    
    
    /**
     * Returns true if user has role specified.
     */
    public boolean hasRole(String roleName) {
        Iterator iter = getRoles().iterator();
        while (iter.hasNext()) {
            UserRole role = (UserRole) iter.next();
            if (role.getRole().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Grant to user role specified by role name.
     */
    public void grantRole(String roleName) throws RollerException {
        if (!hasRole(roleName)) {
            UserRole role = new UserRole(null, this, roleName);
            getRoles().add(role);
            role.setUser(this);
        }
    }
    
    /** activation code 
     * @ejb:persistent-field
     * @hibernate.property column="activationcode" non-null="false"
     * @roller.wrapPojoMethod type="simple"
     */
	public String getActivationCode() { 
		return activationCode;
	}
	
	/** @ejb:persistent-field */
	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}


    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.userName);
        buf.append(", ").append(this.fullName);
        buf.append(", ").append(this.emailAddress);
        buf.append(", ").append(this.dateCreated);
        buf.append(", ").append(this.enabled);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof User != true) return false;
        User o = (User)other;
        return new EqualsBuilder().append(getUserName(), o.getUserName()).isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder().append(getUserName()).toHashCode();
    }
    
}
