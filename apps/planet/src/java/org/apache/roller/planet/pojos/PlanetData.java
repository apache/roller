/*
 * PlanetData.java
 *
 * Created on December 13, 2006, 5:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
    private String name = null;
    private String handle = null;
    private Set groups = new HashSet();
    
    
    public PlanetData() {
    }
    
    
    public PlanetData(String name, String handle) {
        this.name = name;
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
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
