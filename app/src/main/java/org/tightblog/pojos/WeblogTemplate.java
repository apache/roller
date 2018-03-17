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
package org.tightblog.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;
import org.tightblog.util.Utilities;

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
@NamedQueries({
        @NamedQuery(name = "WeblogTemplate.getByWeblog",
                query = "SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1"),
        @NamedQuery(name = "WeblogTemplate.getByWeblogOrderByName",
                query = "SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 ORDER BY w.name"),
        @NamedQuery(name = "WeblogTemplate.getByWeblog&RelativePath",
                query = "SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 AND w.relativePath = ?2"),
        @NamedQuery(name = "WeblogTemplate.getByRole",
                query = "SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 AND w.role = ?2"),
        @NamedQuery(name = "WeblogTemplate.getByWeblog&Name",
                query = "SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 AND w.name= ?2")
})
public class WeblogTemplate implements Template {

    // attributes
    private String id = Utilities.generateUUID();
    private ComponentType role = null;
    @NotBlank(message = "{templates.error.nameNull}")
    private String name = null;
    private String description = null;
    private String relativePath = null;
    private Instant lastModified = null;
    private TemplateDerivation derivation = TemplateDerivation.SPECIFICBLOG;
    private String template = "";

    private String contents = null;

    // associations
    @JsonIgnore
    private Weblog weblog = null;

    // temporary non-persisted fields used for form entry
    private String roleName = null;

    public WeblogTemplate() {
    }

    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public ComponentType getRole() {
        return role;
    }

    public void setRole(ComponentType role) {
        this.role = role;
    }

    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "relative_path")
    public String getRelativePath() {
        return this.relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

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

    @Transient
    public String getContents() {
        return this.contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    @Transient
    public TemplateDerivation getDerivation() {
        return derivation;
    }

    public void setDerivation(TemplateDerivation derivation) {
        this.derivation = derivation;
    }

    @Transient
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Basic(optional = false)
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String toString() {
        return "{" + getId() + ", " + getName() + ", " + getRelativePath() + "}";
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof WeblogTemplate && Objects.equals(id, ((WeblogTemplate) other).id));
    }

    public int hashCode() {
        return Objects.hash(id);
    }

}
