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

import java.io.Serializable;
import java.util.*;

import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Template.ComponentType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Theme object encapsulates all elements of a single weblog theme. It is
 * used mostly to contain all the templates for a theme, but does contain other
 * theme related attributes such as name, last modified date, etc.
 */
@XmlRootElement(name="sharedtheme")
public class SharedTheme implements Serializable {

    private String id = null;
    private String name = null;
    private String description = null;
    private String author = null;
    // the preview image path is relative from the shared theme's base folder
    private String previewImagePath = null;
    private Boolean dualTheme = false;
    private Date lastModified = null;
    private boolean enabled = true;

    // JAXB loads here; ThemeManagerImpl moves them to the three maps.
    @XmlElements(@XmlElement(name="template"))
    private Set<SharedTemplate> tempTemplates = new HashSet<>();

    public Set<SharedTemplate> getTempTemplates() {
        return tempTemplates;
    }

    // the filesystem directory where we should read this theme from
    private String themeDir = null;

    // we keep templates in a Map for faster lookups by name
    private Map<String, Template> templatesByName = new HashMap<>();

    // we keep templates in a Map for faster lookups by link
    private Map<String, Template> templatesByLink = new HashMap<>();

    // we keep templates in a Map for faster lookups by action
    private Map<ComponentType, Template> templatesByAction = new HashMap<>();

    public SharedTheme() {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPreviewImagePath() {
        return previewImagePath;
    }

    public void setPreviewImagePath(String previewImagePath) {
        this.previewImagePath = previewImagePath;
    }

    public Boolean getDualTheme() {
        return dualTheme;
    }

    public void setDualTheme(Boolean dualTheme) {
        this.dualTheme = dualTheme;
    }

    /**
     * Get the name-keyed map of all templates associated with this Theme.
     */
    public Map<String, Template> getTemplatesByName() {
        return templatesByName;
    }

    public void setTemplates(Set<SharedTemplate> templates) {
        for (SharedTemplate t : templates) {
            addTemplate(t);
        }
    }

    public String getThemeDir() {
        return themeDir;
    }

    public void setThemeDir(String themeDir) {
        this.themeDir = themeDir;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Lookup the specified template by name. Returns null if the template
     * cannot be found.
     */
    public Template getTemplateByName(String name) {
        return this.templatesByName.get(name);
    }

    /**
     * Lookup the specified template by link. Returns null if the template
     * cannot be found.
     */
    public Template getTemplateByPath(String link) {
        return this.templatesByLink.get(link);
    }

    /**
     * Lookup the specified template by action. Returns null if the template
     * cannot be found.
     */
    public Template getTemplateByAction(ComponentType action) {
        return this.templatesByAction.get(action);
    }

    /**
     * Set the value for a given template name.
     */
    void addTemplate(SharedTemplate template) {
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getRelativePath(), template);
        if (template.getRole().isSingleton()) {
            this.templatesByAction.put(template.getRole(), template);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("\n");

        for (Template template : templatesByName.values()) {
            sb.append(template);
            sb.append("\n");
        }

        return sb.toString();
    }

}
