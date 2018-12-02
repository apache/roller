/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import org.tightblog.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

/**
 * WeblogRole that a user has for a specific weblog
 */
@Entity(name = "UserWeblogRole")
@Table(name = "user_weblog_role")
public class UserWeblogRole {

    private String id = Utilities.generateUUID();
    private int hashCode;
    private User user;
    private Weblog weblog;
    private boolean emailComments;
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
    @JoinColumn(name = "userid", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Column(name = "weblog_role", nullable = false)
    @Enumerated(EnumType.STRING)
    public WeblogRole getWeblogRole() {
        return weblogRole;
    }

    public void setWeblogRole(WeblogRole weblogRole) {
        this.weblogRole = weblogRole;
    }

    @Basic(optional = false)
    @Column(name = "email_comments")
    public boolean isEmailComments() {
        return emailComments;
    }

    public void setEmailComments(boolean emailComments) {
        this.emailComments = emailComments;
    }

    public String toString() {
        return "UserWeblogRole: user=" + (user != null ? user.getUserName() : "(empty)")
                + ", weblog=" + weblog.getHandle() + ", role=" + weblogRole;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof UserWeblogRole && Objects.equals(id, ((UserWeblogRole) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }
}
