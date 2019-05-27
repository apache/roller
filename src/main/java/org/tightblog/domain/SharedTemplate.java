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

import java.time.Instant;

/**
 * A SharedTemplate represents a template which is part of a SharedTheme.
 */
public class SharedTemplate implements Template {

    private String id;
    private Role role;
    private String name;
    private String description;
    private String contentsFile;
    private String template;

    @SuppressWarnings("unused")
    public SharedTemplate() {
        // used by Spring initialization
    }

    // public scope needed by JAXB
    public String getContentsFile() {
        return contentsFile;
    }

    public void setContentsFile(String contentsFile) {
        this.contentsFile = contentsFile;
    }

    public SharedTemplate(String id, Role role) {
        this.id = id;
        this.role = role;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Instant getLastModified() {
        return null;
    }

    public String toString() {
        return "SharedTemplate: id=" + id + ", role=" + role + ", name=" + name;
    }

    @Override
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public Derivation getDerivation() {
        return Derivation.SHARED;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(String template) {
        this.template = template;
    }

    // type used during JSON deserialization to determine the Role
    public void setType(String type) {
        role = Role.valueOf(type.toUpperCase());
    }
}
