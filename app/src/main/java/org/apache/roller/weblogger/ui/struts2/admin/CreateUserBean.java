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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;


/**
 * Bean used by CreateUser action.
 */
public class CreateUserBean {
    
    private String id = null;
    private String userName = null;
    private String password = null;
    private String screenName = null;
    private String fullName = null;
    private String emailAddress = null;
    private String locale = null;
    private String timeZone = null;
    private String openIdUrl = null;
    private Boolean enabled = Boolean.TRUE;
    private String activationCode = null;
    private boolean administrator = false;
    private List<String> list = new ArrayList<>();

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

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
    
    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
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

    public String getOpenIdUrl() {
        return openIdUrl;
    }

    public void setOpenIdUrl(String openIdUrl) {
        this.openIdUrl = openIdUrl;
    }

    public Boolean getEnabled() {
        return enabled;
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
    
    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }
    
    public void copyTo(User dataHolder) {
        
        dataHolder.setScreenName(this.screenName);
        dataHolder.setFullName(this.fullName);
        dataHolder.setEmailAddress(this.emailAddress);
        dataHolder.setLocale(this.locale);
        dataHolder.setTimeZone(this.timeZone);
        dataHolder.setOpenIdUrl(this.openIdUrl);
        dataHolder.setEnabled(this.enabled);
        dataHolder.setActivationCode(this.activationCode);
    }
    
    
    public void copyFrom(User dataHolder) {

        this.id = dataHolder.getId();
        this.userName = dataHolder.getUserName();
        this.password = dataHolder.getPassword();
        this.screenName = dataHolder.getScreenName();
        this.fullName = dataHolder.getFullName();
        this.emailAddress = dataHolder.getEmailAddress();
        this.locale = dataHolder.getLocale();
        this.timeZone = dataHolder.getTimeZone();
        this.openIdUrl = dataHolder.getOpenIdUrl();
        this.enabled = dataHolder.getEnabled();
        this.activationCode = dataHolder.getActivationCode();

        try {
            GlobalPermission adminPerm = 
                new GlobalPermission(Collections.singletonList(GlobalPermission.ADMIN));
            this.administrator = WebloggerFactory.getWeblogger().getUserManager()
                    .checkPermission(adminPerm, dataHolder);

        } catch (WebloggerException ex) {}
    }
    
}
