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

import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


/**
 * A Theme based implementation of a Template.  A ThemeTemplate represents a
 * template which is part of a shared Theme.
 */
public class SharedThemeTemplate implements ThemeTemplate, Serializable {
    
    private String id = null;
    private ComponentType action = null;
    private String name = null;
    private String description = null;
    private String contents = null;
    private String link = null;
    private Date lastModified = null;
    private boolean hidden = false;
    private boolean navbar = false;
    private String  outputContentType = null;
    private String type = null;

    //hash map to cache template Code objects parsed
    private Map<RenditionType, TemplateRendition> templateRenditionHashMap = new EnumMap<>(RenditionType.class);
    
    public SharedThemeTemplate() {}
    
    public SharedThemeTemplate(String id, ComponentType action, String name,
            String desc, String contents, String link, Date date, 
            boolean hidden, boolean navbar) {
        
        this.id = id;
        this.action = action;
        this.name = name;
        this.description = desc;
        this.contents = contents;
        this.link = link;
        this.lastModified = date;
        this.hidden = hidden;
        this.navbar = navbar;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
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

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean isHidden) {
        this.hidden = isHidden;
    }

    @Override
    public boolean isNavbar() {
        return navbar;
    }

    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
    }

    @Override
    public String getOutputContentType() {
        return outputContentType;
    }

    public void setOutputContentType(String outputContentType) {
        this.outputContentType = outputContentType;
    }
    
    @Override
    public String toString() {
        return (id + "," + name + "," + description + "," + link + "," + 
                lastModified + "\n\n" + contents + "\n");
    }

    @Override
    public ComponentType getAction() {
        return action;
    }

    public void setAction(ComponentType action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    @Override
    public TemplateRendition getTemplateRendition(RenditionType type) throws WebloggerException {
        return templateRenditionHashMap.get(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addTemplateRendition(TemplateRendition rendition){
        this.templateRenditionHashMap.put(rendition.getType(), rendition);
    }
}
