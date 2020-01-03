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

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
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
import javax.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

/**
 * POJO that represents a single user defined template page.
 * <p>
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 */
@Entity
@Table(name = "weblog_template")
public class WeblogTemplate implements Template, WeblogOwned {

    // attributes
    private String id = Utilities.generateUUID();
    private int hashCode;
    private Role role;
    @NotBlank(message = "{templates.error.nameNull}")
    private String name;
    private String description;
    private Instant lastModified;
    private Derivation derivation = Derivation.SPECIFICBLOG;
    private String template = "";

    // associations
    @JsonIgnore
    private Weblog weblog;

    // temporary non-persisted fields used for form entry
    private String roleName;

    public WeblogTemplate() {
    }

    // used in WeblogTemplateDao where template metadata rather than template itself is needed
    public WeblogTemplate(String id, Role role, @NotBlank(message = "{templates.error.nameNull}") String name,
                          String description, Instant lastModified) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.description = description;
        this.lastModified = lastModified;
    }

    @Override
    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    @Column(name = "updatetime", nullable = false)
    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant newtime) {
        lastModified = newtime;
    }

    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = false)
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Override
    @Transient
    public Derivation getDerivation() {
        return derivation;
    }

    public void setDerivation(Derivation derivation) {
        this.derivation = derivation;
    }

    @Transient
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    @Basic(optional = false)
    public String getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(String template) {
        this.template = template;
    }

    public String toString() {
        return "WeblogTemplate: id=" + id + ", name=" + name + ", role=" + role + ", derivation=" + derivation;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof WeblogTemplate && Objects.equals(id, ((WeblogTemplate) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }
}
