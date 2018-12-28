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
import org.tightblog.domain.Template.TemplateDerivation;
import org.tightblog.repository.WeblogTemplateRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Theme which is specifically tied to a given weblog.
 * <p>
 * A WeblogTheme is what is used throughout the rendering process to do the
 * rendering for a given weblog design.
 */
public class WeblogTheme {

    protected WeblogTemplateRepository weblogTemplateRepository;
    protected Weblog weblog;
    private SharedTheme sharedTheme;

    public WeblogTheme(WeblogTemplateRepository weblogTemplateRepository,
                       Weblog weblog, SharedTheme sharedTheme) {
        this.weblogTemplateRepository = weblogTemplateRepository;
        this.weblog = weblog;
        this.sharedTheme = sharedTheme;
    }

    public String getId() {
        return this.sharedTheme.getId();
    }

    public String getName() {
        return this.sharedTheme.getName();
    }

    public String getDescription() {
        return this.sharedTheme.getDescription();
    }

    public Instant getLastModified() {
        return this.sharedTheme.getLastModified();
    }

    /**
     * Get the collection of all templates associated with this Theme.  Presently, for
     * performance reasons, this is the only method that will check the sharedTemplates
     * for the purpose of switching the SHARED derivation to OVERRIDDEN if appropriate.
     */
    public List<? extends Template> getTemplates() {
        Map<String, Template> pageMap = new TreeMap<>();

        // first get shared theme pages (non-db)
        pageMap.putAll(this.sharedTheme.getTemplatesByName());

        // now, unless in preview mode, overwrite individual templates with blog-specific ones stored in the DB
        if (!weblog.isUsedForThemePreview()) {
            for (WeblogTemplate template : weblogTemplateRepository.findByWeblog(this.weblog)) {
                if (pageMap.get(template.getName()) != null) {
                    // mark weblog template as an override
                    template.setDerivation(TemplateDerivation.OVERRIDDEN);
                }
                // add new or replace shared template
                pageMap.put(template.getName(), template);
            }
        }

        return new ArrayList<>(pageMap.values());
    }

    /**
     * Lookup the specified template by action.
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByAction(ComponentType action) {
        Template template = null;

        if (!weblog.isUsedForThemePreview()) {
            template = weblogTemplateRepository.findByWeblogAndRole(this.weblog, action);
        }
        if (template == null) {
            template = sharedTheme.getTemplateByAction(action);
        }
        return template;
    }

    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByName(String name) {
        Template template = null;

        if (!weblog.isUsedForThemePreview()) {
            template = weblogTemplateRepository.findByWeblogAndName(this.weblog, name);
        }
        if (template == null) {
            template = sharedTheme.getTemplateByName(name);
        }
        return template;
    }

    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByPath(String path) {
        Template template = null;

        if (!weblog.isUsedForThemePreview()) {
            template = weblogTemplateRepository.findByWeblogAndRelativePath(this.weblog, path);
        }
        if (template == null) {
            template = sharedTheme.getTemplateByPath(path);
        }
        return template;
    }

}
