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

import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A SharedThemeTemplate represents a template which is part of a SharedTheme.
 */
public class SharedThemeTemplate implements ThemeTemplate, Serializable {
    
    private String id = null;
    private ComponentType action = null;
    private String name = null;
    private String description = null;
    private String link = null;
    private boolean navbar = false;
    private boolean hidden = false;
    private String contentType = null;

    private String contents = null;
    private Date lastModified = null;

    //hash map to cache template Code objects parsed
    private Map<RenditionType, SharedThemeTemplateRendition> templateRenditionHashMap = new HashMap<>();
    
    public SharedThemeTemplate() {}
    
    public SharedThemeTemplate(String id, ComponentType action, String name,
            String desc, String link,
            boolean hidden, boolean navbar) {
        
        this.id = id;
        this.action = action;
        this.name = name;
        this.description = desc;
        this.link = link;
        this.hidden = hidden;
        this.navbar = navbar;
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

    public String getOutputContentType() {
        return contentType;
    }

    @Override
    public ThemeTemplate templateCopy() {
        SharedThemeTemplate copy = new SharedThemeTemplate();
        copy.setId(id);
        copy.setName(name);
        copy.setDescription(description);
        copy.setLink(link);
        copy.setLastModified(lastModified);
        copy.setHidden(hidden);
        copy.setNavbar(navbar);
        copy.setOutputContentType(contentType);
        return copy;
    }

    @XmlElement(name="contentType")
    public void setOutputContentType(String contentType) {
        this.contentType = contentType;
    }

    public void addTemplateRendition(SharedThemeTemplateRendition rendition){
        this.templateRenditionHashMap.put(rendition.getType(), rendition);
    }

    public TemplateRendition getTemplateRendition(RenditionType type) throws WebloggerException {
        return templateRenditionHashMap.get(type);
    }

    public Map<RenditionType, SharedThemeTemplateRendition> getRenditionMap() {
        return templateRenditionHashMap;
    }

    @XmlJavaTypeAdapter(value=MapAdapter.class)
    @XmlElements(@XmlElement(name="renditions"))
    public void setRenditionMap(Map<RenditionType, SharedThemeTemplateRendition> renditionTable) {
        this.templateRenditionHashMap = renditionTable;
    }

    static class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<RenditionType, SharedThemeTemplateRendition>> {

        static class AdaptedMap {
            @XmlElements(@XmlElement(name="rendition"))
            public List<SharedThemeTemplateRendition> renditions = new ArrayList<>();
        }

        @Override
        public Map<RenditionType, SharedThemeTemplateRendition> unmarshal(AdaptedMap list) throws Exception {
            Map<RenditionType, SharedThemeTemplateRendition> map = new HashMap<>();
            for(SharedThemeTemplateRendition item : list.renditions) {
                map.put(item.getType(), item);
            }
            return map;
        }

        @Override
        public AdaptedMap marshal(Map<RenditionType, SharedThemeTemplateRendition> map) throws Exception {
            // unused
            return null;
        }
    }
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }


    public String toString() {
        return (id + "," + name + "," + description + "," + link + "," + 
                lastModified + "\n\n" + contents + "\n");
    }

    public ComponentType getAction() {
        return action;
    }

    @XmlAttribute
    public void setAction(ComponentType action) {
        this.action = action;
    }

}
