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
 */

package org.apache.roller.weblogger.business.themes;

import java.util.HashSet;
import java.util.Set;


/**
 * Represents a parsed version of a theme xml metadata descriptor.
 */
public class ThemeMetadata {
    
    private String id = null;
    private String name = null;
    private String author = null;
    private String previewImage = null;
    private ThemeMetadataTemplate stylesheet = null;
    private Set templates = new HashSet();
    private Set resources = new HashSet();
    
    
    public ThemeMetadata() {}

    
    public void addTemplate(ThemeMetadataTemplate template) {
        this.templates.add(template);
    }
    
    public void addResource(String resource) {
        this.resources.add(resource);
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

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public Set getTemplates() {
        return templates;
    }

    public void setTemplates(Set templates) {
        this.templates = templates;
    }

    public Set getResources() {
        return resources;
    }

    public void setResources(Set resources) {
        this.resources = resources;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ThemeMetadataTemplate getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(ThemeMetadataTemplate stylesheet) {
        this.stylesheet = stylesheet;
    }
    
}
