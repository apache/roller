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
 * are under same ASF license.
 */

package org.apache.roller.weblogger.pojos; 

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Permission for one specific weblog
 */
public class WeblogPermission implements Serializable {

    protected String  id = UUIDGenerator.generateUUID();
    protected String  userName;
    protected String  objectType;
    protected String  objectId;
    protected boolean pending = false;
    protected Date dateCreated = new Date();
    protected WeblogRole weblogRole;

    public WeblogPermission() {
    }

    public boolean hasEffectiveWeblogRole(WeblogRole roleToCheck) {
        return weblogRole.getWeight() >= roleToCheck.getWeight();
    }

    private static Log log = LogFactory.getLog(WeblogPermission.class);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeblogRole(WeblogRole weblogRole) {
        this.weblogRole = weblogRole;
    }

    public WeblogRole getWeblogRole() {
        return weblogRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public WeblogPermission(Weblog weblog, User user, WeblogRole weblogRole) {
        setWeblogRole(weblogRole);
        objectType = "Weblog";
        objectId = weblog.getHandle();
        userName = user.getUserName();
    }

    public WeblogPermission(Weblog weblog, WeblogRole weblogRole) {
        setWeblogRole(weblogRole);
        objectType = "Weblog";
        objectId = weblog.getHandle();
    }

    public Weblog getWeblog() throws WebloggerException {
        if (objectId != null) {
            return WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(objectId, null);
        }
        return null;
    }

    public User getUser() throws WebloggerException {
        if (userName != null) {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(userName);
        }
        return null;
    }

    public String toString() {
        String sb = "WeblogPermission: ";
        sb += "Object ID = " + getObjectId();
        sb += "; User Name = " + getUserName();
        sb += "; WeblogRole = " + getWeblogRole().name();
        return sb;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogPermission)) {
            return false;
        }
        WeblogPermission o = (WeblogPermission)other;
        return new EqualsBuilder()
                .append(getUserName(), o.getUserName())
                .append(getObjectId(), o.getObjectId())
                .append(getWeblogRole(), o.getWeblogRole())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUserName())
                .append(getObjectId())
                .append(getWeblogRole())
                .toHashCode();
    }
}
