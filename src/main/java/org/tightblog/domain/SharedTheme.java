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

import org.tightblog.domain.Template.ComponentType;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Theme object encapsulates all elements of a single weblog theme. It is
 * used mostly to contain all the templates for a theme, but does contain other
 * theme related attributes such as name, last modified date, etc.
 */
public class SharedTheme {

    private String id;
    private String name;
    private String description;
    // the preview image path is relative from the shared theme's base folder
    private String previewImagePath;

    // Site-wide blogs provide aggregated data from all weblogs to this weblog
    private Boolean siteWide = false;
    private Instant lastModified;

    private Set<SharedTemplate> templates = new HashSet<>();

    // the filesystem directory where we should read this theme from
    private String themeDir;

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

    public String getPreviewImagePath() {
        return previewImagePath;
    }

    public String getPreviewPath() {
        return "/blogthemes/" + getId() + "/" + getPreviewImagePath();
    }

    public void setPreviewImagePath(String previewImagePath) {
        this.previewImagePath = previewImagePath;
    }

    public Boolean isSiteWide() {
        return siteWide;
    }

    public void setSiteWide(Boolean siteWide) {
        this.siteWide = siteWide;
    }

    public Set<SharedTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<SharedTemplate> templates) {
        for (SharedTemplate t : templates) {
            addTemplate(t);
        }
    }

    /**
     * Get the name-keyed map of all templates associated with this Theme.
     */
    public Map<String, Template> getTemplatesByName() {
        return templatesByName;
    }

    public String getThemeDir() {
        return themeDir;
    }

    public void setThemeDir(String themeDir) {
        this.themeDir = themeDir;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Lookup the specified template by name. Returns null if the template
     * cannot be found.
     */
    public Template getTemplateByName(String templateName) {
        return this.templatesByName.get(templateName);
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
    public void addTemplate(SharedTemplate template) {
        this.templates.add(template);
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getRelativePath(), template);
        if (template.getRole().isSingleton()) {
            this.templatesByAction.put(template.getRole(), template);
        }
    }

    public String toString() {
        return String.format("SharedTheme: id=%s, name=%s, isSiteWide=%s, # templates=%d", id, name, isSiteWide(),
                templatesByName.size());
    }

}
