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

import org.tightblog.domain.Template.Role;
import org.tightblog.domain.Template.Derivation;
import org.tightblog.dao.WeblogTemplateDao;

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

    private WeblogTemplateDao weblogTemplateDao;
    protected Weblog weblog;
    private SharedTheme sharedTheme;

    public WeblogTheme(WeblogTemplateDao weblogTemplateDao,
                       Weblog weblog, SharedTheme sharedTheme) {
        this.weblogTemplateDao = weblogTemplateDao;
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

        // first get shared theme pages (non-db)
        Map<String, Template> pageMap = new TreeMap<>(this.sharedTheme.getTemplatesByName());

        // overwrite individual templates with blog-specific ones stored in the DB
        for (WeblogTemplate template : weblogTemplateDao.getWeblogTemplateMetadata(this.weblog)) {
            if (pageMap.get(template.getName()) != null) {
                // mark weblog template as an override
                template.setDerivation(Derivation.OVERRIDDEN);
            }
            // add new or replace shared template
            pageMap.put(template.getName(), template);
        }

        return new ArrayList<>(pageMap.values());
    }

    /**
     * Lookup the specified template its role.
     * Intended for use with templates whose Role.isSingleton() is true
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByRole(Role role) {
        Template template = weblogTemplateDao.findByWeblogAndRole(this.weblog, role);

        if (template == null) {
            template = sharedTheme.getTemplateByRole(role);
        }
        return template;
    }

    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByName(String name) {
        Template template = weblogTemplateDao.findByWeblogAndName(this.weblog, name);

        if (template == null) {
            template = sharedTheme.getTemplateByName(name);
        }
        return template;
    }
}
