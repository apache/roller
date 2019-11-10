/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.bloggerui.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.tightblog.domain.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeblogTemplateData {
    private List<Template> templates;
    private Map<String, String> availableTemplateRoles = new HashMap<>();
    private Map<String, String> templateRoleDescriptions = new HashMap<>();

    public List<Template> getTemplates() {
        if (templates == null) {
            templates = new ArrayList<>();
        }
        return templates;
    }

    public Map<String, String> getAvailableTemplateRoles() {
        if (availableTemplateRoles == null) {
            availableTemplateRoles = new HashMap<>();
        }

        return availableTemplateRoles;
    }

    public Map<String, String> getTemplateRoleDescriptions() {
        if (templateRoleDescriptions == null) {
            templateRoleDescriptions = new HashMap<>();
        }
        return templateRoleDescriptions;
    }
}
