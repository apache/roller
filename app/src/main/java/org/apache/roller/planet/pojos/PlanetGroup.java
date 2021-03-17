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
 */

package org.apache.roller.planet.pojos;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Planet Group.
 */
public class PlanetGroup implements Serializable, Comparable<PlanetGroup> {

    private transient String[] catArray = null;
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String handle = null;
    private String title = null;
    private String description = null;
    private int maxPageEntries = 45;
    private int maxFeedEntries = 45;
    
    // is this really used?
    private String categoryRestriction = null;
    
    // associations
    private Planet planet = null;
    private Set<Subscription> subscriptions = new TreeSet<>();
    
    
    public PlanetGroup() {}
    
    public PlanetGroup(Planet planet, String handle, String title, String desc) {
        this.planet = planet;
        this.handle = handle;
        this.title = title;
        this.description = desc;
    }
    
    /**
     * For comparing groups and sorting, ordered by Title.
     */
    @Override
    public int compareTo(PlanetGroup other) {
        return getTitle().compareTo(other.getTitle());
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getMaxFeedEntries() {
        return maxFeedEntries;
    }
    
    public void setMaxFeedEntries(int maxFeedEntries) {
        this.maxFeedEntries = maxFeedEntries;
    }
    
    public int getMaxPageEntries() {
        return maxPageEntries;
    }
    
    public void setMaxPageEntries(int maxPageEntries) {
        this.maxPageEntries = maxPageEntries;
    }
    
    public String getCategoryRestriction() {
        return categoryRestriction;
    }
    
    public void setCategoryRestriction(String categoryRestriction) {
        this.categoryRestriction = categoryRestriction;
        catArray = null;
    }
    
    public Planet getPlanet() {
        return planet;
    }
    
    public void setPlanet(Planet planet) {
        this.planet = planet;
    }
    
    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }
    
    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
    
    
    /**
     * Return a list of the most recent 10 entries from this group.
     */
    public List<SubscriptionEntry> getRecentEntries() {
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        try {
            return mgr.getEntries(this, 0, 10);
        } catch(Exception e) {
            return Collections.emptyList();
        }
    }
    
    
    /**
     * Returns true if entry is qualified for inclusion in this group.
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
    
}
