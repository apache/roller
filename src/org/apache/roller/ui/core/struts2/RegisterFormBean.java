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
    
    private String id = null;
    private String userName = null;
    private String password = null;
    private String fullName = null;
    private String emailAddress = null;
    private String locale = null;
    private String timeZone = null;
    private String mPasswordText = null;
    private String mPasswordConfirm = null;

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public String getPasswordText() {
        return mPasswordText;
    }
    
    public void setPasswordText(String passwordText) {
        mPasswordText = passwordText;
    }
    
    public String getPasswordConfirm() {
        return mPasswordConfirm;
    }
    
    public void setPasswordConfirm(String passwordConfirm) {
        mPasswordConfirm = passwordConfirm;
    }
    
    
    public void copyTo(UserData dataHolder, Locale locale) {
        
        dataHolder.setFullName(this.fullName);
        dataHolder.setEmailAddress(this.emailAddress);
        dataHolder.setLocale(this.locale);
        dataHolder.setTimeZone(this.timeZone);
    }
    
    
    public void copyFrom(UserData dataHolder, Locale locale) {
        
        this.id = dataHolder.getId();
        this.userName = dataHolder.getUserName();
        this.password = dataHolder.getPassword();
        this.fullName = dataHolder.getFullName();
        this.emailAddress = dataHolder.getEmailAddress();
        this.locale = dataHolder.getLocale();
        this.timeZone = dataHolder.getTimeZone();
    }
    
}
