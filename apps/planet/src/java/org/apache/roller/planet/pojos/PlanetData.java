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

import java.util.HashSet;
import java.util.Set;


/**
 * A Roller "Planet".
 * 
 * @hibernate.class lazy="true" table="rag_planet"
 */
public class PlanetData {
    
    private String id = null;
    private String handle = null;
    private String title = null;
    private String description = null;
    private Set groups = new HashSet();
    
    
    public PlanetData() {
    }
    
    
    public PlanetData(String name, String handle) {
        this.title = name;
        this.handle = handle;
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
     * @hibernate.set lazy="true" invert="true" cascade="all"
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
