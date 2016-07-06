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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.time.Instant;

/**
 * WeblogRole that a user has for a specific weblog
 */
@Entity(name="UserWeblogRole")
@Table(name="user_weblog_role")
@NamedQueries({
        @NamedQuery(name="UserWeblogRole.getByUserId",
                query="SELECT p FROM UserWeblogRole p WHERE p.user.id = ?1 AND p.pending <> TRUE"),
        @NamedQuery(name="UserWeblogRole.getByUserIdIncludingPending",
                query="SELECT p FROM UserWeblogRole p WHERE p.user.id = ?1"),
        @NamedQuery(name="UserWeblogRole.getByWeblogId",
                query="SELECT p FROM UserWeblogRole p WHERE p.weblog.id = ?1 AND p.pending <> TRUE"),
        @NamedQuery(name="UserWeblogRole.getByWeblogId&Pending",
                query="SELECT p FROM UserWeblogRole p WHERE p.weblog.id = ?1 AND p.pending = TRUE"),
        @NamedQuery(name="UserWeblogRole.getByWeblogIdIncludingPending",
                query="SELECT p FROM UserWeblogRole p WHERE p.weblog.id = ?1"),
        @NamedQuery(name="UserWeblogRole.getByUserId&WeblogId",
                query="SELECT p FROM UserWeblogRole p WHERE p.user.id = ?1 AND p.weblog.id = ?2 AND p.pending <> true"),
        @NamedQuery(name="UserWeblogRole.getByUserId&WeblogIdIncludingPending",
                query="SELECT p FROM UserWeblogRole p WHERE p.user.id = ?1 AND p.weblog.id = ?2")
})
public class UserWeblogRole {

    private String id = Utilities.generateUUID();
    private User user;
    private Weblog weblog;
    private boolean pending = false;
    private Instant dateCreated = Instant.now();
    private WeblogRole weblogRole;

    public UserWeblogRole() {
    }

    public UserWeblogRole(User user, Weblog weblog, WeblogRole weblogRole) {
        setWeblogRole(weblogRole);
        this.user = user;
        this.weblog = weblog;
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

    @ManyToOne
    @JoinColumn(name="userid",nullable=false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Column(name="weblog_role", nullable=false)
    @Enumerated(EnumType.STRING)
    public WeblogRole getWeblogRole() {
        return weblogRole;
    }

    public void setWeblogRole(WeblogRole weblogRole) {
        this.weblogRole = weblogRole;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Basic(optional=false)
    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }


    public String toString() {
        String sb = "UserWeblogRole: ";
        sb += "Weblog Handle = " + getWeblog().getHandle();
        sb += "; User Name = " + getUser().getUserName();
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
                .append(getUser().getId(), o.getUser().getId())
                .append(getWeblog().getId(), o.getWeblog().getId())
                .append(getWeblogRole(), o.getWeblogRole())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUser().getId())
                .append(getWeblog().getId())
                .append(getWeblogRole())
                .toHashCode();
    }
}
