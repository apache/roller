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
import org.apache.roller.weblogger.WebloggerUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;

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
 * WeblogRole that a user has for a specific weblog
 */
@Entity(name="UserWeblogRole")
@Table(name="user_weblog_role")
@NamedQueries({
        @NamedQuery(name="UserWeblogRole.getByUserName",
                query="SELECT p FROM UserWeblogRole p WHERE p.userName = ?1 AND p.pending <> TRUE"),
        @NamedQuery(name="UserWeblogRole.getByUserName&Pending",
                query="SELECT p FROM UserWeblogRole p WHERE p.userName = ?1 AND p.pending = TRUE"),
        @NamedQuery(name="UserWeblogRole.getByWeblogId",
                query="SELECT p FROM UserWeblogRole p WHERE p.weblogId = ?1 AND p.pending <> TRUE"),
        @NamedQuery(name="UserWeblogRole.getByWeblogId&Pending",
                query="SELECT p FROM UserWeblogRole p WHERE p.weblogId = ?1 AND p.pending = TRUE"),
        @NamedQuery(name="UserWeblogRole.getByWeblogIdIncludingPending",
                query="SELECT p FROM UserWeblogRole p WHERE p.weblogId = ?1"),
        @NamedQuery(name="UserWeblogRole.getByUserName&WeblogId",
                query="SELECT p FROM UserWeblogRole p WHERE p.userName = ?1 AND p.weblogId = ?2 AND p.pending <> true"),
        @NamedQuery(name="UserWeblogRole.getByUserName&WeblogIdIncludingPending",
                query="SELECT p FROM UserWeblogRole p WHERE p.userName = ?1 AND p.weblogId = ?2")
})
public class UserWeblogRole implements Serializable {

    protected String  id = WebloggerUtils.generateUUID();
    protected String  userName;
    protected String  weblogId;
    protected boolean pending = false;
    protected Date dateCreated = new Date();
    protected WeblogRole weblogRole;

    public UserWeblogRole() {
    }

    public UserWeblogRole(Weblog weblog, User user, WeblogRole weblogRole) {
        setWeblogRole(weblogRole);
        weblogId = weblog.getId();
        userName = user.getUserName();
    }

    public boolean hasEffectiveWeblogRole(WeblogRole roleToCheck) {
        return weblogRole.getWeight() >= roleToCheck.getWeight();
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name="weblog_role", nullable=false)
    @Enumerated(EnumType.STRING)
    public WeblogRole getWeblogRole() {
        return weblogRole;
    }

    public void setWeblogRole(WeblogRole weblogRole) {
        this.weblogRole = weblogRole;
    }

    @Basic(optional=false)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    @Basic(optional=false)
    public String getWeblogId() {
        return weblogId;
    }

    public void setWeblogId(String weblogId) {
        this.weblogId = weblogId;
    }

    @Temporal(TemporalType.DATE)
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Basic(optional=false)
    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @Transient
    public Weblog getWeblog() throws WebloggerException {
        if (weblogId != null) {
            return WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(weblogId);
        }
        return null;
    }

    @Transient
    public User getUser() throws WebloggerException {
        if (userName != null) {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(userName);
        }
        return null;
    }

    public String toString() {
        String sb = "UserWeblogRole: ";
        sb += "Weblog ID = " + getWeblogId();
        sb += "; User Name = " + getUserName();
        sb += "; WeblogRole = " + getWeblogRole().name();
        return sb;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UserWeblogRole)) {
            return false;
        }
        UserWeblogRole o = (UserWeblogRole)other;
        return new EqualsBuilder()
                .append(getUserName(), o.getUserName())
                .append(getWeblogId(), o.getWeblogId())
                .append(getWeblogRole(), o.getWeblogRole())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUserName())
                .append(getWeblogId())
                .append(getWeblogRole())
                .toHashCode();
    }
}
