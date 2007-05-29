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

import org.apache.roller.business.BookmarkManager;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * <p>Represents a single URL in a user's favorite web-bookmarks collection.
 * Don't construct one of these yourself, instead use the create method in 
 * the your BookmarkManager implementation.</p>
 *
 * @ejb:bean name="WeblogBookmark"
 * @hibernate.class lazy="true" table="bookmark"
 * @hibernate.cache usage="read-write"
 */
public class WeblogBookmark
    implements Serializable, Comparable
{
    static final long serialVersionUID = 2315131256728236003L;
    
    private WeblogBookmarkFolder folder;

    private String id = UUIDGenerator.generateUUID();
    private String name;
    private String description;
    private String url;
    private Integer weight;
    private Integer priority;
    private String image;
    private String feedUrl;  
    
    private BookmarkManager bookmarkManager = null;

    //----------------------------------------------------------- Constructors
    
    /** Default constructor, for use in form beans only. */
    public WeblogBookmark()
    {
    }
    
    public WeblogBookmark(
        WeblogBookmarkFolder parent,
        String name, 
        String desc, 
        String url, 
        String feedUrl,
        Integer weight, 
        Integer priority, 
        String image)
    {
        this.folder = parent;
        this.name = name;
        this.description = desc;
        this.url = url;
        this.feedUrl = feedUrl;
        this.weight = weight;
        this.priority = priority;
        this.image = image;   
    }

    /** For use by BookmarkManager implementations only. */
    public WeblogBookmark(BookmarkManager bmgr)
    {
        bookmarkManager = bmgr;
    }

    //------------------------------------------------------------- Attributes
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     *
     * @ejb:persistent-field 
     * 
     * @hibernate.id column="id"
     *     generator-class="assigned"  
     */
    public String getId()
    {
        return this.id;
    }

    /** @ejb:persistent-field */
    public void setId(String id)
    {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }

    /** 
     * Name of bookmark.
     * 
     * @roller.wrapPojoMethod type="simple"
     *
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator-args arg0resource="bookmarkForm.name"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName()
    {
        return this.name;
    }

    /** @ejb:persistent-field */
    public void setName(String name)
    {
        this.name = name;
    }

    /** 
     * Description of bookmark.
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public String getDescription()
    {
        return this.description;
    }

    /** @ejb:persistent-field */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /** 
     * URL of bookmark.
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="url" non-null="true" unique="false"
     */
    public String getUrl()
    {
        return this.url;
    }

    /** @ejb:persistent-field */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /** 
     * Weight indicates prominence of link
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="integer" msgkey="errors.integer"
     * @struts.validator-args arg0resource="bookmarkForm.weight"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="weight" non-null="true" unique="false"
     */
    public java.lang.Integer getWeight()
    {
        return this.weight;
    }

    /** @ejb:persistent-field */
    public void setWeight(java.lang.Integer weight)
    {
        this.weight = weight;
    }

    /** 
     * Priority determines order of display 
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="integer" msgkey="errors.integer"
     * @struts.validator-args arg0resource="bookmarkForm.priority"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="priority" non-null="true" unique="false"
     */
    public java.lang.Integer getPriority()
    {
        return this.priority;
    }

    /** @ejb:persistent-field */
    public void setPriority(java.lang.Integer priority)
    {
        this.priority = priority;
    }

    /** 
     * @ejb:persistent-field 
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @hibernate.property column="image" non-null="true" unique="false"
     */
    public String getImage()
    {
        return this.image;
    }

    /** @ejb:persistent-field */
    public void setImage(String image)
    {
        this.image = image;
    }

    /** 
     * @ejb:persistent-field 
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @hibernate.property column="feedurl" non-null="true" unique="false"
     */
    public String getFeedUrl()
    {
        return this.feedUrl;
    }

    /** @ejb:persistent-field */
    public void setFeedUrl(String feedUrl)
    {
        this.feedUrl = feedUrl;
    }

    //---------------------------------------------------------- Relationships
    
    /** 
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="folderid" cascade="none" not-null="true"
     */
    public org.apache.roller.pojos.WeblogBookmarkFolder getFolder()
    {
        return this.folder;
    }

    /** @ejb:persistent-field */
    public void setFolder(org.apache.roller.pojos.WeblogBookmarkFolder folder)
    {
        this.folder = folder;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.url);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogBookmark != true) return false;
        WeblogBookmark o = (WeblogBookmark)other;
        return new EqualsBuilder()
            .append(getName(), o.getName()) 
            .append(getFolder(), o.getFolder()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getName())
            .append(getFolder())
            .toHashCode();
    }
    
    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogBookmark other)
    {
        this.id = other.getId();
        this.name = other.getName();
        this.description = other.getDescription();
        this.url = other.getUrl();
        this.weight = other.getWeight();
        this.priority = other.getPriority();
        this.folder = other.getFolder();
        this.image = other.getImage();
        this.feedUrl = other.getUrl();
    }

    /** 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        return bookmarkComparator.compare(this, o);
    }
    
    private BookmarkComparator bookmarkComparator = new BookmarkComparator();

    /**
     * @param impl
     */
    public void setBookmarkManager(BookmarkManager bmgr)
    {
        bookmarkManager = bmgr;
    }

    public Weblog getWebsite()
    {
        return this.folder.getWebsite();
    }

}