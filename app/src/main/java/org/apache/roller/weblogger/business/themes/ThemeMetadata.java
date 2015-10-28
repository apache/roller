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
package org.apache.roller.weblogger.business.themes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a parsed version of a theme xml metadata descriptor.
 */
@XmlRootElement(name="weblogtheme")
public class ThemeMetadata {

    private String id = null;
    private String name = null;
    private String description = null;
    private String author = null;
    private String previewImagePath = null;
    private Boolean dualTheme = false;
    private Set<SharedThemeTemplate> templates = new HashSet<>();

    public ThemeMetadata() {
    }

    public void addTemplate(SharedThemeTemplate template) {
        this.templates.add(template);
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

    public String getPreviewImagePath() {
        return previewImagePath;
    }

    public void setPreviewImagePath(String previewImagePath) {
        this.previewImagePath = previewImagePath;
    }

    public Set<SharedThemeTemplate> getTemplates() {
        return templates;
    }

    @XmlElements(@XmlElement(name="template"))
    public void setTemplates(Set<SharedThemeTemplate> templates) {
        this.templates = templates;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getDualTheme() {
        return dualTheme;
    }

    public void setDualTheme(Boolean dualTheme) {
        this.dualTheme = dualTheme;
    }
}
