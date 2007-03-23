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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The Theme object encapsulates all elements of a single weblog theme.  It
 * is used mostly to contain all the templates for a theme, but does contain
 * other theme related attributes such as name, last modifed date, etc.
 */
public class Theme implements Serializable, Comparable {
    
    // this is the name that will be used to identify a user customized theme
    public static final String CUSTOM = "custom";
    
    private String id = null;
    private String name = null;
    private String description = null;
    private String author = null;
    private Date lastModified = null;
    private boolean enabled = false;
    
    // we keep templates in a Map for faster lookups by name
    // the Map contains ... (template name, ThemeTemplate)
    private Map templatesByName = new HashMap();
    
    // we keep templates in a Map for faster lookups by link
    // the Map contains ... (template link, ThemeTemplate)
    private Map templatesByLink = new HashMap();
    
    // we keep templates in a Map for faster lookups by action
    // the Map contains ... (template action, ThemeTemplate)
    private Map templatesByAction = new HashMap();
    
    // we keep resources in a Map for faster lookups by path
    // the Map contains ... (resource path, File)
    private Map resources = new HashMap();
    
    
    public Theme() {}

    
    /**
     * Get the collection of all templates associated with this Theme.
     */
    public List getTemplates() {
        return new ArrayList(this.templatesByName.values());
    }
    
    
    public Template getDefaultTemplate() {
        return (ThemeTemplate) this.templatesByAction.get(Template.ACTION_WEBLOG);
    }
    
    
    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplate(String name) {
        return (ThemeTemplate) this.templatesByName.get(name);
    }
    
    
    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByLink(String link) {
        return (ThemeTemplate) this.templatesByLink.get(link);
    }
    
    
    /**
     * Lookup the specified template by action.
     * Returns null if the template cannot be found.
     */
    public Template getTemplateByAction(String action) {
        return (ThemeTemplate) this.templatesByAction.get(action);
    }
    
    
    /**
     * Set the value for a given template name.
     */
    public void addTemplate(ThemeTemplate template) {
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getLink(), template);
        this.templatesByAction.put(template.getAction(), template);
    }
    
    
    /**
     * Get the collection of all resources associated with this Theme.
     *
     * It is assured that the resources are returned sorted by pathname.
     */
    public List getResources() {
        
        // make sure resources are sorted.
        List myResources = new ArrayList(this.resources.values());
        Collections.sort(myResources);
        
        return myResources;
    }
    
    
    /**
     * Lookup the specified resource by path.
     * Returns null if the resource cannot be found.
     */
    public File getResource(String path) {
        return (File) this.resources.get(path);
    }
    
    
    /**
     * Set the value for a given resource path.
     */
    public void setResource(String path, File resource) {
        this.resources.put(path, resource);
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("\n");
        
        Iterator it = this.templatesByName.values().iterator();
        while(it.hasNext()) {
            sb.append(it.next());
            sb.append("\n");
        }
        
        return sb.toString();
        
    }
    
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        Theme other = (Theme) o;
        return getName().compareTo(other.getName());
    }
    
}
