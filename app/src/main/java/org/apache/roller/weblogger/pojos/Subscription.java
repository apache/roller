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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.roller.weblogger.WebloggerCommon;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * Planet Subscription.
 */
@Entity
@Table(name="planet_subscription")
@NamedQueries({
        @NamedQuery(name="Subscription.getAll",
                query="SELECT p FROM Subscription p"),
        @NamedQuery(name="Subscription.getAllOrderByFeedURL",
                query="SELECT p FROM Subscription p ORDER BY p.feedURL DESC"),
        @NamedQuery(name="Subscription.getByPlanetAndFeedURL",
                query="SELECT s FROM Subscription s WHERE s.planet = ?1 AND s.feedURL = ?2")
})
public class Subscription implements Comparable<Subscription> {
    
    // attributes
    private String id = WebloggerCommon.generateUUID();
    private String title;
    private String feedURL;
    private String siteURL;
    private Date lastUpdated;

    // associations
    private Planet planet;
    private Set<SubscriptionEntry> entries = new HashSet<>();
    
    
    public Subscription() {}
    
    @Id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Basic(optional=false)
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name="feed_url", nullable=false)
    public String getFeedURL() {
        return feedURL;
    }
    
    public void setFeedURL(String feedUrl) {
        this.feedURL = feedUrl;
    }

    @Column(name="site_url")
    public String getSiteURL() {
        return siteURL;
    }
    
    public void setSiteURL(String siteUrl) {
        this.siteURL = siteUrl;
    }


    @Column(name="last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @ManyToOne
    @JoinColumn(name="planetid", nullable=false)
    @JsonIgnore
    public Planet getPlanet() {
        return planet;
    }
    
    public void setPlanet(Planet planet) {
        this.planet = planet;
    }


    @OneToMany(targetEntity=SubscriptionEntry.class,
            cascade=CascadeType.ALL, mappedBy="subscription")
    @JsonIgnore
    public Set<SubscriptionEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(Set<SubscriptionEntry> entries) {
        this.entries = entries;
    }
    
    
    /**
     * Add a SubscriptionEntry to this Subscription.
     */
    public void addEntry(SubscriptionEntry entry) {
        // bi-directional one-to-many
        entry.setSubscription(this);
        this.getEntries().add(entry);
    }
    
    /**
     * Add a collection of SubscriptionEntry to this Subscription.
     */
    public void addEntries(Collection<SubscriptionEntry> newEntries) {
        // bi-directional one-to-many
        for (SubscriptionEntry entry : newEntries) {
            entry.setSubscription(this);
        }
        this.getEntries().addAll(newEntries);
    }

    /**
     * Compares subscriptions based on concatenation of title and feed URL.
     * This ensures that feeds are sorted by title, but that identical titles
     * don't make feeds equal.
     */
    public int compareTo(Subscription other) {
        String otherString = other.getTitle() + other.getFeedURL();
        String thisString = getTitle() + getFeedURL();
        return thisString.compareTo(otherString);
    }

    /**
     * Determines if subscriptions are equal by comparing feed URLs.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Subscription)) {
            return false;
        }
        final Subscription that = (Subscription) other;
        return this.feedURL.equals(that.getFeedURL());
    }

    public int hashCode() {
        return this.feedURL.hashCode();
    }

    public String toString() {
        String str = "{ Feed URL:" + getFeedURL();
        str += ", Site URL:" + getSiteURL();
        str += ", Title:" + getTitle() + "}";
        return str;
    }

}
