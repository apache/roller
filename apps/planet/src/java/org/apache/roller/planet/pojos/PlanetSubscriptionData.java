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


/**
 * @struts.form include-all="true"
 * @ejb:bean name="PlanetSubscriptionData"
 * @hibernate.class lazy="true" table="rag_subscription"
 */
public class PlanetSubscriptionData implements Serializable, Comparable
{
    /** Database ID */
    protected String id;
    
    /** Title of the blog or website */
    protected String title;
    
    /** Name of blog or website author */
    protected String author; 
    
    /** URL of the newsfeed */
    protected String feedUrl;
    
    /** URL of the blog or website */
    protected String siteUrl;
    
    /** Last update time of site */
    protected Date lastUpdated;
    
    /** Most recent entries from site (a set of EntityData objects) */
    protected List entries = new ArrayList(); 
    
    /** Inbound links according to Technorati */
    protected int inboundlinks = 0;

    /** Inbound blogs according to Technorati */
    protected int inboundblogs = 0;
    
    protected Set groups = new HashSet();
    
    //----------------------------------------------------------- persistent fields

    /** 
     * @hibernate.set table="rag_group_subscription" lazy="true" cascade="save-update"
     * @hibernate.collection-key column="subscription_id"
     * @hibernate.collection-many-to-many column="group_id" class="org.apache.roller.planet.pojos.PlanetGroupData"
     */
    public Set getGroups()
    {
        return groups;
    }
    public void setGroups(Set groups)
    {
        this.groups = groups;
    }

    /** 
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    /** 
     * @hibernate.property column="feed_url" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getFeedURL()
    {
        return feedUrl;
    }
    public void setFeedURL(String feedUrl)
    {
        this.feedUrl = feedUrl;
    }
    /** 
     * @hibernate.property column="last_updated" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public Date getLastUpdated()
    {
        return lastUpdated;
    }
    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }
    /** 
     * @hibernate.property column="site_url" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getSiteURL()
    {
        return siteUrl;
    }
    public void setSiteURL(String siteUrl)
    {
        this.siteUrl = siteUrl;
    }
    /** 
     * @hibernate.property column="title" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    /** 
     * @hibernate.property column="author" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getAuthor()
    {
        return author;
    }
    public void setAuthor(String author)
    {
        this.author = author;
    }
    /** 
     * @hibernate.property column="inbound_links" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public int getInboundlinks()
    {
        return inboundlinks;
    }
    public void setInboundlinks(int inboundlinks)
    {
        this.inboundlinks = inboundlinks;
    }
    /** 
     * @hibernate.property column="inbound_blogs" non-null="false" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public int getInboundblogs()
    {
        return inboundblogs;
    }
    public void setInboundblogs(int inboundblogs)
    {
        this.inboundblogs = inboundblogs;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     */
    public String getName() {
        return title;
    }
    public void setName(String name) {
        // no op to please XDoclet
    }
    /** 
     * @roller.wrapPojoMethod type="simple"
     */
    public String getURL() {
        return siteUrl;
    }
    public void setURL(String url) {
        // no op to please XDoclet
    }
    
    //-------------------------------------------------------------- implementation
    
    /**
     */
    public int compareTo(Object o)
    {
        PlanetSubscriptionData other = (PlanetSubscriptionData)o;
        return getFeedURL().compareTo(other.getFeedURL());
    }

    public boolean equals(Object other) {
        
        if(this == other) return true;
        if(!(other instanceof PlanetSubscriptionData)) return false;
        
        final PlanetSubscriptionData that = (PlanetSubscriptionData) other;
        return this.feedUrl.equals(that.getFeedURL());
    }
    
    public int hashCode() {
        return this.feedUrl.hashCode();
    }
    
    /** 
     * @hibernate.bag lazy="true" inverse="true" cascade="all" 
     * @hibernate.collection-key column="subscription_id"
     * @hibernate.collection-one-to-many class="org.apache.roller.planet.pojos.PlanetEntryData"
     */
    public List getEntries()
    {
        return entries;
    }

    private void setEntries(List entries)
    {
        this.entries = entries;
    }
    public void addEntry(PlanetEntryData entry)
    {
        // bi-directional one-to-many 
        entry.setSubscription(this);
        this.getEntries().add(entry);
    }
    
    public void addEntries(Collection newEntries)
    {
        // bi-directional one-to-many
        for (Iterator it = newEntries.iterator(); it.hasNext();) {
            PlanetEntryData entry = (PlanetEntryData) it.next();
            entry.setSubscription(this);
        }
        this.getEntries().addAll(newEntries);
    }
    
}
