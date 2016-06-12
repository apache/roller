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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WebloggerFactory;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
public class Planet implements Comparable<Planet> {

    // attributes
    private String id;
    private String handle;
    private String title;
    private String description;

    // associations
    private Set<Subscription> subscriptions = new TreeSet<>();

    public Planet() {}
    
    public Planet(String handle, String title, String desc) {
        this.id = WebloggerCommon.generateUUID();
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

    @OneToMany(targetEntity=Subscription.class,
            cascade=CascadeType.ALL, mappedBy="planet")
    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }
    
    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
    
    @Transient
    public String getAbsoluteURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getPlanetURL(getHandle());
    }

    @Transient
    public LocalDateTime getLastUpdated() {
        LocalDateTime lastUpdated = LocalDateTime.now().minusYears(5);
        if (getSubscriptions() != null) {
            for (Subscription sub : getSubscriptions()) {
                if (sub.getLastUpdated() != null && sub.getLastUpdated().isAfter(lastUpdated)) {
                    lastUpdated = sub.getLastUpdated();
                }
            }
        }
        return lastUpdated;
    }

}
