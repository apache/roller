/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "weblogger_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCredentials {

    private String id;
    @NotBlank(message = "{error.add.user.missingUserName}")
    @Pattern(regexp = "[a-z0-9]*", message = "{error.add.user.badUserName}")
    private String userName;
    private String password;
    private GlobalRole globalRole;

    // for Authenticator app use
    // see http://www.baeldung.com/spring-security-two-factor-authentication-with-soft-token
    private String mfaSecret;

    // below two fields not persisted but used for password entry and confirmation
    // on new user & user update forms.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordText;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordConfirm;

    // below two transient fields used to check status of MFA secret and whether it needs erasing
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean hasMfaSecret;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean eraseMfaSecret;

    @Column(name = "mfa_secret")
    @JsonIgnore
    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
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

    @Column(name = "encr_password", nullable = false)
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "global_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public GlobalRole getGlobalRole() {
        return this.globalRole;
    }

    public void setGlobalRole(GlobalRole globalRole) {
        this.globalRole = globalRole;
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

    @Transient
    public boolean isHasMfaSecret() {
        return mfaSecret != null;
    }

    @Transient
    public boolean isEraseMfaSecret() {
        return eraseMfaSecret;
    }

    public void setEraseMfaSecret(boolean eraseMfaSecret) {
        this.eraseMfaSecret = eraseMfaSecret;
    }
}
