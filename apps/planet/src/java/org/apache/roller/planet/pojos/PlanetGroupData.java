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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


/**
 * @struts.form include-all="true"
 * @ejb:bean name="PlanetGroupData"
 * @hibernate.class lazy="true" table="rag_group"
 */
public class PlanetGroupData implements Serializable
{
    transient private String[] catArray = null;

    /** Database ID */
    private String id = null;
    
    /** Unique handle by which group may be fetched */
    private String handle = null;
    
    /** Title of this group */
    private String title = null;
    
    /** Description of this group */
    private String description = null;
    
    /** Restrict group by this list of comma separated category names */
    private String categoryRestriction = null;
    
    /** Max number of entries to show in HTML representation of this group */
    private int maxPageEntries = 45;
    
    /** Max number of entries to show in feed representation of this group */
    private int maxFeedEntries = 45;
    
    /** Subscriptions in this group */
    private Set subscriptions = new HashSet();

    private PlanetData planet = null;
    
    
    public PlanetGroupData() {}
    
    public PlanetGroupData(PlanetData planet, String handle, String title, String desc) {
        this.planet = planet;
        this.handle = handle;
        this.title = title;
        this.description = desc;
    }
    
    
    //------------------------------------------------------- persistent fields

    /** 
     * @ejb:persistent-field 
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
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
     * @hibernate.set table="rag_group_subscription" lazy="true" invert="true" cascade="none"
     * @hibernate.collection-key column="group_id"
     * @hibernate.collection-many-to-many column="subscription_id" class="org.apache.roller.planet.pojos.PlanetSubscriptionData"
     */
    public Set getSubscriptions()
    {
        return subscriptions;
    }
    public void setSubscriptions(Set subscriptions)
    {
        this.subscriptions = subscriptions;
    }
    /** 
     * @hibernate.property column="cat_restriction" non-null="false" unique="false"
     */
    public String getCategoryRestriction()
    {
        return categoryRestriction;
    }
    public void setCategoryRestriction(String categoryRestriction)
    {
        this.categoryRestriction = categoryRestriction;
        catArray = null;
    }
    /** 
     * @hibernate.property column="description" non-null="false" unique="false"
     */
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    /** 
     * @hibernate.property column="handle" non-null="false" unique="false"
     */
    public String getHandle()
    {
        return handle;
    }
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    /** 
     * @hibernate.property column="max_feed_entries" non-null="false" unique="false"
     */
    public int getMaxFeedEntries()
    {
        return maxFeedEntries;
    }
    public void setMaxFeedEntries(int maxFeedEntries)
    {
        this.maxFeedEntries = maxFeedEntries;
    }
    /** 
     * @hibernate.property column="max_page_entries" non-null="false" unique="false"
     */
    public int getMaxPageEntries()
    {
        return maxPageEntries;
    }
    public void setMaxPageEntries(int maxPageEntries)
    {
        this.maxPageEntries = maxPageEntries;
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
     * @hibernate.many-to-one column="planet_id" cascade="none" non-null="false"
     */
    public PlanetData getPlanet() {
        return planet;
    }

    public void setPlanet(PlanetData planet) {
        this.planet = planet;
    }
    
    //--------------------------------------------------------------- app logic
    
    
    /**
     * Returns true if entry is qualified for inclusion in this group.
     */
    public boolean qualified(PlanetEntryData entry)
    {
        String[] cats = getCategoryRestrictionAsArray();
        if (cats == null || cats.length == 0) return true;
        for (int i=0; i<cats.length; i++) 
        {
            if (entry.inCategory(cats[i])) return true;
        }
        return  false;
    }
    
    //------------------------------------------------------------- convenience

    private String[] getCategoryRestrictionAsArray()
    {
        if (catArray == null && categoryRestriction != null)
        {
            StringTokenizer toker = new StringTokenizer(categoryRestriction,",");
            catArray = new String[toker.countTokens()];
            int i = 0;
    
            while (toker.hasMoreTokens())
            {
                catArray[i++] = toker.nextToken();
            }
        }
        return catArray;
    }
    /** no-op to please XDoclet generated form */
    private void setCategoryRestrictionAsArray(String[] ignored)
    {
    }
    
}
