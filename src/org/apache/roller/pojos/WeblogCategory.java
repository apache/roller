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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.util.UUIDGenerator;


/**
 * Weblog Category.
 * 
 * @ejb:bean name="WeblogCategory"
 * @hibernate.cache usage="read-write"
 * @hibernate.class lazy="true" table="weblogcategory"
 * @struts.form include-all="true"
 */
public class WeblogCategory implements Serializable {
    
    public static final long serialVersionUID = 1435782148712018954L;
    
    private static Log log = LogFactory.getLog(WeblogCategory.class);
    
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private String description = null;
    private String image = null;
    private String path = null;
    
    // associations
    private Weblog website = null;
    private WeblogCategory parentCategory = null;
    private Set childCategories = new HashSet();
    
    
    public WeblogCategory() {
    }
    
    public WeblogCategory(
            Weblog website,
            WeblogCategory parent,
            java.lang.String name,
            java.lang.String description,
            java.lang.String image) {
        
        this.name = name;
        this.description = description;
        this.image = image;
        
        this.website = website;
        this.parentCategory = parent;

        // calculate path
        if(parent == null) {
            this.path = "/";
        } else if("/".equals(parent.getPath())) {
            this.path = "/"+name;
        } else {
            this.path = parent.getPath() + "/" + name;
        }
    }
    
    
    public WeblogCategory(WeblogCategory otherData) {
        this.setData(otherData);
    }
    
    
    public void setData(WeblogCategory otherData) {
        WeblogCategory other = (WeblogCategory) otherData;
        
        this.id = other.getId();
        this.website = other.getWebsite();
        this.name = other.getName();
        this.description = other.getDescription();
        this.image = other.getImage();
        this.path = other.getPath();
        
        this.parentCategory = other.getParent();
        this.childCategories = other.getWeblogCategories();
    }
    
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.path);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        
        if (other == null) return false;
        
        if (other instanceof WeblogCategory) {
            WeblogCategory o = (WeblogCategory)other;
            return new EqualsBuilder()
                .append(getPath(), o.getPath()) 
                //.append(getWebsite(), o.getWebsite()) 
                .isEquals();
        }        
        return false;
    }
        
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getPath())
            //.append(getWebsite())
            .toHashCode();
    }
    
    
    /**
     * Database surrogate key.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.id column="id"
     *  generator-class="assigned"  
     */
    public java.lang.String getId() {
        return this.id;
    }
    
    public void setId(java.lang.String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    
    
    /**
     * The display name for this category.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public java.lang.String getName() {
        return this.name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    
    /**
     * A full description for this category.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public java.lang.String getDescription() {
        return this.description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    
    /**
     * An image icon to represent this category.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="image" non-null="true" unique="false"
     */
    public java.lang.String getImage() {
        return this.image;
    }
    
    public void setImage(java.lang.String image) {
        this.image = image;
    }
    
    
    /**
     * The full path to this category in the hierarchy.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="path" non-null="true" unique="false"
     */
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    
    /**
     * Get the weblog which owns this category.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public Weblog getWebsite() {
        return website;
    }
    
    public void setWebsite(Weblog website) {
        this.website = website;
    }
    
    
    /**
     * Get parent category, or null if category is root of hierarchy.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @hibernate.many-to-one column="parentid" cascade="none" not-null="false"
     */
    public WeblogCategory getParent() {
        return this.parentCategory;
    }
    
    public void setParent(WeblogCategory parent) {
        this.parentCategory = parent;
    }
    
    
    /**
     * Get child categories of this category.
     * 
     * @hibernate.collection-key column="parentid"
     * @hibernate.collection-one-to-many class="org.apache.roller.pojos.WeblogCategory"
     * @hibernate.set lazy="true" inverse="true" cascade="delete"
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategory"
     */
    public Set getWeblogCategories() {
        return this.childCategories;
    }
    
    private void setWeblogCategories(Set cats) {
        this.childCategories = cats;
    }
    
    
    /**
     * Retrieve all weblog entries in this category and, optionally, include
     * weblog entries all sub-categories.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogEntryData"
     *
     * @param subcats True if entries from sub-categories are to be returned.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List retrieveWeblogEntries(boolean subcats) throws RollerException {
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        return wmgr.getWeblogEntries(this, subcats);
    }
    
    
    /**
     * Add a category as a child of this category.
     */
    public void addCategory(WeblogCategory category) {
        
        // make sure category is not null
        if(category == null || category.getName() == null) {
            throw new IllegalArgumentException("Category cannot be null and must have a valid name");
        }
        
        // make sure we don't already have a category with that name
        if(this.hasCategory(category.getName())) {
            throw new IllegalArgumentException("Duplicate category name '"+category.getName()+"'");
        }
        
        // set ourselves as the parent of the category
        category.setParent(this);
        
        // add it to our list of child categories
        getWeblogCategories().add(category);
    }
    
    
    /**
     * Does this category have a child category with the specified name?
     *
     * @param name The name of the category to check for.
     * @return boolean true if child category exists, false otherwise.
     */
    public boolean hasCategory(String name) {
        Iterator cats = this.getWeblogCategories().iterator();
        WeblogCategory cat = null;
        while(cats.hasNext()) {
            cat = (WeblogCategory) cats.next();
            if(name.equals(cat.getName())) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Is this category a descendent of the other category?
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean descendentOf(WeblogCategory ancestor) {
        
        // if this is a root node then we can't be a descendent
        if(getParent() == null) {
            return false;
        } else {
            // if our path starts with our parents path then we are a descendent
            return this.path.startsWith(ancestor.getPath());
        }
    }
    
    
    /**
     * Determine if category is in use. Returns true if any weblog entries
     * use this category or any of it's subcategories.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean isInUse() {
        try {
            return RollerFactory.getRoller().getWeblogManager().isWeblogCategoryInUse(this);
        } catch (RollerException e) {
            throw new RuntimeException(e);
        }
    }
    /** TODO: fix form generation so this is not needed. */
    public void setInUse(boolean dummy) {}
    
    
    // convenience method for updating the category name, which triggers a path tree rebuild
    public void updateName(String newName) throws RollerException {
        
        // update name
        setName(newName);
        
        // calculate path
        if(getParent() == null) {
            setPath("/");
        } else if("/".equals(getParent().getPath())) {
            setPath("/"+getName());
        } else {
            setPath(getParent().getPath() + "/" + getName());
        }
        
        // update path tree for all children
        updatePathTree(this);
    }
    
    
    // updates the paths of all descendents of the given category
    public static void updatePathTree(WeblogCategory cat) 
            throws RollerException {
        
        log.debug("Updating path tree for category "+cat.getPath());
        
        WeblogCategory childCat = null;
        Iterator childCats = cat.getWeblogCategories().iterator();
        while(childCats.hasNext()) {
            childCat = (WeblogCategory) childCats.next();
            
            log.debug("OLD child category path was "+childCat.getPath());
            
            // update path and save
            if("/".equals(cat.getPath())) {
                childCat.setPath("/" + childCat.getName());
            } else {
                childCat.setPath(cat.getPath() + "/" + childCat.getName());
            }
            RollerFactory.getRoller().getWeblogManager().saveWeblogCategory(childCat);
            
            log.debug("NEW child category path is "+ childCat.getPath());
            
            // then make recursive call to update this cats children
            updatePathTree(childCat);
        }
    }
    
}
