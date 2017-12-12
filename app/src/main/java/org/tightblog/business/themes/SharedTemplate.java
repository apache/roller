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
package org.tightblog.business.themes;

import org.tightblog.pojos.Template;

import javax.xml.bind.annotation.XmlAttribute;
import java.time.Instant;

/**
 * A SharedTemplate represents a template which is part of a SharedTheme.
 */
public class SharedTemplate implements Template {

    private String id = null;
    private ComponentType role = null;
    private String name = null;
    private String description = null;
    private String relativePath = null;
    private Parser parser = Parser.THYMELEAF;
    private String contentsFile = null;
    private String template = null;


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

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser templateLanguage) {
        this.parser = templateLanguage;
    }

    public SharedTemplate(String id, Parser lang) {
        this.id = id;
        setParser(lang);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public Instant getLastModified() {
        return null;
    }

    public String toString() {
        return id + "," + role + "," + name + "," + relativePath;
    }

    public ComponentType getRole() {
        return role;
    }

    @XmlAttribute
    public void setRole(ComponentType role) {
        this.role = role;
    }

    @Override
    public TemplateDerivation getDerivation() {
        return TemplateDerivation.SHARED;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(String template) {
        this.template = template;
    }
}
