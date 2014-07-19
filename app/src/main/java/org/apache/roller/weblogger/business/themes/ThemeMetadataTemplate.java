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

import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;

import java.util.HashMap;
import java.util.Map;

/**
 * A parsed 'template' element of a theme metadata descriptor.
 */
public class ThemeMetadataTemplate {
    
    private String action = null;
    private String name = null;
    private String description = null;
    private String link = null;
    private boolean navbar = false;
    private boolean hidden = false;
    private String contentType = null;

    // Hash table to keep metadata about parsed template code files
    private Map<RenditionType, ThemeMetadataTemplateRendition> templateCodeTable
            = new HashMap<RenditionType, ThemeMetadataTemplateRendition>();

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isNavbar() {
        return navbar;
    }

    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void addTemplateCode(RenditionType type, ThemeMetadataTemplateRendition templateCode) {
        this.getTemplateCodeTable().put(type, templateCode);
    }

    public ThemeMetadataTemplateRendition getTemplateRendition(RenditionType type){
        return this.getTemplateCodeTable().get(type);
    }

    public Map<RenditionType, ThemeMetadataTemplateRendition> getTemplateCodeTable() {
        return templateCodeTable;
    }

}
