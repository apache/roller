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
import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;

/**
 * Weblog Category.
 *
 * @struts.form include-all="true"
 *
 * @ejb:bean name="WeblogCategoryData"
 * @hibernate.class lazy="true" table="weblogcategory"
 * @hibernate.cache usage="read-write"
 */
public class WeblogCategoryData 
        implements Serializable {
    
    public static final long serialVersionUID = 1435782148712018954L;
    
    // attributes
    private String id = null;
    private String name = null;
    private String description = null;
    private String image = null;
    private String path = null;
    
    // associations
    private WebsiteData website = null;
    private WeblogCategoryData parentCategory = null;
    private Set childCategories = new HashSet();
    
    
    public WeblogCategoryData() {
    }
    
    public WeblogCategoryData(
            WebsiteData website,
            WeblogCategoryData parent,
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
    
    
    public WeblogCategoryData(WeblogCategoryData otherData) {
        this.setData(otherData);
    }
    
    
    public void setData(WeblogCategoryData otherData) {
        WeblogCategoryData other = (WeblogCategoryData) otherData;
        
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
        return ToStringBuilder.reflectionToString(this);
    }
    
    public boolean equals(Object other) {
        
        if (other == null) return false;
        
        if (other instanceof WeblogCategoryData) {
            WeblogCategoryData o = (WeblogCategoryData)other;
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
     *  generator-class="uuid.hex" unsaved-value="null"
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
    public WebsiteData getWebsite() {
        return website;
    }
    
    public void setWebsite(WebsiteData website) {
        this.website = website;
    }
    
    
    /**
     * Get parent category, or null if category is root of hierarchy.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @hibernate.many-to-one column="parentid" cascade="none" not-null="false"
     */
    public WeblogCategoryData getParent() {
        return this.parentCategory;
    }
    
    public void setParent(WeblogCategoryData parent) {
        this.parentCategory = parent;
    }
    
    
    /**
     * Get child categories of this category.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategoryData"
     *
     * @hibernate.set lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="parentid"
     * @hibernate.collection-one-to-many class="org.apache.roller.pojos.WeblogCategoryData"
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
     * Does this category have a child category with the specified name?
     *
     * @param name The name of the category to check for.
     * @return boolean true if child category exists, false otherwise.
     */
    public boolean hasCategory(String name) {
        Iterator cats = this.getWeblogCategories().iterator();
        WeblogCategoryData cat = null;
        while(cats.hasNext()) {
            cat = (WeblogCategoryData) cats.next();
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
    public boolean descendentOf(WeblogCategoryData ancestor) {
        
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
    
}
