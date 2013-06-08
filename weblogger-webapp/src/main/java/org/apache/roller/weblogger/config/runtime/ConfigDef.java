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
/*
 * ConfigDef.java
 *
 * Created on June 4, 2005, 1:10 PM
 */

package org.apache.roller.weblogger.config.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a logic grouping of runtime configuration properties.
 * Each ConfigDef may contain 0 or more DisplayGroups.
 *
 * @author Allen Gilliland
 */
public class ConfigDef {
    
    private List displayGroups = null;
    private String name = null;
    
    
    public ConfigDef() {
        this.displayGroups = new ArrayList();
    }

    public ConfigDef(List displaygroups) {
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
    
    public List getDisplayGroups() {
        return displayGroups;
    }

    public void setDisplayGroups(List displayGroups) {
        this.displayGroups = displayGroups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
