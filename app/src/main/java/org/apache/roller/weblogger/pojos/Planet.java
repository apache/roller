/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.business.WebloggerFactory;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Planet Group.
 */
@Entity
@Table(name="planet")
@NamedQueries({
        @NamedQuery(name="Planet.getByHandle",
                query="SELECT p FROM Planet p WHERE p.handle = ?1"),
        @NamedQuery(name="Planet.getAll",
                query="SELECT p FROM Planet p")
})
public class Planet implements Serializable, Comparable<Planet> {

    private transient String[] catArray = null;
    
    // attributes
    private String id = WebloggerCommon.generateUUID();
    private String handle = null;
    private String title = null;
    private String description = null;
    private int maxPageEntries = 45;
    private int maxFeedEntries = 45;
    
    // is this really used?
    private String categoryRestriction = null;
    
    // associations
    private Set<Subscription> subscriptions = new TreeSet<Subscription>();
    
    
    public Planet() {}
    
    public Planet(String handle, String title, String desc) {
        this.handle = handle;
        this.title = title;
        this.description = desc;
    }
    
    /**
     * For comparing planets and sorting, ordered by Title.
     */
    public int compareTo(Planet other) {
        return getTitle().compareTo(other.getTitle());
    }
    
    @Id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Basic(optional = false)
    public String getHandle() {
        return handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }

    @Basic(optional = false)
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="max_feed_entries")
    public int getMaxFeedEntries() {
        return maxFeedEntries;
    }
    
    public void setMaxFeedEntries(int maxFeedEntries) {
        this.maxFeedEntries = maxFeedEntries;
    }

    @Column(name="max_page_entries")
    public int getMaxPageEntries() {
        return maxPageEntries;
    }
    
    public void setMaxPageEntries(int maxPageEntries) {
        this.maxPageEntries = maxPageEntries;
    }

    @Column(name="cat_restriction")
    public String getCategoryRestriction() {
        return categoryRestriction;
    }
    
    public void setCategoryRestriction(String categoryRestriction) {
        this.categoryRestriction = categoryRestriction;
        catArray = null;
    }

    @OneToMany(targetEntity=Subscription.class,
            cascade=CascadeType.ALL, mappedBy="planet")
    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }
    
    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
    
    
    /**
     * Return a list of the most recent 10 entries from this planet.
     */
    @Transient
    public List<SubscriptionEntry> getRecentEntries() {
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        try {
            return mgr.getEntries(this, 0, 10);
        } catch(Exception e) {
            return Collections.emptyList();
        }
    }
    
    
    /**
     * Returns true if entry is qualified for inclusion in this planet.
     */
    public boolean qualified(SubscriptionEntry entry) {
        String[] cats = createCategoryRestrictionAsArray();
        if (cats == null || cats.length == 0) {
            return true;
        }
        for (String cat : cats) {
            if (entry.inCategory(cat)) {
                return true;
            }
        }
        return false;
    }
    
    
    private String[] createCategoryRestrictionAsArray() {
        if (catArray == null && getCategoryRestriction() != null) {
            StringTokenizer toker = new StringTokenizer(getCategoryRestriction(),",");
            catArray = new String[toker.countTokens()];
            int i = 0;
            
            while (toker.hasMoreTokens()) {
                catArray[i++] = toker.nextToken();
            }
        }
        return catArray;
    }

    @Transient
    public String getAbsoluteURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getPlanetURL(getHandle());
    }
}
