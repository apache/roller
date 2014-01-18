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
 */

package org.apache.roller.planet.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.util.UUIDGenerator;


/**
 * Represents a planet entry, i.e. an entry that was parsed out of an RSS or 
 * Atom newsfeed by Roller's built-in planet aggregator. 
 * <p>
 * The model coded in this class simple, perhaps too simple, and in the future 
 * it should be replaced by more complete model that can fully represent all 
 * forms of RSS and Atom.
 */
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
    
    
    public String getText() {
        return text;
    }
    
    public void setText(String content) {
        this.text = content;
    }
    
    
    public Timestamp getPubTime() {
        return published;
    }
    
    public void setPubTime(Timestamp published) {
        this.published = published;
    }
    
    
    public Timestamp getUpdateTime() {
        return updated;
    }
    
    public void setUpdateTime(Timestamp updated) {
        this.updated = updated;
    }
    
    
    public String getCategoriesString() {
        return categoriesString;
    }
    
    public void setCategoriesString(String categoriesString) {
        this.categoriesString = categoriesString;
    }
    
    
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
        for (Category cat : getCategories()) {
            String catName = cat.getName().toLowerCase();
            if (catName.contains(category.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    //------------------------------------------------------------- implementation
    
    /**
     * Returns categories as list of WeblogCategoryData objects.
     */
    public List<Category> getCategories() {
        List<Category> list = new ArrayList<Category>();
        if (getCategoriesString() != null) {
            String[] catArray = Utilities.stringToStringArray(getCategoriesString(),",");
            for (String catName : catArray) {
                Category cat = new Category();
                cat.setName(catName);
                cat.setPath(catName);
                list.add(cat);
            }
        }
        return list;
    }
       
    /**
     * Return first entry in category collection.
     */
    public Category getCategory() {
        Category cat = null;
        List cats = getCategories();
        if (cats.size() > 0) {
            cat = (Category)cats.get(0);
        }
        return cat;
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
    public Author getCreator() {
        Author user = null;
        if (getAuthor() != null) {
            user = new Author();
            user.setFullName(getAuthor());
            user.setUserName(getAuthor());
        }
        return user;
    } 
    
    /**
     * Returns summary (always null for planet entry)
     */
    public String getSummary() {
        return null;
    } 
    

    /**
     * Read-only synomym for getSubscription()
     */
    public Subscription getWebsite() {
        return getSubscription();
    }
    public void setWebsite() {
        // noop
    }

    /**
     * Return text as content, to maintain compatibility with PlanetTool templates.
     */
    public String getContent() {
        return getText();
    }
    public void setContent(String ignored) {
        // no-op
    }

    /**
     * Return updateTime as updated, to maintain compatibility with PlanetTool templates.
     */
    public Timestamp getUpdated() {
        return updated;
    }
    public void setUpdated(Timestamp ignored) {
        // no-op
    }

    /**
     * Return pubTime as published, to maintain compatibility with PlanetTool templates.
     */
    public Timestamp getPublished() {
        return published;
    }
    public void setPublished(Timestamp ignored) {
        // no-op
    }

}
