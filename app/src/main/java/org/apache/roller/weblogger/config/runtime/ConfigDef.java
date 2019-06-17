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
package org.apache.roller.weblogger.config.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a logic grouping of runtime configuration properties.
 * Each ConfigDef may contain 0 or more DisplayGroups.
 *
 * Created on June 4, 2005, 1:10 PM
 * @author Allen Gilliland
 */
public class ConfigDef {
    
    private List<DisplayGroup> displayGroups = null;
    private String name = null;

    Map<String, PropertyDef> propertyDefs = null;
    
    
    public ConfigDef() {
        this.displayGroups = new ArrayList<DisplayGroup>();
    }

    public ConfigDef(List<DisplayGroup> displaygroups) {
        this.displayGroups = displaygroups;
    }

    public boolean addDisplayGroup(DisplayGroup group) {
        return this.displayGroups.add(group);
    }
    
    public boolean removeDisplayGroup(DisplayGroup group) {
        return this.displayGroups.remove(group);
    }


    public String toString() {
        return name;
    }
    
    public List<DisplayGroup> getDisplayGroups() {
        return displayGroups;
    }

    public void setDisplayGroups(List<DisplayGroup> displayGroups) {
        this.displayGroups = displayGroups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PropertyDef getPropertyDef( String name ) {
        if ( propertyDefs == null ) {
            propertyDefs = new HashMap<>();
            for (DisplayGroup displayGroup : getDisplayGroups()) {
                for (PropertyDef propertyDef : displayGroup.getPropertyDefs()) {
                    propertyDefs.put( propertyDef.getName(), propertyDef );
                }
            }
        }
        return propertyDefs.get( name );
    }
}
