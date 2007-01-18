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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.util.Date;


/**
 * Represents a simple static Template.
 *
 * This template is not persisted or managed in any way, this class is here
 * mainly as a wrapper so that we can represent our static template files as
 * an object.
 */
public class StaticTemplate implements Template, Serializable {
    
    private String id = null;
    private String name = null;
    private String description = null;
    private String contents = null;
    private String link = null;
    private Date lastModified = new Date();
    private String templateLanguage = null;
    private boolean hidden = false;
    private boolean navbar = false;
    
    
    public StaticTemplate() {}
    
    public StaticTemplate(String id, String contents, String lang) {
        this.id = id;
        this.name = id;
        this.description = id;
        this.contents = contents;
        this.link = id;
        this.templateLanguage = lang;
    }

    
    public Template getDecorator() {
        return null;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
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

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getTemplateLanguage() {
        return templateLanguage;
    }

    public void setTemplateLanguage(String templateLanguage) {
        this.templateLanguage = templateLanguage;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
    }
    
    public boolean isNavbar() {
        return navbar; 
    }
    
    
}
