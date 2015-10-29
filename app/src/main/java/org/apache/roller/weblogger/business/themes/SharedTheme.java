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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Theme object encapsulates all elements of a single weblog theme. It is
 * used mostly to contain all the templates for a theme, but does contain other
 * theme related attributes such as name, last modified date, etc.
 */
@XmlRootElement(name="weblogtheme")
public class SharedTheme implements Theme, Serializable {

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
    private Set<SharedThemeTemplate> tempTemplates = new HashSet<>();

    public Set<SharedThemeTemplate> getTempTemplates() {
        return tempTemplates;
    }

    private static Log log = LogFactory.getLog(SharedTheme.class);

    // the filesystem directory where we should read this theme from
    private String themeDir = null;

    // we keep templates in a Map for faster lookups by name
    private Map<String, SharedThemeTemplate> templatesByName = new HashMap<>();

    // we keep templates in a Map for faster lookups by link
    private Map<String, SharedThemeTemplate> templatesByLink = new HashMap<>();

    // we keep templates in a Map for faster lookups by action
    private Map<ComponentType, SharedThemeTemplate> templatesByAction = new HashMap<>();

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
     * Get the collection of all templates associated with this Theme.
     */
    public List<SharedThemeTemplate> getTemplates() {
        return new ArrayList<>(this.templatesByName.values());
    }

    public void setTemplates(Set<SharedThemeTemplate> templates) {
        for (SharedThemeTemplate t : templates) {
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
     * Looup the default template, action = weblog. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getDefaultTemplate() {
        return this.templatesByAction.get(ComponentType.WEBLOG);
    }

    /**
     * Lookup the specified template by name. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) {
        return this.templatesByName.get(name);
    }

    /**
     * Lookup the specified template by link. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByLink(String link) {
        return this.templatesByLink.get(link);
    }

    /**
     * Lookup the specified template by action. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByAction(ComponentType action) {
        return this.templatesByAction.get(action);
    }

    /**
     * Set the value for a given template name.
     */
    void addTemplate(SharedThemeTemplate template) {
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getLink(), template);
        if (!ComponentType.CUSTOM.equals(template.getAction())) {
            this.templatesByAction.put(template.getAction(), template);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("\n");

        for (ThemeTemplate template : templatesByName.values()) {
            sb.append(template);
            sb.append("\n");
        }

        return sb.toString();
    }

    public int compareTo(Theme other) {
        return getName().compareTo(other.getName());
    }
}
