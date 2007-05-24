/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.planet.pojos;

import java.util.Set;
import org.apache.roller.util.UUIDGenerator;
import java.util.TreeSet;


/**
 * A Roller "Planet".
 * 
 * @hibernate.class lazy="true" table="rag_planet"
 */
public class PlanetData implements Comparable {
    
    private String id = UUIDGenerator.generateUUID();
    private String handle = null;
    private String title = null;
    private String description = null;
    private Set groups = new TreeSet();
    
    
    public PlanetData() {
    }
    
    
    public PlanetData(String handle, String title, String desc) {
        this.title = title;
        this.handle = handle;
        this.description = desc;
    }
    
    
    /**
     * For comparing planets and sorting, ordered by Title.
     */
    public int compareTo(Object o) {
        PlanetData other = (PlanetData) o;
        return getTitle().compareTo(other.getTitle());
    }
    
    
    /**
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * @hibernate.property column="handle" non-null="true" unique="true"
     */
    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
    
       
    /**
     * @hibernate.property column="title" non-null="true" unique="false"
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    
    /**
     * @hibernate.property column="description" non-null="false" unique="false"
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** 
     * @hibernate.set lazy="true" inverse="true" cascade="all" sort="natural"
     * @hibernate.collection-key column="planet_id"
     * @hibernate.collection-one-to-many class="org.apache.roller.planet.pojos.PlanetGroupData"
     */
    public Set getGroups() {
        return groups;
    }

    public void setGroups(Set groups) {
        this.groups = groups;
    }
    
}
