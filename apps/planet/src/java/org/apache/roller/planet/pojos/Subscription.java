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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.roller.util.UUIDGenerator;


/**
 * Planet Subscription.
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
    private Set entries = new HashSet();
    
    
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
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("{");
        buf.append(feedUrl).append(", ");
        buf.append(siteUrl).append(", ");
        buf.append(title).append(", ");
        buf.append(author).append(", ");
        buf.append(lastUpdated);
        buf.append("}");
        
        return buf.toString();
        
    }
    
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    
    public String getFeedURL() {
        return feedUrl;
    }
    
    public void setFeedURL(String feedUrl) {
        this.feedUrl = feedUrl;
    }
    
    
    public String getSiteURL() {
        return siteUrl;
    }
    
    public void setSiteURL(String siteUrl) {
        this.siteUrl = siteUrl;
    }
    
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    
    public int getInboundlinks() {
        return inboundlinks;
    }
    
    public void setInboundlinks(int inboundlinks) {
        this.inboundlinks = inboundlinks;
    }
    
    
    public int getInboundblogs() {
        return inboundblogs;
    }
    
    public void setInboundblogs(int inboundblogs) {
        this.inboundblogs = inboundblogs;
    }
    
    
    public Set getGroups() {
        return groups;
    }
    
    // private because there is no need for people to do this
    private void setGroups(Set groups) {
        this.groups = groups;
    }
    
    
    public Set getEntries() {
        return entries;
    }
    
    // private because there is no need for people to do this
    private void setEntries(Set entries) {
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
