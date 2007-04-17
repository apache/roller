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

package org.apache.roller.ui.core.struts2;

import java.util.Date;
import java.util.Locale;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.UserData;


/**
 * A simple bean for managing the form data used by the RegisterForm.
 */
public class RegisterFormBean {
    
    protected java.lang.Boolean enabled;
    protected java.lang.String id;
    protected java.lang.String userName;
    protected java.lang.String password;
    protected java.lang.String fullName;
    protected java.lang.String emailAddress;
    protected java.lang.String locale;
    protected java.lang.String timeZone;
    protected java.lang.String activationCode;
    private String mPasswordText = null;
    private String mPasswordConfirm = null;
    
    
    public java.lang.Boolean getEnabled() {
        return this.enabled;
    }
    
    /**
     */
    public void setEnabled( java.lang.Boolean enabled ) {
        this.enabled = enabled;
    }
    
    public java.lang.String getId() {
        return this.id;
    }
    
    /**
     * @struts.validator type="required" msgkey="errors.required"
     */
    public void setId( java.lang.String id ) {
        this.id = id;
    }
    
    public java.lang.String getUserName() {
        return this.userName;
    }
    
    /**
     */
    public void setUserName( java.lang.String userName ) {
        this.userName = userName;
    }
    
    public java.lang.String getPassword() {
        return this.password;
    }
    
    /**
     */
    public void setPassword( java.lang.String password ) {
        this.password = password;
    }
    
    public java.lang.String getFullName() {
        return this.fullName;
    }
    
    /**
     */
    public void setFullName( java.lang.String fullName ) {
        this.fullName = fullName;
    }
    
    public java.lang.String getEmailAddress() {
        return this.emailAddress;
    }
    
    /**
     */
    public void setEmailAddress( java.lang.String emailAddress ) {
        this.emailAddress = emailAddress;
    }
    
    public java.lang.String getLocale() {
        return this.locale;
    }
    
    /**
     */
    public void setLocale( java.lang.String locale ) {
        this.locale = locale;
    }
    
    public java.lang.String getTimeZone() {
        return this.timeZone;
    }
    
    /**
     */
    public void setTimeZone( java.lang.String timeZone ) {
        this.timeZone = timeZone;
    }
    
    public java.lang.String getActivationCode() {
        return this.activationCode;
    }
    
    /**
     */
    public void setActivationCode( java.lang.String activationCode ) {
        this.activationCode = activationCode;
    }
    
    /**
     * Don't call it "password" because browser will autofill.
     * @return Returns the passwordText.
     */
    public String getPasswordText() {
        return mPasswordText;
    }
    
    /**
     * Don't call it "password" because browser will autofill.
     * @param passwordText The passwordText to set.
     */
    public void setPasswordText(String passwordText) {
        mPasswordText = passwordText;
    }
    
    /**
     * @return Returns the passwordConfirm.
     */
    public String getPasswordConfirm() {
        return mPasswordConfirm;
    }
    
    /**
     * @param passwordConfirm The passwordConfirm to set.
     */
    public void setPasswordConfirm(String passwordConfirm) {
        mPasswordConfirm = passwordConfirm;
    }
    
    
    /**
     * Copy values from this form bean to the specified data object.
     * Only copies primitive types (Boolean, boolean, String, Integer, int, Timestamp, Date)
     */
    public void copyTo(UserData dataHolder, Locale locale) {
        
        dataHolder.setEnabled(this.enabled);
        dataHolder.setId(this.id);
        dataHolder.setUserName(this.userName);
        dataHolder.setFullName(this.fullName);
        dataHolder.setEmailAddress(this.emailAddress);
        dataHolder.setLocale(this.locale);
        dataHolder.setTimeZone(this.timeZone);
        
        dataHolder.setActivationCode(this.activationCode);
    }
    
    
    /**
     * Copy values from specified data object to this form bean.
     * Includes all types.
     */
    public void copyFrom(UserData dataHolder, Locale locale) {
        
        this.enabled = dataHolder.getEnabled();
        this.id = dataHolder.getId();
        this.userName = dataHolder.getUserName();
        this.password = dataHolder.getPassword();
        this.fullName = dataHolder.getFullName();
        this.emailAddress = dataHolder.getEmailAddress();
        this.locale = dataHolder.getLocale();
        this.timeZone = dataHolder.getTimeZone();
        
        this.activationCode = dataHolder.getActivationCode();
        
    }
    
    
    public void doReset() {
        
        this.enabled = null;
        this.id = null;
        this.userName = null;
        this.password = null;
        this.fullName = null;
        this.emailAddress = null;
        this.locale = null;
        this.timeZone = null;
        
        this.activationCode = null;
        
    }
    
}
