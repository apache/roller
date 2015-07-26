/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.util.UUIDGenerator;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Represents a planet entry, i.e. an entry that was parsed out of an RSS or 
 * Atom newsfeed by Roller's built-in planet aggregator. 
 * <p>
 * The model coded in this class simple, perhaps too simple, and in the future 
 * it should be replaced by more complete model that can fully represent all 
 * forms of RSS and Atom.
 */
@Entity
@Table(name="planet_subscription_entry")
@NamedQueries({
        @NamedQuery(name="SubscriptionEntry.getBySubscription",
                query="SELECT p FROM SubscriptionEntry p WHERE p.subscription = ?1 ORDER BY p.pubTime DESC")
})
public class SubscriptionEntry implements Serializable, Comparable<SubscriptionEntry> {
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String handle;
    private String title;
    private String guid;
    private String permalink;
    private String author;
    private String text = "";
    private Timestamp published;
    private Timestamp updated;
    private String categoriesString;
    
    // associations
    private Subscription subscription = null;
    
    
    public SubscriptionEntry() {}
    
    @Id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    public String getHandle() {
        return handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    @Basic(optional=false)
    public String getPermalink() {
        return permalink;
    }
    
    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }
    
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }

    @Column(name="content")
    public String getText() {
        return text;
    }
    
    public void setText(String content) {
        this.text = content;
    }

    @Column(name="published", nullable=false)
    public Timestamp getPubTime() {
        return published;
    }
    
    public void setPubTime(Timestamp published) {
        this.published = published;
    }

    @Column(name="updated")
    public Timestamp getUpdateTime() {
        return updated;
    }
    
    public void setUpdateTime(Timestamp updated) {
        this.updated = updated;
    }


    @Column(name="categories")
    public String getCategoriesString() {
        return categoriesString;
    }
    
    public void setCategoriesString(String categoriesString) {
        this.categoriesString = categoriesString;
    }


    @ManyToOne
    @JoinColumn(name="subscription_id", nullable=false)
    public Subscription getSubscription() {
        return subscription;
    }
    
    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
    
    //----------------------------------------------------------------- convenience
    
    /**
     * Returns true if any of entry's categories contain a specific string
     * (case-insensitive comparison).
     */
    public boolean inCategory(String category) {
        for (String cat : getCategories()) {
            if (cat.toLowerCase().contains(category.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    //------------------------------------------------------------- implementation
    
    /**
     * Returns categories as list of strings
     */
    @Transient
    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        if (getCategoriesString() != null) {
            String[] catArray = Utilities.stringToStringArray(getCategoriesString(),",");
            list.addAll(Arrays.asList(catArray));
        }
        return list;
    }
       
    /**
     * Return first entry in category collection.
     */
    @Transient
    public String getCategory() {
        List<String> cats = getCategories();
        String firstCat = null;
        if (cats.size() > 0) {
            firstCat = cats.get(0);
        }
        return firstCat;
    }

    public void setCategoriesString(List<String> categoryNames) {
        StringBuilder sb = new StringBuilder();
        Iterator cats = categoryNames.iterator();
        while (cats.hasNext()) {
            String catName = (String) cats.next();
            sb.append(catName);
            if (cats.hasNext()) {
                sb.append(",");
            }
        }
        categoriesString = sb.toString();
    }
    
    /** 
     * Returns creator as a UserData object.
     * TODO: make planet model entry author name, email, and uri
     */
/*
    @Transient
    public Author getCreator() {
        Author user = null;
        if (getAuthor() != null) {
            user = new Author();
            user.setFullName(getAuthor());
            user.setUserName(getAuthor());
        }
        return user;
    } 
*/
    /**
     * Returns summary (always null for planet entry)
     */
    @Transient
    public String getSummary() {
        return null;
    } 
    

    /**
     * Read-only synomym for getSubscription()
     */
    @Transient
    public Subscription getWebsite() {
        return getSubscription();
    }
    public void setWebsite() {
        // noop
    }

    /**
     * Return text as content, to maintain compatibility with PlanetTool templates.
     */
    @Transient
    public String getContent() {
        return getText();
    }
    public void setContent(String ignored) {
        // no-op
    }

    /**
     * Return updateTime as updated, to maintain compatibility with PlanetTool templates.
     */
    @Transient
    public Timestamp getUpdated() {
        return updated;
    }
    public void setUpdated(Timestamp ignored) {
        // no-op
    }

    /**
     * Return pubTime as published, to maintain compatibility with PlanetTool templates.
     */
    @Transient
    public Timestamp getPublished() {
        return published;
    }
    public void setPublished(Timestamp ignored) {
        // no-op
    }

    /**
     * Compare planet entries by comparing permalinks.
     */
    public int compareTo(SubscriptionEntry other) {
        return getPermalink().compareTo(other.getPermalink());
    }

    /**
     * Compare planet entries by comparing permalinks.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SubscriptionEntry)) {
            return false;
        }
        final SubscriptionEntry that = (SubscriptionEntry) other;
        return getPermalink().equals(that.getPermalink());
    }

    /**
     * Generate hash code based on permalink.
     */
    public int hashCode() {
        return getPermalink().hashCode();
    }
}
