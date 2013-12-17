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
 */
public class Planet implements Comparable<Planet> {
    
    private String id = UUIDGenerator.generateUUID();
    private String handle = null;
    private String title = null;
    private String description = null;
    private Set groups = new TreeSet();
    
    
    public Planet() {
    }
    
    
    public Planet(String handle, String title, String desc) {
        this.title = title;
        this.handle = handle;
        this.description = desc;
    }
    
    
    /**
     * For comparing planets and sorting, ordered by Title.
     */
    public int compareTo(Planet other) {
        return getTitle().compareTo(other.getTitle());
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
    
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

    public Set getGroups() {
        return groups;
    }

    public void setGroups(Set groups) {
        this.groups = groups;
    }
    
}
