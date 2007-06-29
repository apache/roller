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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.roller.util.UUIDGenerator;


/**
 * Planet Subscription.
 *
 * @hibernate.class lazy="true" table="rag_subscription"
 */
public class Subscription implements Serializable, Comparable {
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String title;
    private String author;
    private String feedUrl;
    private String siteUrl;
    private Date lastUpdated;
    private int inboundlinks = 0;
    private int inboundblogs = 0;
    
    // associations
    private Set groups = new HashSet();
    private List entries = new ArrayList();
    
    
    public Subscription() {}
    
    
    public int compareTo(Object o) {
        Subscription other = (Subscription) o;
        return getTitle().compareTo(other.getTitle());
    }
    
    public boolean equals(Object other) {
        
        if(this == other) return true;
        if(!(other instanceof Subscription)) return false;
        
        final Subscription that = (Subscription) other;
        return this.feedUrl.equals(that.getFeedURL());
    }
    
    public int hashCode() {
        return this.feedUrl.hashCode();
    }
    
    
    /**
     * @hibernate.id column="id" generator-class="assigned"
     */
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * @hibernate.property column="title" non-null="false" unique="false"
     */
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    
    /**
     * @hibernate.property column="author" non-null="false" unique="false"
     */
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    
    /**
     * @hibernate.property column="feed_url" non-null="true" unique="false"
     */
    public String getFeedURL() {
        return feedUrl;
    }
    
    public void setFeedURL(String feedUrl) {
        this.feedUrl = feedUrl;
    }
    
    
    /**
     * @hibernate.property column="site_url" non-null="false" unique="false"
     */
    public String getSiteURL() {
        return siteUrl;
    }
    
    public void setSiteURL(String siteUrl) {
        this.siteUrl = siteUrl;
    }
    
    
    /**
     * @hibernate.property column="last_updated" non-null="false" unique="false"
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    
    /**
     * @hibernate.property column="inbound_links" non-null="false" unique="false"
     */
    public int getInboundlinks() {
        return inboundlinks;
    }
    
    public void setInboundlinks(int inboundlinks) {
        this.inboundlinks = inboundlinks;
    }
    
    
    /**
     * @hibernate.property column="inbound_blogs" non-null="false" unique="false"
     */
    public int getInboundblogs() {
        return inboundblogs;
    }
    
    public void setInboundblogs(int inboundblogs) {
        this.inboundblogs = inboundblogs;
    }
    
    
    /**
     * @hibernate.set table="rag_group_subscription" lazy="true" cascade="none"
     * @hibernate.collection-key column="subscription_id"
     * @hibernate.collection-many-to-many column="group_id" class="org.apache.roller.planet.pojos.PlanetGroup"
     */
    public Set getGroups() {
        return groups;
    }
    
    // private because there is no need for people to do this
    private void setGroups(Set groups) {
        this.groups = groups;
    }
    
    
    /**
     * @hibernate.bag lazy="true" inverse="true" cascade="all"
     * @hibernate.collection-key column="subscription_id"
     * @hibernate.collection-one-to-many class="org.apache.roller.planet.pojos.SubscriptionEntry"
     */
    public List getEntries() {
        return entries;
    }
    
    // private because there is no need for people to do this
    private void setEntries(List entries) {
        this.entries = entries;
    }
    
    
    public void addEntry(SubscriptionEntry entry) {
        // bi-directional one-to-many
        entry.setSubscription(this);
        this.getEntries().add(entry);
    }
    
    public void addEntries(Collection newEntries) {
        // bi-directional one-to-many
        for (Iterator it = newEntries.iterator(); it.hasNext();) {
            SubscriptionEntry entry = (SubscriptionEntry) it.next();
            entry.setSubscription(this);
        }
        this.getEntries().addAll(newEntries);
    }
    
    
    // for backwards compatability?
    public String getName() {
        return title;
    }
    
    // for backwards compatability?
    public String getURL() {
        return siteUrl;
    }
    
}
