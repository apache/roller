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
package org.apache.roller.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.util.rome.ContentModule;

import org.apache.roller.util.Utilities;
import org.apache.commons.lang.StringUtils;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.roller.RollerException;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;

/**
 * Represents a planet entry, i.e. an entry that was parsed out of an RSS or 
 * Atom newsfeed by Roller's built-in planet aggregator. 
 * <p>
 * The model coded in this class simple, perhaps too simple, and in the future 
 * it should be replaced by more complete model that can fully represent all 
 * forms of RSS and Atom.
 * 
 * @hibernate.class lazy="false" table="rag_entry"
 */
public class PlanetEntryData extends PersistentObject
        implements Serializable, Comparable {
    protected String    id;
    protected String    handle;
    protected String    title;
    protected String    guid;
    protected String    permalink;
    protected String    author;
    protected String    text = "";
    protected Timestamp published;
    protected Timestamp updated;
    
    private String categoriesString;
    protected PlanetSubscriptionData subscription = null;
    
    /**
     * Construct empty entry.
     */
    public PlanetEntryData() {
    }
    
    /**
     * Create entry from Rome entry.
     */
    public PlanetEntryData(
            SyndFeed romeFeed, SyndEntry romeEntry, PlanetSubscriptionData sub) {
        setSubscription(sub);
        initFromRomeEntry(romeFeed, romeEntry);
    }
    
    /**
     * Create entry from Rome entry.
     */
    public PlanetEntryData(
            WeblogEntryData rollerEntry,
            PlanetSubscriptionData sub,
            Map pagePlugins) throws RollerException {
        setSubscription(sub);
        initFromRollerEntry(rollerEntry, pagePlugins);
    }
    
    /**
     * Init entry from Rome entry
     */
    private void initFromRomeEntry(SyndFeed romeFeed, SyndEntry romeEntry) {
        setTitle(romeEntry.getTitle());
        setPermalink(romeEntry.getLink());
        
        // Play some games to get the author
        DCModule entrydc = (DCModule)romeEntry.getModule(DCModule.URI);
        DCModule feeddc = (DCModule)romeFeed.getModule(DCModule.URI);
        if (romeEntry.getAuthor() != null) {
            setAuthor(romeEntry.getAuthor());
        } else {
            setAuthor(entrydc.getCreator()); // use <dc:creator>
        }
        
        // Play some games to get the date too
        if (romeEntry.getPublishedDate() != null) {
            setPubTime(new Timestamp(romeEntry.getPublishedDate().getTime())); // use <pubDate>
        } else if (entrydc != null) {
            setPubTime(new Timestamp(entrydc.getDate().getTime())); // use <dc:date>
        }
        
        // get content and unescape if it is 'text/plain'
        if (romeEntry.getContents().size() > 0) {
            SyndContent content= (SyndContent)romeEntry.getContents().get(0);
            if (content != null && content.getType().equals("text/plain")) {
                setText(StringEscapeUtils.unescapeHtml(content.getValue()));
            } else if (content != null) {
                setText(content.getValue());
            }
        }
        
        // no content, then try <content:encoded>
        if (getText() == null || getText().trim().length() == 0) {
            ContentModule cm = (ContentModule)romeEntry.getModule(ContentModule.URI);
            if (cm != null) {
                setText(StringEscapeUtils.unescapeHtml(cm.getEncoded()));
            }
        }
        
        // copy categories
        if (romeEntry.getCategories().size() > 0) {
            List list = new ArrayList();
            Iterator cats = romeEntry.getCategories().iterator();
            while (cats.hasNext()) {
                SyndCategory cat = (SyndCategory)cats.next();
                list.add(cat.getName());
            }
            setCategoriesString(list);
        }
    }
    
    /**
     * Init entry from Roller entry
     */
    private void initFromRollerEntry(WeblogEntryData rollerEntry, Map pagePlugins)
    throws RollerException {
        Roller roller = RollerFactory.getRoller();
        PluginManager ppmgr = roller.getPagePluginManager();
        
        String content = "";
        if (!StringUtils.isEmpty(rollerEntry.getText())) {
            content = rollerEntry.getText();
        } else {
            content = rollerEntry.getSummary();
        }
        content = ppmgr.applyWeblogEntryPlugins(pagePlugins, rollerEntry, content);
        
        setAuthor(    rollerEntry.getCreator().getFullName());
        setTitle(     rollerEntry.getTitle());
        setPermalink( rollerEntry.getLink());
        setPubTime(   rollerEntry.getPubTime());
        setText(      content);
        
        setPermalink(RollerRuntimeConfig.getProperty("site.absoluteurl")
        + rollerEntry.getPermaLink());
        
        List cats = new ArrayList();
        cats.add(rollerEntry.getCategory().getPath());
        setCategoriesString(cats);
    }
    
    //----------------------------------------------------------- persistent fields
    
    /**
     * @hibernate.id column="id"
     *     generator-class="uuid.hex" unsaved-value="null"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @hibernate.property column="categories" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getCategoriesString() {
        return categoriesString;
    }
    public void setCategoriesString(String categoriesString) {
        this.categoriesString = categoriesString;
    }
    /**
     * @hibernate.many-to-one column="subscription_id" cascade="none" not-null="true"
     */
    public PlanetSubscriptionData getSubscription() {
        return subscription;
    }
    public void setSubscription(PlanetSubscriptionData subscription) {
        this.subscription = subscription;
    }
    /**
     * @hibernate.property column="author" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    /**
     * @hibernate.property column="content" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getText() {
        return text;
    }
    public void setText(String content) {
        this.text = content;
    }
    /**
     * @hibernate.property column="guid" non-null="false" unique="true"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getGuid() {
        return guid;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }
    /**
     * @hibernate.property column="handle" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getHandle() {
        return handle;
    }
    public void setHandle(String handle) {
        this.handle = handle;
    }
    /**
     * @hibernate.property column="published" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public Timestamp getPubTime() {
        return published;
    }
    public void setPubTime(Timestamp published) {
        this.published = published;
    }
    /**
     * @hibernate.property column="permalink" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getPermalink() {
        return permalink;
    }
    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }
    /**
     * @hibernate.property column="title" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @hibernate.property column="updated" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public Timestamp getUpdateTime() {
        return updated;
    }
    public void setUpdateTime(Timestamp updated) {
        this.updated = updated;
    }
    
    //----------------------------------------------------------------- convenience
    
    /**
     * Returns true if any of entry's categories contain a specific string
     * (case-insensitive comparison).
     */
    public boolean inCategory(String category) {
        Iterator cats = getCategories().iterator();
        while (cats.hasNext()) {
            String catName = ((String)cats.next()).toLowerCase();
            if (catName.indexOf(category.toLowerCase()) != -1) {
                return true;
            }
        }
        return false;
    }
    
    //------------------------------------------------------------- implementation
    
    /**
     * Returns categories as list of WeblogCategoryData objects.
     */
    public List getCategories() {
        List list = new ArrayList();
        if (categoriesString != null) {
            String[] catArray = Utilities.stringToStringArray(categoriesString,",");
            for (int i=0; i<catArray.length; i++) {
                WeblogCategoryData cat = new WeblogCategoryData();
                cat.setName(catArray[i]);
                cat.setPath(catArray[i]);
                list.add(cat);
            }
        }
        return list;
    }
       
    /**
     * Return first entry in category collection.
     * @roller.wrapPojoMethod type="pojo"
     */
    public WeblogCategoryData getCategory() {
        WeblogCategoryData cat = null;
        List cats = getCategories();
        if (cats.size() > 0) {
            cat = (WeblogCategoryData)cats.get(0);
        }
        return cat;
    }

    private void setCategoriesString(List categories) {
        StringBuffer sb = new StringBuffer();
        Iterator cats = categories.iterator();
        while (cats.hasNext()) {
            String cat = (String)cats.next();
            sb.append(cat);
            if (cats.hasNext()) sb.append(",");
        }
        categoriesString = sb.toString();
    }
    
    /** 
     * Returns creator as a UserData object.
     * @roller.wrapPojoMethod type="pojo"
     * TODO: make planet model entry author name, email, and uri
     */
    public UserData getCreator() {
        UserData user = null;
        if (author != null) {
            user = new UserData();
            user.setFullName(author);
            user.setUserName(author);
        }
        return user;
    } 
    
    /**
     * Returns summary (always null for planet entry)
     * @roller.wrapPojoMethod type="simple"
     */
    public String getSummary() {
        return null;
    } 
    
    /**
     * Compare planet entries by comparing permalinks.
     */
    public int compareTo(Object o) {
        PlanetEntryData other = (PlanetEntryData)o;
        return getPermalink().compareTo(other.getPermalink());
    }
    
    /**
     * Compare planet entries by comparing permalinks.
     */
    public boolean equals(Object other) {        
        if(this == other) return true;
        if(!(other instanceof PlanetEntryData)) return false;        
        final PlanetEntryData that = (PlanetEntryData) other;
        return this.permalink.equals(that.getPermalink());
    }
    
    /**
     * Generate hash code based on permalink.
     */
    public int hashCode() {
        return this.permalink.hashCode();
    }
    
    public void setData(PersistentObject vo) {}

    /**
     * Read-only synomym for getSubscription()
     * @roller.wrapPojoMethod type="pojo"
     */
    public PlanetSubscriptionData getWebsite() {
        return this.subscription;        
    }
    public void setWebsite() {
        // noop
    }
}



