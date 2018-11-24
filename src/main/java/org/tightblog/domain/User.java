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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.tightblog.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "weblogger_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String id = Utilities.generateUUID();
    private int hashCode;

    @NotBlank(message = "{error.add.user.missingUserName}")
    @Pattern(regexp = "[a-z0-9]*", message = "{error.add.user.badUserName}")
    private String userName;
    private GlobalRole globalRole;

    @NotBlank(message = "{Register.error.screenNameNull}")
    private String screenName;
    private UserStatus status = UserStatus.DISABLED;

    @NotBlank(message = "{Register.error.emailAddressNull}")
    @Email(message = "{error.add.user.badEmail}")
    private String emailAddress;
    private Instant dateCreated;
    private String activationCode;
    private Instant lastLogin;

    public User() {
    }

    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional = false)
    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "global_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public GlobalRole getGlobalRole() {
        return this.globalRole;
    }

    public void setGlobalRole(GlobalRole globalRole) {
        this.globalRole = globalRole;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean hasEffectiveGlobalRole(GlobalRole roleToCheck) {
        return globalRole.getWeight() >= roleToCheck.getWeight();
    }

    @Basic(optional = false)
    public String getScreenName() {
        return this.screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    @Basic(optional = false)
    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Basic(optional = false)
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

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String toString() {
        return "User: id=" + id + ", screenName=" + screenName + ", email=" + emailAddress + ", status=" + status;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof User && Objects.equals(id, ((User) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

}
