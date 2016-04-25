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
import org.apache.roller.weblogger.pojos.Template;
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
 * A SharedTemplate represents a template which is part of a SharedTheme.
 */
public class SharedTemplate implements Template, Serializable {
    
    private String id = null;
    private ComponentType role = null;
    private String name = null;
    private String description = null;
    private String relativePath = null;

    private String contents = null;

    public SharedTemplate(String id, TemplateRendition.TemplateLanguage lang) {
        this.id = id;
        SharedTemplateRendition templateRendition = new SharedTemplateRendition();
        templateRendition.setTemplateLanguage(lang);
        templateRendition.setRenditionType(RenditionType.NORMAL);
        addTemplateRendition(templateRendition);
    }


    //hash map to cache template Code objects parsed
    private Map<RenditionType, SharedTemplateRendition> templateRenditionHashMap = new HashMap<>();
    
    public SharedTemplate() {}
    
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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void addTemplateRendition(SharedTemplateRendition rendition){
        this.templateRenditionHashMap.put(rendition.getRenditionType(), rendition);
    }

    public TemplateRendition getTemplateRendition(RenditionType type) throws WebloggerException {
        return templateRenditionHashMap.get(type);
    }

    public Map<RenditionType, SharedTemplateRendition> getRenditionMap() {
        return templateRenditionHashMap;
    }

    @XmlJavaTypeAdapter(value=MapAdapter.class)
    @XmlElements(@XmlElement(name="renditions"))
    public void setRenditionMap(Map<RenditionType, SharedTemplateRendition> renditionTable) {
        this.templateRenditionHashMap = renditionTable;
    }

    static class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<RenditionType, SharedTemplateRendition>> {

        static class AdaptedMap {
            @XmlElements(@XmlElement(name="rendition"))
            public List<SharedTemplateRendition> renditions = new ArrayList<>();
        }

        @Override
        public Map<RenditionType, SharedTemplateRendition> unmarshal(AdaptedMap list) throws Exception {
            Map<RenditionType, SharedTemplateRendition> map = new HashMap<>();
            for(SharedTemplateRendition item : list.renditions) {
                map.put(item.getRenditionType(), item);
            }
            return map;
        }

        @Override
        public AdaptedMap marshal(Map<RenditionType, SharedTemplateRendition> map) throws Exception {
            // unused
            return null;
        }
    }

    public Date getLastModified() {
        return null;
    }

    public String toString() {
        return id + "," + name + "," + description + "," + relativePath + "\n\n" + contents + "\n";
    }

    public ComponentType getRole() {
        return role;
    }

    @XmlAttribute
    public void setRole(ComponentType role) {
        this.role = role;
    }

    @Override
    public TemplateDerivation getDerivation() {
        return TemplateDerivation.SHARED;
    }
}
