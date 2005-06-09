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
package org.roller.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;



/**
 * @struts.form include-all="true"
 * @ejb:bean name="PlanetSubscriptionData"
 * @hibernate.class table="rag_subscription"
 */
public class PlanetSubscriptionData extends PersistentObject 
    implements Serializable, Comparable
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
    
    protected List groupAssocs = new ArrayList();
    
    //----------------------------------------------------------- persistent fields

    /** 
     * @hibernate.bag lazy="true" inverse="true" cascade="delete" 
     * @hibernate.collection-key column="subscription_id"
     * @hibernate.collection-one-to-many 
     *    class="org.roller.pojos.PlanetGroupSubscriptionAssoc"
     */
    public List getGroupSubscriptionAssocs()
    {
        return groupAssocs;
    }
    public void setGroupSubscriptionAssocs(List groupAssocs)
    {
        this.groupAssocs = groupAssocs;
    }

    /** 
     * @hibernate.id column="id" type="string" 
     *     generator-class="uuid.hex" unsaved-value="null"
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
     * @hibernate.bag lazy="true" inverse="true" cascade="all" 
     * @hibernate.collection-key column="subscription_id"
     * @hibernate.collection-one-to-many 
     *    class="org.roller.pojos.PlanetEntryData"
     */
    public List getEntries()
    {
        return entries;
    }
    public void setEntries(List entries)
    {
        this.entries = entries;
    }
    /** 
     * @hibernate.property column="feed_url" non-null="true" unique="false"
     */
    public String getFeedUrl()
    {
        return feedUrl;
    }
    public void setFeedUrl(String feedUrl)
    {
        this.feedUrl = feedUrl;
    }
    /** 
     * @hibernate.property column="last_updated" non-null="false" unique="false"
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
     */
    public String getSiteUrl()
    {
        return siteUrl;
    }
    public void setSiteUrl(String siteUrl)
    {
        this.siteUrl = siteUrl;
    }
    /** 
     * @hibernate.property column="title" non-null="false" unique="false"
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
     */
    public int getInboundblogs()
    {
        return inboundblogs;
    }
    public void setInboundblogs(int inboundblogs)
    {
        this.inboundblogs = inboundblogs;
    }

    //-------------------------------------------------------------- implementation
   
    /**
     */
    public void setData(PersistentObject vo)
    {
        // TODO Auto-generated method stub        
    }
    /**
     */
    public int compareTo(Object o)
    {
        PlanetSubscriptionData other = (PlanetSubscriptionData)o;
        return getFeedUrl().compareTo(other.getFeedUrl());
    }

    public void addEntry(PlanetEntryData entry)
    {
        entries.add(entry);
    }
    
    public void addEntries(Collection newEntries)
    {
        entries.addAll(newEntries);
    }
    
    public void purgeEntries()
    {
        entries.clear();
    }
}
